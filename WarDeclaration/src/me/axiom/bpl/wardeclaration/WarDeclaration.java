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
	PluginDescriptionFile pdf;
	
	File factionsWarFile = new File(getDataFolder() + "/factionsWar.yml");
	YamlConfiguration factionsWar;
	
	public void onEnable() {
		
		if (!(getDataFolder()).exists()) {
			logger.info(pdf.getName() + " (v" + pdf.getVersion() + ") : attempting to create plugin data folder.");
            getDataFolder().mkdir();
            logger.info(pdf.getName() + " (v" + pdf.getVersion() + ") : plugin data folder has been successfully created.");
        }
		
		if (!factionsWarFile.exists()) {
			try {
				logger.info(pdf.getName() + " (v" + pdf.getVersion() + ") : attempting to create files.");
				factionsWarFile.createNewFile();
				logger.info(pdf.getName() + " (v" + pdf.getVersion() + ") : files have been successfully created.");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
		getCommand("war").setExecutor(new CmdExecutor());
		
		initialiseFactionsWarFile();
		
		logger.info(pdf.getName() + " (v" + pdf.getVersion() + ") : has been successfully enabled.");
		
	}
	
	public void onDisable() {
		
		try {
			factionsWar.save(factionsWarFile);
			logger.info(pdf.getName() + " (v" + pdf.getVersion() + ") : saving all files.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		logger.info(pdf.getName() + " (v" + pdf.getVersion() + ") : has been disabled.");
		
	}
	
	public void initialiseFactionsWarFile() {
		
		for (Faction f : FactionColl.get().getAll()) {
			if (!(factionsWar.contains(f.getName()))) {
				factionsWar.set(f.getName(), "none");
			}
		}
		
	}

}
