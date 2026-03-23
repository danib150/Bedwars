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
package com.gmail.filoghost.bedwars.arena.shop;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.arena.ArenaStatus;
import com.gmail.filoghost.bedwars.arena.PlayerStatus;
import com.gmail.filoghost.bedwars.arena.Team;
import com.gmail.filoghost.bedwars.arena.TeamStatus;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.PlayerUpgrade;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.TeamUpgrade;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.UpgradableStatus;
import com.gmail.filoghost.bedwars.arena.shop.upgrade.Upgrade;
import com.gmail.filoghost.bedwars.hud.shop.Price;
import com.gmail.filoghost.bedwars.hud.shop.PriceCalculator;
import com.gmail.filoghost.bedwars.utils.Utils;

import lombok.RequiredArgsConstructor;
import wild.api.menu.IconMenu;
import wild.api.util.UnitFormatter;

@RequiredArgsConstructor
public class ShopManager {
	
	private final Arena arena;
	
	
	public void tryOpenShop(Player player, ShopGetter shopGetter) {
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		if (playerStatus == null || playerStatus.isSpectator() || playerStatus.getTeam() == null) {
			player.sendMessage(ChatColor.RED + "Non puoi aprire il negozio ora.");
			return;
		}
		
		shopGetter.getShop(arena.getTeamStatus(playerStatus.getTeam())).open(player);
	}
	
	
	public interface ShopGetter {
		public IconMenu getShop(TeamStatus teamStatus);
	}
	
	
	private boolean canBuyFromShop(Player player, PlayerStatus playerStatus) {
		if (playerStatus == null) {
			Utils.reportAnomaly("trying to buy from shop on external player", this, player);
			player.sendMessage(ChatColor.RED + "Non sei all'interno della partita.");
			return false;
		}
		
		if (arena.getArenaStatus() != ArenaStatus.COMBAT) {
			player.sendMessage(ChatColor.RED + "Non puoi fare acquisti ora.");
			return false;
		}
		
		if (playerStatus.isSpectator()) {
			Utils.reportAnomaly("trying to buy from shop while spectator", this, player);
			player.sendMessage(ChatColor.RED + "Non puoi fare acquisti da spettatore.");
			return false;
		}
		
		if (playerStatus.getTeam() == null) {
			Utils.reportAnomaly("trying to buy from shop with no team", this, player);
			player.sendMessage(ChatColor.RED + "Non puoi fare acquisti senza un team.");
			return false;
		}
		
		return true;
	}
		
	
	private boolean collectPrice(Player player, Price price) {
		Inventory inventory = player.getInventory();
		ItemStack priceItem = price.getItemStack();
		
		if (!inventory.containsAtLeast(priceItem, price.getAmount())) {
			player.sendMessage(ChatColor.RED + "Non hai gli oggetti richiesti.");
			return false;
		}
		
		inventory.removeItem(priceItem);
		return true;
	}
	
	
	public TransactionResult<PlayerUpgrade> tryBuyFromShop(Player player, PriceCalculator priceCalculator, ItemStack reward) {
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		
		if (canBuyFromShop(player, playerStatus)) {
			if (!Utils.canFullyFitItem(player.getInventory(), reward)) {
				player.sendMessage(ChatColor.RED + "Il tuo inventario è pieno.");
				return TransactionResult.negative();
			}
			
			if (collectPrice(player, priceCalculator.getPrice(-1))) {
				if (Utils.isSword(reward.getType())) {
					arena.updateSword(reward, arena.getTeamStatus(playerStatus.getTeam()));
					// TODO: sostituire la posizione della spada vecchia e mettere la vecchia nell'inventario
				}
				
				player.getInventory().addItem(reward);
				return TransactionResult.positive(playerStatus);
			}
		}
		
		return TransactionResult.negative();
	}
	
	
	public boolean tryBuyFromShop(Player player, PriceCalculator priceCalculator, Trap trap) {
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		if (!canBuyFromShop(player, playerStatus)) {
			return false;
		}
		
		TeamStatus teamStatus = arena.getTeamStatus(playerStatus.getTeam());
		
		if (teamStatus.isTrapActive(trap)) {
			player.sendMessage(ChatColor.RED + "La trappola è già attiva.");
			return false;
		}
		
		if (!collectPrice(player, priceCalculator.getPrice(-1))) {
			return false;
		}
		
		teamStatus.setTrapActive(trap, true);
		arena.broadcastTeam(playerStatus.getTeam(), playerStatus.getTeam().getChatColor() + player.getName() + ChatColor.GRAY + " ha attivato " + ChatColor.WHITE + trap.getName());
		return true;
	}


	@SuppressWarnings("unchecked")
	public <U extends Upgrade> TransactionResult<U> tryBuyFromShop(Player player, PriceCalculator priceCalculator, U reward) {
		PlayerStatus playerStatus = arena.getPlayerStatus(player);
		if (!canBuyFromShop(player, playerStatus)) {
			return TransactionResult.negative();
		}
		
		UpgradableStatus<U> status;
		
		if (reward instanceof TeamUpgrade) {
			Team team = playerStatus.getTeam();
			if (team == null) {
				player.sendMessage(ChatColor.RED + "Non sei in nessun team.");
				return TransactionResult.negative();
			}
			status = (UpgradableStatus<U>) arena.getTeamStatus(team);
		} else if (reward instanceof PlayerUpgrade) {
			status = (UpgradableStatus<U>) playerStatus;
		} else {
			throw new IllegalArgumentException("Unknown upgrade type: " + reward.getClass().getName());
		}
		
		return tryLevelUp(player, priceCalculator, status, reward);
	}
	
	
	@SuppressWarnings("unchecked")
	private <U extends Upgrade> TransactionResult<U> tryLevelUp(Player player, PriceCalculator priceCalculator, UpgradableStatus<U> status, U upgrade) {
		if (!status.canLevelupUpgrade(upgrade)) {
			if (upgrade.isOneTime()) {
				player.sendMessage(ChatColor.RED + "Questo potenziamento è già sbloccato.");
			} else {
				player.sendMessage(ChatColor.RED + "Questo potenziamento è già al massimo.");
			}
			return TransactionResult.negative();
		}
		
		if (collectPrice(player, priceCalculator.getPrice(status.getUpgradeLevel(upgrade)))) {
			int newLevel = status.levelupUpgrade(upgrade);
			String upgradeName = upgrade.getName() + (upgrade.isOneTime() ? "" : " " + UnitFormatter.getRoman(newLevel));
			
			if (status instanceof TeamStatus) {
				TeamStatus teamStatus = (TeamStatus) status;
				Team team = teamStatus.getTeam();
				arena.applyUpgrade(teamStatus, upgrade);
				arena.broadcastTeam(team, team.getChatColor() + player.getName() + ChatColor.GRAY + " ha sbloccato " + ChatColor.WHITE + upgradeName);
			} else {
				PlayerStatus playerStatus = (PlayerStatus) status;
				((PlayerUpgrade) upgrade).getUnlocks();
				arena.applyUpgrade(playerStatus, arena.getTeamStatus(playerStatus.getTeam()), upgrade);
				player.sendMessage(Bedwars.PREFIX + ChatColor.GRAY + "Hai sbloccato " + ChatColor.WHITE + upgradeName);
			}
			
			if (upgrade.getUnlocks() != null) {
				for (Upgrade unlocked : upgrade.getUnlocks()) {
					if (status.canLevelupUpgrade((U) unlocked)) {
						status.levelupUpgrade((U) unlocked);
					}
				}
			}
			
			return TransactionResult.positive(status);
		} else {
			return TransactionResult.negative();
		}
	}

}
