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
package com.gmail.filoghost.bedwars.hud;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import lombok.Getter;
import wild.api.WildConstants;
import wild.api.sidebar.DynamicSidebarLine;
import wild.api.sidebar.ScoreboardSidebarManager;

public class LobbyScoreboard {
	
	@Getter private Scoreboard scoreboard;
	private ScoreboardSidebarManager sidebarManager;
	
	private DynamicSidebarLine winsLine;
	private DynamicSidebarLine killsLine;
	private DynamicSidebarLine finalKillsLine;
	//private DynamicSidebarLine deathsLine;
	
	
	public LobbyScoreboard() {
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.sidebarManager = new ScoreboardSidebarManager(scoreboard, "       " + ChatColor.WHITE + ChatColor.BOLD + ChatColor.UNDERLINE + "Bed Wars" + ChatColor.RESET + "       ");
		
		sidebarManager.setLine(5, "");
		winsLine = sidebarManager.setDynamicLine(4, "?");
		finalKillsLine = sidebarManager.setDynamicLine(3, "?");
		killsLine = sidebarManager.setDynamicLine(2, "?");
		//deathsLine = sidebarManager.setDynamicLine(2, "?");
		
		sidebarManager.setLine(1, "");
		sidebarManager.setLine(0, WildConstants.Messages.getSidebarIP());
	}
	
	public void setWins(Player player, int wins) {
		winsLine.update(player, ChatColor.WHITE + "Vittorie: " + ChatColor.GRAY + wins);
	}
	
	public void setKills(Player player, int kills) {
		killsLine.update(player, ChatColor.WHITE + "Uccisioni: " + ChatColor.GRAY + kills);
	}
	
	public void setFinalKills(Player player, int finalKills) {
		finalKillsLine.update(player, ChatColor.WHITE + "Uccisioni finali: " + ChatColor.GRAY + finalKills);
	}
	
	public void setDeaths(Player player, int deaths) {
		//deathsLine.update(player, ChatColor.WHITE + "Morti: " + ChatColor.GRAY + deaths);
	}

}
