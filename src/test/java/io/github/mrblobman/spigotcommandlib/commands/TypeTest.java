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
import org.bukkit.Color;
import org.bukkit.command.CommandSender;

/**
 * Created on 2016-01-02.
 */
public class TypeTest implements CommandHandler {

    @CommandHandle(command = "testbool", permission = "test.bool", description = "Test the parsing of a boolean argument")
    public void testbool(CommandSender sender, boolean bool1, Boolean bool2) {
        sender.sendMessage("bool1 = " + bool1);
        sender.sendMessage("bool2 = " + bool2);
        sender.sendMessage("bool1 && bool2 = " + (bool1 && bool2));
        sender.sendMessage("bool1 || bool2 = " + (bool1 || bool2));
    }

    @CommandHandle(command = "testint", permission = "test.int", description = "Test the parsing of a integer argument")
    public void testint(CommandSender sender, int int1, Integer int2) {
        sender.sendMessage("int1 = " + int1);
        sender.sendMessage("int2 = " + int2);
        sender.sendMessage("int1 + int2 = " + (int1 + int2));
    }

    @CommandHandle(command = "testlong", permission = "test.long", description = "Test the parsing of a long argument")
    public void testlong(CommandSender sender, long long1, Long long2) {
        sender.sendMessage("long1 = " + long1);
        sender.sendMessage("long2 = " + long2);
        sender.sendMessage("long1 + long2 = " + (long1 + long2));
    }

    @CommandHandle(command = "testshort", permission = "test.short", description = "Test the parsing of a short argument")
    public void testshort(CommandSender sender, short short1, Short short2) {
        sender.sendMessage("short1 = " + short1);
        sender.sendMessage("short2 = " + short2);
        sender.sendMessage("short1 + short2 = " + (short1 + short2));
    }

    @CommandHandle(command = "testfloat", permission = "test.float", description = "Test the parsing of a float argument")
    public void testfloat(CommandSender sender, float float1, Float float2) {
        sender.sendMessage("float1 = " + float1);
        sender.sendMessage("float2 = " + float2);
        sender.sendMessage("float1 + float2 = " + (float1 + float2));
    }

    @CommandHandle(command = "testdouble", permission = "test.double", description = "Test the parsing of a double argument")
    public void testdouble(CommandSender sender, double double1, Double double2) {
        sender.sendMessage("double1 = " + double1);
        sender.sendMessage("double2 = " + double2);
        sender.sendMessage("double1 + double2 = " + (double1 + double2));
    }

    @CommandHandle(command = "testcolor", permission = "test.color", description = "Test the parsing of a color argument")
    public void testcolor(CommandSender sender, Color color) {
        sender.sendMessage("color = " + color);
    }
}
