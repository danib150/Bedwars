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
package com.gmail.filoghost.bedwars.arena;

import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.material.Bed;

import com.gmail.filoghost.bedwars.arena.menu.ItemShopMenu;
import com.gmail.filoghost.bedwars.arena.menu.TeamShopMenu;
import com.gmail.filoghost.bedwars.arena.shop.Trap;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.TeamUpgrade;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.UpgradableStatus;
import com.gmail.filoghost.bedwars.arena.spawners.Spawner;
import com.gmail.filoghost.bedwars.arena.specialization.SpecializationType;
import com.gmail.filoghost.bedwars.arena.specialization.SpecializationPoll;
import com.gmail.filoghost.bedwars.hud.shop.ShopMenu;
import com.gmail.filoghost.bedwars.utils.Utils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class TeamStatus extends UpgradableStatus<TeamUpgrade> {
	
	@Getter private final Team team;
	@Getter private final Location spawnPoint;
	@Getter private final int spawnX, spawnY, spawnZ;
	private final Block bedHead, bedFeet;
	@Getter private final Location bedCenter;
	@Getter private boolean bedDestroyed;
	@Getter private ShopMenu individualShop;
	@Getter private ShopMenu teamShop;
	@Getter private List<Spawner> teamSpawners;
	@Getter private List<Block> teamChests;
	private Set<Block> teamChestsToRefresh;
	private Queue<Trap> activeTraps;
	@Getter @Setter private SpecializationPoll activeSpecializationPoll;
	@Getter @Setter private SpecializationType specialization;
	
	public TeamStatus(@NonNull Arena arena, @NonNull Team team, @NonNull Location spawnPoint, @NonNull Block bedHead, @NonNull Block bedFeet) {
		this.team = team;
		this.spawnPoint = spawnPoint;
		this.spawnX = spawnPoint.getBlockX();
		this.spawnY = spawnPoint.getBlockY();
		this.spawnZ = spawnPoint.getBlockZ();
		this.bedHead = bedHead;
		this.bedFeet = bedFeet;
		this.activeTraps = Lists.newLinkedList();
		this.teamShop = new TeamShopMenu(arena, this);
		this.individualShop = new ItemShopMenu(arena, this);
		this.teamSpawners = Lists.newArrayList();
		this.teamChests = Lists.newArrayList();
		teamChestsToRefresh = Sets.newHashSet();
		
		// Centro del letto
		double x, y, z;
		y = bedFeet.getY() + 0.5;
		if (bedFeet.getX() == bedHead.getX()) {
			x = bedFeet.getX() + 0.5;
			z = (bedFeet.getZ() + bedHead.getZ()) / 2.0 + 0.5;
		} else if (bedFeet.getZ() == bedHead.getZ()) {
			x = (bedFeet.getX() + bedHead.getX()) / 2.0 + 0.5;
			z = bedFeet.getZ() + 0.5;
		} else {
			x = bedFeet.getX() + 0.5;
			z = bedFeet.getZ() + 0.5;
			Utils.reportAnomaly("bed is not on the same X or Z axis (location: " + x + " " + y + " " + z + ")");
		}
		bedCenter = new Location(bedFeet.getWorld(), x, y, z);
	}
	
	public boolean isBed(Block block) {
		return Utils.isSameBlock(block, bedHead) || Utils.isSameBlock(block, bedFeet);
	}

	public void destroyBed() {
		bedDestroyed = true;
		bedHead.setType(Material.AIR, false);
		bedFeet.setType(Material.AIR, false);
	}
	
	public boolean isTrapActive(Trap trap) {
		return activeTraps.contains(trap);
	}

	public void setTrapActive(Trap trap, boolean active) {
		if (active) {
			activeTraps.offer(trap);
		} else {
			activeTraps.remove(trap);
		}
	}

	public Trap getNextActiveTrap() {
		return activeTraps.peek();
	}

	public void reset() {
		for (Spawner spawner : teamSpawners) {
			spawner.reset();
		}
		
		clearUpgrades();
		this.teamShop = new TeamShopMenu(teamShop.getArena(), this);
		this.individualShop = new ItemShopMenu(individualShop.getArena(), this);
		
		bedDestroyed = false;
		BlockFace direction = bedFeet.getFace(bedHead);
		
		Bed bedHeadState = new Bed();
		bedHeadState.setHeadOfBed(true);
		bedHeadState.setFacingDirection(direction);
		
		Bed bedFeetState = new Bed();
		bedFeetState.setHeadOfBed(false);
		bedFeetState.setFacingDirection(direction);
		
		setBedBlock(bedHead, bedHeadState);
		setBedBlock(bedFeet, bedFeetState);
		
		activeTraps.clear();
		teamChestsToRefresh.clear();
		
		specialization = null;
		activeSpecializationPoll = null;
	}
	
	private void setBedBlock(Block bedBlock, Bed data) {
		BlockState state = bedBlock.getState();
		state.setType(Material.BED_BLOCK);
		state.setData(data);
		state.update(true, false);
	}

	public void setChestNeedsRefresh(Block chest, boolean needsRefresh) {
		if (needsRefresh) {
			teamChestsToRefresh.add(chest);
		} else {
			teamChestsToRefresh.remove(chest);
		}
	}

	public boolean isChestNeedsRefresh(Block chest) {
		return teamChestsToRefresh.contains(chest);
	}

}
