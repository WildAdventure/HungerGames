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
package com.gmail.filoghost.hungergames.timers;

import lombok.Getter;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import wild.api.bridges.CosmeticsBridge;
import wild.api.sound.EasySound;

import com.gmail.filoghost.hungergames.GameState;
import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.hungergames.player.HGamer;
import com.gmail.filoghost.hungergames.player.Kit;
import com.gmail.filoghost.hungergames.player.Status;
import com.gmail.filoghost.hungergames.tasks.UpdateKitUsageTask;
import com.gmail.filoghost.hungergames.utils.MapCounter;
import com.gmail.filoghost.hungergames.utils.UnitUtils;

public class PregameTimer extends TimerMaster {

	@Getter @Setter private int countdown;
	
	@Getter private boolean started;
	
	private EasySound clickSound = new EasySound(Sound.CLICK);
	private EasySound anvilSound = new EasySound(Sound.ANVIL_LAND);
	
	private ItemStack compass = new ItemStack(Material.COMPASS);
	
	@Getter private String lastCountdownMessage;
	
	private Listener movementBlocker;

	public PregameTimer() {
		super(0, 20L);
		resetCountdown();
		lastCountdownMessage = "N/A";
	}
	
	private void resetCountdown() {
		started = false;
		this.countdown = HungerGames.getSettings().startCountdown;
		SidebarManager.setTime("-");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		
		if (!started) {
			
			if (HungerGames.countTributes() >= HungerGames.getSettings().minPlayers) {
				started = true;
				SidebarManager.setTime(UnitUtils.formatMinutes(countdown / 60));
			} else {
				return;
			}
		}
		
		if (countdown <= 0) {
			
			if (movementBlocker != null) {
				PlayerMoveEvent.getHandlerList().unregister(movementBlocker);
				movementBlocker = null;
			}
			
			if (HungerGames.countTributes() < HungerGames.getSettings().minPlayers) {
				Bukkit.broadcastMessage(ChatColor.GREEN + "Ci sono pochi giocatori, il conto alla rovescia riparte.");
				resetCountdown();
				return;
			}
			
			MapCounter<Kit> usageCounter = new MapCounter<Kit>();
			
			for (HGamer tribute : HungerGames.getAllGamersUnsafe()) {
				
				if (tribute.getStatus() == Status.TRIBUTE) {
					CosmeticsBridge.updateCosmetics(tribute.getPlayer(), CosmeticsBridge.Status.GAME);
					tribute.cleanCompletely(GameMode.SURVIVAL);
					tribute.teleportDismount(HungerGames.getHighestSpawn());
					
					if (tribute.hasKit()) {
						tribute.getKit().apply(tribute.getPlayer());
						usageCounter.increment(tribute.getKit());
						
					} else if (HungerGames.getDefaultKit() != null) {
						HungerGames.getDefaultKit().apply(tribute.getPlayer());
						usageCounter.increment(HungerGames.getDefaultKit());
					}
					
					tribute.getPlayer().getInventory().addItem(compass);
				}
			};
			
			Bukkit.getScheduler().scheduleAsyncDelayedTask(HungerGames.getInstance(), new UpdateKitUsageTask(usageCounter), 10 * 20);
			
			anvilSound.playToAll();
			Bukkit.broadcastMessage("");
			Bukkit.broadcastMessage(ChatColor.GREEN + "La partita è iniziata!");
			HungerGames.setState(GameState.INVINCIBILITY);
			HungerGames.getInvincibilityTimer().startNewTask();
			stopTask();
			return;
		}
		
		
		if (countdown == 10 && HungerGames.getSettings().pregameBlockMovement) {
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "I tributi potranno muoversi in 10 secondi!");
			for (HGamer hGamer : HungerGames.getAllGamersUnsafe()) {
				if (hGamer.getStatus() == Status.TRIBUTE) {
					hGamer.teleportDismount(HungerGames.getHighestSpawn());
				}
			}
			movementBlocker = new MovementBlocker();
			Bukkit.getPluginManager().registerEvents(movementBlocker, HungerGames.getInstance());
		}
		
		
		if (countdown >= 60) {
			
			// Ogni 15 secondi
			if (countdown % 15 == 0) {
				lastCountdownMessage = UnitUtils.formatMinutes(countdown / 60);
				Bukkit.broadcastMessage(ChatColor.GREEN + "La partita inizia in " + lastCountdownMessage + ".");
				SidebarManager.setTime(lastCountdownMessage);
			}
			
		} else if (countdown > 10) {
			
			// Ogni 10 secondi
			if (countdown % 10 == 0) {
				lastCountdownMessage = UnitUtils.formatSeconds(countdown);
				Bukkit.broadcastMessage(ChatColor.GREEN + "La partita inizia in " + lastCountdownMessage + ".");
			}
			SidebarManager.setTime(UnitUtils.formatSeconds(countdown));
			
		} else {
			
			// Countdown finale
			clickSound.playToAll();
			lastCountdownMessage = UnitUtils.formatSeconds(countdown);
			Bukkit.broadcastMessage(ChatColor.GREEN + "La partita inizia in " + lastCountdownMessage + ".");
			SidebarManager.setTime(UnitUtils.formatSeconds(countdown));
			
		}

		countdown--;
	}
	
	private static class MovementBlocker implements Listener {
		
		@EventHandler
		public void onMove(PlayerMoveEvent event) {
			if (HungerGames.getHGamer(event.getPlayer()).getStatus() == Status.TRIBUTE && !isSamePosition(event.getFrom(), event.getTo())) {
				event.setTo(event.getFrom());
			}
		}
		
		
		private boolean isSamePosition(Location from, Location to) {
			return from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ();
		}
	}
}
