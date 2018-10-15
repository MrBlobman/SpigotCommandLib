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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import io.github.mrblobman.spigotcommandlib.FragmentExecutionContext;
import io.github.mrblobman.spigotcommandlib.FragmentedCommandContextSupplier;
import io.github.mrblobman.spigotcommandlib.FragmentedCommandHandler;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class FragmentBundle<T extends FragmentExecutionContext> implements Invoker {
    private static final int CACHE_SPEC_CONCURRENCY_LEVEL = 1;
    /* The term cache may be a little bit misleading, we
    really just need a time based evicting map and this lib is
    bundled with spigot.
     */
    //name -> context
    private Cache<UUID, T> openContexts;
    private FragmentedCommandContextSupplier<T> contextGenerator;
    private Map<SubCommand, FragmentHandleInvoker[]> invokers;
    private Set<SubCommand> hasDefaultStateHandler;
    private FragmentedCommandHandler<T> handler;

    FragmentBundle(FragmentedCommandHandler<T> handler, long timeout, FragmentedCommandContextSupplier<T> contextGenerator) {
        this.openContexts = CacheBuilder.newBuilder()
                .concurrencyLevel(CACHE_SPEC_CONCURRENCY_LEVEL)
                .expireAfterAccess(timeout, TimeUnit.MILLISECONDS)
                .removalListener((RemovalNotification<UUID, T> notification) ->
                        handler.onCleanup(notification.getKey(), notification.getValue()))
                .build();
        this.contextGenerator = contextGenerator;
        this.invokers = new HashMap<>();
        this.hasDefaultStateHandler = new HashSet<>();
        this.handler = handler;
    }

    FragmentBundle(FragmentedCommandHandler<T> handler, FragmentedCommandContextSupplier<T> contextGenerator) {
        this.openContexts = CacheBuilder.newBuilder()
                .concurrencyLevel(CACHE_SPEC_CONCURRENCY_LEVEL)
                .removalListener((RemovalNotification<UUID, T> notification) ->
                        handler.onCleanup(notification.getKey(), notification.getValue()))
                .build();
        this.contextGenerator = contextGenerator;
        this.invokers = new HashMap<>();
        this.hasDefaultStateHandler = new HashSet<>();
        this.handler = handler;
    }

    /**
     * Add a sub command handler set to this bundle.
     *
     * @param command  the subcommand that will invoke an invoker
     * @param invokers an array of invokers with the invoker for state 0 at index 0,
     *                 1 at index 1 etc.
     */
    protected void addSubCommand(SubCommand command, FragmentHandleInvoker[] invokers) {
        if (invokers[0] != null) {
            //This is a default state command.
            this.hasDefaultStateHandler.add(command);
        }
        this.invokers.put(command, invokers);
    }

    /**
     * Get the context bound to the sender.
     *
     * @param sender the command sender.
     *
     * @return the context for the sender or new context if there
     *         is no existing context.
     */
    public T getContext(Player sender) {
        T context = openContexts.getIfPresent(sender.getUniqueId());
        if (context == null) return createContext(sender);
        return context;
    }

    private T createContext(Player sender) {
        T context = contextGenerator.get();
        openContexts.put(sender.getUniqueId(), context);
        return context;
    }

    /**
     * Invoke the appropriate {@link FragmentHandleInvoker} for the sender
     * who sent the given args
     *
     * @param command the command in this bundle that is being invoked.
     * @param sender  the command sender who is invoking this command
     * @param args    the arguments passed into the command.
     *
     * @return true if an invocation was preformed. false if there is nothing to invoke.
     *
     * @throws Exception if any errors occur during invocation
     */
    @Override
    public boolean invoke(SubCommand command, CommandSender sender, String[] args) throws Exception {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This can only be executed by a Player. You are a(n) " + sender.getClass().getSimpleName() + ".");
            return true;
        }
        Player player = (Player) sender;
        T context = getContext(player);
        FragmentHandleInvoker invoker = getInvocationHandler(command, context.getState());
        if (invoker == null) {
            if (this.hasDefaultStateHandler.contains(command)) {
                createContext(player);
                invoker = getInvocationHandler(command, 0);
            } else {
                return false;
            }
        }
        //Intellij doesn't realize that hasDefaultStateHandler -> getInvocationHandler(command, 0) != null
        //noinspection ConstantConditions
        invoker.invoke(context, sender, args);
        return true;
    }

    @Override
    public void sendDescription(SubCommand command, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This can only be executed by a Player. You are a(n) " + sender.getClass().getSimpleName() + ".");
            return;
        }
        T context = getContext((Player) sender);
        FragmentHandleInvoker[] invokers = this.invokers.get(command);
        if (invokers == null) {
            sender.sendMessage(ChatColor.RED + "Unknown command.");
            return;
        }
        if (invokers.length <= context.getState() || context.getState() < 0 || invokers[context.getState()] == null) {
            //The description should simply exclude illegal states
            //sender.sendMessage(ChatColor.RED + "Illegal state. You cannot execute this command in your current state.");
            return;
        }
        invokers[context.getState()].sendDescription(command, sender);
    }

    private FragmentHandleInvoker getInvocationHandler(SubCommand command, int state) {
        if (state < 0) return null;
        FragmentHandleInvoker[] invokers = this.invokers.get(command);
        if (invokers == null) return null;
        if (invokers.length <= state) return null;
        return invokers[state];
    }

    public void removeContext(UUID owner) {
        this.openContexts.invalidate(owner);
    }
}
