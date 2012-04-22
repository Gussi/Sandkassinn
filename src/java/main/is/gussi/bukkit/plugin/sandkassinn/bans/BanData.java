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
	
	public BanData() {
		this.date_executed = System.currentTimeMillis()/1000L;
		return;
	}
	
	public BanData(String banned, Type type, String reason, String executor, long date_expire) {
		this.banned = banned;
		this.type = type;
		this.reason = reason;
		this.executor = executor;
		this.date_executed = System.currentTimeMillis()/1000L;
		this.date_expire = this.date_executed + date_expire;
	}

	public BanData(String banned, Type type, String reason, String executor, long date_expire, long date_executed) {
		this.banned = banned;
		this.type = type;
		this.reason = reason;
		this.executor = executor;
		this.date_executed = date_executed;
		this.date_expire = date_expire;
	}
	
	@Override
	public String toString() {
		return "BanData [banned=" + banned + ", type=" + type + ", reason="
				+ reason + ", executor=" + executor + ", date_executed="
				+ date_executed + ", date_expired=" + date_expire + "]";
	}
	
	/**
	 * Sanity check for ban data
	 * @return Boolean					TRUE on sane, FALSE otherwise
	 */
	public Boolean sanityCheck() {
		switch(this.type) {
			case PARDON:
				if (this.banned == null) return false;
				if (this.reason == null) return false;
				if (this.executor == null) return false;
				if (this.date_executed == 0) return false;
				break;
			case PERMABAN:
				if (this.banned == null) return false;
				if (this.reason == null) return false;
				if (this.executor == null) return false;
				if (this.date_executed == 0) return false;
				break;
			case TEMPBAN:
				if (this.banned == null) return false;
				if (this.reason == null) return false;
				if (this.executor == null) return false;
				if (this.date_executed == 0) return false;
				if (this.date_expire == 0) return false;
				break;
			case WARNING:
				if (this.banned == null) return false;
				if (this.reason == null) return false;
				if (this.executor == null) return false;
				if (this.date_executed == 0) return false;
				if (this.date_expire == 0) return false;
				break;
			default:
				return false;
		}
		return true;
	}
}
