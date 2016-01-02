package io.github.mrblobman.spigotcommandlib.commands;

import io.github.mrblobman.spigotcommandlib.CommandHandle;
import io.github.mrblobman.spigotcommandlib.CommandHandler;
import io.github.mrblobman.spigotcommandlib.args.ArgDescription;
import org.bukkit.command.CommandSender;

/**
 * Created on 2016-01-02.
 */
public class ArgDescTest implements CommandHandler {

    @CommandHandle(command = "argdesctest|adtest", permission = "test.argdesc", description = "Test the argument descriptions.")
    public void argDescTest(CommandSender sender,
                            @ArgDescription(description = {"Type in the title of what", "you want echoed."}, name = "echoRespTitle") String sampleArg,
                            @ArgDescription(description = {"Type in what you want", "echoed."}, name = "echoResp") String... rest) {
        sender.sendMessage(sampleArg);
        sender.sendMessage(rest);
    }
}
