package io.github.mrblobman.commandlib;

import javax.annotation.Nullable;

import org.bukkit.Color;

/**
 * A set of defined argument ids.
 */
public class ArgumentType {
	//Set to negative to make optional
	public static final int COLOR = 0;
	public static final int COLORED_STRING = 1;
	public static final int DOUBLE = 2;
	public static final int INTEGER = 3;
	public static final int LONG = 4;
	public static final int SHORT = 5;
	public static final int STRING = 6;
	
	@Nullable
	public static Class<?> getType(int argId) {
		switch (argId) {
		case COLOR: 
			return Color.class;
		case COLORED_STRING:
		case STRING:
			return String.class;
		case DOUBLE:
			return Double.class;
		case INTEGER:
			return Integer.class;
		case LONG:
			return Long.class;
		case SHORT:
			return Short.class;
		default:
			return null;
		}
	}
}
