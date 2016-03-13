package me.axiom.bpl.wardeclaration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;

public class WarDeclaration extends JavaPlugin {
	
	Logger logger = Logger.getLogger("minecraft");
	PluginDescriptionFile pdf = this.getDescription();
	
	File factionsWarFile = new File(getDataFolder() + "/factionsWar.yml");
	YamlConfiguration factionsWar = YamlConfiguration.loadConfiguration(factionsWarFile);
	
	// This is triggered when the plugin starts. (Hence, "onEnable").
	public void onEnable() {
		
		// Check if the plugin folder exists, if not, then create it.
		if (!(getDataFolder()).exists()) {
			logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create plugin data folder.");
            getDataFolder().mkdir();
            logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] plugin data folder has been successfully created.");
        }
		
		// Check if the factionsWar.yml file exists, if not, then create it.
		if (!factionsWarFile.exists()) {
			try {
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create files.");
				factionsWarFile.createNewFile();
				logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] files have been successfully created.");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		// This will trigger the CmdExecutor class if "/war ..." was typed.
		getCommand("war").setExecutor(new CmdExecutor());
		
		// Add all factions to the file for extra convenience.
		initialiseFactionsWarFile();
		
		logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] has been successfully enabled.");
		
	}
	
	// This is triggered when the plugin stops. (When the server is stopped or reloaded).
	public void onDisable() {
		
		// Save the factionsWar.yml file.
		try {
			factionsWar.save(factionsWarFile);
			logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] saving all files.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] has been disabled.");
	}
	
	public YamlConfiguration getFactionsWarFile() {
		
		return factionsWar;
		
	}
	
	public void saveFactionsWarFile() {
		
		try {
			factionsWar.save(factionsWarFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void initialiseFactionsWarFile() {
		
		for (Faction f : FactionColl.get().getAll()) {
			if (!(factionsWar.contains(f.getName()))) {
				factionsWar.set(f.getName(), "none");
			}
		}
		
		try {
			factionsWar.save(factionsWarFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
