package dev.tehbrian.simplechairs;

import dev.tehbrian.simplechairs.command.ChairsCommand;
import dev.tehbrian.simplechairs.config.ChairsConfig;
import dev.tehbrian.simplechairs.listener.InvalidPositionLoginListener;
import dev.tehbrian.simplechairs.listener.TrySitEventListener;
import dev.tehbrian.simplechairs.listener.TryUnsitEventListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public final class SimpleChairs extends JavaPlugin {

    private static SimpleChairs instance;

    private final ChairsConfig config = new ChairsConfig(this);
    private final PlayerSitData sitData = new PlayerSitData(this);
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
        return this.sitData;
    }

    public SitUtils getSitUtils() {
        return this.utils;
    }

    @Override
    public void onEnable() {
        try {
            this.getClass().getClassLoader().loadClass(EntityDismountEvent.class.getName());
        } catch (final ClassNotFoundException e) {
            this.getSLF4JLogger().error("Missing EntityDismountEvent. Update your server to a newer version.", e);
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
        this.getCommand("chairs").setExecutor(new ChairsCommand(this));
    }

    @Override
    public void onDisable() {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (this.sitData.isSitting(player)) {
                this.sitData.unsitPlayerForce(player, true);
            }
        }
    }

    @Override
    public void reloadConfig() {
        this.config.reloadConfig();
    }

}
