/*
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
package io.github.mrblobman.spigotcommandlib.commands;

import io.github.mrblobman.spigotcommandlib.CommandHandle;
import io.github.mrblobman.spigotcommandlib.CommandHandler;
import io.github.mrblobman.spigotcommandlib.args.ArgDescription;
import io.github.mrblobman.spigotcommandlib.registry.CommandLib;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HelpTest implements CommandHandler {
    private CommandLib lib;

    public HelpTest(CommandLib lib) {
        this.lib = lib;
    }

    @CommandHandle(command = { "SpigotCommandLibTest", "help" }, permission = "test.help", description = "Get information about this plugins commands.")
    public void help(CommandSender sender, @ArgDescription(name = "searchQuery") String... searchQuery) {
        this.lib.sendHelpMessage(sender, searchQuery);
    }

    @CommandHandle(command = { "rename" }, permission = "myplugin.commands.itemrename", description = "Rename the item you are holding.")
    public void rename(Player sender, String newName) {
        ItemStack item = sender.getItemInHand();
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "You must be holding an item.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', newName));
        item.setItemMeta(meta);
        sender.sendMessage(ChatColor.GREEN + "Item name set to " + newName);
    }
}
