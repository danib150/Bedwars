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
package com.gmail.filoghost.bedwars.database;

import java.util.List;

import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.database.PlayerEvent.PlayerEventType;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlayerData {
	
	private int wins, kills, finalKills, deaths, destroyedBeds;
	private List<PlayerEvent> events;
	private boolean needSave;
	
	public PlayerData(int wins, int kills, int finalKills, int deaths, int destroyedBeds) {
		this.wins = wins;
		this.kills = kills;
		this.finalKills = finalKills;
		this.deaths = deaths;
		this.destroyedBeds = destroyedBeds;
		this.events = Lists.newLinkedList();
	}
	

	public void addWin(Arena arena) {
		needSave = true;
		addEvent(PlayerEventType.WIN, arena);
		wins++;
	}
	
	public void addKill(Arena arena) {
		needSave = true;
		addEvent(PlayerEventType.KILL, arena);
		kills++;
	}
	
	public void addFinalKill(Arena arena) {
		needSave = true;
		addEvent(PlayerEventType.FINAL_KILL, arena);
		finalKills++;
	}
	
	public void addDeath(Arena arena) {
		needSave = true;
		addEvent(PlayerEventType.DEATH, arena);
		deaths++;
	}
	
	public void addDestroyedBed(Arena arena) {
		needSave = true;
		addEvent(PlayerEventType.DESTROYED_BED, arena);
		destroyedBeds++;
	}
	
	public void resetToZero() {
		needSave = true;
		events.clear();
		wins = 0;
		kills = 0;
		finalKills = 0;
		deaths = 0;
		destroyedBeds = 0;
	}

	private void addEvent(PlayerEventType eventType, Arena arena) {
		events.add(new PlayerEvent(eventType, arena.getMaxPlayersPerTeam(), arena.getTeams().size(), arena.getName(), System.currentTimeMillis()));
	}

}
