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
package com.gmail.filoghost.bedwars.arena.gameloop;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.ArenaStatus;
import com.gmail.filoghost.bedwars.arena.Constants;
import com.gmail.filoghost.bedwars.arena.PlayerStatus;
import com.gmail.filoghost.bedwars.arena.Team;
import com.gmail.filoghost.bedwars.arena.TeamStatus;
import com.gmail.filoghost.bedwars.arena.spawners.Spawner;
import com.gmail.filoghost.bedwars.database.SQLManager;
import com.gmail.filoghost.bedwars.listener.ChunkUnloadListener;
import com.gmail.filoghost.bedwars.settings.MainSettings;
import com.gmail.filoghost.bedwars.timer.CountdownTimer;
import com.gmail.filoghost.bedwars.utils.Utils;
import com.gmail.filoghost.holographicmobs.object.types.HologramVillager;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wild.api.WildCommons;
import wild.api.scheduler.Countdowns;
import wild.api.util.UnitFormatter;

@RequiredArgsConstructor
public class GameloopManager {
	
	private final Arena arena;
	
	@Getter private CountdownTimer lobbyCountdownTimer;
	@Getter private CountdownTimer combatCountdownTimer;
	
	private long startTime;
	
	
	public void checkWinners() {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			Utils.reportAnomaly("checking winners in wrong arena status", this, arena.getArenaStatus());
			return;
		}
		
		Team winnerTeam = null;
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (playerStatus.getTeam() == null) {
				continue;
			}
			
