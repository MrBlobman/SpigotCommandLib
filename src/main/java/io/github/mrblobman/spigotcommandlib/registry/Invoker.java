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
package io.github.mrblobman.spigotcommandlib.registry;

import org.bukkit.command.CommandSender;

public interface Invoker {

    /**
     * Invoke this invoker for the given sender.
     *
     * @param command the command sent.
     * @param sender  the command sender.
     * @param args    the arguments passed to the command
     *
     * @return returns true if an invocation was attempted, false otherwise.
     *
     * @throws Exception if an error occurs during invocation
     */
    boolean invoke(SubCommand command, CommandSender sender, String[] args) throws Exception;

    /**
     * Send a description of the command that will be invoked
     * with this invoker.
     *
     * @param command send the sender a description of this command.
     * @param sender  the sender that could be sending the command.
     */
    void sendDescription(SubCommand command, CommandSender sender);
}
