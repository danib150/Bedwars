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
package com.gmail.filoghost.bedwars.timer;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.ArenaStatus;
import com.gmail.filoghost.bedwars.arena.PlayerStatus;

import lombok.AllArgsConstructor;
import wild.api.WildCommons;
import wild.api.WildConstants;

@AllArgsConstructor
public class RespawnTimer extends BukkitRunnable {
	
	private int countdown;
	private Arena arena;
	private Player player;
	
	public RespawnTimer start() {
		this.runTaskTimer(Bedwars.get(), 20, 20);
		return this;
	}
	
	@Override
	public void run() {
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		if (playerStatus == null || arena.getArenaStatus() != ArenaStatus.COMBAT) {
			cancel();
		}
		
		if (countdown <= 0) {
			cancel();
			playerStatus.setSpectator(player, false);
			arena.getTeleporterMenu().update();
			arena.getEvents().onRespawn(player, false);
			WildCommons.sendTitle(player, 5, 10, 5, ChatColor.GREEN + "Via!", "");
			WildConstants.Sounds.COUNTDOWN_FINISH.playTo(player);
			return;
		}
		
		WildConstants.Sounds.COUNTDOWN_TIMER.playTo(player);
		WildCommons.sendTitle(player, 0, 30, 5, ChatColor.RED + "Sei morto!", ChatColor.YELLOW + "Respawn in: " + countdown);
		countdown--;
	}

}
