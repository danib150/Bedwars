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
package com.gmail.filoghost.bedwars.arena.shop;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import lombok.Getter;

public enum Trap {
	
	BLIND_SLOW("Cecità e lentezza I", 5 * 20, Material.LEATHER_BOOTS, player -> {
		player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 1, true, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 1, true, false));
	}),
	CONFUSION("Confusione e salto bloccato", 5 * 20, Material.LEATHER_BOOTS, player -> {
		player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 5 * 20, 1, true, false));
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 5 * 20, 128, true, false));
	}),
	WEAKNESS("Debolezza II", 1 * 20, Material.LEATHER_CHESTPLATE, player -> {
		player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 10 * 20, 2, true, false));
	}),
	FATIGUE("Fatica da scavo III", 15 * 20, Material.WOOD_PICKAXE, player -> {
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 15 * 20, 3, true, false));
	});
	
	@Getter private final String name;
	@Getter private final int durationTicks;
	@Getter private final Material icon;
	@Getter private final TrapReaction trapReaction;
	
	private Trap(String name, int durationTicks, Material icon, TrapReaction trapReaction) {
		this.name = "Trappola: " + name.substring(0, 1).toLowerCase() + name.substring(1, name.length());
		this.durationTicks = durationTicks;
		this.icon = icon;
		this.trapReaction = trapReaction;
	}



	public interface TrapReaction {
		void onTrigger(Player player);
	}

}
