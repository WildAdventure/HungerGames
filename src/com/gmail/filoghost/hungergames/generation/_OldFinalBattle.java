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
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.google.common.collect.Maps;

public class _OldFinalBattle {

	@SuppressWarnings("deprecation")
	public static void build(Block centerBlock) {

		World world = centerBlock.getWorld();
		
		String[][] stringSchematic = {
			{
			"# # # # # # # # # # #",
			"# # # # # # # # # # #",
			"# # # # # # # # # # #",
			"# # # G # # # G # # #",
			"# # # # # # # # # # #",
			"# # # # # # # # # # #",
			"# # # # # # # # # # #",
			"# # # G # # # G # # #",
			"# # # # # # # # # # #",
			"# # # # # # # # # # #",
			"# # # # # # # # # # #"
			} , {
			"@ @ @ @ @ @ @ @ @ @ @",
			"@ | . . . . . . . | @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ | . . . . . . . | @",
			"@ @ @ @ @ @ @ @ @ @ @"
			} , {
			"@ @ @ @ @ @ @ @ @ @ @",
			"@ | . . . . . . . | @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ | . . . . . . . | @",
			"@ @ @ @ @ @ @ @ @ @ @"
			} , {
			"@ @ @ @ @ @ @ @ @ @ @",
			"@ | . . . . . . . | @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ | . . . . . . . | @",
			"@ @ @ @ @ @ @ @ @ @ @"
			} , {
			"@ @ @ @ @ @ @ @ @ @ @",
			"@ | . . . . . . . | @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ . . . . . . . . . @",
			"@ | . . . . . . . | @",
			"@ @ @ @ @ @ @ @ @ @ @"
						} , {
			"# # # # # # # # # # #",
			"# # # # # # # # # # #",
			"# # # # # # # # # # #",
			"# # # G # # # G # # #",
			"# # # # # # # # # # #",
			"# # # # # V # # # # #",
			"# # # # # # # # # # #",
			"# # # G # # # G # # #",
			"# # # # # # # # # # #",
			"# # # # # # # # # # #",
			"# # # # # # # # # # #"
			}
		 };
		
		
		
		Map<Character, Material> charToMaterialMap = Maps.newHashMap();
		Map<Character, Byte> charToDataMap = Maps.newHashMap();
		
		// "o" = aria
		
		charToMaterialMap.put('#', Material.STONE);
		charToMaterialMap.put('|', Material.COBBLE_WALL);
		charToMaterialMap.put('G', Material.GLOWSTONE);
		charToMaterialMap.put('V', Material.GLASS);
		charToMaterialMap.put('@', Material.SMOOTH_BRICK);		
		
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
				}
			}
		}
	}
}
