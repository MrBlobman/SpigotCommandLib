package io.github.mrblobman.spigotcommandlib;

import io.github.mrblobman.spigotcommandlib.args.Argument;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class HandleInvoker {	
	private SubCommand subCommand;
    private String cmdDesc;
	
	private Object invocationTarget;
	private Method method;
	//Sender
	private Class<?> senderType;
	//Args
	private Argument[] arguments;
	//Will be 1 less that argFormatters.length if containsVarargs
	private int minArgsRequired;
	private boolean containsVarargs;
	
	HandleInvoker(SubCommand subCmd, String cmdDesc, Object invocationTarget, Method cmdHandler, Class<?> senderType, Argument[] arguments, int minArgsRequired) {
		this.subCommand = subCmd;
        this.cmdDesc = cmdDesc;
		this.invocationTarget = invocationTarget;
		this.method = cmdHandler;
		this.method.setAccessible(true);
		this.senderType = senderType;
        this.arguments = arguments;
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
		List<Object> params = new ArrayList<>();
		for (int i = 0; i < minArgsRequired; i++) {
			if (arguments[i].getFormatter().canBeParsedFrom(args[i])) {
                try {
                    params.add(arguments[i].getFormatter().parse(args[i]));
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "Invalid argument value. " + e.getLocalizedMessage());
                    return;
                }
			} else {
				//Invalid type param
				sendUsage(sender);
				return;
			}
		}
		if (containsVarargs) {
			@SuppressWarnings("unchecked")
			List<Object> varArgs = (List<Object>) arguments[minArgsRequired].getFormatter().createTypedList();
			//Handle varargs
			for (int i = minArgsRequired; i < args.length; i++) {
				if (arguments[minArgsRequired].getFormatter().canBeParsedFrom(args[i])) {
					varArgs.add(arguments[minArgsRequired].getFormatter().parse(args[i]));
				} else {
					//Invalid type param
					sendUsage(sender);
					return;
				}
			}
			params.add(varArgs.toArray((Object[]) Array.newInstance(arguments[minArgsRequired].getFormatter().getParseType(), varArgs.size())));
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
		sender.sendMessage(ChatColor.RED + "This can only be executed by a(n) " + senderType.getSimpleName() + ". You are a(n) " + sender.getClass().getSimpleName() + ".");
	}
	
	public void sendUsage(CommandSender sender) {
		StringBuilder strBuilder = new StringBuilder(this.subCommand.toExecutableString());
		for (Argument arg : this.arguments) strBuilder.append(" ").append(arg.getDescriptiveName());
		if (sender instanceof Player) {
			ComponentBuilder message = new ComponentBuilder(this.subCommand.toString());
			message.color(ChatColor.RED);
			message.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, strBuilder.toString()));
            message.event(buildTooltip(ChatColor.YELLOW + this.subCommand.toString(),
                            ChatColor.GRAY + "Click to paste this command's",
                            ChatColor.GRAY + "format in your chat box."));
            for (Argument arg : this.arguments) {
                message.append(" " + arg.getDescriptiveName());
                message.event(buildTooltip(arg.getDescription()));
            }
			((Player) sender).spigot().sendMessage(message.create());
		} else {
			sender.sendMessage(ChatColor.YELLOW + strBuilder.toString());
		}
	}

    public void sendDescription(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + this.cmdDesc);
        StringBuilder strBuilder = new StringBuilder(this.subCommand.toExecutableString());
        for (Argument arg : this.arguments) strBuilder.append(" ").append(arg.getDescriptiveName());
        if (sender instanceof Player) {
            ComponentBuilder message = new ComponentBuilder("    \u27A5" + this.subCommand.toString());
            message.color(ChatColor.RED);
            message.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, strBuilder.toString()));
            message.event(buildTooltip(ChatColor.YELLOW + this.subCommand.toString(),
                    ChatColor.GRAY + "Click to paste this command's",
                    ChatColor.GRAY + "format in your chat box."));
            for (Argument arg : this.arguments) {
                message.append(" " + arg.getDescriptiveName());
                message.event(buildTooltip(arg.getDescription()));
            }
            ((Player) sender).spigot().sendMessage(message.create());
        } else {
            sender.sendMessage(ChatColor.YELLOW + strBuilder.toString());
        }
    }

    /**
     * Build the HoverEvent that would result in the display
     * of lines when you hover over the component the event is
     * for.
     * @param lines the lines in the tooltip
     * @return the constructed hover event
     */
    public static HoverEvent buildTooltip(String... lines) {
        String info = "display:{Name:" + ChatColor.WHITE;
        if (lines.length >= 1) {
            // \"Name\"
            info += lines[0];
            if (lines.length >=2) {
                info = info + ", Lore:[";
                for (int i = 1; i< lines.length; i++) {
                    info = info + "\"" + ChatColor.WHITE + lines[i] + "\", ";
                }
                info = info.substring(0, info.length()-2);
                info = info + "]";
            }
            info = info + "}";
        }
        return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{new TextComponent("{id:1,tag:{"+info+"}}")});
    }
}
