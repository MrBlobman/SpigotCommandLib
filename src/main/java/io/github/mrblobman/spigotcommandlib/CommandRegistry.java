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
package io.github.mrblobman.spigotcommandlib;

import io.github.mrblobman.spigotcommandlib.args.ArgDescription;
import io.github.mrblobman.spigotcommandlib.args.Argument;
import io.github.mrblobman.spigotcommandlib.args.ArgumentFormatter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
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
            throw new InstantiationException("Could not grab the command map from the bukkit server. "+e.getLocalizedMessage());
        }
        Object rawMap;
        try {
            rawMap = commandMap.invoke(Bukkit.getServer());
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            throw new InstantiationException("Could not grab the command map from the bukkit server. "+e.getLocalizedMessage());
        }
        if (rawMap == null || !(rawMap instanceof CommandMap)) {
            throw new InstantiationException("Could not grab the command map from the bukkit server.");
        }
        this.bukkitCommandMap = (CommandMap) rawMap;
        this.lib = lib;
    }
	
	public void register(CommandHandler commandHandler) {
		MethodLoop:
			for (Method method : commandHandler.getClass().getDeclaredMethods()) {
				CommandHandle handlerAnnotation = method.getAnnotation(CommandHandle.class);
				//Move on, this method isnt annotated
				if (handlerAnnotation == null) continue;
				//Check that min requirements are met
				//JDK8+ if (method.getParameterCount() < 1) {
				if (method.getParameterTypes().length < 1) {
					lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Not enough parameters. Requires first param as sender.");
					continue;
				}
				//JDK8+ Parameter[] methodParams = method.getParameters();
                Class<?>[] methodParams = method.getParameterTypes();
                //JDK7 annotation workaround
                ArgDescription[] paramArgDesc = getArgDescs(method);
				//Build parsing data arrays
                Argument<?>[] arguments = new Argument[methodParams.length-1];
				for (int i = 1; i < methodParams.length-(method.isVarArgs() ? 1 : 0); i++) {
					ArgumentFormatter<?> formatter;
                    //JDK8+ Class<?> paramType = methodParams[i].getType();
                    Class<?> paramType = methodParams[i];
					if (String.class.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.STRING;
					} else if (Integer.class.isAssignableFrom(paramType) || Integer.TYPE.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.INTEGER;
					} else if (Double.class.isAssignableFrom(paramType) || Double.TYPE.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.DOUBLE;
					} else if (Long.class.isAssignableFrom(paramType) || Long.TYPE.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.LONG;
					} else if (Short.class.isAssignableFrom(paramType) || Short.TYPE.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.SHORT;
					} else if (Float.class.isAssignableFrom(paramType) || Float.TYPE.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.FLOAT;
					} else if (Boolean.class.isAssignableFrom(paramType) || Boolean.TYPE.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.BOOLEAN;
                    } else if (Color.class.isAssignableFrom(paramType)) {
                        formatter = ArgumentFormatter.COLOR;
					} else {
                        lib.getHook().getLogger().log(Level.WARNING, "Cannot register method "+method.getName()+". Unknown parameter parse type ("+paramType.getName()+"). Accepted types are String, Integer, Long, Short, Double, Float and org.bukkit.Color.");
						continue MethodLoop;
					}
                    ArgDescription desc = paramArgDesc[i];
                    String name;
                    if (desc != null && !desc.name().equals("")) {
                        name = desc.name();
                    } else {
                        //JDK8+ name = methodParams[i].isNamePresent() ? methodParams[i].getName() : "arg" + (i - 1);
                        name = "arg" + (i - 1);
                    }
                    if (desc != null && desc.description().length != 0) {
                        arguments[i - 1] = new Argument<>(formatter, name, desc.description(), false);
                    } else {
                        arguments[i - 1] = new Argument<>(formatter, name, false);
                    }
				}
				if (method.isVarArgs()) {
                    int lastIndex = methodParams.length - 1;
                    ArgumentFormatter<?> formatter;
                    //We need to handle the last arg differently because it is a var arg
					Class<?> paramType = methodParams[lastIndex];
					if (String[].class.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.STRING;
					} else if (Integer[].class.isAssignableFrom(paramType) || int[].class.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.INTEGER;
					} else if (Double[].class.isAssignableFrom(paramType) || double[].class.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.DOUBLE;
					} else if (Long[].class.isAssignableFrom(paramType) || long[].class.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.LONG;
					} else if (Short[].class.isAssignableFrom(paramType) || short[].class.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.SHORT;
					} else if (Float[].class.isAssignableFrom(paramType) || float[].class.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.FLOAT;
					} else if (Boolean[].class.isAssignableFrom(paramType) || boolean[].class.isAssignableFrom(paramType)) {
						formatter = ArgumentFormatter.BOOLEAN;
					} else if (Color[].class.isAssignableFrom(paramType)) {
                        formatter = ArgumentFormatter.COLOR;
                    } else {
                        lib.getHook().getLogger().log(Level.WARNING, "Cannot register method "+method.getName()+". Unknown parameter parse type ("+paramType.getName()+"). Accepted types are String, Integer, Long, Short, Double, Float and org.bukkit.Color.");
						continue;
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
                        arguments[lastIndex-1] = new Argument<>(formatter, name, desc.description(), true);
                    } else {
                        arguments[lastIndex-1] = new Argument<>(formatter, name, true);
                    }
				}
				//Verify the sender type is valid
				Class<?> senderType = methodParams[0];
				if (!CommandSender.class.isAssignableFrom(senderType)) {
                    lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Invalid sender type " + senderType.getSimpleName() + ". Must be accessible from org.bukkit.CommandSender.");
					continue;
				}
				//Register the sub command
				SubCommand cmd = addSubCommand(handlerAnnotation.command(), handlerAnnotation.permission());
				if (cmd == null) {
                    lib.getHook().getLogger().log(Level.WARNING, "Cannot register method " + method.getName() + ". Invalid sub command.");
					continue;
				}
				//-1 for first param sender type
				int minArgs = methodParams.length-1;
				//Last arg is not required
				if (method.isVarArgs()) minArgs--;
				//Finally create the invoker
				this.invokers.put(cmd, new HandleInvoker(cmd, handlerAnnotation.description(), commandHandler, method, senderType, arguments, minArgs));
                lib.getHook().getLogger().log(Level.INFO, "Successfully registered " + method.getName() + " in " + commandHandler.getClass().getSimpleName() + " for " + cmd.toString());
			}
	}

    private ArgDescription[] getArgDescs(Method method) {
        Annotation[][] annotations = method.getParameterAnnotations();
        ArgDescription[] argDesc = new ArgDescription[annotations.length];
        int index = 0;
        for (Annotation[] paramAnnotations : annotations) {
            for (Annotation annotation : paramAnnotations) {
                if (annotation.annotationType().equals(ArgDescription.class)) {
                    argDesc[index] = (ArgDescription) annotation;
                }
            }
            index++;
        }
        return argDesc;
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
				new BaseCommand(this.lib, base.getName(), "/"+base.getName(), "/"+base.getName(), base.getAliases()));
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
			sub = this.getSubCommand(Arrays.copyOfRange(enteredCommand, 0, enteredCommand.length-1));
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
            sender.sendMessage(ChatColor.RED + "You do not have permission to execute "+cmd.toString()+".");
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
