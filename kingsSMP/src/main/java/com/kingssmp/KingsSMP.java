package com.kingssmp;

import com.kingssmp.listeners.*;
import com.kingssmp.managers.DragonBoneManager;
import com.kingssmp.managers.RitualManager;
import com.kingssmp.managers.WeaponRegistry;
import com.kingssmp.weapons.LegendaryWeapon;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class KingsSMP extends JavaPlugin {

    private static KingsSMP instance;
    private WeaponRegistry weaponRegistry;
    private RitualManager ritualManager;
    private DragonBoneManager dragonBoneManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        weaponRegistry   = new WeaponRegistry(this);
        ritualManager    = new RitualManager(this);
        dragonBoneManager = new DragonBoneManager(this);

        weaponRegistry.register(new com.kingssmp.weapons.VoidBow(this));
        weaponRegistry.register(new com.kingssmp.weapons.Lifestealer(this));
        weaponRegistry.register(new com.kingssmp.weapons.GhostBlade(this));
        weaponRegistry.register(new com.kingssmp.weapons.DragonBoneBlade(this));
        weaponRegistry.register(new com.kingssmp.weapons.JudgementGavel(this));
        weaponRegistry.register(new com.kingssmp.weapons.PrettyKittyBlade(this));

        getServer().getPluginManager().registerEvents(new WeaponInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new WeaponCombatListener(this), this);
        getServer().getPluginManager().registerEvents(new WeaponCraftListener(this), this);
        getServer().getPluginManager().registerEvents(new StorageBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new DragonDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);

        getLogger().info("KingsSMP Legendary Weapons loaded!");
    }

    @Override
    public void onDisable() {
        ritualManager.cancelAll();
        dragonBoneManager.cleanup();
        getLogger().info("KingsSMP disabled.");
    }

    // ─── COMMANDS ─────────────────────────────────────────────────────────────

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("kingssmp")) return false;
        if (!sender.hasPermission("kingssmp.admin")) {
            sender.sendMessage(Component.text("No permission.").color(NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            // /kingssmp give <weapon> <player>
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /kingssmp give <weapon> <player>").color(NamedTextColor.RED));
                    return true;
                }
                Player target = getServer().getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage(Component.text("Player not found: " + args[2]).color(NamedTextColor.RED));
                    return true;
                }
                LegendaryWeapon weapon = weaponRegistry.getByName(args[1]);
                if (weapon == null) {
                    sender.sendMessage(Component.text("Unknown weapon: " + args[1]).color(NamedTextColor.RED));
                    sendWeaponList(sender);
                    return true;
                }
                target.getInventory().addItem(weapon.buildItem());
                sender.sendMessage(Component.text("Gave " + weapon.getDisplayName() + " to " + target.getName() + ".").color(NamedTextColor.GREEN));
            }

            // /kingssmp recipe enable|disable|list [weapon]
            case "recipe" -> {
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /kingssmp recipe <enable|disable|list> [weapon]").color(NamedTextColor.RED));
                    return true;
                }
                switch (args[1].toLowerCase()) {

                    case "enable" -> {
                        if (args.length < 3) { sender.sendMessage(Component.text("Usage: /kingssmp recipe enable <weapon>").color(NamedTextColor.RED)); return true; }
                        LegendaryWeapon w = weaponRegistry.getByName(args[2]);
                        if (w == null) { sender.sendMessage(Component.text("Unknown weapon: " + args[2]).color(NamedTextColor.RED)); sendWeaponList(sender); return true; }
                        if (weaponRegistry.isRecipeEnabled(w.getId())) {
                            sender.sendMessage(Component.text("Recipe for " + w.getDisplayName() + " is already enabled.").color(NamedTextColor.YELLOW));
                            return true;
                        }
                        weaponRegistry.enableRecipe(w.getId());
                        sender.sendMessage(Component.text("✦ Recipe ENABLED for " + w.getDisplayName() + ". Players can now craft it!").color(NamedTextColor.GREEN));
                        // Broadcast to all online players
                        getServer().broadcast(
                            Component.text("⚒ A new legendary weapon recipe has been unlocked: ")
                                .color(NamedTextColor.GOLD)
                                .append(Component.text(w.getDisplayName()).color(NamedTextColor.LIGHT_PURPLE))
                                .append(Component.text("!").color(NamedTextColor.GOLD))
                        );
                    }

                    case "disable" -> {
                        if (args.length < 3) { sender.sendMessage(Component.text("Usage: /kingssmp recipe disable <weapon>").color(NamedTextColor.RED)); return true; }
                        LegendaryWeapon w = weaponRegistry.getByName(args[2]);
                        if (w == null) { sender.sendMessage(Component.text("Unknown weapon: " + args[2]).color(NamedTextColor.RED)); sendWeaponList(sender); return true; }
                        if (!weaponRegistry.isRecipeEnabled(w.getId())) {
                            sender.sendMessage(Component.text("Recipe for " + w.getDisplayName() + " is already disabled.").color(NamedTextColor.YELLOW));
                            return true;
                        }
                        weaponRegistry.disableRecipe(w.getId());
                        sender.sendMessage(Component.text("✦ Recipe DISABLED for " + w.getDisplayName() + ".").color(NamedTextColor.RED));
                    }

                    case "list" -> {
                        sender.sendMessage(Component.text("══ KingsSMP Recipe Status ══").color(NamedTextColor.GOLD));
                        for (LegendaryWeapon w : weaponRegistry.getAll()) {
                            boolean enabled  = weaponRegistry.isRecipeEnabled(w.getId());
                            boolean crafted  = weaponRegistry.hasBeenCrafted(w.getId());
                            boolean noRecipe = w.getId().equals("dragonbone_blade");

                            Component status;
                            if (noRecipe) {
                                status = Component.text("  [Dragon Drop]").color(NamedTextColor.DARK_PURPLE);
                            } else if (enabled) {
                                status = Component.text("  ✔ Enabled").color(NamedTextColor.GREEN);
                            } else {
                                status = Component.text("  ✘ Disabled").color(NamedTextColor.RED);
                            }

                            Component craftedTag = crafted
                                    ? Component.text(" [Already Crafted]").color(NamedTextColor.DARK_GRAY)
                                    : Component.empty();

                            sender.sendMessage(
                                Component.text("  " + w.getDisplayName()).color(NamedTextColor.YELLOW)
                                    .append(status)
                                    .append(craftedTag)
                            );
                        }
                        sender.sendMessage(Component.text("  Tip: /kingssmp recipe enable <weapon>").color(NamedTextColor.GRAY));
                    }

                    default -> sender.sendMessage(Component.text("Usage: /kingssmp recipe <enable|disable|list> [weapon]").color(NamedTextColor.RED));
                }
            }

            // /kingssmp status
            case "status" -> {
                sender.sendMessage(Component.text("══ KingsSMP Weapon Status ══").color(NamedTextColor.GOLD));
                for (LegendaryWeapon w : weaponRegistry.getAll()) {
                    boolean crafted = weaponRegistry.hasBeenCrafted(w.getId());
                    boolean enabled = weaponRegistry.isRecipeEnabled(w.getId());
                    sender.sendMessage(
                        Component.text("  " + w.getDisplayName() + " ")
                            .color(NamedTextColor.YELLOW)
                            .append(Component.text(crafted ? "Crafted " : "Uncrafted ")
                                .color(crafted ? NamedTextColor.RED : NamedTextColor.GREEN))
                            .append(Component.text("| Recipe: ")
                                .color(NamedTextColor.GRAY))
                            .append(Component.text(enabled ? "On" : "Off")
                                .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED))
                    );
                }
            }

            // /kingssmp reload
            case "reload" -> {
                reloadConfig();
                sender.sendMessage(Component.text("Config reloaded.").color(NamedTextColor.GREEN));
            }

            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("══ KingsSMP Commands ══").color(NamedTextColor.GOLD));
        sender.sendMessage(Component.text("  /kingssmp give <weapon> <player>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /kingssmp recipe enable <weapon>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /kingssmp recipe disable <weapon>").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /kingssmp recipe list").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /kingssmp status").color(NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  /kingssmp reload").color(NamedTextColor.YELLOW));
    }

    private void sendWeaponList(CommandSender sender) {
        sender.sendMessage(Component.text("Available weapons: ").color(NamedTextColor.GRAY)
            .append(Component.text(
                String.join(", ", weaponRegistry.getAll().stream()
                    .map(LegendaryWeapon::getId).toList())
            ).color(NamedTextColor.WHITE)));
    }

    public static KingsSMP getInstance() { return instance; }
    public WeaponRegistry getWeaponRegistry() { return weaponRegistry; }
    public RitualManager getRitualManager() { return ritualManager; }
    public DragonBoneManager getDragonBoneManager() { return dragonBoneManager; }
}
