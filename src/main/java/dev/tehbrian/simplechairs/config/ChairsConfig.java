package dev.tehbrian.simplechairs.config;

import dev.tehbrian.simplechairs.SimpleChairs;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

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
    private static final String SIT_CHAIR_ENTITY_TYPE_PATH = "chair-entity-type";
    private static final String SIT_ARROW_RESIT_INTERVAL_PATH = "arrow-resit-interval";
    private static final String SIT_STAIRS_SECTION_PATH = "stairs";
    private static final String SIT_STAIRS_ENABLED_PATH = "enabled";
    private static final String SIT_STAIRS_ROTATE_PATH = "rotate";
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

    private final SimpleChairs plugin;

    private final Set<String> sitDisabledWorlds = new HashSet<>();
    private final Map<Material, Double> sitAdditionalBlocks = new EnumMap<>(Material.class);
    private boolean sitRequireEmptyHand = false;
    private double sitMaxDistance = 2;
    private ChairEntityType sitChairEntityType = ChairEntityType.ARROW;
    private int sitArrowResitInterval = 1000;
    private boolean sitStairsEnabled = true;
    private boolean sitStairsAutoRotate = true;
    private int sitStairsMaxWidth = 16;
    private boolean sitStairsSpecialEndEnabled = false;
    private boolean sitStairsSpecialEndSign = true;
    private boolean sitStairsSpecialEndCornerStairs = true;
    private double sitStairsHeight = 0.5D;

    private boolean msgEnabled = true;
    private String msgSitEnter = "&7You are now sitting.";
    private String msgSitLeave = "&7You are no longer sitting.";
    private String msgSitDisabled = "&7You have disabled chairs for yourself!";
    private String msgSitEnabled = "&7You have enabled chairs for yourself!";

    public ChairsConfig(final SimpleChairs plugin) {
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

        {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            final ConfigurationSection sitConfigSection = config.getConfigurationSection(SIT_SECTION_PATH);
            if (sitConfigSection != null) {
                this.sitDisabledWorlds().clear();
                this.sitDisabledWorlds().addAll(sitConfigSection.getStringList(SIT_DISABLED_WORLDS_PATH));
                this.sitRequireEmptyHand = sitConfigSection.getBoolean(SIT_REQUIRE_EMPTY_HAND_PATH, this.sitRequireEmptyHand());
                this.sitMaxDistance = sitConfigSection.getDouble(SIT_MAX_DISTANCE_PATH, this.sitMaxDistance());
                this.sitChairEntityType = ChairEntityType.fromString(sitConfigSection.getString(
                        SIT_CHAIR_ENTITY_TYPE_PATH,
                        this.sitChairEntityType().name()
                ));
                this.sitArrowResitInterval = sitConfigSection.getInt(SIT_ARROW_RESIT_INTERVAL_PATH, this.sitArrowResitInterval());
                if (this.sitArrowResitInterval() > 1000) {
                    this.sitArrowResitInterval = 1000;
                }

                final ConfigurationSection sitConfigStairsSection = sitConfigSection.getConfigurationSection(SIT_STAIRS_SECTION_PATH);
                if (sitConfigStairsSection != null) {
                    this.sitStairsEnabled = sitConfigStairsSection.getBoolean(SIT_STAIRS_ENABLED_PATH, this.sitStairsEnabled());
                    this.sitStairsAutoRotate = sitConfigStairsSection.getBoolean(SIT_STAIRS_ROTATE_PATH, this.sitStairsAutoRotate());
                    this.sitStairsMaxWidth = sitConfigStairsSection.getInt(SIT_STAIRS_MAX_WIDTH_PATH, this.sitStairsMaxWidth());
                    final ConfigurationSection sitConfigStairsSpecialEndSection = sitConfigStairsSection.getConfigurationSection(
                            SIT_STAIRS_SPECIAL_END_PATH);
                    if (sitConfigStairsSpecialEndSection != null) {
                        this.sitStairsSpecialEndSign = sitConfigStairsSpecialEndSection.getBoolean(
                                SIT_STAIRS_SPECIAL_END_SIGN_PATH,
                                this.sitStairsSpecialEndSign()
                        );
                        this.sitStairsSpecialEndCornerStairs = sitConfigStairsSpecialEndSection.getBoolean(
                                SIT_STAIRS_SPECIAL_END_CORNER_STAIRS_PATH,
                                this.sitStairsSpecialEndCornerStairs()
                        );
                        this.sitStairsSpecialEndEnabled = this.sitStairsSpecialEndSign() || this.sitStairsSpecialEndCornerStairs();
                    }
                    this.sitStairsHeight = sitConfigStairsSection.getDouble(SIT_STAIRS_HEIGHT, this.sitStairsHeight());
                }

                final ConfigurationSection sitConfigAdditionalBlocksSection = sitConfigSection.getConfigurationSection(
                        SIT_ADDITIONAL_BLOCKS_PATH);
                if (sitConfigAdditionalBlocksSection != null) {
                    for (final String materialName : sitConfigAdditionalBlocksSection.getKeys(false)) {
                        final Material material = Material.getMaterial(materialName);
                        if (material != null) {
                            this.sitAdditionalBlocks().put(material, sitConfigAdditionalBlocksSection.getDouble(materialName));
                        }
                    }
                }
            }

            final ConfigurationSection msgSection = config.getConfigurationSection(MSG_SECTION_PATH);
            if (msgSection != null) {
                this.msgEnabled = msgSection.getBoolean(MSG_ENABLED_PATH, this.msgEnabled());
                final ConfigurationSection msgSitSection = msgSection.getConfigurationSection(MSG_SIT_SECTION_PATH);
                if (msgSitSection != null) {
                    this.msgSitEnter = msgSitSection.getString(MSG_SIT_ENTER_PATH, this.msgSitEnter());
                    this.msgSitLeave = msgSitSection.getString(MSG_SIT_LEAVE_PATH, this.msgSitLeave());
                    this.msgSitEnabled = msgSitSection.getString(MSG_SIT_ENABLED_PATH, this.msgSitEnabled());
                    this.msgSitDisabled = msgSitSection.getString(MSG_SIT_DISABLED_PATH, this.msgSitDisabled());
                }
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
        sitSection.set(SIT_CHAIR_ENTITY_TYPE_PATH, this.sitChairEntityType().name());
        sitSection.set(SIT_ARROW_RESIT_INTERVAL_PATH, this.sitArrowResitInterval());

        final ConfigurationSection sitStairsSection = sitSection.createSection(SIT_STAIRS_SECTION_PATH);
        sitStairsSection.set(SIT_STAIRS_ENABLED_PATH, this.sitStairsEnabled());
        sitStairsSection.set(SIT_STAIRS_ROTATE_PATH, this.sitStairsAutoRotate());
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

    public ChairEntityType sitChairEntityType() {
        return this.sitChairEntityType;
    }

    public int sitArrowResitInterval() {
        return this.sitArrowResitInterval;
    }

    public boolean sitStairsEnabled() {
        return this.sitStairsEnabled;
    }

    public boolean sitStairsAutoRotate() {
        return this.sitStairsAutoRotate;
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

    public enum ChairEntityType {
        ARROW,
        ARMOR_STAND;

        public static ChairEntityType fromString(final String string) {
            try {
                return ChairEntityType.valueOf(string);
            } catch (final IllegalArgumentException e) {
                return ChairEntityType.ARMOR_STAND;
            }
        }
    }

}
