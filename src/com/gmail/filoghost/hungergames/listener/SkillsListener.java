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
package com.gmail.filoghost.hungergames.listener;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import wild.api.WildCommons;
import wild.api.sound.EasySound;
import wild.api.translation.Translation;
import wild.api.world.Particle;

import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.Messages;
import com.gmail.filoghost.hungergames.files.SkillsLang;
import com.gmail.filoghost.hungergames.player.HGamer;
import com.gmail.filoghost.hungergames.player.Skill;
import com.gmail.filoghost.hungergames.player.Status;
import com.gmail.filoghost.hungergames.tasks.ClearHandTask;
import com.gmail.filoghost.hungergames.tasks.RemoveEntityTask;
import com.gmail.filoghost.hungergames.tasks.SlenderTeleportTask;
import com.gmail.filoghost.hungergames.utils.Format;
import com.gmail.filoghost.hungergames.utils.PlayerUtils;
import com.gmail.filoghost.hungergames.utils.UnitUtils;
import com.google.common.collect.Sets;

public class SkillsListener implements Listener {
	
	private EasySound abilityActivationSound = new EasySound(Sound.ORB_PICKUP);
	private DecimalFormat optionalDecimalFormat = new DecimalFormat("#.#");
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		
		if (!HungerGames.getState().allowSkills()) return;
		Action action = event.getAction();
		
		if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
			
			if (!event.hasItem()) return;
			Material itemType = event.getItem().getType();
			
