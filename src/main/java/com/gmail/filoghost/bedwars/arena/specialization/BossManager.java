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
package com.gmail.filoghost.bedwars.arena.specialization;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.ArenaStatus;
import com.gmail.filoghost.bedwars.arena.PlayerStatus;
import com.gmail.filoghost.bedwars.arena.Team;
import com.gmail.filoghost.bedwars.settings.MainSettings;
import com.gmail.filoghost.bedwars.utils.Utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import wild.api.util.UnitFormatter;

@RequiredArgsConstructor
public class BossManager {
	
	private final Arena arena;
	@Getter private final Location bossLocation;
	
	private Boss currentBoss;
	private int bossCountdown;
	private boolean allowBossSpawn;
	
	
	private void spawnBoss() {
		currentBoss = new Boss(arena, arena.getTeamStatuses(), bossLocation, 200 * arena.getMaxPlayersPerTeam());
		arena.getRegion().getWorld().strikeLightningEffect(bossLocation);
		arena.broadcast(ChatColor.YELLOW + "Un boss è stato generato al centro della mappa!");
	}
	
	
	public void onBossCountdown() {
		if (!allowBossSpawn) {
			return;
		}
		
		if (currentBoss != null) {
			// Combattimento in corso
			return;
		}
		
		if (bossCountdown <= 0) {
			arena.getScoreboard().setBossSpawned();
			spawnBoss();
			
		} else {
			arena.getScoreboard().setBossCountdown(bossCountdown);
			if (bossCountdown == 60 || bossCountdown == 30 || bossCountdown == 10) {
				arena.broadcast(ChatColor.YELLOW + "Un boss verrà generato tra " + UnitFormatter.formatMinutesOrSeconds(bossCountdown) + ".");
			}
		}
		bossCountdown--;
	}
	
	
	public void onBossDamage(EntityDamageEvent event, Player attacker) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			event.setCancelled(true);
			return;
		}


		PlayerStatus attackerStatus = arena.getPlayerStatus(attacker);
		if (attackerStatus.isSpectator()) {
			event.setCancelled(true);
			return;
		}
		
		if (currentBoss == null) {
			Utils.reportAnomaly("boss being attacked when there is no boss", this);
			return;
		}
		
		if (!currentBoss.canBeAttackedBy(attackerStatus.getTeam())) {
			attacker.sendMessage(ChatColor.RED + "Il tuo team ha già sconfitto un boss.");
			event.setCancelled(true);
			return;
		}
		
		if (currentBoss.damage(attackerStatus.getTeam(), event.getFinalDamage())) {
			endBoss(attackerStatus, attackerStatus.getTeam());
		}
	}
	
	
	public void endBossForever() {
		if (currentBoss != null) {
			currentBoss.despawnSilently();
			currentBoss = null;
		}
		allowBossSpawn = false;
		arena.getScoreboard().setBossNotSpawning();
	}
	
	
	private void endBoss(PlayerStatus lastHitter, Team winnerTeam) {
		currentBoss.despawn();
		currentBoss = null;
		arena.broadcast("Il team " + winnerTeam.getChatColor() + winnerTeam.getNameSingular() + ChatColor.GRAY + " ha sconfitto il boss!");
		
		lastHitter.setStartedSpecializationPoll(true);
		arena.getSpecManager().startPoll(arena.getTeamStatus(winnerTeam), new SpecializationPoll(), true);
		arena.broadcastTeamTitle(winnerTeam, ChatColor.GREEN + "Specializzazione sbloccata", "Segui la chat per scegliere", 60);
		
		if (arena.getSpecManager().allTeamsSpecialized()) {
			allowBossSpawn = false;
			arena.getScoreboard().setBossNotSpawning();
			
		} else {
			// Se c'è almeno un giocatore ancora in gioco di un team senza specializzazione, spawna un altro boss
			bossCountdown = MainSettings.getSubsequentBossCountdown(arena.getTeams().size());
		}
	}

	
	public void reset() {
		bossCountdown = MainSettings.getInitialBossCountdown(arena.getTeams().size());
		allowBossSpawn = true;
	}

}
