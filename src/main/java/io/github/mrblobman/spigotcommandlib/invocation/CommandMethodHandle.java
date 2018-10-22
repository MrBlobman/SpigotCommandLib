package io.github.mrblobman.spigotcommandlib.invocation;

import io.github.mrblobman.spigotcommandlib.args.CommandParameter;
import io.github.mrblobman.spigotcommandlib.args.CommandParameters;
import io.github.mrblobman.spigotcommandlib.args.ParseException;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandMethodHandle {
    private static final Map<Class<?>, Object> PRIMITIVE_DEFAULTS = new HashMap<>(8);

    static {
        PRIMITIVE_DEFAULTS.put(boolean.class, false);
        PRIMITIVE_DEFAULTS.put(char.class, '\0');
        PRIMITIVE_DEFAULTS.put(byte.class, (byte) 0);
        PRIMITIVE_DEFAULTS.put(short.class, (short) 0);
        PRIMITIVE_DEFAULTS.put(int.class, 0);
        PRIMITIVE_DEFAULTS.put(long.class, 0L);
        PRIMITIVE_DEFAULTS.put(float.class, 0f);
        PRIMITIVE_DEFAULTS.put(double.class, 0d);
    }

    private final Class<?> senderType;
    private final CommandParameters parameters;
    private final List<String> desc;
    private final MethodHandle executor;

    /**
     * Construct a new command handle. This is a wrapper around a method handle that parses
     * the arguments from strings.
     *
     * @param senderType the required type of the sender
     * @param parameters the command parameters
     * @param desc       the command description
     * @param executor   the method executor. This handle may return anything but has a strict format for the
     *                   arguments. See {@link CommandMethodHandle#invoke(CommandSender, List, List)}.
     */
    public CommandMethodHandle(Class<?> senderType, CommandParameters parameters, List<String> desc, MethodHandle executor) {
        this.senderType = senderType;
        this.parameters = parameters;
        this.desc = desc;
        this.executor = executor;
    }

    public Class<?> getSenderType() {
        return senderType;
    }

    public CommandParameters getParameters() {
        return parameters;
    }

    public List<String> getDescription() {
        return desc;
    }

    public MethodHandle getExecutor() {
        return executor;
    }

    /**
     * Invoke the underlying method handle with the {@code implicit} arguments, followed by the {@code sender}, and
     * finally
     * the parsed arguments.
     *
     * If this order is not appropriate then the method handle provided should be produced via a {@link
     * java.lang.invoke.MethodHandles#permuteArguments(MethodHandle, MethodType, int...)
     * MethodHandles#permuteArguments(MethodHandle, MethodType, int...)}.
     *
     * @param sender   the entity that is invoking the command
     * @param rawArgs  the arguments the {@code sender} gave when executing the command
     * @param implicit the implicit arguments that are supplied by the caller and need to be passed to the underlying
     *                 {@link #getExecutor() executor}.
     *
     * @throws CommandInvocationException if a error occurred while invoking the command or parsing the arguments.
     */
    public void invoke(CommandSender sender, List<String> rawArgs, List<?> implicit) throws CommandInvocationException {
        if (!this.senderType.isInstance(sender))
            throw new BadCommandSenderException(this.senderType, sender);

        List<Object> given = this.compileArgs(rawArgs);

        List<Object> args = new ArrayList<>(implicit.size() + 1 + given.size());

        args.addAll(implicit);
        args.add(sender);
        args.addAll(given);

        try {
            this.executor.invokeWithArguments(args);
        } catch (Throwable throwable) {
            throw new CommandInvocationException(throwable);
        }
    }

    private List<Object> compileArgs(List<String> rawArgs) throws CommandInvocationException {
        List<Object> args = new ArrayList<>();

        List<CommandParameter<?>> requiredParams = this.parameters.getRequiredParams();

        if (rawArgs.size() < requiredParams.size())
            throw new ArgumentCountException(rawArgs.size(), requiredParams.size());

        int rawArgIndex = 0;
        // There are at least enough rawArgs to fill the requireParams
        for (; rawArgIndex < requiredParams.size(); rawArgIndex++) {
            String rawArg = rawArgs.get(rawArgIndex);
            CommandParameter<?> cmdParam = requiredParams.get(rawArgIndex);

            Object arg;
            try {
                arg = cmdParam.getFormatter().parse(rawArg);
            } catch (ParseException e) {
                throw new BadArgumentException(cmdParam, rawArg);
            }

            args.add(arg);
        }

        List<CommandParameter<?>> optionalParams = this.parameters.getOptionalParams();

        for (; rawArgIndex < requiredParams.size() + optionalParams.size(); rawArgIndex++) {
            CommandParameter<?> cmdParam = optionalParams.get(rawArgIndex - requiredParams.size());

            if (rawArgIndex < rawArgs.size()) {
                // The optional arg is present
                String rawArg = rawArgs.get(rawArgIndex);
                Object arg;
                try {
                    arg = cmdParam.getFormatter().parse(rawArg);
                } catch (ParseException e) {
                    throw new BadArgumentException(cmdParam, rawArg);
                }

                args.add(arg);
            } else {
                // Provide the default
                args.add(PRIMITIVE_DEFAULTS.getOrDefault(cmdParam.getArgumentType(), null));
            }
        }

        if (this.parameters.hasVararg()) {
            CommandParameter<?> varargParam = this.parameters.getVarargParam();

            List<Object> vararg = new ArrayList<>(Math.max(0, rawArgs.size() - rawArgIndex));
            for (; rawArgIndex < rawArgs.size(); rawArgIndex++) {
                String rawArg = rawArgs.get(rawArgIndex);
                Object arg;
                try {
                    arg = varargParam.getFormatter().parse(rawArg);
                } catch (ParseException e) {
                    throw new BadArgumentException(varargParam, rawArg);
                }

                vararg.add(arg);
            }

            args.add(vararg.toArray((Object[]) Array.newInstance(varargParam.getFormatter().getParseType(), vararg.size())));
        } else if (rawArgIndex < rawArgs.size()) {
            throw new ArgumentCountException(rawArgs.size(), requiredParams.size() + optionalParams.size());
        }

        return args;
    }
}
