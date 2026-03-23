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

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.gmail.filoghost.bedwars.hud.shop.PriceCalculator.FixedPriceCalculator;
import com.gmail.filoghost.bedwars.settings.objects.ItemConfig;

import wild.api.item.ItemBuilder;
import wild.api.menu.ClickHandler;
import wild.api.sound.EasySound;

public class ItemBuyableIcon extends BuyableIcon implements ClickHandler {
	
	private ItemStack reward;
	private ItemStack display;
	
	public ItemBuyableIcon(ShopMenu parentMenu, String name, ItemConfig itemConfig, ItemBuilder reward) {
		this(parentMenu, name, itemConfig, reward.build());
	}
	
	public ItemBuyableIcon(ShopMenu parentMenu, String name, ItemConfig itemConfig, ItemStack reward) {
		super(parentMenu, name, new FixedPriceCalculator(new Price(itemConfig.priceType, itemConfig.priceNumber)));
		reward.setAmount(itemConfig.amount);
		this.reward = reward;
		this.display = reward.clone();
		
		ItemMeta displayMeta = display.getItemMeta();
		displayMeta.setDisplayName(ChatColor.GREEN + name + (reward.getAmount() != 1 ? " x" + reward.getAmount() : ""));
		Price price = priceCalculator.getPrice(0);
		displayMeta.setLore(Arrays.asList("", formatPrice(price)));
		display.setItemMeta(displayMeta);
		
		setClickHandler(this);
	}
	
	@Override
	public ItemStack createItemstack() {
		return display;
	}
	
	@Override
	public void onClick(Player player) {
		if (parentMenu.getArena().getShopManager().tryBuyFromShop(player, priceCalculator, reward.clone()).isSuccess()) {
			EasySound.quickPlay(player, Sound.ORB_PICKUP);
		} else {
			EasySound.quickPlay(player, Sound.NOTE_BASS);
		}
	}
	
}
