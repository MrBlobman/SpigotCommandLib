package io.github.mrblobman.spigotcommandlib.args;

import java.util.*;

public class CommandParameters implements Iterable<CommandParameter<?>> {
    public static CommandParameters fromList(List<CommandParameter<?>> params) {
        List<CommandParameter<?>> required = new ArrayList<>();
        List<CommandParameter<?>> optional = new ArrayList<>();
        CommandParameter<?> vararg = null;

        for (CommandParameter<?> param : params) {
            if (vararg != null)
                throw new IllegalArgumentException(String.format("Vararg argument %s must be the last argument in the command.", vararg.getName()));

            if (param.isVarArgs())
                vararg = param;
            else if (param.isOptional())
                optional.add(param);
            else if (!optional.isEmpty())
                throw new IllegalArgumentException(String.format("Required argument %s cannot follow an optional argument (%s).", param.getName(), optional.get(optional.size()).getName()));
            else
                required.add(param);
        }

        return new CommandParameters(
                required.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(required),
                optional.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(optional),
                vararg
        );
    }

    private final List<CommandParameter<?>> requiredParams;
    private final List<CommandParameter<?>> optionalParams;
    private final CommandParameter<?> varargParam;

    public CommandParameters(List<CommandParameter<?>> requiredParams, List<CommandParameter<?>> optionalParams, CommandParameter<?> varargParam) {
        this.requiredParams = requiredParams;
        this.optionalParams = optionalParams;
        this.varargParam = varargParam;
    }

    /**
     * The {@link io.github.mrblobman.spigotcommandlib.args.CommandParameterKind#REQUIRED REQUIRED} parameters. These
     * come first in the argument list.
     *
     * @return the required parameters.
     */
    public List<CommandParameter<?>> getRequiredParams() {
        return this.requiredParams;
    }

    /**
     * The {@link io.github.mrblobman.spigotcommandlib.args.CommandParameterKind#OPTIONAL OPTIONAL} parameters. These
     * come second in the argument list.
     *
     * @return the optional parameters.
     */
    public List<CommandParameter<?>> getOptionalParams() {
        return this.optionalParams;
    }

    /**
     * An optional {@link io.github.mrblobman.spigotcommandlib.args.CommandParameterKind#VAR_ARGS VAR_ARGS} parameter.
     * This comes last in the argument list.
     *
     * @return the vararg parameter if present.
     */
    public CommandParameter<?> getVarargParam() {
        return this.varargParam;
    }

    public boolean hasVararg() {
        return this.varargParam != null;
    }

    @Override
    public Iterator<CommandParameter<?>> iterator() {
        return new Iterator<CommandParameter<?>>() {
            private final Iterator<CommandParameter<?>> required = getRequiredParams().iterator();
            private final Iterator<CommandParameter<?>> optional = getOptionalParams().iterator();
            private boolean varargAvailable = hasVararg();

            @Override
            public boolean hasNext() {
                return required.hasNext()
                        || optional.hasNext()
                        || varargAvailable;
            }

            @Override
            public CommandParameter<?> next() {
                if (required.hasNext())
                    return required.next();

                if (optional.hasNext())
                    return optional.next();

                if (varargAvailable) {
                    varargAvailable = false;
                    return getVarargParam();
                }

                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public String toString() {
        return "CommandParameters{" +
                "requiredParams=" + requiredParams +
                ", optionalParams=" + optionalParams +
                ", varargParam=" + varargParam +
                '}';
    }
}
