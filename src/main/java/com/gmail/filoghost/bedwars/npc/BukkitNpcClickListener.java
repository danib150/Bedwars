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
        if (!(event.getRightClicked() instanceof Villager villager)) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();

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