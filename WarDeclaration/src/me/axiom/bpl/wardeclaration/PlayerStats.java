package me.axiom.bpl.wardeclaration;

import org.bukkit.entity.Player;

public class PlayerStats {

	private Player p;
	private int kills, deaths;
	
	public PlayerStats(Player player, int kills, int deaths) {
		this.p = player;
		this.kills = kills;
		this.deaths = deaths;
	}
	
	public Player getPlayer() {
		return p;
	}
	
	public void setPlayer(Player p) {
		this.p = p;
	}
	
	public int getKills() {
		return kills;
	}
	
	public void setKills(int kills) {
		this.kills = kills;
	}
	
	public int getDeaths() {
		return deaths;
	}
	
	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}
	
	public float getKD() {
		return this.kills / this.deaths;
	}
	
}
