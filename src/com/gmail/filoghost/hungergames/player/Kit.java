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
package com.gmail.filoghost.hungergames.player;

import java.util.List;
import java.util.Set;

import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;

import wild.api.WildCommons;

import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.utils.EnchantmentData;
import com.gmail.filoghost.hungergames.utils.Format;
import com.gmail.filoghost.hungergames.utils.ItemStackWrapper;
import com.gmail.filoghost.hungergames.utils.JsonUtils;
import com.gmail.filoghost.hungergames.utils.LoreUtils;
import com.gmail.filoghost.hungergames.utils.Parser;
import com.gmail.filoghost.hungergames.utils.ParserException;
import com.gmail.filoghost.hungergames.utils.PlayerUtils;
import com.gmail.filoghost.hungergames.utils.PotionEffectWrapper;
import com.gmail.filoghost.hungergames.utils.UnitUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class Kit {

	@Getter private 	String name, permission;
	@Getter private 	int price;
	@Getter private 	int creationTimestampMinute;
	private 			Set<Skill> skills;
	private 			ItemStack helmet, chestplate, leggings, boots;
	private 			List<ItemStack> items;
	private 			List<PotionEffect> potionEffects;
	
	@Getter private		ItemStack menuIcon;
	@Getter private		List<String> menuDescription;
	
	private static JsonParser jsonParser = new JsonParser();
	
	public Kit(String name, YamlConfiguration config) {
		
		this.name = name;
		skills = Sets.newHashSet();
		items = Lists.newArrayList();
		potionEffects = Lists.newArrayList();
		menuDescription = Lists.newArrayList();
		
		price = config.getInt("coins");
		if (price < 0) {
			price = 0;
		}
		
		creationTimestampMinute = config.getInt("timestamp");
		
		// Permessi
		String permission = config.getString("permission");
		if (permission != null && !permission.isEmpty()) {
			this.permission = permission;
		}
		
		// Skill
		String skillsBundle = config.getString("skills");
		if (skillsBundle != null && !skillsBundle.isEmpty()) {
			String[] skills = skillsBundle.split(",");
			
			for (String skill : skills) {
				try {
					this.skills.add(Skill.valueOf(skill.trim().toUpperCase().replace(" ", "_")));
				} catch (IllegalArgumentException ex) {
					HungerGames.logPurple("Abilità nel kit '" + name + "' non trovata: " + skill.trim());
				}
			}
		}
		
		// Oggetti
		List<String> itemsBundles = config.getStringList("items");
		List<ItemStackWrapper> itemWrappersLocal = Lists.newArrayList();
		
		if (itemsBundles != null) {
			for (String itemBundle : itemsBundles) {
				
				ItemStack item;
				ItemStackWrapper itemWrapper;

				String jsonString = null;
				
				// Controllo per dati json
				if (JsonUtils.containsJson(itemBundle)) {
					jsonString = JsonUtils.extractJson(itemBundle);
					itemBundle = JsonUtils.removeJson(itemBundle);
				}
				
				try {
					item = Parser.ItemStacks.parse(itemBundle);
					itemWrapper = new ItemStackWrapper(item);
				} catch (ParserException e) {
					HungerGames.logPurple("Oggetto non valido nel kit '" + name + "' (" + e.getMessage() + "): " + itemBundle);
					continue;
				}
				
				if (jsonString != null) {
					try {
						JsonObject data = jsonParser.parse(jsonString).getAsJsonObject();
						
						// Incantesimi
						if (data.has("enchants")) {
							JsonArray enchantsArray = data.get("enchants").getAsJsonArray();
							for (JsonElement singleEnchant : enchantsArray) {
								
								try {
									EnchantmentData enchantmentData = Parser.Enchantments.parse(singleEnchant.getAsString());
									item.addUnsafeEnchantment(enchantmentData.getEnchant(), enchantmentData.getLevel());
								} catch (ParserException ex) {
									HungerGames.logPurple("Incantesimo non valido nel kit '" + name + "' (" + ex.getMessage() + "): " + singleEnchant.getAsString());
								}
							}
						}
						
						if (data.has("color") && item.getItemMeta() instanceof LeatherArmorMeta) {
							
							String colorString = data.get("color").getAsString();
							
							Color color = Parser.Colors.parse(colorString);
							
							LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) item.getItemMeta();
							leatherArmorMeta.setColor(color);
							item.setItemMeta(leatherArmorMeta);

						}
						
						if (data.has("materialExtra")) {
							itemWrapper.setAfterMaterial(data.get("materialExtra").getAsString());
						}
						
						if (data.has("materialName")) {
							itemWrapper.setMaterialName(data.get("materialName").getAsString());
						}
						
						if (data.has("helmet")) {
							itemWrapper.setForceHelmet(data.get("helmet").getAsBoolean());
						}
						
						if (data.has("description")) {
							itemWrapper.setOverrideDescription(WildCommons.color(data.get("description").getAsString()));
						}
						
					} catch (JsonParseException ex) {
						HungerGames.logPurple("JSON non valido nel kit '" + name + "' (" + ex.getMessage() + "): " + jsonString);
					} catch (Exception ex) {
						ex.printStackTrace();
						HungerGames.logPurple("Errore nel kit '" + name + "', eccezione non gestita");
					}
				}

				itemWrappersLocal.add(itemWrapper);
			}
			
			// Ora iteriamo e distribuiamo le cose negli slot
			for (ItemStackWrapper itemWrapper : itemWrappersLocal) {
				
				ItemStack item = itemWrapper.getItemStack();
				
				if ((PlayerUtils.isHelmet(item) || itemWrapper.isForceHelmet()) && helmet == null) {
					helmet = item;
				} else if (PlayerUtils.isChestplate(item) && chestplate == null) {
					chestplate = item;
				} else if (PlayerUtils.isLeggings(item) && leggings == null) {
					leggings = item;
				} else if (PlayerUtils.isBoots(item) && boots == null) {
					boots = item;
				} else {
					
					if (item.getAmount() > item.getMaxStackSize()) {
						int amount = item.getAmount();
						
						while (amount > item.getMaxStackSize()) {
							amount -= item.getMaxStackSize();
							
							ItemStack clone = item.clone();
							clone.setAmount(item.getMaxStackSize());
							items.add(clone);
						}
						
						item = item.clone();
						item.setAmount(amount);
					}
					
					items.add(item);
				}
			}
		}
		
		// Pozioni
		List<String> potionsBundles = config.getStringList("effects");
		List<PotionEffectWrapper> potionWrappersLocal = Lists.newArrayList();
		
		if (potionsBundles != null) {
			for (String potionBundle : potionsBundles) {
				
				String jsonString = null;
				if (JsonUtils.containsJson(potionBundle)) {
					jsonString = JsonUtils.extractJson(potionBundle);
					potionBundle = JsonUtils.removeJson(potionBundle);
				}
				
				try {
					
					PotionEffectWrapper effectWrapper = new PotionEffectWrapper(Parser.Potions.parse(potionBundle, true));
					
					if (jsonString != null) {
						JsonObject data = jsonParser.parse(jsonString).getAsJsonObject();
						
						if (data.has("hidden")) {
							effectWrapper.setHidden(data.get("hidden").getAsBoolean());
						}
						
						if (data.has("ignoreInvincibility")) {
							effectWrapper.setIgnoreInvincibility(data.get("ignoreInvincibility").getAsBoolean());
						}
						
						if (data.has("extra")) {
							effectWrapper.setExtraData(WildCommons.color(data.get("extra").getAsString()));
						}
					}
					
					potionWrappersLocal.add(effectWrapper);
					
				} catch (ParserException ex) {
					HungerGames.logPurple("Effetto non valido nel kit '" + name + "' (" + ex.getMessage() + "): " + potionBundle);
				} catch (JsonParseException ex) {
					HungerGames.logPurple("JSON non valido nel kit '" + name + "' (" + ex.getMessage() + "): " + jsonString);
				} catch (Exception ex) {
					ex.printStackTrace();
					HungerGames.logPurple("Errore nel kit '" + name + "', eccezione non gestita");
				}
			}
			
			for (PotionEffectWrapper potionWrapper : potionWrappersLocal) {
				potionEffects.add(potionWrapper.getPotionEffectWithInvincibility());
			}
		}
		
		
		// Descrizione nel menù
		List<String> menuLines = config.getStringList("description");
		if (menuLines != null && !menuLines.isEmpty()) {
			
			for (String line : menuLines) {
				menuDescription.add(
						WildCommons.color(line)
						.replace("-", Format.LORE_ITEM)
						.replace("*", Format.LORE_POTION_EFFECT)
						.replace("+", Format.LORE_ABILITY)
						.replace("[E]", Format.LORE_ENCHANT)
						.replace("[Q]", Format.LORE_AMOUNT)
				);
			}
		} else {
			// Generazione automatica
			
			if (price > 0) {
				menuDescription.add(ChatColor.GOLD + "Prezzo: " + UnitUtils.formatCoins(price));
			}
			
			
			if (!itemWrappersLocal.isEmpty()) {
				
				if (!menuDescription.isEmpty()) menuDescription.add("");
				
				for (ItemStackWrapper itemWrapper : itemWrappersLocal) {
					menuDescription.add(Format.formatItemWrapper(itemWrapper));
				}
			}
			
			if (!potionEffects.isEmpty()) {
				
				List<String> formattedPotionEffects = Format.formatPotionWrappers(potionWrappersLocal);
				
				if (!formattedPotionEffects.isEmpty()) {
					
					if (!menuDescription.isEmpty()) {
						menuDescription.add("");
					}
					
					menuDescription.addAll(formattedPotionEffects);
				}
			}
			
			if (!skills.isEmpty()) {
				
				if (!menuDescription.isEmpty()) menuDescription.add("");
				
				for (Skill skill : skills) {
					
					List<String> skillLines = LoreUtils.splitMultipleLines(skill.getDescription());
					skillLines.set(0, Format.LORE_ABILITY + " " + skillLines.get(0));
					for (int i = 1; i < skillLines.size(); i++) {
						skillLines.set(i, ChatColor.LIGHT_PURPLE + "  " + skillLines.get(i));
					}
					
					menuDescription.addAll(skillLines);
				}
			}
		}
		
		try {
			menuIcon = Parser.ItemStacks.parse(config.getString("icon"));
		} catch (ParserException e) {
			HungerGames.logPurple("Icona non valida nel kit " + name + ": " + e.getMessage());
			menuIcon = new ItemStack(Material.STAINED_GLASS, 1, (byte) 15);
		} catch (NullPointerException e) {
			HungerGames.logPurple("Icona non settata nel kit " + name);
			menuIcon = new ItemStack(Material.STAINED_GLASS, 1, (byte) 15);
		}
	}
	
	public boolean hasSkill(Skill skill) {
		return skills.contains(skill);
	}
	
	public void apply(Player player) {
		PlayerInventory inv = player.getInventory();
		
		if (helmet != null) {
			inv.setHelmet(helmet);
		}
		
		if (chestplate != null) {
			inv.setChestplate(chestplate);
		}
		
		if (leggings != null) {
			inv.setLeggings(leggings);
		}
		
		if (boots != null) {
			inv.setBoots(boots);
		}
		
		for (ItemStack item : items) {
			inv.addItem(item);
		}
		
		if (potionEffects.size() > 0) {
			for (PotionEffect potionEffect : potionEffects) {
				player.addPotionEffect(potionEffect, true);
			}
		}
	}
	
	public boolean isFree() {
		return price == 0;
	}

	public String getColorPrefix() {
		if (price > 0) {
			return Format.KIT_COINS;
		} else {
			return Format.KIT_FREE;
		}
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		Kit other = (Kit) obj;
		return other.name.equals(this.name);
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
