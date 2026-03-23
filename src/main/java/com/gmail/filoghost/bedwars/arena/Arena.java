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
package com.gmail.filoghost.bedwars.arena;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.VanishManager;
import com.gmail.filoghost.bedwars.arena.entities.SpawningManager;
import com.gmail.filoghost.bedwars.arena.events.EventManager;
import com.gmail.filoghost.bedwars.arena.gameloop.GameloopManager;
import com.gmail.filoghost.bedwars.arena.menu.TeamSelectorMenu;
import com.gmail.filoghost.bedwars.arena.region.PlacedBlocksRegistry;
import com.gmail.filoghost.bedwars.arena.region.ProtectedBlocksRegistry;
import com.gmail.filoghost.bedwars.arena.region.ProtectedBlocksRegistry.ProtectionReason;
import com.gmail.filoghost.bedwars.arena.region.Region;
import com.gmail.filoghost.bedwars.arena.scoreboard.ScoreboardManager;
import com.gmail.filoghost.bedwars.arena.shop.ShopManager;
import com.gmail.filoghost.bedwars.arena.shop.Trap;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.PlayerUpgrade;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.TeamUpgrade;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.Upgrade;
import com.gmail.filoghost.bedwars.arena.spawners.Spawner;
import com.gmail.filoghost.bedwars.arena.specialization.BossManager;
import com.gmail.filoghost.bedwars.arena.specialization.SpecializationManager;
import com.gmail.filoghost.bedwars.arena.specialization.SpecializationPoll;
import com.gmail.filoghost.bedwars.command.CommandValidateExtra;
import com.gmail.filoghost.bedwars.hud.shop.TrapBuyableIcon;
import com.gmail.filoghost.bedwars.hud.spectator.TeleporterMenu;
import com.gmail.filoghost.bedwars.listener.ChunkUnloadListener;
import com.gmail.filoghost.bedwars.settings.objects.ArenaConfig;
import com.gmail.filoghost.bedwars.settings.objects.SpawnerConfig;
import com.gmail.filoghost.bedwars.settings.objects.TeamConfig;
import com.gmail.filoghost.bedwars.utils.Utils;
import com.gmail.filoghost.holographicmobs.api.HolographicMobsAPI;
import com.gmail.filoghost.holographicmobs.object.types.HologramVillager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import wild.api.WildCommons;
import wild.api.WildConstants;
import wild.api.item.ItemBuilder;
import wild.api.menu.Icon;
import wild.api.sound.EasySound;
import wild.api.util.UnitFormatter;
import wild.api.world.Particle;

public class Arena {
	
	// Settings
	@Getter private final String name;
	@Getter private final Region region;
	@Getter private final Location lobby;
	@Getter private final Location spectatorSpawn;
	private final Block sign;
	private final int minPlayers;
	private final int maxPlayers;
	@Getter private final int maxPlayersPerTeam;

	// Statuses
	@Getter @Setter private ArenaStatus arenaStatus;
	private final Map<Player, PlayerStatus> playerStatuses;
	private final Map<Team, TeamStatus> teamStatuses;
	
	// Dynamic objects
	@Getter private final List<HologramVillager> villagers;
	@Getter private final List<Spawner> globalSpawners;
	@Getter private TeamSelectorMenu teamSelectorMenu;
	@Getter private TeleporterMenu teleporterMenu;
	
	// Managers
	@Getter private final ScoreboardManager scoreboard;
	@Getter private final GameloopManager gameloop;
	@Getter private final EventManager events;
	@Getter private final BossManager bossManager;
	@Getter private final SpecializationManager specManager;
	@Getter private final SpawningManager spawningManager;
	@Getter private final ShopManager shopManager;
	
