/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 MrBlobman
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

import org.bukkit.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ArgumentFormatter<T> {
    public static final ArgumentFormatter<String> STRING = new ArgumentFormatter<>(
            String.class,
            "^.*$",
            (String arg) -> arg,
            "String",
            "A sequence of characters.",
            "Ex: IAmAString or \"I Am A String\"");

    public static final ArgumentFormatter<Boolean> BOOLEAN = new ArgumentFormatter<>(
            Boolean.class,
            "^true|false|yes|no$",
            (String arg) -> {
                switch (arg.toLowerCase()) {
                    case "true":
                    case "yes":
                        return true;
                    default:
                        return false;
                }
            },
            "Boolean",
            "A true or false value.",
            "Ex: true, false, yes, no");

    public static final ArgumentFormatter<Integer> INTEGER = new ArgumentFormatter<>(
            Integer.class,
            "^\\-?\\d+$",
            Integer::parseInt,
            "Integer",
            "A sequence of digits 0-9 with",
            "an optional starting - sign.",
            "Ex: -4536 (Min: " + Integer.MIN_VALUE + " Max: " + Integer.MAX_VALUE + ")");

    public static final ArgumentFormatter<Long> LONG = new ArgumentFormatter<>(
            Long.class,
            "^\\-?\\d+$",
            Long::parseLong,
            "Long",
            "A sequence of digits 0-9 with",
            "an optional starting - sign.",
            "Ex: -9287 (Min: " + Long.MIN_VALUE + " Max: " + Long.MAX_VALUE + ")");

    public static final ArgumentFormatter<Short> SHORT = new ArgumentFormatter<>(
            Short.class,
            "^\\-?\\d+$",
            Short::parseShort,
            "Short",
            "A sequence of digits 0-9 with",
            "an optional starting - sign.",
            "Ex: -4536 (Min: " + Short.MIN_VALUE + " Max: " + Short.MAX_VALUE + ")");

    public static final ArgumentFormatter<Double> DOUBLE = new ArgumentFormatter<>(
            Double.class,
            "^\\-?\\d+(\\.(\\d)+)?$",
            Double::parseDouble,
            "Double",
            "A sequence of digits 0-9 with an optional",
            "starting - sign and decimal portion.",
            "Ex: -93.2 (Min: " + Double.MIN_VALUE + " Max: " + Double.MAX_VALUE + ")");

    public static final ArgumentFormatter<Float> FLOAT = new ArgumentFormatter<>(
            Float.class,
            "^\\-?\\d+(\\.(\\d)+)?$",
            Float::parseFloat,
            "Float",
            "A sequence of digits 0-9 with an optional",
            "starting - sign and decimal portion.",
            "Ex: -2.883 (Min: " + Float.MIN_VALUE + " Max: " + Float.MAX_VALUE + ")");

    public static final ArgumentFormatter<Color> COLOR = new ArgumentFormatter<>(
            Color.class,
            "^(0*)((1?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-5]))," +
                    "(0*)((1?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-5]))," +
                    "(0*)((1?[0-9]?[0-9])|([2][0-4][0-9])|(25[0-5]))$",
            (String arg) -> {
                String[] parts = arg.split(",");
                return Color.fromRGB(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            },
            "Color",
            "3 number values ranging from 0-255",
            "separated by commas representing the",
            "3 RGB values in the color.",
            "Ex: 0,255,13");

    private interface ArgumentParser<T> {
        T parse(String arg);
    }

    private Pattern pattern;
    private ArgumentParser<T> parser;
    private String typeName;
    private String[] typeDesc;
    private Class<T> formatType;

    private ArgumentFormatter(Class<T> parseType, String pattern, ArgumentParser<T> parser, String typeName, String... typeDesc) {
        this.formatType = parseType;
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        this.parser = parser;
        this.typeName = typeName;
        this.typeDesc = typeDesc;
    }

    /**
     * Check if {@code arg} can be parsed as this argument type.
     *
     * @param arg the argument to check parsability
     *
     * @return true iff {@link ArgumentFormatter#parse(String)} will successfully complete for {@code arg}
     */
    public boolean canBeParsedFrom(String arg) {
        return this.pattern.matcher(arg).matches();
    }

    /**
     * Parse {@code arg} as this argument type.<br>
     * This may throw all sorts of strange errors if arg is invalid.<br>
     * To prevent this see {@link ArgumentFormatter#canBeParsedFrom(String)}
     *
     * @param arg the argument to parse
     *
     * @return the parsed argument.
     */
    public T parse(String arg) {
        try {
            return this.parser.parse(arg);
        } catch (Exception e) {
            if (!(e instanceof ParseException)) throw new ParseException(e);
            throw e;
        }
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

    /**
     * Create and return a {@link List} with the type of this formatter.
     *
     * @return the created list.
     */
    public List<T> createTypedList() {
        return new ArrayList<>();
    }

    /**
     * Get the type that this formatter parses.
     *
     * @return the type of the parsed object.
     */
    public Class<T> getParseType() {
        return this.formatType;
    }

    @Override
    public String toString() {
        return "ArgumentFormatter{" +
                "typeName='" + typeName + '\'' +
                '}';
    }
}
