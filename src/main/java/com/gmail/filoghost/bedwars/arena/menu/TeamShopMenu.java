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

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.PotionEffectType;

import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.TeamStatus;
import com.gmail.filoghost.bedwars.arena.shop.Trap;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.TeamUpgrade;
import com.gmail.filoghost.bedwars.arena.spawners.ResourceType;
import com.gmail.filoghost.bedwars.arena.specialization.SpecializationType;
import com.gmail.filoghost.bedwars.hud.shop.ItemBuyableIcon;
import com.gmail.filoghost.bedwars.hud.shop.Price;
import com.gmail.filoghost.bedwars.hud.shop.ShopMenu;
import com.gmail.filoghost.bedwars.hud.shop.ShopSubMenu;
import com.gmail.filoghost.bedwars.hud.shop.SpecializationIcon;
import com.gmail.filoghost.bedwars.hud.shop.TrapBuyableIcon;
import com.gmail.filoghost.bedwars.hud.shop.UpgradeBuyableIcon;
import com.gmail.filoghost.bedwars.settings.ItemSettings;
import com.gmail.filoghost.bedwars.utils.Utils;

import wild.api.item.ItemBuilder;

/*
 * Nota: i livelli partono da 0, va incluso il costo anche per quello.
 */
public class TeamShopMenu extends ShopMenu {
	
	private ShopSubMenu pvpMenu, defenseMenu, destructionMenu;
	
	public TeamShopMenu(Arena arena, TeamStatus teamStatus) {
		super(arena, "Negozio", 4);
		
		pvpMenu = getPvPMenu();
		defenseMenu = getDefenseMenu(teamStatus);
		destructionMenu = getDestructionMenu();
		
		setIcon(2, 2, new UpgradeBuyableIcon<>(this, (upgradeLevel) -> new Price(ItemSettings.sharpness.priceType, (upgradeLevel + 1) * ItemSettings.sharpness.priceNumber), TeamUpgrade.SHARPNESS, teamStatus));
		setIcon(3, 2, new UpgradeBuyableIcon<>(this, (upgradeLevel) -> new Price(ItemSettings.fastSpawners.priceType, (upgradeLevel + 1) * ItemSettings.fastSpawners.priceNumber), TeamUpgrade.SPEEDUP_SPAWNERS, teamStatus));
		setIcon(4, 2, new UpgradeBuyableIcon<>(this, (upgradeLevel) -> new Price(ItemSettings.protection.priceType, (upgradeLevel + 1) * ItemSettings.protection.priceNumber), TeamUpgrade.PROTECTION, teamStatus));
		setIcon(5, 2, new UpgradeBuyableIcon<>(this, (upgradeLevel) -> new Price(ItemSettings.haste.priceType, (upgradeLevel + 1) * ItemSettings.haste.priceNumber), TeamUpgrade.HASTE, teamStatus));
		setIcon(6, 2, new UpgradeBuyableIcon<>(this, (upgradeLevel) -> new Price(ItemSettings.spawnRegeneration.priceType, (upgradeLevel + 1) * ItemSettings.spawnRegeneration.priceNumber), TeamUpgrade.SPAWN_REGEN, teamStatus));
		
		setIcon(2, 3, new TrapBuyableIcon(this, ItemSettings.blindTrap, Trap.BLIND_SLOW, teamStatus));
		setIcon(3, 3, new TrapBuyableIcon(this, ItemSettings.fatigueTrap, Trap.FATIGUE, teamStatus));
		setIcon(4, 3, new TrapBuyableIcon(this, ItemSettings.weaknessTrap, Trap.WEAKNESS, teamStatus));
		
		setIcon(8, 2, new SpecializationIcon(this, "Specializzazione", new Price(ResourceType.EMERALD, 1), teamStatus));
		
		refresh();
	}
	
