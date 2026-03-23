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
package com.gmail.filoghost.bedwars;

import java.io.File;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.command.ArenaCommand;
import com.gmail.filoghost.bedwars.command.BedwarsCommand;
import com.gmail.filoghost.bedwars.command.ClassificaCommand;
import com.gmail.filoghost.bedwars.command.GlobalCommand;
import com.gmail.filoghost.bedwars.command.PodiumCommand;
import com.gmail.filoghost.bedwars.command.SpawnCommand;
import com.gmail.filoghost.bedwars.command.StatsCommand;
import com.gmail.filoghost.bedwars.command.VotespecCommand;
import com.gmail.filoghost.bedwars.database.PlayerData;
import com.gmail.filoghost.bedwars.database.SQLManager;
import com.gmail.filoghost.bedwars.hud.LobbyScoreboard;
import com.gmail.filoghost.bedwars.listener.ChatListener;
import com.gmail.filoghost.bedwars.listener.ChunkUnloadListener;
import com.gmail.filoghost.bedwars.listener.DamageListener;
import com.gmail.filoghost.bedwars.listener.DeathListener;
import com.gmail.filoghost.bedwars.listener.InteractListener;
import com.gmail.filoghost.bedwars.listener.NatureListener;
import com.gmail.filoghost.bedwars.listener.PlayerJoinQuitListener;
import com.gmail.filoghost.bedwars.listener.PlayerListener;
import com.gmail.filoghost.bedwars.settings.ItemSettings;
import com.gmail.filoghost.bedwars.settings.MainSettings;
import com.gmail.filoghost.bedwars.settings.PodiumSettings;
import com.gmail.filoghost.bedwars.settings.objects.ArenaConfig;
import com.gmail.filoghost.bedwars.timer.MySQLKeepAliveTimer;
import com.gmail.filoghost.bedwars.timer.RankingUpdateTimer;
import com.gmail.filoghost.bedwars.timer.SpectatorLocationCheckTimer;
import com.gmail.filoghost.bedwars.timer.TickTimer;
import com.gmail.filoghost.bedwars.utils.Utils;
import com.google.common.collect.Maps;

import lombok.Getter;
import lombok.Setter;
import net.cubespace.yamler.YamlerConfigurationException;
import wild.api.WildCommons;
import wild.api.bridges.CosmeticsBridge;
import wild.api.command.CommandFramework.ExecuteException;
import wild.api.item.BookTutorial;
import wild.api.util.CaseInsensitiveMap;

public class Bedwars extends JavaPlugin {

	public static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.LIGHT_PURPLE + "Bed Wars" + ChatColor.GRAY + "] ";
	
	private static Bedwars plugin;
	@Getter private static TickTimer tickTimer;
	@Getter private static PodiumSettings podiumSettings;
	private static BookTutorial bookTutorial;
	@Getter @Setter private static Location spawn;
	private static File arenasFolder;
	@Getter private static LobbyScoreboard mainScoreboard;
	
	@Getter private static Map<String, Arena> arenasByName = new CaseInsensitiveMap<>();
	@Getter private static Map<Player, Arena> arenasByPlayers = Maps.newConcurrentMap();
	
	private static Map<String, PlayerData> statsByPlayerName = Collections.synchronizedMap(new CaseInsensitiveMap<>());

	public static Bedwars get() {
		return plugin;
	}
	
	@Override
	public void onEnable() {
		if (!checkDependancies()) {
			return;
		}
		
		bookTutorial = new BookTutorial(this, "Bed Wars");
		
		// Impostazioni
		try {
			new MainSettings(this, "config.yml").init();
			if (MainSettings.spawn != null) {
				spawn = MainSettings.spawn.getLocation();
			} else {
				spawn = Bukkit.getWorlds().get(0).getSpawnLocation();
			}
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			criticalShutdown("Impossibile caricare config.yml");
			return;
		}
		
		// Costi
		try {
			new ItemSettings(this, "prices.yml").init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			criticalShutdown("Impossibile caricare prices.yml");
			return;
		}
		
		// Classifica
		try {
			(podiumSettings = new PodiumSettings(this, "podium.yml")).init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			criticalShutdown("Impossibile caricare podium.yml");
			return;
		}
		
		// Database
		try {
			SQLManager.connect(MainSettings.mysql_host, MainSettings.mysql_port, MainSettings.mysql_database, MainSettings.mysql_user, MainSettings.mysql_pass);
			SQLManager.createTables();
		} catch (Exception e) {
			e.printStackTrace();
			criticalShutdown("Impossibile connettersi al database");
			return;
		}
		
		// Inizializzazione
		plugin = this;
		arenasFolder = new File(getDataFolder(), "arenas");
		arenasFolder.mkdirs();
		mainScoreboard = new LobbyScoreboard();
		
		// Listener
		Bukkit.getPluginManager().registerEvents(new NatureListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
		Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(), this);
		Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
		Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
		Bukkit.getPluginManager().registerEvents(new InteractListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChunkUnloadListener(), this);
		
		// Comandi
		new BedwarsCommand(this, "bedwars", "bw");
		new ArenaCommand(this, "arena");
		new SpawnCommand(this, "spawn");
		new VotespecCommand(this, "votespec");
		new ClassificaCommand(this, "classifica");
		new StatsCommand(this, "stats");
		new PodiumCommand(this, "podium");
		new GlobalCommand(this, "g");
		
		// Carica le arene salvate
		for (File arenaFile : arenasFolder.listFiles()) {
			try {
				if (arenaFile.getName().toLowerCase().endsWith(".yml")) {
					ArenaConfig arenaConfig = loadArenaConfig(arenaFile);
					addArena(new Arena(arenaConfig));
				}
			} catch (ExecuteException e) {
				criticalShutdown("Non è stato possibile caricare l'arena " + arenaFile.getName() + ": " + e.getMessage());
				return;
			} catch (Exception e) {
				e.printStackTrace();
				criticalShutdown("Eccezione non gestita durante il caricamento dell'arena.");
				return;
			}
		}
		
		// Timer
		(tickTimer = new TickTimer()).start();
		new MySQLKeepAliveTimer().start();
		new RankingUpdateTimer().start();
		new SpectatorLocationCheckTimer().start();
	}
	
