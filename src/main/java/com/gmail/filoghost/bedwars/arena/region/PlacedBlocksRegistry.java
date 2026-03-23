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

import java.util.List;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.gmail.filoghost.bedwars.utils.IntVector;
import com.google.common.collect.Lists;

import lombok.AllArgsConstructor;
import lombok.ToString;

@SuppressWarnings("deprecation")
public class PlacedBlocksRegistry {
	
	private World world;
	private boolean[][][] bitmask; // Organizzata in modo "lazy" in base alla y (è meno probabile che i giocatori vadano in alto o in basso)
	private IntVector offset;
	private IntVector size;
	private List<BlockRestoreInfo> blocksToRestore;
	
	
	public PlacedBlocksRegistry(Region region) {
		int xSize = region.getMaxCorner().getX() - region.getMinCorner().getX() + 1;
		int ySize = region.getMaxCorner().getY() - region.getMinCorner().getY() + 1;
		int zSize = region.getMaxCorner().getZ() - region.getMinCorner().getZ() + 1;
		
		world = region.getWorld();
		offset = region.getMinCorner();
		size = new IntVector(xSize, ySize, zSize);
		bitmask = new boolean[ySize][][];
		blocksToRestore = Lists.newLinkedList();
	}
	
	/**
	 * 	Salva lo stato del blocco per poi ripristinarlo in seguito
	 */
	public void saveBlockState(BlockState blockState) {
		if (blockState.getTypeId() != 0) {
			Block block = blockState.getBlock();
			blocksToRestore.add(new BlockRestoreInfo(block.getX(), block.getY(), block.getZ(), blockState.getTypeId(), blockState.getRawData()));
		}
	}
	
	public void saveBlockState(Block block) {
		if (block.getTypeId() != 0) {
			blocksToRestore.add(new BlockRestoreInfo(block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData()));
		}
	}


	
	/**
	 * @return true se è la prima volta che viene posizionato
	 */
	public boolean setPlayerPlaced(Block block) {
		int yIndex = block.getY() - offset.getY();
		boolean[][] yLayer = bitmask[yIndex];
		if (yLayer == null) {
			yLayer = new boolean[size.getX()][size.getZ()];
			bitmask[yIndex] = yLayer;
		}
		
		int xIndex = block.getX() - offset.getX();
		int zIndex = block.getZ() - offset.getZ();
		if (yLayer[xIndex][zIndex]) {
			return false;
		} else {
			yLayer[xIndex][zIndex] = true;
			return true;
		}
	}
	
	
	/**
	 * Nota: questo metodo non controlla che il blocco sia nella regione.
	 * Se è fuori -> ArrayOutOfBoundsException
	 */
	public boolean isPlayerPlaced(Block block) {
		int yIndex = block.getY() - offset.getY();
		boolean[][] yLayer = bitmask[yIndex];
		if (yLayer == null) {
			// Nessun giocatore ha mai costruito a questa Y
			return false;
		}
		
		int xIndex = block.getX() - offset.getX();
		int zIndex = block.getZ() - offset.getZ();
		
		return yLayer[xIndex][zIndex];
	}

	
	public void restore() {
		for (int yIndex = 0; yIndex < size.getY(); yIndex++) {
			boolean[][] yLayer = bitmask[yIndex];
			if (yLayer == null) {
				continue;
			}
			
			for (int xIndex = 0; xIndex < size.getX(); xIndex++) {
				for (int zIndex = 0; zIndex < size.getZ(); zIndex++) {
					if (yLayer[xIndex][zIndex] == true) {
						world.getBlockAt(xIndex + offset.getX(), yIndex + offset.getY(), zIndex + offset.getZ()).setType(Material.AIR, false);
						yLayer[xIndex][zIndex] = false;
					}
				}
			}
			
			bitmask[yIndex] = null;
		}
		
		// Ripristina i blocchi salvati
		for (BlockRestoreInfo blockRestoreInfo : blocksToRestore) {
			world.getBlockAt(blockRestoreInfo.x, blockRestoreInfo.y, blockRestoreInfo.z).setTypeIdAndData(blockRestoreInfo.type, blockRestoreInfo.data, false);
		}
		blocksToRestore.clear();
	}
	
	
	@AllArgsConstructor
	@ToString
	private static class BlockRestoreInfo {
		
		private int x, y, z;
		private int type;
		private byte data;
		
	}

}
