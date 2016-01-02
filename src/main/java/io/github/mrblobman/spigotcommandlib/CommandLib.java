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
	
	protected boolean execute(CommandSender sender, String[] command) throws CommandException {
		return registry.handleCommand(sender, command);
	}
	
	protected List<String> tabComplete(CommandSender sender, String[] command) {
		return registry.getPossibleSubCommands(command);
	}

	public void sendHelpMessage(CommandSender sender, String... searchQuery) {
		this.registry.displayHelp(sender, searchQuery);
	}
}
