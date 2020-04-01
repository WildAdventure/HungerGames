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
package com.gmail.filoghost.hungergames.generation;

import java.util.Map;
import java.util.Set;

import lombok.Getter;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import wild.core.utils.MathUtils;

import com.gmail.filoghost.hungergames.HungerGames;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Cornucopia {

	private static final int RADIUS = 9;
	private static final int RADIUS_SQUARED = RADIUS * RADIUS;
	
	@Getter private static Set<Block> chests;
	private static int centerX, centerZ;
	
	

	public static void setup(Block centerBlock, boolean build) {
		centerX = centerBlock.getX();
		centerZ = centerBlock.getZ();
		chests = Sets.newHashSet();
		
		if (build) {
			makeFloor(centerBlock);
			makeFeast(centerBlock);
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void removeAbove(Block block) {
		Location loc = block.getLocation();
		World world = block.getWorld();
		
		Block newBlock;
		while(loc.getY() < world.getMaxHeight() + 1) {
			
			loc.setY(loc.getY() + 1);
			newBlock = world.getBlockAt(loc);
			newBlock.setTypeIdAndData(0, (byte) 0, false);
		}
	}
	
	@SuppressWarnings("deprecation")
	private static void placeStoneBelow(Block block) {
		Location loc = block.getLocation();
		World world = block.getWorld();
		
		Block newBlock;
		while(loc.getY() > 5) {
			loc.setY(loc.getY() - 1);
			newBlock = world.getBlockAt(loc);
			newBlock.setTypeIdAndData(1, (byte) 0, false);
		}
	}
	
	
	public static int getHighestYIgnoreTrees(World world, int x, int z) {
		
		int y = 255;
		
		while (y > 0) {
			Block block = world.getBlockAt(x, y, z);
			
			switch (block.getType()) {
				case AIR:
				case LOG:
				case LOG_2:
				case LEAVES:
				case LEAVES_2:
					break;
				default:
					return y;
			}
			y--;
		}
		
		return 0;
	}

	@SuppressWarnings("deprecation")
	private static void makeFloor(Block centerBlock) {
		
		World world = centerBlock.getWorld();
		
		for (int x = -RADIUS; x <= RADIUS; x++) {
			for (int z = -RADIUS; z <= RADIUS; z++) {

				if (x * x + z * z < RADIUS_SQUARED) {
					Block block = world.getBlockAt(centerBlock.getX() + x, centerBlock.getY(), centerBlock.getZ() + z);
					removeAbove(block);
					placeStoneBelow(block);
					block.setType(Material.QUARTZ_BLOCK);
					block.setData((byte) 0);
				}
			}
		}
	}
	
	public static boolean isFloorColumn(Block block) {
		return isFloorColumn(block.getX(), block.getY(), block.getZ());
	}
	
	public static boolean isFloorColumn(int x, int y, int z) {
		return HungerGames.getSettings().randomWorlds_enable && MathUtils.square(centerX - x) + MathUtils.square(centerZ - z) < RADIUS_SQUARED;	
	}

	@SuppressWarnings("deprecation")
	private static void makeFeast(Block centerBlock) {
		
		World world = centerBlock.getWorld();
		
		String[][] stringSchematic = {
			{
			". . . . . . .",
			". | ^ G ^ | .",
			". < G G G > .",
			". G G G G G .",
			". < G G G > .",
			". | v G v | .",
			". . . . . . ."
			} , {
			". . . . . . .",
			". | . . . | .",
			". . . . . . .",
			". . . E . . .",
			". . . . . . .",
			". | . . . | .",
			". . . . . . .",
			} , {
			". . . . . . .",
			". | . . . | .",
			". . . . . . .",
			". . . . . . .",
			". . . . . . .",
			". | . . . | .",
			". . . . . . .",
			} , {
			". . W W W . .",
			". W W W W W .",
			"W W . . . W W",
			"W W . . . W W",
			"W W . . . W W",
			". W W W W W .",
			". . W W W . .",
			} , {
			". . . . . . .",
			". . . W . . .",
			". . W W W . .",
			". W W . W W .",
			". . W W W . .",
			". . . W . . .",
			". . . . . . .",
			} , {
			". . . . . . .",
			". . . . . . .",
			". . . . . . .",
			". . . W . . .",
			". . . . . . .",
			". . . . . . .",
			". . . . . . .",
			}
		 };
		
		
		
		Map<Character, Material> charToMaterialMap = Maps.newHashMap();
		Map<Character, Byte> charToDataMap = Maps.newHashMap();
		
		// "o" = aria
		
		charToMaterialMap.put('W', Material.WOOD);
		charToMaterialMap.put('|', Material.FENCE);
		charToMaterialMap.put('E', Material.ENCHANTMENT_TABLE);
		charToMaterialMap.put('G', Material.GOLD_BLOCK);
		
		charToMaterialMap.put('^', Material.CHEST);
		charToMaterialMap.put('>', Material.CHEST);
		charToMaterialMap.put('v', Material.CHEST);
		charToMaterialMap.put('<', Material.CHEST);
		
		charToDataMap.put('^', (byte) 2);
		charToDataMap.put('>', (byte) 3);
		charToDataMap.put('v', (byte) 4);
		charToDataMap.put('<', (byte) 5);
		
		
		
		// Ha sempre forma quadrata
		int side = stringSchematic[0].length;
		int shift = side / 2;
		
		int height = stringSchematic.length;
		
		Material[][][] materialSchematic = new Material[height][side][side];
		byte[][][] byteSchematic = new byte[height][side][side];
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < side; x++) {
				
				stringSchematic[y][x] = stringSchematic[y][x].replace(" ", ""); 
				for (int z = 0; z < side; z++) {
					
					char currentChar = stringSchematic[y][x].charAt(z);
					materialSchematic[y][x][z] = charToMaterialMap.containsKey(currentChar) ? charToMaterialMap.get(currentChar) : Material.AIR;
					
					if (charToDataMap.containsKey(currentChar)) {
						byteSchematic[y][x][z] = charToDataMap.get(currentChar);
					}
				}
			}
		}

		Block current = world.getBlockAt(centerBlock.getX() - shift, centerBlock.getY() + 1, centerBlock.getZ() - shift);
		
		final int startX = current.getX();
		final int startY = current.getY();
		final int startZ = current.getZ();
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < side; x++) {
				for (int z = 0; z < side; z++) {
					
					Material mat = materialSchematic[y][x][z];
					byte data = byteSchematic[y][x][z];
					
					Block block = world.getBlockAt(startX + x, startY + y, startZ + z);
					block.setTypeIdAndData(mat.getId(), data, false);
					
					if (mat == Material.CHEST) {
						chests.add(block);
					}
				}
			}
		}
	}
}
