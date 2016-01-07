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

import org.bukkit.Color;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

import static io.github.mrblobman.spigotcommandlib.args.ArgumentFormatter.*;

/**
 * Created on 2016-01-07.
 */
public class FormatterMapping {
    private static Map<Class, ArgumentFormatter> SUPPORTED_FORMATTERS = new HashMap<>();
    private static Map<Class, ArgumentFormatter> SUPPORTED_ARRAY_FORMATTERS = new HashMap<>();
    static {
        registerMapping(String.class, STRING);
        registerMapping(Boolean.class, BOOLEAN);
        registerMapping(boolean.class, BOOLEAN);
        registerMapping(Integer.class, INTEGER);
        registerMapping(int.class, INTEGER);
        registerMapping(Long.class, LONG);
        registerMapping(long.class, LONG);
        registerMapping(Short.class, SHORT);
        registerMapping(short.class, SHORT);
        registerMapping(Double.class, DOUBLE);
        registerMapping(double.class, DOUBLE);
        registerMapping(Float.class, FLOAT);
        registerMapping(float.class, FLOAT);
        registerMapping(Color.class, COLOR);
    }

    private static Class getArrayVersionOfClass(Class clazz) {
        return Array.newInstance(clazz, 0).getClass();
    }

    private static void registerMapping(Class clazz, ArgumentFormatter formatter) {
        SUPPORTED_FORMATTERS.put(clazz, formatter);
        SUPPORTED_ARRAY_FORMATTERS.put(getArrayVersionOfClass(clazz), formatter);
    }

    /**
     * Get the formatter that can format the given type.
     * @param type the type to lookup
     * @return the formatter used to format the given type.
     */
    public static ArgumentFormatter lookup(Class type) {
        return SUPPORTED_FORMATTERS.get(type);
    }

    /**
     * Get the formatter that can format the given type. The type
     * should be an array (but will simply return null if not) and
     * the lookup will return the formatter that can be used to
     * parse each element in the array.
     * @param type the type to lookup
     * @return the formatter used to format the given type.
     */
    public static ArgumentFormatter lookpArray(Class type) {
        return SUPPORTED_ARRAY_FORMATTERS.get(type);
    }
}
