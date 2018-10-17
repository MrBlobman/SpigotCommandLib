package io.github.mrblobman.spigotcommandlib.commands;

import io.github.mrblobman.spigotcommandlib.CommandHandle;
import io.github.mrblobman.spigotcommandlib.CommandHandler;
import org.bukkit.command.CommandSender;

public class ExplicitSubCommand implements CommandHandler {
    @CommandHandle(command = "explicitsub", description = "Base cmd")
    public void explicitsub(CommandSender sender) {
        sender.sendMessage("explicitsub base");
    }

    @CommandHandle(command = {"explicitsub", "subone"}, description = "Sub one")
    public void explicitsub1(CommandSender sender) {
        sender.sendMessage("explicitsub subone");
    }

    @CommandHandle(command = {"explicitsub", "subtwo"}, description = "Sub two")
    public void explicitsub2(CommandSender sender) {
        sender.sendMessage("explicitsub subtwo");
    }
}
