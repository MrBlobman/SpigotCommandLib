package io.github.mrblobman.commandlib;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.annotation.Nullable;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandRegistry {
	private Map<String, SubCommand> baseCommands = new HashMap<String, SubCommand>();
	private Map<SubCommand, HandleInvoker> invokers = new HashMap<SubCommand, HandleInvoker>();
	
	//Seal the class
	CommandRegistry() {}
	
	public void register(Object commandHandler) {
		MethodLoop:
			for (Method method : commandHandler.getClass().getMethods()) {
				SubCommandHandler handlerAnnotation = method.getAnnotation(SubCommandHandler.class);
				//Move on, this method isnt annotated
				if (handlerAnnotation == null) continue MethodLoop;
				//Check that min requirements are met
				if (method.getParameterCount() < 1) {
					Bukkit.getLogger().log(Level.WARNING, "Cannot register method "+method.getName()+". Not enough parameters. Requires first param as sender.");
					continue MethodLoop;
				}
				Parameter[] methodParams = method.getParameters();
				//Build parsing data arrays
				String[] argNames = new String[methodParams.length-1];
				ArgumentFormatter<?>[] formatters = new ArgumentFormatter<?>[methodParams.length-1];
				for (int i = 1; i < methodParams.length-(method.isVarArgs() ? 2 : 1); i++) {
					argNames[i-1] = methodParams[i-1].getName();
					Class<?> paramType = methodParams[i].getType().getClass();
					if (paramType.equals(String.class)) {
						formatters[i-1] = ArgumentFormatter.STRING;
					} else if (paramType.equals(Integer.class) || paramType.equals(Integer.TYPE)) {
						formatters[i-1] = ArgumentFormatter.INTEGER;
					} else if (paramType.equals(Double.class) || paramType.equals(Double.TYPE)) {
						formatters[i-1] = ArgumentFormatter.DOUBLE;
					} else if (paramType.equals(Long.class) || paramType.equals(Long.TYPE)) {
						formatters[i-1] = ArgumentFormatter.LONG;
					} else if (paramType.equals(Short.class) || paramType.equals(Short.TYPE)) {
						formatters[i-1] = ArgumentFormatter.SHORT;
					} else if (paramType.equals(Float.class) || paramType.equals(Float.TYPE)) {
						formatters[i-1] = ArgumentFormatter.FLOAT;
					} else if (paramType.equals(Color.class)) {
						formatters[i-1] = ArgumentFormatter.COLOR;
					} else {
						Bukkit.getLogger().log(Level.WARNING, "Cannot register method "+method.getName()+". Unknown parameter parse type ("+paramType.getName()+"). Accepted types are String, Integer, Long, Short, Double, Float and org.bukkit.Color.");
						continue MethodLoop;
					}
				}
				if (method.isVarArgs()) {
					argNames[methodParams.length-2] = methodParams[methodParams.length-2].getName();
					//We need to handle the last arg differently because it is a var arg
					Class<?> paramType = methodParams[methodParams.length-2].getType().getClass();
					if (paramType.equals(String[].class)) {
						formatters[formatters.length-2] = ArgumentFormatter.STRING;
					} else if (paramType.equals(Integer[].class) || paramType.equals(Integer.TYPE)) {
						formatters[formatters.length-2] = ArgumentFormatter.INTEGER;
					} else if (paramType.equals(Double.class) || paramType.equals(Double.TYPE)) {
						formatters[formatters.length-2] = ArgumentFormatter.DOUBLE;
					} else if (paramType.equals(Long.class) || paramType.equals(Long.TYPE)) {
						formatters[formatters.length-2] = ArgumentFormatter.LONG;
					} else if (paramType.equals(Short.class) || paramType.equals(Short.TYPE)) {
						formatters[formatters.length-2] = ArgumentFormatter.SHORT;
					} else if (paramType.equals(Float.class) || paramType.equals(Float.TYPE)) {
						formatters[formatters.length-2] = ArgumentFormatter.FLOAT;
					} else if (paramType.equals(Color.class)) {
						formatters[formatters.length-2] = ArgumentFormatter.COLOR;
					} else {
						Bukkit.getLogger().log(Level.WARNING, "Cannot register method "+method.getName()+". Unknown parameter parse type ("+paramType.getName()+"). Accepted types are String, Integer, Long, Short, Double, Float and org.bukkit.Color.");
						continue MethodLoop;
					}
				}
				//Verify the sender type is valid
				Class<?> senderType = methodParams[0].getClass();
				if (!senderType.equals(CommandSender.class) || !senderType.equals(Player.class)) {
					Bukkit.getLogger().log(Level.WARNING, "Cannot register method "+method.getName()+". Invalid sender type "+senderType.getSimpleName()+". Must be CommandSender or Player.");
					continue MethodLoop;
				}
				//Register the sub command
				SubCommand cmd = addSubCommand(handlerAnnotation.subCommand(), handlerAnnotation.permission());
				if (cmd == null) {
					Bukkit.getLogger().log(Level.WARNING, "Cannot register method "+method.getName()+". Invalid sub command.");
					continue MethodLoop;
				}
				//-1 for first param sender type
				int minArgs = method.getParameterCount()-1;
				//Last arg is not required
				if (method.isVarArgs()) minArgs--;
				//Finally create the invoker
				this.invokers.put(cmd, new HandleInvoker(cmd, commandHandler, method, senderType, argNames, formatters, minArgs));
			}
	}
	
	@Nullable
	public SubCommand getSubCommand(String[] command) {
		if (command == null || command.length < 1) return null;
		SubCommand cmd = this.getBaseCommand(command[0]);
		if (cmd == null) return null;
		for (int i = 1; i < command.length; i++) {
			cmd = cmd.getSubCommand(command[i]);
			if (cmd == null) return null;
		}
		return cmd;
	}
	
	@Nullable
	public SubCommand addSubCommand(String[] command, String permission) {
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
			superCmd = subCmd;
		}
		return superCmd;
	}
	
	@Nullable
	private SubCommand getBaseCommand(String baseCommand) {
		for (String baseAlias : baseCommand.split("\\|")) {
			if (this.baseCommands.containsKey(baseAlias.toLowerCase())) {
				return this.baseCommands.get(baseAlias.toLowerCase());
			}
		}
		return null;
	}
	
	private SubCommand addBaseCommand(String baseCommand, String permission) {
		String[] baseAliases = baseCommand.split("\\|");
		SubCommand base = getBaseCommand(baseAliases[0]);
		if (base != null) {
			base.addPermission(permission);
			return base;
		}
		if (baseAliases.length > 1) {
			base = new SubCommand(baseAliases[0], Arrays.copyOfRange(baseAliases, 1, baseAliases.length), permission, null);
		} else {
			base = new SubCommand(baseAliases[0], new String[0], permission, null);
		}
		return base;
	}
	
	public void handleCommand(CommandSender sender, String[] command) throws Exception {
		if (command == null || command.length < 1) throw new IllegalArgumentException("command was empty");
		SubCommand cmd = this.getBaseCommand(command[0]);
		if (cmd == null) {
			displayHelp(sender, null);
			return;
		}
		SubCommand next;
		int i;
		for (i = 1; i < command.length; i++) {
			next = cmd.getSubCommand(command[i]);
			if (next == null) break; //We went as far as we could go
			else cmd = next;
		}
		if (!cmd.canExecute(sender)) {
			displayNoPermission(sender, cmd);
		}
		//Invoke the command
		HandleInvoker invoker = this.invokers.get(cmd);
		if (invoker == null) {
			displayHelp(sender, cmd);
			return;
		}
		invoker.invoke(sender, i > command.length-1 ? Arrays.copyOfRange(command, i, command.length) : new String[0]);
	}
	
	public void displayHelp(CommandSender sender, @Nullable SubCommand cmdGiven) {
		sender.sendMessage(ChatColor.YELLOW+"Acceptable sub commands are the following: ");
		for (HandleInvoker invoker : this.invokers.values()) {
			invoker.sendUsage(sender);
		}
	}
	
	public void displayNoPermission(CommandSender sender, SubCommand cmdGiven) {
		sender.sendMessage(ChatColor.RED+"You do not have permission to execute this command.");
	}
}
