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

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wild.api.translation.Translation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Format {

	public static final String LORE_ABILITY = 			ChatColor.DARK_PURPLE + "●" + 	ChatColor.LIGHT_PURPLE;
	public static final String LORE_POTION_EFFECT = 	ChatColor.DARK_BLUE + 	"●" + 	ChatColor.BLUE;
	public static final String LORE_ITEM = 				ChatColor.DARK_GRAY + 	"●" + 	ChatColor.GRAY;
	
	public static final String LORE_AMOUNT = 			ChatColor.WHITE + "x";
	public static final String LORE_ENCHANT = 			ChatColor.WHITE + "" + ChatColor.ITALIC;
	
	public static final String KIT_FREE = 				ChatColor.GREEN + "";
	public static final String KIT_COINS = 				ChatColor.GOLD + "";
	
	public static String CHAT_ECONOMY = 				ChatColor.GOLD + "";
	public static String CHAT_GAME_BROADCAST = 			ChatColor.GREEN + "";
	public static String CHAT_INFO = 					ChatColor.GRAY + "";
	public static String CHAT_SKILL = 					ChatColor.DARK_AQUA + "";
	
	public static String formatAmount(int i) {
		if (i > 1) {
			return " " + LORE_AMOUNT + i;
		}
		
		return "";
	}
	
	public static String getRoman(int i) {
		switch (i) {
			case 1:		return "I";
			case 2:		return "II";
			case 3:		return "III";
			case 4:		return "IV";
			case 5:		return "V";
			case 6:		return "VI";
			case 7:		return "VII";
			case 8:		return "VIII";
			case 9:		return "IX";
			case 10:	return "X";
			default: 	return Integer.toString(i);
		}
	}
	
	public static String formatItemWrapper(ItemStackWrapper itemWrapper) {
		StringBuilder output = new StringBuilder();
		
		output.append(Format.LORE_ITEM);
		output.append(" ");
		
		if (itemWrapper.getOverrideDescription() != null) {
			output.append(itemWrapper.getOverrideDescription());
			return output.toString();
		}
		
		ItemStack item = itemWrapper.getItemStack();
		
		if (itemWrapper.getMaterialName() == null) {
			output.append(Translation.of(item.getType()));
		} else {
			output.append(itemWrapper.getMaterialName());
		}
		
		if (itemWrapper.getAfterMaterial() != null) {
			output.append(" ");
			output.append(itemWrapper.getAfterMaterial());
		}
		
		if (item.getAmount() > 1) {
			output.append(" ");
			output.append(Format.LORE_AMOUNT);
			output.append(item.getAmount());
		}
		
		if (!item.getEnchantments().isEmpty()) {
			
			List<String> pieces = Lists.newArrayList();
			
			for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
				pieces.add(Translation.of(entry.getKey()) + " " + getRoman(entry.getValue()));
			}
			
			output.append(" ");
			output.append(LORE_ENCHANT);
			output.append(StringUtils.join(pieces, ", "));
		}

		return output.toString();
	}
	
	public static int INFINITE_POTION_THREESHOLD = 1000000000;
	
	public static String formatPotionWrapper(PotionEffectWrapper potionWrapper) {
		PotionEffect potion = potionWrapper.getPotionEffect();
		StringBuilder output = new StringBuilder();
		
		output.append(Format.LORE_POTION_EFFECT);
		output.append(" ");
		output.append(Translation.of(potion.getType()));
		
		if (!uselessAmplifier.contains(potion.getType())) {
			output.append(" ");
			output.append(getRoman(potion.getAmplifier() + 1));
		}
		
		if (potion.getDuration() < INFINITE_POTION_THREESHOLD) {
			
			int seconds = potion.getDuration() / 20;
			int minutes = 0;
			
			if (seconds >= 60) {
				minutes = seconds / 60;
				seconds = seconds % 60;
			}
			
			output.append(" (");
			output.append(minutes);
			output.append(":");
			if (seconds < 10) output.append("0");
			output.append(seconds);
			output.append(")");
		}
		
		if (potionWrapper.getExtraData() != null) {
			output.append(" ");
			output.append(potionWrapper.getExtraData());
		}
		
		return output.toString();
	}
	
	public static List<String> formatPotionWrappers(List<PotionEffectWrapper> wrappers) {
		List<String> output = Lists.newArrayList();
		
		for (PotionEffectWrapper potionEffectWrapper : wrappers) {
			if (!potionEffectWrapper.isHidden()) {
				output.add(formatPotionWrapper(potionEffectWrapper));
			}
		}
		
		return output;
	}
	
	private static Set<PotionEffectType> uselessAmplifier = Sets.newHashSet(
			PotionEffectType.BLINDNESS,
			PotionEffectType.CONFUSION,
			PotionEffectType.FIRE_RESISTANCE,
			PotionEffectType.INVISIBILITY,
			PotionEffectType.NIGHT_VISION,
			PotionEffectType.WITHER
	);
}
