package dev.tehbrian.simplechairs;

import dev.tehbrian.simplechairs.api.SimpleChairs;
import dev.tehbrian.simplechairs.command.ChairsCommand;
import dev.tehbrian.simplechairs.config.ChairsConfig;
import dev.tehbrian.simplechairs.listener.InvalidPositionLoginListener;
import dev.tehbrian.simplechairs.listener.TrySitEventListener;
import dev.tehbrian.simplechairs.listener.TryUnsitEventListener;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class SimpleChairsPlugin extends JavaPlugin implements SimpleChairs {

	private final ChairsConfig config = new ChairsConfig(this);
	private final PlayerSitService sitService = new PlayerSitService(this);
	private final SitUtils utils = new SitUtils(this);

	public ChairsConfig getChairsConfig() {
		return this.config;
	}

	public PlayerSitService getSitService() {
		return this.sitService;
	}

	public SitUtils getSitUtils() {
		return this.utils;
	}

	@Override
	public void onEnable() {
		final Path dataFolderPath = this.getDataFolder().toPath();
		try {
			// data folder may not exist.
			if (Files.notExists(dataFolderPath)) {
				Files.createDirectory(dataFolderPath);
			}

			Files.copy(
					Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("config_help.txt")),
					dataFolderPath.resolve("config_help.txt"),
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
		for (final Player player : this.getServer().getOnlinePlayers()) {
			if (this.sitService.isSitting(player)) {
				this.sitService.unsitForce(player, true);
			}
		}

		this.config.saveToConfig();
	}

	@Override
	public void reloadConfig() {
		this.config.loadFromConfig();
	}

	public boolean isSitting(final Player player) {
		return this.getSitService().isSitting(player);
	}

	public boolean sit(final Player player, final Block blockToOccupy, final Location perch) {
		return this.getSitService().sit(player, blockToOccupy, perch);
	}

	public void unsit(final Player player) {
		this.getSitService().unsitForce(player, true);
	}

	public boolean isBlockOccupied(final Block block) {
		return this.getSitService().isBlockOccupied(block);
	}

	public @Nullable Player getBlockOccupant(final Block block) {
		return this.getSitService().getBlockOccupant(block);
	}

	public boolean isSittingDisabled(final Player player) {
		return this.getSitService().isSittingDisabled(player);
	}

	public void setSittingDisabled(final Player player, final boolean sittingDisabled) {
		this.getSitService().setSittingDisabled(player, sittingDisabled);
	}

}
