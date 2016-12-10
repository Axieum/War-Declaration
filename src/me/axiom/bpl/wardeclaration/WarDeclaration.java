package me.axiom.bpl.wardeclaration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;

import me.axiom.bpl.wardeclaration.listeners.FactionCreationListener;
import me.axiom.bpl.wardeclaration.listeners.FactionNameChangeListener;
import me.axiom.bpl.wardeclaration.listeners.LoginListener;
import me.axiom.bpl.wardeclaration.listeners.TerrainListener;
import me.axiom.bpl.wardeclaration.listeners.WarDeathListener;

public class WarDeclaration extends JavaPlugin {
	
	public Logger logger = Logger.getLogger("minecraft");
	PluginDescriptionFile pdf = this.getDescription();
	
	public File factionWarsFile = new File(getDataFolder() + "/FactionWars.yml");
	public YamlConfiguration factionWars = YamlConfiguration.loadConfiguration(factionWarsFile);
	
	public File factionWarsLogFile = new File(getDataFolder() + "/FactionWarLog.yml");
	public YamlConfiguration factionWarLog = YamlConfiguration.loadConfiguration(factionWarsLogFile);
	
	public File playerStatsFile = new File(getDataFolder() + "/playerStats.yml");
	public YamlConfiguration playerStatsLog = YamlConfiguration.loadConfiguration(playerStatsFile);
	
	public File savedBlocksFile = new File(getDataFolder() + "/savedBlocks.yml");
	public YamlConfiguration savedBlocksLog = YamlConfiguration.loadConfiguration(savedBlocksFile);
	public HashMap<Location, String> savedBlocks = new HashMap<Location, String>();
	
	// HashSet<PlayerStats information>
	public HashSet<PlayerStats> playerStats = new HashSet<PlayerStats>();
	
	public HashSet<Faction> engagedWars = new HashSet<Faction>();
	public HashSet<Faction> engageConfirmation = new HashSet<Faction>();
	
	public HashMap<Faction, Faction> factionVictoryDecider = new HashMap<Faction, Faction>();
	
	public void onEnable() {
		
		if (!(getDataFolder()).exists()) {
			logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create Plugin Data folder.");
            getDataFolder().mkdir();
            logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] plugin data folder has been successfully created.");
        }
		
		if (!factionWarsFile.exists()) {
			try {
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create War Status file.");
				factionWarsFile.createNewFile();
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] war status file has been successfully created.");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		if (!factionWarsLogFile.exists()) {
			try {
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create War Log file.");
				factionWarsLogFile.createNewFile();
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] war log has been successfully created.");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		if (!playerStatsFile.exists()) {
			try {
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create Player Stats log file.");
				playerStatsFile.createNewFile();
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] Player Stats log file has been successfully created.");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		if (!savedBlocksFile.exists()) {
			try {
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create Saved Blocks log file.");
				savedBlocksFile.createNewFile();
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] Saved Blocks log file has been successfully created.");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		getCommand("war").setExecutor(new CmdExecutor(this));
		
		getServer().getPluginManager().registerEvents(new LoginListener(this), this);
		getServer().getPluginManager().registerEvents(new FactionNameChangeListener(this), this);
		getServer().getPluginManager().registerEvents(new FactionCreationListener(this), this);
		getServer().getPluginManager().registerEvents(new WarDeathListener(this), this);
		getServer().getPluginManager().registerEvents(new TerrainListener(this), this);

		initialiseFactionWarsFile();
		initialiseEngagedWars();
		initialisePlayerStats();
		
		// Begin task to check for block regeneration.
		initialiseRegenerationTask();
		
		logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] has been successfully enabled.");
		
