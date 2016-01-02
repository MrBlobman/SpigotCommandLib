package io.github.mrblobman.spigotcommandlib;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CommandLib {
	private CommandRegistry registry;
	private Plugin hook;
	
	public CommandLib (Plugin hook) throws IllegalStateException {
		this.hook = hook;
		try {
			this.registry = new CommandRegistry(this);
		} catch (InstantiationException e) {
			throw new IllegalStateException("Could not retrieve the bukkit command map. It is likely that this instance is being constructed before a server is available.");
		}
	}
	
	/**
	 * Register a new handler. Methods that handle specific commands must
	 * be flagged with the {@code @SubCommandHandler} annotation.
	 * @see CommandHandler
	 * @param handler the command handler
	 */
	public void registerCommandHandler(CommandHandler handler) {
		registry.register(handler);
	}
	
	/**
	 * @return the plugin using this instance of the lib.
	 */
	public Plugin getHook() {
		return this.hook;
	}
	
	protected void execute(CommandSender sender, String[] command) throws Exception {
		registry.handleCommand(sender, command);
	}
	
	protected List<String> tabComplete(CommandSender sender, String[] command) {
		return registry.getPossibleSubCommands(command);
	}

	public void sendHelpMessage(CommandSender sender, String... searchQuery) {
		this.registry.displayHelp(sender, searchQuery);
	}
}
