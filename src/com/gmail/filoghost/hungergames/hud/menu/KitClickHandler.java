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
package com.gmail.filoghost.hungergames.hud.menu;

import java.sql.SQLException;

import lombok.AllArgsConstructor;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import wild.api.menu.ClickHandler;
import wild.api.sound.EasySound;

import com.gmail.filoghost.hungergames.GameState;
import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.Messages;
import com.gmail.filoghost.hungergames.mysql.SQLColumns;
import com.gmail.filoghost.hungergames.mysql.SQLManager;
import com.gmail.filoghost.hungergames.mysql.SQLTask;
import com.gmail.filoghost.hungergames.player.HGamer;
import com.gmail.filoghost.hungergames.player.Kit;
import com.gmail.filoghost.hungergames.utils.UnitUtils;

@AllArgsConstructor
public class KitClickHandler implements ClickHandler {
	
	private Kit kit;
	
	@Override
	public void onClick(Player player) {
		
		if (HungerGames.getState() != GameState.PRE_GAME) {
			player.sendMessage(ChatColor.YELLOW + "La partita è già iniziata!");
			return;
		}
		
		final HGamer hGamer = HungerGames.getHGamer(player);
		String extra = "";
		
		if (hGamer.hasKit() && hGamer.getKit().equals(kit)) {
			hGamer.sendMessage(ChatColor.YELLOW + "Hai già scelto questo kit.");
			return;
		}
		
		if (kit.getPermission() != null && !hGamer.getPlayer().hasPermission(kit.getPermission())) {
			hGamer.sendMessage(ChatColor.YELLOW + "Non hai il permesso per usare questo kit.");
			return;
		}
		
		if (!kit.isFree()) {
		
			if (hGamer.hasBoughtKit() && !hGamer.getBoughtKit().equals(kit)) {
				// Ha già un kit a pagamento
				hGamer.sendMessage(ChatColor.YELLOW + "Hai già comprato un altro kit per questa partita.");
				return;
			}
			
			if (!kit.equals(hGamer.getBoughtKit())) {
				// Se non l'ha già comprato e non ha coins
				int coins;
				try {
					coins = SQLManager.getStat(hGamer.getName(), SQLColumns.COINS);
				} catch (SQLException e) {
					e.printStackTrace();
					hGamer.sendMessage(Messages.DATABASE_ERROR);
					return;
				}
				
				if (coins < kit.getPrice()) {
					hGamer.sendMessage(ChatColor.YELLOW + "Ti Servono altri " + UnitUtils.formatCoins(kit.getPrice() - coins) + " per questo kit.");
					return;
				}
				
				final int coinsToTake = kit.getPrice();
				new SQLTask() {
					@Override
					public void execute() throws SQLException {
						SQLManager.decreaseStat(hGamer.getName(), SQLColumns.COINS, coinsToTake);
					}
				}.submitAsync(hGamer.getPlayer());
				
				hGamer.setBoughtKit(kit);
			}
		}
		
		EasySound.quickPlay(player, Sound.NOTE_PLING, 2.0f);
		hGamer.setKit(kit);
		hGamer.sendMessage(ChatColor.GRAY + "Hai scelto il kit " + kit.getColorPrefix() + kit.getName() + extra);
	}

	
	
}
