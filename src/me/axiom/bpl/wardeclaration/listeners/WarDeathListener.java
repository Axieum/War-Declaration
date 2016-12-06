package me.axiom.bpl.wardeclaration.listeners;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;

import me.axiom.bpl.wardeclaration.PlayerStats;
import me.axiom.bpl.wardeclaration.WarDeclaration;

public class WarDeathListener implements Listener {

	WarDeclaration plugin;
	public WarDeathListener(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent e) {
		
		Player dead = e.getEntity();
		Player killer = dead.getKiller();
		
		MPlayer Mdead = MPlayer.get(dead);
		MPlayer Mkiller = MPlayer.get(killer);
		
		if (!Mkiller.hasFaction() && !Mdead.hasFaction()) { // No one is in a war. Since they're not in a faction.
			return;
		}
		
		// We need to ensure the two players are in the same war (not from a different war).
		if (!Mkiller.hasFaction() && hasWar(Mdead.getFaction())) { // Killer not in war. // Dead is in war.
			killer.sendMessage("§6[§cWAR§6] §cUnfortunately, you are not involved in §e" + getWarOpponent(Mdead.getFaction()).getName() + "§c's war! §4§lLet them fight their own war!");
			dead.sendMessage("§6[§cWAR§6] §c§lOuch! §aLuckily, this death did not count as §e" + killer.getName() + " §ais not in your war.");
			return;
		}
		
		if (!Mdead.hasFaction() && hasWar(Mkiller.getFaction())) {
			killer.sendMessage("§6[§cWAR§6] §cUnfortunately, you are not involved in §e" + getWarOpponent(Mdead.getFaction()).getName() + "§c's war! §4§lLet them fight their own war!");
			dead.sendMessage("§6[§cWAR§6] §c§lOuch! §aLuckily, this death did not count as §e" + killer.getName() + " §ais not in your war.");
			return;
		}
		
		if (hasWar(Mdead.getFaction()) && getWarEngaged(Mdead.getFaction()) == true) { // Check if the dead person has a war and is engaged.
			if (hasWar(Mkiller.getFaction()) && getWarEngaged(Mdead.getFaction()) == true) { // Check if the killer has a war and is engaged.
				if (getWarOpponent(Mdead.getFaction()) == Mkiller.getFaction()) { // This means that that they are in the same war.
				
					// Retrieve the stats for both players.
					PlayerStats killerStats = plugin.getPlayerStats(killer.getUniqueId());
					PlayerStats deadStats = plugin.getPlayerStats(dead.getUniqueId());
					int killerKills = killerStats.getKills();
					int deadDeaths = deadStats.getDeaths();
					
					// Add one to their kills/deaths...
					killerStats.setKills(killerKills+1);
					deadStats.setDeaths(deadDeaths+1);				
					
					// Send them a message with their NEW K/D Ratio.					
					killer.sendMessage("§6[§cWAR§6] §8K/D Ratio: §7" + colorCode(killerStats.getKD()) + roundFloatTwo(killerStats.getKD()));
					dead.sendMessage("§6[§cWAR§6] §8K/D Ratio: §7" + colorCode(deadStats.getKD()) + roundFloatTwo(deadStats.getKD()));
					
				} else {
					// Now, this means that they are both engaged, although they are in seperate wars.
					killer.sendMessage("§6[§cWAR§6] §2Unfortunately, that kill did not count as §e" + dead.getName() + " §2is not in your war.");
					dead.sendMessage("§6[§cWAR§6] §c§lOuch! §2Luckily, this death did not count as §e" + killer.getName() + " §2is not in your war.");
				}
			} else {
				// Now, this means that the dead person is engaged, but the killer is not.
				killer.sendMessage("§6[§cWAR§6] §cUnfortunately, you are not involved in §e" + getWarOpponent(Mdead.getFaction()).getName() + "§c's war! §4§lLet them fight their own war!");
				dead.sendMessage("§6[§cWAR§6] §c§lOuch! §aLuckily, this death did not count as §e" + killer.getName() + " §ais not in your war.");
			}
		}
		
		// Just allow sending messages to the killer, since the IF Statements above do not allow multi way.
		if (hasWar(Mkiller.getFaction()) && getWarEngaged(Mdead.getFaction()) == true) {
			if (hasWar(Mdead.getFaction()) && getWarEngaged(Mdead.getFaction()) == true) {
				
			} else {
				// Now, this means that the killer person is engaged, but the dead person is not.
				killer.sendMessage("§6[§cWAR§6] §2Unfortunately, that kill did not count as §e" + dead.getName() + " §2is not in your war.");
				dead.sendMessage("§6[§cWAR§6] §cYou are not involved in §e" + getWarOpponent(Mkiller.getFaction()).getName() + "§c's war! §4§lLet them fight their own war!");
			}
		}
		
	}
	
	public String roundFloatTwo(float n) {
		
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		return df.format((double)n);
		
	}
	
	public ChatColor colorCode(float i) {
		
		if (i == 1) {
			return ChatColor.YELLOW;
		} else if (i < 1) {
			return ChatColor.RED;
		} else {
			return ChatColor.GREEN;
		}
		
	}
	
	public boolean getWarEngaged(Faction f) {
		return plugin.factionWars.getBoolean(f.getName() + ".Engaged");
	}
	
	public boolean hasWar(Faction f) {
		if (plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("available") || plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("denied") || plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("forfeited") || plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("cancelled")) {
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
