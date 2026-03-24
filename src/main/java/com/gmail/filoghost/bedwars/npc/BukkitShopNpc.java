package com.gmail.filoghost.bedwars.npc;

import java.util.function.Consumer;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class BukkitShopNpc {

    @Getter
    private final Villager villager;
    private final Consumer<Player> clickHandler;

    public BukkitShopNpc(Location location, Villager.Profession profession, String displayName, Consumer<Player> clickHandler) {
        this.clickHandler = clickHandler;
        villager = setupVillager(location, profession);
        villager.setCustomName(displayName);
    }

    private Villager setupVillager(Location location, Villager.Profession profession) {
        Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
        villager.setProfession(profession);
        villager.setAdult();
        Entity craftEntity = ((CraftEntity) villager).getHandle();
        setNoAI(villager);
        craftEntity.b(true);
        System.out.println("Spawnato un villager in " + location.getWorld().getName());
        return villager;
    }

    private void setNoAI(Villager villager) {
        net.minecraft.server.v1_8_R3.EntityVillager nmsVillager = ((CraftVillager) villager).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        nmsVillager.c(tag);
        tag.setInt("NoAI", 1);
        nmsVillager.f(tag);
    }

    public void handleClick(Player player) {
        clickHandler.accept(player);
    }
    
}