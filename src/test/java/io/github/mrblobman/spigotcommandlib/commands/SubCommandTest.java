package io.github.mrblobman.spigotcommandlib.commands;

import io.github.mrblobman.spigotcommandlib.SubCommandHandle;
import io.github.mrblobman.spigotcommandlib.SubCommandHandler;
import org.bukkit.command.CommandSender;

/**
 * Created on 2016-01-07.
 */
public class SubCommandTest implements SubCommandHandler {

    @SubCommandHandle(permission = "subcommand.test", description = "Testing subcommand handler 1.")
    private void subOne(CommandSender sender, String arg1) {
        sender.sendMessage("Sub1: "+arg1);
    }

    @SubCommandHandle(permission = "subcommand.test", description = "Testing subcommand handler 2.")
    private void subTwo(CommandSender sender, String arg1) {
        sender.sendMessage("Sub2: "+arg1);
    }
}
