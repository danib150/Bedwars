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

import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.filoghost.bedwars.arena.Arena;

import wild.api.WildCommons;
import wild.api.translation.Translation;
import wild.api.util.UnitFormatter;

public class Utils {
	
	private static Map<Player, ItemStack> headsByPlayer = new WeakHashMap<>();
	
	public static ItemStack getHeadItem(Player player) {
		ItemStack headItem = headsByPlayer.get(player);
		
		if (headItem == null) {
			headItem = new ItemStack(Material.SKULL_ITEM);
			headItem.setDurability((short) 3);
			SkullMeta meta = (SkullMeta) headItem.getItemMeta();
			meta.setOwner(player.getName());
			headItem.setItemMeta(meta);
			headsByPlayer.put(player, headItem);
		}
		
		return headItem;
	}
	
	
	public static void consumeOneItemInHand(Player player) {
		if (player.getItemInHand().getAmount() > 1) {
			player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		} else {
			player.setItemInHand(null);
		}
	}
	
	
	public static int findSmallestIntIndex(int[] array) {
		int minIndex = 0;
		int minValue = array[minIndex];
		
		for (int i = 1; i < array.length; i++) {
			if (array[i] < minValue) {
				minIndex = i;
				minValue = array[minIndex];
			}
		}
		
		return minIndex;
	}
	
	
	public static int square(int i) {
		return i * i;
	}
	
	public static int square(double d) {
		return (int) (d * d);
	}
	
	
	public static Location getRandomAround(Player player) {
		Location loc = player.getLocation();
		double angle = Math.random() * Math.PI * 2;
		return new Location(player.getWorld(), loc.getX() + Math.cos(angle) * 2.0, loc.getY(), loc.getZ() + Math.sin(angle) * 2.0);
	}
	
	
	public static Player getRealPlayerDamager(Entity damager) {
		if (damager == null) {
			return null;
		}
		
		if (damager.getType() == EntityType.PLAYER) {
			return (Player) damager;
			
		} else if (damager instanceof Projectile) {
			Projectile projectileDamager = (Projectile) damager;
			if (projectileDamager.getShooter() instanceof Player) {
				return (Player) projectileDamager.getShooter();
			}
		}
		
		return null;
	}
	
	
	public static ItemStack makePotion(PotionEffectType type, int level, int seconds) {
		ItemStack potion = new ItemStack(Material.POTION, 1);
		potion = WildCommons.removeAttributes(potion);
		PotionMeta potionMeta = (PotionMeta) potion.getItemMeta();
		potionMeta.setMainEffect(type);
		potionMeta.addCustomEffect(new PotionEffect(type, seconds * 20, level - 1), true);
		potionMeta.setDisplayName(ChatColor.WHITE + "Pozione di "  + Translation.of(type) + " " + UnitFormatter.getRoman(level) + " (" + (seconds / 60) + ":" + (seconds < 10 ? ("0" + seconds) : (seconds)) + ")");
		potion.setItemMeta(potionMeta);
		return potion;
	}
	
	
	public static int getActivePotionLevel(Player player, PotionEffectType potionType) {
		for (PotionEffect activeEffect : player.getActivePotionEffects()) {
			if (activeEffect.getType() == potionType) {
				return activeEffect.getAmplifier();
			}
		}
		
		return -1;
	}
	
	
	public static boolean canFullyFitItem(Inventory inventory, ItemStack reward) {
		int remaining = reward.getAmount();
		
		for (ItemStack inventoryItem : inventory.getContents()) {
			if (inventoryItem == null || inventoryItem.getType() == Material.AIR) {
				return true; // C'è almeno uno spazio vuoto
			}
			
			if (inventoryItem.isSimilar(reward)) {
				remaining -= inventoryItem.getMaxStackSize() - inventoryItem.getAmount();
				if (remaining <= 0) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	public static boolean isInRange(Block block, int radius, int x, int y, int z) {
		int diffX = Math.abs(block.getX() - x);
		int diffY = Math.abs(block.getY() - y);
		int diffZ = Math.abs(block.getZ() - z);
		
		// Calcolo approssimato
		if (diffX <= radius && diffY <= radius && diffZ <= radius) {
			// Calcolo preciso
			int distanceSquared = Utils.square(diffX) + Utils.square(diffY) + Utils.square(diffZ);
			if (distanceSquared <= Utils.square(radius + 0.5)) {
				return true;
			}
		}
		
		return false;
	}
	

	public static boolean isSameBlock(Block block1, Block block2) {
		return block1.getWorld() == block2.getWorld() && block1.getX() == block2.getX() && block1.getY() == block2.getY() && block1.getZ() == block2.getZ();
	}
	
	
	public static boolean isSword(Material type) {
		switch (type) {
			case DIAMOND_SWORD:
			case GOLD_SWORD:
			case IRON_SWORD:
			case STONE_SWORD:
			case WOOD_SWORD:
				return true;
			default:
				return false;
		}
	}
	

	public static Location roundedLocation(Location location) {
		Location roundedLocation = location.clone();
		roundedLocation.setPitch(0);
		if (Math.abs(roundedLocation.getYaw()) % 45.0f != 0.0f) {
			float yawNormalized = Math.round(roundedLocation.getYaw() / 45.0f) * 45.0f;
			roundedLocation.setYaw(yawNormalized);
		}
		
		roundedLocation.setX(Math.round(roundedLocation.getX() * 2.0) / 2.0);
		roundedLocation.setZ(Math.round(roundedLocation.getZ() * 2.0) / 2.0);
		return roundedLocation;
	}
	
	
	public static void reportAnomaly(String message, Object... params) {
		StringBuilder paramsString = new StringBuilder();
		for (Object param : params) {
			if (paramsString.length() > 0) {
				paramsString.append(", ");
			}
			paramsString.append(formatParam(param));
		}
		
		Thread.dumpStack();
		Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + message + (paramsString.length() > 0 ? " (" + paramsString + ")" : ""));
	}

	
	private static String formatParam(Object param) {
		if (param instanceof Player) {
			return "player: " + ((Player) param).getName();
		} else if (param instanceof Arena) {
			return "arena: " + ((Arena) param).getName();
		} else if (param instanceof Enum) {
			return param.getClass().getSimpleName() + ": " + ((Enum<?>) param).name();
		} else {
			return "unknown object: " + param.toString();
		}
	}

}
