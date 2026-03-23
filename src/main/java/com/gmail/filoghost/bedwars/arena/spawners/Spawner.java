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
package com.gmail.filoghost.bedwars.arena.spawners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.utils.ResourceLocator;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;

import lombok.Getter;
import lombok.Setter;

@Getter
public class Spawner {
	
	private static final Vector ZERO_VECTOR = new Vector(0, 0, 0);
	
	private static final char PROGRESS_BAR_SYMBOL = '▶';
	private static final int PROGRESS_BAR_LENGTH = 15;
	
	private final ResourceType resource;
	private final Block block;
	@Setter private TextLine progressLine;
	private int lastColoredSymbolsAmount;
	private int spawnInterval;
	
	private int ticks;
	
	private final double teamSizeModifier;
	private double teamUpgradeModifier;
	private double globalSpeedModifier;
	
	public Spawner(ResourceType resource, Block block, int playersPerTeam) {
		this.resource = resource;
		this.block = block;
		
		if (resource.isPublic()) {
			this.teamSizeModifier = 1;
		} else {
			// 1 giocatore = 1x
			// 4 giocatori = 2x
			this.teamSizeModifier = Math.pow(playersPerTeam, 0.333);
		}
		
		String hologramName = null;
		if (resource == ResourceType.DIAMOND) {
			hologramName = "Diamanti";
		} else if (resource == ResourceType.EMERALD) {
			hologramName = "Smeraldi";
		}
		
		if (hologramName != null) {
			Hologram hologram = HologramsAPI.createHologram(Bedwars.get(), block.getLocation().add(0.5, 3.0, 0.5));
			hologram.appendTextLine("Gen. di " + resource.getChatColor() + ChatColor.BOLD + hologramName);
			this.progressLine = hologram.appendTextLine("");
		}
		reset();
	}
	
	public void onTick() {
		// TODO: fare che se ci sono troppi drop non va nemmeno avanti con i ticks?
		
		ticks++;
		if (ticks % spawnInterval == 0) {
			// Genera la risorsa
			if (ResourceLocator.countDropsBySpawner(this) < resource.getSpawnLimit()) {
				Item itemEntity = block.getWorld().dropItem(new Location(block.getWorld(), block.getX() + 0.5, block.getY() + 2.0, block.getZ() + 0.5), new ItemStack(resource.getItemMaterial()));
				itemEntity.setVelocity(ZERO_VECTOR);
				ResourceLocator.setDropOwningSpawner(itemEntity, this);
			}
		}
		
		if (progressLine != null) {
			double progress = (double) (ticks % spawnInterval) / (double) spawnInterval;
			int coloredSymbolsAmount = (int) (PROGRESS_BAR_LENGTH * progress);
			
			if (coloredSymbolsAmount != this.lastColoredSymbolsAmount) {
				this.lastColoredSymbolsAmount = coloredSymbolsAmount;
				StringBuilder progressBar = new StringBuilder(PROGRESS_BAR_LENGTH + 4); // 4 = per i due colori
				
				progressBar.append(ChatColor.WHITE);
				for (int i = 0; i < coloredSymbolsAmount; i++) {
					progressBar.append(PROGRESS_BAR_SYMBOL);
				}
				
				progressBar.append(ChatColor.DARK_GRAY);
				for (int i = 0; i < (PROGRESS_BAR_LENGTH - coloredSymbolsAmount); i++) {
					progressBar.append(PROGRESS_BAR_SYMBOL);
				}
				
				progressLine.setText(progressBar.toString());
			}
			
			
		}
	}

	public void setTeamUpgradeModifier(double speed) {
		this.teamUpgradeModifier = speed;
		updateSpawnInterval();
	}
	
	public void setGlobalSpeedModifier(double speed) {
		this.globalSpeedModifier = speed;
		updateSpawnInterval();
	}
	
	private void updateSpawnInterval() {
		this.spawnInterval = Math.max((int) (getResource().getSpawnInterval() / (teamUpgradeModifier * teamSizeModifier * globalSpeedModifier)), 1); // Non può essere meno di 1
	}
	
	public void reset() {
		ticks = 0;
		teamUpgradeModifier = 1;
		globalSpeedModifier = 1;
		lastColoredSymbolsAmount = -1;
		updateSpawnInterval();
		
		if (progressLine != null) {
			progressLine.setText("");
		}
	}

	

}
