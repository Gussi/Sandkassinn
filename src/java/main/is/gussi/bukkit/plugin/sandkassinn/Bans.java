package is.gussi.bukkit.plugin.sandkassinn;

import is.gussi.bukkit.plugin.sandkassinn.Common;
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
import org.bukkit.event.player.PlayerLoginEvent;

public class Bans implements Listener {
	private Datasource ds;

	public Bans(Sandkassinn plugin) {

		// Check if enabled, disable silently if not
		if(!Sandkassinn.plugin.getConfig().getBoolean("sandkassinn.modules.bans.enabled")) {
			return;
		}

		// Check if perm service is set, disable with noise if not
		if(Sandkassinn.perms == null) {
			Sandkassinn.log.warning("Disabling bans module - missing permissions.");
			return;
		}
		
		// Register datasource
		// TODO: Multiple datasources?
		this.ds = new DatasourceMySQL();

		// Register events within class
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
				try {
					data.banned = getPlayer(args[0]).getName();
					data.type = BanData.Type.TEMPBAN;
					data.reason = getReason(args, 2);
					data.executor = sender.getName();
					data.date_expire = getTime(args[1]) + (System.currentTimeMillis()/1000L);
					
					if (!data.sanityCheck()) return false;
				} catch(UserException e) {
					// TODO: i18n
					Common.sendMessage(sender, "Villa: " + e.getMessage());
					return true;
				} catch(Exception e) {
					// TODO: i18n
					Common.sendMessage(sender, "Mega villa, láta Gussa vita");
					e.printStackTrace();
					return true;
				}
				
				// Check if already banned
				// TODO: Remove code dupe
				BanData data_current = ds.check(data.banned);
				
				if (data_current != null) {
					switch (data_current.type) {
						case PERMABAN:
							// TODO: i18n
							Common.sendMessage(sender, "&4{banned} &cer nú thegar endanlega bannadur af &4{executor} &cvegna: &4{reason}", data_current.getHashMap());
							return true;
						case TEMPBAN:
							if(data_current.date_expire * 1000 < System.currentTimeMillis()) {
								break;
							}
							// TODO: i18n
							Common.sendMessage(sender, "&4{banned} &cer nú thegar bannadur til &4{date_expire} &caf &4{executor} &cvegna: &4{reason}", data_current.getHashMap());
							return true;
						case COMPROMISED:
							// TODO: i18n
							Common.sendMessage(sender, "&4{banned} &cer stolinn account - nú thegar í endanlegu banni", data_current.getHashMap());
					}
				}
				
				// Add data and broadcast message
				// TODO: Remove code dupe
				ds.add(data);
				
				// TODO: i18n
				Common.broadcastMessage("&4{executor} &cbannadi &4{banned} &ctil &4{date_expire} &cvegna: &4{reason}", data.getHashMap());

				OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(args[0]);
				if (player.isOnline()) {
					// TODO: Common.kickPlayer wit formatting
					((Player)player).kickPlayer("Bannadur til " + data.getHashMap().get("date_expire") + " vegna: " + data.getHashMap().get("reason"));
				}
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
				try {
					data.banned = getPlayer(args[0]).getName();
					data.type = BanData.Type.PERMABAN;
					data.reason = getReason(args, 1);
					data.executor = sender.getName();
					data.date_expire = 0L;
					
					if (!data.sanityCheck()) return false;
				} catch(UserException e) {
					Common.sendMessage(sender, "Villa: " + e.getMessage());
					return true;
				}
				
				// Check if already banned
				BanData data_current = ds.check(data.banned);
				if (data_current != null) {
					switch (data_current.type) {
						case PERMABAN:
							// TODO: i18n
							Common.sendMessage(sender, "&4{banned} &cer nú thegar endanlega bannadur af &4{executor} &cvegna: &4{reason}", data_current.getHashMap());
							return true;
						case TEMPBAN:
							// Check if already expired
							if(data_current.date_expire*1000 < System.currentTimeMillis()) {
								break;
							}
							// TODO: i18n
							Common.sendMessage(sender, "&4{banned} &cer nú thegar bannadur af &4{executor} &ctil &4{date_expire} &cvegna: &4{reason}", data_current.getHashMap());
							return true;
					}
				}
				
				// Add data and broadcast message
				// TODO: Remove code dupe
				ds.add(data);
				// TODO: i18n
				Common.broadcastMessage("&4{executor} &cbannadi &4{banned} &cendanlega vegna: &4{reason}", data.getHashMap());
				OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(args[0]);
				if (player.isOnline()) {
					// TODO: Common.kickPlayer
					((Player)player).kickPlayer("Endanlegt bann vegna: " + data.reason);
				}
				
				return true;
			}
		});

		// Unban command handler
		plugin.getCommand("unban").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				// One argument is a must, we can work up from there
				if (args.length < 1) {
					return false;
				}
				
				// Set unban data
				BanData data = new BanData();
				try {
					data.banned = getPlayer(args[0]).getName();
					data.type = BanData.Type.PARDON;
					data.reason = getReason(args, 1);
					data.executor = sender.getName();
					data.date_expire = 0L;

					if (!data.sanityCheck()) return false;
				} catch(UserException e) {
					Common.sendMessage(sender, "Villa: " + e.getMessage());
					return true;
				}
				
				BanData data_old = ds.check(data.banned);
				if (data_old == null) {
					// TODO: i18n
					Common.sendMessage(sender, "&4{banned} &cer ekki bannadur", data.getHashMap());
					return true;
				}
				
				switch(data_old.type) {
					case PARDON:
						// TODO: i18n
						Common.sendMessage(sender, "&4{banned} &cvar unbanned af &4{executor} &cthann &4{date_executed} &cvegna: &4{reason}", data_old.getHashMap());
						break;
					case PERMABAN:
						// TODO: i18n
						Common.sendMessage(sender, "&4{banned} &cvar endanlega banned af &4{executor} thann &4{date_executed} &cvegna: &4{reason}", data_old.getHashMap());
						ds.add(data);
						// TODO: i18n
						Common.broadcastMessage("&aEndanlegt bann á &2{banned} &avar aflétt af &2{executor} &avegna: &2{reason}", data.getHashMap());
						break;
					case TEMPBAN:
						// TODO: i18n
						Common.sendMessage(sender, "&4{banned} &cvar tempbanned af &4{executor} &cthann &4{date_executed} &ctil &4{date_expire} &cvegna: &4{reason}", data_old.getHashMap());
						ds.add(data);
						// TODO: i18n
						Common.broadcastMessage("&aTímabundid bann á &2{banned} &avar aflétt af &2{executor} &avegna: &2{reason}", data.getHashMap());
						break;
					case WARNING:
						// TODO: i18n
						Common.sendMessage(sender, "&4{banned} &cfékk advörun frá &4{executor} &cthann &4{date_executed} &cvegna: &4{reason}", data_old.getHashMap());
						ds.add(data);
						// TODO: i18n
						Common.broadcastMessage("&aAdvörun á &2{banned} &avar aflétt af &2{executor} &avegna: &2{reason}", data.getHashMap());
						break;
				}
				return true;
			}
		});
		
		// Warn command handler
		plugin.getCommand("warning").setExecutor(new CommandExecutor() {
			@Override
			public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
				// Three arguments is a must
				if (args.length < 3) {
					return false;
				}
				
				// Set unban data
				BanData data = new BanData();
				try {
					data.banned = getPlayer(args[0]).getName();
					data.type = BanData.Type.WARNING;
					data.reason = getReason(args, 2);
					data.executor = sender.getName();
					data.date_expire = 0L;

					if (!data.sanityCheck()) return false;
				} catch(UserException e) {
					Common.sendMessage(sender, "Villa: " + e.getMessage());
					return true;
				} catch(Exception e) {
					Common.sendMessage(sender, "Major villa, láta Gussa vita.");
					return true;
				}
				
				ds.add(data);
				// TODO: i18n
				Bukkit.getServer().broadcastMessage(ChatColor.DARK_RED + sender.getName() + ChatColor.RED + " varar " + ChatColor.DARK_RED + data.banned + ChatColor.RED + ": " + ChatColor.DARK_RED + data.reason);
				
				return true;
			}
		});

		Sandkassinn.log.info("Ban module enabled.");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void eventPlayerLogin(PlayerLoginEvent e) {
		Player player = e.getPlayer();
		BanData data = ds.check(player.getName());
		if (data != null) {
			switch (data.type) {
				case COMPROMISED:
					Sandkassinn.log.info(Common.buildString("Connection attempt from stolen account {banned}", data.getHashMap()));
					// TODO: i18n
					e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Common.buildString("&4Stolinn account - IP logged", data.getHashMap()));
					break;
				case PERMABAN:
					Sandkassinn.log.info(Common.buildString("Connection attempt from permabanned account {banned}", data.getHashMap()));
					// TODO: i18n
					e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Common.buildString("&cEndanlegt bann vegna: &4{reason}", data.getHashMap()));
					break;
				case TEMPBAN:
					if (data.date_expire * 1000L < System.currentTimeMillis()) {
						// Ban expired, do nothing
						break;
					}
					Sandkassinn.log.info(Common.buildString("Connection attempt from tempbanned account {banned} ", data.getHashMap()));
					// TODO: i18n
					e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Common.buildString("&cTímabundid bann til &4{date_expire} &cvegna: &4{reason}", data.getHashMap()));
					break;
			}
		}
	}

	/**
	 * Get reason from arguments
	 * @param args					Arguments passed to command
	 * @param start					Start from given arg index
	 * @return						String with given reason, or default reason
	 */
	private String getReason(String[] args, int start) {
		if (args.length > start) {
			StringBuilder reason = new StringBuilder();
			reason.append(args[start]);
			for (int i = start+1; i < args.length; ++i) {
				reason.append(" ").append(args[i]);
			}
			return reason.toString();
		} else {
			String reason = Sandkassinn.plugin.getConfig().getString("sandkassinn.modules.ban.default-reason");
			if (reason == null) {
				// TODO: i18n
				reason = "Engin ástæda";
			}
			return reason;
		}
	}

	private OfflinePlayer getPlayer(String playerName) throws UserException {
		OfflinePlayer player = Bukkit.getServer().getOfflinePlayer(playerName);
		// TODO: Make it work properly
		// if (!player.hasPlayedBefore()) {
		// 	throw new UserException(playerName + " hefur aldrei komid inn á serverinn");
		//}
		return player;
	}

	/**
	 * Convert something like 1Y2M3w4d5h6m7s to seconds
	 *
	 * @param time
	 * @return
	 * @throws Exception Invalid time format
	 */
	private long getTime(String time) throws Exception {
		Sandkassinn.log.info("Time:" + time);
		long seconds = 0;
		StringBuilder stack = new StringBuilder();
		for(int i = 0; i < time.length(); ++i) {
			if(Character.isDigit(time.charAt(i))) {
				stack.append(time.charAt(i));
			} else {
				int s = 0;
				try {
					s = Integer.parseInt(stack.toString());
				} catch(NumberFormatException e) {
					throw new UserException(time + " is not a proper time format");
				}
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

				throw new UserException(time.charAt(i) + " is an invalid token in " + time);
			}
		}
		if (seconds == 0) {
			throw new UserException("Return value is zero seconds");
		}
		return seconds;
	}
}
