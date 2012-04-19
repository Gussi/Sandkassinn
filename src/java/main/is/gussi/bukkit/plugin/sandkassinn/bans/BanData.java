package is.gussi.bukkit.plugin.sandkassinn.bans;

public class BanData {
	public enum Type {
		PERMABAN,
		TEMPBAN,
		WARNING,
		PARDON
	}
	public String banned;
	public Type type;
	public String reason;
	public String executor;
	public long date_executed;
	public long date_expire;

	@Override
	public String toString() {
		return "BanData [banned=" + banned + ", type=" + type + ", reason="
				+ reason + ", executor=" + executor + ", date_executed="
				+ date_executed + ", date_expired=" + date_expire + "]";
	}
}
