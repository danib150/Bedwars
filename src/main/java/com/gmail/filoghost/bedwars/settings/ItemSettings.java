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
package com.gmail.filoghost.bedwars.settings;

import org.bukkit.plugin.Plugin;

import com.gmail.filoghost.bedwars.arena.spawners.ResourceType;
import com.gmail.filoghost.bedwars.settings.objects.ItemConfig;

import net.cubespace.yamler.PreserveStatic;
import net.cubespace.yamler.YamlerConfig;

@PreserveStatic
public class ItemSettings extends YamlerConfig {
	
	public static ItemConfig

		wool = new ItemConfig(20, ResourceType.IRON, 10),
		clay = new ItemConfig(20, ResourceType.IRON, 25),
		endstone = new ItemConfig(10, ResourceType.IRON, 30),
		wood = new ItemConfig(20, ResourceType.GOLD, 10),
		obsidian = new ItemConfig(1, ResourceType.EMERALD, 1),
		ladder = new ItemConfig(10, ResourceType.IRON, 5),
		
		enderpearl = new ItemConfig(1, ResourceType.EMERALD, 4),
		goldenApple = new ItemConfig(1, ResourceType.GOLD, 6),
		fireball = new ItemConfig(1, ResourceType.GOLD, 10),
		waterBucket = new ItemConfig(1, ResourceType.GOLD, 8),
		
		zombie = new ItemConfig(1, ResourceType.GOLD, 15),
		skeleton = new ItemConfig(1, ResourceType.GOLD, 15),
		
		stoneSword = new ItemConfig(1, ResourceType.IRON, 20),
		ironSword = new ItemConfig(1, ResourceType.GOLD, 15),
		diamondSword = new ItemConfig(1, ResourceType.EMERALD, 4),
		knockbackFish = new ItemConfig(1, ResourceType.GOLD, 15),
		
		bow1 = new ItemConfig(1, ResourceType.GOLD, 20),
		bow2 = new ItemConfig(1, ResourceType.GOLD, 40),
		bow3 = new ItemConfig(1, ResourceType.EMERALD, 5),
		arrows = new ItemConfig(5, ResourceType.GOLD, 3),
	
		woodPickaxe = new ItemConfig(1, ResourceType.IRON, 10),
		stonePickaxe = new ItemConfig(1, ResourceType.IRON, 30),
		ironPickaxe = new ItemConfig(1, ResourceType.GOLD, 5),
		diamondPickaxe = new ItemConfig(1, ResourceType.GOLD, 10),
		
		stoneAxe = new ItemConfig(1, ResourceType.IRON, 30),
		ironAxe = new ItemConfig(1, ResourceType.GOLD, 5),
		diamondAxe = new ItemConfig(1, ResourceType.GOLD, 10),
		shears = new ItemConfig(1, ResourceType.IRON, 20),
		
		chainmailArmor = new ItemConfig(1, ResourceType.IRON, 50),
		ironArmor = new ItemConfig(1, ResourceType.GOLD, 20),
		diamondArmor = new ItemConfig(1, ResourceType.EMERALD, 8),
		
		invisibilityPotion = new ItemConfig(1, ResourceType.EMERALD, 2),
		jumpPotion = new ItemConfig(1, ResourceType.EMERALD, 1),
		speedPotion = new ItemConfig(1, ResourceType.EMERALD, 1),
		resistancePotion = new ItemConfig(1, ResourceType.EMERALD, 2),
		
		sharpness = new ItemConfig(1, ResourceType.DIAMOND, 8),
		fastSpawners = new ItemConfig(1, ResourceType.DIAMOND, 6),
		protection = new ItemConfig(1, ResourceType.DIAMOND, 4),
		haste = new ItemConfig(1, ResourceType.DIAMOND, 4),
		spawnRegeneration = new ItemConfig(1, ResourceType.DIAMOND, 8),
		
		blindTrap = new ItemConfig(1, ResourceType.DIAMOND, 5),
		fatigueTrap = new ItemConfig(1, ResourceType.DIAMOND, 5),
		weaknessTrap = new ItemConfig(1, ResourceType.DIAMOND, 5),
	
		// PvP
		knockbackDiamondSword = new ItemConfig(1, ResourceType.EMERALD, 6),
		fireDiamondSword = new ItemConfig(1, ResourceType.EMERALD, 6),
		fishingRod = new ItemConfig(1, ResourceType.GOLD, 5),
		healthBoostPotion = new ItemConfig(1, ResourceType.EMERALD, 2),
		
		// Difesa
		ironBar = new ItemConfig(10, ResourceType.IRON, 10),
		web = new ItemConfig(5, ResourceType.GOLD, 5),
		soulStone = new ItemConfig(10, ResourceType.GOLD, 5),
		ice = new ItemConfig(10, ResourceType.GOLD, 5),
		spawnResistance = new ItemConfig(1, ResourceType.DIAMOND, 8),
				
		// Distruzione
		superDiamondPickaxe = new ItemConfig(1, ResourceType.DIAMOND, 3),
		superDiamondAxe = new ItemConfig(1, ResourceType.DIAMOND, 3),
		tnt = new ItemConfig(1, ResourceType.GOLD, 4),
		creeper = new ItemConfig(1, ResourceType.GOLD, 25);

	

	public ItemSettings(Plugin plugin, String filename) {
		super(plugin, filename);
	}

}
