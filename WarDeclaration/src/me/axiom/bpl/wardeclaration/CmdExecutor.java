package me.axiom.bpl.wardeclaration;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CmdExecutor implements CommandExecutor {

	WarDeclaration plugin;
	public void plugin(WarDeclaration plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		
		boolean isPlayer = false;
		
		// Declare whether the CommandSender is in-game or is in the console.
		if (s instanceof Player) {
			isPlayer = true;
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
				s.sendMessage("§4/war §c§ldeclare <enemy_clan>");
				s.sendMessage("§4/war §c§lcancel");
				s.sendMessage("§4/war §c§lforfeit");
				s.sendMessage("§4/war §c§l<accept/deny>");
				s.sendMessage("§8/war §7§llist");
				s.sendMessage("§8/war §7§ldeclare <clan> <clan>");
				s.sendMessage("§8/war §7§lcancel <clan>");
				s.sendMessage("§8/war §7§lforfeit <clan>");
				s.sendMessage("§6---------------------------");
			}
		}
		
		if (args.length == 1) { // They typed, "/war something".
			if (args[0].equalsIgnoreCase("list")) { // They typed, "/war list".
				
			}
		}
		
		return true;
		
	}

}
