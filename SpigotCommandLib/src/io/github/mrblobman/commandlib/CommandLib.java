package io.github.mrblobman.commandlib;

import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public class CommandLib {
	private CommandRegistry registry;
	private Plugin hook;
	
	public CommandLib (Plugin hook) throws InstantiationException {
		this.hook = hook;
		this.registry = new CommandRegistry(this);
		//Bukkit.getPluginManager().registerEvents(this, hook);
	}
	
	/**
	 * Register a new handler. Methods that handle specific commands must
	 * be flagged with the {@code @SubCommandHandler} annotation.
	 * @see CommandHandler
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
	
	protected void execute(CommandSender sender, String[] command) throws Exception {
		registry.handleCommand(sender, command);
	}
	
	protected List<String> tabComplete(CommandSender sender, String[] command) {
		return registry.getPossibleSubCommands(command);
	}
}
