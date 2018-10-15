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

import org.bukkit.permissions.Permissible;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SubCommand {
    private Map<String, SubCommand> subCommands = new HashMap<>();
    private String name;
    private SubCommand superCommand;
    private Set<String> permissions;
    private List<String> aliases;

    SubCommand(String name, String[] aliases, String permission, SubCommand superCommand, SubCommand... subCommands) {
        this.name = name;
        this.aliases = new ArrayList<>();
        for (String alias : aliases) {
            this.aliases.add(alias.toLowerCase());
        }
        this.permissions = new HashSet<>();
        this.permissions.add(permission);
        for (SubCommand cmd : subCommands) {
            this.subCommands.put(cmd.getName().toLowerCase(), cmd);
        }
        this.superCommand = superCommand;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getAliases() {
        return Collections.unmodifiableList(this.aliases);
    }

    public boolean canBeInvokedBy(String name) {
        return this.name.equals(name) || this.aliases.contains(name);
    }

    /**
     * Check if {@code caller} has permission to execute this {@code SubCommand}
     * and all super commands.
     *
     * @param caller the caller executing the sub command
     *
     * @return true iff the called has permission to execute this and all super commands.
     */
    public boolean canExecute(Permissible caller) {
        for (String permission : this.permissions) {
            if (caller.hasPermission(permission) && (this.isBase() || this.superCommand.canExecute(caller)))
                return true;
        }
        return false;
    }

    public void addPermission(String permission) {
        this.permissions.add(permission);
    }

    public boolean removePermission(String permission) {
        return this.permissions.remove(permission);
    }

    public SubCommand getSubCommand(String name) {
        String lowerCaseName = name.toLowerCase();
        SubCommand cmd = this.subCommands.get(lowerCaseName);
        if (cmd != null) return cmd;
        else {
            for (SubCommand sub : this.subCommands.values()) {
                if (sub.getAliases().contains(lowerCaseName)) {
                    return sub;
                }
            }
        }
        return null;
    }

    public List<String> getSubCommands() {
        return new ArrayList<>(this.subCommands.keySet());
    }

    public void addSubCommand(SubCommand cmd) {
        this.subCommands.put(cmd.getName().toLowerCase(), cmd);
    }

    /**
     * The super command that directly leads this command.
     *
     * @return null if this SubCommand is a BaseCommand
     */
    public SubCommand getSuperCommand() {
        return this.superCommand;
    }

    public boolean isBase() {
        return this.superCommand == null;
    }

    @Override
    public String toString() {
        String name = this.name;
        if (!this.aliases.isEmpty()) {
            name = this.aliases.stream()
                    .collect(Collectors.joining("|", name + "|", ""));
        }
        if (this.superCommand == null) {
            return "/" + name;
        } else {
            return this.superCommand.toString() + " " + name;
        }
    }

    public String toExecutableString() {
        String name = this.name;
        if (this.superCommand == null) {
            return "/" + name;
        } else {
            return this.superCommand.toExecutableString() + " " + name;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubCommand)) return false;

        SubCommand that = (SubCommand) o;

        return subCommands.keySet().equals(that.subCommands.keySet())
                && name.equals(that.name)
                && permissions.equals(that.permissions)
                && aliases.equals(that.aliases);

    }

    @Override
    public int hashCode() {
        int result = subCommands.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + permissions.hashCode();
        result = 31 * result + aliases.hashCode();
        return result;
    }
}
