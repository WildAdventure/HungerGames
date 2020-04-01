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

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.gmail.filoghost.hungergames.GameState;
import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.hud.menu.KitMenuManager;
import com.gmail.filoghost.hungergames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.hungergames.player.HGamer;
import com.gmail.filoghost.hungergames.player.Status;
import com.gmail.filoghost.hungergames.utils.PlayerUtils;

public class InventoryListener implements Listener {
	
	DecimalFormat decimalFormat = new DecimalFormat("0.0");
	
	@EventHandler (priority = EventPriority.HIGH, ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent event) {
		if (isRightClick(event.getAction()) && event.hasItem()) {
			
			Material type = event.getItem().getType();
			
			if (type == Material.EMERALD) {
				
				if (HungerGames.getState() == GameState.PRE_GAME) {
					KitMenuManager.open(event.getPlayer());
				}
				
			}else if (type == Material.COMPASS) {
				
				HGamer hGamer = HungerGames.getHGamer(event.getPlayer());
				
				if (hGamer.getStatus() == Status.TRIBUTE) {
					Player nearest = PlayerUtils.getCompassNearestTribute(hGamer);
					
					if (nearest != null) {
						hGamer.sendMessage(ChatColor.GREEN + "Giocatore: " + nearest.getName() + ChatColor.DARK_GRAY + " | " + ChatColor.GREEN + "Distanza: " + decimalFormat.format(nearest.getLocation().distance(event.getPlayer().getLocation())));
					} else {
						hGamer.sendMessage(ChatColor.GREEN + "Non ci sono giocatori vicino, la bussola punta allo spawn.");
					}
				} else {
					
					TeleporterMenu.open(event.getPlayer());
				}
			} else if (type == Material.BED) {
				
				HGamer hGamer = HungerGames.getHGamer(event.getPlayer());
				
				if (hGamer.getStatus() != Status.TRIBUTE) {
					hGamer.getPlayer().kickPlayer("Hai scelto di uscire." + "§0§0§0");
				}
			}
		}
	}
	
	private boolean isRightClick(Action action) {
		return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
	}

}
