package dev.tehbrian.simplechairs.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public final class ChairsConfig {

	private static final String SIT_SECTION_PATH = "sit-config";
	private static final String SIT_DISABLED_WORLDS_PATH = "disabled-worlds";
	private static final String SIT_MAX_DISTANCE_PATH = "max-distance";
	private static final String SIT_REQUIRE_EMPTY_HAND_PATH = "require-empty-hand";
	private static final String SIT_STAIRS_SECTION_PATH = "stairs";
	private static final String SIT_STAIRS_ENABLED_PATH = "enabled";
	private static final String SIT_STAIRS_ROTATE_PLAYER_PATH = "rotate-player";
	private static final String SIT_STAIRS_MAX_WIDTH_PATH = "max-width";
	private static final String SIT_STAIRS_SPECIAL_END_PATH = "special-end";
	private static final String SIT_STAIRS_SPECIAL_END_SIGN_PATH = "sign";
	private static final String SIT_STAIRS_SPECIAL_END_CORNER_STAIRS_PATH = "corner-stairs";
	private static final String SIT_STAIRS_HEIGHT = "height";
	private static final String SIT_ADDITIONAL_BLOCKS_PATH = "additional-blocks";

	private static final String MSG_SECTION_PATH = "messages";
	private static final String MSG_ENABLED_PATH = "enabled";
	private static final String MSG_SIT_SECTION_PATH = "sit";
	private static final String MSG_SIT_ENTER_PATH = "enter";
	private static final String MSG_SIT_LEAVE_PATH = "leave";
	private static final String MSG_SIT_ENABLED_PATH = "enabled";
	private static final String MSG_SIT_DISABLED_PATH = "disabled";

	private final JavaPlugin plugin;

	private final Set<String> sitDisabledWorlds = new HashSet<>();
	private final Map<Material, Double> sitAdditionalBlocks = new EnumMap<>(Material.class);
	private boolean sitRequireEmptyHand = false;
	private double sitMaxDistance = 3;
	private boolean sitStairsEnabled = true;
	private boolean sitStairsRotatePlayer = true;
	private int sitStairsMaxWidth = 0;
	private boolean sitStairsSpecialEndEnabled = false;
	private boolean sitStairsSpecialEndSign = false;
	private boolean sitStairsSpecialEndCornerStairs = false;
	private double sitStairsHeight = 0.5D;

	private boolean msgEnabled = true;
	private String msgSitEnter = "&7You are now sitting.";
	private String msgSitLeave = "&7You are no longer sitting.";
	private String msgSitDisabled = "&7You have disabled chairs for yourself.";
	private String msgSitEnabled = "&7You have enabled chairs for yourself.";

	public ChairsConfig(final JavaPlugin plugin) {
		this.plugin = plugin;
	}

	private File getConfigFile() {
		return new File(this.plugin.getDataFolder(), "config.yml");
	}

	public void reloadConfig() {
		this.loadFromConfig();
		this.saveToConfig();
	}

	public void loadFromConfig() {
		final File file = this.getConfigFile();
		final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		final var sitSection = config.getConfigurationSection(SIT_SECTION_PATH);
		if (sitSection != null) {
			this.sitDisabledWorlds.clear();
			this.sitDisabledWorlds.addAll(sitSection.getStringList(SIT_DISABLED_WORLDS_PATH));
			this.sitRequireEmptyHand = sitSection.getBoolean(SIT_REQUIRE_EMPTY_HAND_PATH, this.sitRequireEmptyHand());
			this.sitMaxDistance = sitSection.getDouble(SIT_MAX_DISTANCE_PATH, this.sitMaxDistance());

			final var sitStairsSection = sitSection.getConfigurationSection(SIT_STAIRS_SECTION_PATH);
			if (sitStairsSection != null) {
				this.sitStairsEnabled = sitStairsSection.getBoolean(SIT_STAIRS_ENABLED_PATH, this.sitStairsEnabled());
				this.sitStairsRotatePlayer = sitStairsSection.getBoolean(
						SIT_STAIRS_ROTATE_PLAYER_PATH,
						this.sitStairsRotatePlayer()
				);
				this.sitStairsMaxWidth = sitStairsSection.getInt(SIT_STAIRS_MAX_WIDTH_PATH, this.sitStairsMaxWidth());

				final var sitStairsSpecialEndSection = sitStairsSection.getConfigurationSection(SIT_STAIRS_SPECIAL_END_PATH);
				if (sitStairsSpecialEndSection != null) {
					this.sitStairsSpecialEndSign = sitStairsSpecialEndSection.getBoolean(
							SIT_STAIRS_SPECIAL_END_SIGN_PATH,
							this.sitStairsSpecialEndSign()
					);
					this.sitStairsSpecialEndCornerStairs = sitStairsSpecialEndSection.getBoolean(
							SIT_STAIRS_SPECIAL_END_CORNER_STAIRS_PATH,
							this.sitStairsSpecialEndCornerStairs()
					);
					this.sitStairsSpecialEndEnabled = this.sitStairsSpecialEndSign() || this.sitStairsSpecialEndCornerStairs();
				}

				this.sitStairsHeight = sitStairsSection.getDouble(SIT_STAIRS_HEIGHT, this.sitStairsHeight());
			}

			final var sitAdditionalBlocksSection = sitSection.getConfigurationSection(SIT_ADDITIONAL_BLOCKS_PATH);
			if (sitAdditionalBlocksSection != null) {
				this.sitAdditionalBlocks.clear();
				for (final String materialName : sitAdditionalBlocksSection.getKeys(false)) {
					final Material material = Material.getMaterial(materialName);
					if (material != null) {
						this.sitAdditionalBlocks.put(material, sitAdditionalBlocksSection.getDouble(materialName));
					}
				}
			}
		}

		final var msgSection = config.getConfigurationSection(MSG_SECTION_PATH);
		if (msgSection != null) {
			this.msgEnabled = msgSection.getBoolean(MSG_ENABLED_PATH, this.msgEnabled());

			final var msgSitSection = msgSection.getConfigurationSection(MSG_SIT_SECTION_PATH);
			if (msgSitSection != null) {
				this.msgSitEnter = msgSitSection.getString(MSG_SIT_ENTER_PATH, this.msgSitEnter());
				this.msgSitLeave = msgSitSection.getString(MSG_SIT_LEAVE_PATH, this.msgSitLeave());
				this.msgSitEnabled = msgSitSection.getString(MSG_SIT_ENABLED_PATH, this.msgSitEnabled());
				this.msgSitDisabled = msgSitSection.getString(MSG_SIT_DISABLED_PATH, this.msgSitDisabled());
			}
		}
	}

	public void saveToConfig() {
		final File file = this.getConfigFile();
		final FileConfiguration config = new YamlConfiguration();

		final ConfigurationSection sitSection = config.createSection(SIT_SECTION_PATH);
		sitSection.set(SIT_DISABLED_WORLDS_PATH, new ArrayList<>(this.sitDisabledWorlds()));
		sitSection.set(SIT_REQUIRE_EMPTY_HAND_PATH, this.sitRequireEmptyHand());
		sitSection.set(SIT_MAX_DISTANCE_PATH, this.sitMaxDistance());

		final ConfigurationSection sitStairsSection = sitSection.createSection(SIT_STAIRS_SECTION_PATH);
		sitStairsSection.set(SIT_STAIRS_ENABLED_PATH, this.sitStairsEnabled());
		sitStairsSection.set(SIT_STAIRS_ROTATE_PLAYER_PATH, this.sitStairsRotatePlayer());
		sitStairsSection.set(SIT_STAIRS_MAX_WIDTH_PATH, this.sitStairsMaxWidth());

		final ConfigurationSection sitStairsSpecialEndSection = sitStairsSection.createSection(SIT_STAIRS_SPECIAL_END_PATH);
		sitStairsSpecialEndSection.set(SIT_STAIRS_SPECIAL_END_SIGN_PATH, this.sitStairsSpecialEndSign());
		sitStairsSpecialEndSection.set(SIT_STAIRS_SPECIAL_END_CORNER_STAIRS_PATH, this.sitStairsSpecialEndCornerStairs());

		sitStairsSection.set(SIT_STAIRS_HEIGHT, this.sitStairsHeight());

		final ConfigurationSection sitAdditionalBlocksSection = sitSection.createSection(SIT_ADDITIONAL_BLOCKS_PATH);
		for (final Entry<Material, Double> entry : this.sitAdditionalBlocks().entrySet()) {
			sitAdditionalBlocksSection.set(entry.getKey().toString(), entry.getValue());
		}

		final ConfigurationSection msgSection = config.createSection(MSG_SECTION_PATH);
		msgSection.set(MSG_ENABLED_PATH, this.msgEnabled());

		final ConfigurationSection msgSitSection = msgSection.createSection(MSG_SIT_SECTION_PATH);
		msgSitSection.set(MSG_SIT_ENTER_PATH, this.msgSitEnter());
		msgSitSection.set(MSG_SIT_LEAVE_PATH, this.msgSitLeave());
		msgSitSection.set(MSG_SIT_ENABLED_PATH, this.msgSitEnabled());
		msgSitSection.set(MSG_SIT_DISABLED_PATH, this.msgSitDisabled());

		try {
			config.save(file);
		} catch (final IOException e) {
			this.plugin.getSLF4JLogger().warn("Failed to save config to file.");
		}
	}

	public Set<String> sitDisabledWorlds() {
		return this.sitDisabledWorlds;
	}

	public Map<Material, Double> sitAdditionalBlocks() {
		return this.sitAdditionalBlocks;
	}

	public boolean sitRequireEmptyHand() {
		return this.sitRequireEmptyHand;
	}

	public double sitMaxDistance() {
		return this.sitMaxDistance;
	}

	public double sitMaxDistanceSquared() {
		return Math.pow(this.sitMaxDistance, 2);
	}

	public boolean sitStairsEnabled() {
		return this.sitStairsEnabled;
	}

	public boolean sitStairsRotatePlayer() {
		return this.sitStairsRotatePlayer;
	}

	public int sitStairsMaxWidth() {
		return this.sitStairsMaxWidth;
	}

	public boolean sitStairsSpecialEndEnabled() {
		return this.sitStairsSpecialEndEnabled;
	}

	public boolean sitStairsSpecialEndSign() {
		return this.sitStairsSpecialEndSign;
	}

	public boolean sitStairsSpecialEndCornerStairs() {
		return this.sitStairsSpecialEndCornerStairs;
	}

	public double sitStairsHeight() {
		return this.sitStairsHeight;
	}

	public boolean msgEnabled() {
		return this.msgEnabled;
	}

	public String msgSitEnter() {
		return this.msgSitEnter;
	}

	public String msgSitLeave() {
		return this.msgSitLeave;
	}

	public String msgSitDisabled() {
		return this.msgSitDisabled;
	}

	public String msgSitEnabled() {
		return this.msgSitEnabled;
	}

}
