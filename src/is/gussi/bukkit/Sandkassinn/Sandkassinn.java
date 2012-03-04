package is.gussi.bukkit.Sandkassinn;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class Sandkassinn extends JavaPlugin {
	public static Sandkassinn plugin;
	public static final SandkassinnLogger log = new SandkassinnLogger();

	@Override
	public void onDisable() {
		Sandkassinn.log.info(this.getDescription().getName() + " version " + this.getDescription().getVersion() + " is enabled");
	}

	@Override
	public void onEnable() {
		Sandkassinn.plugin = this;
		Sandkassinn.log.info(this.getDescription().getName() + " version " + this.getDescription().getVersion() + " is disabled");
	}

	/**
	 * Static logger class
	 */
	public static class SandkassinnLogger {
		private final Logger log = Logger.getLogger("is.gussi.bukkit.plugin.sandkassinn");
		protected String prefix = "[GateKeeper] ";

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
