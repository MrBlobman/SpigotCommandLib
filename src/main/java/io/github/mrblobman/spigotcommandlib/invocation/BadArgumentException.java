package io.github.mrblobman.spigotcommandlib.invocation;

import io.github.mrblobman.spigotcommandlib.args.CommandParameter;

public class BadArgumentException extends CommandInvocationException {
    private final CommandParameter<?> param;
    private final String rawArg;

    public BadArgumentException(CommandParameter<?> param, String rawArg) {
        this.param = param;
        this.rawArg = rawArg;
    }

    public CommandParameter<?> getParam() {
        return param;
    }

    public String getRawArg() {
        return rawArg;
    }

    @Override
    public String getMessage() {
        return String.format("Bad argument value: '%s' for parameter %s.", this.rawArg, this.param.getDescriptiveName());
    }
}
