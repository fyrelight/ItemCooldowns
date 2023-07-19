package uk.fyrelight.itemcooldowns.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import uk.fyrelight.itemcooldowns.ItemCooldownsPlugin;
import uk.fyrelight.itemcooldowns.objects.MaterialCooldown;
import uk.fyrelight.itemcooldowns.objects.Messages;

import java.util.*;

public class CooldownsListener implements Listener {
    private static final Map<Material, List<MaterialCooldown>> consumablesMap = new HashMap<>();
    private static final Map<EntityType, List<MaterialCooldown>> projectilesMap = new HashMap<>();

    public static void reload() {
        consumablesMap.clear();
        projectilesMap.clear();
        registerConsumables();
        registerProjectiles();
    }

    public static void registerConsumables() {
        ItemCooldownsPlugin plugin = ItemCooldownsPlugin.getInstance();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("consumables");
        if (section == null) {
            plugin.getLogger().warning("Invalid configuration for consumables.");
            return;
        }

        for (String consumableName : section.getKeys(false)) {
            Material consumable = Material.getMaterial(consumableName.toUpperCase(Locale.ROOT));
            if (consumable == null) {
                plugin.getLogger().warning("Invalid material for consumable: " + consumableName);
                continue;
            }

            ConfigurationSection cooldownSection = section.getConfigurationSection(consumableName);
            if (cooldownSection == null) {
                plugin.getLogger().warning("Invalid configuration for consumable: " + consumableName);
                continue;
            }

            List<MaterialCooldown> materials = new ArrayList<>();
            for (String matName : cooldownSection.getKeys(false)) {
                Material material = Material.getMaterial(matName.toUpperCase(Locale.ROOT));
                if (material == null) {
                    plugin.getLogger().warning("Invalid material " + matName + " for consumable: " + consumableName);
                    continue;
                }

                int seconds = cooldownSection.getInt(matName);
                if (seconds <= 0) {
                    plugin.getLogger().warning("Invalid cooldown " + matName + " for consumable: " + consumableName);
                    continue;
                }

                materials.add(new MaterialCooldown(material, seconds));
            }
            consumablesMap.put(consumable, materials);
        }
    }

    public static void registerProjectiles() {
        ItemCooldownsPlugin plugin = ItemCooldownsPlugin.getInstance();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("projectiles");
        if (section == null) {
            return;
        }

        for (String projectileName : section.getKeys(false)) {
            EntityType projectile;
            try {
                projectile = EntityType.valueOf(projectileName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid entity type for projectile: " + projectileName);
                continue;
            }

            ConfigurationSection cooldownSection = section.getConfigurationSection(projectileName);
            if (cooldownSection == null) {
                plugin.getLogger().warning("Invalid configuration for consumable: " + projectileName);
                continue;
            }

            List<MaterialCooldown> materials = new ArrayList<>();
            for (String matName : cooldownSection.getKeys(false)) {
                Material material = Material.getMaterial(matName.toUpperCase(Locale.ROOT));
                if (material == null) {
                    plugin.getLogger().warning("Invalid material " + matName + " for projectile: " + projectileName);
                    continue;
                }

                int seconds = cooldownSection.getInt(matName);
                if (seconds <= 0) {
                    plugin.getLogger().warning("Invalid cooldown " + matName + " for consumable: " + projectileName);
                    continue;
                }

                materials.add(new MaterialCooldown(material, seconds));
            }
            projectilesMap.put(projectile, materials);
        }
    }

    public CooldownsListener() {
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Material material = player.getInventory().getItem(event.getHand()).getType();
        int cooldown = player.getCooldown(material);
        if (cooldown > 0) {
            Messages.COOLDOWN.sendActionBar(player, cooldown / 20);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractAtEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.ARMOR_STAND) return;
        Player player = event.getPlayer();
        Material material = player.getInventory().getItem(event.getHand()).getType();
        int cooldown = player.getCooldown(material);
        if (cooldown > 0) {
            Messages.COOLDOWN.sendActionBar(player, cooldown / 20);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null) return;
        Player player = event.getPlayer();
        Material material = event.getItem().getType();
        int cooldown = player.getCooldown(material);
        if (cooldown > 0 && (event.useItemInHand() != PlayerInteractEvent.Result.ALLOW)) {
            Messages.COOLDOWN.sendActionBar(player, cooldown / 20);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemConsumed(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material consumable = event.getItem().getType();
        if (!consumablesMap.containsKey(consumable)) {
            return;
        }
        for (MaterialCooldown materialCooldown : consumablesMap.get(consumable)) {
            Material material = materialCooldown.getMaterial();
            int cooldown = materialCooldown.getCooldown() * 20;
            // Set cooldown to prevent the player spamming - Full amount so the message is correct
            player.setCooldown(material, cooldown);
            // Set it a tick later to prevent Vanilla overriding it
            Bukkit.getScheduler().runTaskLater(ItemCooldownsPlugin.getInstance(), () -> player.setCooldown(material, cooldown - 1), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunched(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (!(projectile.getShooter() instanceof Player player)) {
            return;
        }
        if (!projectilesMap.containsKey(projectile.getType())) {
            return;
        }
        for (MaterialCooldown materialCooldown : projectilesMap.get(projectile.getType())) {
            Material material = materialCooldown.getMaterial();
            int cooldown = materialCooldown.getCooldown() * 20;
            // Set cooldown to prevent the player spamming - Full amount so the message is correct
            player.setCooldown(material, cooldown);
            // Set it a tick later to prevent Vanilla overriding it
            Bukkit.getScheduler().runTaskLater(ItemCooldownsPlugin.getInstance(), () -> player.setCooldown(material, cooldown - 1), 1);
        }
    }
}
