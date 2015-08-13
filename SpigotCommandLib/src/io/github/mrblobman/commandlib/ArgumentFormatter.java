package io.github.mrblobman.commandlib;

import java.util.regex.Pattern;

import org.bukkit.Color;

import net.md_5.bungee.api.ChatColor;

public class ArgumentFormatter<T> {
	
	public static final ArgumentFormatter<String> STRING = new ArgumentFormatter<String>(
			"^.*$",
			(String arg) -> {
				return arg;
			});
	public static final ArgumentFormatter<String> COLORED_STRING = new ArgumentFormatter<String>(
			"^.*$",
			(String arg) -> {
				return ChatColor.translateAlternateColorCodes('&', arg);
			});
	public static final ArgumentFormatter<Integer> INTEGER = new ArgumentFormatter<Integer>(
			"^\\-?\\d+$",
			(String arg) -> {
				return Integer.parseInt(arg);
			});
	public static final ArgumentFormatter<Long> LONG = new ArgumentFormatter<Long>(
			"^\\-?\\d+$",
			(String arg) -> {
				return Long.parseLong(arg);
			});
	public static final ArgumentFormatter<Short> SHORT = new ArgumentFormatter<Short>(
			"^\\-?\\d+$",
			(String arg) -> {
				return Short.parseShort(arg);
			});
	public static final ArgumentFormatter<Double> DOUBLE = new ArgumentFormatter<Double>(
			"^\\-?\\d+(\\.(\\d)+)?$",
			(String arg) -> {
				return Double.parseDouble(arg);
			});
	public static final ArgumentFormatter<Color> COLOR = new ArgumentFormatter<Color>(
			"^(0*)((1?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-5]))," +
			"(0*)((1?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-5]))," + 
			"(0*)((1?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-5]))$",
			(String arg) -> {
				String[] parts = arg.split(",");
				return Color.fromRGB(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
			});
	
	private interface ArgumentParser<T> {
		public T parse(String arg);
	}
	
	private Pattern pattern;
	private ArgumentParser<T> parser;
	
	private ArgumentFormatter(String pattern, ArgumentParser<T> parser) {
		this.pattern = Pattern.compile(pattern);
		this.parser = parser;
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
}
