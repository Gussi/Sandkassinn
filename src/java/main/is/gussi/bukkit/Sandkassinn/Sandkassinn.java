package is.gussi.bukkit.Sandkassinn;

import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.java.JavaPlugin;

public class Sandkassinn extends JavaPlugin {
	// Basic stuff
	public static Sandkassinn plugin;
	public static Log log;
	
	// Service providers
	public static Permission perms;
	public static Economy econ;
	public static Chat chat;

	@Override
	public void onEnable() {
		Sandkassinn.plugin = this;
		Sandkassinn.log = new Log(this);
		
		// Check if Vault is there
		if(getServer().getPluginManager().getPlugin("Vault") == null) {
			Sandkassinn.log.severe("Vault plugin not found, disabling");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		// Get service providers, care less if it's enabled or not
		Sandkassinn.perms = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
		Sandkassinn.econ = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
		Sandkassinn.chat = getServer().getServicesManager().getRegistration(Chat.class).getProvider();
		
		// Register modules
		new Bans(this);

		Sandkassinn.log.info(this.getDescription().getName() + " version " + this.getDescription().getVersion() + " is enabled");
	}

	@Override
	public void onDisable() {
		Sandkassinn.log.info(this.getDescription().getName() + " version " + this.getDescription().getVersion() + " is disabled");
	}

	/**
	 * Logger class
	 */
	public class Log {
		private Logger log;
		protected String prefix;

		public Log(JavaPlugin plugin) {
			this.prefix = "[" + plugin.getDescription().getName() + "] ";
			this.log = Logger.getLogger("Minecraft.plugin." + plugin.getDescription().getName());
		}

		public void info(String msg) {
			this.log.info(this.prefix + msg);
		}

		public void warning(String msg) {
			this.log.warning(this.prefix + msg);
		}

		public void severe(String msg) {
			this.log.severe(this.prefix + msg);
		}
	};
}
