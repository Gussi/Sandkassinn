package is.gussi.bukkit.plugin.sandkassinn;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import is.gussi.bukkit.plugin.sandkassinn.bans.BanData;
import is.gussi.bukkit.plugin.sandkassinn.bans.Datasource;
import is.gussi.bukkit.plugin.sandkassinn.bans.datasource.DatasourceMySQL;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class Bans implements Listener {
	public Datasource ds;

	public Bans(Sandkassinn plugin) {
		// Init datasource
		this.ds = new DatasourceMySQL();
		// Check if enabled, disable silently if not
		if(!Sandkassinn.plugin.getConfig().getBoolean("sandkassinn.modules.bans.enabled")) {
			return;
		}

		// Check if perm service is set, disable if not
		if(Sandkassinn.perms == null) {
			Sandkassinn.log.warning("Disabling bans module - missing permissions.");
			return;
		}

		// Register events
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		// Tempban command handler
		plugin.getCommand("tempban").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				// Two arguments is a must, we can work up from there
				if (args.length < 2) {
					return false;
				}

				// Set ban data
				BanData data = new BanData();
				data.type = BanData.Type.TEMPBAN;
				data.executor = sender.getName();
				data.date_executed = System.currentTimeMillis() / 1000L;

				// Get Player
				// TODO: Remove code dupe
				OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(args[0]);
				if (!player.hasPlayedBefore()) {
					sender.sendMessage(ChatColor.RED + "Player " + ChatColor.DARK_RED + args[0] + ChatColor.RESET + ChatColor.RED + " has never been on this server");
					return true;
				}
				data.banned = player.getName();

				// Parse time
				try {
					data.date_expire = data.date_executed + Bans.getTime(args[1]);
				} catch (Exception e) {
					sender.sendMessage(ChatColor.RED + "Time " + ChatColor.DARK_RED + args[1] + ChatColor.RED + " is invalid.");
					return false;
				}

				// Get optional reason, rest of args
				// TODO: Remove code dupe
				if (args.length > 2) {
					StringBuilder reason = new StringBuilder();
					for (int i = 2; i < args.length; ++i) {
						reason.append(" ").append(args[i]);
					}
					data.reason = reason.toString();
				} else {
					data.reason = Sandkassinn.plugin.getConfig().getString("sandkassinn.modules.ban.default-reason");
					if (data.reason == null) {
						data.reason = "No reason";
					}
				}
				
				// Add data
				ds.add(data);

				return true;
			}
		});

		// Ban command handler
		plugin.getCommand("ban").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				// One argument is a must, we can work up from there
				if (args.length < 1) {
					return false;
				}
				
				// Set ban data
				BanData data = new BanData();
				data.type = BanData.Type.PERMABAN;
				data.executor = sender.getName();
				data.date_executed = System.currentTimeMillis() / 1000L;
				data.date_expire = 0L;
				
				// Get player
				// TODO: Remove code dupe
				OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(args[0]);
				if (!player.hasPlayedBefore()) {
					sender.sendMessage(ChatColor.RED + "Player " + ChatColor.DARK_RED + args[0] + ChatColor.RESET + ChatColor.RED + " has never been on this server");
					return true;
				}
				data.banned = player.getName();
				
				// Get optional reason, rest of args
				// TODO: Remove code dupe
				if (args.length > 1) {
					StringBuilder reason = new StringBuilder();
					for (int i = 1; i < args.length; ++i) {
						reason.append(" ").append(args[i]);
					}
					data.reason = reason.toString();
				} else {
					data.reason = Sandkassinn.plugin.getConfig().getString("sandkassinn.modules.ban.default-reason");
					if (data.reason == null) {
						data.reason = "No reason";
					}
				}
				
				// Add data
				ds.add(data);
				
				return true;
			}
		});

		// Unban command handler
		plugin.getCommand("unban").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				// TODO Implement unban
				return false;
			}
		});
		
		// Warn command handler
		plugin.getCommand("warning").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				// TODO Implement warning
				return false;
			}
		});

		Sandkassinn.log.info("Ban module enabled.");
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void eventLogin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		BanData data = ds.check(player);
		if (data == null) {
			return;
		}
		
		// Might has ban
		switch(data.type) {
			// Unbanned, do nothing
			case PARDON:
				break;
			// Permabanned, frown upon user
			case PERMABAN:
				e.setJoinMessage(null);
				player.kickPlayer(ChatColor.DARK_RED + "Endanlegt bann : " + ChatColor.RED + data.reason);
				break;
			// Temporary ban, frown upon user
			case TEMPBAN:
				e.setJoinMessage(null);
				Date date = new Date((long)data.date_expire*1000);
				DateFormat fmt = new SimpleDateFormat("dd.MM.yyyy hh:mm");
				player.kickPlayer(ChatColor.DARK_RED + "Tímabundid bann til " + fmt.format(date) + " : " + ChatColor.RED + data.reason);
				break;
			// Warning, warn user
			case WARNING:
				player.sendMessage(ChatColor.DARK_RED + "Advörun : " + ChatColor.RED + data.reason);
				break;
		}
	}

	/**
	 * Convert something like 1Y2M3w4d5h6m7s to seconds
	 *
	 * @param time
	 * @return
	 * @throws Exception Invalid time format
	 */
	public static long getTime(String time) throws Exception {
		long seconds = 0;
		StringBuilder stack = new StringBuilder();
		for(int i = 0; i < time.length(); ++i) {
			if(Character.isDigit(time.charAt(i))) {
				stack.append(time.charAt(i));
			} else {
				int s = Integer.parseInt(stack.toString());
				stack = new StringBuilder();

				// Seconds
				if(time.charAt(i) == 's') {
					seconds += s;
					continue;
				}

				// Minutes
				s = s * 60;
				if(time.charAt(i) == 'm') {
					seconds += s;
					continue;
				}

				// Hours
				s = s * 60;
				if(time.charAt(i) == 'h') {
					seconds += s;
					continue;
				}

				// Days
				s = s * 24;
				if(time.charAt(i) == 'd') {
					seconds += s;
					continue;
				}

				// Weeks
				s = s * 7;
				if(time.charAt(i) == 'w') {
					seconds += s;
					continue;
				}

				// Months
				s = s * 4;
				if(time.charAt(i) == 'M') {
					seconds += s;
					continue;
				}

				// Years
				s = s * 12;
				if(time.charAt(i) == 'Y') {
					seconds += s;
					continue;
				}

				throw new Exception("Invalid time format");
			}
		}
		return seconds;
	}
}