	public Arena(ArenaConfig config) throws Exception {
		CommandValidateExtra.checkArenaConfig(config);
		
		this.name = config.name;
		this.region = new Region(config);
		this.lobby = config.bossLocation.getLocation();
		this.spectatorSpawn = config.spectatorSpawn.getLocation();
		this.sign = config.sign.getBlock();
		this.maxPlayersPerTeam = config.maxPlayersPerTeam;
		this.maxPlayers = maxPlayersPerTeam * config.teamConfigs.size();
		this.minPlayers = maxPlayers;
		
		this.scoreboard = new ScoreboardManager();
		this.gameloop = new GameloopManager(this);
		this.events = new EventManager(this, new PlacedBlocksRegistry(region), new ProtectedBlocksRegistry(region));
		this.specManager = new SpecializationManager(this);
		this.spawningManager = new SpawningManager(this);
		this.shopManager = new ShopManager(this);
		this.bossManager = new BossManager(this, config.bossLocation.getLocation());

		this.villagers = Lists.newArrayList();
		
		this.playerStatuses = Maps.newConcurrentMap();
		this.teamStatuses = new EnumMap<>(Team.class);
		for (Entry<String, TeamConfig> entry : config.teamConfigs.entrySet()) {
			Team team = Team.valueOf(entry.getKey().toUpperCase());
			TeamConfig teamConfig = entry.getValue();
			TeamStatus status = new TeamStatus(this, team, teamConfig.spawnLocation.getLocation(), teamConfig.bedHeadLocation.getBlock(), teamConfig.bedHeadLocation.getBlock().getRelative(teamConfig.bedFeetDirection));
			this.teamStatuses.put(team, status);
			
			HologramVillager teamVillager = HolographicMobsAPI.spawnVillager(teamConfig.teamVillagerLocation.getLocation());
			teamVillager.setProfession(Profession.PRIEST);
			teamVillager.setCustomName(Lists.newArrayList(ChatColor.BOLD + "POTENZIAMENTI", ChatColor.GRAY + "(Click sinistro)"));
			teamVillager.setClickHandler(clicker -> shopManager.tryOpenShop(clicker, TeamStatus::getTeamShop));
			teamVillager.update();
			villagers.add(teamVillager);
			
			HologramVillager itemVillager = HolographicMobsAPI.spawnVillager(teamConfig.itemVillagerLocation.getLocation());
			itemVillager.setCustomName(Lists.newArrayList(ChatColor.BOLD + "OGGETTI", ChatColor.GRAY + "(Click sinistro)"));
			itemVillager.setClickHandler(clicker -> shopManager.tryOpenShop(clicker, TeamStatus::getIndividualShop));
			itemVillager.update();
			villagers.add(itemVillager);
		}
		
		this.globalSpawners = Lists.newArrayList();
		for (SpawnerConfig spawnerConfig : config.spawners) {
			Spawner spawner = new Spawner(spawnerConfig.type, spawnerConfig.block.getBlock(), maxPlayersPerTeam);
			
			if (spawner.getResource().isPublic()) {
				// Imposta come globale
				this.globalSpawners.add(spawner);
			} else {
				// Aggiungi al team più vicino
				getNearestTeam(spawner.getBlock()).getTeamSpawners().add(spawner);
			}
		}
		
		
		events.getProtectedBlocksRegistry().setRangeProtectionReason(bossManager.getBossLocation().getBlock(), config.bossProtectionRadius, ProtectionReason.BOSS);
		for (TeamStatus teamStatus : teamStatuses.values()) {
			events.getProtectedBlocksRegistry().setRangeProtectionReason(teamStatus.getSpawnPoint().getBlock(), config.spawnProtectionRadius, ProtectionReason.SPAWN);
		}
		for (HologramVillager villager : villagers) {
			events.getProtectedBlocksRegistry().setRangeProtectionReason(villager.getLocation().getBlock(), config.villagerProtectionRadius, ProtectionReason.SPAWN);
		}
		for (Spawner globalSpawner : globalSpawners) {
			events.getProtectedBlocksRegistry().setRangeProtectionReason(globalSpawner.getBlock(), config.generatorsProtectionRadius, ProtectionReason.GENERATORS);
		}
		
		reset();
	}
	
	
	public void reset() {
		arenaStatus = ArenaStatus.LOBBY;
		
		for (Player player : getPlayers()) {
			VanishManager.setHidden(player, false);
			Bedwars.getArenasByPlayers().remove(player);
			Bedwars.setupToLobby(player);
			player.sendMessage(ChatColor.GRAY + "Sei stato mandato allo spawn.");
		}
		
		playerStatuses.clear();
		
		// Reset blocchi
		events.getPlacedBlocksRegistry().restore();
		
		// Reset casse memorizzate
		for (TeamStatus teamStatus : teamStatuses.values()) {
			teamStatus.getTeamChests().clear();
		}
	
		region.iterateChunks(chunk -> {
			// Aggiornamento casse e reset contenuti
			for (BlockState tileEntity : chunk.getTileEntities()) {
				if (tileEntity instanceof Chest && region.isInside(tileEntity.getBlock())) {
					Chest chest = (Chest) tileEntity;
					chest.getInventory().clear();
					getNearestTeam(chest.getBlock()).getTeamChests().add(chest.getBlock());
				}
			}
			
			// Pulizia oggetti e entità
			for (Entity entity : chunk.getEntities()) {
				if (entity.getType() == EntityType.DROPPED_ITEM || (entity instanceof LivingEntity && entity.getType() != EntityType.PLAYER)) {
					if (region.isInside(entity.getLocation())) {
						entity.remove();
					}
				}
			}
		});

		// Reset status team
		for (TeamStatus teamStatus : teamStatuses.values()) {
			teamStatus.reset();
		}
		
		// Reset spawner
		for (Spawner spawner : globalSpawners) {
			spawner.reset();
		}
		
		// Consenti lo scaricamento dei chunk
		ChunkUnloadListener.allowUnload(this);
		
		// Menu e scoreboard
		teamSelectorMenu = new TeamSelectorMenu(this);
		teleporterMenu = new TeleporterMenu(this);
		scoreboard.reset();
		scoreboard.displayLobby();
		
		// Boss
		bossManager.endBossForever();
		bossManager.reset();
		
		refreshSign();
	}

	
	public void refreshSign() {
		BlockState state = sign.getState();
		if (state instanceof Sign) {
			Sign sign = (Sign) state;
			sign.setLine(0, ChatColor.BOLD + name);
			sign.setLine(1, "----------------");
			if (gameloop.getLobbyCountdownTimer() != null) {
				sign.setLine(2, ChatColor.GREEN + "[" + UnitFormatter.formatMinutesOrSeconds(gameloop.getLobbyCountdownTimer().getCountdown()) + "]");
			} else {
				sign.setLine(2, (arenaStatus == ArenaStatus.LOBBY ? ChatColor.GREEN : ChatColor.RED) + arenaStatus.getName());
			}
			sign.setLine(3, ChatColor.DARK_GRAY + "[" + playerStatuses.size() + "/" + maxPlayers + "]");
			sign.update(true, false); // Force per ripristinare cartelli distrutti, e senza farli cadere
		}
	}
	
	
	public void tryAddPlayer(Player player) {
		if (Bedwars.getArenaByPlayer(player) != null) {
			Utils.reportAnomaly("player was trying to join while already inside arena", this, player);
			player.sendMessage(ChatColor.RED + "Sei già in un'arena.");
			return;
		}
		
		if (arenaStatus == ArenaStatus.LOBBY && playerStatuses.size() >= maxPlayers) {
			player.sendMessage(ChatColor.RED + "Questa arena è piena.");
			return;
		}
		
		PlayerStatus playerStatus = new PlayerStatus(player);
		if (arenaStatus != ArenaStatus.LOBBY) {
			// Eccetto che nella lobby, si entra sempre come spettatori
			playerStatus.setSpectator(player, true);
		} else {
			playerStatus.setSpectator(player, false); // Per aggiornare i cosmetici
		}

		// Salva lo stato del giocatore
		playerStatuses.put(player, playerStatus);
		Bedwars.getArenasByPlayers().put(player, this);
		
		if (arenaStatus == ArenaStatus.LOBBY) {
			if (gameloop.getLobbyCountdownTimer() == null && playerStatuses.size() >= minPlayers) {
				gameloop.startLobbyCountdown();
			}
			broadcast(ChatColor.GRAY + player.getName() + " è entrato (" + ChatColor.WHITE + playerStatuses.size() + ChatColor.GRAY + "/" + ChatColor.WHITE + maxPlayers + ChatColor.GRAY + ")");
		}
		
		refreshSign();
		player.teleport(lobby);
		giveEquip(player, playerStatus, null);
		player.setScoreboard(scoreboard.getScoreboard());
	}
	

