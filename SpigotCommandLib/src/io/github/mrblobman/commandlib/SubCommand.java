package io.github.mrblobman.commandlib;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.permissions.Permissible;

public class SubCommand {
	private Map<String, SubCommand> subCommands = new HashMap<String, SubCommand>();
	private String name;
	private SubCommand superCommand;
	private Set<String> permissions;
	private Set<String> aliases;
	
	SubCommand(@Nonnull String name, String[] aliases, String permission, SubCommand superCommand, SubCommand... subCommands) {
		this.name = name;
		this.aliases = new HashSet<String>();
		for (String alias : aliases) {
			this.aliases.add(alias.toLowerCase());
		}
		this.permissions = new HashSet<String>();
		this.permissions.add(permission);
		for (SubCommand cmd : subCommands) {
			this.subCommands.put(cmd.getName().toLowerCase(), cmd);
		}
		this.superCommand = superCommand;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Set<String> getAliases() {
		return Collections.unmodifiableSet(this.aliases);
	}
	
	/**
	 * Check if {@code caller} has permission to execute this {@code SubCommand}
	 * and all super commands.
	 * @param caller the caller executing the sub command
	 * @return true iff the called has permission to execute this and all super commands.
	 */
	public boolean canExecute(Permissible caller) {
		for (String permission : this.permissions) {
			if (caller.isOp() || (caller.hasPermission(permission) && this.superCommand.canExecute(caller))) return true;
		}
		return false;
	}
	
	public void addPermission(String permission) {
		this.permissions.add(permission);
	}
	
	public boolean removePermission(String permission) {
		return this.permissions.remove(permission);
	}
	
	@Nullable
	public SubCommand getSubCommand(String name) {
		String lowerCaseName = name.toLowerCase();
		SubCommand cmd = this.subCommands.get(lowerCaseName);
		if (cmd != null) return cmd;
		else {
			for (SubCommand sub : this.subCommands.values()) {
				if (sub.getAliases().contains(lowerCaseName)) {
					return sub;
				}
			}
		}
		return null;
	}
	
	public void addSubCommand(SubCommand cmd) {
		this.subCommands.put(cmd.getName().toLowerCase(), cmd);
	}
	
	/**
	 * The super command that directly leads this command.
	 * @return null if this SubCommand is a {@link BaseCommand}
	 */
	@Nullable
	public SubCommand getSuperCommand() {
		return this.superCommand;
	}
	
	@Override
	public String toString() {
		if (this.superCommand == null) {
			return "/"+this.name;
		} else {
			return this.superCommand.toString()+" "+this.name;
		}
	}
}
