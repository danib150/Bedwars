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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import com.gmail.filoghost.bedwars.arena.PlayerStatus;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.val;

public class SpecializationPoll {
	
	@Getter private Collection<SpecializationType> voteOptions;
	private Map<PlayerStatus, SpecializationType> votesByPlayer = Maps.newHashMap();
	@Getter private int ticksRemaining = 60 * 20;
	
	public SpecializationPoll() {
		this.voteOptions = Arrays.asList(SpecializationType.values());
	}
	
	public SpecializationPoll(Collection<SpecializationType> voteOptions) {
		this.voteOptions = Lists.newArrayList(voteOptions);
	}
	
	public void onTick() {
		ticksRemaining--;
	}
	
	public int getHasVotedAmount() {
		return votesByPlayer.size();
	}
	
	public boolean hasVote(PlayerStatus playerStatus) {
		return votesByPlayer.containsKey(playerStatus);
	}
	
	public void removeVote(PlayerStatus playerStatus) {
		votesByPlayer.remove(playerStatus);
	}
	
	public void setVote(PlayerStatus playerStatus, SpecializationType spec) {
		votesByPlayer.put(playerStatus, spec);
	}
	
	public Collection<SpecializationType> getMostVoted() {
		EnumMap<SpecializationType, Integer> votesBySpec = Maps.newEnumMap(SpecializationType.class);
		for (val entry : votesByPlayer.entrySet()) {
			votesBySpec.merge(entry.getValue(), entry.getKey().isStartedSpecializationPoll() ? 11 : 10, Integer::sum); // Peso diverso per non far pareggiare chi ha iniziato il sondaggio
		}
		
		if (votesBySpec.isEmpty()) {
			return votesBySpec.keySet();
		}
		
		// Calcola il maggior numero di voti
		int maxVotes = Collections.max(votesBySpec.values());
		
		// Rimuove tutte le cose con meno voti del max
		votesBySpec.values().removeIf(value -> value < maxVotes);
		
		return votesBySpec.keySet();
	}

	

}
