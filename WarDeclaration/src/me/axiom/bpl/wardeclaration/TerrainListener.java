package me.axiom.bpl.wardeclaration;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.massivecore.ps.PS;

public class TerrainListener implements Listener {

	WarDeclaration plugin;
	TerrainListener(WarDeclaration instance) {
		this.plugin = instance;
	}
	
	public void onExplosionEvent(EntityExplodeEvent e) {
		
		List<Block> blocks = e.blockList();
		
		for (Block b : blocks) {
			String facName = BoardColl.get().getFactionAt(PS.valueOf(b)).getName();
			if (plugin.factionWars.getBoolean(facName + ".Engaged") && plugin.factionWars.getString(facName + ".Target").equalsIgnoreCase(facName)) { // If that faction is engaged. And they are the defender.
				
				final BlockState bs = b.getState();
				int delay = 20; // ticks, 20 = 1 sec
				
				b.setType(Material.AIR);
				
				if (b.getType() == Material.SAND || b.getType() == Material.GRAVEL || b.getType() == Material.ANVIL || b.getType() == Material.LADDER) {
					// This means the solid blocks need to generate, then these blocks after those blocks have.
					delay += 5;
				}
				
				Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    public void run() {
                            bs.update(true, false);
                    }
				}, delay);
				
			}
			
		}
		
	}
	
}
