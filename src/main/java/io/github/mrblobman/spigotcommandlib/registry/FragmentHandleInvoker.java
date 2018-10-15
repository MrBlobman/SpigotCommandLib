/**
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

import io.github.mrblobman.spigotcommandlib.FragmentExecutionContext;
import io.github.mrblobman.spigotcommandlib.args.Argument;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created on 2016-01-07.
 */
public class FragmentHandleInvoker extends HandleInvoker {

    FragmentHandleInvoker(SubCommand subCmd, String cmdDesc, Object invocationTarget, Method cmdHandler, Class<?> senderType, Argument[] arguments) {
        super(subCmd, cmdDesc, invocationTarget, cmdHandler, senderType, arguments);
    }

    /**
     * Invoke this handler with the given arguments. The args are all String args that follow the sub command.<br>
     * Ex: /baseCmd sub1 sub2 arg0 arg1 arg2
     *
     * @param fragmentContext the context in which this command was executed.
     * @param sender          the command sender. If this type doesn't match the sender type it will inform the sender.
     * @param args            the args in which to invoke the handler with.
     *
     * @throws Exception if the method invocation fails for a non user based error, user based errors will directly be
     *                   messaged to the player.
     */
    public void invoke(FragmentExecutionContext fragmentContext, CommandSender sender, String[] args) throws Exception {
        if (!senderType.isInstance(sender)) {
            //Wrong sender type, cannot invoke
            sendIncorrectSenderMessage(sender);
            return;
        }

        List<Object> params = buildMethodParams(sender, args);
        if (params == null) return;

        int i = 0;
        Object[] callParams = new Object[params.size() + 2];
        callParams[i++] = fragmentContext;
        callParams[i++] = sender;
        for (Object param : params) {
            callParams[i++] = param;
        }
        method.invoke(invocationTarget, callParams);
    }

    /**
     * INVALID:
     * Must use {@link #invoke(FragmentExecutionContext, CommandSender, String[])}
     */
    @Deprecated
    @Override
    public boolean invoke(SubCommand command, CommandSender sender, String[] args) throws Exception {
        throw new UnsupportedOperationException("This method is invalid. Please use FragmentHandleInvoker#invoke(FragmentExecutionContext, CommandSender, String[])");
    }

    protected SubCommand getSubCommand() {
        return this.subCommand;
    }
}
