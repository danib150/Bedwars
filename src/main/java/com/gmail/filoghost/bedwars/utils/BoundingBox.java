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

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class BoundingBox {
	
	private final String world;
	private final double minX, minY, minZ, maxX, maxY, maxZ;
	
	public BoundingBox(Block block) {
		this.world = block.getWorld().getName();
		this.minX = block.getX();
		this.minY = block.getY();
		this.minZ = block.getZ();
		this.maxX = minX + 1;
		this.maxY = minY + 1;
		this.maxZ = minZ + 1;
	}
	
	public BoundingBox(Player player) {
		Location loc = player.getLocation();
		this.world = loc.getWorld().getName();
		this.minX = loc.getX() - 0.3;
		this.maxX = loc.getX() + 0.3;
		this.minY = loc.getY();
		this.maxY = loc.getY() + 1.8;
		this.minZ = loc.getZ() - 0.3;
		this.maxZ = loc.getZ() + 0.3;
	}
	
	public boolean intersects(BoundingBox other) {
		return
			this.minX <= other.maxX && this.maxX >= other.minX &&
			this.minY <= other.maxY && this.maxY >= other.minY &&
			this.minZ <= other.maxZ && this.maxZ >= other.minZ &&
			this.world.equals(other.world);
	}

}
