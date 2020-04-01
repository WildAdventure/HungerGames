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
package com.gmail.filoghost.hungergames.listener.protection;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wild.api.WildCommons;

import com.gmail.filoghost.hungergames.GameState;
import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.Perms;
import com.gmail.filoghost.hungergames.player.HGamer;
import com.gmail.filoghost.hungergames.player.Status;
import com.gmail.filoghost.hungergames.utils.PlayerUtils;
public class EntityListener implements Listener {

	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onEntityDamage(EntityDamageEvent event) {
		
		if (event instanceof EntityDamageByEntityEvent) {
			// E' l'altro metodo a gestirla
			return;
		}
		
		if (event.getEntityType() == EntityType.PLAYER) {
			if (!HungerGames.getState().allowPlayerDamage()) {
				event.setCancelled(true);
			}
		} else {
			if (!HungerGames.getState().allowMobDamage()) {
				event.setCancelled(true);
			}
		}
		
		if (event.isCancelled()) {
			if (event.getCause() == DamageCause.FIRE_TICK || event.getCause() == DamageCause.FIRE) {
				event.getEntity().setFireTicks(0);

			} else if (event.getCause() == DamageCause.POISON) {
				((LivingEntity) event.getEntity()).removePotionEffect(PotionEffectType.POISON);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		
		if (event.getEntity().getType() == EntityType.PLAYER) {
			// Se il defender è un giocatore
			
			if (!HungerGames.getState().allowPlayerDamage()) {
				event.setCancelled(true);
				return;
			}
			
			if (event.getDamager().getType() == EntityType.PLAYER && !canInteractWithWorld((Player) event.getDamager())) {
				event.setCancelled(true);
			}
			
		} else {
			// Se il defender è un mob
			
			if (!HungerGames.getState().allowMobDamage()) {
				event.setCancelled(true);
				return;
			}
			
			if (event.getDamager().getType() == EntityType.PLAYER && !canInteractWithWorld((Player) event.getDamager())) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onHangingBreak(HangingBreakEvent event) {
		
		if (!HungerGames.getState().allowMobDamage()) {
			event.setCancelled(true);
			return;
		}
		
		if (event instanceof HangingBreakByEntityEvent) {
			HangingBreakByEntityEvent eventByEntity = (HangingBreakByEntityEvent) event;
			
			if (eventByEntity.getRemover() != null && eventByEntity.getRemover().getType() == EntityType.PLAYER) {
				
				if (!canInteractWithWorld((Player) eventByEntity.getRemover())) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onHangingPlace(HangingPlaceEvent event) {
		
		if (!canInteractWithWorld(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (event.getEntity().getShooter() instanceof Player && !canInteractWithWorld(PlayerUtils.getOnlineShooter(event.getEntity()), true)) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onItemPickup(PlayerPickupItemEvent event) {
		if (!canInteractWithWorld(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onCreativeItem(InventoryCreativeEvent event) {
		HGamer hGamer = HungerGames.getHGamer((Player) event.getWhoClicked());
		if (hGamer.getStatus() == Status.SPECTATOR) {
			event.setCancelled(true);
		}
	}

	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST) // Lowest perché previene il danno
	public void onPotionSplash(PotionSplashEvent event) {
		
		if (HungerGames.getState() == GameState.PRE_GAME) {
			event.setCancelled(true);
			return;
		}
		
		boolean isBad = false;
		for (PotionEffect effect : event.getPotion().getEffects()) {
			if (WildCommons.isBadPotionEffect(effect.getType())) {
				isBad = true;
			}
		}
		
		if (!isBad) return; // Se è buono ok
		
		Iterator<LivingEntity> iter = event.getAffectedEntities().iterator();
		while(iter.hasNext()) {
			
			LivingEntity entity = iter.next();
			
			if (entity.getType() == EntityType.PLAYER && !HungerGames.getState().allowPlayerDamage()) {
				iter.remove();
			} else if (!HungerGames.getState().allowMobDamage()) {
				iter.remove();
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onEnderpearl(PlayerTeleportEvent event) {
		if (event.getCause() == TeleportCause.ENDER_PEARL && HungerGames.getState() == GameState.FINAL_BATTLE) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "Non puoi usarla ora!");
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onInteract(PlayerInteractEvent event) {
		if (!canInteractWithWorld(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onInteractEntity(PlayerInteractEntityEvent event) {
		if (!canInteractWithWorld(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onDrop(PlayerDropItemEvent event) {
		if (!canInteractWithWorld(event.getPlayer())) {
			event.setCancelled(true);
		}
	}
	
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onFoodLevelChange(FoodLevelChangeEvent event) {
		if (HungerGames.getState() == GameState.PRE_GAME) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onEntityTarget(EntityTargetEvent event) {
		if (HungerGames.getState() == GameState.PRE_GAME) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onItemSpawn(ItemSpawnEvent event) {
		if (HungerGames.getState() == GameState.PRE_GAME) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBedEnter(PlayerBedEnterEvent event) {
		event.setCancelled(true);
	}
	
	private boolean canInteractWithWorld(Player player) {
		return canInteractWithWorld(player, false);
	}
	
	private boolean canInteractWithWorld(Player player, boolean allowInPregame) {
		
		if (player == null) {
			return false;
		}
		
		Status status = HungerGames.getHGamer(player).getStatus();
				
		if (status == Status.SPECTATOR) {
			return false;
		} else if (status == Status.GAMEMAKER) {
			if (!player.hasPermission(Perms.INTERACT_GAMEMAKER)) {
				return false;
			}
		}
		
		if (!allowInPregame && HungerGames.getState() == GameState.PRE_GAME && !player.hasPermission(Perms.INTERACT_PREGAME)) {
			return false;
		}
		
		return true;
	}
}
