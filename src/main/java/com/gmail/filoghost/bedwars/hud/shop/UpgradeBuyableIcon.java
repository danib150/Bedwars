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
package com.gmail.filoghost.bedwars.hud.shop;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.gmail.filoghost.bedwars.arena.shop.TransactionResult;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.UpgradableStatus;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.Upgrade;
import com.google.common.collect.Lists;

import wild.api.menu.ClickHandler;
import wild.api.menu.Icon;
import wild.api.sound.EasySound;
import wild.api.util.UnitFormatter;

public class UpgradeBuyableIcon<U extends Upgrade> extends BuyableIcon implements ClickHandler {
	
	private U upgrade;
	
	public UpgradeBuyableIcon(ShopMenu parentMenu, PriceCalculator priceCalculator, U upgrade, UpgradableStatus<U> status) {
		super(parentMenu, upgrade.getName(), priceCalculator);
		this.upgrade = upgrade;
		setMaterial(upgrade.getIcon());
		setClickHandler(this);
		updateIcon(status);
	}

	public void updateIcon(UpgradableStatus<U> status) {
		int level = status.getUpgradeLevel(upgrade);
		String name;

		if (upgrade.isOneTime()) {
			if (level > 0) {
				name = ChatColor.RED + upgrade.getName();
			} else {
				name = ChatColor.GREEN + upgrade.getName();
			}
		} else {
			if (level >= upgrade.getMaxLevel()) {
				name = ChatColor.RED + upgrade.getName() + " " + UnitFormatter.getRoman(level);
			} else {
				name = ChatColor.GREEN + upgrade.getName() + " " + UnitFormatter.getRoman(level + 1);
			}
		}
		setMaterial(upgrade.getIcon(level));
		setName(name);
		List<String> lore = Lists.newArrayList();
		if (level < upgrade.getMaxLevel()) {
			lore.add("Potenzia");
			lore.add("");
			Price price = priceCalculator.getPrice(level);
			lore.add(formatPrice(price));
		} else {
			lore.add("Potenziato al massimo");
		}
		
		setLore(lore);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onClick(Player player) {
		TransactionResult<U> result = parentMenu.getArena().getShopManager().tryBuyFromShop(player, priceCalculator, upgrade);
		if (result.isSuccess()) {
			
			updateIcon(result.getStatus());
			Upgrade[] thisUnlocks = upgrade.getUnlocks();
			
			if (thisUnlocks != null) {
				for (int i = 0; i < parentMenu.getSize(); i++) {
					Icon icon = parentMenu.getIconAt(i);
					if (icon != null && icon instanceof UpgradeBuyableIcon) {
						UpgradeBuyableIcon<U> otherBuyableIcon = (UpgradeBuyableIcon<U>) icon;
						if (ArrayUtils.contains(thisUnlocks, otherBuyableIcon.upgrade)) {
							otherBuyableIcon.updateIcon(result.getStatus());
							parentMenu.refresh(otherBuyableIcon);
						}
					}
				}
			}
			parentMenu.refresh(this);
			EasySound.quickPlay(player, Sound.LEVEL_UP);
		} else {
			EasySound.quickPlay(player, Sound.NOTE_BASS);
		}
	}
	
}
