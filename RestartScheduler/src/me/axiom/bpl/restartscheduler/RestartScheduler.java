package me.axiom.bpl.restartscheduler;

import java.util.Calendar;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class RestartScheduler extends JavaPlugin implements CommandExecutor, Listener {

	Logger logger = Logger.getLogger("minecraft");
	PluginDescriptionFile pdf = this.getDescription();
	private boolean creativeEnabled = true;
	
	public void onEnable() {
		
		// Create the folder used to store the 'config.yml' file.
		if (!(getDataFolder()).exists()) {
			logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] attempting to create plugin data folder.");
            getDataFolder().mkdir();
            logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] plugin data folder has been successfully created.");
        }
		
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();
		
		// Register the command '/restartscheduler' for use in-game.
		getCommand("restartscheduler").setExecutor(this);
		
		// Register event to stop teleportation to creative world.
		getServer().getPluginManager().registerEvents(this, this);
		
		// Register the times and schedule them.
		for (String key : getConfig().getKeys(false)) {
			if (isParsable(key)) {
				ConfigurationSection section = getConfig().getConfigurationSection(key);
				int hours = section.getInt("hours");
				int minutes = section.getInt("minutes");
				int seconds = section.getInt("seconds");
				logger.info("Adding schedule at: " + hours + ":" + minutes + ":" + seconds + "."); 
				scheduleRepeatAtTime(this, new Runnable() {
					@Override
					public void run() {
						for (Player p : Bukkit.getOnlinePlayers()) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("message")));
							if (p.getWorld().getName().equalsIgnoreCase(getConfig().getString("creative_world_name"))) { // This means they're in the creative world.
								// Teleport them back to the main world and disable the creative world temporarily.
								creativeEnabled = false;
								getServer().dispatchCommand(p, "spawn");
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("creative_message")));
							}
						}
					}
				}, hours, minutes, seconds);
			}
		}
		
		logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] has been successfully enabled.");
		
	}
	
	public void onDisable() {
		
		logger.info("[" + pdf.getName() + " v" + pdf.getVersion() + "] has been disabled.");
		saveConfig();
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		World w = e.getTo().getWorld();
		if (!creativeEnabled) {
			if (w == Bukkit.getWorld(getConfig().getString("creative_world_name"))) {
				e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("creative_message")));
				e.setCancelled(true);
			}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("currenttime")) {
				Calendar cal = Calendar.getInstance();
				int hrs = cal.get(Calendar.HOUR_OF_DAY);
				int mins = cal.get(Calendar.MINUTE);
				int secs = cal.get(Calendar.SECOND);
				s.sendMessage("§6[§f?§6] §f" + hrs + "§7:§f" + mins + "§7:§f" + secs + "§7:§f.");
				return true;
			}
		}
		
		if (args.length < 3) { // They didn't type enough arguments; show them how to use the command.
			s.sendMessage("§6[§c!§6] §4Not enough arguments; §4§lUsage: §c/restartscheduler <hh> <mm> <ss>");
			return false;
		}
		
		if (args.length > 3) { // They typed too many arguments; show them how to use the command.
			s.sendMessage("§6[§c!§6] §4Too many arguments; §4§lUsage: §c/restartscheduler <hh> <mm> <ss>");
			return false;
		}
		
		String Shrs = args[0];
		String Smins = args[1];
		String Ssecs = args[2];
		
		if (isParsable(Shrs) && isParsable(Smins) && isParsable(Ssecs)) { // They did type all numbers :D
			
			// Create an instance of the numbers (convert from string).
			int hrs = Integer.valueOf(Shrs);
			int mins = Integer.valueOf(Smins);
			int secs = Integer.valueOf(Ssecs);
			
			// Check if the hours, mins and days are within the ranges (0-23, 0-59, 0-59).
			if ((hrs >= 0 && hrs <= 23) && (mins >= 0 && mins <= 59) && (secs >= 0 && secs <= 59)) {

				// Save the numbers to configuration file.
				int newKey = getMaxKey(getConfig()) + 1;
				getConfig().set(newKey + ".hours", hrs);
				getConfig().set(newKey + ".minutes", mins);
				getConfig().set(newKey + ".seconds", secs);
				saveConfig();
				
				// Schedule the task.
				logger.info("Addding schedule at: " + hrs + ":" + mins + ":" + secs + "."); 
				scheduleRepeatAtTime(this, new Runnable() {
					@Override
					public void run() {
						
						for (Player p : Bukkit.getOnlinePlayers()) {
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("message")));
							if (p.getWorld().getName().equalsIgnoreCase(getConfig().getString("creative_world_name"))) { // This means they're in the creative world.
								// Teleport them back to the main world and disable the creative world temporarily.
								getServer().dispatchCommand(p, "spawn");
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("creative_message")));
							}
						}
						
					}
				}, hrs, mins, secs);
				
				s.sendMessage("§aTime has been scheduled for: §7" + hrs + "§a:§7" + mins + "§a:§7" + secs + "§a.");
				
			} else { // Their numbers out of the range.
				s.sendMessage("§6[§c!§6] §4Out of range; §4§lUsage: §c/restartscheduler <hh:0-23> <mm:0-59> <ss:0-59>");
				return false;
			}
			
			return true;
			
		} else { // They didn't type numbers.
			s.sendMessage("§6[§c!§6] §4Numbers only; §4§lUsage: §c/restartscheduler <hh> <mm> <ss>");
			return false;
		}
		
	}
	
	// Credit: Courier; https://bukkit.org/threads/performing-actions-at-specific-times.103357/. //
	
	/**
	 * Schedules a task to run at a certain hour every day.
	 * @param plugin The plugin associated with this task
	 * @param task The task to run
	 * @param hour [0-23] The hour of the day to run the task
	 * @param minute [0-59] The minute of the hour to run the task
	 * @param seconds [0-59] The seconds of the minute to run the task
	 * @return Task id number (-1 if scheduling failed)
	 **/
	
	public static int scheduleRepeatAtTime(Plugin plugin, Runnable task, int hour, int minute, int seconds)
	{
	    //Calendar is a class that represents a certain time and date.
	    Calendar cal = Calendar.getInstance(); //obtains a calendar instance that represents the current time and date
	 
	    //time is often represented in milliseconds since the epoch,
	    //as a long, which represents how many milliseconds a time is after
	    //January 1st, 1970, 00:00.
	 
	    //this gets the current time
	    long now = cal.getTimeInMillis();
	    //you could also say "long now = System.currentTimeMillis()"
	 
	    //since we have saved the current time, we need to figure out
	    //how many milliseconds are between that and the next
	    //time it is 7:00pm, or whatever was passed into hour
	    //we do this by setting this calendar instance to the next 7:00pm (or whatever)
	    //then we can compare the times
	 
	    //if it is already after 7:00pm,
	    //we will schedule it for tomorrow,
	    //since we can't schedule it for the past.
	    //we are not time travelers.
	    if(cal.get(Calendar.HOUR_OF_DAY) >= hour && cal.get(Calendar.MINUTE) >= minute && cal.get(Calendar.SECOND) >= seconds) {
	        cal.add(Calendar.DATE, 1); //do it tomorrow if now is after "hours:minute:seconds"
	    }
	    //we need to set this calendar instance to 7:00pm, or whatever.
	    cal.set(Calendar.HOUR_OF_DAY, hour);
	    cal.set(Calendar.MINUTE, minute);
	    cal.set(Calendar.SECOND, seconds);
	    cal.set(Calendar.MILLISECOND, 0);
	 
	    //cal is now properly set to the next time it will be 7:00pm
	 
	    long offset = cal.getTimeInMillis() - now;
	    long ticks = offset / 50L; //there are 50 milliseconds in a tick
	 
	    //we now know how many ticks are between now and the next time it is 7:00pm
	    //we schedule an event to go off the next time it is 7:00pm,
	    //and repeat every 24 hours.
	    return Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, task, ticks, 1728000L); 
	}
	
	public boolean isParsable(String input){
	    boolean parsable = true;
	    try{
	        Integer.parseInt(input);
	    }catch(NumberFormatException e){
	        parsable = false;
	    }
	    return parsable;
	}
	
	public int getMaxKey(FileConfiguration fileConfiguration) {
		int maxKey = 0;
		for (String s : fileConfiguration.getKeys(false)) {
			if (isParsable(s)) {
				int number = Integer.parseInt(s);
				if (number > maxKey) {
					maxKey = number;
				}
			}
		}
		return maxKey;
	}
	
}
