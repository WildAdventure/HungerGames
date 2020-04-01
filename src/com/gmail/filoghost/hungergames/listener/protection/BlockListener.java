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
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import com.gmail.filoghost.hungergames.GameState;
import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.Perms;
import com.gmail.filoghost.hungergames.generation.Cornucopia;
import com.gmail.filoghost.hungergames.player.Status;

public class BlockListener implements Listener {

	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		
		if (!canInteractWithBlocks(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		
		if (HungerGames.isMaterialProtected(event.getBlock().getType()) && !player.hasPermission(Perms.INTERACT_PROTECTED_MATERIALS)) {
			player.sendMessage(ChatColor.RED + "Non puoi distruggere questo tipo di blocco.");
			event.setCancelled(true);
			return;
		}
	
		if (Cornucopia.isFloorColumn(event.getBlock()) && !player.hasPermission(Perms.INTERACT_CORNUCOPIA)) {
			player.sendMessage(ChatColor.RED + "Questa zona è protetta.");
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		
		if (!canInteractWithBlocks(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		
		if (HungerGames.isMaterialProtected(event.getBlock().getType()) && !player.hasPermission(Perms.INTERACT_PROTECTED_MATERIALS)) {
			player.sendMessage(ChatColor.RED + "Non puoi piazzare questo tipo di blocco.");
			event.setCancelled(true);
			return;
		}
	
		if (Cornucopia.isFloorColumn(event.getBlock()) && !player.hasPermission(Perms.INTERACT_CORNUCOPIA)) {
			player.sendMessage(ChatColor.RED + "Questa zona è protetta.");
			event.setCancelled(true);
			return;
		}
	}
	
	
	/**
	 * 
	 *  Cose secondarie
	 *  
	 */
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBucketFill(PlayerBucketFillEvent event) {
		
		if (!canInteractWithBlocks(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		
		if (Cornucopia.isFloorColumn(event.getBlockClicked()) && !event.getPlayer().hasPermission(Perms.INTERACT_CORNUCOPIA)) {
			event.getPlayer().sendMessage(ChatColor.RED + "Questa zona è protetta.");
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBucketEmpty(PlayerBucketEmptyEvent event) {
		
		if (!canInteractWithBlocks(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}
		
		if (Cornucopia.isFloorColumn(event.getBlockClicked().getRelative(event.getBlockFace())) && !event.getPlayer().hasPermission(Perms.INTERACT_CORNUCOPIA)) {
			event.getPlayer().sendMessage(ChatColor.RED + "Questa zona è protetta.");
			event.setCancelled(true);
			return;
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onIgnite(BlockIgniteEvent event) {
		if (HungerGames.isBlockProtected(event.getBlock()) || HungerGames.getState() == GameState.PRE_GAME) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBurn(BlockBurnEvent event) {
		if (HungerGames.isBlockProtected(event.getBlock()) || HungerGames.getState() == GameState.PRE_GAME) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onExtend(BlockPistonExtendEvent event) {
		List<Block> affectedBlocks = event.getBlocks();
		for (Block affectedBlock : affectedBlocks) {
			if (HungerGames.isBlockProtected(affectedBlock)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onRetract(BlockPistonRetractEvent event) {
		if (event.isSticky() && HungerGames.isBlockProtected(event.getBlock().getRelative(event.getDirection(), 2))) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler (ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onExplode(EntityExplodeEvent event) {
		if (!HungerGames.getState().isBlockEditingAllowed()) {
			event.blockList().clear();
			return;
		}
		
		Iterator<Block> blockIterator = event.blockList().iterator();
		while (blockIterator.hasNext()) {
			if (HungerGames.isBlockProtected(blockIterator.next())) {
				blockIterator.remove();
			}
		}
	}
	
	private boolean canInteractWithBlocks(Player player) {
		
		Status status = HungerGames.getHGamer(player).getStatus();
		if (status == Status.SPECTATOR) {
			return false;
		} else if (status == Status.GAMEMAKER) {
			if (!player.hasPermission(Perms.INTERACT_GAMEMAKER)) {
				return false;
			}
		}
		
		if (!HungerGames.getState().isBlockEditingAllowed()) {
			if (HungerGames.getState() == GameState.PRE_GAME && player.hasPermission(Perms.INTERACT_PREGAME)) {
				// Autorizzato
			} else {
				return false;
			}
		}
		
		return true;
	}
}
