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
package com.gmail.filoghost.bedwars.hud.spectator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import wild.api.WildCommons;
import wild.api.bridges.PexBridge;
import wild.api.bridges.PexBridge.PrefixSuffix;
import wild.api.menu.Icon;
import wild.api.menu.IconBuilder;
import wild.api.menu.IconMenu;
import wild.api.menu.StaticIcon;
import wild.api.sound.EasySound;
import wild.api.sound.SoundEnum;

import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.PlayerStatus;
import com.gmail.filoghost.bedwars.utils.Utils;
import com.google.common.collect.Lists;

public class TeleporterMenu {
	
	private static final Sound PAGE_CHANGE_SOUND = SoundEnum.get("CLICK");
	private static final int SPECTATOR_ROWS = 5;
	private static final int SPECTATOR_SLOTS = SPECTATOR_ROWS * 9;
	private static final int TOTAL_ROWS = SPECTATOR_ROWS + 1;
	
	private final Arena arena;
	private List<IconMenu> teleporterMenus;
	
	public TeleporterMenu(Arena arena) {
		this.arena = arena;
		this.teleporterMenus = Lists.newArrayList();
	}

	
	public void update() {
		Collection<PlayerStatus> gamerStatuses = Lists.newArrayList();
		
		for (PlayerStatus playerStatus : arena.getPlayerStatuses()) {
			if (playerStatus.getTeam() != null && !playerStatus.isSpectator()) {
				gamerStatuses.add(playerStatus);
			}
		}
		
		// Approssima per eccesso, minimo 1 pagina
		int pages = gamerStatuses.size() % SPECTATOR_SLOTS == 0 ? (gamerStatuses.size() / SPECTATOR_SLOTS) : (gamerStatuses.size() / SPECTATOR_SLOTS) + 1;
		if (pages < 1) {
			pages = 1;
		}
		
		// Aggiunge o toglie pagine in base al numero giusto
		while (teleporterMenus.size() < pages) {
			teleporterMenus.add(new IconMenu("Teletrasporto rapido", TOTAL_ROWS));
		}
		while (teleporterMenus.size() > pages) {
			teleporterMenus.remove(teleporterMenus.size() - 1);
		}
		
		// Pulisce tutti i menu, per riutilizzarli
		for (IconMenu menu : teleporterMenus) {
			menu.clearIcons();
		}
		
		int index = 0;
		for (PlayerStatus gamerStatus : gamerStatuses) {
			Player gamer = gamerStatus.getPlayer();
			String gamerName = gamer.getName();
			int page = index / SPECTATOR_SLOTS;
			int slot = index % SPECTATOR_SLOTS;
			
			ItemStack headItem = Utils.getHeadItem(gamerStatus.getPlayer());
			ItemMeta headItemMeta = headItem.getItemMeta();
			PrefixSuffix prefixSuffix = PexBridge.getCachedPrefixSuffix(gamer);
			headItemMeta.setDisplayName(ChatColor.WHITE + WildCommons.color(prefixSuffix.getPrefix() + gamer.getName() + prefixSuffix.getSuffix()));
			headItemMeta.setLore(Arrays.asList(gamerStatus.getTeam().getChatColor() + "Team " + gamerStatus.getTeam().getNameSingular()));
			headItem.setItemMeta(headItemMeta);
			
			Icon icon = new StaticIcon(headItem);
			icon.setClickHandler(clicker -> {
				Player target = Bukkit.getPlayerExact(gamerName);
				
				PlayerStatus clickerStatus = arena.getPlayerStatus(clicker);
				if (clickerStatus == null || !clickerStatus.isSpectator()) {
					clicker.sendMessage(ChatColor.RED + "Puoi teletrasportarti solo da spettatore.");
					return;
				}
				
				if (target != null) {
					PlayerStatus targetStatus = arena.getPlayerStatus(target);
					
					if (targetStatus != null && targetStatus.getTeam() != null && !targetStatus.isSpectator()) {
						clicker.teleport(target);
						clicker.sendMessage(ChatColor.GRAY + "Teletrasportato da " + gamerName);
						return;
					}
				}
				
				clicker.sendMessage(ChatColor.RED + "Quel giocatore non è più online o è uno spettatore.");
			});
			teleporterMenus.get(page).setIconRaw(slot, icon);
			
			index++;
		}
		
		
		if (pages > 1) {
			for (int page = 0; page < teleporterMenus.size(); page++) {
				IconMenu currentPageMenu = teleporterMenus.get(page);
				
				if (page > 0) {
					// Se non è il primo, visualizza la pagina precedente
					IconMenu previousPageMenu = teleporterMenus.get(page - 1);
					
					currentPageMenu.setIcon(4, TOTAL_ROWS, new IconBuilder(Material.ARROW).name(ChatColor.WHITE + "Pagina precedente").clickHandler(player -> {
						EasySound.quickPlay(player, PAGE_CHANGE_SOUND, 1.6f, 0.5f);
						previousPageMenu.open(player);
					}).build());
				}
				
				if (page < teleporterMenus.size() - 1) {
					// Se non è l'ultimo, visualizza la pagina successiva
					IconMenu nextPageMenu = teleporterMenus.get(page + 1);
					
					currentPageMenu.setIcon(6, TOTAL_ROWS, new IconBuilder(Material.ARROW).name(ChatColor.WHITE + "Pagina successiva").clickHandler(player -> {
						EasySound.quickPlay(player, PAGE_CHANGE_SOUND, 1.6f, 0.5f);
						nextPageMenu.open(player);
					}).build());
				}
				
				currentPageMenu.setIcon(5, TOTAL_ROWS, new IconBuilder(Material.PAPER).name(ChatColor.WHITE + "Pagina " + (page + 1)).build());
			}
		}

		
		// Refresh di tutti i menu
		for (IconMenu menu : teleporterMenus) {
			menu.refresh();
		}
	}

	public void open(Player player) {
		if (!teleporterMenus.isEmpty()) {
			teleporterMenus.get(0).open(player);
		}
	}
}
