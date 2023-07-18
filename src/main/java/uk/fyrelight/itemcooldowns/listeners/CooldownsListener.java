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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import uk.fyrelight.itemcooldowns.ItemCooldownsPlugin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CooldownsListener implements Listener {
    private final ItemCooldownsPlugin plugin;
    private final Map<Material, Integer> consumableCooldowns = new HashMap<>();
    private final Map<Material, Integer> projectileCooldowns = new HashMap<>();
    private final Map<EntityType, Material> projectileMaterials = new HashMap<>();

    public void registerConsumables() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("consumables");
        if (section == null) {
            return;
        }
        for (String matName : section.getKeys(false)) {
            int seconds = section.getInt(matName);
            if (seconds <= 0) {
                plugin.getLogger().warning("Invalid cooldown for consumable: " + matName);
                continue;
            }
            Material material = Material.getMaterial(matName.toUpperCase(Locale.ROOT));
            if (material == null) {
                plugin.getLogger().warning("Invalid material for consumable: " + matName);
                continue;
            }
            consumableCooldowns.put(material, seconds);
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
            String matName = section.getString(entityName+".material");
            if (matName == null) {
                plugin.getLogger().warning("Invalid material for projectile: " + entityName);
                continue;
            }
            Material material = Material.getMaterial(matName.toUpperCase(Locale.ROOT));
            if (material == null) {
                plugin.getLogger().warning("Invalid material for projectile: " + entityName);
                continue;
            }
            projectileMaterials.put(entityType, material);
            projectileCooldowns.put(material, seconds);
        }
    }

    public CooldownsListener(ItemCooldownsPlugin plugin) {
        this.plugin = plugin;
        registerConsumables();
        registerProjectiles();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemConsumed(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material material = event.getItem().getType();
        if (!consumableCooldowns.containsKey(material)) {
            return;
        }
        int cooldown = consumableCooldowns.get(material);
        player.setCooldown(material, cooldown * 20);
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
        Material material = projectileMaterials.get(projectile.getType());
        int cooldown = projectileCooldowns.get(material);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.setCooldown(material, cooldown * 20);
        }, 1);
    }
}
