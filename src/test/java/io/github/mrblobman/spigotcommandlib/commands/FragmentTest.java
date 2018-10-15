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

import io.github.mrblobman.spigotcommandlib.FragmentExecutionContext;
import io.github.mrblobman.spigotcommandlib.FragmentedCommandHandle;
import io.github.mrblobman.spigotcommandlib.FragmentedCommandHandler;
import io.github.mrblobman.spigotcommandlib.args.ArgDescription;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FragmentTest implements FragmentedCommandHandler<FragmentTest.MyContext> {
    public static class MyContext extends FragmentExecutionContext {
        protected Set<Location> location;
    }

    private static final int DEFAULT_STATE = 0;
    private static final int LOC_SET_STATE = 1;

    @Override
    public void onCleanup(UUID id, MyContext context) {
        Bukkit.getPlayer(id).sendMessage("Please start the command again, you took too long.");
    }

    @FragmentedCommandHandle(state = DEFAULT_STATE, description = "Set the location")
    private void setLocation(MyContext context, Player sender, @ArgDescription(optional = true) String name) {
        if (name != null) sender.sendMessage(name);
        context.location = new HashSet<>();
        context.location.add(sender.getLocation());
        context.setState(LOC_SET_STATE);
        sender.sendMessage("Location set.");
    }

    @FragmentedCommandHandle(command = "setLocation", state = LOC_SET_STATE, description = "Set the location")
    private void setLocation2(MyContext context, Player sender, @ArgDescription(optional = true) String name) {
        if (name != null) sender.sendMessage(name);
        context.location.add(sender.getLocation());
        sender.sendMessage("Location added.");
    }

    @FragmentedCommandHandle(state = LOC_SET_STATE, description = "Set the block type to the block you are holding.")
    private void setType(MyContext context, Player sender) {
        if (sender.getItemInHand() == null || !sender.getItemInHand().getType().isBlock()) {
            sender.sendMessage("You must be holding a block.");
            return;
        }
        Material mat = sender.getItemInHand().getType();
        for (Location loc : context.location) {
            sender.getWorld().getBlockAt(loc).setType(mat);
        }
        sender.sendMessage("Blocks set!");
        context.setState(DEFAULT_STATE);
    }
}
