package io.github.mrblobman.spigotcommandlib.commands;

import io.github.mrblobman.spigotcommandlib.SubCommandHandle;
import io.github.mrblobman.spigotcommandlib.SubCommandHandler;
import org.bukkit.command.CommandSender;

/**
 * Created on 2016-01-31.
 */
public class SubCommandTest2 implements SubCommandHandler {
    @SubCommandHandle(description = "Testing subcommand handler 3.")
    private void subThree(CommandSender sender, String arg1, String arg2) {
        sender.sendMessage("Sub3: "+arg1);
    }

    @SubCommandHandle(command = {"subThree", "sub"}, description = "Testing subcommand handler 4.")
    private void subFour(CommandSender sender, String arg1, String arg2, String arg3) {
        sender.sendMessage("Sub4: "+arg1);
    }
}
