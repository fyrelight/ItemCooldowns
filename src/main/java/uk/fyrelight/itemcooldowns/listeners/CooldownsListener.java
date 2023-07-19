package uk.fyrelight.itemcooldowns.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import uk.fyrelight.itemcooldowns.ItemCooldownsPlugin;
import uk.fyrelight.itemcooldowns.objects.MaterialCooldown;

import java.util.*;

public class CooldownsListener implements Listener {
    private final ItemCooldownsPlugin plugin;
    private final Map<Material, List<MaterialCooldown>> consumablesMap = new HashMap<>();
    private final Map<EntityType, List<MaterialCooldown>> projectilesMap = new HashMap<>();

    public void reloadListener() {
        consumablesMap.clear();
        projectilesMap.clear();
        registerConsumables();
        registerProjectiles();
    }

    public void registerConsumables() {
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

    public void registerProjectiles() {
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

    public CooldownsListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
        registerConsumables();
        registerProjectiles();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getItem() == null) return;
        Player player = event.getPlayer();
        Material material = event.getItem().getType();
        int cooldown = player.getCooldown(material);
        if (cooldown > 0 && (!event.hasBlock() || event.useItemInHand() == PlayerInteractEvent.Result.DENY)) {
            player.sendActionBar(Component.text("You can use this again in ").color(NamedTextColor.RED)
                    .append(Component.text((int)(cooldown / 20.0)).color(NamedTextColor.YELLOW))
                    .append(Component.text(" seconds.").color(NamedTextColor.RED)));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemConsumed(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material consumable = event.getItem().getType();
        if (!consumablesMap.containsKey(consumable)) {
            return;
        }
        for (MaterialCooldown materialCooldown : consumablesMap.get(consumable)) {
            Material material = materialCooldown.getMaterial();
            int cooldown = materialCooldown.getCooldown() * 20;
            player.setCooldown(material, 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.setCooldown(material, cooldown - 1), 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
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
            player.setCooldown(material, 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.setCooldown(material, cooldown - 1), 1);
        }
    }
}
