package dev.tehbrian.simplechairs.listener;

import dev.tehbrian.simplechairs.SimpleChairs;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class TrySitEventListener implements Listener {

    protected final SimpleChairs plugin;

    public TrySitEventListener(final SimpleChairs plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK) && (event.getHand() == EquipmentSlot.HAND)) {
            final Player player = event.getPlayer();
            final Block block = event.getClickedBlock();
            final Location sitLocation = this.plugin.getSitUtils().calculateSitLocation(player, block);
            if ((sitLocation != null) && this.plugin.getPlayerSitData().sitPlayer(player, block, sitLocation)) {
                event.setCancelled(true);
            }
        }
    }

}
