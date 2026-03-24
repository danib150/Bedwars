package com.gmail.filoghost.bedwars.npc;

import java.util.function.Consumer;

import com.gmail.filoghost.bedwars.Bedwars;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class BukkitShopNpc {

    @Getter
    private final Villager villager;
    @Setter private Consumer<Player> clickHandler;
    private Hologram hologram;

    public BukkitShopNpc(Location location, Villager.Profession profession) {
        villager = setupVillager(location, profession);
        clickHandler = player -> Bedwars.get().getLogger().warning("NPC without click handler configured.");
    }

    public void setupHologram(@NonNull String... lines) {
        if (hologram != null) {
            hologram.delete();
        }

        Location holoLoc = villager.getLocation().clone().add(0.0, 2.6, 0.0);
        Hologram hologram = Bedwars.getHolographicDisplaysAPI().createHologram(holoLoc);
        for (String line : lines) {
            hologram.getLines().appendText(line);
        }
        this.hologram = hologram;
    }

    private Villager setupVillager(Location location, Villager.Profession profession) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Location/world null");
        }

        Location loc = location.clone();
        loc.getChunk().load();

        Villager villager = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER);

        villager.setProfession(profession);
        villager.setAdult();
        villager.setRemoveWhenFarAway(false);

        applyNpcFlags(villager);

        return villager;
    }

    private void applyNpcFlags(Villager villager) {
        net.minecraft.server.v1_8_R3.EntityVillager nmsVillager = ((CraftVillager) villager).getHandle();

        NBTTagCompound tag = new NBTTagCompound();
        nmsVillager.c(tag);

        tag.setInt("NoAI", 1);
        tag.setInt("Silent", 1);
        tag.setInt("Invulnerable", 1);

        nmsVillager.f(tag);
    }
    public void destroy() {
        if (hologram != null) {
            hologram.delete();
            hologram = null;
        }
        if (villager != null && villager.isValid()) {
            villager.remove();
        }
    }

    public void handleClick(Player player) {
        clickHandler.accept(player);
    }
    
}