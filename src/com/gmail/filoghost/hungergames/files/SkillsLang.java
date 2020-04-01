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
package com.gmail.filoghost.hungergames.files;

import java.io.IOException;

import org.bukkit.configuration.InvalidConfigurationException;

import com.gmail.filoghost.hungergames.HungerGames;
import com.gmail.filoghost.hungergames.player.Skill;
import com.gmail.filoghost.hungergames.utils.MathUtils;

import wild.api.config.PluginConfig;

public class SkillsLang {
	
	public static int arrowHeadshots_rangeSquared = 25;
	public static int killForRegeneration_duration = 10;
	public static int killForRegeneration_amplifier = 0;
	public static int pigSlayer_amount = 3;
	public static int watchStopper_duration = 5;
	public static int watchStopper_radius = 15;

	public static void load() throws IOException, InvalidConfigurationException {
		PluginConfig config = new PluginConfig(HungerGames.getInstance(), "abilities.yml");
		
		String path;
		boolean needsSave = false;
		
		for (Skill skill : Skill.values()) {
			path = skill.toString() + ".description";
			
			if (!config.isSet(path)) {
				needsSave = true;
				config.set(path, "N/A");
			} else {
				skill.setDescription(config.getString(path));
			}
		}
		
		path = Skill.ARROW_HEADSHOTS.toString() + ".range";
		if (!config.isSet(path)) {
			config.set(path, 25);
			needsSave = true;
		} else {
			arrowHeadshots_rangeSquared = MathUtils.square(config.getInt(path));
		}
		
		path = Skill.KILL_FOR_REGENERATION.toString() + ".time";
		if (!config.isSet(path)) {
			config.set(path, 10);
			needsSave = true;
		} else {
			killForRegeneration_duration = config.getInt(path);
		}
		
		path = Skill.KILL_FOR_REGENERATION.toString() + ".amplifier";
		if (!config.isSet(path)) {
			config.set(path, 1);
			needsSave = true;
		} else {
			killForRegeneration_amplifier = config.getInt(path) - 1;
		}
		
		path = Skill.PIG_SLAYER.toString() + ".amount";
		if (!config.isSet(path)) {
			config.set(path, 3);
			needsSave = true;
		} else {
			pigSlayer_amount  = config.getInt(path);
		}
		
		path = Skill.WATCH_STOPPER.toString() + ".time";
		if (!config.isSet(path)) {
			config.set(path, 5);
			needsSave = true;
		} else {
			watchStopper_duration = config.getInt(path);
		}
		
		path = Skill.WATCH_STOPPER.toString() + ".radius";
		if (!config.isSet(path)) {
			config.set(path, 15);
			needsSave = true;
		} else {
			watchStopper_radius = config.getInt(path);
		}
		
		if (needsSave) {
			config.save();
		}
	}
	
	
	
}