	@Override
	public void onDisable() {
		for (Arena arena : getAllArenas()) {
			arena.getEvents().getPlacedBlocksRegistry().restore();
		}
		for (Entry<String, PlayerData> entry : statsByPlayerName.entrySet()) {
			if (entry.getValue().isNeedSave()) {
				try {
					SQLManager.savePlayerData(entry.getKey(), entry.getValue());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void setupToLobby(Player player) {
		if (getArenaByPlayer(player) != null) {
			throw new IllegalStateException("Player is still playing");
		}
		
		player.resetMaxHealth();
		player.setHealth(player.getMaxHealth());
		player.teleport(spawn);
		giveLobbyEquip(player);
		CosmeticsBridge.updateCosmetics(player, CosmeticsBridge.Status.LOBBY);
		
		PlayerData stats = Bedwars.getPlayerData(player);
		LobbyScoreboard mainScoreboard = Bedwars.getMainScoreboard();
		player.setScoreboard(mainScoreboard.getScoreboard());
		mainScoreboard.setWins(player, stats.getWins());
		mainScoreboard.setKills(player, stats.getKills());
		mainScoreboard.setFinalKills(player, stats.getFinalKills());
		mainScoreboard.setDeaths(player, stats.getDeaths());
	}
	
	public static void giveLobbyEquip(Player player) {
		player.setGameMode(GameMode.SURVIVAL);
		WildCommons.clearInventoryFully(player);
		WildCommons.removePotionEffects(player);
		
		PlayerInventory inventory = player.getInventory();
		bookTutorial.giveTo(inventory);
		CosmeticsBridge.giveCosmeticsItems(inventory);
	}
	
	public static Collection<Arena> getAllArenas() {
		return arenasByName.values();
	}
	
	public static Arena getArenaByName(String name) {
		return arenasByName.get(name);
	}
	
	public static Arena getArenaByPlayer(Player player) {
		return arenasByPlayers.get(player);
	}
	
	public static PlayerData getPlayerData(Player player) {
		PlayerData stats = statsByPlayerName.get(player.getName());
		if (stats == null) {
			Utils.reportAnomaly("stats were not loaded", player);
			stats = new PlayerData(0, 0, 0, 0, 0); // Stats di default per evitare errori, alla peggio non vengono segnate
		}
		
		return stats;
	}
	
	public static void resetOnlinePlayersData() {
		for (PlayerData stats : statsByPlayerName.values()) {
			stats.resetToZero();
		}
	}
	
	public static void loadStatsFromDatabase(String playerName) throws SQLException {
		statsByPlayerName.put(playerName, SQLManager.getStats(playerName));
	}
	
	public static void unloadStatsToDatabase(String playerName) throws SQLException {
		PlayerData stats = statsByPlayerName.remove(playerName);
		if (stats == null) {
			throw new IllegalStateException(playerName + "'s stats were not loaded");
		}
		
		if (stats.isNeedSave()) {
			SQLManager.savePlayerData(playerName, stats);
		}
	}
	
	public static ArenaConfig loadArenaConfig(String name) throws YamlerConfigurationException {
		return loadArenaConfig(new File(arenasFolder, name.toLowerCase() + ".yml"));
	}

	public static ArenaConfig loadArenaConfig(File arenaFile) throws YamlerConfigurationException {
		if (!arenaFile.exists()) {
			return null;
		}
		
		String fileName = arenaFile.getName();
		String arenaName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
		
		ArenaConfig arenaConfig = new ArenaConfig();
		arenaConfig.load(arenaFile);
		if (!arenaConfig.name.equalsIgnoreCase(arenaName)) {
			throw new YamlerConfigurationException("name mismatch: " + arenaConfig.name + " vs " + arenaName);
		}
		return arenaConfig;
	}
	
	public static void saveArenaConfig(ArenaConfig arenaConfig) throws YamlerConfigurationException {
		File arenaFile = new File(arenasFolder, arenaConfig.name.toLowerCase() + ".yml");
		arenaConfig.save(arenaFile);
	}
	
	public static void addArena(Arena arena) {
		if (getArenaByName(arena.getName()) != null) {
			throw new IllegalArgumentException("arena " + arena.getName() + " already loaded");
		}
		
		arenasByName.put(arena.getName(), arena);
	}
	
	
	
	private boolean checkDependancies() {
		return checkDependancy("WildCommons") && checkDependancy("HolographicDisplays") && checkDependancy("HolographicMobs");
	}
	
	private boolean checkDependancy(String pluginName) {
		if (Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
			return true;
		} else {
			criticalShutdown("Richiesto " + pluginName);
			return false;
		}
	}
	
	private void criticalShutdown(String message) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] " + message);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) { }
		setEnabled(false);
		Bukkit.shutdown();
	}
	
}
