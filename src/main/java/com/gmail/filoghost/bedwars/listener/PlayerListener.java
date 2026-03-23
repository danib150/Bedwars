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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.Perms;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.entities.EntityOwnership;
import com.gmail.filoghost.bedwars.listener.BlockModifyManager.Action;
import com.gmail.filoghost.bedwars.utils.Utils;

public class PlayerListener implements Listener {
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onLiquidFlow(BlockFromToEvent event) {
		if (event.getBlock().isLiquid()) {
			BlockModifyManager.onLiquidFlow(event, event.getBlock(), event.getToBlock());
		} else {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBucketFill(PlayerBucketFillEvent event) {
		BlockModifyManager.onModifyAction(event, event.getPlayer(), Action.BREAK, event.getBlockClicked(), null);
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		BlockModifyManager.onModifyAction(event, event.getPlayer(), Action.PLACE, event.getBlockClicked().getRelative(event.getBlockFace()), null);
		if (!event.isCancelled()) {
			Bukkit.getScheduler().runTask(Bedwars.get(), () -> {
				event.getPlayer().getInventory().remove(Material.BUCKET);
			});
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		BlockModifyManager.onModifyAction(event, event.getPlayer(), Action.BREAK, event.getBlock(), null);
		// TODO: consentire anche il break di alcuni blocchi?
	}
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();

		// Considera la sostituzione come rompere il blocco precedente, in aggiunta
		boolean replacingBlock = event.getBlockReplacedState().getType() != Material.AIR;
		if (replacingBlock && allowBlockReplace(event.getBlockReplacedState())) {
			BlockModifyManager.onModifyAction(event, player, Action.BREAK, event.getBlock(), null);
			if (event.isCancelled()) {
				return;
			}
		}
		
		BlockModifyManager.onModifyAction(event, player, Action.PLACE, block, event.getBlockReplacedState());
		
		if (!event.isCancelled() && block.getType() == Material.TNT) {
			event.setCancelled(true);
			TNTPrimed tnt = block.getWorld().spawn(block.getLocation().add(0.5, 0, 0.5), TNTPrimed.class);
			Arena arena = Bedwars.getArenaByPlayer(player);
			EntityOwnership.set(tnt, arena, arena.getPlayerStatus(player).getTeam());

			Utils.consumeOneItemInHand(player);
		}
	}

	
	private boolean allowBlockReplace(BlockState blockReplacedState) {
		switch (blockReplacedState.getBlock().getType()) {
			case SNOW:
			case LONG_GRASS:
				return true;
			default:
				return false;
		}
	}

	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onExplosion(EntityExplodeEvent event) {
		if (event.getEntityType() != EntityType.PRIMED_TNT && event.getEntityType() != EntityType.FIREBALL && event.getEntityType() != EntityType.CREEPER) {
			event.setCancelled(true);
			return;
		}
		
		EntityOwnership entityOwnership = EntityOwnership.get(event.getEntity());
		if (entityOwnership != null) {
			entityOwnership.getArena().getEvents().onExplosion(event, event.blockList());
		} else {
			event.setCancelled(true);
		}
	}
	
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetLivingEntityEvent event) {
		EntityOwnership entityOwnership = EntityOwnership.get(event.getEntity());
		
		if (event.getTarget() instanceof Player && entityOwnership != null) {
			entityOwnership.getArena().getEvents().onMobTarget(event, entityOwnership.getTeam(), (Player) event.getTarget());
		}
	}

	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityAttack(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			// Un player viene attaccato
			EntityOwnership attackerOwnership = EntityOwnership.get(event.getDamager());
			if (attackerOwnership != null) {
				attackerOwnership.getArena().getEvents().onMobTarget(event, attackerOwnership.getTeam(), (Player) event.getEntity());
			}
		} else {
			EntityOwnership defenderOwnership = EntityOwnership.get(event.getEntity());
			if (defenderOwnership != null) {
				defenderOwnership.getArena().getEvents().onMobAttacked(event, defenderOwnership.getTeam());
			}
		}
	}
	
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onHanging(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Hanging) {
			Player playerDamager = Utils.getRealPlayerDamager(event.getDamager());
			if (playerDamager == null || !playerDamager.hasPermission(Perms.BUILD)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onHangingBreak(HangingBreakEvent event) {
		Player breaker = event instanceof HangingBreakByEntityEvent ? Utils.getRealPlayerDamager(((HangingBreakByEntityEvent) event).getRemover()) : null;
		if (breaker == null || !breaker.hasPermission(Perms.BUILD)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onHangingPlace(HangingPlaceEvent event) {
		if (!event.getPlayer().hasPermission(Perms.BUILD)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (!event.getPlayer().hasPermission(Perms.BUILD)) { // TODO mettere un toggle per costruire?
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onCraft(CraftItemEvent event) {
		event.setCancelled(true);
	}

}
