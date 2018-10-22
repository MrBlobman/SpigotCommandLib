package io.github.mrblobman.spigotcommandlib.invocation;

import org.bukkit.command.CommandSender;

public class BadCommandSenderException extends CommandInvocationException {
    private final Class<?> expectedType;
    private final CommandSender actual;

    public BadCommandSenderException(Class<?> expectedType, CommandSender actual) {
        this.expectedType = expectedType;
        this.actual = actual;
    }

    public Class<?> getExpectedSenderType() {
        return expectedType;
    }

    public CommandSender getActualSender() {
        return actual;
    }
}