		// Auto-Save task.
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				saveFactionWarsFile();
				saveSavedBlocksFile();
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] saved war files.");
			}
			
		}, 0L, 24000L); // 24000 ticks = 20mins.
		
	}

	public void onDisable() {
		
		saveFactionWarsFile();
		saveFactionWarsLogFile();
		savePlayerStatsFile();
		saveSavedBlocksFile();
		
		logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] has been disabled.");
			
	}
	
	public void initialiseRegenerationTask() {
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {

			@Override
			public void run() {
				/*
				 *   - Loop through war history.
				 *   - Ensure the war hasn't been regerated back yet.
				 *   - If not, check time since war ended is greater than 60 minutes.
				 *   - If so, regenerate blocks back.
				 */				
				for (String warId : factionWarLog.getKeys(false)) {
					if (!factionWarLog.getBoolean(warId + ".Regenerated")) {
						long timeWarEnd = factionWarLog.getLong(warId + ".TimeOfEnd");
						long timeNow = System.currentTimeMillis();
						double secsSinceEnd = (timeNow - timeWarEnd) / 1000;
						
						if (secsSinceEnd > 900) {
							logger.info("[WarDeclaration] The war with ID '" + warId + "', is being regenerated back.");
							revertWarDamage(factionWarLog.getString(warId + ".Target"), warId);
						}
					}
					
				}
			}
			
		}, 1200L, 1200L); // 1200 ticks = 60 seconds.
	}
	
	public void revertWarDamage(String faction, String warId) {
		// Grab all keys relating to that war and randomly regenerate the blocks back!
		ConfigurationSection sectionFaction = savedBlocksLog.getConfigurationSection(faction);
		if (sectionFaction == null) { // Nothing to regenerate back.
			saveSavedBlocksFile();
			return;
		}
		int blocksToRegenerate = sectionFaction.getKeys(false).size();
		int blocksRegenerated = 0;
		for (String value : sectionFaction.getKeys(false)) {
			
			/*
			 * CONVERT STRING to LOCATION and BLOCK.
			 */
			
			String[] arg = value.split("\\|");
			double[] parsed = new double[3];
			for (int i = 0; i < 3; i++) {
			    parsed[i] = Double.parseDouble(arg[i+1]);
			}
			
			Location location = new Location(Bukkit.getWorld(arg[0]), parsed[0], parsed[1], parsed[2]);
			Block b = location.getBlock();
			
			String blockString = savedBlocksLog.getString(faction + "." + value);
			// Convert blockString to Material and Data.
			String[] args = blockString.split("\\|"); // An array. Should be 2 large.
        	Material regenType = Material.getMaterial(args[0]);
        	byte regenData = Byte.parseByte(args[1]);
			
			/*
			 * REGENERATE BLOCK.
			 */
			
			int delay = (int)Math.floor((Math.random() * 120) + 20); // ticks, 20 = 1 sec
			
			if (b.getType() == Material.SAND || b.getType() == Material.GRAVEL || b.getType() == Material.ANVIL || b.getType() == Material.LADDER || b.getType() == Material.LONG_GRASS || b.getType() == Material.YELLOW_FLOWER || b.getType() == Material.RED_ROSE) {
				// This means the solid blocks need to generate, then these blocks after those blocks have.
				delay += 20;
			}
			
			// If last block is regenerated, clean the blocks file.
			blocksRegenerated++;
			
			if (blocksRegenerated >= blocksToRegenerate) {
				savedBlocksLog.set(faction, null);
				factionWarLog.set(warId + ".Regenerated", true);
				saveFactionWarsLogFile();
				saveSavedBlocksFile();
			}
			
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	            @SuppressWarnings("deprecation")
				public void run() {
	            	// Set the block.
	                b.setType(regenType);
	                b.setData(regenData, true);
					b.getWorld().playEffect(b.getLocation(), Effect.FLYING_GLYPH, 0);
	            }
			}, (int)Math.floor(Math.random()*delay)+delay);
			
		}
		
	}
	
	public void initialisePlayerStats() {
		
		for (Player p : getServer().getOnlinePlayers()) {
			if (MPlayer.get(p).hasFaction()) {
				String playerName = p.getName();
				if (getPlayerStats(p.getUniqueId()) == null) { // They're not loaded yet.
					if (factionWars.getBoolean(MPlayer.get(p).getFaction().getName() + ".Engaged")) { // Are they engaged, should we actually load them in necessary?
						PlayerStats pS;
						if (playerStatsLog.getKeys(false).contains(p.getUniqueId().toString())) { // Try to load their saved stats.
							pS = new PlayerStats(p.getUniqueId(), playerStatsLog.getInt(p.getUniqueId() + ".Kills"), playerStatsLog.getInt(p.getUniqueId() + ".Deaths"));
						} else { // Weren't saved. Reset.
							pS = new PlayerStats(p.getUniqueId(), 0, 0);
						}
						playerStats.add(pS);
						logger.info("[WarDeclaration | Player Initialization] " + playerName + "'s stats are now being tracked!");
					}
				}
			}
		}
		
		for (OfflinePlayer p : getServer().getOfflinePlayers()) { // Load offline players.
			if (MPlayer.get(p.getUniqueId()).hasFaction()) {
				if (getPlayerStats(p.getUniqueId()) == null) { // They're not loaded yet.
					if (factionWars.getBoolean(MPlayer.get(p.getUniqueId()).getFaction().getName() + ".Engaged")) { // We only need to load them if they're engaged.
						String playerName = p.getName();
						PlayerStats pS;
						if (playerStatsLog.getKeys(false).contains(p.getUniqueId().toString())) { // Try to load their saved stats.
							pS = new PlayerStats(p.getUniqueId(), playerStatsLog.getInt(p.getUniqueId() + ".Kills"), playerStatsLog.getInt(p.getUniqueId() + ".Deaths"));
						} else { // Weren't saved. Reset.
							pS = new PlayerStats(p.getUniqueId(), 0, 0);
						}
						playerStats.add(pS);
						logger.info("[WarDeclaration | Player Initialization] " + playerName + "'s stats are ready for their login.");
					}
				}
			}
		}
		
	}

	public PlayerStats getPlayerStats(UUID uuid) {
		for (PlayerStats pS : playerStats) {
			if (pS.getUUID().toString().equals(uuid.toString())) {
				return pS;
			}
		}
		return null;
	}
	
	public void resetPlayerStatsForFaction(Faction f) {
		// Ensure to only account for the online players.
		// Note: Players who join the war late, will be controlled via the LoginListener.class
		for (Player p : getServer().getOnlinePlayers()) {
			MPlayer mP = MPlayer.get(p);
			if (mP.hasFaction()) {
				if (mP.getFaction() == f) {
					PlayerStats pS = getPlayerStats(p.getUniqueId());
					pS.setDeaths(0);
					pS.setKills(0);
				}
			}
		}
		for (OfflinePlayer p : getServer().getOfflinePlayers()) { // Load offline players.
			if (MPlayer.get(p.getUniqueId()).hasFaction()) {
				if (MPlayer.get(p.getUniqueId()).getFaction() == f) { // Ensure we are targeting a player from the defined faction.
					// They're in the faction, so reset their stats.
					if (getPlayerStats(p.getUniqueId()) != null) {
						PlayerStats pS = getPlayerStats(p.getUniqueId());
						pS.setKills(0);
						pS.setDeaths(0);
					} else {
						// They're stats aren't loaded.
						PlayerStats pS = new PlayerStats(p.getUniqueId(), 0, 0);
						playerStats.add(pS);
						logger.info("[WarDeclaration | Player Initialization] " + p.getName() + "'s stats are now being tracked!");
					}
				}
			}
		}
	}
	
	public void initialiseEngagedWars() {
		
		for (Faction f : FactionColl.get().getAll()) {
			if (factionWars.contains(f.getName())) {
				if (factionWars.getBoolean(f.getName() + ".Engaged")) {
					engagedWars.add(f);
				}
			}
		}
		
	}
	
	public void initialiseFactionWarsFile() {
		
		for (Faction f : FactionColl.get().getAll()) {
			if (!(factionWars.contains(f.getName())) && !(f.getName().equalsIgnoreCase("safezone")) && !(f.getName().equalsIgnoreCase("none")) && !(f.getName().equalsIgnoreCase("warzone")) && !(f.getName().equalsIgnoreCase(FactionColl.get().getByName("Wilderness").getName()))) {
				factionWars.set(f.getName() + ".Target", "none");
				factionWars.set(f.getName() + ".Requester", "none");
				factionWars.set(f.getName() + ".Status", "available");
				factionWars.set(f.getName() + ".Engaged", false);
				factionWars.set(f.getName() + ".ForfeitedBy", "none");
				factionWars.set(f.getName() + ".TimeOfDeclaration", 0);
			}
		}
		
		saveFactionWarsFile();
		
	}
	
	public void addWarLog(Faction winFaction, Faction loseFaction, String method) {
		
		int newKey = getMaxKey(factionWarLog) + 1;
		factionWarLog.set(String.valueOf(newKey) + ".Requester", getWarRequester(winFaction).getName());
		factionWarLog.set(String.valueOf(newKey) + ".Target", getWarTarget(winFaction).getName());
		factionWarLog.set(String.valueOf(newKey) + ".TimeOfDeclaration", factionWars.getLong(winFaction.getName() + ".TimeOfDeclaration"));
		factionWarLog.set(String.valueOf(newKey) + ".TimeOfEngage", factionWars.getLong(winFaction.getName() + ".TimeOfEngage"));
		factionWarLog.set(String.valueOf(newKey) + ".TimeOfEnd", System.currentTimeMillis());
		factionWarLog.set(String.valueOf(newKey) + ".Regenerated", false);
		factionWarLog.set(String.valueOf(newKey) + ".Victory", winFaction.getName());
		factionWarLog.set(String.valueOf(newKey) + ".Defeat", loseFaction.getName());
		factionWarLog.set(String.valueOf(newKey) + ".Method", method); // Method being, forfeit, or ended.
		
		int winFactionTotalKills = 0;
		int winFactionTotalDeaths = 0;
		int loseFactionTotalKills = 0;
		int loseFactionTotalDeaths = 0;

		for (PlayerStats pS : playerStats) {
			
			MPlayer mP;
			Faction f;
			
			if (pS.getPlayer() != null) {
				mP = MPlayer.get(pS.getPlayer());
			} else {
				mP = MPlayer.get(pS.getOfflinePlayer().getUniqueId());
			}
			
			f = mP.getFaction();
			
			if (f == winFaction || f == loseFaction) { // That player is associated with this war that took place.
				factionWarLog.set(String.valueOf(newKey) + "." + f.getName() + "." + mP.getUuid() + ".Kills", pS.getKills());
				factionWarLog.set(String.valueOf(newKey) + "." + f.getName() + "." + mP.getUuid() + ".Deaths", pS.getDeaths());
				if (f == winFaction) {
					winFactionTotalKills = winFactionTotalKills + pS.getKills();
					winFactionTotalDeaths = winFactionTotalDeaths + pS.getDeaths();
				} else if (f == loseFaction) {
					loseFactionTotalKills = loseFactionTotalKills + pS.getKills();
					loseFactionTotalDeaths = loseFactionTotalDeaths + pS.getDeaths();
				}
			}
		}
		
		factionWarLog.set(String.valueOf(newKey) + ".VictoryKills", winFactionTotalKills);
		factionWarLog.set(String.valueOf(newKey) + ".VictoryDeaths", winFactionTotalDeaths);
		factionWarLog.set(String.valueOf(newKey) + ".DefeatKills", loseFactionTotalKills);
		factionWarLog.set(String.valueOf(newKey) + ".DefeatDeaths", loseFactionTotalDeaths);
		
		saveFactionWarsLogFile();
		saveSavedBlocksFile();
		
	}
	
	public int getMaxKey(YamlConfiguration yml) {
		int maxKey = 0;
		for (String s : yml.getKeys(false)) {
			int number = Integer.parseInt(s);
			if (number > maxKey) {
				maxKey = number;
			}
		}
		return maxKey;
	}
	
	public void saveSavedBlocksFile() {

		if (!savedBlocks.isEmpty()) { // Are there really any blocks to save?
			logger.info("[WarDeclaration] Saving Blocks to be regenerated back later!");
			int totalBlocks = savedBlocks.size();
			int scannedBlocks = 0;
			logger.info("[WarDeclaration] " + totalBlocks + " blocks are to be saved.");
			for (Location loc : savedBlocks.keySet()) {
				String facName = BoardColl.get().getFactionAt(PS.valueOf(loc)).getName(); // Retrieve the block's faction. Used when deciding to regenerate back later.
				// Turn the Location into a string to save as a KEY.
				String keyString = facName + "." + loc.getWorld().getName() + "|" + loc.getBlockX() + "|" + loc.getBlockY() + "|" + loc.getBlockZ();
				// Save key to file with the assigned material type and data as a string.
				savedBlocksLog.set(keyString, savedBlocks.get(loc));
				scannedBlocks++;
				if (scannedBlocks >= totalBlocks) { // Check if we've scanned all the blocks and are ready to save.
					try {
						savedBlocksLog.save(savedBlocksFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} else {
			logger.info("[WarDeclaration] There are no blocks to be saved.");
			try {
				savedBlocksLog.save(savedBlocksFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void saveFactionWarsLogFile() {
		try {
			factionWarLog.save(factionWarsLogFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveFactionWarsFile() {
		try {
			factionWars.save(factionWarsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void savePlayerStatsFile() {
		
		for (PlayerStats pS : playerStats) {
			
			playerStatsLog.set(pS.getUUID() + ".Kills", pS.getKills());
			playerStatsLog.set(pS.getUUID() + ".Deaths", pS.getDeaths());
			
			// Online or Offline?
			if (pS.getPlayer() != null) {
				playerStatsLog.set(pS.getUUID() + ".Name", pS.getPlayer().getName());
			} else {
				playerStatsLog.set(pS.getUUID() + ".Name", pS.getOfflinePlayer().getName());
				
			}
			
		}
			
		try {
			playerStatsLog.save(playerStatsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Faction getWarRequester(Faction f) {
		if (factionWars.getString(f.getName() + ".Requester").equalsIgnoreCase("none")) {
			return null;
		} else {
			return FactionColl.get().getByName(factionWars.getString(f.getName() + ".Requester"));
		}
	}
	
	public Faction getWarTarget(Faction f) {
		if (factionWars.getString(f.getName() + ".Target").equalsIgnoreCase("none")) {
			return null;
		} else {
			return FactionColl.get().getByName(factionWars.getString(f.getName() + ".Target"));
		}
	}
	
}
