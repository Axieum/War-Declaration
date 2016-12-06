package me.axiom.bpl.wardeclaration;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PlayerStats {

	private UUID uuid;
	private int kills, deaths;
	
	public PlayerStats(UUID uuid, int kills, int deaths) {
		this.uuid = uuid;
		this.kills = kills;
		this.deaths = deaths;
	}
	
	public Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}
	
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(uuid);
	}

	public UUID getUUID() {
		return uuid;
	}
	
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
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
		if (this.deaths == 0) {
			return (float)(this.kills);
		} else {
			return (float)((float)(this.kills)/(float)(this.deaths));
		}
	}
	
}
