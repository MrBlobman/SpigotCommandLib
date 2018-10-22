package io.github.mrblobman.spigotcommandlib.registry;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandExecutor {
    public SubCommand getTrigger();

    public boolean canExecute(CommandSender sender);

    public void execute(CommandSender sender, List<String> args) throws Exception;

    public void sendDescription(CommandSender sender);
}
