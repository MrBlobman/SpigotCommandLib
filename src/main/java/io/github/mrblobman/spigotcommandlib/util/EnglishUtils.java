package io.github.mrblobman.spigotcommandlib.util;

import java.util.BitSet;

public class EnglishUtils {
    private static final BitSet VOWELS = new BitSet();
    static {
        VOWELS.set('a');
        VOWELS.set('e');
        VOWELS.set('i');
        VOWELS.set('o');
        VOWELS.set('u');
    }

    public static String aOrAn(String noun) {
        if (noun == null || noun.isEmpty())
            return noun;

        return VOWELS.get(noun.charAt(0)) ? "an " + noun : "a " + noun;
    }
}
