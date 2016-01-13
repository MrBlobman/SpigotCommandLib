package io.github.mrblobman.spigotcommandlib.registry;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created on 2016-01-12.
 */
public class BundleCleaner implements Listener {
    private Collection<FragmentBundle> managedBundles;

    public BundleCleaner(Plugin owningPlugin) {
        managedBundles = new ArrayList<>();
        Bukkit.getPluginManager().registerEvents(this, owningPlugin);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        for (FragmentBundle bundle : managedBundles) {
            bundle.removeContext(event.getPlayer().getUniqueId());
        }
    }

    public void addBundle(FragmentBundle bundle) {
        managedBundles.add(bundle);
    }

    public void removeBundle(FragmentBundle bundle) {
        managedBundles.remove(bundle);
    }
}
