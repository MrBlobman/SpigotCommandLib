/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 MrBlobman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.github.mrblobman.spigotcommandlib.registry;

import io.github.mrblobman.spigotcommandlib.CommandException;
import io.github.mrblobman.spigotcommandlib.CommandHandle;
import io.github.mrblobman.spigotcommandlib.CommandHandler;
import io.github.mrblobman.spigotcommandlib.args.ArgDescription;
import io.github.mrblobman.spigotcommandlib.args.Argument;
import io.github.mrblobman.spigotcommandlib.args.ArgumentFormatter;
import io.github.mrblobman.spigotcommandlib.args.FormatterMapping;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

public class CommandRegistry {
    private Map<String, SubCommand> baseCommands = new HashMap<>();
    private Map<SubCommand, HandleInvoker> invokers = new HashMap<>();
    private CommandMap bukkitCommandMap;
    private CommandLib lib;

    CommandRegistry(CommandLib lib) throws InstantiationException {
        Method commandMap;
        try {
            commandMap = Bukkit.getServer().getClass().getMethod("getCommandMap");
        } catch (NoSuchMethodException | SecurityException e) {
            throw new InstantiationException("Could not grab the command map from the bukkit server. " + e.getLocalizedMessage());
        }
        Object rawMap;
        try {
            rawMap = commandMap.invoke(Bukkit.getServer());
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new InstantiationException("Could not grab the command map from the bukkit server. " + e.getLocalizedMessage());
        }
        if (rawMap == null || !(rawMap instanceof CommandMap)) {
            throw new InstantiationException("Could not grab the command map from the bukkit server.");
        }
        this.bukkitCommandMap = (CommandMap) rawMap;
        this.lib = lib;
    }

    public void register(CommandHandler commandHandler) {
        for (Method method : commandHandler.getClass().getDeclaredMethods()) {
            CommandHandle handlerAnnotation = method.getAnnotation(CommandHandle.class);
            //Move on, this method isn't annotated
            if (handlerAnnotation == null) continue;
            //Check that min requirements are met
            //JDK8+ if (method.getParameterCount() < 1) {
            if (method.getParameterTypes().length < 1) {
                lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Not enough parameters. Requires first param as sender.");
                continue;
            }
            if (!registerSingleMethod(method, commandHandler, handlerAnnotation)) continue;
            lib.getHook().getLogger().log(Level.INFO, "Successfully registered " + method.getName() + " in " + commandHandler.getClass().getSimpleName() + " for /" + Arrays.toString(handlerAnnotation.command()).replaceAll("[,\\[\\]]", ""));
        }
    }

    private boolean registerSingleMethod(Method method, CommandHandler commandHandler, CommandHandle handlerAnnotation) {
        //JDK8+ Parameter[] methodParams = method.getParameters();
        Class<?>[] methodParams = method.getParameterTypes();
        //JDK7 annotation workaround
        ArgDescription[] paramArgDesc = getArgDescs(method);
        if (paramArgDesc[0] != null) {
            lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Sender param cannot be annotated as an argument.");
        }
        //Check that optional args are not followed by varArgs or required args
        if (!isArgFlowValid(paramArgDesc, method.isVarArgs())) {
            lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Illegal argument flow. Optional arguments cannot be followed by required arguments.");
            return false;
        }

        //Build parsing data arrays
        Argument<?>[] arguments = new Argument[methodParams.length - 1];
        for (int i = 1; i < methodParams.length - (method.isVarArgs() ? 1 : 0); i++) {
            //JDK8+ Class<?> paramType = methodParams[i].getType();
            Class<?> paramType = methodParams[i];
            ArgumentFormatter<?> formatter = FormatterMapping.lookup(paramType);
            if (formatter == null) {
                lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Unknown parameter parse type (" + paramType.getName() + "). Accepted types are String, Integer, Long, Short, Double, Float and org.bukkit.Color.");
                return false;
            }
            ArgDescription desc = paramArgDesc[i];
            int type = (desc != null && desc.optional() ? Argument.OPTIONAL : Argument.REQUIRED);
            String name;
            if (desc != null && !desc.name().equals("")) {
                name = desc.name();
            } else {
                //JDK8+ name = methodParams[i].isNamePresent() ? methodParams[i].getName() : "arg" + (i - 1);
                name = "arg" + (i - 1);
            }
            if (desc != null && desc.description().length != 0) {
                arguments[i - 1] = new Argument<>(formatter, paramType, name, desc.description(), type);
            } else {
                arguments[i - 1] = new Argument<>(formatter, paramType, name, type);
            }
        }
        if (method.isVarArgs()) {
            int lastIndex = methodParams.length - 1;
            //We need to handle the last arg differently because it is a var arg
            Class<?> paramType = methodParams[lastIndex];
            ArgumentFormatter<?> formatter = FormatterMapping.lookpArray(paramType);
            if (formatter == null) {
                lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Unknown parameter parse type (" + paramType.getName() + "). Accepted types are String, Integer, Long, Short, Double, Float and org.bukkit.Color.");
                return false;
            }
            //JDK8+ Uses Parameter ArgDescription desc = methodParams[lastIndex].getAnnotation(ArgDescription.class);
            ArgDescription desc = paramArgDesc[lastIndex];
            String name;
            if (desc != null && !desc.name().equals("")) {
                name = desc.name();
            } else {
                //JDK8+ name = methodParams[lastIndex].isNamePresent() ? methodParams[lastIndex].getName() : "arg" + (lastIndex);
                name = "arg" + (lastIndex);
            }
            if (desc != null && desc.description().length != 0) {
                arguments[lastIndex - 1] = new Argument<>(formatter, paramType, name, desc.description(), Argument.VAR_ARGS);
            } else {
                arguments[lastIndex - 1] = new Argument<>(formatter, paramType, name, Argument.VAR_ARGS);
            }
        }
        //Verify the sender type is valid
        Class<?> senderType = methodParams[0];
        if (!CommandSender.class.isAssignableFrom(senderType)) {
            lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Invalid sender type " + senderType.getSimpleName() + ". Must be accessible from org.bukkit.CommandSender.");
            return false;
        }
        //Register the sub command
        SubCommand cmd = addSubCommand(handlerAnnotation.command(), handlerAnnotation.permission());
        if (cmd == null) {
            lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Invalid sub command.");
            return false;
        }
        //Finally create the invoker
        this.invokers.put(cmd, new HandleInvoker(cmd, handlerAnnotation.description(), commandHandler, method, senderType, arguments));
        return true;
    }

