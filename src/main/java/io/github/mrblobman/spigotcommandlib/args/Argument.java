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
package io.github.mrblobman.spigotcommandlib.args;

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

/**
 * Created on 11/12/2015.
 */
public class Argument<T> {
    public static final int REQUIRED = 0;
    public static final int OPTIONAL = 1;
    public static final int VAR_ARGS = 2;

    private ArgumentFormatter<T> formatter;
    private Class type;
    private String name;
    private String[] desc;
    private boolean isOptional;
    private boolean isVarArgs;

    public Argument(ArgumentFormatter<T> formatter, Class argClass, String name, String[] desc, int type) {
        this.formatter = formatter;
        this.type = argClass;
        this.name = name;
        this.desc = desc;
        if (type == OPTIONAL) {
            isOptional = true;
            isVarArgs = false;
        } else if (type == VAR_ARGS) {
            isOptional = true;
            isVarArgs = true;
        } else {
            isOptional = false;
            isVarArgs = false;
        }
    }

    /**
     * Equivalent to calling {@link Argument#Argument(ArgumentFormatter, Class, String, String[], int)}
     * with the description derived from the formatter.
     */
    public Argument(ArgumentFormatter<T> formatter, Class argClass, String name, int type) {
        this(formatter, argClass, name, null, type);
        this.desc = new String[formatter.getTypeDesc().length+1];
        this.desc[0] = ChatColor.YELLOW + formatter.getTypeName();
        for (int i = 1; i < this.desc.length; i++)
            this.desc[i] = ChatColor.GRAY + formatter.getTypeDesc()[i-1];
    }

    /**
     * The name of the argument. Each argument should have a unique name.
     * @return the name of this argument.
     */
    public String getName() {
        return name;
    }

    /**
     * A descriptive name is a name that implies additional
     * information about the argument. For example [argname] vs &lt;argname&gt;.
     * @return the descriptive name for this argument
     */
    public String getDescriptiveName() {
        if (isVarArgs())        return "[" + getName() + "]...";
        else if (isOptional())  return "[" + getName() + "]";
        else                    return "<" + getName() + ">";
    }

    /**
     * Get a description of the use of the argument. This
     * explains the value for the argument.
     * @return the description of the argument.
     */
    public String[] getDescription() {
        return this.desc;
    }

    /**
     * Get the {@link ArgumentFormatter} for this argument. It can
     * be used to check if an argument value can be parsed for this
     * argument as well as actually doing the parsing.
     * @return the argument formatter for this argument.
     */
    public ArgumentFormatter<T> getFormatter() {
        return this.formatter;
    }

    /**
     * Get the type that this argument is declared as.
     * @return the type this argument is declared as
     */
    public Class getArgumentType() {
        return this.type;
    }

    /**
     * Check if this argument is an argument of varying length.
     * @return true iff this argument is of varying length, false otherwise.
     */
    public boolean isVarArgs() {
        return this.isVarArgs;
    }

    /**
     * Check if this argument is optional or not.
     * if {@link #isVarArgs()} returns true, isOptional() will also return true
     * @return true iff this argument is optional, false otherwise.
     */
    public boolean isOptional() {
        return this.isOptional;
    }

    @Override
    public String toString() {
        return "Argument{" +
                "formatter=" + formatter +
                ", name='" + name + '\'' +
                ", desc=" + Arrays.toString(desc) +
                ", isOptional=" + isOptional +
                ", isVarArgs=" + isVarArgs +
                '}';
    }
}
