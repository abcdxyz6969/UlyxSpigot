package io.papermc.paper.command;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.ulyxspigot.ulyxspigot.UlyxConfig;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@DefaultQualifier(NonNull.class)
public final class UlyxCommand extends Command {
    private static final String BASE_PERMISSION = "bukkit.command.ulyx";

    public UlyxCommand(final String name) {
        super(name);
        this.description = "UlyxSpigot related commands";
        this.usageMessage = "/ulyx reload";
        this.setAliases(List.of("ulyxspigot"));
        this.setPermission(BASE_PERMISSION);

        final PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        if (pluginManager.getPermission(BASE_PERMISSION) == null) {
            pluginManager.addPermission(new Permission(BASE_PERMISSION, PermissionDefault.OP));
        }
        if (pluginManager.getPermission(BASE_PERMISSION + ".reload") == null) {
            pluginManager.addPermission(new Permission(BASE_PERMISSION + ".reload", PermissionDefault.OP));
        }
    }

    @Override
    public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args, final Location location) throws IllegalArgumentException {
        if (args.length <= 1) {
            return CommandUtil.getListMatchingLast(sender, args, List.of("reload"));
        }
        return List.of();
    }

    @Override
    public boolean execute(final CommandSender sender, final String commandLabel, final String[] args) {
        if (!testPermission(sender)) {
            return true;
        }

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(text("Usage: " + this.usageMessage, RED));
            return false;
        }

        if (!sender.hasPermission(BASE_PERMISSION + ".reload") && !sender.hasPermission(BASE_PERMISSION)) {
            sender.sendMessage(Bukkit.permissionMessage());
            return true;
        }

        try {
            final List<String> blockedOptions = UlyxConfig.getReloadBlockedOptionsFromFile();
            if (!blockedOptions.isEmpty()) {
                sender.sendMessage(text("[UlyxSpigot] Reload blocked. These options require restart while enabled: " + String.join(", ", blockedOptions), RED));
                sender.sendMessage(text("[UlyxSpigot] Set them to false then run /ulyx reload, or restart the server.", RED));
                return true;
            }

            UlyxConfig.reload();
            sender.sendMessage(text("[UlyxSpigot] Reloaded ulyxspigot/ulyxspigot.yml", GREEN));
        } catch (Throwable throwable) {
            sender.sendMessage(text("[UlyxSpigot] Reload failed. Check console for details.", RED));
            Bukkit.getLogger().severe("[UlyxSpigot] Failed to reload config via /ulyx reload: " + throwable.getMessage());
            throwable.printStackTrace();
        }

        return true;
    }
}
