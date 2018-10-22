/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 MrBlobman
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

import io.github.mrblobman.spigotcommandlib.*;
import io.github.mrblobman.spigotcommandlib.args.*;
import io.github.mrblobman.spigotcommandlib.context.SimpleContextProvider;
import io.github.mrblobman.spigotcommandlib.invocation.CommandMethodHandle;
import io.github.mrblobman.spigotcommandlib.util.ChatUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandRegistry implements Listener {
    private static final String[] EMPTY_STR_ARRAY = new String[0];

    private static final int CTX_INSENSITIVE_SENDER_PARAM_IDX = 0;

    private static final int CTX_SENSITIVE_CONTEXT_PARAM_IDX = 0;
    private static final int CTX_SENSITIVE_SENDER_PARAM_IDX = 1;

    private static final BitSet CTX_INSENSITIVE_IMPLICIT_PARAMS_IDX = new BitSet();

    static {
        CTX_INSENSITIVE_IMPLICIT_PARAMS_IDX.set(CTX_INSENSITIVE_SENDER_PARAM_IDX);
    }

    private static final BitSet CTX_SENSITIVE_IMPLICIT_PARAMS_IDX = new BitSet();

    static {
        CTX_SENSITIVE_IMPLICIT_PARAMS_IDX.set(CTX_SENSITIVE_CONTEXT_PARAM_IDX);
        CTX_SENSITIVE_IMPLICIT_PARAMS_IDX.set(CTX_SENSITIVE_SENDER_PARAM_IDX);
    }

    private final Map<String, SubCommand> baseCommands = new HashMap<>();
    private final Map<SubCommand, CommandExecutor> executors = new HashMap<>();
    private final BundleCleaner bundleCleaner;
    private final CommandMap bukkitCommandMap;
    private final CommandLib lib;

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
        if (!(rawMap instanceof CommandMap)) {
            throw new InstantiationException("Could not grab the command map from the bukkit server.");
        }
        this.bundleCleaner = new BundleCleaner(lib.getHook());
        this.bukkitCommandMap = (CommandMap) rawMap;
        this.lib = lib;
    }

    public void register(CommandHandler commandHandler) throws HandlerCompilationException {
        for (Method method : commandHandler.getClass().getDeclaredMethods()) {
            CommandHandle handlerAnnotation = method.getAnnotation(CommandHandle.class);
            //Move on, this method isn't annotated
            if (handlerAnnotation == null) continue;

            String[] command = buildCommand(EMPTY_STR_ARRAY, handlerAnnotation.command(), method.getName());
            CommandExecutor executor = buildContextInsensitiveCommand(method, commandHandler, command, handlerAnnotation.permission(),
                    ChatUtils.splitAndColorLines(handlerAnnotation.description()));

            this.executors.put(executor.getTrigger(), executor);

            lib.getHook().getLogger().log(Level.INFO, "Successfully registered " + method.getName() + " in " + commandHandler.getClass().getSimpleName() + " for /" + Arrays.toString(handlerAnnotation.command()).replaceAll("[,\\[\\]]", ""));
        }
    }

    public void register(SubCommandHandler commandHandler, String... subCommandPrefix) throws HandlerCompilationException {
        register(commandHandler, CommandLib.NO_PERMISSION, subCommandPrefix);
    }

    public void register(SubCommandHandler commandHandler, String permission, String... subCommandPrefix) throws HandlerCompilationException {
        for (Method method : commandHandler.getClass().getDeclaredMethods()) {
            SubCommandHandle handlerAnnotation = method.getAnnotation(SubCommandHandle.class);
            //Move on, this method isn't annotated
            if (handlerAnnotation == null) continue;

            String[] command = buildCommand(subCommandPrefix, handlerAnnotation.command(), method.getName());
            CommandExecutor executor = buildContextInsensitiveCommand(method, commandHandler, command, handlerAnnotation.permission().isEmpty() ? permission : handlerAnnotation.permission(),
                    ChatUtils.splitAndColorLines(handlerAnnotation.description()));

            this.executors.put(executor.getTrigger(), executor);

            lib.getHook().getLogger().log(Level.INFO, "Successfully registered " + method.getName() + " in " + commandHandler.getClass().getSimpleName() + " for /" + Arrays.toString(command).replaceAll("[,\\[\\]]", ""));
        }
    }

    public <T extends FragmentExecutionContext> void register(FragmentedCommandHandler<T> commandHandler, String permission, long timeout, FragmentedCommandContextSupplier<T> supplier, String... subCommandPrefix) throws HandlerCompilationException {
        Class<?> contextType = supplier.get().getClass(); // A small hack to get the generic type of the handler.
        Map<SubCommand, Map<Integer, CommandMethodHandle>> commandHandles = new HashMap<>();

        for (Method method : commandHandler.getClass().getDeclaredMethods()) {
            FragmentedCommandHandle handlerAnnotation = method.getAnnotation(FragmentedCommandHandle.class);
            // Move on, this method isn't annotated
            if (handlerAnnotation == null) continue;

            String[] command = buildCommand(subCommandPrefix, handlerAnnotation.command(), method.getName());

            CommandMethodHandle handle = buildFragmentHandle(method, commandHandler, contextType, command, handlerAnnotation.permission().isEmpty() ? permission : handlerAnnotation.permission(),
                    ChatUtils.splitAndColorLines(handlerAnnotation.description()));

            SubCommand trigger = this.getSubCommand(command);
            Map<Integer, CommandMethodHandle> variants = commandHandles.computeIfAbsent(trigger, t -> new HashMap<>());

            if (variants.containsKey(handlerAnnotation.state()))
                lib.getHook().getLogger().log(Level.WARNING, "Overwriting handle for " + trigger.toString() + " with " + method.getName() + " because both fragments have the same sub command and state.");

            variants.put(handlerAnnotation.state(), handle);
            lib.getHook().getLogger().log(Level.INFO, "Registering fragment " + method.getName() + " in " + commandHandler.getClass().getSimpleName() + " for " + trigger.toString() + " when in state " + handlerAnnotation.state());
        }

        // All the commands need to share the context provider
        SimpleContextProvider<T> provider = new SimpleContextProvider<>(supplier::get);

        commandHandles.forEach((trigger, variants) -> {
            CommandExecutor executor = new ContextSensitiveCommand<>(trigger, variants, provider);

            this.executors.put(trigger, executor);

            lib.getHook().getLogger().log(Level.INFO, "Successfully registered variants for " + trigger.toString());
        });
    }

    private CommandMethodHandle buildFragmentHandle(Method method, Object commandHandler, Class<?> contextType, String[] command, String permission, List<String> desc) throws HandlerCompilationException {
        MethodDescriptor methodDesc = MethodDescriptor.fromMethod(method);

        CommandParameters cmdParams = this.compileParams(methodDesc, CTX_SENSITIVE_IMPLICIT_PARAMS_IDX);
        Parameter ctxParam = checkImplicitParam(methodDesc, "context", CTX_SENSITIVE_CONTEXT_PARAM_IDX, contextType);
        Parameter senderParam = checkImplicitParam(methodDesc, "sender", CTX_SENSITIVE_SENDER_PARAM_IDX, Player.class);

        SubCommand cmd = this.addSubCommand(command, permission);
        if (cmd == null)
            throw new HandlerCompilationException(methodDesc, "Invalid sub command %s.", Arrays.toString(command));

        MethodHandle methodHandle;
        try {
            method.setAccessible(true);
            methodHandle = MethodHandles.lookup()
                    .unreflect(method)
                    .bindTo(commandHandler);
        } catch (IllegalAccessException e) {
            throw new HandlerCompilationException(methodDesc, "Error accessing method.", e);
        }

        return new CommandMethodHandle(senderParam.getType(), cmdParams, desc, methodHandle);
    }

    private CommandExecutor buildContextInsensitiveCommand(Method method, Object commandHandler, String[] command, String permission, List<String> desc) throws HandlerCompilationException {
        MethodDescriptor methodDesc = MethodDescriptor.fromMethod(method);

        CommandParameters cmdParams = this.compileParams(methodDesc, CTX_INSENSITIVE_IMPLICIT_PARAMS_IDX);
        Parameter senderParam = checkImplicitParam(methodDesc, "sender", CTX_INSENSITIVE_SENDER_PARAM_IDX, CommandSender.class);

        SubCommand cmd = this.addSubCommand(command, permission);
        if (cmd == null)
            throw new HandlerCompilationException(methodDesc, "Invalid sub command %s.", Arrays.toString(command));

        MethodHandle methodHandle;
        try {
            method.setAccessible(true);
            methodHandle = MethodHandles.lookup()
                    .unreflect(method)
                    .bindTo(commandHandler);
        } catch (IllegalAccessException e) {
            throw new HandlerCompilationException(methodDesc, "Error accessing method.", e);
        }

        CommandMethodHandle handle = new CommandMethodHandle(senderParam.getType(), cmdParams, desc, methodHandle);

        return new ContextInsensitiveCommand(cmd, handle);
    }

    private String[] buildCommand(String[] prefix, String[] cmd, String fallback) {
        if (cmd == null || cmd.length == 0)
            cmd = new String[]{ fallback };

        if (prefix.length == 0)
            return cmd;

        String[] concat = Arrays.copyOf(prefix, prefix.length + cmd.length);
        System.arraycopy(cmd, 0, concat, prefix.length, cmd.length);
        return concat;
    }

    private Parameter checkImplicitParam(MethodDescriptor method, String name, int idx, Class<?> assignableTo) throws HandlerCompilationException {
        Parameter param = method.getParameter(idx);
        if (param == null)
            throw new HandlerCompilationException(method, "Missing required implicit parameter %s in position %d.", name, idx);

        if (!assignableTo.isAssignableFrom(param.getType()))
            throw new HandlerCompilationException(method, "Invalid %s type %s. Must be assignable to %s.", name, param.getType().getSimpleName(), assignableTo.getName());

        return param;
    }

    private CommandParameters compileParams(MethodDescriptor method, BitSet skip) throws HandlerCompilationException {
        List<Parameter> methodParams = method.getParameters();

        List<CommandParameter<?>> cmdParams = new ArrayList<>(methodParams.size() - skip.cardinality());
        int argIdx = -1;
        for (int i = 0; i < methodParams.size(); i++) {
            if (skip.get(i))
                continue;
            argIdx++;

            Parameter p = methodParams.get(i);

            ArgDescription argDesc = p.getAnnotation(ArgDescription.class);

            CommandParameterKind kind = p.isVarArgs()
                    ? CommandParameterKind.VAR_ARGS
                    : argDesc != null && argDesc.optional()
                            ? CommandParameterKind.OPTIONAL
                            : CommandParameterKind.REQUIRED;

            ArgumentFormatter<?> formatter = p.isVarArgs()
                    ? FormatterMapping.lookpArray(p.getType())
                    : FormatterMapping.lookup(p.getType());

            String name = argDesc != null && !argDesc.name().isEmpty()
                    ? argDesc.name()
                    : p.isNamePresent()
                            ? p.getName()
                            : String.format("arg%d", argIdx);

            List<String> desc = argDesc != null && argDesc.description().length != 0
                    ? Arrays.asList(argDesc.description())
                    : null;

            if (formatter == null)
                throw new HandlerCompilationException(method, "Unknown argument type %s for parameter %s.", p.getType().getSimpleName(), name);

            CommandParameter<?> param = new CommandParameter<>(kind, formatter, p.getType(), name, desc);

            cmdParams.add(param);
        }

        try {
            return CommandParameters.fromList(cmdParams);
        } catch (IllegalArgumentException e) {
            throw new HandlerCompilationException(method, e.getMessage());
        }
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
            SubCommand subCmd = null;
            for (String s : subCommandAliases) {
                subCmd = superCmd.getSubCommand(s);
                if (subCmd != null) break; //We found it
            }
            if (subCmd == null) {
                //We could find an existing one, so many a new one
                if (subCommandAliases.length > 1) {
                    subCmd = new SubCommand(subCommandAliases[0], Arrays.copyOfRange(subCommandAliases, 1, subCommandAliases.length), permission, superCmd);
                } else {
                    subCmd = new SubCommand(subCommandAliases[0], new String[0], permission, superCmd);
                }
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
                new BukkitInterceptorCommand(this.lib, base.getName(), "/" + base.getName(), "/" + base.getName(), base.getAliases()));

        return base;
    }

    /**
     * Usage designed for tab complete.
     *
     * @param enteredCommand the partial command entered
     *
     * @return a List containing the possible sub commands that may follow, will never return null
     */
    public List<String> getPossibleSubCommands(String[] enteredCommand) {
        // TODO not case insensitive
        SubCommand sub = this.getSubCommand(enteredCommand);
        if (sub == null) {
            //Try to partially fix the last arg
            sub = this.getSubCommand(Arrays.copyOfRange(enteredCommand, 0, enteredCommand.length - 1));
            if (sub == null) {
                //Nope they are lost
                return Collections.emptyList();
            }
            String lastEntered = enteredCommand[enteredCommand.length - 1];
            return sub.getSubCommands().stream()
                    .filter(it -> it.startsWith(lastEntered))
                    .collect(Collectors.toList());
        } else {
            if (sub.canBeInvokedBy(enteredCommand[enteredCommand.length - 1])) {
                //We have a complete command
                return Collections.emptyList();
            }
            return sub.getSubCommands();
        }
    }

    /**
     * Handle the given command.
     *
     * @param sender  the {@link CommandSender} that sent the command
     * @param command the command split into parts. The command followed by each argument
     *
     * @return true iff the base command is registered with this registry and an attempt to execute was preformed
     *
     * @throws CommandException if an error occurs while handling the command.
     */
    protected boolean handleCommand(CommandSender sender, String[] command) throws CommandException {
        if (command == null || command.length < 1)
            throw new IllegalArgumentException("command was empty");

        SubCommand cmd = this.getBaseCommand(command[0]);
        if (cmd == null)
            return false;

        SubCommand next;
        int i;
        for (i = 1; i < command.length; i++) {
            next = cmd.getSubCommand(command[i]);
            if (next == null) break; // We went as far as we could go
            else cmd = next;
        }

        if (!cmd.canBeExecutedBy(sender)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute " + cmd.toString() + ".");
            return true;
        }

        // Invoke the command
        CommandExecutor executor = this.executors.get(cmd);
        if (executor == null)
            return false;

        if (!executor.canExecute(sender)) {
            sender.sendMessage(ChatColor.RED + "Cannot execute " + cmd.toString() + " in your current state.");
            return true;
        }

        // i is index of first arg
        try {
            List<String> args = i < command.length
                    ? Arrays.asList(command).subList(i, command.length)
                    : Collections.emptyList();

            executor.execute(sender, args);
        } catch (Exception e) {
            throw new CommandException(e);
        }

        return true;
    }

    public void displayHelp(CommandSender sender, String[] partialCmdRaw) {
        Stream<SubCommand> cmds = this.executors.keySet().stream();

        if (partialCmdRaw.length > 0) {
            SubCommand partialCmd = this.getSubCommand(partialCmdRaw);
            if (partialCmd == null) {
                sender.sendMessage(ChatColor.YELLOW + "No commands match the query " + Arrays.toString(partialCmdRaw) + ".");
                return;
            }

            cmds = cmds.filter(cmd -> cmd.startsWith(partialCmd));
        }

        List<CommandExecutor> matching = cmds
                .sorted()
                .map(this.executors::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (matching.isEmpty())
            sender.sendMessage(ChatColor.RED + "No commands you are allowed to execute match the query.");
        else
            matching.forEach(executor -> executor.sendDescription(sender));
    }
}
