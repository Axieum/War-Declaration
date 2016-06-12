package me.axiom.bpl.wardeclaration;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class WarDeathListener implements Listener {

	WarDeclaration plugin;
	public WarDeathListener(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent e) {
		
		Player dead = e.getEntity();
		Player killer = dead.getKiller();
		
		PlayerStats killerStats = plugin.getPlayerStats(killer);
		PlayerStats deadStats = plugin.getPlayerStats(dead);
		int killerKills = killerStats.getKills();
		int deadDeaths = deadStats.getDeaths();
		
		killerStats.setKills(killerKills+1);
		deadStats.setDeaths(deadDeaths+1);
		
		int killerKD = (killerKills+1)/(killerStats.getDeaths());
		int deadKD = (deadStats.getKills())/(deadDeaths+1);
		
		killer.sendMessage("§6[§cWAR§6] §8K/D Ratio: §7" + colorCode(killerKD) + killerKD);
		dead.sendMessage("§6[§cWAR§6] §8K/D Ratio: §7" + colorCode(deadKD) + deadKD);
		
	}
	
	public ChatColor colorCode(int i) {
		
		if (i < 1) {
			return ChatColor.RED;
		} else {
			return ChatColor.GREEN;
		}
		
	}
	
}
