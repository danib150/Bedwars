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
package com.gmail.filoghost.bedwars.arena.scoreboard;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import com.gmail.filoghost.bedwars.arena.Team;
import com.google.common.collect.Maps;

import lombok.Getter;
import wild.api.WildConstants;
import wild.api.sidebar.DynamicSidebarLine;
import wild.api.sidebar.ScoreboardSidebarManager;

public class ScoreboardManager {
	
	@Getter private Scoreboard scoreboard;
	private ScoreboardSidebarManager sidebar;
	
	private DynamicSidebarLine lobbyCountdownLine;
	private DynamicSidebarLine gameCountdownLine;
	private DynamicSidebarLine bossCountdownLine;
	private DynamicSidebarLine killsLine;
	private DynamicSidebarLine deathsLine;
	
	private Map<Team, DynamicSidebarLine> teamLines;
	private Map<Player, Integer> kills;
	private Map<Player, Integer> deaths;
	
	public ScoreboardManager() {
		reset();
	}
	
	public void reset() {
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.kills = Maps.newHashMap();
		this.deaths = Maps.newHashMap();
		
		lobbyCountdownLine = null;
		gameCountdownLine = null;
		bossCountdownLine = null;
		killsLine = null;
		deathsLine = null;
		
		teamLines = null;
	}
	
	private void resetSidebar() {
		this.sidebar = new ScoreboardSidebarManager(scoreboard, "       " + ChatColor.GREEN + ChatColor.BOLD + "Bed Wars" + ChatColor.RESET + "       ");
		sidebar.setLine(0, WildConstants.Messages.getSidebarIP());
		sidebar.setLine(1, "");
	}
	
	public void displayLobby() {
		resetSidebar();
		
		lobbyCountdownLine = sidebar.setDynamicLine(2, "?");
		sidebar.setLine(3, "");
		unsetLobbyCountdown();
	}
	
	public void setLobbyCountdown(int seconds) {
		int minutes = seconds / 60;
		seconds = seconds % 60;
		lobbyCountdownLine.updateAll(ChatColor.WHITE + "Inizio partita: " + ChatColor.GRAY + String.format("%d:%02d", minutes, seconds));
	}
	
	public void unsetLobbyCountdown() {
		lobbyCountdownLine.updateAll(ChatColor.WHITE + "In attesa...");
	}
	

	public void displayGame(Collection<Team> teams) {
		resetSidebar();
		this.teamLines = new EnumMap<>(Team.class);
		
		for (Team team : teams) {
			org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.registerNewTeam(team.name());
			scoreboardTeam.setPrefix(team.getChatColor().toString());
			scoreboardTeam.setCanSeeFriendlyInvisibles(true);
			//scoreboardTeam.setOption(Option.COLLISION_RULE, OptionStatus.NEVER); TODO: abilitare in 1.9
		}
		
		int index = 2;
		
		deathsLine = sidebar.setDynamicLine(index++, "");
		killsLine = sidebar.setDynamicLine(index++, ""); // Così gli spettatori vedono righe vuote
		sidebar.setLine(index++, "");
		
		for (Team team : Team.values()) {
			// Per mantenere un ordine deterministico (come definiti nell'enum)
			if (teams.contains(team)) {
				DynamicSidebarLine teamLine = sidebar.setDynamicLine(index++, team.getChatColor() + team.getNamePluralCapitalized() + ":" + ChatColor.WHITE + " ?");
				teamLines.put(team, teamLine);
				
			}
		}
		
		sidebar.setLine(index++, "");
		bossCountdownLine = sidebar.setDynamicLine(index++, "");
		gameCountdownLine = sidebar.setDynamicLine(index++, "");
	}
	
	public void setGameCountdown(int seconds) {
		int minutes = seconds / 60;
		seconds = seconds % 60;
		gameCountdownLine.updateAll((minutes == 0 && seconds <= 10 ? ChatColor.YELLOW : ChatColor.WHITE) + "Fase finale: " + ChatColor.GRAY + String.format("%d:%02d", minutes, seconds));
	}
	
	public void endGameCountdown() {
		gameCountdownLine.updateAll(ChatColor.YELLOW + "Fase finale in corso");
	}
	
	public void setBossCountdown(int seconds) {
		int minutes = seconds / 60;
		seconds = seconds % 60;
		bossCountdownLine.updateAll((minutes == 0 && seconds <= 10 ? ChatColor.YELLOW : ChatColor.WHITE) + "Prossimo boss: " + ChatColor.GRAY + String.format("%d:%02d", minutes, seconds));
	}
	
	public void setBossSpawned() {
		bossCountdownLine.updateAll(ChatColor.YELLOW + "Boss generato!");
	}
	
	public void setBossNotSpawning() {
		if (bossCountdownLine != null) {
			bossCountdownLine.updateAll("");
		}
	}
	
	public void setTeamStatus(Team team, boolean bedDestroyed, int playersCount) {
		if (teamLines == null) {
			return;
		}
		
		ChatColor teamColor = playersCount > 0 ? team.getChatColor() : ChatColor.DARK_GRAY;
		ChatColor playersCountColor = playersCount > 0 ? ChatColor.WHITE : ChatColor.DARK_GRAY;
		
		DynamicSidebarLine teamLine = teamLines.get(team);
		teamLine.updateAll((bedDestroyed ? ChatColor.RED + "✖ " : ChatColor.GREEN + "✔ ") + teamColor + team.getNamePluralCapitalized() + ": " + playersCountColor + playersCount);
		
	}
	
	public void addKill(Player killer) {
		int newKillsValue = kills.merge(killer, 1, Integer::sum);
		setKills(killer, newKillsValue);
	}
	
	public void addDeath(Player victim) {
		int newDeathsValue = deaths.merge(victim, 1, Integer::sum);
		setDeaths(victim, newDeathsValue);
	}
	
	public void setKills(Player player, int kills) {
		killsLine.update(player, ChatColor.WHITE + "Uccisioni: " + ChatColor.GRAY + kills);
	}
	
	public void setDeaths(Player player, int deaths) {
		deathsLine.update(player, ChatColor.WHITE + "Morti: " + ChatColor.GRAY + deaths);
	}
	
	public void addTeamColor(Player player, Team team) {
		scoreboard.getTeam(team.name()).addEntry(player.getName());
	}

	public void removeTeamColor(Player player, Team team) {
		scoreboard.getTeam(team.name()).removeEntry(player.getName());
	}

	public void untrackPlayer(Player player) {
		kills.remove(player);
		deaths.remove(player);
	}
	
}
