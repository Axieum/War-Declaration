package me.axiom.bpl.wardeclaration.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.event.EventFactionsNameChange;

import me.axiom.bpl.wardeclaration.WarDeclaration;

public class FactionNameChangeListener implements Listener {

	WarDeclaration plugin;
	public FactionNameChangeListener(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onFactionNameChange(EventFactionsNameChange e) {
		
		if (e.isCancelled()) {
			return;
		}
		
		plugin.logger.info("Faction name change detected! Updating data...");
		Faction f = e.getFaction();
		String newName = e.getNewName();
		String oldName = f.getName();
		
		plugin.factionWars.set(newName, plugin.factionWars.get(oldName));
		plugin.factionWars.set(oldName, null);
		plugin.saveFactionWarsFile();
		plugin.logger.info("Faction data has been successfully updated!");
	}
	
}