    private ArgDescription[] getArgDescs(Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        ArgDescription[] argDesc = new ArgDescription[annotations.length];
        int index = 0;
        for (Annotation[] paramAnnotations : annotations) {
            for (Annotation annotation : paramAnnotations) {
                if (annotation.annotationType().equals(ArgDescription.class)) {
                    argDesc[index] = (ArgDescription) annotation;
                    break;
                }
            }
            index++;
        }
        return argDesc;
    }

    private boolean isArgFlowValid(ArgDescription[] paramArgDesc, boolean isVarargs) {
        boolean canBeRequired = true;
        for (int i = 0; i < paramArgDesc.length - (isVarargs ? 1 : 0); i++) {
            if (paramArgDesc[i] != null) {
                if (paramArgDesc[i].optional()) {
                    //We hit an optional arg so there can no long be any req args
                    canBeRequired = false;
                } else if (!canBeRequired) {
                    //We found a required arg but they are not allowed
                    return false;
                }
            } else if (!canBeRequired) {
                //We found a required arg but they are not allowed
                return false;
            }
        }
        return true;
    }

    protected SubCommand getSubCommand(String[] command) {
        if (command == null || command.length < 1) return null;
        SubCommand cmd = this.getBaseCommand(command[0]);
        if (cmd == null) return null;
        for (int i = 1; i < command.length; i++) {
            cmd = cmd.getSubCommand(command[i]);
            if (cmd == null) return null;
        }
        return cmd;
    }

    private SubCommand getBaseCommand(String baseCommand) {
        for (String baseAlias : baseCommand.split("\\|")) {
            if (this.baseCommands.containsKey(baseAlias.toLowerCase())) {
                return this.baseCommands.get(baseAlias.toLowerCase());
            }
        }
        return null;
    }

    protected SubCommand addSubCommand(String[] command, String permission) {
        if (command == null || command.length < 1) return null;
        SubCommand superCmd = addBaseCommand(command[0], permission);
        for (int i = 1; i < command.length; i++) {
            String[] subCommandAliases = command[i].split("\\|");
            SubCommand subCmd;
            if (subCommandAliases.length > 1) {
                subCmd = new SubCommand(subCommandAliases[0], Arrays.copyOfRange(subCommandAliases, 1, subCommandAliases.length), permission, superCmd);
            } else {
                subCmd = new SubCommand(subCommandAliases[0], new String[0], permission, superCmd);
            }
            subCmd.addPermission(permission);
            superCmd.addSubCommand(subCmd);
            superCmd = subCmd;
        }
        return superCmd;
    }

