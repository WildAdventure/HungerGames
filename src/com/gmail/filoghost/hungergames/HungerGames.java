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
package com.gmail.filoghost.hungergames;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import lombok.Getter;
import net.cubespace.yamler.YamlerConfigurationException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import wild.api.WildCommons;
import wild.api.bridges.BoostersBridge;
import wild.api.bridges.BoostersBridge.Booster;
import wild.api.config.PluginConfig;
import wild.api.item.BookTutorial;
import wild.api.item.ItemBuilder;

import com.gmail.filoghost.hungergames.commands.ClassificaCommand;
import com.gmail.filoghost.hungergames.commands.CoinCommand;
import com.gmail.filoghost.hungergames.commands.DebugCommand;
import com.gmail.filoghost.hungergames.commands.FinalbattleCommand;
import com.gmail.filoghost.hungergames.commands.FixCommand;
import com.gmail.filoghost.hungergames.commands.GamemakerCommand;
import com.gmail.filoghost.hungergames.commands.GlobalChatCommand;
import com.gmail.filoghost.hungergames.commands.KitCommand;
import com.gmail.filoghost.hungergames.commands.SpawnCommand;
import com.gmail.filoghost.hungergames.commands.SpectatorCommand;
import com.gmail.filoghost.hungergames.commands.StartCommand;
import com.gmail.filoghost.hungergames.commands.StatsCommand;
import com.gmail.filoghost.hungergames.commands.TeamCommand;
import com.gmail.filoghost.hungergames.files.HelpFile;
import com.gmail.filoghost.hungergames.files.Settings;
import com.gmail.filoghost.hungergames.files.SkillsLang;
import com.gmail.filoghost.hungergames.generation.Cornucopia;
import com.gmail.filoghost.hungergames.generation.RandomItem;
import com.gmail.filoghost.hungergames.hud.menu.KitMenuManager;
import com.gmail.filoghost.hungergames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.hungergames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.hungergames.hud.tags.TagsManager;
import com.gmail.filoghost.hungergames.listener.BoatFixListener;
import com.gmail.filoghost.hungergames.listener.ChatListener;
import com.gmail.filoghost.hungergames.listener.DeathListener;
import com.gmail.filoghost.hungergames.listener.InventoryListener;
import com.gmail.filoghost.hungergames.listener.InvisibleFireFixListener;
import com.gmail.filoghost.hungergames.listener.JoinQuitListener;
import com.gmail.filoghost.hungergames.listener.LastDamageCauseListener;
import com.gmail.filoghost.hungergames.listener.PingListener;
import com.gmail.filoghost.hungergames.listener.SkillsListener;
import com.gmail.filoghost.hungergames.listener.StrengthFixListener;
import com.gmail.filoghost.hungergames.listener.protection.BlockListener;
import com.gmail.filoghost.hungergames.listener.protection.CommandListener;
import com.gmail.filoghost.hungergames.listener.protection.EntityListener;
import com.gmail.filoghost.hungergames.listener.protection.WeatherListener;
import com.gmail.filoghost.hungergames.mysql.SQLColumns;
import com.gmail.filoghost.hungergames.mysql.SQLManager;
import com.gmail.filoghost.hungergames.mysql.SQLTask;
import com.gmail.filoghost.hungergames.player.HGamer;
import com.gmail.filoghost.hungergames.player.Kit;
import com.gmail.filoghost.hungergames.player.Status;
import com.gmail.filoghost.hungergames.timers.CheckWinnerTimer;
import com.gmail.filoghost.hungergames.timers.CompassUpdateTimer;
import com.gmail.filoghost.hungergames.timers.EndTimer;
import com.gmail.filoghost.hungergames.timers.FinalBattleTimer;
import com.gmail.filoghost.hungergames.timers.GameTimer;
import com.gmail.filoghost.hungergames.timers.InvincibilityTimer;
import com.gmail.filoghost.hungergames.timers.MySQLKeepAliveTimer;
import com.gmail.filoghost.hungergames.timers.PregameTimer;
import com.gmail.filoghost.hungergames.timers.TimerMaster;
import com.gmail.filoghost.hungergames.timers.WorldBorderTimer;
import com.gmail.filoghost.hungergames.utils.Matcher;
import com.gmail.filoghost.hungergames.utils.MathUtils;
import com.gmail.filoghost.hungergames.utils.Parser;
import com.gmail.filoghost.hungergames.utils.ParserException;
import com.gmail.filoghost.hungergames.utils.UnitUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class HungerGames extends JavaPlugin {

	public static final 				String PLUGIN_ID = "hunger_games";
	
	@Getter private static 				HungerGames instance;
	@Getter private static 				Settings settings;
	@Getter	private static 				GameState state;
	private static 						Set<Material> protectedMaterials;
	private static 						Set<String> spectatorCommandBlacklist;
	
	// Tutti i timer
	@Getter private static 				PregameTimer pregameTimer;
	@Getter private static 				TimerMaster invincibilityTimer;
	@Getter private static 				GameTimer gameTimer;
	@Getter private static 				TimerMaster finalBattleTimer;
	@Getter private static 				WorldBorderTimer worldBorderTimer;
	@Getter private static 				TimerMaster checkWinnerTimer;
	@Getter private static 				EndTimer endTimer;
	
	@Getter private static 				String mapName;
	private static 						World world;
	@Getter private static 				int worldBorderLimit;
	
	@Getter private static 				Random randomGenerator;
	
	public static 						Map<Player, HGamer> players;
	private static						List<Kit> kits;
	@Getter private static				List<RandomItem> cornucopiaItems;
	@Getter private static				Kit defaultKit;
	@Getter private static				Location highestSpawn;
	
	@Getter private static				BookTutorial bookTutorial;
	@Getter private static				ItemStack kitSelector;
	
	@Getter private static 				boolean wildChat;
	
	@Override
	public void onLoad() {
		// Prima di tutto
		instance = this;
		randomGenerator = new Random();
		
		// Configurazione
		try {
			settings = new Settings();
			settings.init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			logPurple("config.yml non caricato! Spegnimento server fra 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		// Mappe
		File mapsFolder = new File(settings.mapsFolder);
		
		if (!mapsFolder.isDirectory()) {
			logPurple("Cartella mappe (" + mapsFolder.getAbsolutePath() + ") non trovata, utilizzando quella di default!");
			mapsFolder = new File(getDataFolder(), "maps");
		} else {
			try {
				getLogger().info("Trovata cartella mappe: " + mapsFolder.getCanonicalPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (!mapsFolder.isDirectory()) mapsFolder.mkdirs();
		
		logAqua("Cancellazione mondo vecchio.");
		Bukkit.getServer().unloadWorld("world", false);
		try {
			FileUtils.deleteDirectory(new File("world"));
		} catch (IOException e) {
			e.printStackTrace();
			logPurple("Impossibile cancellare il vecchio mondo!");
		}
		
		worldBorderLimit = settings.randomWorlds_border; // Di default
		
		if (settings.randomWorlds_enable) {
			logAqua("Verrà creato un nuovo mondo casuale.");
		} else {
			List<MapInfo> mapInfoList = Lists.newArrayList();
			
			for (String worldInfo : settings.maps) {
				String[] split = worldInfo.replace(" ", "").split(",");
				
				if (split.length < 2) {
					logPurple("Informazioni mappa non valide: " + worldInfo);
					continue;
				}
				
				if (!MathUtils.isValidInteger(split[1])) {
					logPurple("Dimensione bordo non valide: " + worldInfo);
					continue;
				}
				
				File map = new File(mapsFolder, split[0]);
				if (!map.isDirectory()) {
					logPurple("Mappa non trovata: " + worldInfo);
					continue;
				}
				
				mapInfoList.add(new MapInfo(map, Integer.parseInt(split[1])));
			}
			
			if (mapInfoList.isEmpty()) {
				logPurple("Nessuna mappa valida trovata! Verrà utilizzato un mondo casuale.");
				
			} else {
				
				MapInfo randomMap = mapInfoList.get(randomGenerator.nextInt(mapInfoList.size()));
				worldBorderLimit = randomMap.getBorder();
				logAqua("Caricamento mappa casuale '" + randomMap.getFolder().getName() + "', bordo: " + worldBorderLimit + ".");
				
				try {
					FileUtils.copyDirectory(randomMap.getFolder(), new File("world"), false);
				} catch (IOException e) {
					e.printStackTrace();
					logPurple("Impossibile copiare la mappa salvata!");
				}
			}
		}
	}
	
	@Override
	public void onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("WildCommons")) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[" + this.getName() + "] Richiesto WildCommons!");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		if (Bukkit.getPluginManager().isPluginEnabled("WildChat")) {
			wildChat = true;
		}
		
		// File di aiuto
		try {
			new HelpFile().init();
		} catch (YamlerConfigurationException e) {
			e.printStackTrace();
			logPurple("help.yml non caricato!");
		}
		
		
		// Database MySQL
		try {
			SQLManager.connect(settings.mysql_host, settings.mysql_port, settings.mysql_database, settings.mysql_user, settings.mysql_pass);
			SQLManager.checkConnection();
					
			SQLManager.getMysql().update("CREATE TABLE IF NOT EXISTS hg_players ("
					+ SQLColumns.NAME + " varchar(20) NOT NULL,"
					+ SQLColumns.COINS + " MEDIUMINT unsigned NOT NULL,"
					+ SQLColumns.KILLS + " MEDIUMINT unsigned NOT NULL,"
					+ SQLColumns.DEATHS + " MEDIUMINT unsigned NOT NULL,"
					+ SQLColumns.WINS + " MEDIUMINT unsigned NOT NULL"
					+ ") ENGINE = InnoDB DEFAULT CHARSET = UTF8;");
					
			SQLManager.getMysql().update("CREATE TABLE IF NOT EXISTS hg_kits ("
					+ SQLColumns.NAME + " varchar(50) NOT NULL,"
					+ SQLColumns.COUNT + " MEDIUMINT unsigned NOT NULL"
					+ ") ENGINE = InnoDB DEFAULT CHARSET = UTF8;");
					
		} catch (Exception ex) {
			ex.printStackTrace();
			logPurple("Impossibile connettersi al database! Il server verrà spento in 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
				
		// Variabili
		world = Bukkit.getWorld("world");
		if (world == null) {
			logPurple("Impossibile trovare il mondo principale! Il server verrà spento in 10 secondi...");
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		
		BoostersBridge.registerPluginID(PLUGIN_ID);
		
		if (settings.randomWorlds_enable) {
			highestSpawn = world.getHighestBlockAt(world.getSpawnLocation()).getLocation().add(0.5, 0, 0.5);
			highestSpawn.setY(Cornucopia.getHighestYIgnoreTrees(world, highestSpawn.getBlockX(), highestSpawn.getBlockZ()));
		} else {
			highestSpawn = world.getSpawnLocation().add(0.5, 0, 0.5);
		}
		
		state = GameState.PRE_GAME;
		protectedMaterials = Sets.newHashSet();
		spectatorCommandBlacklist = Sets.newHashSet();
		
		players = Maps.newConcurrentMap();
		kits = Lists.newArrayList();
		
		cornucopiaItems = Lists.newArrayList();
		for (String randomItemString : settings.cornucopia) {
			try {
				cornucopiaItems.add(Parser.RandomItems.parse(randomItemString));
			} catch (ParserException e) {
				logPurple("Oggetto random non valido (" + e.getMessage() + "): " + randomItemString);
			}
		}
		
		// Lettura items
		bookTutorial = new BookTutorial(this, "Hunger Games");
		kitSelector = ItemBuilder
							.of(Material.EMERALD)
							.name(ChatColor.GREEN + "Scegli un kit " + ChatColor.GRAY + "(Click destro)")
							.lore(ChatColor.GRAY + "Per aprire fai click con mouse destro",
								  ChatColor.GRAY + "mentre tieni l'oggetto in mano.")
							.build();

		// Blocchi protetti
		for (String protectedBlock : settings.protectedBlocks) {
			Material mat = Matcher.Materials.match(protectedBlock);
					
			if (mat != null) {
				protectedMaterials.add(mat);
			} else {
				logPurple("Materiale protetto non riconosciuto: " + protectedBlock);
			}
		}
		
		// Comandi bloccati
		for (String blacklistedCommand : settings.spectatorCommandBlacklist) {
			spectatorCommandBlacklist.add(blacklistedCommand.toLowerCase());
		}
		
		// Lang per le Skill
		try {
			SkillsLang.load();
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
			logPurple("Impossibile leggere o salvare abilities.yml");
		}
				
			
		// Lettura kit
		File kitFolder = new File(getDataFolder(), "kits");
		if (!kitFolder.isDirectory()) {
			kitFolder.mkdirs();
		}
				
		File[] kitFiles = kitFolder.listFiles();
		for (File kitFile : kitFiles) {
			if (kitFile.getName().endsWith(".yml")) {
								
				String kitName = kitFile.getName().replace(".yml", "").replace("_", " ");
								
				try {
							
					PluginConfig kitYaml = new PluginConfig(instance, kitFile);
					
					if (!kitYaml.isSet("timestamp")) {
						kitYaml.set("timestamp", System.currentTimeMillis() / 60000L);
						kitYaml.save();
					}
					
					if (!kitName.equalsIgnoreCase("default")) {
						kits.add(new Kit(kitName, kitYaml));
					} else {
						defaultKit = new Kit(kitName, kitYaml);
					}
					
				} catch (InvalidConfigurationException e) {
					e.printStackTrace();
					logPurple("Impossibile leggere il kit '" + kitName + "': configurazione non valida");
						
				} catch (IOException e) {
					e.printStackTrace();
					logPurple("Impossibile leggere il kit '" + kitName + "': errore I/O");
				}
			}
		}
		
		Collections.sort(kits, new Comparator<Kit>() {

			@Override
			public int compare(Kit kit1, Kit kit2) {
				return kit1.getCreationTimestampMinute() - kit2.getCreationTimestampMinute();
			}
		});
		
		KitMenuManager.load(kits);
		for (Kit kit : kits) {
			try {
				if (!SQLManager.kitExists(kit.getName())) {
					SQLManager.createKitData(kit.getName());
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logPurple("Impossibile creare la riga per il kit '" + kit.getName() + "'");
			}
		}
		logAqua("Caricati " + kits.size() + " kit.");
		
		// Teleporter
		TeleporterMenu.load();
		
		// Sidebar & teams
		SidebarManager.initialize(state);
		TagsManager.initialize();

		
		
		// Impostazioni del mondo
		try {
			world.setDifficulty(Difficulty.valueOf(settings.difficulty.toUpperCase()));
		} catch (IllegalArgumentException e) {
			world.setDifficulty(Difficulty.HARD);
			logPurple("Difficoltà non valida. Default: hard");
		}
		world.setPVP(true);
		world.setSpawnFlags(true, true);
		world.setStorm(false);
		world.setThundering(false);
		world.setKeepSpawnInMemory(true);
		world.setGameRuleValue("doFireTick", "true");
		world.setGameRuleValue("doMobLoot", "true");
		world.setGameRuleValue("doMobSpawning", "true");
		world.setGameRuleValue("doTileDrops", "true");
		world.setGameRuleValue("keepInventory", "false");
		world.setGameRuleValue("mobGriefing", "true");
		world.setGameRuleValue("naturalRegeneration", "true");
		world.setGameRuleValue("doDaylightCycle", "false");
		world.setTime(3000);
		
		// world.setAutoSave(false);
		
		
		// Comandi
		new StartCommand();
		new KitCommand();
		new DebugCommand();
		new FixCommand();
		new GamemakerCommand();
		new SpectatorCommand();
		new CoinCommand();
		new StatsCommand();
		new SpawnCommand();
		new FinalbattleCommand();
		new TeamCommand();
		new ClassificaCommand();
		new GlobalChatCommand();
		
		
		// Listeners
		Bukkit.getPluginManager().registerEvents(new JoinQuitListener(), this);
		Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
		Bukkit.getPluginManager().registerEvents(new EntityListener(), this);
		Bukkit.getPluginManager().registerEvents(new DeathListener(), this);
		Bukkit.getPluginManager().registerEvents(new SkillsListener(), this);
		Bukkit.getPluginManager().registerEvents(new InventoryListener(), this);
		Bukkit.getPluginManager().registerEvents(new PingListener(), this);
		Bukkit.getPluginManager().registerEvents(new WeatherListener(), this);
		Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
		Bukkit.getPluginManager().registerEvents(new CommandListener(), this);
		Bukkit.getPluginManager().registerEvents(new LastDamageCauseListener(), this);
		
		Bukkit.getPluginManager().registerEvents(new StrengthFixListener(), this);
		Bukkit.getPluginManager().registerEvents(new InvisibleFireFixListener(), this);
		Bukkit.getPluginManager().registerEvents(new BoatFixListener(), this);
		
		// Cornucopia
		Cornucopia.setup(highestSpawn.getBlock(), settings.randomWorlds_enable);
		
		if (settings.randomWorlds_enable) {
			highestSpawn = world.getHighestBlockAt(highestSpawn).getLocation().add(0.5, 0, 0.5); // Spostiamo di nuovo
			world.setSpawnLocation(highestSpawn.getBlockX(), highestSpawn.getBlockY(), highestSpawn.getBlockZ());
		}
		
		// Riempie le casse
		for (Block block : Cornucopia.getChests()) {
			
			Inventory inv = ((Chest) block.getState()).getInventory();
			
			List<Integer> slotsToFill = Lists.newArrayList();
			for (int i = 0; i < inv.getSize(); i++) {
				slotsToFill.add(i);
			}
			
			Collections.shuffle(slotsToFill);
			
			for (RandomItem randomItem : HungerGames.getCornucopiaItems()) {
				randomItem.distribute(inv, slotsToFill);
			}
		}
		
		
		// Timer iniziali
		pregameTimer = new PregameTimer();
		invincibilityTimer = new InvincibilityTimer();
		gameTimer = new GameTimer();
		finalBattleTimer = new FinalBattleTimer();
		worldBorderTimer = new WorldBorderTimer(world, worldBorderLimit);
		checkWinnerTimer = new CheckWinnerTimer();
		endTimer = new EndTimer();
		worldBorderTimer.startNewTask();
		pregameTimer.startNewTask();
		checkWinnerTimer.startNewTask();
		new CompassUpdateTimer().startNewTask();
		new MySQLKeepAliveTimer().startNewTask();
	}
	
	@Override
	public void onDisable() {
		BoostersBridge.unregisterPluginID(PLUGIN_ID);
	}
	
	public static HGamer registerHGamer(Player bukkitPlayer, Status status) {
		HGamer hGamer = new HGamer(bukkitPlayer, status);
		players.put(bukkitPlayer, hGamer);
		return hGamer;
	}
	
	public static HGamer unregisterHGamer(Player bukkitPlayer) {
		return players.remove(bukkitPlayer);
	}
	
	public static HGamer getHGamer(String name) {
		name = name.toLowerCase();
		for (HGamer hGamer : players.values()) {
			if (hGamer.getName().toLowerCase().equals(name)) {
				return hGamer;
			}
		}
		return null;
	}
	
	public static HGamer getHGamer(Player bukkitPlayer) {
		if (bukkitPlayer == null) {
			return null;
		}
		return players.get(bukkitPlayer);
	}
	
	public static void setState(GameState state) {
		HungerGames.state = state;
		SidebarManager.updateState(state);
	}
	
	public static void checkWinner() {
		if (state == GameState.GAME || state == GameState.FINAL_BATTLE) {
			
			HGamer winner = null;
			for (HGamer hGamer : players.values()) {
				
				if (hGamer.getStatus() == Status.TRIBUTE) {
					
					if (winner == null) {
						winner = hGamer;
					} else {
						return; // Già settato quindi sono almeno 2
					}
				}
			}
			
			setState(GameState.END);
			SidebarManager.hideSidebar();
			
			gameTimer.stopTask();
			finalBattleTimer.stopTask();
			
			if (winner == null) {
				
				logAqua("Nessun vincitore!");
				stopServer(ChatColor.RED + "Non c'è stato nessun vincitore, riavvio del server.");
				
			} else {
				Booster booster = BoostersBridge.getActiveBooster(PLUGIN_ID);
				final int coins = BoostersBridge.applyMultiplier(UnitUtils.getWinCoins(winner.getPlayer()), booster);
				
				for (HGamer other : HungerGames.getAllGamersUnsafe()) {
					if (other != winner) {
						other.sendMessage(ChatColor.GOLD + winner.getName() + " ha vinto la partita!");
					}
				}
				
				winner.sendMessage("");
				winner.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "HAI VINTO LA PARTITA!" + ChatColor.RESET + ChatColor.GOLD + " +" + UnitUtils.formatCoins(coins) + BoostersBridge.messageSuffix(booster));
				winner.getPlayer().setGameMode(GameMode.CREATIVE);
				logAqua("Vincitore: " + winner.getName());
				
				final String name = winner.getName();
				new SQLTask() {
					@Override
					public void execute() throws SQLException {
						SQLManager.increaseStat(name, SQLColumns.WINS, 1);
						SQLManager.increaseStat(name, SQLColumns.COINS, coins);
					}
				}.submitAsync(winner.getPlayer());
				
				endTimer.setWinnerAndStart(winner.getPlayer());
			}
		}
	}
	
	public static void stopServer(String message) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			player.kickPlayer(message + "§0§0§0");
		}
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
			
			@Override
			public void run() {
				Bukkit.shutdown();
			}
		}, 20L);
		
	}
	
	public static Collection<HGamer> getAllGamersUnsafe() {
		return players.values();
	}
	
	public static Collection<Player> getByStatus(Status status) {
		Set<Player> match = Sets.newHashSet();
		
		for (HGamer hGamer : players.values()) {
			if (hGamer.getStatus() == status) {
				match.add(hGamer.getPlayer());
			}
		}
		
		return match;
	}
	
	public static int countTributes() {
		int count = 0;
		
		for (HGamer hGamer : players.values()) {
			if (hGamer.getStatus() == Status.TRIBUTE) {
				count++;
			}
		}
		
		return count;
	}
	
	public static Collection<HGamer> getNearTributes(Player nearWho, double distance) {
		return getNearTributes(nearWho.getLocation(), distance, nearWho);
	}
	
	public static Collection<HGamer> getNearTributes(Location loc, double distance, Player excluded) {
		
		double distanceSquared = distance * distance;
		Set<HGamer> near = Sets.newHashSet();
		
		for (HGamer hGamer : players.values()) {
			if (hGamer.getPlayer() != excluded &&
				hGamer.getStatus() == Status.TRIBUTE &&
				hGamer.getPlayer().getWorld() == loc.getWorld() &&
				hGamer.getPlayer().getLocation().distanceSquared(loc) <= distanceSquared) {
				
				near.add(hGamer);
			}
		}
		
		return near;
	}
	
	public static Kit matchKit(String input) {
		input = input.replaceAll("[ _-]", "").toLowerCase();
		for (Kit kit : kits) {
			if (kit.getName().replaceAll("[ _-]", "").toLowerCase().equals(input)) {
				return kit;
			}
		}
		
		return null;
	}

	// Scritte di errore
	public static void logPurple(String log) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.LIGHT_PURPLE + log);
	}
	
	// Scritte normali
	public static void logAqua(String log) {
		Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + log);
	}
	
	public static boolean isMaterialProtected(Material material) {
		return protectedMaterials.contains(material);
	}
	
	public static boolean isSpectatorBlacklistCommand(String command) {
		return spectatorCommandBlacklist.contains(command.toLowerCase());
	}

	public static boolean isBlockProtected(Block block) {
		return isMaterialProtected(block.getType()) || Cornucopia.isFloorColumn(block);
	}
}
