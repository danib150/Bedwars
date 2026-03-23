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
package com.gmail.filoghost.bedwars.command;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Bed;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.Perms;
import com.gmail.filoghost.bedwars.arena.Team;
import com.gmail.filoghost.bedwars.arena.spawners.ResourceType;
import com.gmail.filoghost.bedwars.settings.objects.ArenaConfig;
import com.gmail.filoghost.bedwars.settings.objects.BlockConfig;
import com.gmail.filoghost.bedwars.settings.objects.LocationConfig;
import com.gmail.filoghost.bedwars.settings.objects.SpawnerConfig;
import com.gmail.filoghost.bedwars.settings.objects.TeamConfig;
import com.gmail.filoghost.bedwars.utils.Utils;
import com.google.common.collect.Lists;

import lombok.val;
import net.cubespace.yamler.YamlerConfigurationException;
import net.md_5.bungee.api.ChatColor;
import wild.api.command.CommandFramework.Permission;
import wild.api.command.SubCommandFramework;
import wild.api.translation.Translation;
import wild.api.util.CaseInsensitiveMap;

@Permission(Perms.COMMAND_ARENA)
public class ArenaCommand extends SubCommandFramework {
	
	private Map<String, ArenaConfig> playerSetups = new CaseInsensitiveMap<>();
	
	public ArenaCommand(JavaPlugin plugin, String label) {
		super(plugin, label);
	}

	private ArenaConfig getCurrentSetup(Player player) {
		return playerSetups.get(player.getName());
	}
	
	private void setCurrentSetup(Player player, ArenaConfig config) {
		playerSetups.put(player.getName(), config);
	}
	
	private ArenaConfig getCurrentSetupNotNull(Player player) {
		ArenaConfig setup = playerSetups.get(player.getName());
		CommandValidate.notNull(setup, "Non hai nessuna arena caricata.");
		return setup;
	}
	
	private TeamConfig getTeamConfigNotNull(Player player, Team team) {
		TeamConfig teamConfig = getCurrentSetupNotNull(player).teamConfigs.get(team.name());
		CommandValidate.notNull(teamConfig, "Non hai ancora aggiunto il team " + team.getNameSingular() + " alla mappa. Usa /" + this.label + " addteam");
		return teamConfig;
	}
	
	
	
