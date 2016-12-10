package me.axiom.bpl.wardeclaration.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.massivecraft.factions.event.EventFactionsCreate;

import me.axiom.bpl.wardeclaration.WarDeclaration;

public class FactionCreationListener implements Listener {

	WarDeclaration plugin;
	public FactionCreationListener(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onFactionCreation(EventFactionsCreate e) {
		plugin.logger.info("New faction detected! Initialising data...");
		String name = e.getFactionName();
		plugin.factionWars.set(name + ".Target", "none");
		plugin.factionWars.set(name + ".Requester", "none");
		plugin.factionWars.set(name + ".Status", "available");
		plugin.factionWars.set(name + ".Engaged", false);
		plugin.factionWars.set(name + ".ForfeitedBy", "none");
		plugin.factionWars.set(name + ".TimeOfDeclaration", 0);
		plugin.factionWars.set(name + ".TimeOfEngage", 0);
		plugin.factionWars.set(name + ".TimeOfEnd", 0);
		plugin.logger.info("Faction has been successfully initialised!");
		plugin.saveFactionWarsFile();
	}
	
}