			if (itemType == Material.STONE_AXE && event.hasBlock()) { // Bisogna cliccare un blocco
				HGamer hGamer = HungerGames.getHGamer(event.getPlayer());
				
				if (hGamer.hasSkill(Skill.THOR_AXE)) {
					
					if (hGamer.tryUse(Skill.THOR_AXE, 10, true)) {
						event.getPlayer().getWorld().strikeLightning(event.getClickedBlock().getLocation());
					}
				}
				
			} else if (itemType == Material.APPLE) {
				HGamer hGamer = HungerGames.getHGamer(event.getPlayer());
				
				if (hGamer.hasSkill(Skill.APPLE_NINJA)) {
					
					if (hGamer.tryUse(Skill.APPLE_NINJA, 30, true)) {
						hGamer.sendMessage(Format.CHAT_SKILL + "Sei invisibile per 5 secondi!");
						removeOneItem(event.getPlayer());
						hGamer.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 5 * 20, 0), true);
					}
				}
			} else if (itemType == Material.WATCH) {
				HGamer hGamer = HungerGames.getHGamer(event.getPlayer());

				if (hGamer.hasSkill(Skill.WATCH_STOPPER)) {
					if (HungerGames.getState().allowPlayerDamage()) {
						if (hGamer.tryUse(Skill.WATCH_STOPPER, 30, true)) {
							int durationTicks = SkillsLang.watchStopper_duration * 20;
							for (HGamer near : HungerGames.getNearTributes(hGamer.getPlayer(), SkillsLang.watchStopper_radius)) {
								near.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, durationTicks, 3), true);
								near.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, durationTicks, 1), true);
								near.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, durationTicks, 128), true);
								near.sendMessage(Format.CHAT_SKILL + "Sei stato rallentato!");
							}
							
							hGamer.sendMessage(Format.CHAT_SKILL + "Hai rallentato i giocatori entro " + SkillsLang.watchStopper_radius + " blocchi per " + UnitUtils.formatSeconds(SkillsLang.watchStopper_duration) + "!");
						}
					} else {
						hGamer.sendMessage(Messages.SKILL_NOT_ALLOWED_YET);
					}
				}
				
			} else if (itemType == Material.WORKBENCH) {
				if (HungerGames.getHGamer(event.getPlayer()).hasSkill(Skill.CRAFTER)) {
					event.setCancelled(true);
					event.getPlayer().openWorkbench(event.getPlayer().getLocation(), true);
				}
				
			} else if (event.hasBlock() && itemType == Material.MONSTER_EGG && event.getItem().getDurability() == 100) {
				
				if (HungerGames.getHGamer(event.getPlayer()).hasSkill(Skill.HORSE_TAMER)) {
						
					event.setCancelled(true);
					removeOneItem(event.getPlayer());

					Horse horse = event.getPlayer().getWorld().spawn(event.getPlayer().getLocation(), Horse.class);
					horse.setVariant(Horse.Variant.HORSE);
					horse.setCarryingChest(false);
					horse.setMaxHealth(30.0);
					horse.setHealth(30.0);
					horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
					horse.getInventory().setArmor(new ItemStack(Material.GOLD_BARDING));
					horse.setColor(Horse.Color.values()[HungerGames.getRandomGenerator().nextInt(Horse.Color.values().length)]);
					horse.setStyle(Horse.Style.values()[HungerGames.getRandomGenerator().nextInt(Horse.Style.values().length)]);
					horse.setJumpStrength(0.75);
					horse.setOwner(event.getPlayer());
					horse.setPassenger(event.getPlayer());
				}
				
			} else if (itemType == Material.BOOK) {
				HGamer hGamer = HungerGames.getHGamer(event.getPlayer());
				
				if (hGamer.hasSkill(Skill.INSPECTOR)) {
					Player nearest = PlayerUtils.getCompassNearestTribute(hGamer);
					if (nearest != null) {
						
						String itemInHandName = "Nessuno";
						if (nearest.getItemInHand() != null) {
							itemInHandName = Translation.of(nearest.getItemInHand().getType());
						}
						
						String keyColor = ChatColor.WHITE.toString();
						String valueColor = ChatColor.GRAY.toString();
						
						hGamer.sendMessage("");
						hGamer.sendMessage(ChatColor.GREEN + "----- Giocatore: " + nearest.getName() + " -----");
						hGamer.sendMessage(keyColor + "Salute: " + valueColor + optionalDecimalFormat.format(((Damageable) nearest).getHealth()) + " / " + optionalDecimalFormat.format(((Damageable) nearest).getMaxHealth()));
						hGamer.sendMessage(keyColor + "Armatura: " + valueColor + PlayerUtils.getArmorLevel(nearest) + " / 10");
						hGamer.sendMessage(keyColor + "Cibo: " + valueColor + nearest.getFoodLevel() + " / 20");
						hGamer.sendMessage(keyColor + "Oggetto in mano: " + valueColor + itemInHandName);
						
					} else {
						event.getPlayer().sendMessage(ChatColor.GREEN + "Non ci sono giocatori vicini.");
					}
				}
				
			} else if (itemType == Material.SLIME_BALL) {
				
				if (HungerGames.getHGamer(event.getPlayer()).hasSkill(Skill.WEB_SHOOTER)) {
					
					if (HungerGames.getState().allowPlayerDamage()) {
						Snowball web = event.getPlayer().launchProjectile(Snowball.class);
						web.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.5));
						web.setMetadata("Ragnatela", new FixedMetadataValue(HungerGames.getInstance(), true));
						removeOneItem(event.getPlayer());
						EasySound.quickPlay(event.getPlayer(), Sound.SPIDER_IDLE, 1.6F);
					} else {
						event.getPlayer().sendMessage(Messages.SKILL_NOT_ALLOWED_YET);
					}
				}
			} else if (itemType == Material.NETHER_STAR) {
				HGamer hGamer = HungerGames.getHGamer(event.getPlayer());
				
				if (hGamer.hasSkill(Skill.SLENDERMAN)) {
					
					if (HungerGames.getState().allowPlayerDamage()) {

						Collection<HGamer> nearTributes = HungerGames.getNearTributes(hGamer.getPlayer(), 50);

						if (nearTributes.size() > 0) {
								
							if (hGamer.tryUse(Skill.SLENDERMAN, 15, true)) {
								Location slenderLocation = hGamer.getPlayer().getLocation();
								double minDistanceSquared = 0.0;
								HGamer nearest = null;
									
								for (HGamer nearTribute : nearTributes) {
									double distanceSquared = nearTribute.getPlayer().getLocation().distanceSquared(slenderLocation);
										
									if (minDistanceSquared == 0.0 || distanceSquared < minDistanceSquared) {
										nearest = nearTribute;
										minDistanceSquared = distanceSquared;
									}
								}
								
								EasySound.quickPlay(nearest.getPlayer(), Sound.WITHER_SPAWN, 1F, 2F);
								removeOneItem(event.getPlayer());
								Bukkit.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), new SlenderTeleportTask(event.getPlayer(), nearest.getPlayer()), 10L);
							}
							
						} else {
							hGamer.sendMessage(ChatColor.RED + "Non ci sono giocatori in un raggio di 50 blocchi.");
						}
					} else {
						hGamer.sendMessage(Messages.SKILL_NOT_ALLOWED_YET);
					}
				}
			}
			
		} else if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			
			if (!event.hasItem()) return;
			Material itemType = event.getItem().getType();
			HGamer hGamer = HungerGames.getHGamer(event.getPlayer());
			
			if (itemType == Material.FIREBALL && hGamer.hasSkill(Skill.FIREBALL_SHOOTER)) {
				
				if (hGamer.tryUse(Skill.FIREBALL_SHOOTER, 3, true)) {
					Fireball fireball = event.getPlayer().launchProjectile(Fireball.class);
					fireball.setVelocity(event.getPlayer().getLocation().getDirection().multiply(1.5));
					EasySound.quickPlay(event.getPlayer(), Sound.FIRE);
					removeOneItem(event.getPlayer());
				}
			}

		} else if (action == Action.PHYSICAL) {
			
			if (!event.hasBlock()) return;
			if (!HungerGames.getState().allowPlayerDamage()) return;
			Block block = event.getClickedBlock();
			
			if (block.getType() == Material.STONE_PLATE) {
				HGamer hStepper = HungerGames.getHGamer(event.getPlayer());

				if (hStepper.getStatus() == Status.TRIBUTE && block.getRelative(BlockFace.DOWN).getType() == Material.GRAVEL && block.hasMetadata("Mina") && !hStepper.hasSkill(Skill.MINES)) {

					block.removeMetadata("Mina", HungerGames.getInstance());
					hStepper.sendMessage(Format.CHAT_SKILL + "** Hai calpestato una mina! **");
					block.setType(Material.AIR);
					block.getWorld().createExplosion(block.getLocation(), 0.0F);
					hStepper.getPlayer().damage(14.0 + HungerGames.getRandomGenerator().nextInt(3)); // 7-8 cuori
				}
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if (event.getEntityType() == EntityType.PLAYER) {
			
			Player player = (Player) event.getEntity();
			DamageCause cause = event.getCause();
			
			if (cause == DamageCause.FALL && HungerGames.getHGamer(player).hasSkill(Skill.STOMPER)) {
				
				double damage = event.getDamage();
				abilityActivationSound.playTo(player);
				
				for (HGamer other : HungerGames.getNearTributes(player, 5)) {
					other.getPlayer().damage(damage, player); // Damage from the current player
				}
				
				if (damage > 4.0) {
					event.setDamage(4.0);
				}
				
			} else if (cause == DamageCause.LIGHTNING && HungerGames.getHGamer(player).hasSkill(Skill.THOR_AXE)) {
				
				event.setCancelled(true);
				
			} else if ((cause == DamageCause.FIRE || cause == DamageCause.FIRE_TICK) && HungerGames.getHGamer(player).hasSkill(Skill.FIRE_STRENGTH)) {
				
				event.setCancelled(true);
				player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 5 * 20 + 10, 1), true);
				
			} else if (cause == DamageCause.POISON && HungerGames.getHGamer(player).hasSkill(Skill.NO_POISON)) {
				event.setCancelled(true);
				player.removePotionEffect(PotionEffectType.POISON);
			}
		}
	}
	
	
	@EventHandler (ignoreCancelled = true)
	public void onDamageByEntity(EntityDamageByEntityEvent event) {
		
		// Fix per un bug
		if (event.getEntity() instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) event.getEntity();
			if (livingEntity.getNoDamageTicks() > livingEntity.getMaximumNoDamageTicks() / 2F) return;
		}
		
		if (event.getDamager().getType() == EntityType.SNOWBALL && event.getDamager().hasMetadata("Esplosiva") && HungerGames.getState().allowPlayerDamage()) {
			
			event.getDamager().getWorld().createExplosion(event.getEntity().getLocation(), 0.0F);
			Collection<HGamer> nearTributes = HungerGames.getNearTributes(event.getEntity().getLocation(), 3.0, PlayerUtils.getOnlineShooter((Projectile) event.getDamager()));
			
			for (HGamer nearTribute : nearTributes) {
				nearTribute.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
				nearTribute.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 8 * 20, 0));
			}
		}
		
		if (event.getDamager().getType() == EntityType.SNOWBALL && event.getDamager().hasMetadata("Ragnatela")) {
			Set<Block> webBlocks = Sets.newHashSet();
			Block webCenter = event.getEntity().getLocation().add(0, 1, 0).getBlock();
			
			int x = webCenter.getX();
			int y = webCenter.getY();
			int z = webCenter.getZ();
			
			webBlocks.add(webCenter);
			webBlocks.add(webCenter.getWorld().getBlockAt(x - 1, y + 1, z - 1));
			webBlocks.add(webCenter.getWorld().getBlockAt(x + 1, y + 1, z - 1));
			webBlocks.add(webCenter.getWorld().getBlockAt(x - 1, y + 1, z + 1));
			webBlocks.add(webCenter.getWorld().getBlockAt(x + 1, y + 1, z + 1));
			
			webBlocks.add(webCenter.getWorld().getBlockAt(x - 1, y - 1, z - 1));
			webBlocks.add(webCenter.getWorld().getBlockAt(x + 1, y - 1, z - 1));
			webBlocks.add(webCenter.getWorld().getBlockAt(x - 1, y - 1, z + 1));
			webBlocks.add(webCenter.getWorld().getBlockAt(x + 1, y - 1, z + 1));

			for (Block webBlock : webBlocks) {
				if (webBlock.getType() == Material.AIR) {
					webBlock.setType(Material.WEB);
				}
			}
			
			event.setCancelled(true);
		}
		
		// La vittima è un giocatore
		if (event.getEntityType() == EntityType.PLAYER) {
			
			Entity damagerEntity = event.getDamager();
			Player victim = (Player) event.getEntity();
			
			if (damagerEntity.getType() == EntityType.PLAYER || (damagerEntity instanceof Projectile && PlayerUtils.getOnlineShooter((Projectile) damagerEntity) != null)) {
				
				if (((Damageable) victim).getHealth() <= 10.0) {
					HGamer hVictim = HungerGames.getHGamer(victim);
					if (hVictim.hasSkill(Skill.RUSH) && hVictim.tryUse(Skill.RUSH, 30, false)) {
						abilityActivationSound.playTo(victim);
						victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1, false), true);
						victim.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 10 * 20, 0, false), true);
					}
				}
			}
			
			// Il damager è un giocatore
			if (damagerEntity.getType() == EntityType.PLAYER) {
				
				HGamer hDamager = HungerGames.getHGamer((Player) damagerEntity);
				
				if (hDamager.hasInHand(Material.IRON_HOE) && hDamager.hasSkill(Skill.WITHER) && HungerGames.getRandomGenerator().nextDouble() <= 0.20) {
					// 8 secondi = 2 cuori di danno
					victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 10 * 20 + 10, 0), true);
					victim.sendMessage(Format.CHAT_SKILL + "Sei stato colpito dall'abilità Wither!");
					hDamager.sendMessage(Format.CHAT_SKILL + "Hai attivato l'abilità Wither!");
				}
				
				if (hDamager.hasSkill(Skill.POISON_CHANCE) && HungerGames.getRandomGenerator().nextDouble() <= 0.20) {
					
					victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 3 * 20 + 10, 0), true);
					victim.sendMessage(Format.CHAT_SKILL + "Sei stato avvelenato!");
					hDamager.sendMessage(Format.CHAT_SKILL + "Hai avvelenato il nemico!");
				}
				
				if (hDamager.hasInHand(Material.BLAZE_ROD) && victim.getItemInHand() != null && hDamager.hasSkill(Skill.THIEF) && HungerGames.getRandomGenerator().nextDouble() <= 0.15) {
					
					ItemStack stolen = victim.getItemInHand();
					victim.setItemInHand(null);
					abilityActivationSound.playTo(hDamager.getPlayer());
					
					ItemStack remainingRods = hDamager.getPlayer().getItemInHand();
					if (remainingRods.getAmount() > 1) {
						remainingRods.setAmount(remainingRods.getAmount() - 1);
						hDamager.getPlayer().getInventory().addItem(remainingRods);
					}
					
					hDamager.getPlayer().setItemInHand(stolen);
					hDamager.sendMessage(Format.CHAT_SKILL + "Hai rubato l'oggetto in mano al nemico!");
					victim.sendMessage(Format.CHAT_SKILL + "Il nemico ti ha rubato l'oggetto in mano!");
				}
				
				if (hDamager.hasEmptyHand() && hDamager.hasSkill(Skill.BERSERK)) {
					event.setDamage(event.getDamage() + 5.0);
				}
				
				if (hDamager.hasInHand(Material.CACTUS) && hDamager.hasSkill(Skill.CACTUS_POWER)) {
					event.setDamage(event.getDamage() + 6.0);
				}
				
				if (hDamager.hasSkill(Skill.SLOWER)) {
					victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 1), true);
				}
				
			} else if (damagerEntity.getType() == EntityType.ARROW) {
				
				Arrow arrow = (Arrow) event.getDamager();
				HGamer hShooter = HungerGames.getHGamer(PlayerUtils.getOnlineShooter(arrow));
								
				if (hShooter != null) {
					
					if (victim.getInventory().getHelmet() == null && hShooter.hasSkill(Skill.ARROW_HEADSHOTS)) {
						
						if (hShooter.getPlayer().getLocation().distanceSquared(victim.getLocation()) >= SkillsLang.arrowHeadshots_rangeSquared) {
							hShooter.sendMessage(Format.CHAT_SKILL + "Headshot!");
							DeathListener.getCustomDeathMessages().put(victim.getName(), ChatColor.DARK_RED + victim.getName() + ChatColor.RED + " è stato headshottato da " + ChatColor.DARK_RED + hShooter.getName() + ChatColor.RED + ".");
							event.setDamage(10000.0);
						}
					}
					
					if (hShooter.hasSkill(Skill.CUPID) && hShooter.getPlayer() != victim) {
						WildCommons.heal(hShooter.getPlayer(), event.getDamage() / 2.0);
						Particle.HEART.display(hShooter.getPlayer().getLocation().add(0.0, 0.4, 0.0), 0.5f, 0.5f, 0.5f, 0, 20);
					}
					
					// Fix per frecce veloci
					if (arrow.hasMetadata("Cecchino")) {
						event.setDamage(event.getDamage() / 2.0);
					}
				}
				
				if (!victim.isDead() && HungerGames.getHGamer(victim).hasSkill(Skill.ARROW_BLOCKER)) {
					// Protezione frecce, anche non lanciate dai player
					event.setDamage(event.getDamage() / 3.0);
				}
			} else if (damagerEntity.getType() == EntityType.EGG) {
				
				HGamer hShooter = HungerGames.getHGamer(PlayerUtils.getOnlineShooter((Projectile) damagerEntity));
				
				if (hShooter != null && hShooter.hasSkill(Skill.SWITCHER) && hShooter.getPlayer() != victim) {
					
					if (hShooter.tryUse(Skill.SWITCHER, 5, true)) {
						
						event.setCancelled(true);
						
						Location shooterLocation = hShooter.getPlayer().getLocation();
						Location victimLocation = victim.getLocation();
	
						shooterLocation.getWorld().playEffect(shooterLocation, Effect.ENDER_SIGNAL, 0);
						victimLocation.getWorld().playEffect(victimLocation, Effect.ENDER_SIGNAL, 0);
	
						hShooter.getPlayer().teleport(victimLocation);
						victim.teleport(shooterLocation);
						
						EasySound.quickPlay(victim, Sound.ENDERMAN_TELEPORT);
						EasySound.quickPlay(hShooter.getPlayer(), Sound.ENDERMAN_TELEPORT);
						
						victim.sendMessage(Format.CHAT_SKILL + "Sei stato scambiato con " + hShooter.getName() + "!");
						hShooter.sendMessage(Format.CHAT_SKILL + "Sei stato scambiato con " + victim.getName() + "!");
					}
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onProjectileHit(ProjectileHitEvent event) {
		if (event.getEntityType() == EntityType.SNOWBALL) {
			
			if (event.getEntity().hasMetadata("Esplosiva")) {
			
				event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 0.0F);
				
				if (HungerGames.getState().allowPlayerDamage()) {
					Collection<HGamer> nearTributes = HungerGames.getNearTributes(event.getEntity().getLocation(), 3.0, PlayerUtils.getOnlineShooter(event.getEntity()));
					
					for (HGamer nearTribute : nearTributes) {
						nearTribute.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 0));
						nearTribute.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 8 * 20, 0));
					}
				}
			}
		
			if (event.getEntity().hasMetadata("Ragnatela")) {
				Block webCenter = event.getEntity().getLocation().getBlock();
				if (webCenter.getType() == Material.AIR) {
					webCenter.setType(Material.WEB);
				}
			}
		
		} else if (event.getEntityType() == EntityType.ARROW && event.getEntity().hasMetadata("Esplosiva") && HungerGames.getState().allowSkills()) {
			
			Location explosionLoc = event.getEntity().getLocation();
			float size = 2.0F;
			event.getEntity().getWorld().createExplosion(explosionLoc, 2.0F, false);
			
			if (event.getEntity().getShooter() instanceof Player) {
				
				Player shooter = (Player) event.getEntity().getShooter();
				HGamer gamer = HungerGames.getHGamer(shooter);
				
				if (!shooter.isDead() && gamer != null && gamer.getStatus() == Status.TRIBUTE) {
					// Applica lastDamage, se il tiratore è vivo ed è un tributo
					for (HGamer near : HungerGames.getAllGamersUnsafe()) {
						
						if (near.getStatus() != Status.TRIBUTE) {
							continue;
						}
						
						double distanceSquared = near.getPlayer().getLocation().distanceSquared(explosionLoc);
						if (distanceSquared <= size * size) {
							DeathListener.setLastDamager(near.getPlayer(), shooter);
						}
					}
				}
			}
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), new RemoveEntityTask(event.getEntity()), 1L);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void playerShootEvent(ProjectileLaunchEvent event) {
		if (event.getEntityType() == EntityType.SNOWBALL) {
			Player shooter = PlayerUtils.getOnlineShooter(event.getEntity());

			if (shooter != null && HungerGames.getHGamer(shooter).hasSkill(Skill.CONFUSING_SNOWBALLS)) {
				event.getEntity().setMetadata("Esplosiva", new FixedMetadataValue(HungerGames.getInstance(), true));
			}
		} else if (event.getEntityType() == EntityType.ARROW) {
			Player shooter = PlayerUtils.getOnlineShooter(event.getEntity());

			if (shooter != null && HungerGames.getHGamer(shooter).hasSkill(Skill.EXPLOSIVE_ARROWS)) {
				event.getEntity().setMetadata("Esplosiva", new FixedMetadataValue(HungerGames.getInstance(), true));
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void playerShootBowEvent(EntityShootBowEvent event) {

		if (event.getEntityType() != EntityType.PLAYER) return;
		
		if (event.getProjectile().getType() == EntityType.ARROW) {
			
			if (HungerGames.getHGamer((Player) event.getEntity()).hasSkill(Skill.FAST_ARROWS)) {
				event.getProjectile().setVelocity(event.getProjectile().getVelocity().multiply(1.9));
				event.getProjectile().setMetadata("Cecchino", new FixedMetadataValue(HungerGames.getInstance(), true)); // Altrimenti se lo shooter esce tolgono troppa vita
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onConsume(PlayerItemConsumeEvent event) {
		
		HGamer consumer = HungerGames.getHGamer(event.getPlayer());
		Material itemType = event.getItem().getType();
		
		if (itemType == Material.COOKIE && consumer.hasSkill(Skill.COOKIES_STRENGTH)) {
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 30 * 20, 0), true);
			
		} else if (itemType == Material.APPLE && consumer.hasSkill(Skill.APPLE_NINJA)) {
			event.setCancelled(true);
			consumer.sendMessage(ChatColor.RED + "Non puoi mangiare le mele poiché le usi per l'abilità.");
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		
		if (block.getType() == Material.TNT) {
			HGamer hPlacer = HungerGames.getHGamer(event.getPlayer());
			
			if (hPlacer.hasSkill(Skill.INSTANT_TNT)) {
				block.setType(Material.AIR);
				block.getWorld().spawnEntity(block.getLocation().add(0.5, 0.5, 0.5), EntityType.PRIMED_TNT);
			}
			
		} else if (event.getBlock().getType() == Material.STONE_PLATE && event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.GRAVEL) {
			HGamer hPlacer = HungerGames.getHGamer(event.getPlayer());
			
			if (hPlacer.hasSkill(Skill.MINES)) {
				hPlacer.sendMessage(Format.CHAT_SKILL + "Hai posizionato una mina.");
				block.setMetadata("Mina", new FixedMetadataValue(HungerGames.getInstance(), hPlacer.getName()));
			}
		}
	}
	
	@EventHandler (ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		
		Material type = event.getBlock().getType();
		
		if (isLog(type)) {
			HGamer hGamer = HungerGames.getHGamer(event.getPlayer());
			
			if (hGamer.hasSkill(Skill.TREE_FELLER)) {
				
				Block current = event.getBlock();
				int x;
				int z;
				int curY = current.getY();
				
				while(isLog(current.getType())) {
					x = current.getX();
					z = current.getZ();
					
					current.breakNaturally();
					
					curY++;
					current = current.getWorld().getBlockAt(x, curY, z);
				}
			}
		} else if (type == Material.IRON_ORE || type == Material.GOLD_ORE) {
			
			if (HungerGames.getHGamer(event.getPlayer()).hasSkill(Skill.ORE_COOKER)) {

				Block block = event.getBlock();
					
				if (block.getType() == Material.IRON_ORE) {
					block.setType(Material.AIR);
					event.setCancelled(true);
					block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.IRON_INGOT));
					abilityActivationSound.playTo(event.getPlayer());
						
				} else if (block.getType() == Material.GOLD_ORE) {
					block.setType(Material.AIR);
					event.setCancelled(true);
					block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.GOLD_INGOT));
					abilityActivationSound.playTo(event.getPlayer());
				}
			}
		}
	}
	
	private boolean isLog(Material mat) {
		return mat == Material.LOG || mat == Material.LOG_2;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onEntityTarget(EntityTargetEvent event) {
		Entity entity = event.getTarget();
		if (entity != null && entity.getType() == EntityType.PLAYER && event.getReason() == TargetReason.CLOSEST_PLAYER) {
			HGamer hGamer = HungerGames.getHGamer((Player) entity);
			if (hGamer != null && hGamer.hasSkill(Skill.MONSTER_FRIEND)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onMobDeath(EntityDeathEvent event) {
		if (event.getEntityType() == EntityType.PIG) {
			
			Player player = PlayerUtils.getRealDamager(event.getEntity().getLastDamageCause());
			
			if (player != null && player.isOnline() && HungerGames.getHGamer(player).hasSkill(Skill.PIG_SLAYER)) {
				event.getDrops().clear();
				event.getDrops().add(new ItemStack(Material.GRILLED_PORK, SkillsLang.pigSlayer_amount));
				abilityActivationSound.playTo(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (!HungerGames.getState().allowSkills()) return; // Su tutti gli eventi
		
		Player victim = event.getEntity();
		Player killer = PlayerUtils.getRealDamager(event.getEntity().getLastDamageCause());
		
		if (killer != null) {
			HGamer hKiller = HungerGames.getHGamer(killer);
			
			if (hKiller.hasSkill(Skill.KILL_FOR_FOOD)) {
				
				killer.setFoodLevel(20);
				killer.setExhaustion(0f);
				if (killer.getSaturation() < 10f) {
					killer.setSaturation(10f);
				}
				
			}
			
			if (hKiller.hasSkill(Skill.KILL_FOR_HEAL)) {
				killer.setHealth(((Damageable) killer).getMaxHealth());
			}
			
			if (hKiller.hasSkill(Skill.KILL_FOR_REGENERATION)) {
				killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, SkillsLang.killForRegeneration_duration * 20, SkillsLang.killForRegeneration_amplifier), true);
			}
			
			if (hKiller.hasSkill(Skill.WEAPON_LEVELUP) && killer.getItemInHand() != null) {
				
				if (swordPowerUps.containsKey(killer.getItemInHand().getType())) {
					killer.setItemInHand(new ItemStack(swordPowerUps.get(killer.getItemInHand().getType())));
					killer.sendMessage(Format.CHAT_SKILL + "La tua spada è stata potenziata!");
					EasySound.quickPlay(killer, Sound.ANVIL_USE);

				}
			}
		}
		
		HGamer hVictim = HungerGames.getHGamer(victim);
		
		if (hVictim.hasSkill(Skill.KAMIKAZE)) {
			victim.getWorld().createExplosion(victim.getLocation(), 1.8f, false);
		}
		
		if (hVictim.hasSkill(Skill.CREEPER_ON_DEATH)) {
			Creeper creeper = victim.getWorld().spawn(victim.getLocation(), Creeper.class);
			creeper.setCustomName(ChatColor.RED + hVictim.getName());
			creeper.setCustomNameVisible(true);
		}
	}
	
	@SuppressWarnings("serial")
	private Map<Material, Material> swordPowerUps = new HashMap<Material, Material>() {{
		put(Material.WOOD_SWORD, Material.STONE_SWORD);
		put(Material.STONE_SWORD, Material.IRON_SWORD);
		put(Material.GOLD_SWORD, Material.IRON_SWORD);
		put(Material.IRON_SWORD, Material.DIAMOND_SWORD);
	}};
	
	public static void removeOneItem(Player player) {
		ItemStack item = player.getItemInHand();
		if (item == null) return;
		
		int amount = item.getAmount();
		if (amount > 1) {
			item.setAmount(amount - 1);
			player.setItemInHand(item);
		} else {
			Bukkit.getScheduler().scheduleSyncDelayedTask(HungerGames.getInstance(), new ClearHandTask(player));
		}
	}
}
