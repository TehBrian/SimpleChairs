package dev.tehbrian.simplechairs;

import dev.tehbrian.simplechairs.command.ChairsCommand;
import dev.tehbrian.simplechairs.config.ChairsConfig;
import dev.tehbrian.simplechairs.listener.InvalidPositionLoginListener;
import dev.tehbrian.simplechairs.listener.TrySitEventListener;
import dev.tehbrian.simplechairs.listener.TryUnsitEventListener;
import dev.tehbrian.simplechairs.sitaddons.ChairEffects;
import dev.tehbrian.simplechairs.sitaddons.CommandRestrict;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public class SimpleChairs extends JavaPlugin {

    private static SimpleChairs instance;
    private final ChairsConfig config = new ChairsConfig(this);
    private final PlayerSitData psitdata = new PlayerSitData(this);
    private final ChairEffects chairEffects = new ChairEffects(this);
    private final SitUtils utils = new SitUtils(this);

    public SimpleChairs() {
        instance = this;
    }

    public static SimpleChairs getInstance() {
        return instance;
    }

    public ChairsConfig getChairsConfig() {
        return this.config;
    }

    public PlayerSitData getPlayerSitData() {
        return this.psitdata;
    }

    public ChairEffects getChairEffects() {
        return this.chairEffects;
    }

    public SitUtils getSitUtils() {
        return this.utils;
    }

    @Override
    public void onEnable() {
        try {
            this.getClass().getClassLoader().loadClass(EntityDismountEvent.class.getName());
        } catch (final Throwable t) {
            this.getLogger().log(Level.SEVERE, "Missing EntityDismountEvent", t);
            this.setEnabled(false);
            return;
        }
        try {
            Files.copy(
                    this.getClass().getClassLoader().getResourceAsStream("config_help.txt"),
                    new File(this.getDataFolder(), "config_help.txt").toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (final IOException e) {
        }
        this.reloadConfig();
        this.getServer().getPluginManager().registerEvents(new InvalidPositionLoginListener(), this);
        this.getServer().getPluginManager().registerEvents(new TrySitEventListener(this), this);
        this.getServer().getPluginManager().registerEvents(new TryUnsitEventListener(this), this);
        this.getServer().getPluginManager().registerEvents(new CommandRestrict(this), this);
        this.getCommand("chairs").setExecutor(new ChairsCommand(this));
    }

    @Override
    public void onDisable() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (this.psitdata.isSitting(player)) {
                this.psitdata.unsitPlayerForce(player, true);
            }
        }
        this.chairEffects.cancelHealing();
        this.chairEffects.cancelPickup();
    }

    @Override
    public void reloadConfig() {
        this.config.reloadConfig();
        if (this.config.effectsHealEnabled) {
            this.chairEffects.restartHealing();
        } else {
            this.chairEffects.cancelHealing();
        }
        if (this.config.effectsItemPickupEnabled) {
            this.chairEffects.restartPickup();
        } else {
            this.chairEffects.cancelPickup();
        }
    }

}
