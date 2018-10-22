package io.github.mrblobman.spigotcommandlib.invocation;

public class ArgumentCountException extends CommandInvocationException {
    private final int amtGiven;
    private final int amtExpected;

    public ArgumentCountException(int amtGiven, int amtExpected) {
        this.amtGiven = amtGiven;
        this.amtExpected = amtExpected;
    }

    public boolean isTooFew() {
        return this.amtGiven < this.amtExpected;
    }

    public boolean isTooMany() {
        return this.amtGiven > this.amtExpected;
    }

    public int getAmtGiven() {
        return amtGiven;
    }

    public int getAmtExpected() {
        return amtExpected;
    }

    @Override
    public String getMessage() {
        return String.format("%d arguments were expected but %d were given.", this.amtExpected, this.amtGiven);
    }
}
