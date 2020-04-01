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
package com.gmail.filoghost.hungergames.listener;

import java.sql.SQLException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.filoghost.hungergames.GameState;
import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.Perms;
import com.gmail.filoghost.hungergames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.hungergames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.hungergames.mysql.SQLManager;
import com.gmail.filoghost.hungergames.mysql.SQLTask;
import com.gmail.filoghost.hungergames.player.HGamer;
import com.gmail.filoghost.hungergames.player.Status;
import com.google.common.collect.Sets;

public class JoinQuitListener implements Listener {

	@EventHandler
	public void onLogin(PlayerLoginEvent event) {
		
		if (event.getResult() == Result.KICK_FULL && event.getPlayer().hasPermission(Perms.JOIN_FULL)) {
			event.allow();
		}
		
		if (HungerGames.getState() != GameState.PRE_GAME &&	!(event.getPlayer().hasPermission(Perms.GAMEMAKER) || event.getPlayer().hasPermission(Perms.SPECTATOR))) {
			event.disallow(Result.KICK_OTHER, ChatColor.RED + "La partita è già iniziata.");
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		Status status = Status.TRIBUTE;

		if (HungerGames.getState() != GameState.PRE_GAME) {
			if (player.hasPermission(Perms.GAMEMAKER)) {
				status = Status.GAMEMAKER;
			} else if (player.hasPermission(Perms.SPECTATOR)) {
				status = Status.SPECTATOR;
			} else {
				HungerGames.logPurple(player.getName() + " è un tributo ed è entrato fuori dalla partita!?");
			}
		}
		
		final String name = player.getName();
		new SQLTask() {
			@Override
			public void execute() throws SQLException {
				if (!SQLManager.playerExists(name)) {
					SQLManager.createPlayerData(name);
				}
			}
		}.submitAsync(player);
		
		HGamer hGamer = HungerGames.registerHGamer(player, status); // IMPORTANTE!
		hGamer.teleportDismount(HungerGames.getHighestSpawn());
		
		// Dopo aver registrato il giocatore
		SidebarManager.setPlayers(HungerGames.countTributes());
		TeleporterMenu.update();
		
		if (HungerGames.getState() == GameState.PRE_GAME && Bukkit.getOnlinePlayers().size() >= Bukkit.getMaxPlayers()) {
			// Significa che è pieno
			if (HungerGames.getPregameTimer().getCountdown() > 15) {
				Bukkit.broadcastMessage(ChatColor.YELLOW + "Server pieno: il conto alla rovescia è stato ridotto.");
				HungerGames.getPregameTimer().setCountdown(15);
			}
		}
	}
	
	public static Set<HGamer> kickedOnDeath = Sets.newHashSet();
	
	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		HGamer hQuitted = HungerGames.unregisterHGamer(event.getPlayer()); // IMPORTANTE!
		
		if (!kickedOnDeath.remove(hQuitted) && hQuitted.getStatus() == Status.TRIBUTE && HungerGames.getState().allowPlayerDamage()) { // Altrimenti niente, o deve ancora iniziare o è finita
			DeathListener.parseDeath(hQuitted, null, ChatColor.RED + hQuitted.getName() + " è uscito dalla partita.", false, false);
		}

		SidebarManager.setPlayers(HungerGames.countTributes());
		TeleporterMenu.update();
	}
	
}