	@Override
	public void noArgs(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_GREEN + "Lista comandi " + this.label + ":");
		for (SubCommandDetails sub : this.getAccessibleSubCommands(sender)) {
			sender.sendMessage(ChatColor.GREEN + "/" + this.label + " " + sub.getName() + (sub.getUsage() != null ?  " " + sub.getUsage() : ""));
		}
	}
	

	@SubCommand("info")
	@SubCommandUsage("[-v] [-r]")
	public void info(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		player.sendMessage(ChatColor.WHITE + "Arena in fase di modifica: " + ChatColor.GRAY + currentSetup.name);
		player.sendMessage(ChatColor.WHITE + "Giocatori massimi per team: " + ChatColor.GRAY + currentSetup.maxPlayersPerTeam);
		player.sendMessage(ChatColor.WHITE + "Raggio di protezione spawn: " + ChatColor.GRAY + currentSetup.spawnProtectionRadius);
		player.sendMessage(ChatColor.WHITE + "Raggio di protezione villager: " + ChatColor.GRAY + currentSetup.villagerProtectionRadius);
		player.sendMessage(ChatColor.WHITE + "Raggio di protezione generatori: " + ChatColor.GRAY + currentSetup.generatorsProtectionRadius);
		player.sendMessage(ChatColor.WHITE + "Raggio di protezione boss: " + ChatColor.GRAY + currentSetup.bossProtectionRadius);
		
		player.sendMessage(ChatColor.WHITE + "Lobby: " + ChatColor.GRAY + format(currentSetup.lobby));
		player.sendMessage(ChatColor.WHITE + "Spawn spettatori: " + ChatColor.GRAY + format(currentSetup.spectatorSpawn));
		player.sendMessage(ChatColor.WHITE + "Cartello: " + ChatColor.GRAY + format(currentSetup.sign));
		
		player.sendMessage(ChatColor.WHITE + "Boss: " + ChatColor.GRAY + format(currentSetup.bossLocation));
		
		player.sendMessage(ChatColor.WHITE + "Spawners (" + currentSetup.spawners.size() + "): ");
		for (SpawnerConfig spawnerConfig : currentSetup.spawners) {
			player.sendMessage(ChatColor.WHITE + " - " + ChatColor.GRAY + spawnerConfig.type.name() + "(" + format(spawnerConfig.block) + ")");
		}
		
		for (val entry : currentSetup.teamConfigs.entrySet()) {
			Team team = Team.valueOf(entry.getKey());
			TeamConfig teamConfig = entry.getValue();
			
			player.sendMessage(ChatColor.WHITE + "=-----= Team " + team.getChatColor() + team.name() + ChatColor.WHITE + " =-----=");
			player.sendMessage(team.getChatColor() + "Spawn: " + ChatColor.GRAY + format(teamConfig.spawnLocation));
			player.sendMessage(team.getChatColor() + "Letto: " + ChatColor.GRAY + format(teamConfig.bedHeadLocation));
			player.sendMessage(team.getChatColor() + "Villager (oggetti): " + ChatColor.GRAY + format(teamConfig.itemVillagerLocation));
			player.sendMessage(team.getChatColor() + "Villager (team): " + ChatColor.GRAY + format(teamConfig.teamVillagerLocation));
		}
		
		if (Arrays.asList(args).contains("-v")) {
			boolean replace = Arrays.asList(args).contains("-r");
			displayArenaVolume(player, currentSetup, replace);
		} else {
			player.sendMessage(ChatColor.GRAY + "Usa \"-v\" e \"-r\" per visualizzare i volumi dell'arena");
		}
	}
	
	private static DecimalFormat decimalFormat = new DecimalFormat("0.00");
	private String format(LocationConfig loc) {
		if (loc == null) return "-";
		return decimalFormat.format(loc.x) + ", " + decimalFormat.format(loc.y) + ", " + decimalFormat.format(loc.z);
	}
	private String format(BlockConfig block) {
		if (block == null) return "-";
		return block.x + ", " + block.y + ", " + block.z;
	}
	
	
	@SubCommand("new")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void _new(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		String name = args[0];
		
		CommandValidate.isTrue(getCurrentSetup(player) == null, "Stai già modificando un'arena. Fai prima /" + this.label + " unload o /" + this.label + " save");
		CommandValidate.isTrue(Bedwars.getArenaByName(name) == null, "Esiste già un'arena con quel nome.");
		
		ArenaConfig currentSetup = new ArenaConfig();
		setCurrentSetup(player, currentSetup);
		currentSetup.name = name;
		
		player.sendMessage(ChatColor.GREEN + "Stai creando l'arena " + name + ".");
	}
	
	
	@SubCommand("load")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void load(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		String name = args[0];
		CommandValidate.isTrue(getCurrentSetup(player) == null, "Stai già modificando un'arena. Fai prima /" + this.label + " unload o /" + this.label + " save");
		
		ArenaConfig config;
		
		try {
			config = Bedwars.loadArenaConfig(name);
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile caricare il file dell'arena " + name + ".");
		}
		
		CommandValidate.notNull(config, "Arena non trovata.");
		setCurrentSetup(player, config);
		
		player.sendMessage(ChatColor.GREEN + "Hai caricato l'arena " + name + ".");
	}
	
	
	@SubCommand("unload")
	public void unload(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		getCurrentSetupNotNull(player);
		
		setCurrentSetup(player, null);
		player.sendMessage(ChatColor.GREEN + "Hai scaricato l'arena, le eventuali modifiche non salvate sono state ignorate.");
	}
	
	
	@SubCommand("save")
	public void save(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		CommandValidateExtra.checkArenaConfig(currentSetup);
		
		try {
			Bedwars.saveArenaConfig(currentSetup);
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile salvare il file dell'arena " + currentSetup.name + ".");
		}
		
		player.sendMessage(ChatColor.GREEN + "Hai salvato le modifiche all'arena " + currentSetup.name + ".");
	}
	
	@SubCommand("playersPerTeam")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<numero>")
	public void playersPerTeam(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		currentSetup.maxPlayersPerTeam = CommandValidate.getPositiveIntegerNotZero(args[0]);
		player.sendMessage(ChatColor.GREEN + "Hai impostato il numero di giocatori per team.");
	}
	
	@SubCommand("protection")
	@SubCommandMinArgs(2)
	@SubCommandUsage("<spawn|generator|villager|boss> <raggio>")
	public void spawnProtection(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		int radius = CommandValidate.getPositiveIntegerNotZero(args[1]);
		String location = args[0].toLowerCase();
		String aroundWhat;
		
		if (location.equals("spawn")) {
			currentSetup.spawnProtectionRadius = radius;
			aroundWhat = "agli spawn";
		} else if (location.equals("generator")) {
			currentSetup.generatorsProtectionRadius = radius;
			aroundWhat = "ai generatori";
		} else if (location.equals("villager")) {
			currentSetup.villagerProtectionRadius = radius;
			aroundWhat = "ai villager";
		} else if (location.equals("boss")) {
			currentSetup.bossProtectionRadius = radius;
			aroundWhat = "al boss";
		} else {
			throw new ExecuteException("Locazione non valida, deve essere una di queste: spawn, generator, villager, boss.");
		}
		
		player.sendMessage(ChatColor.GREEN + "Hai impostato il raggio di protezione intorno " + aroundWhat + ".");
	}
	
	@SubCommand("loc1")
	public void loc1(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		Location loc1 = player.getLocation();
		currentSetup.corner1 = new BlockConfig(loc1.getWorld().getName(), loc1.getBlockX(), loc1.getBlockY(), loc1.getBlockZ());
		player.sendMessage(ChatColor.GREEN + "Hai impostato la posizione 1.");
	}

	@SubCommand("loc2")
	public void loc2(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		Location loc2 = player.getLocation();
		currentSetup.corner2 = new BlockConfig(loc2.getWorld().getName(), loc2.getBlockX(), loc2.getBlockY(), loc2.getBlockZ());
		player.sendMessage(ChatColor.GREEN + "Hai impostato la posizione 2.");
	}
	
	
	@SubCommand("lobby")
	public void lobby(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		currentSetup.lobby = new LocationConfig(Utils.roundedLocation(player.getLocation()));
		player.sendMessage(ChatColor.GREEN + "Hai impostato la lobby.");
	}
	
	
	@SubCommand("spectatorSpawn")
	public void spectatorSpawn(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		currentSetup.spectatorSpawn = new LocationConfig(Utils.roundedLocation(player.getLocation()));
		player.sendMessage(ChatColor.GREEN + "Hai impostato lo spawn degli spettatori.");
	}
	
	
	@SubCommand("boss")
	public void boss(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		currentSetup.bossLocation = new LocationConfig(Utils.roundedLocation(player.getLocation()));
		player.sendMessage(ChatColor.GREEN + "Hai impostato la posizione del boss.");
	}
	
	
	@SubCommand("sign")
	public void sign(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		Block signBlock = CommandValidateExtra.getTargetBlock(player, Material.WALL_SIGN, "Non stai guardando un cartello sul muro.");
		
		currentSetup.sign = new BlockConfig(signBlock);
		player.sendMessage(ChatColor.GREEN + "Hai impostato il cartello.");
	}
	
	
	@SubCommand("addSpawner")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<tipo>")
	public void addSpawner(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		ResourceType spawnerType;
		try {
			spawnerType = ResourceType.valueOf(args[0].toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ExecuteException("Tipo di spawner non trovato: " + args[0] + " (" + StringUtils.join(ResourceType.values(), ", ").toLowerCase() + ").");
		}
				
		Block spawnerBlock = CommandValidateExtra.getTargetBlock(player, spawnerType.getBlockMaterial(), "Non stai guardando un " + Translation.of(spawnerType.getBlockMaterial()) + ".");
		
		for (SpawnerConfig spawnerConfig : currentSetup.spawners) {
			if (spawnerConfig.block.isSame(spawnerBlock) && spawnerConfig.type == spawnerType) {
				throw new ExecuteException("Esiste già uno spawner " + spawnerType.name() + " in quel blocco.");
			}
		}
		
		currentSetup.spawners.add(new SpawnerConfig(spawnerType, new BlockConfig(spawnerBlock)));
		player.sendMessage(ChatColor.GREEN + "Hai aggiunto uno spawner " + spawnerType.name() + " alla mappa.");
	}
	
	
	@SubCommand("removeSpawner")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<tipo>")
	public void removeSpawner(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		ResourceType spawnerType;
		try {
			spawnerType = ResourceType.valueOf(args[0].toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new ExecuteException("Tipo di spawner non trovato: " + args[0] + " (" + StringUtils.join(ResourceType.values(), ", ").toLowerCase() + ").");
		}
		
		Block targetBlock = player.getTargetBlock((Set<Material>) null, 64);
		Iterator<SpawnerConfig> iter = currentSetup.spawners.iterator();
		
		while (iter.hasNext()) {
			SpawnerConfig spawnerConfig = iter.next();
			if (spawnerConfig.block.isSame(targetBlock) && spawnerConfig.type == spawnerType) {
				iter.remove();
				player.sendMessage(ChatColor.GREEN + "Hai rimosso uno spawner " + spawnerConfig.type.name() + " dal blocco.");
				return;
			}
		}
		
		throw new ExecuteException("Nessuno spawner " + spawnerType.name() + " trovato nel blocco che stai guardando (" + Translation.of(targetBlock.getType()) + ").");
	}
	
	
	@SubCommand("addTeam")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<team>")
	public void addteam(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Team team = CommandValidateExtra.parseTeam(args[0]);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		CommandValidate.isTrue(currentSetup.teamConfigs.get(team.name()) == null, "Il team " + team.getNameSingular() + " è già stato aggiunto.");
		currentSetup.teamConfigs.put(team.name(), new TeamConfig());
		player.sendMessage(ChatColor.GREEN + "Hai aggiunto il team " + team.getNameSingular() + " alla mappa.");
	}
	
	
	@SubCommand("removeTeam")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<team>")
	public void removeteam(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Team team = CommandValidateExtra.parseTeam(args[0]);
		ArenaConfig currentSetup = getCurrentSetupNotNull(player);
		
		CommandValidate.notNull(currentSetup.teamConfigs.get(team.name()), "Il team " + team.getNameSingular() + " non è presente.");
		currentSetup.teamConfigs.remove(team.name());
		player.sendMessage(ChatColor.GREEN + "Hai rimosso il team " + team.getNameSingular() + " e le relative configurazioni dalla mappa.");
	}
	
	
	@SubCommand("bed")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<team>")
	public void bed(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Team team = CommandValidateExtra.parseTeam(args[0]);
		TeamConfig config = getTeamConfigNotNull(player, team);
		
		Block bedBlock = CommandValidateExtra.getTargetBlock(player, Material.BED_BLOCK, "Non stai guardando un letto.");
		
		Bed bed = (Bed) bedBlock.getState().getData();
		if (!bed.isHeadOfBed()) {
			bedBlock = bedBlock.getRelative(bed.getFacing());
			CommandValidate.isTrue(bedBlock.getType() == Material.BED_BLOCK, "Il letto è incompleto.");
			bed = (Bed) bedBlock.getState().getData();
		}
		
		CommandValidate.isTrue(bed.isHeadOfBed(), "Il letto è incompleto.");

		config.bedHeadLocation = new BlockConfig(bedBlock);
		config.bedFeetDirection = bed.getFacing().getOppositeFace();
		player.sendMessage(ChatColor.GREEN + "Hai impostato il letto " + team.getNameSingular() + ".");
	}
	
	
	@SubCommand("spawn")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<team>")
	public void spawn(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Team team = CommandValidateExtra.parseTeam(args[0]);
		TeamConfig config = getTeamConfigNotNull(player, team);
		
		config.spawnLocation = new LocationConfig(Utils.roundedLocation(player.getLocation()));
		player.sendMessage(ChatColor.GREEN + "Hai impostato lo spawn " + team.getNameSingular() + ".");
	}
	
	
	@SubCommand("villager")
	@SubCommandMinArgs(2)
	@SubCommandUsage("<team> <item|team>")
	public void villager(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Team team = CommandValidateExtra.parseTeam(args[0]);
		TeamConfig config = getTeamConfigNotNull(player, team);
		
		LocationConfig location = new LocationConfig(Utils.roundedLocation(player.getLocation()));
		
		if (args[1].equalsIgnoreCase("item")) {
			config.itemVillagerLocation = location;
		} else if (args[1].equalsIgnoreCase("team")) {
			config.teamVillagerLocation = location;
		} else {
			throw new ExecuteException("Devi specificare \"item\" o \"team\", \"" + args[1] + "\" non è corretto.");
		}
		
		player.sendMessage(ChatColor.GREEN + "Hai impostato il villager " + team.getNameSingular() + " " + (args[1].equalsIgnoreCase("team") ? "di squadra" : "di item") + ".");
	}
	
	
	@SuppressWarnings("deprecation")
	private void displayArenaVolume(Player player, ArenaConfig currentSetup, boolean replace) {
		if (currentSetup.corner1 != null && currentSetup.corner2 != null) {
			Block corner1 = currentSetup.corner1.getBlock();
			Block corner2 = currentSetup.corner2.getBlock();
			World world = corner1.getWorld();
			
			List<Block> changedBlocks = Lists.newArrayList();
			DyeColor highlightColor;
			
			for (int x : range(corner1.getX(), corner2.getX())) {
				for (int y : range(corner1.getY(), corner2.getY())) {
					for (int z : range(corner1.getZ(), corner2.getZ())) {
						Block block = world.getBlockAt(x, y, z);
						
						if (block.getType().isOccluding() && !replace) {
							continue;
						}
						
						if (countTrues(x == corner1.getX(), x == corner2.getX(), y == corner1.getY(), y == corner2.getY(), z == corner1.getZ(), z == corner2.getZ()) >= 2) {
							DyeColor woolColor = (x + y + z) % 2 == 0 ? DyeColor.BLACK : DyeColor.YELLOW;
							player.sendBlockChange(block.getLocation(), Material.WOOL, woolColor.getWoolData());
							changedBlocks.add(block);
							continue;
						}
						
						highlightColor = null;
						
						if (currentSetup.spawnProtectionRadius > 0) {
							for (TeamConfig teamConfig : currentSetup.teamConfigs.values()) {
								highlightColor = highlightRange(highlightColor, block, currentSetup.spawnProtectionRadius, teamConfig.spawnLocation);
							}
						}
						
						if (currentSetup.villagerProtectionRadius > 0) {
							for (TeamConfig teamConfig : currentSetup.teamConfigs.values()) {
								highlightColor = highlightRange(highlightColor, block, currentSetup.villagerProtectionRadius, teamConfig.itemVillagerLocation);
								highlightColor = highlightRange(highlightColor, block, currentSetup.villagerProtectionRadius, teamConfig.teamVillagerLocation);
							}
						}
						
						if (currentSetup.generatorsProtectionRadius > 0) {
							for (SpawnerConfig spawnerConfig : currentSetup.spawners) {
								if (spawnerConfig.type.isPublic()) {
									highlightColor = highlightRange(highlightColor, block, currentSetup.generatorsProtectionRadius, spawnerConfig.block.toLocationConfig());
								}
							}
						}
						
						if (highlightColor != null) {
							player.sendBlockChange(block.getLocation(), Material.STAINED_GLASS, highlightColor.getWoolData());
							changedBlocks.add(block);
						}
					}
				}
			}
			
			Bukkit.getScheduler().runTaskLater(Bedwars.get(), () -> {
				if (player.isOnline()) {
					for (Block changedBlock : changedBlocks) {
						player.sendBlockChange(changedBlock.getLocation(), changedBlock.getType(), changedBlock.getData());
					}
				}
			}, 200);
		}
	}
		
	private DyeColor highlightRange(DyeColor currentColor, Block block, int radius, LocationConfig location) {
		if (location != null) {
			if (Utils.isInRange(block, radius, (int) location.x, (int) location.y, (int) location.z)) {
				return DyeColor.GRAY;
			} else if (Utils.isInRange(block, radius + 1, (int) location.x, (int) location.y, (int) location.z)) {
				if (currentColor == null) {
					return DyeColor.PURPLE;
				}
			}
		}
		
		return currentColor;
	}
	
	private int countTrues(boolean... values) {
		int count = 0;
		for (boolean value : values) {
			if (value) {
				count++;
			}
		}
		return count;
	}
	
	private int[] range(int from, int to) {
		int index = 0;
		if (from < to) {
			int[] range = new int[to - from + 1];
			for (int current = from; current <= to; current++) {
				range[index++] = current;
			}
			return range;
		} else {
			int[] range = new int[from - to + 1];
			for (int current = to; current <= from; current++) {
				range[index++] = current;
			}
			return range;
		}
	}


}
