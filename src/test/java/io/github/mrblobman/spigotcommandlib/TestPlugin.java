package io.github.mrblobman.spigotcommandlib;

import io.github.mrblobman.spigotcommandlib.commands.ArgDescTest;
import io.github.mrblobman.spigotcommandlib.commands.HelpTest;
import io.github.mrblobman.spigotcommandlib.commands.TypeTest;
import io.github.mrblobman.spigotcommandlib.commands.VarargsTest;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 2016-01-02.
 */
public class TestPlugin extends JavaPlugin {
    private CommandLib lib;

    @Override
    public void onEnable() {
        this.lib = new CommandLib(this);
        this.lib.registerCommandHandler(new VarargsTest());
        this.lib.registerCommandHandler(new TypeTest());
        this.lib.registerCommandHandler(new ArgDescTest());
        this.lib.registerCommandHandler(new HelpTest(lib));
    }

    @Override
    public void onDisable() { }
}