			if (winnerTeam == null) {
				winnerTeam = playerStatus.getTeam();
			} else {
				if (winnerTeam != playerStatus.getTeam()) {
					// Almeno un team diverso
					return;
				}
			}
		}
		
		// Un team ha vinto se siamo arrivati qui
		long duration = System.currentTimeMillis() - startTime;
		SQLManager.insertAnalyticsAsync("duration", String.valueOf(duration), arena);
		
		arena.setArenaStatus(ArenaStatus.ENDING);
		arena.broadcast(ChatColor.GRAY + "Il team " + winnerTeam.getChatColor() + winnerTeam.getNameSingular() + ChatColor.GRAY + " ha vinto la partita!");
		
		// Statistiche sui rate di vittoria e di sconfitta delle specializzazioni
		for (TeamStatus teamStatus : arena.getTeamStatuses()) {
			SQLManager.insertAnalyticsAsync(teamStatus.getTeam() == winnerTeam ? "spec_win" : "spec_loss", teamStatus.getSpecialization() != null ? teamStatus.getSpecialization().name() : "NONE", arena);
		}
		
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			Player player = playerStatus.getPlayer();
			
			if (playerStatus.getTeam() == winnerTeam) {
				Bedwars.getPlayerData(player).addWin(arena);
			}
			
			if (player.isDead()) {
				WildCommons.respawn(player); // Respawna i giocatori morti per evitare crash
			}
		}
		
		cancelCombatCountdown();
		startWinCountdown();
	}
	
	
	/*
	 * 
	 *			 _         ____    ____    ____   __     __
	 *			| |       / __ \  |  _ \  |  _ \  \ \   / /
	 *			| |      | |  | | | |_) | | |_) |  \ \_/ /
	 *			| |      | |  | | |  _ <  |  _ <    \   /
	 *			| |____  | |__| | | |_) | | |_) |    | |
	 *			|______|  \____/  |____/  |____/     |_|
	 * 
	 * 
	 */
	public void startLobbyCountdown() {
		lobbyCountdownTimer = new CountdownTimer(MainSettings.countdown_start, this::onLobbyCountdown, this::finishLobbyCountdown).start();
	}
	
	public void cancelLobbyCountdown() {
		if (lobbyCountdownTimer != null) {
			lobbyCountdownTimer.cancel();
			lobbyCountdownTimer = null;
		}
		arena.getScoreboard().unsetLobbyCountdown();
	}
	
	private void onLobbyCountdown(int seconds) {
		if (Countdowns.shouldAnnounceCountdown(seconds)) {
			Countdowns.announceStartingCountdown(Bedwars.PREFIX, arena.getPlayers(), seconds);
        }
		arena.getScoreboard().setLobbyCountdown(seconds);
	}
	
	private void finishLobbyCountdown() {
		lobbyCountdownTimer = null;
		
		// Segna i giocatori senza team
		List<PlayerStatus> noTeamPlayers = Lists.newArrayList();
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (playerStatus.getTeam() == null) {
				noTeamPlayers.add(playerStatus);
			}
		}
		Collections.shuffle(noTeamPlayers);
		
		// Assegna un team a tutti
		Team[] playingTeams = arena.getTeams().toArray(new Team[0]);
		for (PlayerStatus playerStatus : noTeamPlayers) {
			playerStatus.setTeam(arena.findLowestPlayersTeam(playingTeams));
		}
		
		arena.setArenaStatus(ArenaStatus.COMBAT);
		arena.getScoreboard().displayGame(arena.getTeams());
		for (Team team : arena.getTeams()) {
			arena.getScoreboard().setTeamStatus(team, false, arena.countPlayersByTeam(team));
		}
		
		// Manda i giocatori ai rispettivi spawn
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			Player player = playerStatus.getPlayer();
			Team team = playerStatus.getTeam();
			TeamStatus teamStatus = arena.getTeamStatus(team);
			player.teleport(teamStatus.getSpawnPoint());
			arena.giveEquip(player, playerStatus, teamStatus);
			player.getEnderChest().clear();
			arena.getScoreboard().addTeamColor(player, team);
			arena.getScoreboard().setKills(player, 0);
			arena.getScoreboard().setDeaths(player, 0);
			if (player.getOpenInventory() != null) {
				player.getOpenInventory().close();
			}
		}
		
		// Impedisce a certi chunk importanti essere unloadati
		for (Spawner spawner : arena.getGlobalSpawners()) {
			ChunkUnloadListener.setAlwaysLoaded(spawner.getBlock().getChunk(), arena);
		}
		for (TeamStatus teamStatus : arena.getTeamStatuses()) {
			for (Spawner spawner : teamStatus.getTeamSpawners()) {
				ChunkUnloadListener.setAlwaysLoaded(spawner.getBlock().getChunk(), arena);
			}
		}
		for (HologramVillager villager : arena.getVillagers()) {
			ChunkUnloadListener.setAlwaysLoaded(villager.getLocation().getChunk(), arena);
		}
		ChunkUnloadListener.setAlwaysLoaded(arena.getBossManager().getBossLocation().getChunk(), arena);
		
		arena.refreshSign();
		arena.getTeleporterMenu().update();
		
		startTime = System.currentTimeMillis();
		Countdowns.announceEndedCountdown(Bedwars.PREFIX, arena.getPlayers());
		startCombatCountdown();
	}
	
	
	
	
	/*
	 * 
	 *			  _____    ____    __  __   ____               _______
	 *			 / ____|  / __ \  |  \/  | |  _ \      /\     |__   __|
	 *			| |      | |  | | | \  / | | |_) |    /  \       | |
	 *			| |      | |  | | | |\/| | |  _ <    / /\ \      | |
	 *			| |____  | |__| | | |  | | | |_) |  / ____ \     | |
	 *			 \_____|  \____/  |_|  |_| |____/  /_/    \_\    |_|
     * 
	 * 
	 */
	private void startCombatCountdown() {
		combatCountdownTimer = new CountdownTimer(MainSettings.countdown_game, this::onCombatCountdown, this::finishCombatCountdown).start();
	}
	
	private void cancelCombatCountdown() {
		if (combatCountdownTimer != null) {
			combatCountdownTimer.cancel();
			combatCountdownTimer = null;
		}
	}
	
	private void onCombatCountdown(int seconds) {
		boolean announce = false;
		if (seconds <= 60) {
			announce = seconds == 60 || seconds == 30 || seconds == 10; // 60, 30, 10 secondi
		} else if (seconds <= 5 * 60) {
			announce = seconds % 60 == 0; // Ogni minuto
		}
		if (announce) {
			arena.broadcast(ChatColor.YELLOW + "Tutti i letti verranno distrutti tra " + UnitFormatter.formatMinutesOrSeconds(seconds) + ".");
		}
		arena.getScoreboard().setGameCountdown(seconds);
	}
	
	private void finishCombatCountdown() {
		combatCountdownTimer = null;

		// Si assume che non tutti i letti fossero stati distrutti, altrimenti questo countdown sarebbe stato cancellato
		for (TeamStatus teamStatus : arena.getTeamStatuses()) {
			if (!teamStatus.isBedDestroyed()) {
				arena.getEvents().onBedDestroy(null, null, teamStatus.getTeam());
			}
		}
		
		arena.broadcastSound(Constants.SOUND_BED_DESTROY);
		arena.broadcast(ChatColor.YELLOW + "Tutti i letti sono stati distrutti!");
		arena.getScoreboard().endGameCountdown();
	}
	
	
	/*
	 * 
	 *		   	__          __  _____   _   _
	 *		   	\ \        / / |_   _| | \ | |
	 * 			 \ \  /\  / /    | |   |  \| |
	 * 			  \ \/  \/ /     | |   | . ` |
	 *  	       \  /\  /     _| |_  | |\  |
	 * 			  	\/  \/     |_____| |_| \_|
	 * 
	 * 
	 */
	private void startWinCountdown() {
		new CountdownTimer(MainSettings.countdown_end, this::onWinCountdown, this::finishWinCountdown).start();
	}
	
	private void onWinCountdown(int seconds) {
		// Lancia fuochi di artificio
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (!playerStatus.isSpectator() && playerStatus.getTeam() != null) {
				// Giocatore del team vincente
				Player player = playerStatus.getPlayer();
				Firework firework = (Firework) player.getWorld().spawnEntity(Utils.getRandomAround(player), EntityType.FIREWORK);
		        firework.setFireworkMeta(playerStatus.getTeam().getWinningFireworkMeta());
			}
		}
	}
	
	private void finishWinCountdown() {
		arena.reset();
	}

}
