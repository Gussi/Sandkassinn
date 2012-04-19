package is.gussi.bukkit.plugin.sandkassinn.bans;

/**
 * Common datasource interface for bans
 * 
 * @author Gussi
 */
public interface Datasource {
	public boolean add(BanData data);
	public BanData check(String player);
}
