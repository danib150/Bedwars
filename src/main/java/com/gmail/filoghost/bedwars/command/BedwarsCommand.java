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

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.gmail.filoghost.bedwars.Bedwars;
import com.gmail.filoghost.bedwars.Perms;
import com.gmail.filoghost.bedwars.arena.Arena;
import com.gmail.filoghost.bedwars.database.SQLManager;
import com.gmail.filoghost.bedwars.settings.MainSettings;
import com.gmail.filoghost.bedwars.settings.objects.ArenaConfig;
import com.gmail.filoghost.bedwars.settings.objects.LocationConfig;
import com.gmail.filoghost.bedwars.utils.Utils;

import net.cubespace.yamler.YamlerConfigurationException;
import net.md_5.bungee.api.ChatColor;
import wild.api.command.CommandFramework.Permission;
import wild.api.command.SubCommandFramework;

@Permission(Perms.COMMAND_BEDWARS)
public class BedwarsCommand extends SubCommandFramework {
	
	public BedwarsCommand(JavaPlugin plugin, String label, String... aliases) {
		super(plugin, label, aliases);
	}
	
	@Override
	public void noArgs(CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_GREEN + "Lista comandi " + this.label + ":");
		for (SubCommandDetails sub : this.getAccessibleSubCommands(sender)) {
			sender.sendMessage(ChatColor.GREEN + "/" + this.label + " " + sub.getName() + (sub.getUsage() != null ?  " " + sub.getUsage() : ""));
		}
	}
	
	@SubCommand("debug")
	public void debug(CommandSender sender, String label, String[] args) {
		CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Comando solo per console.");
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Giocatori (" + Bedwars.getArenasByPlayers().size() + "):");
		for (Entry<Player, Arena> entry : Bedwars.getArenasByPlayers().entrySet())  {
			sender.sendMessage(ChatColor.LIGHT_PURPLE + entry.getKey().getName() + " -> " + (entry.getValue() != null ? entry.getValue().getName() : "/"));
		}
	}
	
	@SubCommand("setSpawn")
	public void setSpawn(CommandSender sender, String label, String[] args) {
		Player player = CommandValidate.getPlayerSender(sender);
		Location spawn = Utils.roundedLocation(player.getLocation());
		
		Bedwars.setSpawn(spawn);
		MainSettings.spawn = new LocationConfig(spawn);
		spawn.getWorld().setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());
		
		try {
			new MainSettings(Bedwars.get(), "config.yml").save();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile salvare la configurazione.");
		}

		player.sendMessage(ChatColor.GREEN + "Hai impostato lo spawn globale.");
	}
	
	@SubCommand("tpArena")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void tpArena(CommandSender sender, String label, String[] args) {
		String name = args[0];
		Arena arena = Bedwars.getArenaByName(name);
		CommandValidate.notNull(arena, "Arena non trovata.");
		
		CommandValidate.getPlayerSender(sender).teleport(arena.getSpectatorSpawn());
		sender.sendMessage("Teletrasportato nell'arena " + name + ".");
	}
	
	@SubCommand("loadArena")
	@SubCommandMinArgs(1)
	@SubCommandUsage("<nome>")
	public void loadArena(CommandSender sender, String label, String[] args) {
		String name = args[0];
		try {
			CommandValidate.isTrue(Bedwars.getArenaByName(name) == null, "Arena già caricata.");
			ArenaConfig arenaConfig = Bedwars.loadArenaConfig(name);
			CommandValidate.notNull(arenaConfig, "Arena non trovata.");
			Bedwars.addArena(new Arena(arenaConfig));
			sender.sendMessage(ChatColor.GREEN + "Hai caricato l'arena " + name + ".");
			
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			throw new ExecuteException("Impossibile leggere la configurazione.");
		} catch (ExecuteException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new ExecuteException("Eccezione non gestita durante il caricamento dell'arena.");
		}
	}
	
	@SubCommand("reset")
	public void reset(CommandSender sender, String label, String[] args) {
		CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Eseguibile solo da console.");
		
		sender.sendMessage(ChatColor.GRAY + "Attendi...");
		Bukkit.getScheduler().runTaskAsynchronously(Bedwars.get(), () -> {
			try {
				SQLManager.resetStats();
				Bukkit.getScheduler().runTask(Bedwars.get(), () -> {
					Bedwars.resetOnlinePlayersData();
				});
				
				sender.sendMessage(ChatColor.GREEN + "Statistiche resettate!");
				
			} catch (Exception e) {
				e.printStackTrace();
				sender.sendMessage(ChatColor.RED + "Errore durante il reset.");
			}
		});
	}


}