	public ShopSubMenu getPvPMenu() {
		ShopSubMenu menu = new ShopSubMenu(this, SpecializationType.PVP.getName(), 3);
		menu.setIcon(2, 2, new ItemBuyableIcon(menu, "Spada di Diamante (Contraccolpo I)", ItemSettings.knockbackDiamondSword, ItemBuilder.of(Material.DIAMOND_SWORD).enchant(Enchantment.KNOCKBACK)));
		menu.setIcon(3, 2, new ItemBuyableIcon(menu, "Spada di Diamante (Aspetto di Fuoco I)", ItemSettings.fireDiamondSword, ItemBuilder.of(Material.DIAMOND_SWORD).enchant(Enchantment.FIRE_ASPECT)));
		menu.setIcon(4, 2, new ItemBuyableIcon(menu, "Canna da Pesca", ItemSettings.fishingRod, ItemBuilder.of(Material.FISHING_ROD)));
		menu.setIcon(5, 2, new ItemBuyableIcon(menu, "Pozione di Salute Aumentata I", ItemSettings.healthBoostPotion, Utils.makePotion(PotionEffectType.HEALTH_BOOST, 1, 120)));
		
		menu.refresh();
		return menu;
	}
	
	public ShopSubMenu getDefenseMenu(TeamStatus teamStatus) {
		ShopSubMenu menu = new ShopSubMenu(this, SpecializationType.DEFENSE.getName(), 3);
		menu.setIcon(2, 2, new ItemBuyableIcon(menu, "Sbarre di Ferro", ItemSettings.ironBar, ItemBuilder.of(Material.IRON_FENCE)));
		menu.setIcon(3, 2, new ItemBuyableIcon(menu, "Ragnatele", ItemSettings.web, ItemBuilder.of(Material.WEB)));
		menu.setIcon(4, 2, new ItemBuyableIcon(menu, "Sabbia delle Anime", ItemSettings.soulStone, ItemBuilder.of(Material.SOUL_SAND)));
		menu.setIcon(5, 2, new ItemBuyableIcon(menu, "Ghiaccio Compresso", ItemSettings.ice, ItemBuilder.of(Material.PACKED_ICE)));
		menu.setIcon(6, 2, new UpgradeBuyableIcon<>(menu, (upgradeLevel) -> new Price(ItemSettings.spawnResistance.priceType, (upgradeLevel + 1) * ItemSettings.spawnResistance.priceNumber), TeamUpgrade.SPAWN_RESISTANCE, teamStatus));
		
		menu.refresh();
		return menu;
	}
	
	public ShopSubMenu getDestructionMenu() {
		ShopSubMenu menu = new ShopSubMenu(this, SpecializationType.DESTRUCTION.getName(), 3);
		menu.setIcon(2, 2, new ItemBuyableIcon(menu, "Alleato Creeper", ItemSettings.creeper, new SpawnEgg(EntityType.CREEPER).toItemStack()));
		menu.setIcon(3, 2, new ItemBuyableIcon(menu, "TNT", ItemSettings.tnt, ItemBuilder.of(Material.TNT)));
		menu.setIcon(4, 2, new ItemBuyableIcon(menu, "Piccone di Diamante (Efficienza III)", ItemSettings.superDiamondPickaxe, ItemBuilder.of(Material.DIAMOND_PICKAXE).enchant(Enchantment.DIG_SPEED, 3)));
		menu.setIcon(5, 2, new ItemBuyableIcon(menu, "Ascia di Diamante (Efficienza III)", ItemSettings.superDiamondAxe, ItemBuilder.of(Material.DIAMOND_AXE).enchant(Enchantment.DIG_SPEED, 3)));
		
		menu.refresh();
		return menu;
	}

	public ShopSubMenu getSpecializationMenu(SpecializationType specialization) {
		switch (specialization) {
			case PVP: return pvpMenu;
			case DEFENSE: return defenseMenu;
			case DESTRUCTION: return destructionMenu;
			default: throw new IllegalStateException("Unhandled specialization: " + specialization);
		}
	}
}
