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

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.ArenaStatus;
import com.gmail.filoghost.bedwars.arena.PlayerStatus;
import com.gmail.filoghost.bedwars.arena.TeamStatus;
import com.gmail.filoghost.bedwars.database.SQLManager;
import com.gmail.filoghost.bedwars.hud.shop.SpecializationIcon;

import lombok.RequiredArgsConstructor;
import wild.api.WildCommons;
import wild.api.menu.Icon;
import wild.core.nms.interfaces.FancyMessage;

@RequiredArgsConstructor
public class SpecializationManager {

	private final Arena arena;
	
	
	public boolean allTeamsSpecialized() {
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (playerStatus.getTeam() != null) {
				TeamStatus teamStatus = arena.getTeamStatus(playerStatus.getTeam());
				if (teamStatus.getSpecialization() == null && teamStatus.getActiveSpecializationPoll() == null) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	
	public void onPollVote(Player player, SpecializationType spec) {
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			return;
		}
		
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		if (playerStatus.getTeam() == null) {
			return;
		}
		
		TeamStatus teamStatus = arena.getTeamStatus(playerStatus.getTeam());
		if (teamStatus.getActiveSpecializationPoll() == null) {
			player.sendMessage("Il sondaggio per la specializzazione è chiuso.");
			return;
		}
		
		if (!teamStatus.getActiveSpecializationPoll().getVoteOptions().contains(spec)) {
			player.sendMessage("Non puoi scegliere quell'opzione.");
			return;
		}
		
		teamStatus.getActiveSpecializationPoll().setVote(playerStatus, spec);
		player.sendMessage(ChatColor.GRAY + "Hai votato " + spec.getColor() + spec.getName());
		
		if (arena.countPlayersByTeam(teamStatus.getTeam()) == teamStatus.getActiveSpecializationPoll().getHasVotedAmount()) {
			endPoll(teamStatus);
		}
	}
	
	public void endPoll(TeamStatus teamStatus) {
		Collection<SpecializationType> mostVoted = teamStatus.getActiveSpecializationPoll().getMostVoted();
		if (mostVoted.size() > 1) {
			// Troppe scelte, si rifà la votazione tra le opzioni più votate
			teamStatus.setActiveSpecializationPoll(new SpecializationPoll(mostVoted));
			arena.broadcastTeam(teamStatus.getTeam(), "Il sondaggio per la specializzazione è finito in pareggio. Inizia una nuovo sondaggio tra le opzioni più votate.");
			
			
		} else if (mostVoted.size() == 1) {
			// Ok, c'è stata una scelta
			teamStatus.setActiveSpecializationPoll(null);
			SpecializationType votedSpecialization = mostVoted.iterator().next();
			teamStatus.setSpecialization(votedSpecialization);
			arena.broadcastTeam(teamStatus.getTeam(), "Sondaggio finito: il team si è specializzato in " + votedSpecialization.getColor() + votedSpecialization.getName());
			
			for (int slot = 0; slot < teamStatus.getTeamShop().getSize(); slot++) {
				Icon icon = teamStatus.getTeamShop().getIconAt(slot);
				if (icon instanceof SpecializationIcon) {
					((SpecializationIcon) icon).updateIcon();
					teamStatus.getTeamShop().refresh(icon);
				}
			}
			
			SQLManager.insertAnalyticsAsync("spec_choice", votedSpecialization.name(), arena);
			
		} else /* 0, nessuno ha votato */ {
			// Ricomincia da zero e avvisa
			arena.broadcastTeam(teamStatus.getTeam(), "Nessuno ha votato per la specializzazione. La votazione ricomincia.");
			startPoll(teamStatus, new SpecializationPoll(), false);
		}
		
		
	}
	
	
	public void startPoll(TeamStatus teamStatus, SpecializationPoll specializationPoll, boolean initialMessage) {
		teamStatus.setActiveSpecializationPoll(specializationPoll);
		if (initialMessage) {
			arena.broadcastTeam(teamStatus.getTeam(), "E' iniziato il sondaggio per la specializzazione.");
		}
		promptPollOptions(teamStatus, specializationPoll);
	}
	
	
	public void promptPollOptions(TeamStatus teamStatus, SpecializationPoll specializationPoll) {
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (playerStatus.getTeam() == teamStatus.getTeam()) {
				if (!specializationPoll.hasVote(playerStatus)) {
					FancyMessage prompt = WildCommons.fancyMessage(Bedwars.PREFIX).then("Vota: ").color(ChatColor.GRAY);
					for (SpecializationType voteOption : specializationPoll.getVoteOptions()) {
						prompt.then("[" + voteOption.getName() + "]")
							.color(ChatColor.WHITE)
							.command("/votespec " + voteOption.ordinal())
							.tooltip(ChatColor.GRAY + "Vota " + voteOption.getColor() + voteOption.getName());
						prompt.then(" ");
					}
					
					prompt.send(playerStatus.getPlayer());
				}
			}
		}
	}
	
}
