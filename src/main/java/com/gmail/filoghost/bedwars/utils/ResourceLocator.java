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
package com.gmail.filoghost.bedwars.utils;

import java.util.List;
import java.util.Map;

import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.arena.spawners.Spawner;
import com.google.common.collect.Maps;

import lombok.AllArgsConstructor;

public class ResourceLocator {
	
	private static Map<String, WorldResourcesCache> resourcesByWorld = Maps.newHashMap();
	
	
	public static void setDropOwningSpawner(Item drop, Spawner spawner) {
		drop.setMetadata("spawner", new FixedMetadataValue(Bedwars.get(), spawner)); // La reference allo spawner, non importa se è grande, è sempre la stessa
	}
	
	
	public static int countDropsBySpawner(Spawner spawner) {
		int currentTickstamp = Bedwars.getTickTimer().getTicks();
		String worldName = spawner.getBlock().getWorld().getName();

		// Se è aggiornato, usa la cache
		WorldResourcesCache worldResources = resourcesByWorld.get(worldName);
		if (worldResources != null && worldResources.lastCacheUpdateTickstamp == currentTickstamp) {
			return intValue(worldResources.resourcesBySpawnerCache.get(spawner));
		}
		
		// Se non è aggiornato o è null, pulisce il vecchio o ne crea uno nuovo e lo mette nella map
		if (worldResources == null) {
			worldResources = new WorldResourcesCache(currentTickstamp, Maps.newHashMap());
			resourcesByWorld.put(worldName, worldResources);
		} else {
			worldResources.lastCacheUpdateTickstamp = currentTickstamp;
			worldResources.resourcesBySpawnerCache.clear();
		}
		
		// Inserisce i nuovi valori
		for (Item item : spawner.getBlock().getWorld().getEntitiesByClass(Item.class)) {
			List<MetadataValue> metadata = item.getMetadata("spawner");
			if (!metadata.isEmpty()) {
				Spawner owningSpawner = (Spawner) metadata.get(0).value();
				worldResources.resourcesBySpawnerCache.merge(owningSpawner, item.getItemStack().getAmount(), Integer::sum);
			}
		}
		
		return intValue(worldResources.resourcesBySpawnerCache.get(spawner));
	}
	
	
	private static int intValue(Integer i) {
		return i != null ? i.intValue() : 0;
	}
	
	
	@AllArgsConstructor
	private static class WorldResourcesCache {
		
		private int lastCacheUpdateTickstamp;
		private Map<Spawner, Integer> resourcesBySpawnerCache;
		
	}

}
