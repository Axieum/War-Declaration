package me.axiom.bpl.wardeclaration;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.event.EventFactionsNameChange;

public class FactionNameChangeListener implements Listener {

	WarDeclaration plugin;
	public FactionNameChangeListener(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onFactionNameChange(EventFactionsNameChange e) {
		Faction f = e.getFaction();
		String newName = e.getNewName();
		String oldName = f.getName();
		
		plugin.factionWars.set(newName, plugin.factionWars.get(oldName));
		plugin.factionWars.set(oldName, null);
		plugin.saveFactionWarsFile();
	}
	
}
