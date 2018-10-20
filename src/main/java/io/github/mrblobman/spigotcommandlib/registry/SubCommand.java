/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 MrBlobman
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

public class SubCommand implements Comparable<SubCommand> {
    private final Map<String, SubCommand> subCommands;
    private final String name;

    private final Set<String> permissions;
    private final List<String> aliases;

    private final SubCommand superCommand;
    private final int length;
    // Lowercase to make searches case insensitive
    private final Set<String> allNames;

    SubCommand(String name, String[] aliases, String permission, SubCommand superCommand, SubCommand... subCommands) {
        this.name = name;

        this.aliases = Arrays.stream(aliases)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        this.permissions = new LinkedHashSet<>();
        this.permissions.add(permission);

        this.subCommands = new LinkedHashMap<>();
        for (SubCommand cmd : subCommands)
            this.subCommands.put(cmd.getName().toLowerCase(), cmd);

        this.superCommand = superCommand;
        this.length = superCommand != null ? superCommand.length + 1 : 1;

        this.allNames = new LinkedHashSet<>();
        this.allNames.add(this.name.toLowerCase());
        this.aliases.stream()
                .map(String::toLowerCase)
                .forEach(this.allNames::add);
    }

    public String getName() {
        return this.name;
    }

    public List<String> getAliases() {
        return Collections.unmodifiableList(this.aliases);
    }

    public boolean canBeInvokedBy(String name) {
        return this.allNames.contains(name);
    }

    /**
     * Check if {@code caller} has permission to execute this {@code SubCommand}
     * and all super commands.
     *
     * @param caller the caller executing the sub command
     *
     * @return true iff the called has permission to execute this and all super commands.
     */
    public boolean canBeExecutedBy(Permissible caller) {
        return (this.isBase() || this.superCommand.canBeExecutedBy(caller))
                && this.permissions.stream().anyMatch(caller::hasPermission);
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

    public boolean startsWith(SubCommand other) {
        if (other.length > this.length)
            return false;

        // A length is always greater than 0
        SubCommand self = this;
        while (self.length > other.length)
            self = self.superCommand;

        // Both self and other have the same length, now we
        // just need to check that the super chain is the same
        while (self == other && self != null) {
            self = self.superCommand;
            other = other.superCommand;
        }

        // If self is null then we matched all the way to the end,
        // otherwise we exited the loop early and the commands are different
        return self == null;
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
        if (o == null || getClass() != o.getClass()) return false;
        SubCommand that = (SubCommand) o;
        return Objects.equals(allNames, that.allNames) &&
                Objects.equals(superCommand, that.superCommand) &&
                Objects.equals(permissions, that.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(allNames, superCommand, permissions);
    }

    @Override
    public int compareTo(SubCommand that) {
        return this.toExecutableString().compareToIgnoreCase(that.toExecutableString());
    }
}
