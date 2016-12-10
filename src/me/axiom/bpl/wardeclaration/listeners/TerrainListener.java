package me.axiom.bpl.wardeclaration.listeners;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
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
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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
				
				if (!b.getType().equals(Material.TNT)) b.setType(Material.AIR);
			}
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onPlayerMineEvent(BlockBreakEvent e) {
		
		Block b = e.getBlock();
		MPlayer.get(e.getPlayer()).getFaction().getName();
		
		String facName = BoardColl.get().getFactionAt(PS.valueOf(b)).getName();
		if (plugin.factionWars.getBoolean(facName + ".Engaged") && plugin.factionWars.getString(facName + ".Target").equalsIgnoreCase(facName)) { // If that faction is engaged. And they are the defender.
			e.setCancelled(true);
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
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
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
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onBlockIgnite(BlockIgniteEvent e) {
		
		Block b = e.getBlock();
		
		String facName = BoardColl.get().getFactionAt(PS.valueOf(b)).getName();
		if (plugin.factionWars.getBoolean(facName + ".Engaged") && plugin.factionWars.getString(facName + ".Target").equalsIgnoreCase(facName)) { // If that faction is engaged. And they are the defender.
			e.setCancelled(true);
			if (!plugin.savedBlocks.containsKey(b.getLocation())) {
				String blockData; // Will soon add the material and blockstate to a single string for saving.
				
				Material blockMaterial = b.getType();
				MaterialData materialData = b.getState().getData();
				
				blockData = blockMaterial.name() + "|" + materialData.getData();
				
				plugin.savedBlocks.put(b.getLocation(), blockData);
			}
			if (b.getType().equals(Material.TNT)) {
				// Crazy workaround.				
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
	
					@Override
					public void run() {
						Block block = e.getIgnitingBlock();
						block.setType(Material.AIR);
						block.getState().update();
					}
					
				}, 0L); // No delay, shouldn't affect the block dissapearing!
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void playerInteract(PlayerInteractEvent e) {
		
		if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
		
		Block b = e.getClickedBlock();
		String facName = BoardColl.get().getFactionAt(PS.valueOf(b)).getName();
		if (plugin.factionWars.getBoolean(facName + ".Engaged") && plugin.factionWars.getString(facName + ".Target").equalsIgnoreCase(facName)) { // If that faction is engaged. And they are the defender.
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (e.getMaterial().equals(Material.FLINT_AND_STEEL) || e.getMaterial().equals(Material.FIREBALL))) {
				e.setCancelled(true);
				// Crazy workaround.				
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						Block block = b.getRelative(e.getBlockFace());
						block.setType(Material.FIRE);
						block.getState().update();
					}
					
				}, 0L); // No delay, shouldn't affect the block dissapearing!
			}
			
			if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && (e.getMaterial().equals(Material.CHEST))) {
				e.setCancelled(true);
				// Crazy workaround.				
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						Block block = b.getRelative(e.getBlockFace());
						block.setType(Material.CHEST);
						block.getState().update();
					}
					
				}, 0L); // No delay, shouldn't affect the block dissapearing!
			}
			
			if (e.getAction().equals(Action.LEFT_CLICK_BLOCK) && (b.getType().equals(Material.TNT) || b.getType().equals(Material.SAPLING))) {
				e.setCancelled(true);
				// Crazy workaround.				
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

					@Override
					public void run() {
						Block block = b;
						block.setType(Material.AIR);
						block.getState().update();
					}
					
				}, 1L); // No delay, shouldn't affect the block dissapearing!
			}
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = false)
	public void onBlockPlaceEvent(BlockPlaceEvent e) {
		Block b = e.getBlock();
		String facName = BoardColl.get().getFactionAt(PS.valueOf(b)).getName();
		if (plugin.factionWars.getBoolean(facName + ".Engaged") && plugin.factionWars.getString(facName + ".Target").equalsIgnoreCase(facName)) { // If that faction is engaged. And they are the defender.
			e.setCancelled(false);
			
			if (!plugin.savedBlocks.containsKey(b.getLocation())) {
				String blockData; // Will soon add the material and blockstate to a single string for saving.
				
				Material blockMaterial = Material.AIR;
				byte materialData = 0;
				
				blockData = blockMaterial.name() + "|" + materialData;
				
				plugin.savedBlocks.put(b.getLocation(), blockData);
			}
			
			// Crazy workaround.
			final Block newBlock = e.getBlockPlaced();
			Material newBlockMaterial = newBlock.getType();
			byte newBlockData = newBlock.getData();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

				@Override
				public void run() {
					Block block = b;
					b.setType(newBlockMaterial);
					b.setData(newBlockData);
					block.getState().update();
					
				}
				
			}, 0L); // No delay, shouldn't affect the block dissapearing!
			
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				final Player p = e.getPlayer();
				@Override
				public void run() {
					ItemStack iS = p.getItemInHand();
					if (iS.getAmount() <= 1) {
						p.getInventory().removeItem(iS);
					} else {
						iS.setAmount(iS.getAmount()-1); // Since we cancelled the event, remove the item from their inventory manually.
					}	
				}
				
			}, 0L); // No delay, shouldn't affect the block dissapearing!
			
			e.getPlayer().sendBlockChange(newBlock.getLocation(), newBlockMaterial, newBlockData);
			
			e.setBuild(false); // Prevents the block placement, mainly to stop Faction's terrible messages.
		}
		
	}
	
}
