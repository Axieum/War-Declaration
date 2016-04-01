package me.axiom.bpl.wardeclaration;

import java.util.Calendar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;

public class LoginListener implements Listener {

	WarDeclaration plugin;
	public LoginListener(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	@EventHandler
	public void onPlayerLoginEvent(PlayerLoginEvent e) {
		MPlayer p = (MPlayer)e.getPlayer();
		Faction f = p.getFaction();
		Faction req = getWarRequester(f);
		Faction tar = getWarTarget(f);
		Faction opp = getWarOpponent(f);
		String sta = statusToColour(getWarStatus(f));
		
		if (p.hasFaction()) {
			if (getWarEngaged(f)) {
				p.sendMessage("§6[§cWARS§6] §7Your war against " + opp.getColorTo(p) + opp.getName() + " §7is currently §3ENGAGED§7!");
			} else if (hasWar(f)){
				p.sendMessage(req.getColorTo(f) + req.getName() + " §7recently declared war against your clan! (§8" + convertMillisToDate(getWarTimeOfDeclaration(f)) + "§7)");
				p.sendMessage("§7War status: " + sta + "§7.");
			}
			if (p == f.getLeader()) {
				if (getWarStatus(f).equalsIgnoreCase("pending") && f == tar) {
					p.sendMessage("§7To respond to the war against " + f.getColorTo(req) + req.getName() + " §7use §3'/war <accept/deny>'§7.");
				} else if (getWarStatus(f).equalsIgnoreCase("accepted")) {
					p.sendMessage("§e" + opp.getColorTo(f) + opp.getName() + " §ahas accepted the war against your clan!");
				} else if (getWarStatus(f).equalsIgnoreCase("denied") && f == req) {
					p.sendMessage("§e" + tar.getColorTo(f) + tar.getName() + " §chas §4denied §cthe war against your clan!");
				} else if (getWarStatus(f).equalsIgnoreCase("denied") && f == tar) {
					p.sendMessage("§cYour §eclan §chas §4denied §cthe war against " + tar.getColorTo(f) + tar.getName() + "§c!");
				} else if (getWarStatus(f).equalsIgnoreCase("cancelled") && f == tar) {
					p.sendMessage("§e" + req.getColorTo(f) + req.getName() + " §chas §4cancelled §cthe war against your clan!");
				} else if (getWarStatus(f).equalsIgnoreCase("cancelled") && f == req) {
					p.sendMessage("§cYour §eclan §chas §4cancelled §cthe war against " + tar.getColorTo(f) + tar.getName() + "§c!");
				}
			}
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
	
}
