package io.github.mrblobman.commandlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * For best usage help compile with parameter names.
 */
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.RUNTIME)
public @interface CommandHandler {
	
	/**
	 * Specifies the sub command that this method handles.
	 * Ex: /baseCommand subCmd1 subCmd2 = <code>new String[] {"baseCommand|baseAlias1", "subCmd1|alias1|alias2", "subCmd2"}</code>
	 * @return a string array containing the full sub command that this method handles
	 */
	String[] command();
	
	/**
	 * Specifies the permission required by the executer to successfully
	 * execute this sub command.
	 * Ex: my.subcommands.permission
	 * @return the String representation of the required permission
	 */
	String permission();
}
