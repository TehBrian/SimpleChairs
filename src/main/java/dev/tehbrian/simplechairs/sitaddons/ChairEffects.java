package dev.tehbrian.simplechairs.sitaddons;

import dev.tehbrian.simplechairs.SimpleChairs;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import dev.tehbrian.simplechairs.config.ChairsConfig;
import dev.tehbrian.simplechairs.PlayerSitData;

public class ChairEffects {

    protected final SimpleChairs plugin;
    protected final ChairsConfig config;
    protected final PlayerSitData sitdata;
    protected int healTaskID = -1;
    protected int pickupTaskID = -1;

    public ChairEffects(final SimpleChairs plugin) {
        this.plugin = plugin;
        this.config = plugin.getChairsConfig();
        this.sitdata = plugin.getPlayerSitData();
    }

    protected void startHealing() {
        this.healTaskID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                this.plugin,
            () ->
                Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("chairs.sit.health"))
                .filter(this.plugin.getPlayerSitData()::isSitting)
                .forEach(p -> {
                    final double health = p.getHealth();
                    final double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                    if ((((health / maxHealth) * 100d) < this.config.effectsHealMaxHealth) && (health < maxHealth)) {
                        double newHealth = this.config.effectsHealHealthPerInterval + health;
                        if (newHealth > maxHealth) {
                            newHealth = maxHealth;
                        }
                        p.setHealth(newHealth);
                    }
                }),
                this.config.effectsHealInterval, this.config.effectsHealInterval
        );
    }

    public void cancelHealing() {
        if (this.healTaskID != -1) {
            this.plugin.getServer().getScheduler().cancelTask(this.healTaskID);
            this.healTaskID = -1;
        }
    }

    public void restartHealing() {
        this.cancelHealing();
        this.startHealing();
    }

    protected void startPickup() {
        this.pickupTaskID = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
                this.plugin,
            () ->
                Bukkit.getOnlinePlayers().stream()
                .filter(this.plugin.getPlayerSitData()::isSitting)
                .forEach(p -> {
                    for (final Entity entity : p.getNearbyEntities(1, 2, 1)) {
                        if (entity instanceof Item) {
                            final Item item = (Item) entity;
                            if (item.getPickupDelay() == 0) {
                                if (p.getInventory().firstEmpty() != -1) {
                                    final EntityPickupItemEvent pickupevent = new EntityPickupItemEvent(p, item, 0);
                                    Bukkit.getPluginManager().callEvent(pickupevent);
                                    if (!pickupevent.isCancelled()) {
                                        p.getInventory().addItem(item.getItemStack());
                                        entity.remove();
                                    }
                                }
                            }
                        } else if (entity instanceof ExperienceOrb) {
                            final ExperienceOrb eorb = (ExperienceOrb) entity;
                            int exptoadd = eorb.getExperience();
                            while (exptoadd > 0) {
                                int localexptoadd = 0;
                                if (p.getExpToLevel() < exptoadd) {
                                    localexptoadd = p.getExpToLevel();
                                    final PlayerExpChangeEvent expchangeevent = new PlayerExpChangeEvent(p, localexptoadd);
                                    Bukkit.getPluginManager().callEvent(expchangeevent);
                                    p.giveExp(expchangeevent.getAmount());
                                    if (p.getExpToLevel() <= 0) {
                                        final PlayerLevelChangeEvent levelchangeevent = new PlayerLevelChangeEvent(p, p.getLevel(), p.getLevel()+1);
                                        Bukkit.getPluginManager().callEvent(levelchangeevent);
                                        p.setExp(0);
                                        p.giveExpLevels(1);
                                    }
                                } else {
                                    localexptoadd = exptoadd;
                                    final PlayerExpChangeEvent expchangeevent = new PlayerExpChangeEvent(p, localexptoadd);
                                    Bukkit.getPluginManager().callEvent(expchangeevent);
                                    p.giveExp(expchangeevent.getAmount());
                                }
                                exptoadd -= localexptoadd;
                            }
                            entity.remove();
                        }
                    }
                }),
            1,1
        );
    }

    public void cancelPickup() {
        if (this.pickupTaskID != -1) {
            this.plugin.getServer().getScheduler().cancelTask(this.pickupTaskID);
        }
        this.pickupTaskID = -1;
    }

    public void restartPickup() {
        this.cancelPickup();
        this.startPickup();
    }

}
