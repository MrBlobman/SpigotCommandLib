package io.github.mrblobman.spigotcommandlib.commands;

import io.github.mrblobman.spigotcommandlib.CommandHandle;
import io.github.mrblobman.spigotcommandlib.CommandHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Created on 2016-01-02.
 */
public class VarargsTest implements CommandHandler {

    @CommandHandle(command = "broadcast|bc", permission = "test.permission", description = "Send a message to the entire server.")
    public void bc(CommandSender sender, String... message) {
        StringBuilder msg = new StringBuilder();
        for (String part : message) msg.append(part).append(" ");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg.toString()));
    }
}