	public void removePlayer(Player player) {
		// Rimuove il player dalle collezioni
		if (!playerStatuses.containsKey(player)) {
			Utils.reportAnomaly("removing player but wasn't in arena", this, player);
			return;
		}
		
		PlayerStatus leftStatus = playerStatuses.remove(player);
		Team leftTeam = leftStatus.getTeam();
		if (leftTeam != null) {
			SpecializationPoll activeSpecPoll = getTeamStatus(leftTeam).getActiveSpecializationPoll();
			if (activeSpecPoll != null) {
				activeSpecPoll.removeVote(leftStatus);
			}
		}
		
		scoreboard.untrackPlayer(player);
		Bedwars.getArenasByPlayers().remove(player);
		
		if (arenaStatus == ArenaStatus.LOBBY) {
			broadcast(ChatColor.GRAY + player.getName() + " è uscito (" + ChatColor.WHITE + playerStatuses.size() + ChatColor.GRAY + "/" + ChatColor.WHITE + maxPlayers + ChatColor.GRAY + ")");
			
			if (gameloop.getLobbyCountdownTimer() != null && playerStatuses.size() < minPlayers) {
				gameloop.cancelLobbyCountdown();
				broadcast(ChatColor.YELLOW + "Il conto alla rovescia è stato interrotto.");
			}
			
		} else if (arenaStatus == ArenaStatus.COMBAT) {
			if (leftTeam != null) {
				broadcast(leftTeam.getChatColor() + player.getName() + ChatColor.GRAY + " è uscito");
				events.onFinalElimination(player, leftTeam, null);
			}
			gameloop.checkWinners();
		}
		
		if (leftTeam != null) {
			teamSelectorMenu.updateCount(leftTeam, countPlayersByTeam(leftTeam), maxPlayersPerTeam);
			teleporterMenu.update();
		}
		
		refreshSign();
		
		// Rimuove l'eventuale vanish
		leftStatus.setSpectator(player, false);
	}
	
	
	public void trySetPlayerTeam(Player player, @NonNull Team team) {
		if (arenaStatus != ArenaStatus.LOBBY) {
			Utils.reportAnomaly("trying to set team not in lobby", this, player, arenaStatus);
			player.sendMessage(ChatColor.RED + "Non puoi cambiare team ora.");
			return;
		}
		
		PlayerStatus playerStatus = getPlayerStatus(player);
		
		if (playerStatus == null) {
			Utils.reportAnomaly("trying to set team on external player", this, player);
			player.sendMessage(ChatColor.RED + "Non sei all'interno della partita.");
			return;
		}
		
		if (!teamStatuses.containsKey(team)) {
			Utils.reportAnomaly("trying to set team that is not present in arena", this, player);
			player.sendMessage(ChatColor.RED + "Non sei all'interno della partita.");
			return;
		}
		
		if (playerStatus.getTeam() == team) {
			player.sendMessage(ChatColor.RED + "Sei già nel team " + team.getNameSingular() + ".");
			return;
		}
		
		
		if (countPlayersByTeam(team) >= maxPlayersPerTeam) {
			player.sendMessage(ChatColor.RED + "Il team " + team.getNameSingular() + " è al completo.");
			return;
		}
		
		Team previousTeam = playerStatus.getTeam();
		playerStatus.setTeam(team);
		
		if (previousTeam != null) {
			teamSelectorMenu.updateCount(previousTeam, countPlayersByTeam(previousTeam), maxPlayersPerTeam);
		}
		teamSelectorMenu.updateCount(team, countPlayersByTeam(team), maxPlayersPerTeam);
		teleporterMenu.update();
		player.sendMessage(Bedwars.PREFIX + ChatColor.GRAY + "Hai scelto il team " + team.getChatColor() + team.getNameSingular() + ChatColor.GRAY + ".");
	}
	
	
	public void onTick(int ticks) {
		if (arenaStatus != ArenaStatus.COMBAT) {
			return;
		}
		
		// Controllo regen e resistenza ogni mezzo secondo
		boolean checkSpawnEffects = ticks % 10 == 0;
		
		// Controllot trappole ogni mezzo secondo ma alternato rispetto a regen
		boolean checkTraps = ticks % 10 == 5;
		
		// Mostra le particelle spesso
		boolean showInvincibilityParticles = ticks % 5 == 0;
		
		// Tick al boss ogni secondo
		boolean tickBoss = ticks % 20 == 0;
		
		for (Spawner spawner : globalSpawners) {
			spawner.onTick();
		}
		
		for (TeamStatus teamStatus : teamStatuses.values()) {
			SpecializationPoll activeSpecPoll = teamStatus.getActiveSpecializationPoll();
			
			if (activeSpecPoll != null) {
				int ticksRemaining = activeSpecPoll.getTicksRemaining();
				
				if (ticksRemaining <= 0) {
					specManager.endPoll(teamStatus);
				} else {
					if (ticksRemaining == 10 * 20 || ticksRemaining == 30 * 20) {
						broadcastTeam(teamStatus.getTeam(), "Mancano " + (ticksRemaining / 20) + " secondi alla fine del sondaggio per la specializzazione!");
						specManager.promptPollOptions(teamStatus, activeSpecPoll);
					}
				}
				
				activeSpecPoll.onTick();
			}
			
			for (Spawner spawner : teamStatus.getTeamSpawners()) {
				spawner.onTick();
			}
			
			if (checkSpawnEffects) {
				int regenLevel = teamStatus.getUpgradeLevel(TeamUpgrade.SPAWN_REGEN);
				int resistanceLevel = teamStatus.getUpgradeLevel(TeamUpgrade.SPAWN_RESISTANCE);
				
				if (regenLevel > 0 || resistanceLevel > 0) {
					for (PlayerStatus playerStatus : playerStatuses.values()) {
						if (playerStatus.getTeam() == teamStatus.getTeam()) {
							// Applica regen a quelli entro 10 blocchi dal proprio letto
							Player player = playerStatus.getPlayer();
							
							if (player.getLocation().distanceSquared(teamStatus.getBedCenter()) <= 225.0) {
								if (regenLevel > 0) {
									if (Utils.getActivePotionLevel(player, PotionEffectType.REGENERATION) <= 0) {
										player.addPotionEffect(Constants.EFFECT_REGEN, true);
									}
								}
								if (resistanceLevel > 0) {
									playerStatus.getPlayer().addPotionEffect(Constants.EFFECT_RESISTANCE, true);
								}
							}
						}
					}
				}
			}
			
			if (checkTraps) {
				Trap activeTrap = teamStatus.getNextActiveTrap();
				// Continua a controllare finché ci sono trappole attive
				
				if (activeTrap != null) {
					// Trova un nemico in un certo raggio dal letto
					PlayerStatus intruderStatus = getNearIntruder(teamStatus, true, ticks);
					
					if (intruderStatus != null) {
						Player intruder = intruderStatus.getPlayer();
						
						// Applica la trappola solo a quel nemico, la disattiva e ignora gli altri
						teamStatus.setTrapActive(activeTrap, false);
						activeTrap.getTrapReaction().onTrigger(intruder);
						WildCommons.sendTitle(intruder, 5, 40, 5, ChatColor.RED + activeTrap.getName(), ChatColor.DARK_RED + "Trappola attivata!");
						EasySound.quickPlay(intruder, Sound.CHEST_OPEN, 0.8f);
						intruderStatus.startTrapCooldown(ticks, activeTrap);
						
						// Notifica il team
						broadcastTeam(teamStatus.getTeam(), intruderStatus.getTeam().getChatColor() + intruder.getName() + ChatColor.GRAY + " ha attivato una trappola!");
						broadcastTeamSound(teamStatus.getTeam(), Sound.NOTE_PLING);
						
						// Aggiorna l'icona nello shop
						for (int slot = 0; slot < teamStatus.getTeamShop().getSize(); slot++) {
							Icon icon = teamStatus.getTeamShop().getIconAt(slot);
							if (icon instanceof TrapBuyableIcon) {
								TrapBuyableIcon trapIcon = (TrapBuyableIcon) icon;
								if (trapIcon.getTrap() == activeTrap) {
									trapIcon.updateIcon();
									teamStatus.getTeamShop().refresh(trapIcon);
								}
							}
						}
					}
				}
			}
		}
		
		if (showInvincibilityParticles) {
			for (PlayerStatus playerStatus : playerStatuses.values()) {
				if (!playerStatus.isSpectator() && playerStatus.getTeam() != null && playerStatus.hasRespawnInvincibility()) {
					// Mostra effetto
					Location loc = playerStatus.getPlayer().getLocation();
					Particle.SPELL.display(loc, 0.3F, 1.0F, 0.3F, 0.0F, 20);
					Particle.SMOKE.display(loc, 0.3F, 1.2F, 0.3F, 0.0F, 60);
					Particle.HAPPY_VILLAGER.display(loc, 0.3F, 1.2F, 0.3F, 0.0F, 4);
				}
			}
		}
		
		if (tickBoss) {
			bossManager.onBossCountdown();
		}
	}
	
