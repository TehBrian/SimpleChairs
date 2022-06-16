package com.cnaude.chairs.sitaddons;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import com.cnaude.chairs.core.Chairs;
import com.cnaude.chairs.core.ChairsConfig;
import com.cnaude.chairs.core.PlayerSitData;

public class ChairEffects {

	protected final Chairs plugin;
	protected final ChairsConfig config;
	protected final PlayerSitData sitdata;
	protected int healTaskID = -1;
	protected int pickupTaskID = -1;

	public ChairEffects(Chairs plugin) {
		this.plugin = plugin;
		this.config = plugin.getChairsConfig();
		this.sitdata = plugin.getPlayerSitData();
	}

	protected void startHealing() {
		healTaskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
			plugin,
			() ->
				Bukkit.getOnlinePlayers().stream()
				.filter(p -> p.hasPermission("chairs.sit.health"))
				.filter(plugin.getPlayerSitData()::isSitting)
				.forEach(p -> {
					double health = p.getHealth();
					double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
					if ((((health / maxHealth) * 100d) < config.effectsHealMaxHealth) && (health < maxHealth)) {
						double newHealth = config.effectsHealHealthPerInterval + health;
						if (newHealth > maxHealth) {
							newHealth = maxHealth;
						}
						p.setHealth(newHealth);
					}
				}),
			config.effectsHealInterval, config.effectsHealInterval
		);
	}

	public void cancelHealing() {
		if (healTaskID != -1) {
			plugin.getServer().getScheduler().cancelTask(healTaskID);
			healTaskID = -1;
		}
	}

	public void restartHealing() {
		cancelHealing();
		startHealing();
	}

	protected void startPickup() {
		pickupTaskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(
			plugin,
			() ->
				Bukkit.getOnlinePlayers().stream()
				.filter(plugin.getPlayerSitData()::isSitting)
				.forEach(p -> {
					for (Entity entity : p.getNearbyEntities(1, 2, 1)) {
						if (entity instanceof Item) {
							Item item = (Item) entity;
							if (item.getPickupDelay() == 0) {
								if (p.getInventory().firstEmpty() != -1) {
									EntityPickupItemEvent pickupevent = new EntityPickupItemEvent(p, item, 0);
									Bukkit.getPluginManager().callEvent(pickupevent);
									if (!pickupevent.isCancelled()) {
										p.getInventory().addItem(item.getItemStack());
										entity.remove();
									}
								}
							}
						} else if (entity instanceof ExperienceOrb) {
							ExperienceOrb eorb = (ExperienceOrb) entity;
							int exptoadd = eorb.getExperience();
							while (exptoadd > 0) {
								int localexptoadd = 0;
								if (p.getExpToLevel() < exptoadd) {
									localexptoadd = p.getExpToLevel();
									PlayerExpChangeEvent expchangeevent = new PlayerExpChangeEvent(p, localexptoadd);
									Bukkit.getPluginManager().callEvent(expchangeevent);
									p.giveExp(expchangeevent.getAmount());
									if (p.getExpToLevel() <= 0) {
										PlayerLevelChangeEvent levelchangeevent = new PlayerLevelChangeEvent(p, p.getLevel(), p.getLevel()+1);
										Bukkit.getPluginManager().callEvent(levelchangeevent);
										p.setExp(0);
										p.giveExpLevels(1);
									}
								} else {
									localexptoadd = exptoadd;
									PlayerExpChangeEvent expchangeevent = new PlayerExpChangeEvent(p, localexptoadd);
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
		if (pickupTaskID != -1) {
			plugin.getServer().getScheduler().cancelTask(pickupTaskID);
		}
		pickupTaskID = -1;
	}

	public void restartPickup() {
		cancelPickup();
		startPickup();
	}

}
