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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import wild.api.sound.EasySound;

import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.hungergames.utils.UnitUtils;

public class FinalBattleTimer extends TimerMaster {

	private int countdown = 70;
	
	private EasySound plingSound = new EasySound(Sound.ORB_PICKUP, 1.5f);
	private EasySound clickSound = new EasySound(Sound.CLICK);
	private EasySound anvilSound = new EasySound(Sound.ANVIL_LAND);
	
	Listener damageListener;
	
	public FinalBattleTimer() {
		super(0, 20L);
	}

	@Override
	public void startNewTask() {
		super.startNewTask();
		
		damageListener = new DamageListener();
		Bukkit.getPluginManager().registerEvents(damageListener, HungerGames.getInstance());
	}


	@Override
	public void run() {
		
		if (countdown <= 0) {
			
			HungerGames.stopServer(ChatColor.RED + "Il tempo è scaduto, nessuno ha vinto la battaglia finale.");
		}
		
		if (countdown > 60) {
			
			Bukkit.broadcastMessage(ChatColor.GREEN + "Potrai combattere in " + UnitUtils.formatSeconds(countdown - 60) + ".");
			if (countdown <= 65) {
				clickSound.playToAll();
			}
			
		} else if (countdown == 60) {

			Bukkit.broadcastMessage(ChatColor.GREEN + "Ora puoi combattere!");
			EntityDamageEvent.getHandlerList().unregister(damageListener);
			anvilSound.playToAll();
			
		} else if (countdown > 10) {
			
			if (countdown < 60 && countdown % 10 == 0) {
				Bukkit.broadcastMessage(ChatColor.GREEN + "La battaglia finale termina in " + UnitUtils.formatSeconds(countdown) + ".");
			}
				
		} else {
			
			plingSound.playToAll();
			Bukkit.broadcastMessage(ChatColor.GREEN + "La battaglia finale termina in " + UnitUtils.formatSeconds(countdown) + ".");
			
		}

		SidebarManager.setTime(UnitUtils.formatSeconds(countdown));
		countdown--;
	}
	
	private static class DamageListener implements Listener {
		
		@EventHandler (priority = EventPriority.LOWEST)
		public void onDamage(EntityDamageEvent event) {
			event.setCancelled(true);
		}
		
	}
}
