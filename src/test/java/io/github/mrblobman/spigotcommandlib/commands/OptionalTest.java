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
package io.github.mrblobman.spigotcommandlib.commands;

import io.github.mrblobman.spigotcommandlib.CommandHandle;
import io.github.mrblobman.spigotcommandlib.CommandHandler;
import io.github.mrblobman.spigotcommandlib.args.ArgDescription;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

/**
 * Created on 2016-01-07.
 */
public class OptionalTest implements CommandHandler {
    private Plugin plugin;

    public OptionalTest(Plugin plugin) {
        this.plugin = plugin;
    }

    @CommandHandle(command = "optionaltest|ot", description = "Test the usage of optional arguments.")
    private void optionalTest(CommandSender sender,
                              String required,
                              @ArgDescription(optional = true) Integer times,
                              @ArgDescription(optional = true) long delay,
                              String... callback) {
        int timesArg = (times != null ? times : 1);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (int i = 0; i < timesArg; i++) sender.sendMessage(required);
            sender.sendMessage(callback);
        }, delay);
    }
}
