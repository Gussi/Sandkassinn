package is.gussi.bukkit.plugin.sandkassinn.bans;

import org.bukkit.entity.Player;

/**
 * Common datasource interface for bans
 * 
 * @author Gussi
 */
public interface Datasource {
	public boolean add(BanData data);
	public BanData check(Player player);
}
