package io.github.mrblobman.spigotcommandlib;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * A bridge from bukkit command to our custom registry.
 */
public class BaseCommand extends Command {
	private static Pattern argPattern = Pattern.compile("(?:(['\"])(.*?)(?<!\\\\)(?>\\\\\\\\)*\\1|([^\\s]+))");
	private CommandLib lib;
	
	BaseCommand(CommandLib lib, String name, String description, String usageMessage, List<String> aliases) {
		super(name, description, usageMessage, aliases);
		this.lib = lib;
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		//Put the command back together.
		StringBuilder cmd = new StringBuilder();
		cmd.append(label);
		for (String arg : args) {
			cmd.append(" ");
			cmd.append(arg);
		}
		try {
			lib.execute(sender, parseCommandString(cmd.toString()));
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "An internal error has occured. Please contact a server administrator.");
			lib.getHook().getLogger().log(Level.SEVERE, "Error executing "+cmd.toString(), e);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> results =  super.tabComplete(sender, alias, args);
		//Put the command back together.
		StringBuilder cmd = new StringBuilder();
		cmd.append(alias);
		for (String arg : args) {
			cmd.append(" ");
			cmd.append(arg);
		}
		List<String> possibleSubs = lib.tabComplete(sender, parseCommandString(cmd.toString()));
		if (possibleSubs.isEmpty()) {
			return results;
		} else {
			return possibleSubs;
		}
	}
	
	private String[] parseCommandString(String command) {
		if (command.startsWith("/")) {
			command = command.substring(1);
		}
		List<String> matches = new ArrayList<>();
		Matcher m = argPattern.matcher(command);
		while (m.find()) {
			if (m.group(2) != null) {
				matches.add(m.group(2).replace("\\\"", "\""));
			} else if (m.group(3) != null) {
				matches.add(m.group(3).replace("\\\"", "\""));
			}
		}
		return matches.toArray(new String[matches.size()]);
	}
}
