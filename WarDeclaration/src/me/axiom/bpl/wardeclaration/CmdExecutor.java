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
	public CmdExecutor(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		
		MPlayer mP = null;
		Faction f = null;
		
		if (s instanceof Player) {
			mP = MPlayer.get(s);
			if (mP.hasFaction()) {
				f = mP.getFaction();
			}
		}
		
		// COMMAND MENU
		if (args.length == 0) {
			if (s instanceof Player) { // Check if they're in-game or in console.
				s.sendMessage("§6---------§c§lClan Wars§r§6---------");
				s.sendMessage("§8/war §7§ldeclare <enemy_clan>");
				s.sendMessage("§8/war §7§lcancel");
				s.sendMessage("§8/war §7§lforfeit");
				s.sendMessage("§8/war §7§laccept/deny");
				s.sendMessage("§8/war §7§lupcoming");
				s.sendMessage("§6---------------------------");
			} else {
				s.sendMessage("§6---------§c§lClan Wars§r§6---------");
				s.sendMessage("§8/war §7§lupcoming");
				s.sendMessage("§6---------------------------");
			}
		}
		
		// DECLARE
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("declare")) {
				if (mP.hasFaction()) {
					if (mP == f.getLeader()) {
						if (!f.getName().equalsIgnoreCase(args[1])) {
							Faction enemyF = FactionColl.get().getByName(args[1]);
							if (enemyF != null) {
								if (!hasWar(f) && !hasWar(enemyF)) {
									setWarTarget(f, enemyF);
									setWarRequester(f, f);
									setWarStatus(f, "pending");
									setWarTarget(enemyF, enemyF);
									setWarRequester(enemyF, f);
									setWarStatus(enemyF, "pending");
									s.sendMessage("§eYou §7have requested a war against " + enemyF.getColorTo(mP) + enemyF.getName() + "§7!");
									f.sendMessage("§7Your clan has requested a war against " + enemyF.getColorTo(f) + enemyF.getName() + "§7!");
									enemyF.sendMessage(f.getColorTo(enemyF) + f.getName() + " §7has requested a war against your clan!");
									enemyF.getLeader().sendMessage("§7To accept the war against " + f.getColorTo(enemyF) + f.getName() + " §7use the §3'/war <accept/deny>'§7.");
									plugin.saveFactionWarsFile();
								} else {
									if (hasWar(f)) {
										s.sendMessage("§cYour clan is already in a war!");
									} else if (hasWar(enemyF)) {
										s.sendMessage(enemyF.getColorTo(f) + enemyF.getName() + " §cis already in a war!");
									}
								}
							} else {
								s.sendMessage("§e" + args[1] + " §cdoes not exist!");
							}
						} else {
							s.sendMessage("§cYou cannot declare a war against yourself!");
						}
					} else {
						s.sendMessage("§cWars may only be declared by your clan leader! §7(§e" + f.getLeader().getName() + "§7)");
					}
				} else {
					s.sendMessage("§cYou are not in a clan!");
				}
			}
		}
		
		// ACCEPT
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("accept")) {
				if (mP.hasFaction()) {
					if (mP == f.getLeader()) {
						if (!getWarTarget(f).getName().equalsIgnoreCase("none")) {
							// The war request must still be pending in order to accept.
							if (getWarStatus(f).equalsIgnoreCase("pending")) {
								Faction enemyF = getWarTarget(f);
								setWarStatus(f, "accepted");
								setWarStatus(enemyF, "accepted");
								s.sendMessage("§eYou §ahave §2accepted §athe war against " + enemyF.getColorTo(f) + enemyF.getName() + "§a!");
								f.sendMessage("§aYour clan has accepted the war against " + enemyF.getColorTo(f) + enemyF.getName() + "§a!");
								enemyF.sendMessage("§e" + f.getColorTo(enemyF) + f.getName() + " §ahas accepted the war against your clan!");
								plugin.saveFactionWarsFile();
							} else { // War is not pending and has been accepted or is engaged.
								s.sendMessage("§cYour war has already been approved!");
							}
						} else {
							s.sendMessage("§cYour clan has no pending wars!");
						}
					} else  {
						s.sendMessage("§cWars may only be accepted by your clan leader! §7(§e" + f.getLeader().getName() + "§7)");
					}
				} else {
					s.sendMessage("§cYou are not in a clan!");
				} 
			}
		}
			
		// DENY
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("deny")) {
				if (mP.hasFaction()) {
					if (mP == f.getLeader()) {
						if (hasWar(f)) {
							// The war request must still be pending in order to deny.
							if (getWarStatus(f).equalsIgnoreCase("pending")) {
								Faction enemyF = getWarTarget(f);
								setWarStatus(f, "denied");
								setWarStatus(enemyF, "denied");
								s.sendMessage("§eYou §chave §4denied §cthe war against " + enemyF.getColorTo(f) + enemyF.getName() + "§c!");
								f.sendMessage("§cYour clan has §4denied §cthe war against " + enemyF.getColorTo(f) + enemyF.getName() + "§c!");
								enemyF.sendMessage(f.getColorTo(enemyF) + f.getName() + " §chas §4denied §cthe war against your clan!");
								plugin.saveFactionWarsFile();
							} else { // War is not pending and has been accepted or is engaged.
								s.sendMessage("§cYou cannot deny a war after approval!");
								s.sendMessage("§cInstead, you must forfeit the war. §7/war forfeit§c.");
							}
						} else {
							s.sendMessage("§cYour clan has no pending wars!");
						}
					} else  {
						s.sendMessage("§cWars may only be denied by your clan leader! §7(§e" + f.getLeader().getName() + "§7)");
					}
				} else {
					s.sendMessage("§cYou are not in a clan!");
				} 
			}
		}
		
		// CANCEL
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("cancel")) {
				if (mP.hasFaction()) {
					if (mP == f.getLeader()) {
						if (hasWar(f)) { 
							if (getWarStatus(f).equalsIgnoreCase("pending")) {
								Faction enemyF = getWarTarget(f);
								setWarTarget(f, null);
								setWarStatus(f, "cancelled");
								setWarRequester(f, null);
								setWarStatus(enemyF, "cancelled");
								s.sendMessage("§eYou §7have cancelled the war against " + enemyF.getColorTo(f) + enemyF.getName() + "§7!");
								f.sendMessage("§7Your clan has cancelled the war against " + enemyF.getColorTo(f) + enemyF.getName() + "§7!");
								enemyF.sendMessage(f.getColorTo(enemyF) + f.getName() + " §chas cancelled the war against your clan!");
								plugin.saveFactionWarsFile();
							} else { // War is not pending and has been accepted or is engaged.
								s.sendMessage("§cYou cannot cancel a war after approval!");
								s.sendMessage("§cInstead, you must forfeit the war. §7/war forfeit§c.");
							}
						} else {
							s.sendMessage("§cYour clan has no pending wars!");
						}
					} else {
						s.sendMessage("§cWars may only be cancelled by your clan leader! §7(§e" + f.getLeader().getName() + "§7)");
					} 
				} else {
					s.sendMessage("§cYou are not in a clan!");
				}
			} 
		}
		
		// UPCOMING
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("upcoming")) {
				s.sendMessage("§6------------§c§lWars Upcoming§r§6------------");
				HashSet<String> factionsProcessed = new HashSet<String>();
				for (Faction fa : FactionColl.get().getAll()) {
					if (!(fa.getName().equalsIgnoreCase("safezone")) && !(fa.getName().equalsIgnoreCase("warzone")) && !(fa.getName().equalsIgnoreCase(FactionColl.get().getByName("wilderness").getName()))) {
						if (getWarTarget(fa) != null && getWarRequester(fa) != null && !factionsProcessed.contains(fa.getName())) {
							s.sendMessage(fa.getColorTo(mP) + getWarRequester(fa).getName() + " §7<--[" + statusToColour(getWarStatus(fa)) + "§7]--> " + getWarTarget(fa).getColorTo(mP) + getWarTarget(fa).getName());
							factionsProcessed.add(getWarTarget(fa).getName());
							factionsProcessed.add(getWarRequester(fa).getName());
						}
					}
				}
			}
		}
		
		return false;
		
	}

	public String statusToColour(String msg) {
		
		String m = "";
		
		switch (msg) {
		
		case "pending":
			m = "§3PENDING";
			break;
			
		case "engaged":
			m = "§6ENGAGED";
			break;
			
		case "accepted":
			m = "§aACCEPTED";
			break;
		
		case "denied":
			m = "§cDENIED";
			break;
			
		}
		
		return m;
		
	}
	
	public boolean hasWar(Faction f) {
		if (!plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("available") || !plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("denied")) {
			return true;
		} else {
			return false;
		}
	}
	
	public String getWarStatus(Faction f) {
		return plugin.factionWars.getString(f.getName() + ".Status");
	}
	
	public Faction getWarTarget(Faction f) {
		if (plugin.factionWars.getString(f.getName() + ".Target").equalsIgnoreCase("none")) {
			return null;
		} else {
			return FactionColl.get().getByName(plugin.factionWars.getString(f.getName() + ".Target"));
		}
	}
	
	public Faction getWarRequester(Faction f) {
		if (plugin.factionWars.getString(f.getName() + ".Requester").equalsIgnoreCase("none")) {
			return null;
		} else {
			return FactionColl.get().getByName(plugin.factionWars.getString(f.getName() + ".Requester"));
		}
	}
	
	public void setWarStatus(Faction f, String s) {
		plugin.factionWars.set(f.getName() + ".Status", s);
	}
	
	public void setWarTarget(Faction f, Faction eF) {
		if (eF != null) {
			plugin.factionWars.set(f.getName() + ".Target", eF.getName());
		} else {
			plugin.factionWars.set(f.getName() + ".Target", "none");
		}
	}
	
	public void setWarRequester(Faction f, Faction eF) {
		if (eF != null) {
			plugin.factionWars.set(f.getName() + ".Requester", eF.getName());
		} else {
			plugin.factionWars.set(f.getName() + ".Requester", "none");
		}
	}
	
}
