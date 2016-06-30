package me.axiom.bpl.wardeclaration;

import java.util.Calendar;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
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
		
		boolean cmdSuccess = false;
		
		MPlayer mP = null;
		Faction f = null;
		
		if (s instanceof Player) {
			mP = MPlayer.get(s);
			if (mP.hasFaction()) {
				f = mP.getFaction();
			}
		}
		
		// COMMAND MENU
		if (args.length == 0 || args.length >= 1) {
			if (s instanceof Player) { // Check if they're in-game or in console.
				if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?") || args.length == 0) {
					s.sendMessage("§6---------§c§lClan Wars§r§6---------");
					s.sendMessage("§8/war §7§ldeclare <enemy_clan>");
					s.sendMessage("§8/war §7§lcancel");
					s.sendMessage("§8/war §7§lforfeit");
					s.sendMessage("§8/war §7§lend <victorious_clan>");
					s.sendMessage("§8/war §7§laccept/deny");
					s.sendMessage("§8/war §7§lupcoming");
					s.sendMessage("§8/war §7§lstatus");
					s.sendMessage("§8/war §7§lengage");
					s.sendMessage("§8/war §7§llookup <id/clan>");
					s.sendMessage("§6---------------------------");
					cmdSuccess = true;
				}
			} else {
				if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?") || args.length == 0) {
					s.sendMessage("§6---------§c§lClan Wars§r§6---------");
					s.sendMessage("§8/war §7§lupcoming");
					s.sendMessage("§8/war §7§llookkup <id/clan>");
					s.sendMessage("§6---------------------------");
					cmdSuccess = true;
				}
			}
		}
		
		// ENGAGE
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("engage")) {
				cmdSuccess = true;
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
									long currentTime = System.currentTimeMillis();
									setWarTimeOfEngage(f, currentTime);
									setWarTimeOfEngage(enemyF, currentTime);
									plugin.resetPlayerStatsForFaction(f);
									plugin.resetPlayerStatsForFaction(enemyF);
									for (Player p : Bukkit.getOnlinePlayers()) {
										p.sendMessage("§6[§cWAR§6] §7The war between " + getWarRequester(f).getColorTo(MPlayer.get(p)) + getWarRequester(f).getName() + " §7and " + getWarTarget(f).getColorTo(MPlayer.get(p)) + getWarTarget(f).getName() + " §7has §3BEGUN§7!");
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
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("status")) {
				cmdSuccess = true;
				if (mP.hasFaction()) {
					s.sendMessage("§6-----§a§l" + f.getName() + " §c§lStatus§r§6-----");
					if (hasStatusWar(f)) {
						String status = getWarStatus(f);
						Faction target = getWarTarget(f);
						Faction requester = getWarRequester(f);
						String timeOfDeclaration = convertMillisToDate(getWarTimeOfDeclaration(f));
						s.sendMessage("§7Declared by " + requester.getColorTo(f) + requester.getName() + "§7.");
						s.sendMessage("§7Targeted against " + target.getColorTo(f) + target.getName() + "§7.");
						s.sendMessage("§7Time of declaration: §e" + timeOfDeclaration + "§7.");
						s.sendMessage("§7Current status: " + statusToColour(status) + "§7.");
					} else {
						s.sendMessage("§cYour clan is not involved in any wars!");
					}
				} else {
					s.sendMessage("§cYou are not in a clan!");
				}
			}
		}
		
		// FORFEIT
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("forfeit")) {
				cmdSuccess = true;
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
								plugin.addWarLog(enemyF, f, "forfeit");
								s.sendMessage("§eYou §chave §4forfeited §cthe war against " + enemyF.getColorTo(f) + enemyF.getName() + "§c!");
								f.sendMessage("§cYour clan has §4forfeited §cthe war against " + enemyF.getColorTo(f) + enemyF.getName() + "§c!");
								enemyF.sendMessage(f.getColorTo(enemyF) + f.getName() + " §ahas §cforfeited §athe war against your clan!");
								for (Player p : Bukkit.getOnlinePlayers()) {
									p.sendMessage("§6[§cWAR§6] " + f.getColorTo(MPlayer.get(p)) + f.getName() + " §7has §cFORFEITED §7the war against " + enemyF.getColorTo(MPlayer.get(p)) + enemyF.getName() + "§7!");
								}
								plugin.saveFactionWarsFile();
								plugin.saveFactionWarsLogFile();
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
		
		// LOOKUP <id/clan>
		if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("lookup")) {
				cmdSuccess = true;
				
				boolean valid = true;
				
				if (FactionColl.get().getByName(args[1]) != null) { // Show them all War IDs that include the specific clan.
					
					HashSet<String> ids = new HashSet<String>();
					
					for (String i : plugin.factionWarLog.getKeys(false)) {
						ConfigurationSection section = plugin.factionWarLog.getConfigurationSection(i);
						if (section.contains(FactionColl.get().getByName(args[1]).getName())) {
							ids.add(i);
						}
					}
					
					if (!(ids.size() <= 0)) {
						s.sendMessage("§6[§f?§6] §e" + FactionColl.get().getByName(args[1]).getName() + " §7is included in the following wars:");
						s.sendMessage("§6[§f?§6] §7Use §2/war lookup <id>§7, to view the statistics of that war.");
						s.sendMessage("§6[§f?§6] §7IDs: §a" + ids.toString());
					} else {
						s.sendMessage("§6[§c!!§6] §e" + FactionColl.get().getByName(args[1]).getName() + " §chas not featured in any wars.");
					}
					
				} else if (isParsable(args[1])) { // They probably typed a War ID.
					
					if (plugin.factionWarLog.contains(args[1])) { // That ID exists. Give them info about the war.
						
						String method = plugin.factionWarLog.getString(args[1] + ".Method");
						Faction target = FactionColl.get().getByName(plugin.factionWarLog.getString(args[1] + ".Target"));
						Faction requester = FactionColl.get().getByName(plugin.factionWarLog.getString(args[1] + ".Requester"));
						String timeOfDeclaration = convertMillisToDate(plugin.factionWarLog.getLong(args[1] + ".TimeOfDeclaration"));
						String timeOfEngage = convertMillisToDate(plugin.factionWarLog.getLong(args[1] + ".TimeOfEngage"));
						String timeOfEnd = convertMillisToDate(plugin.factionWarLog.getLong(args[1] + ".TimeOfEnd"));
						Faction victory = FactionColl.get().getByName(plugin.factionWarLog.getString(args[1] + ".Victory"));
						Faction defeat = FactionColl.get().getByName(plugin.factionWarLog.getString(args[1] + ".Defeat"));
						int victoryKills = plugin.factionWarLog.getInt(args[1]+ ".VictoryKills");
						int victoryDeaths = plugin.factionWarLog.getInt(args[1]+ ".VictoryDeaths");
						int defeatKills = plugin.factionWarLog.getInt(args[1]+ ".DefeatKills");
						int defeatDeaths = plugin.factionWarLog.getInt(args[1]+ ".DefeatDeaths");
						int victoryKD = 0;
						if (victoryDeaths == 0) {
							victoryKD = victoryKills;
						} else {
							victoryKD = victoryKills / victoryDeaths;
						}
						int defeatKD = 0;
						if (defeatDeaths == 0) {
							defeatKD = defeatKills;
						} else {
							defeatKD = defeatKills / defeatDeaths;
						}
						
						s.sendMessage("§7Declared by " + requester.getColorTo(f) + requester.getName() + "§7.");
						s.sendMessage("§7Targeted against " + target.getColorTo(f) + target.getName() + "§7.");
						s.sendMessage("§7Time of declaration: §e" + timeOfDeclaration + "§7.");
						s.sendMessage("§7Time of war engage: §e" + timeOfEngage + "§7.");
						s.sendMessage("§7Time of war end: §e" + timeOfEnd + "§7.");
						s.sendMessage(victory.getColorTo(f) + victory.getName() + " §7was §aVictorious§7!");
						s.sendMessage(defeat.getColorTo(f) + defeat.getName() + " §7was §cDefeated§7.");
						s.sendMessage(victory.getColorTo(f) + victory.getName() + " §7had §8" + victoryKills + " kills §7and §8" + victoryDeaths + " deaths§7.");
						s.sendMessage(victory.getColorTo(f) + victory.getName() + " §7had a K/D Ratio of " + colorCodeKD(victoryKD) + victoryKD + "§7.");
						s.sendMessage(defeat.getColorTo(f) + defeat.getName() + " §7had §8" + defeatKills + " kills §7and §8" + defeatDeaths + " deaths§7.");
						s.sendMessage(defeat.getColorTo(f) + defeat.getName() + " §7had a K/D Ratio of " + colorCodeKD(defeatKD) + defeatKD + "§7.");
						if (method.equalsIgnoreCase("forfeited")) {
							s.sendMessage(defeat.getColorTo(f) + defeat.getName() + " §7had §cFORFEITED §7the war!");
						}
						
						s.sendMessage("§7---- §6§lVICTORIOUS STATS§7 ----");
						
						ConfigurationSection sectionVictory = plugin.factionWarLog.getConfigurationSection(args[1] + "." + victory.getName());
						
						for (String value : sectionVictory.getKeys(false)) {
							int kills = plugin.factionWarLog.getInt(args[1] + "." + victory.getName() + "." + value + ".Kills");
							int deaths = plugin.factionWarLog.getInt(args[1] + "." + victory.getName() + "." + value + ".Deaths");
							float kd = 0;
							if (deaths == 0) {
								kd = kills;
							} else {
								kd = kills / deaths;
							}
							s.sendMessage("§e" + value + " §8had §7" + kills + " kills §8and §7" + deaths + " deaths §8with a K/D of " + colorCodeKD(kd) + kd + "§8.");
						}
						
						s.sendMessage("§7------ §6§lDEFEAT STATS§7 ------");
						
						ConfigurationSection sectionDefeat = plugin.factionWarLog.getConfigurationSection(args[1] + "." + defeat.getName());
						
						for (String value : sectionDefeat.getKeys(false)) {
							int kills = plugin.factionWarLog.getInt(args[1] + "." + defeat.getName() + "." + value + ".Kills");
							int deaths = plugin.factionWarLog.getInt(args[1] + "." + defeat.getName() + "." + value + ".Deaths");
							float kd = 0;
							if (deaths == 0) {
								kd = kills;
							} else {
								kd = kills / deaths;
							}
							s.sendMessage("§e" + value + " §8had §7" + kills + " kills §8and §7" + deaths + " deaths §8with a K/D of " + colorCodeKD(kd) + kd + "§8.");
						}
						
					} else {
						valid = false;
					}
					
				} else {
					valid = false;
				}
				
				if (!valid) {
					s.sendMessage("§6[§c!§6] §cNo Clan or War ID was found for §e" + args[1] + "§c.");
				}
 			}
		}
		
		// END WAR
		if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("end")) {
				cmdSuccess = true;
				if (mP.hasFaction()) {
					if (mP == f.getLeader()) {
						if (hasWar(f)) {
							// The war must be engaged in order to end.
							// The player requesting must be from the attacking clan.
							if (plugin.factionVictoryDecider.containsKey(getWarOpponent(f))) { // The opponent has also request the end.
								if (getWarStatus(f).equalsIgnoreCase("engaged")) {
									if (args[1].equalsIgnoreCase(getWarRequester(f).getName()) || args[1].equalsIgnoreCase(getWarTarget(f).getName())) {
										if (plugin.factionVictoryDecider.get(getWarOpponent(f)).getName().equalsIgnoreCase(args[1])) {
											// The victory clan specified matches both clans opinions.
											Faction enemyF = getWarTarget(f);
											Faction victory = FactionColl.get().getByName(args[1]);
											setWarTarget(f, null);
											setWarStatus(f, "available");
											setWarRequester(f, null);
											setWarStatus(enemyF, "available");
											setWarEngaged(f, false);
											setWarEngaged(enemyF, false);
											plugin.addWarLog(victory, getWarOpponent(victory), "ended");
											for (Player p : Bukkit.getOnlinePlayers()) {
												p.sendMessage("§6[§cWAR§6] §a" + victory.getName() + " §7was victorious in the war against §c" + enemyF.getName() + "§7!");
											}
											plugin.saveFactionWarsFile();
											plugin.saveFactionWarsLogFile();
										} else {
											s.sendMessage("§cThe clan you specified does not match the victorious clan specified by §e" + getWarOpponent(f).getName() + "!");
											s.sendMessage("§cPlease come to an agreement as to who won!");
											getWarOpponent(f).getLeader().sendMessage("§e" + f.getName() + " §cdid not match the victorious clan specified by you.");
											getWarOpponent(f).getLeader().sendMessage("§cPlease come to an agreement as to who won!");
											plugin.factionVictoryDecider.remove(getWarOpponent(f));
										}
									} else { // The faction decided to win is NOT in the war.
										s.sendMessage("§cThe clan specified for victory is not in this war!");
									}
								} else { // War is not engaged and is accepted.
									s.sendMessage("§cYou cannot end a war that isn't engaged!");
									s.sendMessage("§cInstead, you must cancel/forfeit the war. §7/war cancel/forfeit§c.");
								}
							} else {
								// They're the first to decide, so also notify the other clan to request an end too.
								plugin.factionVictoryDecider.put(f, FactionColl.get().getByName(args[1]));
								getWarOpponent(f).getLeader().sendMessage(f.getColorTo(getWarOpponent(f)) + f.getName() + " §7has request to end the war with §e" + FactionColl.get().getByName(args[1]).getName() + " §7being victorious!");
								s.sendMessage("§eYou §ahave request to end the war with §e" + FactionColl.get().getByName(args[1]).getName() + " §abeing victorious!");
								
							}
						} else {
							s.sendMessage("§cYour clan is not involved in any wars!");
						}
					} else {
						s.sendMessage("§cWars can only be ended by your clan leader! §7(§e" + f.getLeader().getName() + "§7)");
					}
				} else {
					s.sendMessage("§cYou are not in a clan!");
				}
			}
		}
		
		// DECLARE
		if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("declare")) {
				cmdSuccess = true;
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
									setWarTimeOfDeclaration(enemyF, Calendar.getInstance().getTimeInMillis());
									s.sendMessage("§eYou §7have requested a war against " + enemyF.getColorTo(mP) + enemyF.getName() + "§7!");
									f.sendMessage("§7Your clan has requested a war against " + enemyF.getColorTo(f) + enemyF.getName() + "§7!");
									enemyF.sendMessage(f.getColorTo(enemyF) + f.getName() + " §7has requested a war against your clan!");
									enemyF.getLeader().sendMessage("§7To respond to the war against " + f.getColorTo(enemyF) + f.getName() + " §7use §3'/war <accept/deny>'§7.");
									plugin.saveFactionWarsFile();
									for (Player p : Bukkit.getOnlinePlayers()) {
										p.sendMessage("§6[§cWAR§6] §e" + f.getName() + " §7has declared war against §e" + FactionColl.get().getByName(args[1]).getName() + "§7!");
									}
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
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("accept")) {
				cmdSuccess = true;
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
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("deny")) {
				cmdSuccess = true;
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
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("cancel")) {
				cmdSuccess = true;
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
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("upcoming")) {
				cmdSuccess = true;
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
		
		if (!cmdSuccess) {
			s.sendMessage("§6[§cWAR§6] §cInvalid command/arguments. Type, '§7/clan [? | help]§c'.");;
		}
		
		return false;
		
	}
	
	public boolean isParsable(String input){
	    boolean parsable = true;
	    try{
	        Integer.parseInt(input);
	    }catch(NumberFormatException e){
	        parsable = false;
	    }
	    return parsable;
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
		int Month = calendar.get(Calendar.MONTH) + 1;
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
		if (plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("forfeited") || plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("available") || plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("denied") || plugin.factionWars.getString(f.getName() + ".Status").equalsIgnoreCase("cancelled")) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean hasStatusWar(Faction f) {
		String status = plugin.factionWars.getString(f.getName() + ".Status");
		if (status.equalsIgnoreCase("pending") || status.equalsIgnoreCase("engaged") || status.equalsIgnoreCase("accepted")) {
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
	
	public boolean getWarEngaged(Faction f) {
		return plugin.factionWars.getBoolean(f.getName() + ".Engaged");
	}
	
	public long getWarTimeOfDeclaration(Faction f) {
		return plugin.factionWars.getLong(f.getName() + ".TimeOfDeclaration");
	}
	
	public void setWarTimeOfDeclaration(Faction f, long t) {
		plugin.factionWars.set(f.getName() + ".TimeOfDeclaration", t);
	}
	
	public long getWarTimeOfEngage(Faction f) {
		return plugin.factionWars.getLong(f.getName() + ".TimeOfEngage");
	}
	
	public void setWarTimeOfEngage(Faction f, long t) {
		plugin.factionWars.set(f.getName() + ".TimeOfEngage", t);
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
	
	public ChatColor colorCodeKD(float kd) {
		
		if (kd < 1) {
			return ChatColor.RED;
		} else {
			return ChatColor.GREEN;
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
