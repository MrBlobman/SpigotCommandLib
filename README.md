# SpigotCommandLib
A command library to increase the development rate and reduce boiler plate code for Spigot developers.

Now create commands as quickly as you can write a method. See the [wiki](https://github.com/MrBlobman/SpigotCommandLib/wiki) for more
information. Also, never worry about all of those boiler plater messages such as invalid use, not a player, can't parse arg as int etc.

Here is a quick sample for 3 commands:
* sum <arg0> <arg1>
* difference <argo> <arg1>
* rename <arg0>

```java
public class MyPluginCommands implements CommandHandler {

    @CommandHandle(command = {"sum|add"}, permission = "myplugin.commands.sum", description = "Make a quick calculation. Addition.")
    public void add(CommandSender sender, int firstNum, int secondNum) {
        sender.sendMessage(ChatColor.GREEN.toString() + firstNum + " + " + secondNum + " = " + (firstNum + secondNum));
    }
    
    @CommandHandle(command = {"subtract|sub|difference"}, permission = "myplugin.commands.sub", description = "Make a quick calculation. Subtraction")
    public void sub(CommandSender sender, int firstNum, int secondNum) {
        sender.sendMessage(ChatColor.GREEN.toString() + firstNum + " - " + secondNum + " = " + (firstNum - secondNum));
    }
    
    @CommandHandle(command = "rename", permission = "myplugin.commands.itemrename", description = "Rename the item you are holding.")
    public void rename(Player sender, String newName) {
        //Strings with spaces can be given like this: /rename "&bNew name for item"
        ItemStack item = sender.getItemInHand();
        if (item == null) {
            sender.sendMessage(ChatColor.RED + "You must be holding an item.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', newName));
        item.setItemMeta(meta);
        sender.sendMessage(ChatColor.GREEN + "Item name set to "+newName);
    }
}
```
And all you need to do is register them.
```java
public class SamplePlugin extends JavaPlugin {
    private CommandLib lib;

    @Override
    public void onEnable() {
        this.lib = new CommandLib(this);
        this.lib.registerCommandHandler(new MyPluginCommands());
    }
}
```
