package me.axiom.bpl.wardeclaration;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
		
		MPlayer mP = MPlayer.get(s);
		Faction f = null;
		
		if (s instanceof Player) {
			mP = MPlayer.get(s);
			if (mP.hasFaction()) {
				f = mP.getFaction();
			}
		}
		
		if (args.length <= 0) {
			if (s instanceof Player) {
				s.sendMessage("§6---------§c§lClan Wars§r§6---------");
				s.sendMessage("§8/war §7§ldeclare <enemy_clan>");
				s.sendMessage("§8/war §7§lcancel");
				s.sendMessage("§8/war §7§lforfeit");
				s.sendMessage("§8/war §7§lend <victorious_clan>");
				s.sendMessage("§8/war §7§laccept/deny");
				s.sendMessage("§8/war §7§lupcoming");
				s.sendMessage("§8/war §7§lstatus [clan]");
				s.sendMessage("§8/war §7§lengage");
				s.sendMessage("§8/war §7§lstats [clan]");
				s.sendMessage("§8/war §7§llookup <id/clan>");
				s.sendMessage("§6---------------------------");
				cmdSuccess = true;
				return true;
			} else {
				s.sendMessage("§6---------§c§lClan Wars§r§6---------");
				s.sendMessage("§8/war §7§lstatus [clan]");
				s.sendMessage("§8/war §7§lupcoming");
				s.sendMessage("§8/war §7§lstats [clan]");
				s.sendMessage("§8/war §7§llookup <id/clan>");
				s.sendMessage("§6---------------------------");
				cmdSuccess = true;
				return true;
			}
		}
		
		// COMMAND MENU
		if (args.length >= 1) {
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
					s.sendMessage("§8/war §7§lstats [clan]");
					s.sendMessage("§8/war §7§llookup <id/clan>");
					s.sendMessage("§6---------------------------");
					cmdSuccess = true;
					return true;
				}
			} else if (args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("?") || args.length == 0) {
					s.sendMessage("§6---------§c§lClan Wars§r§6---------");
					s.sendMessage("§8/war §7§lupcoming");
					s.sendMessage("§8/war §7§lstats [clan]");
					s.sendMessage("§8/war §7§llookup <id/clan>");
					s.sendMessage("§6---------------------------");
					cmdSuccess = true;
					return true;
			}
		}
		
		if (!(s instanceof Player)) {
			// They're in the console, hence they must only have typed the 'upcoming' and 'lookup' command.
			if (args.length >= 1) {
				if (!(args[0].equalsIgnoreCase("lookup") || args[0].equalsIgnoreCase("upcoming") || args[0].equalsIgnoreCase("status"))) {
					// They typed a command only available in-game.
					s.sendMessage("§cUnfortunately, you must be in-game to use other commands.");
					return true;
				}
			}
		}
		
		// STATS [clan]
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("stats")) {
				cmdSuccess = true;
				
				// THEIR CLAN
				if (args.length == 1) { // Attempt to retrieve their faction's current stats.
					if (mP.hasFaction()) { // They must have a faction in order to show their stats.
						// Are they currently engaged?
						if (getWarEngaged(f)) {
							
							Faction enemyF = getWarOpponent(f);
							
							s.sendMessage("§6-----§a§l" + f.getName() + " §7vs. §c§l" + enemyF.getName() + " §c§lStats§r§6-----");
							
							// Send player stats.
							PlayerStats pStats = plugin.getPlayerStats(mP.getUuid());
							s.sendMessage("§7Your §lKills: §r§a" + pStats.getKills());
							s.sendMessage("§7Your §lDeaths: §r§c" + pStats.getDeaths());
							s.sendMessage("§7Your §lK/D: §r" + colorCodeKD(pStats.getKD()) + roundFloatTwo(pStats.getKD()) + "§7.");
							
							// Send overall faction stats.
							s.sendMessage("§7---- §6§l" + f.getName() + "§7 ----");
							int factionTotalKills = 0;
							int factionTotalDeaths = 0;
							int enemyFactionTotalKills = 0;
							int enemyFactionTotalDeaths = 0;
							
							int fPlayersScanned = 0;
							int fPlayersToScan = f.getMPlayers().size();
							int eFPlayersScanned = 0;
							int eFPlayersToScan = enemyF.getMPlayers().size();
							
							// Their FACTION Stats
							for (MPlayer p : f.getMPlayers()) {
								PlayerStats pS = plugin.getPlayerStats(p.getUuid());
								factionTotalKills += pS.getKills();
								factionTotalDeaths += pS.getDeaths();
								s.sendMessage("§e" + p.getName() + " §8has §7" + pS.getKills() + " kills §8and §7" + pS.getDeaths() + " deaths §8with a K/D of " + colorCodeKD(pS.getKD()) + roundFloatTwo(pS.getKD()) + "§8.");
								fPlayersScanned++;
								if (fPlayersScanned >= fPlayersToScan) { // That's the last of this faction.
									float fKD = (float)((float)factionTotalKills / (float)factionTotalDeaths);
									if (factionTotalDeaths == 0) {
										fKD = 0;
									}
									s.sendMessage(f.getColorTo(f) + f.getName() + " §8in total, has §a" + factionTotalKills + " §7kills §8and §c" + factionTotalDeaths + " §7deaths §8with a §7K/D §8of §r" + colorCodeKD(fKD) + roundFloatTwo(fKD) + "§7!");
								}
							}
							
							s.sendMessage("§7---- §6§l" + enemyF.getName() + "§7 ----");
							
							// Enemy's FACTION Stats.
							for (MPlayer p : enemyF.getMPlayers()) {
								PlayerStats pS = plugin.getPlayerStats(p.getUuid());
								enemyFactionTotalKills += pS.getKills();
								enemyFactionTotalDeaths += pS.getDeaths();
								s.sendMessage("§e" + p.getName() + " §8has §7" + pS.getKills() + " kills §8and §7" + pS.getDeaths() + " deaths §8with a K/D of " + colorCodeKD(pS.getKD()) + roundFloatTwo(pS.getKD()) + "§8.");
								eFPlayersScanned++;
								if (eFPlayersScanned >= eFPlayersToScan) { // That's the last of this faction.
									float eFKD = (float)((float)enemyFactionTotalKills / (float)enemyFactionTotalDeaths);
									if (enemyFactionTotalDeaths == 0) {
										eFKD = 0;
									}
									s.sendMessage(enemyF.getColorTo(f) + enemyF.getName() + " §8in total, has §a" + enemyFactionTotalKills + " §7kills §8and §c" + enemyFactionTotalDeaths + " §7deaths §8with a §7K/D §8of §r" + colorCodeKD(eFKD) + roundFloatTwo(eFKD) + "§7!");
								}
							}

						} else {
							s.sendMessage("§cYour clan is currently not engaged in a war!");
						}
					} else {
						s.sendMessage("§cYou are not in a clan!");
						s.sendMessage("§cInstead, you may specify a clan's stats. §3/war stats [clan]§c.");
					}
				}
				
				// OTHER CLAN
				if (args.length > 1) { // Attempt to retrieve the specified clan's stats.
					String factionName = args[1];
					Faction fac = FactionColl.get().getByName(args[1]);
					if (fac != null) { // Does the faction they specified exist?
						// Are they currently engaged?
						if (getWarEngaged(fac)) {
							
							Faction enemyF = getWarOpponent(fac);
							
							s.sendMessage("§6-----§a§l" + fac.getName() + " §7vs. §c§l" + enemyF.getName() + " §c§lStats§r§6-----");
							
							// Send overall faction stats.
							s.sendMessage("§7---- §6§l" + fac.getName() + "§7 ----");
							int factionTotalKills = 0;
							int factionTotalDeaths = 0;
							int enemyFactionTotalKills = 0;
							int enemyFactionTotalDeaths = 0;
							
							int fPlayersScanned = 0;
							int fPlayersToScan = fac.getMPlayers().size();
							int eFPlayersScanned = 0;
							int eFPlayersToScan = enemyF.getMPlayers().size();
							
							// Their FACTION Stats
							for (MPlayer p : fac.getMPlayers()) {
								PlayerStats pS = plugin.getPlayerStats(p.getUuid());
								factionTotalKills += pS.getKills();
								factionTotalDeaths += pS.getDeaths();
								s.sendMessage("§e" + p.getName() + " §8has §7" + pS.getKills() + " kills §8and §7" + pS.getDeaths() + " deaths §8with a K/D of " + colorCodeKD(pS.getKD()) + roundFloatTwo(pS.getKD()) + "§8.");
								fPlayersScanned++;
								if (fPlayersScanned >= fPlayersToScan) { // That's the last of this faction.
									float fKD = (float)((float)factionTotalKills / (float)factionTotalDeaths);
									if (factionTotalDeaths == 0) {
										fKD = 0;
									}
									s.sendMessage(fac.getColorTo(f) + fac.getName() + " §8in total, has §a" + factionTotalKills + " §7kills §8and §c" + factionTotalDeaths + " §7deaths §8with a §7K/D §8of §r" + colorCodeKD(fKD) + roundFloatTwo(fKD) + "§7!");
								}
							}
							
							s.sendMessage("§7---- §6§l" + enemyF.getName() + "§7 ----");
							
							// Enemy's FACTION Stats.
							for (MPlayer p : enemyF.getMPlayers()) {
								PlayerStats pS = plugin.getPlayerStats(p.getUuid());
								enemyFactionTotalKills += pS.getKills();
								enemyFactionTotalDeaths += pS.getDeaths();
								s.sendMessage("§e" + p.getName() + " §8has §7" + pS.getKills() + " kills §8and §7" + pS.getDeaths() + " deaths §8with a K/D of " + colorCodeKD(pS.getKD()) + roundFloatTwo(pS.getKD()) + "§8.");
								eFPlayersScanned++;
								if (eFPlayersScanned >= eFPlayersToScan) { // That's the last of this faction.
									float eFKD = (float)((float)enemyFactionTotalKills / (float)enemyFactionTotalDeaths);
									if (enemyFactionTotalDeaths == 0) {
										eFKD = 0;
									}
									s.sendMessage(enemyF.getColorTo(f) + f.getName() + " §8in total, has §a" + enemyFactionTotalKills + " §7kills §8and §c" + enemyFactionTotalDeaths + " §7deaths §8with a §7K/D §8of §r" + colorCodeKD(eFKD) + roundFloatTwo(eFKD) + "§7!");
								}
							}
							
							
						} else {
							s.sendMessage("§e" + fac.getName() + " §cis currently not engaged in a war!");
						}
					} else {
						s.sendMessage("§e" + factionName + " §cdoes not exist!");
					}
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
								plugin.engageConfirmation.add(f);
								s.sendMessage("§eYou §ahave §2engaged §athe war against " + enemyF.getColorTo(f) + enemyF.getName() + "§a!");
								f.sendMessage("§aYour clan has engaged the war against " + enemyF.getColorTo(f) + enemyF.getName() + "§a!");
								if (plugin.engageConfirmation.contains(enemyF)) { // The enemy has already engaged their part of the war.
									plugin.engageConfirmation.remove(f);
									plugin.engageConfirmation.remove(enemyF);
									enemyF.sendMessage("§e" + f.getColorTo(enemyF) + f.getName() + " §ahas §2engaged §athe war against your clan!");
									setWarEngaged(f, true);
									setWarEngaged(enemyF, true);
									setWarStatus(f, "engaged");
									setWarStatus(enemyF, "engaged");
									plugin.initialisePlayerStats(); // Make sure we are loading the necessary players.
									long currentTime = System.currentTimeMillis();
									setWarTimeOfEngage(f, currentTime);
									setWarTimeOfEngage(enemyF, currentTime);
									plugin.resetPlayerStatsForFaction(f);
									plugin.resetPlayerStatsForFaction(enemyF);
									plugin.saveFactionWarsFile();
									for (Player p : Bukkit.getOnlinePlayers()) {
										p.sendMessage("§6[§cWAR§6] §7The war between " + getWarRequester(f).getColorTo(MPlayer.get(p)) + getWarRequester(f).getName() + " §7and " + getWarTarget(f).getColorTo(MPlayer.get(p)) + getWarTarget(f).getName() + " §7has §3BEGUN§7!");
									}
								} else {
									f.sendMessage("§3Waiting for " + enemyF.getColorTo(f) + enemyF.getName() + " §3to also engage the war!");
									enemyF.sendMessage("§e" + f.getColorTo(enemyF) + f.getName() + " §ahas §2engaged §athe war against your clan!");
									enemyF.sendMessage("§3Your leader must also engage the war against " + f.getColorTo(f) + f.getName() + "§3!");
								}
								plugin.saveFactionWarsFile();
							} else if (getWarEngaged(f)) {
								s.sendMessage("§cYour war is already engaged!");
							} else if (plugin.engageConfirmation.contains(f)) { // They've already requested to engage!
								s.sendMessage("§cYou have already requested to engage the war! You must wait for the enemy to also engage.");
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
				Faction fac;
				
				// Do they want to see someone else's status, or their own clans?
				if (args.length == 1) {
					if (mP.hasFaction()) {
						fac = f;
					} else {
						s.sendMessage("§cYou are not in a clan!");
						return true;
					}
					
				} else {
					fac = FactionColl.get().getByName(args[1]);
				}

				if (fac != null) {
					s.sendMessage("§6-----§a§l" + fac.getName() + " §c§lStatus§r§6-----");
					if (hasStatusWar(fac)) {
						String status = getWarStatus(fac);
						Faction target = getWarTarget(fac);
						Faction requester = getWarRequester(fac);
						String timeOfDeclaration = convertMillisToDate(getWarTimeOfDeclaration(fac));
						s.sendMessage("§7Declared by " + requester.getColorTo(f) + requester.getName() + "§7.");
						s.sendMessage("§7Targeted against " + target.getColorTo(f) + target.getName() + "§7.");
						s.sendMessage("§7Time of declaration: §e" + timeOfDeclaration + "§7.");
						s.sendMessage("§7Current status: " + statusToColour(status) + "§7.");
					} else if (fac == f) {
						s.sendMessage("§cYour clan is not involved in any wars!");
					} else {
						s.sendMessage("§e" + fac.getName() + " §cis not involved in any wars!");
					}
				} else {
					s.sendMessage("§e" + args[1] + " §cdoes not exist!");
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
								plugin.addWarLog(enemyF, f, "forfeit"); // The opposite faction wins.
								setWarStatus(f, "forfeited");
								setWarStatus(enemyF, "forfeited");
								setWarEngaged(f, false);
								setWarEngaged(enemyF, false);
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
						s.sendMessage(victory.getColorTo(f) + victory.getName() + " §7had a K/D Ratio of " + colorCodeKD(victoryKD) + roundFloatTwo(victoryKD) + "§7.");
						s.sendMessage(defeat.getColorTo(f) + defeat.getName() + " §7had §8" + defeatKills + " kills §7and §8" + defeatDeaths + " deaths§7.");
						s.sendMessage(defeat.getColorTo(f) + defeat.getName() + " §7had a K/D Ratio of " + colorCodeKD(defeatKD) + roundFloatTwo(defeatKD) + "§7.");
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
								kd = (float)kills;
							} else {
								kd = (float)kills / (float)deaths;
							}
							String playerName = "";
							boolean foundPlayer = false;
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (!foundPlayer) {
									if (p.getUniqueId().toString().equalsIgnoreCase(value)) {
										playerName = p.getName();
										s.sendMessage("§e" + playerName + " §8had §7" + kills + " kills §8and §7" + deaths + " deaths §8with a K/D of " + colorCodeKD(kd) + roundFloatTwo(kd) + "§8.");
										foundPlayer = true;
									}
								}
							}
							for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
								if (!foundPlayer) {
									if (p.getUniqueId().toString().equalsIgnoreCase(value)) {
										playerName = p.getName();
										s.sendMessage("§e" + playerName + " §8had §7" + kills + " kills §8and §7" + deaths + " deaths §8with a K/D of " + colorCodeKD(kd) + roundFloatTwo(kd) + "§8.");
										foundPlayer = true;
									}
								}
							}
						}
						
						s.sendMessage("§7------ §6§lDEFEAT STATS§7 ------");
						
						ConfigurationSection sectionDefeat = plugin.factionWarLog.getConfigurationSection(args[1] + "." + defeat.getName());
						
						for (String value : sectionDefeat.getKeys(false)) {
							int kills = plugin.factionWarLog.getInt(args[1] + "." + defeat.getName() + "." + value + ".Kills");
							int deaths = plugin.factionWarLog.getInt(args[1] + "." + defeat.getName() + "." + value + ".Deaths");
							float kd = 0;
							if (deaths == 0) {
								kd = (float)kills;
							} else {
								kd = (float)kills / (float)deaths;
							}
							String playerName = "";
							boolean foundPlayer = false;
							for (Player p : Bukkit.getOnlinePlayers()) {
								if (!foundPlayer) {
									if (p.getUniqueId().toString().equalsIgnoreCase(value)) {
										playerName = p.getName();
										s.sendMessage("§e" + playerName + " §8had §7" + kills + " kills §8and §7" + deaths + " deaths §8with a K/D of " + colorCodeKD(kd) + roundFloatTwo(kd) + "§8.");
										foundPlayer = true;
									}
								}
							}
							for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
								if (!foundPlayer) {
									if (p.getUniqueId().toString().equalsIgnoreCase(value)) {
										playerName = p.getName();
										s.sendMessage("§e" + playerName + " §8had §7" + kills + " kills §8and §7" + deaths + " deaths §8with a K/D of " + colorCodeKD(kd) + roundFloatTwo(kd) + "§8.");
										foundPlayer = true;
									}
								}
							}
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
							if (getWarStatus(f).equalsIgnoreCase("engaged")) { // They must be engaged to proceed.
								if (plugin.factionVictoryDecider.containsKey(getWarOpponent(f))) { // The opponent has also request the end.								
									if (args[1].equalsIgnoreCase(getWarRequester(f).getName()) || args[1].equalsIgnoreCase(getWarTarget(f).getName())) {
										if (plugin.factionVictoryDecider.get(getWarOpponent(f)).getName().equalsIgnoreCase(args[1])) {
											// The victory clan specified matches both clans opinions.
											Faction victory = FactionColl.get().getByName(args[1]);
											Faction loss = getWarOpponent(victory);
											plugin.logger.info(loss.getName() + " was defeated by " + victory.getName());
											plugin.addWarLog(victory, loss, "ended");
											setWarStatus(f, "available");
											setWarStatus(loss, "available");
											setWarEngaged(f, false);
											setWarEngaged(loss, false);
											for (Player p : Bukkit.getOnlinePlayers()) {
												p.sendMessage("§6[§cWAR§6] §a" + victory.getName() + " §7was victorious in the war against §c" + loss.getName() + "§7!");
											}
											plugin.saveFactionWarsFile();
											plugin.saveFactionWarsLogFile();
										} else {
											s.sendMessage("§cThe clan you specified does not match the victorious clan specified by §e" + getWarOpponent(f).getName() + "!");
											s.sendMessage("§cPlease come to an agreement as to who won!");
											getWarOpponent(f).getLeader().message("§e" + f.getName() + " §cdid not match the victorious clan specified by you.");
											getWarOpponent(f).getLeader().message("§cPlease come to an agreement as to who won!");
											plugin.factionVictoryDecider.remove(getWarOpponent(f));
										}
									} else { // The faction decided to win is NOT in the war.
										s.sendMessage("§cThe clan specified for victory is not in this war!");
									}
								} else if (args[1].equalsIgnoreCase(getWarRequester(f).getName()) || args[1].equalsIgnoreCase(getWarTarget(f).getName())) {
									// They're the first to decide, so also notify the other clan to request an end too.
									plugin.factionVictoryDecider.put(f, FactionColl.get().getByName(args[1]));
									getWarOpponent(f).getLeader().message(f.getColorTo(getWarOpponent(f)) + f.getName() + " §7has request to end the war with §e" + FactionColl.get().getByName(args[1]).getName() + " §7being victorious!");
									s.sendMessage("§eYou §ahave request to end the war with §e" + FactionColl.get().getByName(args[1]).getName() + " §abeing victorious!");
								} else {
									// The faction decided to win is NOT in the war.
									s.sendMessage("§cThe clan specified for victory is not in this war!");
								}
							} else { // War is not engaged and is accepted.
								s.sendMessage("§cYou cannot end a war that isn't engaged!");
								s.sendMessage("§cInstead, you must cancel/forfeit the war. §7/war cancel/forfeit§c.");
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
									enemyF.getLeader().message("§7To respond to the war against " + f.getColorTo(enemyF) + f.getName() + " §7use §3'/war <accept/deny>'§7.");
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
							String status = getWarStatus(fa);
							if (!status.equalsIgnoreCase("denied") && !status.equalsIgnoreCase("cancelled") && !status.equalsIgnoreCase("forfeited") && !status.equalsIgnoreCase("available")) {
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
			switch (args[0]) {
			case "stats":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war stats [clan]");
				break;
			case "status":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war status [clan]");
				break;
			case "lookup":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war lookup <id/clan>");
				break;
			case "declare":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war declare <clan>");
				break;
			case "end":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war end <victorious_clan>");
				break;
			case "forfeit":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war ");
				break;
			case "cancel":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war ");
				break;
			case "accept":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war ");
				break;
			case "deny":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war ");
				break;
			case "upcoming":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war ");
				break;
			case "engage":
				s.sendMessage("§6[§cWAR§6] §4§lUsage: §r§c/war ");
				break;
			default:
				s.sendMessage("§6[§cWAR§6] §4§lInvalid Command. §r§cType §3/war [help|?]§c.");
				break;
			}
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
		String status = plugin.factionWars.getString(f.getName() + ".Status");
		if (status.equalsIgnoreCase("forfeited") || status.equalsIgnoreCase("available") || status.equalsIgnoreCase("denied") || status.equalsIgnoreCase("cancelled")) {
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
	
public String roundFloatTwo(float n) {
		
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);
		return df.format((double)n);
		
	}
	
	public ChatColor colorCodeKD(float kd) {
		
		if (kd == 1) {
			return ChatColor.YELLOW;
		} else if (kd < 1) {
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