	private PlayerStatus getNearIntruder(TeamStatus allyTeamStatus, boolean skipPlayersWithTrapCooldown, int ticks) {
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (!playerStatus.isSpectator() &&
				playerStatus.getTeam() != allyTeamStatus.getTeam() &&
				(skipPlayersWithTrapCooldown || !playerStatus.hasTrapCooldown(ticks)) &&
				playerStatus.getPlayer().getLocation().distanceSquared(allyTeamStatus.getBedCenter()) < 100.0) {
					return playerStatus;
			}
		}
		
		return null;
	}

	
	
	/*
	 * 
	 * 			  _____   _    _    ____    _____
  	 *			 / ____| | |  | |  / __ \  |  __ \
  	 *			| (___   | |__| | | |  | | | |__) |
  	 *		     \___ \  |  __  | | |  | | |  ___/
  	 *	     	 ____) | | |  | | | |__| | | |
 	 *			|_____/  |_|  |_|  \____/  |_|
 	 *
 	 *
 	 */
	
	
	public void applyUpgrade(TeamStatus teamStatus, Upgrade upgrade) {
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (!playerStatus.isSpectator() && playerStatus.getTeam() == teamStatus.getTeam()) {
				applyUpgrade(playerStatus, teamStatus, upgrade);
			}
		}

		// Aggiorna anche gli oggetti nelle casse e le cose globali
		if (upgrade == TeamUpgrade.SHARPNESS) {
			for (Block chest : teamStatus.getTeamChests()) {
				if (chest.getChunk().isLoaded()) {
					if (chest.getState() instanceof Chest) {
						updateSwords(((Chest) chest.getState()).getInventory(), teamStatus);
					}
				} else {
					teamStatus.setChestNeedsRefresh(chest, true);
				}
			}
			
		} else if (upgrade == TeamUpgrade.SPEEDUP_SPAWNERS) {
			double newSpeed = 1 + (0.25 * teamStatus.getUpgradeLevel(TeamUpgrade.SPEEDUP_SPAWNERS));
			
			for (Spawner spawner : teamStatus.getTeamSpawners()) {
				spawner.setTeamUpgradeModifier(newSpeed);
			}
		}
		
		
	}
	
	public void applyUpgrade(PlayerStatus playerStatus, TeamStatus teamStatus, Upgrade upgrade) {
		if (upgrade == PlayerUpgrade.ARMOR_CHAINMAIL ||
			upgrade == PlayerUpgrade.ARMOR_IRON	||
			upgrade == PlayerUpgrade.ARMOR_DIAMOND ||
			upgrade == TeamUpgrade.PROTECTION) {
			
			updateArmor(playerStatus.getPlayer().getInventory(), playerStatus, teamStatus);
			
		} else if (upgrade == TeamUpgrade.SHARPNESS) {
			updateSwords(playerStatus.getPlayer().getEnderChest(), teamStatus);
			updateSwords(playerStatus.getPlayer().getInventory(), teamStatus);
			
		} else if (upgrade == TeamUpgrade.HASTE) {
			updateEffects(playerStatus.getPlayer(), playerStatus, teamStatus);
			
		} else if (upgrade == TeamUpgrade.SPEEDUP_SPAWNERS || upgrade == TeamUpgrade.SPAWN_REGEN) {
			// Ignora, non ha effetti sui giocatori
			
		} else {
			Utils.reportAnomaly("unhandled upgrade", this, upgrade);
		}
	}
	
	
	/*
	 * 
	 *			 _    _   _______   _____   _        _____   _______  __     __
	 *			| |  | | |__   __| |_   _| | |      |_   _| |__   __| \ \   / /
	 *			| |  | |    | |      | |   | |        | |      | |     \ \_/ /
	 *			| |  | |    | |      | |   | |        | |      | |      \   /
	 *			| |__| |    | |     _| |_  | |____   _| |_     | |       | |
	 *			 \____/     |_|    |_____| |______| |_____|    |_|       |_|
	 *
	 *	
	 */
	public void broadcast(String message) {
		for (Player player : getPlayers()) {
			player.sendMessage(Bedwars.PREFIX + message);
		}
	}
	
	public void broadcastSound(Sound sound) {
		for (Player player : getPlayers()) {
			EasySound.quickPlay(player, sound);
		}
	}
	
	public void broadcastTeam(Team team, String message) {
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (playerStatus.getTeam() == team) {
				playerStatus.getPlayer().sendMessage(Bedwars.PREFIX + message);
			}
		}
	}
	
	private void broadcastTeamSound(Team team, Sound sound) {
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (playerStatus.getTeam() == team) {
				EasySound.quickPlay(playerStatus.getPlayer(), sound);
			}
		}
	}
	
	public void broadcastTeamTitle(Team team, String title, String subtitle, int ticksVisible) {
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (playerStatus.getTeam() == team) {
				WildCommons.sendTitle(playerStatus.getPlayer(), 5, ticksVisible, 5, title, subtitle);
			}
		}
	}

	
	public void giveEquip(Player player, PlayerStatus playerStatus, TeamStatus teamStatus) {
		WildCommons.clearInventoryFully(player);
		WildCommons.removePotionEffects(player);
		PlayerInventory inventory = player.getInventory();
		
		if (arenaStatus == ArenaStatus.LOBBY) {
			player.setGameMode(GameMode.ADVENTURE);
			Bukkit.getScheduler().runTask(Bedwars.get(), () -> {
				inventory.addItem(Constants.ITEM_TEAM_PICKER);
			});
		} else {
			if (playerStatus.isSpectator()) {
				player.setGameMode(GameMode.CREATIVE);
				Bukkit.getScheduler().runTask(Bedwars.get(), () -> {
					inventory.setItem(0, WildConstants.Spectator.TELEPORTER);
					if (playerStatus.getTeam() == null) {
						inventory.setItem(8, WildConstants.Spectator.QUIT_SPECTATING);
					}
				});
			} else {
				// Qui non si usa una scheduled task perché è sempre il respawn "finto", dopo i 5 secondi da spettatore
				// Negli altri casi invece si "annulla" la morte e bisogna dare dopo gli oggetti
				player.setGameMode(GameMode.SURVIVAL);
				updateArmor(inventory, playerStatus, teamStatus);
				
				ItemStack sword = ItemBuilder.of(Material.WOOD_SWORD).unbreakable(true).build();
				updateSword(sword, teamStatus);
				inventory.addItem(sword);
				
				updateEffects(player, playerStatus, teamStatus);
			}
		}
	}
	
	private void updateArmor(PlayerInventory inventory, PlayerStatus playerStatus, TeamStatus teamStatus) {
		Team team = teamStatus.getTeam();
		ItemStack[] armorContents = new ItemStack[4];
		armorContents[3] = ItemBuilder.of(Material.LEATHER_HELMET).color(team.getRgbColor()).build();
		armorContents[2] = ItemBuilder.of(Material.LEATHER_CHESTPLATE).color(team.getRgbColor()).build();
		
		if (playerStatus.hasUpgrade(PlayerUpgrade.ARMOR_DIAMOND)) {
			armorContents[1] = ItemBuilder.of(Material.DIAMOND_LEGGINGS).build();
			armorContents[0] = ItemBuilder.of(Material.DIAMOND_BOOTS).build();
		} else if (playerStatus.hasUpgrade(PlayerUpgrade.ARMOR_IRON)) {
			armorContents[1] = ItemBuilder.of(Material.IRON_LEGGINGS).build();
			armorContents[0] = ItemBuilder.of(Material.IRON_BOOTS).build();
		} else if (playerStatus.hasUpgrade(PlayerUpgrade.ARMOR_CHAINMAIL)) {
			armorContents[1] = ItemBuilder.of(Material.CHAINMAIL_LEGGINGS).build();
			armorContents[0] = ItemBuilder.of(Material.CHAINMAIL_BOOTS).build();
		} else {
			armorContents[1] = ItemBuilder.of(Material.LEATHER_LEGGINGS).color(team.getRgbColor()).build();
			armorContents[0] = ItemBuilder.of(Material.LEATHER_BOOTS).color(team.getRgbColor()).build();
		}
		
		int protectionLevel = teamStatus.getUpgradeLevel(TeamUpgrade.PROTECTION);
		if (protectionLevel > 0) {
			armorContents[1].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protectionLevel);
			armorContents[0].addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, protectionLevel);
		}
		
		// Armatura indistruttibile
		for (ItemStack armorPiece : armorContents) {
			ItemMeta meta = armorPiece.getItemMeta();
			meta.spigot().setUnbreakable(true);
			armorPiece.setItemMeta(meta);
		}
		
		inventory.setArmorContents(armorContents);
	}
	
	public void updateSwords(Inventory inventory, TeamStatus teamStatus) {
		int sharpnessLevel = teamStatus.getUpgradeLevel(TeamUpgrade.SHARPNESS);
		
		if (sharpnessLevel > 0) {
			for (ItemStack item : inventory.getContents()) {
				if (item != null && Utils.isSword(item.getType())) {
					updateSword(item, teamStatus, sharpnessLevel);
				}
			}
		}
	}
	
	public void updateSword(ItemStack sword, TeamStatus teamStatus) {
		updateSword(sword, teamStatus, teamStatus.getUpgradeLevel(TeamUpgrade.SHARPNESS));
	}
	
	private void updateSword(ItemStack sword, TeamStatus teamStatus, int sharpnessLevel) {
		if (sharpnessLevel > 0) {
			sword.addUnsafeEnchantment(Enchantment.DAMAGE_ALL, sharpnessLevel);
		} else {
			sword.removeEnchantment(Enchantment.DAMAGE_ALL);
		}
	}
	
	private void updateEffects(Player player, PlayerStatus playerStatus, TeamStatus teamStatus) {
		int hasteLevel = teamStatus.getUpgradeLevel(TeamUpgrade.HASTE);
		
		if (hasteLevel > 0) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, hasteLevel - 1, true, false), true);
		}
	}


	public Team findLowestPlayersTeam(Team[] teams) {
		int[] count = new int[teams.length];
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (playerStatus.getTeam() != null) {
				count[playerStatus.getTeam().ordinal()]++;
			}
		}
		return teams[Utils.findSmallestIntIndex(count)];
	}
	
	
	public int countPlayersByTeam(Team countTeam) {
		int count = 0;
		for (PlayerStatus playerStatus : playerStatuses.values()) {
			if (countTeam == playerStatus.getTeam()) {
				count++;
			}
		}
		return count;
	}
		
	
	public boolean allBedsDestroyed() {
		for (TeamStatus teamStatus : teamStatuses.values()) {
			if (!teamStatus.isBedDestroyed()) {
				return false;
			}
		}
		return true;
	}
	
	private TeamStatus getNearestTeam(Block block) {
		TeamStatus nearestTeamStatus = null;
		double nearestDistance = 0;
		
		for (TeamStatus teamStatus : teamStatuses.values()) {
			double distance = teamStatus.getSpawnPoint().distanceSquared(block.getLocation());
			
			if (nearestTeamStatus == null || distance < nearestDistance) {
				nearestTeamStatus = teamStatus;
				nearestDistance = distance;
			}
		}
		
		return nearestTeamStatus;
	}
	
	public Collection<Player> getPlayers() {
		return playerStatuses.keySet();
	}
	
	public PlayerStatus getPlayerStatus(Player player) {
		return playerStatuses.get(player);
	}
	
	public Collection<PlayerStatus> getPlayerStatuses() {
		return playerStatuses.values();
	}
	
	public Collection<Team> getTeams() {
		return teamStatuses.keySet();
	}
	
	public TeamStatus getTeamStatus(@NonNull Team team) {
		return teamStatuses.get(team);
	}

	public Collection<TeamStatus> getTeamStatuses() {
		return teamStatuses.values();
	}
	

}
