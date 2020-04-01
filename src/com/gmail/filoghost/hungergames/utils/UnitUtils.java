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
package com.gmail.filoghost.hungergames.utils;

import java.text.DecimalFormat;

import org.bukkit.entity.Player;

import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.Perms;

public class UnitUtils {

	public static String formatMinutes(int x) {
		return x == 1 ? "1 minuto" : x + " minuti";
	}
	
	public static String formatSeconds(int x) {
		return x == 1 ? "1 secondo" : x + " secondi";
	}
	
	public static String formatCoins(int x) {
		return x == 1 ? "1 Coin" : x + " Coins";
	}
	
	public static String formatSecondsAuto(int x) {
		if (x >= 60) {
			return formatMinutes(x / 60);
		} else {
			return formatSeconds(x);
		}
	}
	
	public static int getWinCoins(Player player) {
		int coins = HungerGames.getSettings().coins_win;
		
		if (player.hasPermission(Perms.DOUBLE_COINS_WIN)) {
			coins = coins * 2;
		}
		
		return coins;
	}
	
	private static DecimalFormat decimalFormat = new DecimalFormat("0.0");
	public static String formatCooldown(long millis) {
		return decimalFormat.format(millis / 1000.0);
	}
}
