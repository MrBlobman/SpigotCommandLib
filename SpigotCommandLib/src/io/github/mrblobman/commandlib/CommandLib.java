package io.github.mrblobman.commandlib;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;

public class CommandLib implements Listener {
	private static Pattern argPattern = Pattern.compile("(?:(['\"])(.*?)(?<!\\\\)(?>\\\\\\\\)*\\1|([^\\s]+))");
	private CommandRegistry registry;
	private Plugin hook;
	
	public CommandLib (Plugin hook) {
		this.hook = hook;
		this.registry = new CommandRegistry();
		Bukkit.getPluginManager().registerEvents(this, hook);
	}
	
	/**
	 * Register a new handler. Methods that handle specific commands must
	 * be flagged with the {@code @SubCommandHandler} annotation.
	 * @see SubCommandHandler
	 * @param handler the command handler
	 */
	public void registerCommandHandler(Object handler) {
		registry.register(handler);
	}
	
	/**
	 * @return the plugin using this instance of the lib.
	 */
	public Plugin getHook() {
		return this.hook;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onConsoleCommand(ServerCommandEvent event) {
		try {
			registry.handleCommand(event.getSender(), parseCommandString(event.getCommand()));
		} catch (Exception e) {
			event.getSender().sendMessage(ChatColor.RED + "An internal error has occured. Please contact a server administrator.");
			hook.getLogger().log(Level.SEVERE, e.getLocalizedMessage());
		}
	}

	@EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
	private void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		try {
			registry.handleCommand(event.getPlayer(), parseCommandString(event.getMessage()));
		} catch (Exception e) {
			event.getPlayer().sendMessage(ChatColor.RED + "An internal error has occured. Please contact a server administrator.");
			hook.getLogger().log(Level.SEVERE, e.getLocalizedMessage());
		}
	}
	
	private String[] parseCommandString(String command) {
		if (command.startsWith("/")) {
			command = command.substring(1);
		}
		List<String> matches = new ArrayList<String>();
		Matcher m = argPattern.matcher(command);
		while (m.find()) {
			if (m.group(2) != null) {
				matches.add(m.group(2));
		    } else if (m.group(3) != null) {
		    	matches.add(m.group(3));
		    }
		}
		return matches.toArray(new String[matches.size()]);
	}
}
