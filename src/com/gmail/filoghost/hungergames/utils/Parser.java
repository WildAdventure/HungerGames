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

import lombok.NonNull;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.gmail.filoghost.hungergames.generation.RandomItem;

public class Parser {

	public static class Potions {
		
		public static PotionEffect parse(@NonNull String input, boolean ambient) throws ParserException {
			
			PotionEffectType type = null;
			int durationTicks = 0;
			int level = 1;
			
			input = input.replace(" ", ""); // Togli gli spazi.
			
			String[] splitDuration = input.split(",");
			
			if (splitDuration.length < 2) {
				throw new ParserException("durata non impostata");
			}
			
			input = splitDuration[0]; // Tieni la prima parte
			if (!MathUtils.isValidInteger(splitDuration[1])) {
				throw new ParserException("durata non valida (" + splitDuration[1] + ")");
			}
			
			durationTicks = Integer.parseInt(splitDuration[1]) * 20;
			
			if (durationTicks == 0) {
				throw new ParserException("durata non valida (" + durationTicks + ")");
			}
			
			if (durationTicks < 0) {
				durationTicks = Format.INFINITE_POTION_THREESHOLD;
			}
			
			// Leggi il livello (opzionale).
			String[] splitByColons = input.split(":");
			
			if (splitByColons.length > 1) {
				
				if (!MathUtils.isValidInteger(splitByColons[1])) {
					throw new ParserException("livello non valido (" + splitByColons[1] + ")");
				}
				
				level = Integer.parseInt(splitByColons[1]);
				if (level <= 0) {
					throw new ParserException("livello non valido (" + level + ")");
				}
				input = splitByColons[0]; // Tieni la prima parte
			}
			
			
			type = Matcher.Potions.match(input);
			
			if (type == null) {
				throw new ParserException("tipo di effetto non trovato");
			}
			
			return new PotionEffect(type, durationTicks, level - 1, ambient);
		}	
	}
	
	public static class ItemStacks {
		
		@SuppressWarnings("deprecation")
		public static ItemStack parse(@NonNull String input) throws ParserException {
			
			Material material = null;
			int amount = 1;
			short dataValue = 0;
			
			input = input.replace(" ", ""); // Remove spaces, they're not needed.
			
			// Read the optional amount.
			String[] splitAmount = input.split(",");
			
			if (splitAmount.length > 1) {
				
				if (!MathUtils.isValidInteger(splitAmount[1])) {
					throw new ParserException("quantità non valida");
				}
				
				amount = Integer.parseInt(splitAmount[1]);
				if (amount <= 0) throw new ParserException("quantità non valida");
				
				// Only keep the first part as input.
				input = splitAmount[0];
			}
			
			
			// Read the optional data value.
			String[] splitByColons = input.split(":");
			
			if (splitByColons.length > 1) {
				
				if (!MathUtils.isValidShort(splitByColons[1])) {
					throw new ParserException("data value non valido");
				}
				
				dataValue = Short.parseShort(splitByColons[1]);
				if (dataValue < 0) {
					throw new ParserException("data value non valido");
				}
				
				// Only keep the first part as input.
				input = splitByColons[0];
			}
			
			
			if (MathUtils.isValidInteger(input)) {
				material = Material.getMaterial(Integer.parseInt(input));
			} else {
				material = Matcher.Materials.match(input);
			}
			
			if (material == null || material == Material.AIR) {
				throw new ParserException("materiale non valido");
			}
			
			return new ItemStack(material, amount, dataValue);
		}
	}
	
	
	public static class Enchantments {

		public static EnchantmentData parse(@NonNull String input) throws ParserException {
			
			Enchantment enchant = null;
			int level = 1;
			
			input = input.replace(" ", ""); // Remove spaces, they're not needed.
			
			// Read the optional amount.
			String[] splitLevel = input.split(":");
			
			if (splitLevel.length > 1) {
				
				if (!MathUtils.isValidInteger(splitLevel[1])) {
					throw new ParserException("livello non valido");
				}
				
				level = Integer.parseInt(splitLevel[1]);
				if (level <= 0) throw new ParserException("livello non valido");
				
				// Only keep the first part as input.
				input = splitLevel[0];
			}
			
			
			enchant = Matcher.Enchantments.match(input);

			if (enchant == null) {
				throw new ParserException("incantesimo non trovato");
			}
			
			return new EnchantmentData(enchant, level);
		}
	}
	
	public static class Colors {
		
		public static Color parse(@NonNull String input) throws ParserException {
			
			String[] splitRGB = input.replace(" ", "").split(",");
			
			if (splitRGB.length != 3) {
				throw new ParserException("servono 3 colori");
			}
			
			try {
				
				int red = Integer.parseInt(splitRGB[0]);
				int green = Integer.parseInt(splitRGB[1]);
				int blue = Integer.parseInt(splitRGB[2]);
				
				if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
					throw new ParserException("numeri dei colori non validi");
				}
				
				return Color.fromRGB(red, green, blue);
				
			} catch (NumberFormatException ex) {
				throw new ParserException("numeri dei colori non validi");
			}
			
		}
	}
	
	public static class RandomItems {
		
		public static RandomItem parse(@NonNull String input) throws ParserException {
			
			String[] split = input.replace(" ", "").split(",");
			
			ItemStack item = ItemStacks.parse(split[0]);
			RandomItem randomItem = new RandomItem(item);
			
			if (split.length > 1) {
				
				int minAmount;
				int maxAmount;
				
				try {
					if (split[1].contains("-")) {
						String[] splitMinMax = split[1].split("-");
						minAmount = Integer.parseInt(splitMinMax[0]);
						maxAmount = Integer.parseInt(splitMinMax[1]);
					} else {
						minAmount = Integer.parseInt(split[1]);
						maxAmount = Integer.parseInt(split[1]);
					}
				} catch (NumberFormatException ex) {
					throw new ParserException("quantità non valida");
				}
				
				randomItem.setAmountMin(minAmount);
				randomItem.setAmountMax(maxAmount);
				
			}
			
			if (split.length > 2) {
				
				try {
					int percentage = Integer.parseInt(split[2].replace("%", ""));
					randomItem.setChanceToSpawn(percentage);					
				} catch (NumberFormatException ex) {
					throw new ParserException("percentuale non valida");
				}
			}
			
			if (split.length > 3) {
				
				if (split[3].equalsIgnoreCase("sameslot")) {
					randomItem.setSameSlot(true);
				}
			}
			
			return randomItem;
			
		}
	}
}
