package io.github.mrblobman.spigotcommandlib.registry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.mrblobman.spigotcommandlib.args.CommandParameter;
import io.github.mrblobman.spigotcommandlib.invocation.ArgumentCountException;
import io.github.mrblobman.spigotcommandlib.invocation.BadArgumentException;
import io.github.mrblobman.spigotcommandlib.invocation.BadCommandSenderException;
import io.github.mrblobman.spigotcommandlib.invocation.CommandMethodHandle;
import io.github.mrblobman.spigotcommandlib.util.EnglishUtils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public abstract class BaseCommandExecutor implements CommandExecutor {
    private final SubCommand command;

    public BaseCommandExecutor(SubCommand command) {
        this.command = command;
    }

    public SubCommand getCommand() {
        return command;
    }

    @Override
    public SubCommand getTrigger() {
        return this.command;
    }

    @Override
    public boolean canExecute(CommandSender sender) {
        return getTrigger().canBeExecutedBy(sender);
    }

    protected void sendUsage(CommandSender sender, CommandMethodHandle handle) {
        StringBuilder executableStr = new StringBuilder(this.command.toExecutableString());

        for (CommandParameter<?> param : handle.getParameters())
            executableStr.append(" ").append(param.getDescriptiveName());

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.YELLOW + executableStr.toString());
            return;
        }

        ComponentBuilder message = new ComponentBuilder(this.command.toString());

        message.color(ChatColor.RED);
        message.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, executableStr.toString()));
        message.event(buildTooltip(ChatColor.YELLOW + this.command.toString(),
                ChatColor.GRAY + "Click to paste this command's",
                ChatColor.GRAY + "format in your chat box."));

        for (CommandParameter<?> arg : handle.getParameters()) {
            message.append(" " + arg.getDescriptiveName());
            message.event(buildTooltip(arg.getDescription()));
        }

        ((Player) sender).spigot().sendMessage(message.create());
    }

    protected void sendDescription(CommandSender sender, CommandMethodHandle handle) {
        sender.sendMessage(handle.getDescription().stream()
                .map(s -> ChatColor.AQUA + s)
                .toArray(String[]::new));

        StringBuilder executableStr = new StringBuilder(this.command.toExecutableString());

        for (CommandParameter<?> param : handle.getParameters())
            executableStr.append(" ").append(param.getDescriptiveName());

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.YELLOW + executableStr.toString());
            return;
        }

        ComponentBuilder message = new ComponentBuilder("    \u27A5" + this.command.toString());

        message.color(ChatColor.RED);
        message.event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, executableStr.toString()));
        message.event(buildTooltip(ChatColor.YELLOW + this.command.toString(),
                ChatColor.GRAY + "Click to paste this command's",
                ChatColor.GRAY + "format in your chat box."));

        for (CommandParameter<?> arg : handle.getParameters()) {
            message.append(" " + arg.getDescriptiveName());
            message.event(buildTooltip(arg.getDescription()));
        }

        ((Player) sender).spigot().sendMessage(message.create());
    }

    protected void sendBadCommandSender(CommandSender to, Class<?> expectedType, CommandSender actualSender) {
        to.sendMessage(ChatColor.RED + String.format("This can only be executed by %s. You are %s.",
                EnglishUtils.aOrAn(expectedType.getSimpleName()),
                EnglishUtils.aOrAn(actualSender.getClass().getSimpleName())
        ));
    }

    protected void sendBadCommandSenderHelp(CommandSender to, CommandMethodHandle handle, BadCommandSenderException e) {
        this.sendBadCommandSender(to, e.getExpectedSenderType(), e.getActualSender());
    }

    protected void sendBadArgumentCountHelp(CommandSender to, CommandMethodHandle handle, ArgumentCountException e) {
        to.sendMessage(ChatColor.RED + String.format("%s arguments. Expected %d but got %d.",
                e.isTooFew() ? "Too few" : "Too many",
                e.getAmtExpected(),
                e.getAmtGiven()
        ));
        this.sendUsage(to, handle);
    }

    protected void sendBadArgumentHelp(CommandSender to, CommandMethodHandle handle, BadArgumentException e) {
        to.sendMessage(ChatColor.RED + String.format("Incorrect argument format for parameter %s. Received: '%s'",
                e.getParam().getDescriptiveName(),
                e.getRawArg()
        ));
        this.sendUsage(to, handle);
    }

    protected static HoverEvent buildTooltip(String... lines) {
        return buildTooltip(Arrays.asList(lines));
    }

    /**
     * Build the HoverEvent that would result in the display
     * of lines when you hover over the component the event is
     * for.
     *
     * @param lines the lines in the tooltip
     *
     * @return the constructed hover event
     */
    protected static HoverEvent buildTooltip(List<String> lines) {
        JsonObject item = new JsonObject();
        item.addProperty("id", "minecraft:stone");
        item.addProperty("Count", 1);

        if (lines.isEmpty())
            return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{ new TextComponent(item.toString()) });

        JsonObject tag = new JsonObject();
        item.add("tag", tag);

        JsonObject display = new JsonObject();
        tag.add("display", display);

        display.addProperty("Name", net.md_5.bungee.api.ChatColor.WHITE + lines.get(0));
        if (lines.size() > 1) {
            JsonArray lore = new JsonArray();
            for (int i = 1; i < lines.size(); i++)
                lore.add(new JsonPrimitive(net.md_5.bungee.api.ChatColor.WHITE + lines.get(i)));
            display.add("Lore", lore);
        }
        return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[]{ new TextComponent(item.toString()) });
    }
}
