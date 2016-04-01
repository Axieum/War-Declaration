package me.axiom.bpl.wardeclaration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;

public class WarDeclaration extends JavaPlugin {
	
	Logger logger = Logger.getLogger("minecraft");
	PluginDescriptionFile pdf = this.getDescription();
	
	public File factionWarsFile = new File(getDataFolder() + "/FactionWars.yml");
	public YamlConfiguration factionWars = YamlConfiguration.loadConfiguration(factionWarsFile);
	
	public HashSet<Faction> engagedWars = new HashSet<Faction>();
	
	public void onEnable() {
		
		if (!(getDataFolder()).exists()) {
			logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create plugin data folder.");
            getDataFolder().mkdir();
            logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] plugin data folder has been successfully created.");
        }
		
		if (!factionWarsFile.exists()) {
			try {
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create files.");
				factionWarsFile.createNewFile();
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] files have been successfully created.");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		getCommand("war").setExecutor(new CmdExecutor(this));
		
		getServer().getPluginManager().registerEvents(new LoginListener(this), this);
		getServer().getPluginManager().registerEvents(new FactionNameChangeListener(this), this);
		getServer().getPluginManager().registerEvents(new FactionCreationListener(this), this);

		initialiseFactionWarsFile();
		initialiseEngagedWars();
		
		logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] has been successfully enabled.");
		
	}
	
	public void onDisable() {
		
		saveFactionWarsFile();
		
		logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] has been disabled.");
		
	}
	
	public void initialiseEngagedWars() {
		
		for (Faction f : FactionColl.get().getAll()) {
			if (factionWars.contains(f.getName())) {
				if (factionWars.getBoolean(f.getName() + ".Engaged")) {
					
				}
			}
		}
		
	}
	
	public void initialiseFactionWarsFile() {
		
		for (Faction f : FactionColl.get().getAll()) {
			if (!(factionWars.contains(f.getName())) && !(f.getName().equalsIgnoreCase("safezone")) && !(f.getName().equalsIgnoreCase("warzone")) && !(f.getName().equalsIgnoreCase(FactionColl.get().getByName("Wilderness").getName()))) {
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
	
	public void saveFactionWarsFile() {
		
		try {
			factionWars.save(factionWarsFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
