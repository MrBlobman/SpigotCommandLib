package io.github.mrblobman.spigotcommandlib.util;

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChatUtils {

    public static String transformColors(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    public static String[] splitLines(String in) {
        return in.split("\n");
    }

    public static List<String> splitAndColorLines(String in) {
        return Arrays.stream(splitLines(in))
                .map(ChatUtils::transformColors)
                .collect(Collectors.toList());
    }
}
