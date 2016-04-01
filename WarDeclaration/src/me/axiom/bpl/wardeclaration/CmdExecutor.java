package me.axiom.bpl.wardeclaration;

import java.util.Calendar;
import java.util.HashSet;

import org.bukkit.Bukkit;
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
				s.sendMessage("§8/war §7§lstatus");
				s.sendMessage("§8/war §7§lengage");
				s.sendMessage("§6---------------------------");
			} else {
				s.sendMessage("§6---------§c§lClan Wars§r§6---------");
				s.sendMessage("§8/war §7§lupcoming");
				s.sendMessage("§6---------------------------");
			}
		}
		
		// ENGAGE
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("engage")) {
				if (mP.hasFaction()) {
					if (mP == f.getLeader()) {
						if (hasWar(f)) {
							// The war request must be accepted in order to engage.
							if (getWarStatus(f).equalsIgnoreCase("accepted")) {
								Faction enemyF = getWarOpponent(f);
								setWarStatus(f, "engaged");
								s.sendMessage("§eYou §ahave §2engaged §athe war against " + enemyF.getColorTo(f) + enemyF.getName() + "§a!");
								f.sendMessage("§aYour clan has engaged the war against " + enemyF.getColorTo(f) + enemyF.getName() + "§a!");
								if (getWarStatus(enemyF).equalsIgnoreCase("engaged")) {
									enemyF.sendMessage("§e" + f.getColorTo(enemyF) + f.getName() + " §ahas §2engaged §athe war against your clan!");
									setWarEngaged(f, true);
									setWarEngaged(enemyF, true);
									for (Player p : Bukkit.getOnlinePlayers()) {
										p.sendMessage("§6[§cWAR§6] §7The war between " + getWarRequester(f).getColorTo((MPlayer)p) + getWarRequester(f).getName() + " §7and " + getWarTarget(f).getColorTo((MPlayer)p) + getWarTarget(f).getName() + " §7has been §3ENGAGED§7!");
									}
								} else {
									f.sendMessage("§3Waiting for " + enemyF.getColorTo(f) + enemyF.getName() + " §3to also engage the war!");
									enemyF.sendMessage("§e" + f.getColorTo(enemyF) + f.getName() + " §ahas §2engaged §athe war against your clan!");
									enemyF.sendMessage("§3You must also engage the war against " + f.getColorTo(f) + f.getName() + "§3!");
								}
								plugin.saveFactionWarsFile();
							} else if (getWarEngaged(f)) {
								s.sendMessage("§cYour war is already engaged!");
							} else if (!getWarStatus(f).equalsIgnoreCase("engaged") || !getWarStatus(f).equalsIgnoreCase("accepted")) {
								s.sendMessage("§cYou cannot engage a war that is not agreed upon! §7(" + statusToColour(getWarStatus(f)) + "§7)");
							}
						} else {
							s.sendMessage("§cYour clan is not involved in any wars!");
						}
					} else  {
						s.sendMessage("§cWars may only be engaged by your clan leader! §7(§e" + f.getLeader().getName() + "§7)");
					}
				} else {
					s.sendMessage("§cYou are not in a clan!");
				} 
			}
		}
		
		// STATUS
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("status")) {
				if (mP.hasFaction()) {
					s.sendMessage("§6-----§a§l" + f.getName() + " §c§lStatus§r§6-----");
					if (hasWar(f)) {
						String status = getWarStatus(f);
						Faction target = getWarTarget(f);
						Faction requester = getWarRequester(f);
						Faction forfeiter = getWarForfeiter(f);
						String timeOfDeclaration = convertMillisToDate(getWarTimeOfDeclaration(f));
						s.sendMessage("§7Declared by " + requester.getColorTo(f) + requester.getName() + "§7.");
						s.sendMessage("§7Targeted against " + target.getColorTo(f) + target.getName() + "§7.");
						s.sendMessage("§7Time of declaration: §e" + timeOfDeclaration + "§7.");
						s.sendMessage("§7Current status: " + statusToColour(status) + "§7.");
						s.sendMessage("§7Forfeited by " + forfeiter.getColorTo(f) + forfeiter.getName() + "§7.");
					} else {
						s.sendMessage("§cYour clan is not involved in any wars!");
					}
				} else {
					s.sendMessage("§cYou are not in a clan!");
				}
			}
		}
		
		// FORFEIT
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("forfeit")) {
				if (mP.hasFaction()) {
					if (mP == f.getLeader()) {
						if (hasWar(f)) {
							// The war request must be accepted or engaged in order to forfeit.
							if (getWarStatus(f).equalsIgnoreCase("accepted") || getWarStatus(f).equalsIgnoreCase("engaged")) {
								Faction enemyF = getWarOpponent(f);
								setWarStatus(f, "forfeited");
								setWarStatus(enemyF, "forfeited");
								setWarEngaged(f, false);
								setWarEngaged(enemyF, false);
								setWarForfeitedBy(f, f);
								setWarForfeitedBy(enemyF, f);
								s.sendMessage("§eYou §chave §4forfeited §cthe war against " + enemyF.getColorTo(f) + enemyF.getName() + "§c!");
								f.sendMessage("§cYour clan has §4forfeited §cthe war against " + enemyF.getColorTo(f) + enemyF.getName() + "§c!");
								enemyF.sendMessage(f.getColorTo(enemyF) + f.getName() + " §ahas §cforfeited §athe war against your clan!");
								for (Player p : Bukkit.getOnlinePlayers()) {
									p.sendMessage("§6[§cWAR§6] " + f.getColorTo((MPlayer)p) + f.getName() + " §7has §cFORFEITED §7the war against " + enemyF.getColorTo((MPlayer)p) + enemyF.getName() + "§7!");
								}
								plugin.saveFactionWarsFile();
							} else { // War is not accepted or engaged and thus cannot be forfeited.
								s.sendMessage("§cYou cannot forfeit a war that isn't active!");
								s.sendMessage("§cInstead, you may cancel the war. §7/war cancel§c.");
							}
						} else {
							s.sendMessage("§cYour clan is not involved in any wars!");
						}
					} else  {
						s.sendMessage("§cWars may only be forfeited by your clan leader! §7(§e" + f.getLeader().getName() + "§7)");
					}
				} else {
					s.sendMessage("§cYou are not in a clan!");
				} 
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
									setWarTimeOfDeclaration(f, Calendar.getInstance().getTimeInMillis());
									s.sendMessage("§eYou §7have requested a war against " + enemyF.getColorTo(mP) + enemyF.getName() + "§7!");
									f.sendMessage("§7Your clan has requested a war against " + enemyF.getColorTo(f) + enemyF.getName() + "§7!");
									enemyF.sendMessage(f.getColorTo(enemyF) + f.getName() + " §7has requested a war against your clan!");
									enemyF.getLeader().sendMessage("§7To respond to the war against " + f.getColorTo(enemyF) + f.getName() + " §7use §3'/war <accept/deny>'§7.");
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
							s.sendMessage("§cYou cannot declare a war against your own clan!");
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
						if (hasWar(f)) {
							if (f != getWarRequester(f)) {
								// The war request must still be pending in order to accept.
								if (getWarStatus(f).equalsIgnoreCase("pending")) {
									Faction enemyF = getWarRequester(f);
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
								s.sendMessage("§cYou cannot accept a war you requested!");
								s.sendMessage("§cOnly the enemy can accept the request. §7(" + getWarTarget(f).getColorTo(f) + getWarTarget(f).getName() + "§7)");
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
							if (f != getWarRequester(f)) {
								// The war request must still be pending in order to deny.
								if (getWarStatus(f).equalsIgnoreCase("pending")) {
									Faction enemyF = getWarRequester(f);
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
								s.sendMessage("§cYou cannot deny a war you requested!");
								s.sendMessage("§cInstead, you must cancel the war. §7/war cancel§c.");
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
							if (f == getWarRequester(f)) {
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
								s.sendMessage("§cYou cannot cancel a war you didn't request!");
								s.sendMessage("§cInstead, you must deny the war. §7/war deny§c.");
							}
						} else {
							s.sendMessage("§cYour clan has no war requests!");
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
							if (!getWarStatus(fa).equalsIgnoreCase("forfeited") && !getWarStatus(fa).equalsIgnoreCase("denied") && !getWarStatus(fa).equalsIgnoreCase("cancelled")) {
								s.sendMessage(getWarRequester(fa).getColorTo(mP) + getWarRequester(fa).getName() + " §7<--[" + statusToColour(getWarStatus(fa)) + "§7]--> " + getWarTarget(fa).getColorTo(mP) + getWarTarget(fa).getName());
								factionsProcessed.add(getWarTarget(fa).getName());
								factionsProcessed.add(getWarRequester(fa).getName());
							}
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
			
		}
		
		return m;
		
	}
	
	public String convertMillisToDate(long i) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(i);

		int Year = calendar.get(Calendar.YEAR);
		int Month = calendar.get(Calendar.MONTH);
		int Day = calendar.get(Calendar.DAY_OF_MONTH);
		
		return (Day + "/" + Month + "/" + Year);
	}
	
	public Faction getWarOpponent(Faction f) {
		if (plugin.factionWars.getString(f.getName() + ".Target").equalsIgnoreCase(f.getName())) {
			return FactionColl.get().getByName(plugin.factionWars.getString(f.getName() + ".Requester"));
		} else {
			return FactionColl.get().getByName(plugin.factionWars.getString(f.getName() + ".Target"));
		}
	}
	
	public boolean hasWar(Faction f) {
		if (plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("available") || plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("denied") || plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("forfeited") || plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("cancelled")) {
			return false;
		} else {
			return true;
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
	
	public Faction getWarForfeiter(Faction f) {
		if (plugin.factionWars.getString(f.getName() + ".ForfeitedBy").equalsIgnoreCase("none")) {
			return null;
		} else {
			return FactionColl.get().getByName(plugin.factionWars.getString(f.getName() + ".ForfeitedBy"));
		}
	}
	
	public boolean getWarEngaged(Faction f) {
		return plugin.factionWars.getBoolean(f.getName() + ".Engaged");
	}
	
	public long getWarTimeOfDeclaration(Faction f) {
		return plugin.factionWars.getLong(f.getName() + ".TimeOfDeclaration");
	}
	
	public void setWarTimeOfDeclaration(Faction f, long t) {
		plugin.factionWars.set(f.getName() + ".TimeOfDeclaration", t);
	}
	
	public void setWarForfeitedBy(Faction f, Faction eF) {
		plugin.factionWars.set(f.getName() + ".ForfeitedBy", eF.getName());
	}
	
	public void setWarEngaged(Faction f, boolean b) {
		plugin.factionWars.set(f.getName() + ".Engaged", b);
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
