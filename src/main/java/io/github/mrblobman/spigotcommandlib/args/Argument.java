package io.github.mrblobman.spigotcommandlib.args;

import net.md_5.bungee.api.ChatColor;

/**
 * Created on 11/12/2015.
 */
public class Argument<T> {
    private ArgumentFormatter<T> formatter;
    private String name;
    private String[] desc;
    private boolean isVarArgs;


    public Argument(ArgumentFormatter<T> formatter, String name, String[] desc, boolean isVarArgs) {
        this.formatter = formatter;
        this.name = name;
        this.desc = desc;
        this.isVarArgs = isVarArgs;
    }

    /**
     * Equivalent to calling {@link Argument#Argument(ArgumentFormatter, String, String[], boolean)}
     * with the description derived from the formatter.
     */
    public Argument(ArgumentFormatter<T> formatter, String name, boolean isVarArgs) {
        this.formatter = formatter;
        this.name = name;
        this.desc = new String[formatter.getTypeDesc().length+1];
        this.desc[0] = ChatColor.YELLOW + formatter.getTypeName();
        for (int i = 1; i < this.desc.length; i++)
            this.desc[i] = ChatColor.GRAY + formatter.getTypeDesc()[i-1];
        this.isVarArgs = isVarArgs;
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
        if (isVarArgs()) return "[" + getName() + "]";
        else             return "<" + getName() + ">";
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
     * Check if this argument is an argument of varying length.
     * @return true iff this argument is of varying length, false otherwise.
     */
    public boolean isVarArgs() {
        return this.isVarArgs;
    }
}
