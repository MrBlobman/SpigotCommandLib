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
package io.github.mrblobman.spigotcommandlib;

import io.github.mrblobman.spigotcommandlib.commands.*;
import io.github.mrblobman.spigotcommandlib.registry.CommandLib;
import org.bukkit.plugin.java.JavaPlugin;

public class TestPlugin extends JavaPlugin {
    private CommandLib lib;

    @Override
    public void onEnable() {
        this.lib = new CommandLib(this);
        this.lib.registerCommandHandler(new VarargsTest());
        this.lib.registerCommandHandler(new TypeTest());
        this.lib.registerCommandHandler(new ArgDescTest());
        this.lib.registerCommandHandler(new HelpTest(lib));
        this.lib.registerCommandHandler(new OptionalTest(this));
        String[] SUB_TEST_CMD = new String[]{ "subtest|st" };
        this.lib.registerSubCommandHandler(new SubCommandTest(), "subcommand.test", SUB_TEST_CMD);
        this.lib.registerSubCommandHandler(new SubCommandTest2(), SUB_TEST_CMD);
        this.lib.registerFragmentedCommandHandler(new FragmentTest(),
                CommandLib.NO_PERMISSION,
                10000L, FragmentTest.MyContext::new, "fragtest|ft");
    }

    @Override
    public void onDisable() { }
}
