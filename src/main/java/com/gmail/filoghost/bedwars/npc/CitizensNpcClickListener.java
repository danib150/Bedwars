package com.gmail.filoghost.bedwars.npc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.citizensnpcs.api.event.NPCRightClickEvent;

public class CitizensNpcClickListener implements Listener {

    private final Map<Integer, CitizensShopNpc> registered = new ConcurrentHashMap<>();

    public void register(CitizensShopNpc shopNpc) {
        registered.put(shopNpc.getNpc().getId(), shopNpc);
    }

    public void unregister(CitizensShopNpc shopNpc) {
        registered.remove(shopNpc.getNpc().getId());
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        CitizensShopNpc shopNpc = registered.get(event.getNPC().getId());
        if (shopNpc != null) {
            shopNpc.handleClick(event.getClicker());
        }
    }
}