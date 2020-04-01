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
package com.gmail.filoghost.hungergames.commands;

import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import wild.api.command.CommandFramework;
import com.gmail.filoghost.hungergames.GameState;
import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.mysql.SQLColumns;
import com.gmail.filoghost.hungergames.mysql.SQLManager;
import com.gmail.filoghost.hungergames.mysql.SQLTask;
import com.gmail.filoghost.hungergames.player.HGamer;
import com.gmail.filoghost.hungergames.utils.Format;
import com.gmail.filoghost.hungergames.utils.UnitUtils;

public class CoinCommand extends CommandFramework {

	public CoinCommand() {
		super(HungerGames.getInstance(), "coin", "coins");
	}

	@Override
	public void execute(final CommandSender sender, String label, String[] args) {
		
		if (args.length == 0) {
			final HGamer hGamer = HungerGames.getHGamer(CommandValidate.getPlayerSender(sender));
			new SQLTask() {
				@Override
				public void execute() throws SQLException {
					hGamer.sendMessage("");
					hGamer.sendMessage(Format.CHAT_ECONOMY + "Possiedi " + UnitUtils.formatCoins(SQLManager.getStat(hGamer.getName(), SQLColumns.COINS)) + ".");
					hGamer.sendMessage(Format.CHAT_INFO + "Guadagni " + ChatColor.WHITE + UnitUtils.formatCoins(HungerGames.getSettings().coins_kill) + ChatColor.GRAY + " per ogni uccisione, " + ChatColor.WHITE + UnitUtils.formatCoins(UnitUtils.getWinCoins(hGamer.getPlayer())) + ChatColor.GRAY + " per ogni vittoria.");
					hGamer.sendMessage(Format.CHAT_INFO + "Per mandare dei Coins, usa /coin send <giocatore> <coins>");
				}
			}.submitAsync(hGamer.getPlayer());
			return;
		}
		
		if (args[0].equalsIgnoreCase("send")) {
			final HGamer hGamer = HungerGames.getHGamer(CommandValidate.getPlayerSender(sender));
			CommandValidate.minLength(args, 3, "Utilizzo: /coin give <giocatore> <coins>");
			CommandValidate.isTrue(HungerGames.getState() == GameState.PRE_GAME, "Puoi inviare i Coins solo prima che inizi la partita.");
			
			final HGamer receiver = HungerGames.getHGamer(args[1]);
			final int amount = CommandValidate.getPositiveIntegerNotZero(args[2]);
			
			CommandValidate.notNull(receiver, "Quel giocatore non è online!");
			CommandValidate.isTrue(hGamer != receiver, "Non puoi mandarti Coins da solo!");
			
			new SQLTask() {
				@Override
				public void execute() throws SQLException {
					
					int senderCoins = SQLManager.getStat(hGamer.getName(), SQLColumns.COINS);
					
					if (senderCoins < amount) {
						hGamer.sendMessage(ChatColor.RED + "Non hai abbastanza Coins.");
						return;
					}
					
					SQLManager.decreaseStat(hGamer.getName(), SQLColumns.COINS, amount);
					SQLManager.increaseStat(receiver.getName(), SQLColumns.COINS, amount);
					
					hGamer.sendMessage(Format.CHAT_ECONOMY + "Hai mandato " + UnitUtils.formatCoins(amount) + " a " + receiver.getName());
					receiver.sendMessage(Format.CHAT_ECONOMY + "Hai ricevuto " + UnitUtils.formatCoins(amount) + " da " + hGamer.getName());
				}
			}.submitAsync(hGamer.getPlayer());
			return;
		}
		
		if (args[0].equalsIgnoreCase("give")) {
			CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Questo comando può essere usato solo da console.");
			CommandValidate.minLength(args, 3, "Utilizzo: /coin give <giocatore> <coins>");
			
			final String playerName = args[1];
			final int coins = CommandValidate.getPositiveIntegerNotZero(args[2]);

			new SQLTask() {
				@Override
				public void execute() throws SQLException {

					if (!SQLManager.playerExists(playerName)) {
						sender.sendMessage(ChatColor.RED + "Quel giocatore non ha mai giocato qui!");
						return;
					}
						
					SQLManager.increaseStat(playerName, SQLColumns.COINS, coins);
					sender.sendMessage(ChatColor.YELLOW + "Sono stati mandati " + coins + " coins a " + playerName);
				}
			}.submitAsync(sender);
			return;
		}
		
		
		if (args[0].equalsIgnoreCase("set")) {
			CommandValidate.isTrue(sender instanceof ConsoleCommandSender, "Questo comando può essere usato solo da console.");
			CommandValidate.minLength(args, 3, "Utilizzo: /coin set <giocatore> <coins>");
			
			final String playerName = args[1];
			final int coins = CommandValidate.getPositiveInteger(args[2]);
			
			new SQLTask() {
				@Override
				public void execute() throws SQLException {

					if (!SQLManager.playerExists(playerName)) {
						sender.sendMessage(ChatColor.RED + "Quel giocatore non ha mai giocato qui!");
						return;
					}
							
					SQLManager.setStat(playerName, SQLColumns.COINS, coins);
					sender.sendMessage(ChatColor.YELLOW + "Sono stati settati " + coins + " coins a " + playerName);
				}
			}.submitAsync(sender);
			return;
		}
		
		sender.sendMessage(ChatColor.RED + "Sub-comando sconosciuto. Scrivi /coin per una lista dei comandi.");
	}

}