    private SubCommand addBaseCommand(String baseCommand, String permission) {
        String[] baseAliases = baseCommand.split("\\|");
        SubCommand base = getBaseCommand(baseAliases[0]);
        if (base != null) {
            base.addPermission(permission);
            for (int i = 1; i < baseAliases.length; i++)
                if (!base.getAliases().contains(baseAliases[i]))
                    base.getAliases().add(baseAliases[i]);
            return base;
        }
        if (baseAliases.length > 1) {
            base = new SubCommand(baseAliases[0], Arrays.copyOfRange(baseAliases, 1, baseAliases.length), permission, null);
        } else {
            base = new SubCommand(baseAliases[0], new String[0], permission, null);
        }
        for (String alias : baseAliases)
            this.baseCommands.put(alias.toLowerCase(), base);
        this.bukkitCommandMap.register(base.getName(), this.lib.getHook().getName().toLowerCase(),
                new BaseCommand(this.lib, base.getName(), "/" + base.getName(), "/" + base.getName(), base.getAliases()));
        return base;
    }

    /**
     * Usage designed for tab complete.
     * @param enteredCommand the partial command entered
     * @return a List containing the possible sub commands that may follow, will never return null
     */
    public List<String> getPossibleSubCommands(String[] enteredCommand) {
        SubCommand sub = this.getSubCommand(enteredCommand);
        if (sub == null) {
            //Try to partially fix the last arg
            sub = this.getSubCommand(Arrays.copyOfRange(enteredCommand, 0, enteredCommand.length - 1));
            if (sub == null) {
                //Nope they are lost
                return new ArrayList<>();
            }
            /*return sub.getSubCommands().stream()
                    .filter(possibleArg -> possibleArg.startsWith(enteredCommand[enteredCommand.length - 1]))
                    .collect(Collectors.toList());
              JDK8+ */
            List<String> possibleCmds = new ArrayList<>();
            for (String subCmd : sub.getSubCommands()) {
                if (subCmd.startsWith(enteredCommand[enteredCommand.length - 1])) {
                    possibleCmds.add(subCmd);
                }
            }
            return possibleCmds;
        } else {
            return sub.getSubCommands();
        }
    }

    /**
     * Handle the given command.
     * @param sender the {@link CommandSender} that sent the command
     * @param command the command split into parts. The command followed by each argument
     * @return true iff the base command is registered with this registry and an attempt to execute was preformed
     * @throws CommandException if an error occurs while handling the command.
     */
    protected boolean handleCommand(CommandSender sender, String[] command) throws CommandException {
        if (command == null || command.length < 1) throw new IllegalArgumentException("command was empty");
        SubCommand cmd = this.getBaseCommand(command[0]);
        if (cmd == null) {
            return false;
        }
        SubCommand next;
        int i;
        for (i = 1; i < command.length; i++) {
            next = cmd.getSubCommand(command[i]);
            if (next == null) break; //We went as far as we could go
            else cmd = next;
        }
        if (!cmd.canExecute(sender)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute " + cmd.toString() + ".");
            return true;
        }
        //Invoke the command
        HandleInvoker invoker = this.invokers.get(cmd);
        if (invoker == null) return false;
        //i is index of first arg
        try {
            invoker.invoke(sender, i < command.length ? Arrays.copyOfRange(command, i, command.length) : new String[0]);
        } catch (Exception e) {
            throw new CommandException(e);
        }
        return true;
    }

    public void displayHelp(CommandSender sender, String[] cmdGiven) {
        if (cmdGiven.length > 0) {
            SubCommand subCommand = getSubCommand(cmdGiven);
            if (subCommand == null) {
                sender.sendMessage(ChatColor.YELLOW + "No commands match the query.");
                return;
            }
            String commandString = subCommand.toString();
            boolean[] sentSomething = {false};
            /*this.invokers.forEach((cmd, invoker) -> {
                if (cmd.toString().startsWith(commandString) && cmd.canExecute(sender)) {
                    invoker.sendDescription(sender);
                    sentSomething[0] = true;
                }
            });
            JDK8+ */
            for (Map.Entry<SubCommand, HandleInvoker> entry : this.invokers.entrySet()) {
                if (entry.getKey().toString().startsWith(commandString) && entry.getKey().canExecute(sender)) {
                    entry.getValue().sendDescription(sender);
                    sentSomething[0] = true;
                }
            }
            if (!sentSomething[0])
                sender.sendMessage(ChatColor.RED + "No commands you are allowed to execute match the query.");
        } else {
            boolean[] sentSomething = {false};
            /*this.invokers.forEach((cmd, invoker) -> {
                if (cmd.canExecute(sender)) {
                    invoker.sendDescription(sender);
                    sentSomething[0] = true;
                }
            });
            JDK8+ */
            for (Map.Entry<SubCommand, HandleInvoker> entry : this.invokers.entrySet()) {
                if (entry.getKey().canExecute(sender)) {
                    entry.getValue().sendDescription(sender);
                    sentSomething[0] = true;
                }
            }
            if (!sentSomething[0])
                sender.sendMessage(ChatColor.RED + "No commands you are allowed to execute match the query.");
        }
    }


}
