package is.gussi.bukkit.plugin.sandkassinn;

@SuppressWarnings("serial")
public class UserException extends Exception {
	public UserException() {}
	public UserException(String msg) { super(msg); }
	public UserException(Throwable cause) { super(cause); }
	public UserException(String msg, Throwable cause) { super(msg, cause); }
}
