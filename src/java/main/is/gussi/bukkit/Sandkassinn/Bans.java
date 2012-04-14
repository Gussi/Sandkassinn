package is.gussi.bukkit.Sandkassinn;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class Bans implements Listener {
	public Bans(Sandkassinn plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void eventLogin(PlayerLoginEvent e) {
		Sandkassinn.log.info("player login event");
	}
	
	@EventHandler
	public void eventCommand(ServerCommandEvent e) {
		Sandkassinn.log.info("server command event");
	}
}
