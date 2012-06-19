package is.gussi.bukkit.plugin.sandkassinn;

import java.util.logging.Logger;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
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
		
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		
		// Register services, care less if it's not enabled
		this.setupServices();

		// Register modules
		new Bans(this);
		new AutoCollect(this);
		new AutoExperience(this);

		Sandkassinn.log.info(this.getDescription().getName() + " version " + this.getDescription().getVersion() + " is enabled");
	}
	
	private void setupServices() {
		// Check if vault is enabled
		if(getServer().getPluginManager().getPlugin("Vault") == null) {
			Sandkassinn.log.severe("Vault plugin not found, most if not all features will be disabled");
			return;
		}
		
		// Economy service
		RegisteredServiceProvider<Economy> econRsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (econRsp != null) {
        	econ = econRsp.getProvider();
        }

        // Chat service
        RegisteredServiceProvider<Chat> chatRsp = getServer().getServicesManager().getRegistration(Chat.class);
        if (chatRsp != null) {
        	chat = chatRsp.getProvider();
        }

        // Permission service
        RegisteredServiceProvider<Permission> permsRsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (permsRsp != null) {
        	perms = permsRsp.getProvider();
        }
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
