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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * A bridge from a {@link Command} to our custom registry.
 */
public class BaseCommand extends Command {
	private static final Pattern ARG_PATTERN = Pattern.compile("(?:(['\"])(.*?)(?<!\\\\)(?>\\\\\\\\)*\\1|([^\\s]+))");

    private CommandLib lib;
	
	BaseCommand(CommandLib lib, String name, String description, String usageMessage, List<String> aliases) {
		super(name, description, usageMessage, aliases);
		this.lib = lib;
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		//Put the command back together.
		StringBuilder cmd = new StringBuilder(label);
		for (String arg : args) {
			cmd.append(" ");
			cmd.append(arg);
		}
		try {
			String[] cmdGiven = parseCommandString(cmd.toString());
            if (!lib.execute(sender, cmdGiven)) {
                lib.sendHelpMessage(sender, cmdGiven);
                return false;
            }
		} catch (Exception e) {
			sender.sendMessage(ChatColor.RED + "An internal error has occurred. Please contact a server administrator.");
			lib.getHook().getLogger().log(Level.SEVERE, "Error executing "+cmd.toString(), e);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		List<String> results =  super.tabComplete(sender, alias, args);
		//Put the command back together.
		StringBuilder cmd = new StringBuilder(alias);
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

    /**
     * Parse the command with our custom splitter to support
     * wrapping args in quotation marks for args with spaces.
     * @param command the entire submitted command.
     * @return the properly split command
     */
	private String[] parseCommandString(String command) {
		if (command.startsWith("/")) {
			command = command.substring(1);
		}
		List<String> matches = new ArrayList<>();
		Matcher m = ARG_PATTERN.matcher(command);
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
