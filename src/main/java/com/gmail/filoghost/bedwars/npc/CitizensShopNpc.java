package com.gmail.filoghost.bedwars.npc;

import java.util.function.Consumer;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

public class CitizensShopNpc {

    @Getter
    private final NPC npc;
    private final Consumer<Player> clickHandler;

    public CitizensShopNpc(Location location, String name, Consumer<Player> clickHandler) {
        this.clickHandler = clickHandler;

        this.npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, name);
        this.npc.spawn(location);

        if (this.npc.getEntity() instanceof Villager villager) {
            villager.setProfession(Villager.Profession.PRIEST);
            villager.setCanPickupItems(false);
            villager.setCustomNameVisible(true);
        }
    }

    public void handleClick(Player player) {
        clickHandler.accept(player);
    }

    public void destroy() {
        if (npc != null) {
            npc.destroy();
        }
    }
}