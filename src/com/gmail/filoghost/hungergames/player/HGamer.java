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

import java.util.Map;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wild.api.WildConstants;
import wild.api.bridges.CosmeticsBridge;
import wild.api.world.SpectatorAPI;

import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.event.PlayerStatusChangeEvent;
import com.gmail.filoghost.hungergames.hud.menu.TeleporterMenu;
import com.gmail.filoghost.hungergames.hud.sidebar.SidebarManager;
import com.gmail.filoghost.hungergames.hud.tags.TagsManager;
import com.gmail.filoghost.hungergames.tasks.GivePotionEffectTask;
import com.gmail.filoghost.hungergames.utils.PlayerUtils;
import com.gmail.filoghost.hungergames.utils.UnitUtils;
import com.google.common.collect.Maps;

public class HGamer {

	@Getter private 			Player player;
	@Getter private 			Status status;
	@Getter @Setter private 	Kit kit;
	@Getter @Setter private		Kit boughtKit;
	@Getter private				Team team;
	
	private 					Map<Skill, Long> skillsLastUsage;
	
	public HGamer(@NonNull Player bukkitPlayer, @NonNull Status status) {
		this.player = bukkitPlayer;
		skillsLastUsage = Maps.newHashMap();
		setStatus(status, false, false, true, false);
	}
	
	public String getName() {
		return player.getName();
	}
	
	public void sendMessage(String message) {
		player.sendMessage(message);
	}
	
	public boolean hasEmptyHand() {
		ItemStack inHand = player.getItemInHand();
		return inHand == null || inHand.getType() == Material.AIR;
	}
		
	public boolean hasInHand(Material mat) {
		ItemStack inHand = player.getItemInHand();
		return inHand != null && inHand.getType() == mat;
	}
	
	public boolean hasSkill(Skill skill) {
		if (kit == null || status != Status.TRIBUTE) return false;
		return kit.hasSkill(skill);
	}
	
	public boolean hasKit() {
		return kit != null;
	}
	
	public boolean hasBoughtKit() {
		return boughtKit != null;
	}
	
	public void showPlayer(HGamer other) {
		player.showPlayer(other.getPlayer());
	}
	
	public void hidePlayer(HGamer other) {
		player.hidePlayer(other.getPlayer());
	}
	
	public void teleportDismount(Location loc) {
		PlayerUtils.teleportDismount(player, loc);
	}
	
	public void teleportDismount(Entity entity) {
		PlayerUtils.teleportDismount(player, entity);
	}
	
	public void cleanCompletely(GameMode mode) {
		PlayerUtils.cleanCompletely(player, mode);
	}
	
	public void onRespawn() {
		switch (status) {
			
			case GAMEMAKER:
				cleanCompletely(GameMode.CREATIVE);
				giveSpectatorStuff();
				Bukkit.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), new GivePotionEffectTask(PotionEffectType.INVISIBILITY, player));
				Bukkit.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), new GivePotionEffectTask(PotionEffectType.NIGHT_VISION, player));
				break;
				
			case SPECTATOR:
				cleanCompletely(GameMode.CREATIVE);
				giveSpectatorStuff();
				Bukkit.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), new GivePotionEffectTask(PotionEffectType.INVISIBILITY, player));
				Bukkit.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), new GivePotionEffectTask(PotionEffectType.NIGHT_VISION, player));
				break;
				
			case TRIBUTE:
				cleanCompletely(GameMode.SURVIVAL);
				givePregameStuff();
				break;
		}
		
		sendMessage(ChatColor.YELLOW + "Ora sei " + status.getNameAndArticle() + "!");
	}
	
	public void setStatus(Status newStatus, boolean sendMessage, boolean updatePlayers, boolean cleanPlayer, boolean updateTeleporter) {
		
		if (newStatus == this.status) {
			if (sendMessage) sendMessage(ChatColor.RED + "Sei già " + status.getNameAndArticle() + "!");
			return;
		}
		
		this.status = newStatus;
		
		switch (newStatus) {
			
			case GAMEMAKER:
				if (cleanPlayer) {
					cleanCompletely(GameMode.CREATIVE);
					giveSpectatorStuff();
				}
				TagsManager.setGhost(player);
				player.spigot().setCollidesWithEntities(false);
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0), true);
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0), true);
				SpectatorAPI.setSpectator(player);
				break;
				
			case SPECTATOR:
				if (cleanPlayer) {
					cleanCompletely(GameMode.CREATIVE);
					giveSpectatorStuff();
				}
				TagsManager.setGhost(player);
				player.spigot().setCollidesWithEntities(false);
				player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0), true);
				player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0), true);
				SpectatorAPI.setSpectator(player);
				break;
				
			case TRIBUTE:
				if (cleanPlayer) {
					cleanCompletely(GameMode.SURVIVAL);
					givePregameStuff();
				}
				
				TagsManager.setTribute(player);
				player.spigot().setCollidesWithEntities(true);
				SpectatorAPI.removeSpectator(player);
				break;
		}
		
		VanishManager.updatePlayer(this);
		
		if (sendMessage) sendMessage(ChatColor.YELLOW + "Ora sei " + newStatus.getNameAndArticle() + "!");
		if (updatePlayers) SidebarManager.setPlayers(HungerGames.countTributes());
		if (updateTeleporter) TeleporterMenu.update();
		
		Bukkit.getPluginManager().callEvent(new PlayerStatusChangeEvent(player, status));
	}
	
	public void givePregameStuff() {
		HungerGames.getBookTutorial().giveTo(player);
		player.getInventory().addItem(HungerGames.getKitSelector());
		
		CosmeticsBridge.giveCosmeticsItems(player.getInventory());
		CosmeticsBridge.updateCosmetics(player, CosmeticsBridge.Status.LOBBY);
	}
	
	public void giveSpectatorStuff() {
		player.getInventory().addItem(WildConstants.Spectator.TELEPORTER);
		player.getInventory().setItem(8, WildConstants.Spectator.BACK_TO_HUB);
		
		CosmeticsBridge.updateCosmetics(player, CosmeticsBridge.Status.SPECTATOR);
	}
	
	public void createTeam() {
		team = new Team();
	}
	
	public boolean hasTeam() {
		return team != null;
	}
	
	public boolean canSeeOnCompass(HGamer other) {
		if (team == null) return true;
		return !team.contains(other.getName());
	}
	
	public boolean tryUse(Skill skill, int seconds, boolean sendMessage) {
		long cooldownMillis = seconds * 1000;
		long nowMillis = System.currentTimeMillis();
		
		Long lastUsage = skillsLastUsage.get(skill);
		
		if (lastUsage != null) {
			if (nowMillis > lastUsage + cooldownMillis) {
				// Può usarla
				skillsLastUsage.put(skill, nowMillis);
				return true;
			} else {
				if (sendMessage) {
					sendMessage(ChatColor.RED + "Potrai usare l'abilità tra " + UnitUtils.formatCooldown(lastUsage + cooldownMillis - nowMillis) + " secondi.");
				}
				return false;
			}
			
		} else {
			skillsLastUsage.put(skill, nowMillis);
			return true;
		}
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
