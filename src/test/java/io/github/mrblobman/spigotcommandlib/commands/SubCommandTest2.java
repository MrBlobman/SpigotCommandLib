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

import io.github.mrblobman.spigotcommandlib.SubCommandHandle;
import io.github.mrblobman.spigotcommandlib.SubCommandHandler;
import org.bukkit.command.CommandSender;

public class SubCommandTest2 implements SubCommandHandler {
    @SubCommandHandle(description = "Testing subcommand handler 3.")
    private void subThree(CommandSender sender, String arg1, String arg2) {
        sender.sendMessage("Sub3: " + arg1);
    }

    @SubCommandHandle(command = { "subThree", "sub" }, description = "Testing subcommand handler 4.")
    private void subFour(CommandSender sender, String arg1, String arg2, String arg3) {
        sender.sendMessage("Sub4: " + arg1);
    }

    @SubCommandHandle(command = { "subThree", "sub2", "moreSubArgs" }, description = "Testing subcommand handler 5.")
    private void subFive(CommandSender sender, String arg1) {
        sender.sendMessage("Sub5: " + arg1);
    }
}
