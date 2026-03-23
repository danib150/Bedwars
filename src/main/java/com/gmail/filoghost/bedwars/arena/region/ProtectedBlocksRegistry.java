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
package com.gmail.filoghost.bedwars.arena.region;

import org.bukkit.block.Block;

import com.gmail.filoghost.bedwars.utils.IntVector;
import com.gmail.filoghost.bedwars.utils.Utils;

public class ProtectedBlocksRegistry {
	
	public static enum ProtectionReason {
		
		SPAWN,
		BOSS,
		GENERATORS;
		
	}
	
	private byte[][][] bitmask; // Organizzata in modo "lazy" in base alla y (è meno probabile che i giocatori vadano in alto o in basso)
	private IntVector offset;
	private IntVector size;
	
	
	public ProtectedBlocksRegistry(Region region) {
		int xSize = region.getMaxCorner().getX() - region.getMinCorner().getX() + 1;
		int ySize = region.getMaxCorner().getY() - region.getMinCorner().getY() + 1;
		int zSize = region.getMaxCorner().getZ() - region.getMinCorner().getZ() + 1;
		
		offset = region.getMinCorner();
		size = new IntVector(xSize, ySize, zSize);
		bitmask = new byte[ySize][][];
	}
	
	public void setRangeProtectionReason(Block block, int radius, ProtectionReason protectionReason) {
		for (int x = block.getX() - radius; x <= block.getX() + radius; x++) {
			for (int y = block.getY() - radius; y <= block.getY() + radius; y++) {
				for (int z = block.getZ() - radius; z <= block.getZ() + radius; z++) {
					
					int distanceSquared = Utils.square(block.getX() - x) + Utils.square(block.getY() - y) + Utils.square(block.getZ() - z);
					if (distanceSquared <= Utils.square(radius + 0.5)) {
						setProtectionReason(x, y, z, protectionReason);
					}
					
				}
			}
		}
	}

	
	private void setProtectionReason(int blockX, int blockY, int blockZ, ProtectionReason protectionReason) {
		int yIndex = blockY - offset.getY();
		byte[][] yLayer = bitmask[yIndex];
		if (yLayer == null) {
			yLayer = new byte[size.getX()][size.getZ()];
			bitmask[yIndex] = yLayer;
		}
		
		int xIndex = blockX - offset.getX();
		int zIndex = blockZ - offset.getZ();
		yLayer[xIndex][zIndex] = (byte) (protectionReason.ordinal() + 1);
	}
	
	
	public ProtectionReason getProtectionReason(Block block) {
		int yIndex = block.getY() - offset.getY();
		byte[][] yLayer = bitmask[yIndex];
		if (yLayer == null) {
			// Nessuna protezione a questa Y
			return null;
		}
		
		int xIndex = block.getX() - offset.getX();
		int zIndex = block.getZ() - offset.getZ();
		
		byte ordinalValue = yLayer[xIndex][zIndex];
		if (ordinalValue > 0) {
			return ProtectionReason.values()[ordinalValue - 1];
		} else {
			return null;
		}
	}

}
