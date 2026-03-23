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

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.gmail.filoghost.bedwars.arena.TeamStatus;
import com.gmail.filoghost.bedwars.arena.shop.Trap;
import com.gmail.filoghost.bedwars.hud.shop.PriceCalculator.FixedPriceCalculator;
import com.gmail.filoghost.bedwars.settings.objects.ItemConfig;
import com.google.common.collect.Lists;

import lombok.Getter;
import wild.api.menu.ClickHandler;
import wild.api.sound.EasySound;

public class TrapBuyableIcon extends BuyableIcon implements ClickHandler {
	
	@Getter private Trap trap;
	private TeamStatus teamStatus;
	
	public TrapBuyableIcon(ShopMenu parentMenu, ItemConfig itemConfig, Trap trap, TeamStatus teamStatus) {
		super(parentMenu, trap.getName(), new FixedPriceCalculator(new Price(itemConfig.priceType, itemConfig.priceNumber)));
		this.trap = trap;
		this.teamStatus = teamStatus;
		setMaterial(trap.getIcon());
		setClickHandler(this);
		updateIcon();
	}

	public void updateIcon() {
		boolean active = teamStatus.isTrapActive(trap);
		String name = (active ? ChatColor.RED : ChatColor.GREEN) + trap.getName();

		setName(name);
		List<String> lore = Lists.newArrayList();
		if (active) {
			lore.add("Attiva");
		} else {
			lore.add("Non attiva");
			lore.add("");
			lore.add(formatPrice(priceCalculator.getPrice(-1)));
		}
		
		setLore(lore);
	}
	
	@Override
	public void onClick(Player player) {
		boolean success = parentMenu.getArena().getShopManager().tryBuyFromShop(player, priceCalculator, trap);
		if (success) {
			updateIcon();
			parentMenu.refresh(this);
			EasySound.quickPlay(player, Sound.LEVEL_UP);
		} else {
			EasySound.quickPlay(player, Sound.NOTE_BASS);
		}
	}
	
}
