package is.gussi.bukkit.plugin.sandkassinn;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

/**
 * Common methods shared between modules
 *
 * @author Gussi
 */
public class Common {
	public static void sendMessage(CommandSender sender, String message) {
		Common.sendMessage(sender, message, new HashMap<String,String>());
	}

	public static void sendMessage(CommandSender sender, String message, HashMap<String, String> tokens) {
		sender.sendMessage(buildString(message, tokens));
	}

	public static void broadcastMessage(String message) {
		Common.broadcastMessage(message, new HashMap<String, String>());
	}

	public static void broadcastMessage(String message, HashMap<String, String> tokens) {
		Bukkit.getServer().broadcastMessage(buildString(message, tokens));
	}

	public static String buildString(String message, HashMap<String, String> tokens) {
		Pattern pattern = Pattern.compile("\\{(.+?)\\}");
		Matcher matcher = pattern.matcher(message);
		StringBuilder builder = new StringBuilder();
		int i = 0;
		while (matcher.find()) {
			String replacement = null;
			try {
				replacement = tokens.get(matcher.group(1));
			} catch(NullPointerException e) {
				Sandkassinn.log.severe("Invalid token: " + matcher.group(1));
				e.printStackTrace();
			}
		    builder.append(message.substring(i, matcher.start()));
		    if (replacement == null)
		        builder.append(matcher.group(0));
		    else
		        builder.append(replacement);
		    i = matcher.end();
		}
		builder.append(message.substring(i, message.length()));
		return builder.toString().replaceAll("&", new Character((char) 167).toString());
	}
}