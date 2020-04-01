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
package com.gmail.filoghost.hungergames.hud.menu;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import wild.api.menu.Icon;
import wild.api.menu.IconMenu;

import com.gmail.filoghost.hungergames.player.Kit;
import com.gmail.filoghost.hungergames.utils.Format;
import com.google.common.collect.Lists;

public class KitMenuManager {

	private static List<IconMenu> freeKitMenus;
	private static List<IconMenu> coinsKitMenus;
	
	public static void load(List<Kit> kits) {
		List<Kit> freeKits = Lists.newArrayList();
		List<Kit> priceKits = Lists.newArrayList();
		
		for (Kit kit : kits) {
			if (kit.isFree()) {
				freeKits.add(kit);
			} else {
				priceKits.add(kit);
			}
		}
		
		freeKitMenus = loadKits("Kit gratis", freeKits, Format.KIT_FREE);
		coinsKitMenus = loadKits("Kit a pagamento", priceKits, Format.KIT_COINS);
		
		OpenMenuClickHandler toFreePages = new OpenMenuClickHandler(freeKitMenus.get(0));
		OpenMenuClickHandler toCoinsPages = new OpenMenuClickHandler(coinsKitMenus.get(0));
		
		freeKitMenus.get(0).setIcon(9, 6, new IconBuilder(Material.ENCHANTED_BOOK).name(Format.KIT_COINS + "Kit a pagamento »").clickHandler(toCoinsPages).build());
		coinsKitMenus.get(0).setIcon(1, 6, new IconBuilder(Material.ENCHANTED_BOOK).name(Format.KIT_FREE + "« Kit gratis").clickHandler(toFreePages).build());
		
		for (IconMenu menu : freeKitMenus) {
			menu.refresh();
		}
		for (IconMenu menu : coinsKitMenus) {
			menu.refresh();
		}
	}
	
	
	private static List<IconMenu> loadKits(String menusName, List<Kit> kits, String kitNameFormat) {
		
		List<IconMenu> menuPages = Lists.newArrayList();
		IconMenu currentMenu = null;
		
		if (kits.isEmpty()) {
			// Pagina vuota
			menuPages.add(new IconMenu(menusName, 6));
			return menuPages;
		}
		
		int iconIndex = 0;
		
		for (Kit kit : kits) {
			
			if (iconIndex == 0) { // 0 = primo elemento -> crea il menù
				currentMenu = new IconMenu(menusName, 6);
				menuPages.add(currentMenu);
			}
			
			Icon icon = new Icon();
			icon.setMaterial(kit.getMenuIcon().getType());
			icon.setAmount(kit.getMenuIcon().getAmount());
			icon.setDataValue(kit.getMenuIcon().getDurability());
			icon.setName(kitNameFormat + kit.getName());
			
			icon.setLore(kit.getMenuDescription());
			icon.setCloseOnClick(true);
			icon.setClickHandler(new KitClickHandler(kit));
			currentMenu.setIconRaw(iconIndex, icon);
			
			iconIndex++;
			if (iconIndex >= 36) {
				iconIndex = 0;
			}
		}
		
		
		// Frecce di navigazione
		for (int i = 0; i < menuPages.size(); i++) {
				
			boolean arrowDown = true;
			boolean arrowUp = true;
				
			if (i == 0) {
				// Primo
				arrowUp = false;
			}
				
			if (i == menuPages.size() - 1) {
				// Ultimo
				arrowDown = false;
			}
				
			if (arrowUp) {
				Icon upIcon = new Icon(Material.ARROW);
				upIcon.setName(ChatColor.WHITE + "" + ChatColor.ITALIC + "Pagina precedente");
				upIcon.setClickHandler(new OpenMenuClickHandler(menuPages.get(i - 1)));
				menuPages.get(i).setIcon(4, 6, upIcon);
			}
				
			if (arrowDown) {
				Icon upIcon = new Icon(Material.ARROW);
				upIcon.setName(ChatColor.WHITE + "" + ChatColor.ITALIC + "Pagina successiva");
				upIcon.setClickHandler(new OpenMenuClickHandler(menuPages.get(i + 1)));
				menuPages.get(i).setIcon(6, 6, upIcon);
			}
			
			if (arrowUp || arrowDown) {
				Icon middle = new Icon(Material.PAPER);
				middle.setName(ChatColor.WHITE + "" + ChatColor.ITALIC + "Pagina " + (i + 1) + " di " + menuPages.size());
				middle.setLore("", "Menù: " + kitNameFormat + menusName);
				middle.setCloseOnClick(false);
				menuPages.get(i).setIcon(5, 6, middle);
			}
		}

		return menuPages;
	}
	
	public static void open(Player player) {
		freeKitMenus.get(0).open(player);
	}
}
