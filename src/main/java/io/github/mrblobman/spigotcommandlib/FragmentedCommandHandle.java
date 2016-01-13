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
package io.github.mrblobman.spigotcommandlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks this method as a fragment handler for a command that is executed in
 * multiple parts. These marked methods should belong to a class that
 * implements {@link FragmentedCommandHandler}
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FragmentedCommandHandle {

    /**
     * The state in which the user must be in to invoke this
     * command.
     * @return the state required to use this fragment.
     */
    int state() default 0;

    /**
     * Specifies the sub command that this method handles.
     * Ex: /baseCommand subCmd1 subCmd2 = <code>new String[] {"baseCommand|baseAlias1", "subCmd1|alias1|alias2", "subCmd2"}</code>
     * @return a string array containing the full sub command that this method handles
     */
    String[] command() default {};

    /**
     * Specifies the permission required by the executer to successfully
     * execute this sub command.
     * Ex: my.subcommands.permission
     * @return the String representation of the required permission
     */
    String permission() default "";

    /**
     * A short description about what this command does.
     * @return the description
     */
    String description();
}
