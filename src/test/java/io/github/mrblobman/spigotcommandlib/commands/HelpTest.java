package io.github.mrblobman.spigotcommandlib.commands;

import io.github.mrblobman.spigotcommandlib.CommandHandle;
import io.github.mrblobman.spigotcommandlib.CommandHandler;
import io.github.mrblobman.spigotcommandlib.CommandLib;
import io.github.mrblobman.spigotcommandlib.args.ArgDescription;
import org.bukkit.command.CommandSender;

/**
 * Created on 2016-01-02.
 */
public class HelpTest implements CommandHandler {
    private CommandLib lib;

    public HelpTest(CommandLib lib) {
        this.lib = lib;
    }

    @CommandHandle(command = {"SpigotCommandLibTest", "help"}, permission = "test.help", description = "Get information about this plugins commands.")
    public void bc(CommandSender sender, @ArgDescription(name = "searchQuery") String... searchQuery) {
        this.lib.sendHelpMessage(sender, searchQuery);
    }
}
