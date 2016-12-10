package me.axiom.bpl.wardeclaration.listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.material.MaterialData;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;

import me.axiom.bpl.wardeclaration.WarDeclaration;

public class TerrainListener implements Listener {

	WarDeclaration plugin;
	public TerrainListener(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onExplosionEvent(EntityExplodeEvent e) {
				
		List<Block> blocks = e.blockList();
		
		for (Block b : blocks) {
			String facName = BoardColl.get().getFactionAt(PS.valueOf(b)).getName();
			if (plugin.factionWars.getBoolean(facName + ".Engaged") && plugin.factionWars.getString(facName + ".Target").equalsIgnoreCase(facName)) { // If that faction is engaged. And they are the defender.
				
				if (!plugin.savedBlocks.containsKey(b.getLocation())) {
					String blockData; // Will soon add the material and blockstate to a single string for saving.
					
					Material blockMaterial = b.getType();
					MaterialData materialData = b.getState().getData();
					
					blockData = blockMaterial.name() + "|" + materialData.getData();
					
					plugin.savedBlocks.put(b.getLocation(), blockData);
				}
				
				b.setType(Material.AIR);
				
			}
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerMineEvent(BlockBreakEvent e) {
		
		Block b = e.getBlock();
		MPlayer.get(e.getPlayer()).getFaction().getName();
		
		String facName = BoardColl.get().getFactionAt(PS.valueOf(b)).getName();
		if (plugin.factionWars.getBoolean(facName + ".Engaged") && plugin.factionWars.getString(facName + ".Target").equalsIgnoreCase(facName)) { // If that faction is engaged. And they are the defender.

			if (!plugin.savedBlocks.containsKey(b.getLocation())) {
				String blockData; // Will soon add the material and blockstate to a single string for saving.
				
				Material blockMaterial = b.getType();
				MaterialData materialData = b.getState().getData();
				
				blockData = blockMaterial.name() + "|" + materialData.getData();
				
				plugin.savedBlocks.put(b.getLocation(), blockData);
			}
			
			b.setType(Material.AIR);
			
		}
		
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockBurnEvent(BlockBurnEvent e) {
		
		Block b = e.getBlock();
		
		String facName = BoardColl.get().getFactionAt(PS.valueOf(b)).getName();
		if (plugin.factionWars.getBoolean(facName + ".Engaged") && plugin.factionWars.getString(facName + ".Target").equalsIgnoreCase(facName)) { // If that faction is engaged. And they are the defender.
		
			if (!plugin.savedBlocks.containsKey(b.getLocation())) {
				String blockData; // Will soon add the material and blockstate to a single string for saving.
				
				Material blockMaterial = b.getType();
				MaterialData materialData = b.getState().getData();
				
				blockData = blockMaterial.name() + "|" + materialData.getData();
				
				plugin.savedBlocks.put(b.getLocation(), blockData);
			}
			
			b.setType(Material.AIR);
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBlockPlaceEvent(BlockPlaceEvent e) {
		Block b = e.getBlock();
		
		String facName = BoardColl.get().getFactionAt(PS.valueOf(b)).getName();
		MPlayer p = MPlayer.get(e.getPlayer());
		p.getFaction().getName();
		if (plugin.factionWars.getBoolean(facName + ".Engaged") && plugin.factionWars.getString(facName + ".Target").equalsIgnoreCase(facName)) { // If that faction is engaged. And they are the defender.
			
			if (!plugin.savedBlocks.containsKey(b.getLocation())) {
				String blockData; // Will soon add the material and blockstate to a single string for saving.
				
				Material blockMaterial = Material.AIR;
				byte materialData = 0;
				
				blockData = blockMaterial.name() + "|" + materialData;
				
				plugin.savedBlocks.put(b.getLocation(), blockData);
			}
			
		}
		
		
		e.setCancelled(false);
		
	}
	
}
