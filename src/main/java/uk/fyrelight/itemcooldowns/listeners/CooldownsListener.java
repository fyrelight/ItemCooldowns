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

import java.util.*;

public class CooldownsListener implements Listener {
    private final ItemCooldownsPlugin plugin;
    private final Map<Material, Integer> consumableCooldowns = new HashMap<>();
    private final Map<Material, List<Material>> consumableMaterials = new HashMap<>();
    private final Map<EntityType, Integer> projectileCooldowns = new HashMap<>();
    private final Map<EntityType, List<Material>> projectileMaterials = new HashMap<>();

    public void reloadListener() {
        consumableCooldowns.clear();
        consumableMaterials.clear();
        projectileCooldowns.clear();
        projectileMaterials.clear();
        registerConsumables();
        registerProjectiles();
    }

    public void registerConsumables() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("consumables");
        if (section == null) {
            return;
        }
        for (String consumableName : section.getKeys(false)) {
            int seconds = section.getInt(consumableName);
            if (seconds <= 0) {
                plugin.getLogger().warning("Invalid cooldown for consumable: " + consumableName);
                continue;
            }
            Material consumable = Material.getMaterial(consumableName.toUpperCase(Locale.ROOT));
            if (consumable == null) {
                plugin.getLogger().warning("Invalid material for consumable: " + consumableName);
                continue;
            }
            List<String> matNames = section.getStringList(consumableName+".materials");
            List<Material> materials = new ArrayList<>();
            for (String matName : matNames) {
                Material material = Material.getMaterial(matName.toUpperCase(Locale.ROOT));
                if (material == null) {
                    plugin.getLogger().warning("Invalid material " + matName + " for consumable: " + consumableName);
                    continue;
                }
                materials.add(material);
            }
            consumableMaterials.put(consumable, materials);
            consumableCooldowns.put(consumable, seconds);
        }
    }

    public void registerProjectiles() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("projectiles");
        if (section == null) {
            return;
        }
        for (String entityName : section.getKeys(false)) {
            int seconds = section.getInt(entityName+".cooldown");
            if (seconds <= 0) {
                plugin.getLogger().warning("Invalid cooldown for projectile: " + entityName);
                continue;
            }
            EntityType entityType = EntityType.valueOf(entityName.toUpperCase(Locale.ROOT));
            List<String> matNames = section.getStringList(entityName+".materials");
            List<Material> materials = new ArrayList<>();
            for (String matName : matNames) {
                Material material = Material.getMaterial(matName.toUpperCase(Locale.ROOT));
                if (material == null) {
                    plugin.getLogger().warning("Invalid material " + matName + " for projectile: " + entityName);
                    continue;
                }
                materials.add(material);
            }
            projectileMaterials.put(entityType, materials);
            projectileCooldowns.put(entityType, seconds);
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
        if (!consumableCooldowns.containsKey(consumable)) {
            return;
        }
        int cooldown = consumableCooldowns.get(consumable) * 20;
        for (Material material : consumableMaterials.get(consumable)) {
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
        if (!projectileMaterials.containsKey(projectile.getType())) {
            return;
        }
        int cooldown = projectileCooldowns.get(projectile.getType()) * 20;
        for (Material material : projectileMaterials.get(projectile.getType())) {
            player.setCooldown(material, 1);
            Bukkit.getScheduler().runTaskLater(plugin, () -> player.setCooldown(material, cooldown - 1), 1);
        }
    }
}
