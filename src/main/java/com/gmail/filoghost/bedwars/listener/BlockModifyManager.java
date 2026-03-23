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
package com.gmail.filoghost.bedwars.listener;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.Perms;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.utils.Utils;

public class BlockModifyManager {
	
	public static enum Action {
		BREAK,
		PLACE
	}
	
	public static void onModifyAction(Cancellable event, Player player, Action action, Block block, BlockState replacedBlockState) {
		Arena arena = Bedwars.getArenaByPlayer(player);
		if (arena != null) {
			if (arena.getRegion().isInside(block)) {
				// Costruzione di un giocatore dentro le arene mentre sta giocando
				switch (action) {
					case BREAK:
						arena.getEvents().onBreak(event, player, block);
						break;
					case PLACE:
						arena.getEvents().onPlace(event, player, block, replacedBlockState);
						break;
					default:
						event.setCancelled(true);
						Utils.reportAnomaly("action unhandled", action);
				}
			} else {
				// Costruzione di un giocatore fuori dall'arena mentre sta giocando
				event.setCancelled(true);
				if (!arena.getPlayerStatus(player).isSpectator()) {
					player.sendMessage(ChatColor.RED + "Hai raggiunto il limite della mappa."); // Segnala solo se sta giocando effettivamente, e non è nella lobby
				}
			}
		} else {
			// Costruzione di un giocatore fuori dalle arene
			if (!player.hasPermission(Perms.BUILD)) {
				event.setCancelled(true);
			}
		}
	}
	
	public static void onLiquidFlow(Cancellable event, Block from, Block to) {
		for (Arena arena : Bedwars.getAllArenas()) {
			if (arena.getRegion().isInside(from)) {
				arena.getEvents().onFlow(event, to);
			}
		}
	}


}
