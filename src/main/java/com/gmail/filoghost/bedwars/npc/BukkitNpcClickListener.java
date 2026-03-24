package com.gmail.filoghost.bedwars.npc;

import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.arena.Arena;

public class BukkitNpcClickListener implements Listener {

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager)) {
            return;
        }

        Villager villager = (Villager) event.getRightClicked();

        // controlla che sia un NPC nostro
        if (!villager.hasMetadata("bw_shop_npc")) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();

        // cerca a quale NPC corrisponde
        for (Arena arena : Bedwars.getAllArenas()) {
            for (BukkitShopNpc shopNpc : arena.getVillagers()) {

                if (shopNpc.getVillager().getUniqueId().equals(villager.getUniqueId())) {
                    shopNpc.handleClick(player);
                    return;
                }
            }
        }
    }
}