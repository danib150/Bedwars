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
package com.gmail.filoghost.bedwars.arena.events;

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nullable;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.projectiles.ProjectileSource;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.ArenaStatus;
import com.gmail.filoghost.bedwars.arena.Constants;
import com.gmail.filoghost.bedwars.arena.PlayerStatus;
import com.gmail.filoghost.bedwars.arena.Team;
import com.gmail.filoghost.bedwars.arena.TeamStatus;
import com.gmail.filoghost.bedwars.arena.entities.EntityOwnership;
import com.gmail.filoghost.bedwars.arena.region.PlacedBlocksRegistry;
import com.gmail.filoghost.bedwars.arena.region.ProtectedBlocksRegistry;
import com.gmail.filoghost.bedwars.arena.region.ProtectedBlocksRegistry.ProtectionReason;
import com.gmail.filoghost.bedwars.database.PlayerData;
import com.gmail.filoghost.bedwars.timer.RespawnTimer;
import com.gmail.filoghost.bedwars.utils.Utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wild.api.translation.Translation;

@RequiredArgsConstructor
public class EventManager {
	
	private final Arena arena;
	
	@Getter private final PlacedBlocksRegistry placedBlocksRegistry;
	@Getter private final ProtectedBlocksRegistry protectedBlocksRegistry;

	
	public void onPlace(Cancellable event, Player player, Block block, BlockState replacedBlockState) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
			return;
		}
		
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		if (playerStatus.isSpectator()) {
			event.setCancelled(true);
			return;
		}
		
		ProtectionReason protectionReason = protectedBlocksRegistry.getProtectionReason(block);
		if (protectionReason != null) {
			String toWhat;
			switch (protectionReason) {
				case SPAWN: toWhat = "allo spawn"; break;
				case BOSS: toWhat = "all'altare del boss"; break;
				case GENERATORS: toWhat = "ai generatori"; break;
				default: toWhat = "a ???"; break;
			}
			
			player.sendMessage(ChatColor.RED + "Non puoi costruire vicino " + toWhat + ".");
			event.setCancelled(true);
			return;
		}

		boolean firstTimePlaced = placedBlocksRegistry.setPlayerPlaced(block);
		
		if (replacedBlockState != null && replacedBlockState.getType() != Material.AIR && firstTimePlaced) {
			placedBlocksRegistry.saveBlockState(replacedBlockState);
		}
	}
	
	
	public void onBreak(Cancellable event, Player player, Block block) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
			return;
		}
		
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		if (playerStatus.isSpectator()) {
			event.setCancelled(true);
			return;
		}
		
		if (!placedBlocksRegistry.isPlayerPlaced(block)) {
			event.setCancelled(true);
			
			if (block.getType() == Material.BED_BLOCK) {
				for (TeamStatus teamStatus : arena.getTeamStatuses()) {
					if (teamStatus.isBed(block)) {
						Team bedTeam = teamStatus.getTeam();
						if (playerStatus.getTeam() == bedTeam) {
							player.sendMessage(ChatColor.RED + "Non puoi distruggere il tuo letto.");
						} else {
							onBedDestroy(player, playerStatus.getTeam(), bedTeam);
						}
						return;
					}
				}
			}
			
			player.sendMessage(ChatColor.RED + "Puoi distruggere solo i blocchi posizionati da giocatori.");
			return;
		}
	}
	
	
	public void onFlow(Cancellable event, Block to) {
		if (arena.getRegion().isInside(to) && arena.getArenaStatus() == ArenaStatus.COMBAT) { // TODO potrebbe rendere difficoltosa la modifica permanente delle arene, creare una modalità di "edit" magari?
			if (arena.getEvents().getProtectedBlocksRegistry().getProtectionReason(to) != null) {
				event.setCancelled(true);
				return;
			}
			
			boolean firstTimePlaced = arena.getEvents().getPlacedBlocksRegistry().setPlayerPlaced(to);
			if (firstTimePlaced) {
				arena.getEvents().getPlacedBlocksRegistry().saveBlockState(to);
			}
		} else {
			event.setCancelled(true);
		}
	}
	
	
	public void onBlockInteract(PlayerInteractEvent event, Player player, Block clickedBlock) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
			return;
		}
		
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		if (playerStatus.isSpectator()) {
			event.setCancelled(true);
			return;
		}
		
		if (!arena.getRegion().isInside(clickedBlock)) {
			event.setCancelled(true);
			return;
		}
		
		if (!placedBlocksRegistry.isPlayerPlaced(clickedBlock)) {
			switch(clickedBlock.getType()) {
				case CHEST:
				case ENDER_CHEST:
					// Autorizzato
					break;
				case BED_BLOCK:
					if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
						// Disattiva solo click destro sui letti
						break;
					}
				default:
					// Non si può interagire con gli altri blocchi che non siano stati piazzati dai giocatori
					event.setUseInteractedBlock(Result.DENY);
					event.setUseItemInHand(Result.ALLOW);
					return;
			}
		}
	}
	
	
	public void onExplosion(Cancellable event, Collection<Block> blocks) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
			return;
		}
		
		for (Iterator<Block> iter = blocks.iterator(); iter.hasNext();) {
			Block block = iter.next();
			if (!placedBlocksRegistry.isPlayerPlaced(block) || block.getType() == Material.BED_BLOCK) {
				iter.remove();
			}
		}
	}
	
	
	public void onInventoryOpen(Cancellable event, Player player, Inventory inventory) {
		if (!(inventory.getHolder() instanceof Chest)) {
			return;
		}
		
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
			return;
		}
		
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		if (playerStatus.isSpectator()) {
			event.setCancelled(true);
			return;
		}
		
		TeamStatus teamStatus = arena.getTeamStatus(playerStatus.getTeam());
		Block block = ((Chest) inventory.getHolder()).getBlock();
		
		if (!teamStatus.getTeamChests().contains(block)) {
			player.sendMessage(ChatColor.RED + "Questa cassa non appartiene al tuo team.");
			event.setCancelled(true);
			return;
		}
		
		if (teamStatus.isChestNeedsRefresh(block)) {
			teamStatus.setChestNeedsRefresh(block, false);
			arena.updateSwords(inventory, teamStatus);
		}
	}


	public void onBedDestroy(@Nullable Player who, @Nullable Team whoTeam, Team destroyedTeam) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			Utils.reportAnomaly("on bed destroy arena status was wrong", this, arena.getArenaStatus());
			return;
		}
		
		TeamStatus destroyedTeamStatus = arena.getTeamStatus(destroyedTeam);
		destroyedTeamStatus.destroyBed();
		
		if (arena.getGameloop().getCombatCountdownTimer() != null && arena.allBedsDestroyed()) {
			arena.getGameloop().getCombatCountdownTimer().cancel();
			arena.getScoreboard().endGameCountdown();
		}
		
		// Elimina tutti i giocatori morti che non potranno respawnare
		/*
		List<PlayerStatus> eliminatedDeadPlayers = Lists.newArrayList();
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (playerStatus.getTeam() == destroyedTeam && playerStatus.getPlayer().isDead()) {
				eliminatedDeadPlayers.add(playerStatus);
			}
		}
		for (PlayerStatus playerStatus : eliminatedDeadPlayers) {
			onFinalElimination(playerStatus.getPlayer(), destroyedTeam, playerStatus); // Il team status viene già settato dopo, ma è trascurabile siccome è difficile che ci siano giocatori morti in questo momento
		}
		*/
		
		if (who != null) {
			// Se il letto è stato distrutto da un giocatore e non dal countdown o altro...
			arena.broadcastSound(Constants.SOUND_BED_DESTROY);
			arena.broadcast(whoTeam.getChatColor() + who.getName() + ChatColor.GRAY + " ha distrutto il letto " + destroyedTeam.getChatColor() + destroyedTeam.getNameSingular());
			
			Bedwars.getPlayerData(who).addDestroyedBed(arena);
		}
		
		arena.broadcastTeamTitle(destroyedTeam, ChatColor.RED + "Letto distrutto!", "", 40);
		
		arena.getScoreboard().setTeamStatus(destroyedTeam, true, arena.countPlayersByTeam(destroyedTeam));
		arena.getGameloop().checkWinners();
	}
	
	
	public void onDamage(Cancellable event, Player defender, @Nullable Player attacker) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
			return;
		}
		
		PlayerStatus defenderStatus = arena.getPlayerStatus(defender);
		if (defenderStatus.isSpectator() || defenderStatus.getTeam() == null) {
			event.setCancelled(true);
			return;
		}
		
		if (defenderStatus.hasRespawnInvincibility()) {
			event.setCancelled(true);
			if (attacker != null) {
				attacker.sendMessage(ChatColor.RED + defender.getName() + " ha la protezione del respawn.");
			}
			return;
		}
		
		if (attacker != null) {
			PlayerStatus attackerStatus = arena.getPlayerStatus(attacker);
			
			if (attackerStatus.isSpectator() || attackerStatus.getTeam() == defenderStatus.getTeam()) {
				event.setCancelled(true);
				return;
			}
			
			if (attackerStatus.hasRespawnInvincibility()) {
				attackerStatus.removeRespawnInvincibility();
			}
		}
	}
	
	
	/**
	 *  Quando un giocatore viene eliminato dalla partita, oppure esce
	 */
	public void onFinalElimination(Player victim, Team victimTeam, @Nullable PlayerStatus playerStatus) {
		if (playerStatus != null) {
			playerStatus.setTeam(null);
			playerStatus.setSpectator(victim, true);
			arena.getTeleporterMenu().update();
		}
		
		TeamStatus victimTeamStatus = arena.getTeamStatus(victimTeam);
		int remainingPlayers = arena.countPlayersByTeam(victimTeam);
		
		if (remainingPlayers == 0 && arena.getArenaStatus() == ArenaStatus.COMBAT && !victimTeamStatus.isBedDestroyed()) {
			victimTeamStatus.destroyBed();
		}
		
		arena.getScoreboard().setTeamStatus(victimTeam, victimTeamStatus.isBedDestroyed(), remainingPlayers);
		arena.getScoreboard().removeTeamColor(victim, victimTeam);
		
		if (arena.getSpecManager().allTeamsSpecialized()) {
			arena.getBossManager().endBossForever();
		}
	}

	
	/**
	 * @return true se bisogna dare un timer di respawn
	 */
	public boolean onDeath(Player victim, @Nullable Player killer, @Nullable EntityDamageEvent lastDamageCause) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			// Può capitare se il giocatore esce dal gioco nella lobby
			return false;
		}
		
		PlayerStatus victimStatus = arena.getPlayerStatus(victim);
		if (victimStatus.isSpectator()) {
			// Può capitare se il giocatore esce dal gioco nella lobby
			return false;
		}
		
		Team victimTeam = victimStatus.getTeam();
		
		TeamStatus victimTeamStatus = arena.getTeamStatus(victimTeam);
		boolean finalKill = victimTeamStatus.isBedDestroyed();
		
		if (finalKill) {
			onFinalElimination(victim, victimTeam, victimStatus);
		}
		
		Team killerTeam = killer != null ? arena.getPlayerStatus(killer).getTeam() : null;
		
		if (killerTeam != null) {
			arena.broadcast(killerTeam.getChatColor() + killer.getName() + ChatColor.GRAY + " ha ucciso " + victimTeam.getChatColor() + victim.getName() + (finalKill ? ChatColor.LIGHT_PURPLE + " (Uccisione finale)" : ""));
			
			arena.getScoreboard().addKill(killer);
			
			PlayerData killerStats = Bedwars.getPlayerData(killer);
			killerStats.addKill(arena);
			if (finalKill) {
				killerStats.addFinalKill(arena);
			}
			
		} else {
			String deathMessage = "è morto";
			
			if (lastDamageCause != null) {
				
				if (lastDamageCause instanceof EntityDamageByEntityEvent) {
					Entity damager = ((EntityDamageByEntityEvent) lastDamageCause).getDamager();
					if (damager instanceof Projectile) {
						ProjectileSource projectileSource = ((Projectile) damager).getShooter();
						if (projectileSource instanceof Entity) {
							damager = (Entity) projectileSource;
						}
					}
					EntityOwnership ownership = EntityOwnership.get(damager);
					String ownershipColor = (ownership != null && ownership.getTeam() != null) ? ownership.getTeam().getChatColor().toString() : "";
					
					deathMessage = "è stato ucciso " + ownershipColor + (damager.getType() == EntityType.WITHER ? "dal boss" : ("da " + Translation.of(damager.getType())));
				} else if (lastDamageCause.getCause() == DamageCause.VOID) {
					deathMessage = "è caduto nel vuoto";
				} else if (lastDamageCause.getCause() == DamageCause.FIRE || lastDamageCause.getCause() == DamageCause.FIRE_TICK || lastDamageCause.getCause() == DamageCause.LAVA) {
					deathMessage = "è morto bruciato";
				} else {
					deathMessage = "è morto";
				}
			}
			
			arena.broadcast(victimTeam.getChatColor() + victim.getName() + ChatColor.GRAY + " " + deathMessage + (finalKill ? ChatColor.LIGHT_PURPLE + " (Morte finale)" : ""));
		}
		
		arena.getScoreboard().addDeath(victim);
		Bedwars.getPlayerData(victim).addDeath(arena);
		victimStatus.setSpectator(victim, true);
		arena.getTeleporterMenu().update();

		if (finalKill) {
			arena.getGameloop().checkWinners();
			return false;
		} else {
			return true;
		}
	}


	public void onRespawn(PlayerRespawnEvent event, Player player, boolean startTimer) {
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		TeamStatus teamStatus = playerStatus.getTeam() != null ? arena.getTeamStatus(playerStatus.getTeam()) : null;
		arena.giveEquip(player, playerStatus, teamStatus);
		
		if (arena.getArenaStatus() == ArenaStatus.COMBAT || arena.getArenaStatus() == ArenaStatus.ENDING) {
			if (playerStatus.isSpectator()) {
				event.setRespawnLocation(arena.getSpectatorSpawn());
				if (startTimer) {
					new RespawnTimer(5, arena, player).start();
				}
			} else {
				event.setRespawnLocation(teamStatus.getSpawnPoint());
				playerStatus.startRespawnInvulnerability(5);
			}
		} else {
			event.setRespawnLocation(arena.getLobby());
		}
	}
	
	
	public void onRespawn(Player player, boolean startTimer) {
		PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(player, null, false);
		onRespawn(respawnEvent, player, startTimer);
		player.teleport(respawnEvent.getRespawnLocation());
	}
	
	
	public void onMobTarget(Cancellable event, @Nullable Team mobTeam, Player target) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
			return;
		}
		
		PlayerStatus targetStatus = arena.getPlayerStatus(target);
		if (targetStatus.isSpectator()) {
			event.setCancelled(true);
			return;
		}
		
		if (targetStatus.getTeam() == null || targetStatus.getTeam() == mobTeam) {
			// Allora il mob è amico o neutrale
			event.setCancelled(true);
			return;
		}
	}
	
	
	public void onMobAttacked(EntityDamageByEntityEvent event, Team attackedByTeam) {
		if (event.getEntity().getType() == EntityType.WITHER) {
			if (event.getDamager() instanceof Player) {
				// Ok, processa (il danno è simulato, il boss è invulnerabile)
				Player damager = (Player) event.getDamager();
				if (arena.getPlayerStatus(damager) == null) {
					event.setCancelled(true);
					return;
				}
				
				arena.getBossManager().onBossDamage(event, damager);
				event.setDamage(0.0);
				
			} else if (event.getDamager() instanceof Projectile) {
				// Avvisa dell'immunità il possibile giocatore che ha sparato
				event.setCancelled(true);
				ProjectileSource shooter = ((Projectile) event.getDamager()).getShooter();
				if (shooter instanceof Player) {
					if (arena.getPlayerStatus(((Player) shooter)) == null) {
						return;
					}
					((Player) shooter).sendMessage(ChatColor.RED + "Il boss può essere colpito solo corpo a corpo.");
				}
				
			} else {
				// Il boss è immune ai danni
				event.setCancelled(true);
			}
		} else {
			if (event.getDamager() instanceof Player) {
				PlayerStatus playerStatus = arena.getPlayerStatus((Player) event.getDamager());
				if (playerStatus == null || playerStatus.isSpectator()) {
					event.setCancelled(true);
				}
			}
		}
	}

}
