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
import org.bukkit.Location;
import com.gmail.filoghost.hungergames.GameState;
import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.generation.FinalBattle;
import com.gmail.filoghost.hungergames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.hungergames.player.HGamer;
import com.gmail.filoghost.hungergames.player.Status;
import com.gmail.filoghost.hungergames.utils.UnitUtils;

public class GameTimer extends TimerMaster {

	@Setter @Getter private int countdown;
	
	public GameTimer() {
		super(0, 20L);
		this.countdown = HungerGames.getSettings().gameMinutes * 60;
	}

	@Override
	public void run() {
		
		if (countdown <= 0) {
			
			Bukkit.broadcastMessage("");
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "Sta per iniziare la battaglia finale! Se entro un minuto non ci sarà un vincitore, il server si riavvierà.");
			
			Location battleLocation = HungerGames.getHighestSpawn().clone();
			battleLocation.setY(249);
			FinalBattle.build(battleLocation.getBlock());
			HungerGames.getHighestSpawn().setY(250.0);
			HungerGames.setState(GameState.FINAL_BATTLE);
			
			for (HGamer tribute : HungerGames.getAllGamersUnsafe()) {
				if (tribute.getStatus() == Status.TRIBUTE) {
					tribute.teleportDismount(HungerGames.getHighestSpawn());
				}
			}
			HungerGames.getFinalBattleTimer().startNewTask();
			stopTask();
			return;
		}
		
		// Se manca 1 minuto
		if (countdown == 60) {
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "Fra 1 minuto inizia la battaglia finale, i tributi verranno teletrasportati nell'arena.");
		}
		
		if (countdown == 10) {
			Bukkit.broadcastMessage(ChatColor.DARK_GREEN + "Fra 10 secondi inizia la battaglia finale, verrai teletrasportato nell'arena.");
		}
		
		SidebarManager.setTime(UnitUtils.formatSecondsAuto(countdown));
		countdown--;
	}
}
