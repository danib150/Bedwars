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

import java.util.Collection;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Wither;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.Team;
import com.gmail.filoghost.bedwars.arena.TeamStatus;
import com.gmail.filoghost.bedwars.arena.entities.EntityOwnership;
import com.gmail.filoghost.bedwars.nms.StationaryWither;
import com.gmail.filoghost.bedwars.utils.Utils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.google.common.collect.Maps;

public class Boss {
	
	private static final int HEALTH_BAR_LENGTH = 10;
	private static final char HEALTH_BAR_SYMBOL = '❤';
	
	private Map<Team, TeamBossStatus> bossStatusByTeam;
	private Hologram hologram;
	private Wither wither;
	
	
	public Boss(Arena arena, Collection<TeamStatus> teamStatuses, Location location, int maxHealth) {
		location = location.clone();
		bossStatusByTeam = Maps.newEnumMap(Team.class);
		hologram = HologramsAPI.createHologram(Bedwars.get(), location);
		hologram.appendTextLine("Uccidi il boss prima degli altri team!");
		
		for (TeamStatus teamStatus : teamStatuses) {
			if (teamStatus.getSpecialization() == null && teamStatus.getActiveSpecializationPoll() == null) {
				bossStatusByTeam.put(teamStatus.getTeam(), new TeamBossStatus(teamStatus.getTeam(), maxHealth, hologram.appendTextLine("")));
			}
		}
		
		wither = StationaryWither.spawn(location);
		//wither.setCustomName("Boss");
		//wither.setCustomNameVisible(false);
		EntityOwnership.set(wither, arena, null);
		
		hologram.teleport(location.add(0, hologram.getHeight() + 3.5, 0));
	}
	
	public void despawn() {
		hologram.delete();
		wither.setHealth(0.0);
		bossStatusByTeam.clear();
	}
	
	public void despawnSilently() {
		hologram.delete();
		wither.remove();
		bossStatusByTeam.clear();
	}
	
	/**
	 * @return true se la salute è andata a zero
	 */
	public boolean damage(Team team, double damage) {
		TeamBossStatus teamBossStatus = bossStatusByTeam.get(team);
		if (teamBossStatus == null) {
			Utils.reportAnomaly("attacking from non-partecipating team", team);
			return false;
		}
		
		return teamBossStatus.damage(damage);
	}
	
	public boolean canBeAttackedBy(Team team) {
		return bossStatusByTeam.containsKey(team);
	}
	
	
	private static class TeamBossStatus {
		
		private Team team;
		private final double maxHealth;
		private double health;
		private TextLine healthBar;
		
		public TeamBossStatus(Team team, double maxHealth, TextLine healthBar) {
			this.team = team;
			this.maxHealth = maxHealth;
			this.health = maxHealth;
			this.healthBar = healthBar;
			refreshBar();
		}

		public boolean damage(double damage) {
			health = Math.max(0.0, health - damage);
			refreshBar();
			return health <= 0.0;
		}
		
		private void refreshBar() {
			double healthPercent = health / maxHealth;
			int coloredSymbolsAmount = (int) Math.ceil(HEALTH_BAR_LENGTH * healthPercent);
			
			StringBuilder progressBar = new StringBuilder(HEALTH_BAR_LENGTH + 4); // 4 = per i due colori
			
			progressBar.append(team.getChatColor());
			for (int i = 0; i < coloredSymbolsAmount; i++) {
				progressBar.append(HEALTH_BAR_SYMBOL);
			}
			
			progressBar.append(ChatColor.DARK_GRAY);
			for (int i = 0; i < (HEALTH_BAR_LENGTH - coloredSymbolsAmount); i++) {
				progressBar.append(HEALTH_BAR_SYMBOL);
			}

			healthBar.setText(team.getChatColor() + "" + progressBar + team.getChatColor());
		}
		
	}

}
