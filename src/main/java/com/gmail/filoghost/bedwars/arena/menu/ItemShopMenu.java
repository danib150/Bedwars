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
package com.gmail.filoghost.bedwars.arena.menu;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.PotionEffectType;

import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.PlayerStatus;
import com.gmail.filoghost.bedwars.arena.TeamStatus;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.PlayerUpgrade;
import com.gmail.filoghost.bedwars.hud.shop.ItemBuyableIcon;
import com.gmail.filoghost.bedwars.hud.shop.ShopMenu;
import com.gmail.filoghost.bedwars.hud.shop.ShopSubMenu;
import com.gmail.filoghost.bedwars.hud.shop.UpgradeBuyableIcon;
import com.gmail.filoghost.bedwars.settings.ItemSettings;
import com.gmail.filoghost.bedwars.utils.Utils;

import wild.api.item.ItemBuilder;
import wild.api.menu.IconBuilder;

public class ItemShopMenu extends ShopMenu {
	
	private static final String
		TITLE_BLOCKS = 		"Blocchi",
		TITLE_UTILITY =		"Utilità",
		TITLE_TOOLS = 		"Strumenti",
		TITLE_SWORDS = 		"Spade",
		TITLE_BOWS = 		"Archi",
		TITLE_ARMORS = 		"Armature",
		TITLE_POTIONS =		"Pozioni";
	

	public ItemShopMenu(Arena arena, TeamStatus teamStatus) {
		super(arena, "Negozio", 3);
		
		ShopSubMenu blocksMenu = getBlocksMenu(teamStatus);
		setIcon(2, 2, new IconBuilder(Material.WOOL).name(TITLE_BLOCKS).clickHandler(blocksMenu::open).build());
		
		ShopSubMenu utilitiesMenu = getUtilitiesMenu();
		setIcon(3, 2, new IconBuilder(Material.TNT).name(TITLE_UTILITY).clickHandler(utilitiesMenu::open).build());
		
		ShopSubMenu swordsMenu = getSwordsMenu();
		setIcon(4, 2, new IconBuilder(Material.DIAMOND_SWORD).name(TITLE_SWORDS).clickHandler(swordsMenu::open).build());
		
		ShopSubMenu bowsMenu = getBowsMenu();
		setIcon(5, 2, new IconBuilder(Material.BOW).name(TITLE_BOWS).clickHandler(bowsMenu::open).build());
		
		setIcon(6, 2, new IconBuilder(Material.DIAMOND_CHESTPLATE).name(TITLE_ARMORS).clickHandler(clicker -> openPlayerStatusMenu(clicker, this::getArmorMenu)).build());
		
		ShopSubMenu toolsMenu = getToolsMenu();
		setIcon(7, 2, new IconBuilder(Material.IRON_PICKAXE).name(TITLE_TOOLS).clickHandler(toolsMenu::open).build());
		
		ShopSubMenu potionsMenu = getPotionsMenu();
		setIcon(8, 2, new IconBuilder(Material.POTION).name(TITLE_POTIONS).clickHandler(potionsMenu::open).build());
		
		refresh();
	}


	private ShopSubMenu getBlocksMenu(TeamStatus teamStatus) {
		ShopSubMenu menu = new ShopSubMenu(this, TITLE_BLOCKS, 3);
		menu.setIcon(2, 2, new ItemBuyableIcon(menu, "Lana", ItemSettings.wool, ItemBuilder.of(Material.WOOL).durability(teamStatus.getTeam().getWoolColor())));
		menu.setIcon(3, 2, new ItemBuyableIcon(menu, "Argilla", ItemSettings.clay, ItemBuilder.of(Material.STAINED_CLAY).durability(teamStatus.getTeam().getWoolColor())));
		menu.setIcon(4, 2, new ItemBuyableIcon(menu, "Pietra dell'End", ItemSettings.endstone, ItemBuilder.of(Material.ENDER_STONE)));
		menu.setIcon(5, 2, new ItemBuyableIcon(menu, "Legna", ItemSettings.wood, ItemBuilder.of(Material.WOOD)));
		menu.setIcon(6, 2, new ItemBuyableIcon(menu, "Ossidiana", ItemSettings.obsidian, ItemBuilder.of(Material.OBSIDIAN)));
		menu.setIcon(7, 2, new ItemBuyableIcon(menu, "Scale", ItemSettings.ladder, ItemBuilder.of(Material.LADDER)));
		menu.refresh();
		return menu;
	}
	
