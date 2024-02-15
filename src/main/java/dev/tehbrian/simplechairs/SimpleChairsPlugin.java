package dev.tehbrian.simplechairs;

import dev.tehbrian.simplechairs.api.SimpleChairs;
import dev.tehbrian.simplechairs.command.ChairsCommand;
import dev.tehbrian.simplechairs.config.ChairsConfig;
import dev.tehbrian.simplechairs.listener.InvalidPositionLoginListener;
import dev.tehbrian.simplechairs.listener.TrySitEventListener;
import dev.tehbrian.simplechairs.listener.TryUnsitEventListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class SimpleChairsPlugin extends JavaPlugin implements SimpleChairs {

  private final ChairsConfig config = new ChairsConfig(this);
  private final PlayerSitData sitData = new PlayerSitData(this);
  private final SitUtils utils = new SitUtils(this);

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
      // data folder may not exist on first start-up.
      if (Files.notExists(this.getDataFolder().toPath())) {
        Files.createDirectory(this.getDataFolder().toPath());
      }

      Files.copy(
          Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("config_help.txt")),
          new File(this.getDataFolder(), "config_help.txt").toPath(),
          StandardCopyOption.REPLACE_EXISTING
      );
    } catch (final IOException | NullPointerException e) {
      this.getSLF4JLogger().warn(
          "Failed to copy `config_help.txt` to your config folder. You're on your own, buddy.",
          e
      );
    }

    this.config.loadFromConfig();
    this.config.saveToConfig();

    this.getServer().getPluginManager().registerEvents(new InvalidPositionLoginListener(this), this);
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

    this.config.saveToConfig();
  }

  @Override
  public void reloadConfig() {
    this.config.loadFromConfig();
  }

  public boolean isSitting(final Player player) {
    return this.getPlayerSitData().isSitting(player);
  }

  public boolean isBlockOccupied(final Block block) {
    return this.getPlayerSitData().isBlockOccupied(block);
  }

  public Player getBlockOccupiedBy(final Block block) {
    return this.getPlayerSitData().getPlayerOnChair(block);
  }

  public boolean sit(final Player player, final Block blockToOccupy, final Location sitLocation) {
    return this.getPlayerSitData().sitPlayer(player, blockToOccupy, sitLocation);
  }

  public void unsit(final Player player) {
    this.getPlayerSitData().unsitPlayerForce(player, true);
  }

  public void setSittingDisabled(final Player player, final boolean bool) {
    this.getPlayerSitData().setSittingDisabled(player, bool);
  }

  public boolean isSittingDisabled(final Player player) {
    return this.getPlayerSitData().isSittingDisabled(player);
  }

}
