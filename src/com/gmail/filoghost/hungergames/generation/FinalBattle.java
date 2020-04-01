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

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

public class FinalBattle {

	public static void build(org.bukkit.block.Block centerBlock) {
		long start = System.currentTimeMillis();
		
		centerBlock.getRelative(BlockFace.UP).setType(Material.WORKBENCH);
		makeCylinder(centerBlock, 0, 12, Material.SMOOTH_BRICK, 0, false);
		makeCylinder(centerBlock, 1, 12, Material.QUARTZ_BLOCK, 2, true);
		makeCylinder(centerBlock, 2, 12, Material.QUARTZ_BLOCK, 2, true);
		makeCylinder(centerBlock, 3, 12, Material.QUARTZ_BLOCK, 2, true);
		makeCylinder(centerBlock, 4, 12, Material.QUARTZ_BLOCK, 2, true);
		makeCylinder(centerBlock, 5, 12, Material.QUARTZ_BLOCK, 2, true);
		makeCylinder(centerBlock, 6, 12, Material.STEP, 0, true);
		makeCylinder(centerBlock, 1, 11, Material.STEP, 0, true);
		
		long end = System.currentTimeMillis();
		System.out.println("Final battle build time: " + (end - start) + " ms.");
	}
	
	@SuppressWarnings("deprecation")
	private static void makeCylinder(org.bukkit.block.Block centerBlock, int yOffset, int radius, Material material, int data, boolean hollow) {
		
		World world = centerBlock.getWorld();
		final int centerX = centerBlock.getX();
		final int centerY = centerBlock.getY() + yOffset;
		final int centerZ = centerBlock.getZ();
		
		int radiusSquared = radius * radius;
		int radiusSquaredSmaller = (radius-1) * (radius-1);
		int distanceSquared;
		
		for (int x = -radius; x <= radius; x++) {
			for (int z = -radius; z <= radius; z++) {
				
				distanceSquared = x * x +  z * z;
				
				if (distanceSquared < radiusSquared) {
					org.bukkit.block.Block block = world.getBlockAt(centerX + x, centerY, centerZ + z);
					
					
					if (!hollow) {
						block.setTypeIdAndData(material.getId(), (byte) data, false);
					} else {
						if (distanceSquared >= radiusSquaredSmaller) {
							block.setTypeIdAndData(material.getId(), (byte) data, false);
						} else {
							if (block.getType() != Material.AIR) {
								block.setTypeIdAndData(0, (byte) 0, false);
							}
						}
					}
				}
			}
		}
	}
}
