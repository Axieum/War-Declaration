package me.axiom.bpl.wardeclaration.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
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
		
		if (hasWar(f)) { // If they're currently in a war, this will intefere with many components.
			e.getMPlayer().message("§6[§cWAR§6] §cIf you were to change your clan's name now, " + getWarOpponent(f).getColorTo(f) + getWarOpponent(f).getName() + " §cwill be confused!");
			e.setCancelled(true);
			return;
		}
		
		plugin.factionWars.set(newName, plugin.factionWars.get(oldName));
		plugin.factionWars.set(oldName, null);
		plugin.saveFactionWarsFile();
		plugin.logger.info("Faction data has been successfully updated!");
	}
	
	public boolean hasWar(Faction f) {
		String status = plugin.factionWars.getString(f.getName() + ".Status");
		if (status.equalsIgnoreCase("forfeited") || status.equalsIgnoreCase("available") || status.equalsIgnoreCase("denied") || status.equalsIgnoreCase("cancelled")) {
			return false;
		} else {
			return true;
		}
	}
	
	public Faction getWarOpponent(Faction f) {
		if (plugin.factionWars.getString(f.getName() + ".Target").equalsIgnoreCase(f.getName())) {
			return FactionColl.get().getByName(plugin.factionWars.getString(f.getName() + ".Requester"));
		} else {
			return FactionColl.get().getByName(plugin.factionWars.getString(f.getName() + ".Target"));
		}
	}
	
}