	private ShopSubMenu getUtilitiesMenu() {
		ShopSubMenu menu = new ShopSubMenu(this, TITLE_UTILITY, 3);
		menu.setIcon(2, 2, new ItemBuyableIcon(menu, "Perla Ender", ItemSettings.enderpearl, ItemBuilder.of(Material.ENDER_PEARL)));
		menu.setIcon(3, 2, new ItemBuyableIcon(menu, "Mela d'Oro", ItemSettings.goldenApple, ItemBuilder.of(Material.GOLDEN_APPLE)));
		menu.setIcon(4, 2, new ItemBuyableIcon(menu, "Palla di Fuoco", ItemSettings.fireball, ItemBuilder.of(Material.FIREBALL)));
		menu.setIcon(5, 2, new ItemBuyableIcon(menu, "Secchio d'Acqua", ItemSettings.waterBucket, ItemBuilder.of(Material.WATER_BUCKET)));
		menu.setIcon(6, 2, new ItemBuyableIcon(menu, "Alleato Zombie", ItemSettings.zombie, new SpawnEgg(EntityType.ZOMBIE).toItemStack()));
		menu.setIcon(7, 2, new ItemBuyableIcon(menu, "Alleato Scheletro", ItemSettings.skeleton, new SpawnEgg(EntityType.SKELETON).toItemStack()));

		menu.refresh();
		return menu;
	}
	
	private ShopSubMenu getSwordsMenu() {
		ShopSubMenu menu = new ShopSubMenu(this, TITLE_SWORDS, 3);
		menu.setIcon(2, 2, new ItemBuyableIcon(menu, "Spada di Pietra", ItemSettings.stoneSword, ItemBuilder.of(Material.STONE_SWORD)));
		menu.setIcon(3, 2, new ItemBuyableIcon(menu, "Spada di Ferro", ItemSettings.ironSword, ItemBuilder.of(Material.IRON_SWORD)));
		menu.setIcon(4, 2, new ItemBuyableIcon(menu, "Spada di Diamante", ItemSettings.diamondSword, ItemBuilder.of(Material.DIAMOND_SWORD)));
		menu.setIcon(5, 2, new ItemBuyableIcon(menu, "Pesce (Contraccolpo I)", ItemSettings.knockbackFish, ItemBuilder.of(Material.RAW_FISH).enchant(Enchantment.KNOCKBACK, 1)));
		menu.refresh();
		return menu;
	}
	
	private ShopSubMenu getBowsMenu() {
		ShopSubMenu menu = new ShopSubMenu(this, TITLE_BOWS, 3);
		menu.setIcon(2, 2, new ItemBuyableIcon(menu, "Arco", ItemSettings.bow1, ItemBuilder.of(Material.BOW)));
		menu.setIcon(3, 2, new ItemBuyableIcon(menu, "Arco (Potenza I)", ItemSettings.bow2, ItemBuilder.of(Material.BOW).enchant(Enchantment.ARROW_DAMAGE)));
		menu.setIcon(4, 2, new ItemBuyableIcon(menu, "Arco (Potenza I, Contraccolpo I)", ItemSettings.bow3, ItemBuilder.of(Material.BOW).enchant(Enchantment.ARROW_DAMAGE).enchant(Enchantment.ARROW_KNOCKBACK)));
		menu.setIcon(5, 2, new ItemBuyableIcon(menu, "Frecce", ItemSettings.arrows, ItemBuilder.of(Material.ARROW)));
		menu.refresh();
		return menu;
	}
	
