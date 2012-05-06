package is.gussi.bukkit.plugin.sandkassinn;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoCollect implements Listener {

	public AutoCollect(JavaPlugin plugin) {
		// Check if this module is enabled
		if (!Sandkassinn.plugin.getConfig().getBoolean("sandkassinn.modules.autocollect.enabled")) {
			return;
		}
		
		// Check if perms are enabled
		if (Sandkassinn.perms == null) {
			Sandkassinn.log.warning("Disabling autocollect module - missing permissions.");
			return;
		}
		
		// Register events
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		
		Sandkassinn.log.info("AutoCollect module enabled.");
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		// Check perms
		if (!Sandkassinn.perms.playerHas(p, "sandkassinn.autocollect")) {
			return;
		}
		e.setCancelled(true);
		Block b = e.getBlock();
		for (ItemStack dropped_item : b.getDrops()) {
			if (p.getInventory().firstEmpty() != -1) {
				// Inventory not full, give item
				p.getInventory().addItem(dropped_item);
				continue;
			} else {
				// Inventory full, find first non-full same material stack if any
				boolean item_added = false;
				for (ItemStack inventory_item : p.getInventory().getContents()) {
					if (inventory_item.getType() == dropped_item.getType() && (p.getInventory().getMaxStackSize() - inventory_item.getAmount()) >= dropped_item.getAmount() ) {
						p.getInventory().addItem(dropped_item);
						item_added = true;
						break;
					}
				}
				if (!item_added) {
					b.getWorld().dropItem(b.getLocation(), dropped_item);
				}
			}
		}
		b.setType(Material.AIR);
	}
}
