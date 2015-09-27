package io.github.mrblobman.commandlib;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HandleInvoker {	
	private SubCommand subCommand;
	
	private Object invocationTarget;
	private Method method;
	//Sender
	private Class<?> senderType;
	//Args
	//These two are very tightly coupled, must have same length, do not expose
	private String[] argNames;
	private ArgumentFormatter<?>[] argFormatters;
	//Will be 1 less that argFormatters.length if containsVarargs
	private int minArgsRequired;
	private boolean containsVarargs;
	
	HandleInvoker(SubCommand subCmd, Object invocationTarget, Method cmdHandler, Class<?> senderType, String[] argNames, ArgumentFormatter<?>[] argFormatters, int minArgsRequired) {
		this.subCommand = subCmd;
		this.invocationTarget = invocationTarget;
		this.method = cmdHandler;
		this.method.setAccessible(true);
		this.senderType = senderType;
		this.argNames = argNames;
		this.argFormatters = argFormatters;
		this.minArgsRequired = minArgsRequired;
		this.containsVarargs = method.isVarArgs();
	}
	
	/**
	 * Invoke this handler with the given arguments. The args are all String args that follow the sub command.<br>
	 * Ex: /baseCmd sub1 sub2 arg0 arg1 arg2
	 * @param sender the command sender. If this type doesn't match the sender type it will inform the sender.
	 * @param args the args in which to invoke the handler with. 
	 * @throws Exception if the method invocation fails for a non user based error, user based errors will directly be messaged to the player.
	 */
	public void invoke(CommandSender sender, String[] args) throws Exception {
		if (!senderType.isInstance(sender)) {
			//Wrong sender type, cannot invoke
			sendIncorrectSenderMessage(sender);
			return;
		}
		if (args.length < minArgsRequired) {
			//Not enough args, send usage
			sendUsage(sender);
			return;
		}
		List<Object> params = new ArrayList<Object>();
		for (int i = 0; i < minArgsRequired; i++) {
			if (argFormatters[i].canBeParsedFrom(args[i])) {
				params.add(argFormatters[i].parse(args[i]));
			} else {
				//Invalid type param
				sendUsage(sender);
				return;
			}
		}
		if (containsVarargs) {
			@SuppressWarnings("unchecked")
			List<Object> varArgs = (List<Object>) argFormatters[minArgsRequired].createTypedList();
			//Handle varargs
			for (int i = minArgsRequired; i < args.length; i++) {
				if (argFormatters[minArgsRequired].canBeParsedFrom(args[i])) {
					varArgs.add(argFormatters[minArgsRequired].parse(args[i]));
				} else {
					//Invalid type param
					sendUsage(sender);
					return;
				}
			}
			params.add(varArgs.toArray((Object[]) Array.newInstance(argFormatters[minArgsRequired].getParseType(), varArgs.size())));
		}
		int i = 0;
		Object[] callParams = new Object[params.size()+1];
		callParams[i++] = sender;
		for (Object param : params) {
			callParams[i++] = param;
		}
		method.invoke(invocationTarget, callParams);
	}
	
	public void sendIncorrectSenderMessage(CommandSender sender) {
		sender.sendMessage(ChatColor.RED+" This can only be executed by a(n) "+senderType.getSimpleName()+". You are a(n) "+sender.getClass().getSimpleName()+".");
	}
	
	public void sendUsage(CommandSender sender) {
		StringBuilder strBuilder = new StringBuilder(this.subCommand.toString());
		for (int i = 0; i < this.minArgsRequired; i++) {
			strBuilder.append(" <");
			strBuilder.append(this.argNames[i]);
			strBuilder.append(">");
		}
		if (this.containsVarargs) {
			strBuilder.append(" [");
			strBuilder.append(this.argNames[this.argNames.length-1]);
			strBuilder.append("]");
		}
		if (sender instanceof Player) {
			ComponentBuilder builder = new ComponentBuilder(this.subCommand.toString());
			builder.color(ChatColor.RED);
			builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, strBuilder.toString()));
			builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, formatTextToItem(ChatColor.YELLOW+this.subCommand.toString(), 
																					   ChatColor.GRAY+"Click to paste this command's", 
																					   ChatColor.GRAY+"format in your chat box.")));
			for (int i = 0; i < this.minArgsRequired; i++) {
				builder.append(" <"+argNames[i]+">");
				String[] info = new String[argFormatters[i].getTypeDesc().length+1];
				info[0] = ChatColor.YELLOW + argFormatters[i].getTypeName();
				for (int j = 1; j < info.length; j++) {
					info[j] = ChatColor.GRAY+argFormatters[i].getTypeDesc()[j-1];
				}
				builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, formatTextToItem(info)));
			}
			if (this.containsVarargs) {
				builder.append(" ["+argNames[argNames.length-1]+"]");
				String[] info = new String[argFormatters[argNames.length-1].getTypeDesc().length+1];
				info[0] = ChatColor.YELLOW + argFormatters[argNames.length-1].getTypeName();
				for (int j = 1; j < info.length; j++) {
					info[j] = ChatColor.GRAY+argFormatters[argNames.length-1].getTypeDesc()[j-1];
				}
				builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, formatTextToItem(info)));
			}
			//sender.sendMessage(ChatColor.RED+"Incorrect usage. Click the command for the format to be pasted in your chat box and hover an arg for more info on it.");
			((Player) sender).spigot().sendMessage(builder.create());
		} else {
			sender.sendMessage(ChatColor.YELLOW+strBuilder.toString());
		}
	}
	
	/**
	 * This is used to format the lines as a multilined item when used in a {@link HoverEvent}
	 * @return the constructed serialized itemstack
	 */
	public static BaseComponent[] formatTextToItem(String... lines){
		String info = "display:{Name:";
		if (lines.length >= 1){
			// \"Name\"
			info = info + "\"" + lines[0] + "\"";
			if (lines.length >=2){
				info = info + ", Lore:[";
				for (int i = 1; i< lines.length; i++) {
					info = info + "\"" + lines[i] + "\", ";
				}
				info = info.substring(0, info.length()-2);
				info = info + "]";
			}
			info = info + "}";
		}
		return new BaseComponent[]{new TextComponent("{id:1,tag:{"+info+"}}")};
	}
}
