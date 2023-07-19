package uk.fyrelight.itemcooldowns.objects;

import org.bukkit.Material;

public class MaterialCooldown {
    private final Material material;
    private final int cooldown;

    public MaterialCooldown(Material material, int cooldown) {
        this.material = material;
        this.cooldown = cooldown;
    }

    public Material getMaterial() {
        return material;
    }

    public int getCooldown() {
        return cooldown;
    }
}
