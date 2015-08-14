package io.github.mrblobman.commandlib;

import java.lang.reflect.InvocationTargetException;
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

public class HandleInvoker {
	private String subCommand;
	
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
	
	public void invoke(CommandSender sender, String[] args) throws Exception {
		if (!senderType.isInstance(sender)) {
			//Wrong sender type, cannot invoke
			sendIncorrectSenderMessage(sender);
			return;
		}
		if (args.length != minArgsRequired) {
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
			//Handle varargs
			for (int i = minArgsRequired; i < args.length; i++) {
				if (argFormatters[minArgsRequired].canBeParsedFrom(args[i])) {
					params.add(argFormatters[minArgsRequired].parse(args[i]));
				} else {
					//Invalid type param
					sendUsage(sender);
					return;
				}
			}
		}
		method.invoke(invocationTarget, sender, params.toArray());
	}
	
	public void sendIncorrectSenderMessage(CommandSender sender) {
		sender.sendMessage(ChatColor.RED+" This can only be executed by a(n) "+senderType.getSimpleName()+". You are a(n) "+sender.getClass().getSimpleName()+".");
	}
	
	public void sendUsage(CommandSender sender) {
		String[] usage = new String[]{ChatColor.RED+"Incorrect usage. Click the command for the format to be pasted in your chat box and hover an arg for more info on it.", null};
		ComponentBuilder builder = new ComponentBuilder(this.subCommand);
		builder.color(ChatColor.RED);
		builder.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, ""));
		builder.event(new HoverEvent(HoverEvent.Action.SHOW_ITEM, formatTextToItem(ChatColor.YELLOW+this.subCommand, 
																				   ChatColor.GRAY+"Click to paste this command's", 
																				   ChatColor.GRAY+"format in your chat box.")));
		for (String argName : this.argNames) {
			
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
	
	//TEST TEST TEST!@@
	public static class Test {
		public void method(String one, Integer two) {
			System.out.println(one);
			System.out.println(two);
		}
	}
	public static void main(String[] args) {
		try {
			Method m = Test.class.getDeclaredMethod("method", String.class, Integer.class);
			Object[] testArr = new Object[]{"test1", 2};
			m.invoke(new Test(), testArr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