	private ShopSubMenu getToolsMenu() {
		ShopSubMenu menu = new ShopSubMenu(this, TITLE_TOOLS, 4);
		menu.setIcon(2, 2, new ItemBuyableIcon(menu, "Piccone di Pietra", ItemSettings.stonePickaxe, ItemBuilder.of(Material.STONE_PICKAXE)));
		menu.setIcon(3, 2, new ItemBuyableIcon(menu, "Piccone di Ferro", ItemSettings.ironPickaxe, ItemBuilder.of(Material.IRON_PICKAXE)));
		menu.setIcon(4, 2, new ItemBuyableIcon(menu, "Piccone di Diamante", ItemSettings.diamondPickaxe, ItemBuilder.of(Material.DIAMOND_PICKAXE)));
		menu.setIcon(5, 2, new ItemBuyableIcon(menu, "Ascia di Pietra", ItemSettings.stoneAxe, ItemBuilder.of(Material.STONE_AXE)));
		menu.setIcon(6, 2, new ItemBuyableIcon(menu, "Ascia di Ferro", ItemSettings.ironAxe, ItemBuilder.of(Material.IRON_AXE)));
		menu.setIcon(7, 2, new ItemBuyableIcon(menu, "Ascia di Diamante", ItemSettings.diamondAxe, ItemBuilder.of(Material.DIAMOND_AXE)));
		menu.setIcon(8, 3, new ItemBuyableIcon(menu, "Cesoie", ItemSettings.shears, ItemBuilder.of(Material.SHEARS)));
		menu.refresh();
		return menu;
	}
	
	private ShopSubMenu getPotionsMenu() {
		ShopSubMenu menu = new ShopSubMenu(this, TITLE_POTIONS, 3);
		menu.setIcon(2, 2, new ItemBuyableIcon(menu, "Pozione di Velocità II", ItemSettings.speedPotion, Utils.makePotion(PotionEffectType.SPEED, 2, 50)));
		menu.setIcon(3, 2, new ItemBuyableIcon(menu, "Pozione di Salto III", ItemSettings.jumpPotion, Utils.makePotion(PotionEffectType.JUMP, 3, 50)));
		menu.setIcon(4, 2, new ItemBuyableIcon(menu, "Pozione di Invisibilità", ItemSettings.invisibilityPotion, Utils.makePotion(PotionEffectType.INVISIBILITY, 1, 30)));
		menu.setIcon(5, 2, new ItemBuyableIcon(menu, "Pozione di Resistenza I", ItemSettings.resistancePotion, Utils.makePotion(PotionEffectType.DAMAGE_RESISTANCE, 1, 30)));
		menu.refresh();
		return menu;
	}
	
	private ShopSubMenu getArmorMenu(PlayerStatus playerStatus) {
		ShopSubMenu menu = new ShopSubMenu(this, TITLE_ARMORS, 3);
		menu.setIcon(2, 2, new UpgradeBuyableIcon<>(menu, price(ItemSettings.chainmailArmor.priceType, ItemSettings.chainmailArmor.priceNumber), PlayerUpgrade.ARMOR_CHAINMAIL, playerStatus));
		menu.setIcon(3, 2, new UpgradeBuyableIcon<>(menu, price(ItemSettings.ironArmor.priceType, ItemSettings.ironArmor.priceNumber), PlayerUpgrade.ARMOR_IRON, playerStatus));
		menu.setIcon(4, 2, new UpgradeBuyableIcon<>(menu, price(ItemSettings.diamondArmor.priceType, ItemSettings.diamondArmor.priceNumber), PlayerUpgrade.ARMOR_DIAMOND, playerStatus));
		menu.refresh();
		return menu;
	}

	private void openPlayerStatusMenu(Player clicker, IndividualSubMenuGenerator generator) {
		PlayerStatus playerStatus = getArena().getPlayerStatus(clicker);
		if (playerStatus != null) {
			generator.generate(playerStatus).open(clicker);
		} else {
			clicker.sendMessage(ChatColor.RED + "Non sei più nell'arena.");
			Utils.reportAnomaly("opening menu while not in arena", getArena(), clicker);
		}
	}
	
	private interface IndividualSubMenuGenerator {
		
		ShopSubMenu generate(PlayerStatus playerStatus);
		
	}

}
