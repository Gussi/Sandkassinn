package is.gussi.bukkit.plugin.sandkassinn;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoExperience {
	public AutoExperience(JavaPlugin plugin) {
		// Check if this module is enabled
		if (!Sandkassinn.plugin.getConfig().getBoolean("sandkassinn.modules.autoexperience.enabled")) {
			return;
		}
		
		// Check if perms are enabled
		if (Sandkassinn.perms == null) {
			Sandkassinn.log.warning("Disabling autocollect module - missing permissions.");
			return;
		}

		plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {

			@Override
			public void run() {
				// foreach player
				for (Player player: Sandkassinn.plugin.getServer().getOnlinePlayers()) {
					// Check perm for player
					if (!Sandkassinn.perms.playerHas(player, "sandkassinn.autoexperience")) {
						continue;
					}
					
					float exp_add = (1f / (10f * ((float)player.getLevel() + 1))) * 2f; // Horrible formula
					if (player.getExp() + exp_add > 1f) {
						// If exp exceeds 1, raise lvl and add remaining exp
						player.setExp(exp_add - 1f);
						player.setLevel(player.getLevel() + 1);
					} else {
						// Else, just add the exp
						player.setExp(player.getExp() + exp_add);
					}
				}
			}
			
		}, 200L, 200L);
		
		Sandkassinn.log.info("AutoExperience module enabled.");
	}
}
