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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.Perms;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.ArenaStatus;
import com.gmail.filoghost.bedwars.arena.Constants;
import com.gmail.filoghost.bedwars.arena.PlayerStatus;
import com.gmail.filoghost.bedwars.utils.Utils;

import wild.api.WildConstants;

public class InteractListener implements Listener {
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false) // false perché RIGHT_CLICK_AIR è sempre cancellato di default
	public void onPlayerInteract(PlayerInteractEvent event) {
		Action action = event.getAction();
		Player player = event.getPlayer();
		
		if (event.hasBlock() && event.hasItem() && action == Action.RIGHT_CLICK_BLOCK) {
			if (event.getItem().getType() == Material.MONSTER_EGG) {
				Arena arena = Bedwars.getArenaByPlayer(player);
				if (arena != null) {
					SpawnEgg data = (SpawnEgg) event.getItem().getData();
					if (data.getSpawnedType() != null && arena.getSpawningManager().spawnMob(player, data.getSpawnedType(), event.getClickedBlock(), event.getBlockFace())) {
						event.setCancelled(true);
						Utils.consumeOneItemInHand(player); // TODO nella 1.9 questo non va bene...
						return;
					}
				}
			}
		}
		
		if (event.hasBlock() && action == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.WALL_SIGN) {
			Sign sign = (Sign) event.getClickedBlock().getState();
			Arena arena = Bedwars.getArenaByName(ChatColor.stripColor(sign.getLine(0)));
			if (arena != null) {
				event.setCancelled(true);
				arena.tryAddPlayer(player);
				return; // Non gestire le cose successive
			}
		}
			
		if (event.hasItem() && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
			
			if (Constants.ITEM_TEAM_PICKER.isSimilar(event.getItem())) {
				Arena arena = Bedwars.getArenaByPlayer(player);
				if (arena != null) {
					event.setCancelled(true);
					arena.getTeamSelectorMenu().open(player);
					return;
				}
				
			} else if (WildConstants.Spectator.TELEPORTER.isSimilar(event.getItem())) {
				Arena arena = Bedwars.getArenaByPlayer(player);
				if (arena != null) {
					event.setCancelled(true);
					arena.getTeleporterMenu().open(player);
					return;
				}
				
			} else if (WildConstants.Spectator.QUIT_SPECTATING.isSimilar(event.getItem())) {
				Arena arena = Bedwars.getArenaByPlayer(player);
				if (arena != null && arena.getPlayerStatus(player).getTeam() == null) {
					event.setCancelled(true);
					arena.removePlayer(player);
					Bedwars.setupToLobby(player);
					player.sendMessage(ChatColor.GREEN + "Sei andato allo spawn.");
					return;
				}
				
			} else if (event.getItem().getType() == Material.FIREBALL) {
				Arena arena = Bedwars.getArenaByPlayer(player);
				if (arena != null) {
					if (arena.getSpawningManager().throwFireball(player)) {
						event.setCancelled(true);
						Utils.consumeOneItemInHand(player); // TODO nella 1.9 questo non va bene...
						return;
					}
				}
			}
		}
		
		if (event.hasBlock()) {
			Arena arena = Bedwars.getArenaByPlayer(player);
			if (arena != null) {
				arena.getEvents().onBlockInteract(event, player, event.getClickedBlock());
			} else {
				if (!player.hasPermission(Perms.BUILD)) {
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.ALLOW);
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryOpen(InventoryOpenEvent event) {
		Player player = (Player) event.getPlayer();
		Arena arena = Bedwars.getArenaByPlayer(player);
		if (arena != null) {
			arena.getEvents().onInventoryOpen(event, player, event.getInventory());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		
		// L'armatura non si può levare
		if (event.getSlotType() == SlotType.ARMOR) {
			if (Bedwars.getArenaByPlayer(player) != null) {
				event.setCancelled(true);
				return;
			}
		}
		
		// Non si può impedire di mettere spade di legno nelle casse perché è poco affidabile l'evento
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemPickup(PlayerPickupItemEvent event) {
		Arena arena = Bedwars.getArenaByPlayer(event.getPlayer());
		if (arena != null) {
			PlayerStatus playerStatus = arena.getPlayerStatus(event.getPlayer());
			if (playerStatus.isSpectator() || playerStatus.getTeam() == null) {
				event.setCancelled(true);
				return;
			}
			
			ItemStack itemStack = event.getItem().getItemStack();
			if (Utils.isSword(itemStack.getType())) {
				arena.updateSword(itemStack, arena.getTeamStatus(playerStatus.getTeam()));
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent event) {
		Arena arena = Bedwars.getArenaByPlayer(event.getPlayer());
		if (arena == null || arena.getArenaStatus() == ArenaStatus.LOBBY) {
			event.setCancelled(true);
			return;
		}
		
		PlayerStatus playerStatus = arena.getPlayerStatus(event.getPlayer());
		if (playerStatus.isSpectator() || playerStatus.getTeam() == null) {
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemCreative(InventoryCreativeEvent event) {
		Player player = (Player) event.getWhoClicked();
		Arena arena = Bedwars.getArenaByPlayer(player);
		if (arena != null) {
			PlayerStatus playerStatus = arena.getPlayerStatus(player);
			if (playerStatus.isSpectator() || playerStatus.getTeam() == null) {
				event.setCancelled(true);
				return;
			}
		}
	}

	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onItemConsume(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() == Material.POTION) {
			Bukkit.getScheduler().runTask(Bedwars.get(), () -> {
				event.getPlayer().getInventory().remove(Material.GLASS_BOTTLE);
			});
		}
	}


}
