package me.axiom.bpl.wardeclaration.listeners;

import java.util.Calendar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsDisband;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.massivecraft.factions.event.EventFactionsMembershipChange.MembershipChangeReason;

import me.axiom.bpl.wardeclaration.PlayerStats;
import me.axiom.bpl.wardeclaration.WarDeclaration;

public class LoginListener implements Listener {

	WarDeclaration plugin;
	public LoginListener(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	/*
	 * Initialise the joining player's stats, if their faction is currently engaged.
	 */
	
	@EventHandler
	public void onPlayerJoinEvent(PlayerJoinEvent e) {

		if (plugin.getPlayerStats(e.getPlayer().getUniqueId()) == null) { // They were not previously loaded. Hence, they should be a NEW player.
			if (MPlayer.get(e.getPlayer()).hasFaction()) { // They should be in a faction.
				if (getWarEngaged(MPlayer.get(e.getPlayer()).getFaction())) { // Should be engaged.
					PlayerStats pS = new PlayerStats(e.getPlayer().getUniqueId(), 0, 0);
					plugin.playerStats.add(pS);
					plugin.logger.info(e.getPlayer().getName() + "'s stats are now being tracked!");
				}
			}
		} else {
			plugin.logger.info(plugin.getPlayerStats(e.getPlayer().getUniqueId()).getPlayer().getName() + "'s stats are already being tracked.");
		}
		
		// Begin login messaging procedures
		
		MPlayer p = MPlayer.get(e.getPlayer());
		
		if (p.hasFaction()) {
		
			Faction f = p.getFaction();	
			Faction req = getWarRequester(f);
			Faction tar = getWarTarget(f);
			Faction opp = getWarOpponent(f);
			String sta = statusToColour(getWarStatus(f));
			
			if (p.hasFaction()) {
				if (getWarEngaged(f)) {
					p.getPlayer().sendMessage("§6[§cWARS§6] §7Your war against " + opp.getColorTo(p) + opp.getName() + " §7is currently §3ENGAGED§7!");
				} else if (hasWar(f)){
					long secondsSinceDeclaration = getWarTimeOfDeclaration(f);
							
					// Declaration message.
					if (secondsSinceDeclaration > 86400) { // 86400secs = 24 hours.
						if (req == f) {
							p.getPlayer().sendMessage("§6[§cWAR§6] §r" + "§eYour §7clan recently declared war against §c" + tar.getColorTo(f) + tar.getName() + "§7! (§f" + convertMillisToDate(getWarTimeOfDeclaration(f)) + "§7");
						} else {
							p.getPlayer().sendMessage("§6[§cWAR§6] §r" + req.getColorTo(f) + req.getName() + " §7recently declared war against your clan! (§f" + convertMillisToDate(getWarTimeOfDeclaration(f)) + "§7)");
						}
					}

					p.getPlayer().sendMessage("§7War status: " + sta + "§7.");
				} else if (getWarTimeOfEnd(f) > 0) {
					long timeEnd = getWarTimeOfEnd(f);
					long timeNow = System.currentTimeMillis();
					double secsSinceEnd = (timeNow - timeEnd) / 1000;
					// End message.
					if (secsSinceEnd > 86400 && secsSinceEnd != 0) { // 86400secs = 24 hours.
						p.getPlayer().sendMessage("§6[§cWAR§6] §r" + "§eYour §7war against §c" + opp.getColorTo(f) + opp.getName() + " §7recently finished! (§f/war lookup " + f.getName() + "§7).");
					}
				}
				if (p == f.getLeader()) {
					if (getWarStatus(f).equalsIgnoreCase("pending") && f == tar) {
						p.getPlayer().sendMessage("§7To respond to the war against " + f.getColorTo(req) + req.getName() + " §7use §3'/war <accept/deny>'§7.");
					} else if (getWarStatus(f).equalsIgnoreCase("accepted") && f == req) {
						p.getPlayer().sendMessage("§e" + tar.getColorTo(f) + tar.getName() + " §ahas recently §2accepted §athe war against your clan!");
					} else if (getWarStatus(f).equalsIgnoreCase("accepted") && f == tar) {
						p.getPlayer().sendMessage("§eYour §aclan has recently §2accepted §athe war against " + req.getColorTo(f) + req.getName() + "§a!");
					} else if (getWarStatus(f).equalsIgnoreCase("denied") && f == req) {
						p.getPlayer().sendMessage("§e" + tar.getColorTo(f) + tar.getName() + " §chas recently §4denied §cthe war against your clan!");
					} else if (getWarStatus(f).equalsIgnoreCase("denied") && f == tar) {
						p.getPlayer().sendMessage("§cYour §eclan §chas recently §4denied §cthe war against " + tar.getColorTo(f) + tar.getName() + "§c!");
					} else if (getWarStatus(f).equalsIgnoreCase("cancelled") && f == tar) {
						p.getPlayer().sendMessage("§e" + req.getColorTo(f) + req.getName() + " §chas recently §4cancelled §cthe war against your clan!");
					} else if (getWarStatus(f).equalsIgnoreCase("cancelled") && f == req) {
						p.getPlayer().sendMessage("§eYour §cclan has recently §4cancelled §cthe war against " + tar.getColorTo(f) + tar.getName() + "§c!");
					} else if (getWarStatus(f).equalsIgnoreCase("forfeited") && f.getName().equalsIgnoreCase(getWarForfeiter(f).getName())) {
						p.getPlayer().sendMessage("§eYour §cclan has recently §4forfeited §cthe war against " + opp.getColorTo(f) + opp.getName() + "§c!");
					} else if (getWarStatus(f).equalsIgnoreCase("forfeited") && opp.getName().equalsIgnoreCase(getWarForfeiter(f).getName())) {
						p.getPlayer().sendMessage(opp.getColorTo(f) + opp.getName() + " §chas recently §4forfeited §cthe war against §eyour §cclan!");
					}
				}
			}
		}
	}
	
	/*
	 * Prevent players from joining a faction that is currently in a war.
	 * Prevent players from abandoning their faction's war while it's engaged.
	 */
	
	@EventHandler
	public void onPlayerJoinLeaveFaction(EventFactionsMembershipChange e) {
		
		MPlayer p = e.getMPlayer();
		Faction f = e.getNewFaction();
		MembershipChangeReason r = e.getReason();

		// Player joins new faction.
		if (r.equals(MembershipChangeReason.JOIN)) {
			if (getWarEngaged(f)) {
				p.message("§6[§cWAR§6] §e" + f.getName() + " §cis currently at war! Try again later.");
				e.setCancelled(true);
			}
		}
		
		// Player leaves their faction.
		if (r.equals(MembershipChangeReason.LEAVE) || r.equals(MembershipChangeReason.KICK)) {
			if (getWarEngaged(f)) {
				p.message("§6[§cWAR§6] §cYour clan (§e" + f.getName() + "§c) is currently at war! Try again later.");
				e.setCancelled(true);
			}
		}
		
	}
	
	/*
	 * Should a faction be disbanded, if they were in a war, forfeit it/cancel it.
	 */
	
	@EventHandler
	public void onFactionDisband(EventFactionsDisband e) {
		
		Faction f = e.getFaction();
		
		if (hasWar(f)) {
			e.getMPlayer().message("§6[§cWAR§6] §cYou're currently in a war! Forfeit or cancel this war to proceed.");
			e.setCancelled(true);
		}
		
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
	
	public long getWarTimeOfDeclaration(Faction f) {
		return plugin.factionWars.getLong(f.getName() + ".TimeOfDeclaration");
	}
	
	public long getWarTimeOfEngage(Faction f) {
		return plugin.factionWars.getLong(f.getName() + ".TimeOfEngage");
	}
	
	public long getWarTimeOfEnd(Faction f) {
		return plugin.factionWars.getLong(f.getName() + ".TimeOfEnd");
	}
	
}
