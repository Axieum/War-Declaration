package me.axiom.bpl.wardeclaration;

import java.util.HashSet;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;

public class CmdExecutor implements CommandExecutor {

	WarDeclaration plugin;
	public void plugin(WarDeclaration plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		
		boolean isPlayer = false;
		boolean hasFaction = false;
		MPlayer mp = null;
		Faction f = null;
		
		// Declare whether the CommandSender is in-game or is in the console.
		if (s instanceof Player) {
			isPlayer = true;
			mp = MPlayer.get(s);
			if (mp.hasFaction()) {
				hasFaction = true;
				f = mp.getFaction();
			}
		}
		
		// "/war" COMMAND : HELP MENU.
		if (args.length == 0) { // They typed, "/war".
			if (isPlayer) { // Check if they're in-game or in console.
				// They're in-game, show them the player commands.
				s.sendMessage("§6-------§c§lClan Wars§r§6-------");
				s.sendMessage("§8/war §7§ldeclare <enemy_clan>");
				s.sendMessage("§8/war §7§lcancel");
				s.sendMessage("§8/war §7§lforfeit");
				s.sendMessage("§8/war §7§laccept/deny");
				s.sendMessage("§8/war §7§llist");
				s.sendMessage("§6---------------------------");
			} else {
				// They're in the console, show them the admin commands.
				s.sendMessage("§6-------§c§lClan Wars§r§6-------");
				s.sendMessage("§8/war §7§llist");
				s.sendMessage("§8/war §7§ldeclare <clan> <clan>");
				s.sendMessage("§8/war §7§lcancel <clan>");
				s.sendMessage("§8/war §7§lforfeit <clan>");
				s.sendMessage("§6---------------------------");
			}
		}
		
		if (args.length == 1) { // They typed, "/war something".
			if (args[0].equalsIgnoreCase("list")) { // They typed, "/war list".
				s.sendMessage("§6-------§c§lWar List§r§6-------");
				for (Faction fa : FactionColl.get().getAll()) { // Gets every faction and runs the following code FOR EACH.
					HashSet<String> factionsProcessed = new HashSet<String>(); // This will hold all factions already shown, stops duplicates.
					if (plugin.factionsWar.getString(fa.getName()) != "none" && !(factionsProcessed.contains(fa.getName()))) { // Checks if the faction is at war.
						if (f == fa) { // If the sender's faction is shown, make they're faction green.
							s.sendMessage("§a" + fa.getName() + " §7<--[]--> §e" + plugin.factionsWar.getString(fa.getName()));
						} else {
							s.sendMessage("§e" + fa.getName() + " §7<--[]--> §e" + plugin.factionsWar.getString(fa.getName()));
						}
						factionsProcessed.add(plugin.factionsWar.getString(fa.getName()));
					}
				}
			}
		}
		
		return true;
		
	}

}
