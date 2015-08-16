package io.github.mrblobman.commandlib;

import java.util.regex.Pattern;

import org.bukkit.Color;

public class ArgumentFormatter<T> {
	
	public static final ArgumentFormatter<String> STRING = new ArgumentFormatter<String>(
			"^.*$",
			(String arg) -> {
				return arg;
			},
			"String",
			"A sequence of characters.",
			"Ex: IAmAString");
	public static final ArgumentFormatter<Integer> INTEGER = new ArgumentFormatter<Integer>(
			"^\\-?\\d+$",
			(String arg) -> {
				return Integer.parseInt(arg);
			},
			"Integer",
			"A sequence of digits 0-9 with",
			"an optional starting \"-\" sign.",
			"Ex: -4536 (Min: "+Integer.MIN_VALUE+" Max: "+Integer.MAX_VALUE+")");
	public static final ArgumentFormatter<Long> LONG = new ArgumentFormatter<Long>(
			"^\\-?\\d+$",
			(String arg) -> {
				return Long.parseLong(arg);
			},
			"Long",
			"A sequence of digits 0-9 with",
			"an optional starting \"-\" sign.",
			"Ex: -9287 (Min: "+Long.MIN_VALUE+" Max: "+Long.MAX_VALUE+")");
	public static final ArgumentFormatter<Short> SHORT = new ArgumentFormatter<Short>(
			"^\\-?\\d+$",
			(String arg) -> {
				return Short.parseShort(arg);
			},
			"Short",
			"A sequence of digits 0-9 with",
			"an optional starting \"-\" sign.",
			"Ex: -4536 (Min: "+Short.MIN_VALUE+" Max: "+Short.MAX_VALUE+")");
	public static final ArgumentFormatter<Double> DOUBLE = new ArgumentFormatter<Double>(
			"^\\-?\\d+(\\.(\\d)+)?$",
			(String arg) -> {
				return Double.parseDouble(arg);
			},
			"Double",
			"A sequence of digits 0-9 with an optional",
			"starting \"-\" sign and decimal portion.",
			"Ex: -93.2 (Min: "+Double.MIN_VALUE+" Max: "+Double.MAX_VALUE+")");
	public static final ArgumentFormatter<Float> FLOAT = new ArgumentFormatter<Float>(
			"^\\-?\\d+(\\.(\\d)+)?$",
			(String arg) -> {
				return Float.parseFloat(arg);
			},
			"Float",
			"A sequence of digits 0-9 with an optional",
			"starting \"-\" sign and decimal portion.",
			"Ex: -2.883 (Min: "+Float.MIN_VALUE+" Max: "+Float.MAX_VALUE+")");
	public static final ArgumentFormatter<Color> COLOR = new ArgumentFormatter<Color>(
			"^(0*)((1?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-5]))," +
			"(0*)((1?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-5]))," + 
			"(0*)((1?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-5]))$",
			(String arg) -> {
				String[] parts = arg.split(",");
				return Color.fromRGB(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
			},
			"Color",
			"3 number values ranging from 0-255",
			"seperated by commas representing the",
			"3 RGB values in the color.",
			"Ex: 0,255,13");
	
	private interface ArgumentParser<T> {
		public T parse(String arg);
	}
	
	private Pattern pattern;
	private ArgumentParser<T> parser;
	private String typeName;
	private String[] typeDesc;
	
	private ArgumentFormatter(String pattern, ArgumentParser<T> parser, String typeName, String... typeDesc) {
		this.pattern = Pattern.compile(pattern);
		this.parser = parser;
		this.typeName = typeName;
		this.typeDesc = typeDesc;
	}
	
	/**
	 * Check if {@code arg} can be parsed as this argument type.
	 * @param arg the argument to check parsability
	 * @return true iff {@link ArgumentType#parse(String)} will successfully complete for {@code arg}
	 */
	public boolean canBeParsedFrom(String arg) {
		return this.pattern.matcher(arg).matches();
	}
	
	/**
	 * Parse {@code arg} as this argument type.<br>
	 * This may throw all sorts of strange errors if arg is invalid.<br>
	 * To prevent this see {@link ArgumentType#canBeParsedFrom(String)}
	 * @param arg the argument to parse
	 * @return the parsed argument.
	 */
	public T parse(String arg) {
		return this.parser.parse(arg);
	}
	
	/**
	 * @return a nice name for the return type of this formatter
	 */
	public String getTypeName() {
		return this.typeName;
	}
	
	/**
	 * @return a short description of the string format accepted by this formatter
	 */
	public String[] getTypeDesc() {
		return this.typeDesc;
	}
}
