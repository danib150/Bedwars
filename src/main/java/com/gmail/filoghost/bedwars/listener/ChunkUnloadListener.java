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

import java.util.Iterator;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.gmail.filoghost.bedwars.arena.Arena;
import com.google.common.collect.Sets;

import lombok.AllArgsConstructor;

public class ChunkUnloadListener implements Listener {
	
	private static Set<ChunkInfo> alwaysLoadedChunks = Sets.newHashSet(); // Set perché non ammette duplicati e ha un contains() veloce
	
	public static void setAlwaysLoaded(Chunk chunk, Arena arena) {
		alwaysLoadedChunks.add(new ChunkInfo(chunk.getX(), chunk.getZ(), arena));
		if (!chunk.isLoaded()) {
			chunk.load();
		}
	}
	
	public static void allowUnload(Arena arena) {
		for (Iterator<ChunkInfo> iter = alwaysLoadedChunks.iterator(); iter.hasNext();) {
			if (iter.next().arena == arena) {
				iter.remove();
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onUnload(ChunkUnloadEvent event) {
		Chunk chunk = event.getChunk();
		if (alwaysLoadedChunks.contains(new ChunkInfo(chunk.getX(), chunk.getZ(), null))) { // Non serve l'arena per il solo confronto
			event.setCancelled(true);
		}
	}
	
	
	@AllArgsConstructor
	private static class ChunkInfo {
		
		int x, z;
		Arena arena; // Questa è solo un'info aggiuntiva
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + z;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ChunkInfo other = (ChunkInfo) obj;
			if (x != other.x)
				return false;
			if (z != other.z)
				return false;
			return true;
		}
		
		
	}

}
