/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.bedwars.listener;

import java.util.Map;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.spawners.ResourceType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wild.api.WildCommons;

public class DeathListener implements Listener {

	private static Map<Player, LastPlayerDamage> lastPlayerDamageEvent = new WeakHashMap<>();
	
	public static void setLastDamager(Player victim, Player damager) {
		lastPlayerDamageEvent.put(victim, new LastPlayerDamage(damager.getName(), System.currentTimeMillis()));
	}
	
	public static void processDeath(Player victim, Arena victimArena, @Nullable EntityDamageEvent lastDamageCause) {
		Player killer = null;
		LastPlayerDamage lastPlayerDamage = lastPlayerDamageEvent.get(victim);
		
		if (lastPlayerDamage != null && System.currentTimeMillis() - lastPlayerDamage.getTimestamp() < 10000) {
			String killerName = lastPlayerDamage.getDamagerName();
			Player possibleKiller = Bukkit.getPlayerExact(killerName);
			
			if (possibleKiller != null && victimArena == Bedwars.getArenaByPlayer(possibleKiller)) {
				killer = possibleKiller;
			}
		}
		
		boolean startRespawnTimer = victimArena.getEvents().onDeath(victim, killer, lastDamageCause);
		
		// Drop oggetti
		for (ItemStack item : victim.getInventory().getContents()) {
			if (item != null && ResourceType.fromMaterial(item.getType()) != null) {
				victim.getWorld().dropItemNaturally(victim.getLocation(), item);
			}
		}
		
		// Hack?
		victim.setHealth(victim.getMaxHealth());
		victim.setVelocity(new Vector(0, 0, 0));
		victimArena.getEvents().onRespawn(victim, startRespawnTimer);
		
		// Dopo, altrimenti vengono puliti gli effetti
		WildCommons.sendTitle(victim, 5, 30, 5, ChatColor.RED + "Sei morto!", "");
		victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 2 * 20, 1, true, false));
		victim.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 2 * 20, 1, true, false));
	}
	
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onDeath(PlayerDeathEvent event) {
		event.setDeathMessage(null);
		event.setDroppedExp(0);
		event.setKeepLevel(true);
		event.getDrops().clear();
		
		Player victim = event.getEntity();
		Arena victimArena = Bedwars.getArenaByPlayer(victim);
		
		if (victimArena != null) {
			processDeath(victim, victimArena, event.getEntity().getLastDamageCause());
		}
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent event) {
		Arena arena = Bedwars.getArenaByPlayer(event.getPlayer());
		
		if (arena != null) {
			arena.getEvents().onRespawn(event, event.getPlayer(), false);
		} else {
			event.setRespawnLocation(Bedwars.getSpawn());
			Bedwars.giveLobbyEquip(event.getPlayer());
		}
	}

	@AllArgsConstructor
	@Getter
	private static class LastPlayerDamage {
		
		private String damagerName;
		private long timestamp;

	}

}
