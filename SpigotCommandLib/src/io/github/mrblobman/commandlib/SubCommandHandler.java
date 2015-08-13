package io.github.mrblobman.commandlib;

public @interface SubCommandHandler {
	
	/**
	 * Specifies the argument types and amount of arguments for this sub command.
	 * @see ArgumentType
	 * @return
	 */
	int[] args();
}
