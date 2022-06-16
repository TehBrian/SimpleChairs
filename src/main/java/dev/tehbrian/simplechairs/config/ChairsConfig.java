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

    private static final String sitSectionPath = "sit-config";
    private static final String sitDisabledWorldsPath = "disabled-worlds";
    private static final String sitMaxDistancePath = "max-distance";
    private static final String sitRequireEmptyHandPath = "require-empty-hand";
    private static final String sitChairEntityTypePath = "chair-entity-type";
    private static final String sitArrowResitIntervalPath = "arrow-resit-interval";
    private static final String sitStairsSectionPath = "stairs";
    private static final String sitStairsEnabledPath = "enabled";
    private static final String sitStairsRotatePath = "rotate";
    private static final String sitStairsMaxWidthPath = "max-width";
    private static final String sitStairsSpecialEndPath = "special-end";
    private static final String sitStairsSpecialEndSignPath = "sign";
    private static final String sitStairsSpecialEndCornerStairsPath = "corner-stairs";
    private static final String sitStairsHeight = "height";
    private static final String sitAdditionalChairsPath = "additional-blocks";

    private static final String msgSectionPath = "messages";
    private static final String msgEnabledPath = "enabled";
    private static final String msgSitSectionPath = "sit";
    private static final String msgSitEnterPath = "enter";
    private static final String msgSitLeavePath = "leave";
    private static final String msgSitEnabledPath = "enabled";
    private static final String msgSitDisabledPath = "disabled";

    public final Set<String> sitDisabledWorlds = new HashSet<>();
    public final Map<Material, Double> additionalChairs = new EnumMap<>(Material.class);
    private final SimpleChairs plugin;
    public boolean sitRequireEmptyHand = false;
    public double sitMaxDistance = 2;
    public ChairEntityType sitChairEntityType = ChairEntityType.ARROW;
    public int sitArrowResitInterval = 1000;
    public boolean stairsEnabled = true;
    public boolean stairsAutoRotate = true;
    public int stairsMaxWidth = 16;
    public boolean stairsSpecialEndEnabled = false;
    public boolean stairsSpecialEndSign = true;
    public boolean stairsSpecialEndCornerStairs = true;
    public double stairsHeight = 0.5D;

    public boolean msgEnabled = true;
    public String msgSitEnter = "&7You are now sitting.";
    public String msgSitLeave = "&7You are no longer sitting.";
    public String msgSitDisabled = "&7You have disabled chairs for yourself!";
    public String msgSitEnabled = "&7You have enabled chairs for yourself!";
    public ChairsConfig(final SimpleChairs plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        final File file = new File(this.plugin.getDataFolder(), "config.yml");

        {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            final ConfigurationSection sitConfigSection = config.getConfigurationSection(sitSectionPath);
            if (sitConfigSection != null) {
                this.sitDisabledWorlds.clear();
                this.sitDisabledWorlds.addAll(sitConfigSection.getStringList(sitDisabledWorldsPath));
                this.sitRequireEmptyHand = sitConfigSection.getBoolean(sitRequireEmptyHandPath, this.sitRequireEmptyHand);
                this.sitMaxDistance = sitConfigSection.getDouble(sitMaxDistancePath, this.sitMaxDistance);
                this.sitChairEntityType = ChairEntityType.fromString(sitConfigSection.getString(
                        sitChairEntityTypePath,
                        this.sitChairEntityType.name()
                ));
                this.sitArrowResitInterval = sitConfigSection.getInt(sitArrowResitIntervalPath, this.sitArrowResitInterval);
                if (this.sitArrowResitInterval > 1000) {
                    this.sitArrowResitInterval = 1000;
                }

                final ConfigurationSection sitConfigStairsSection = sitConfigSection.getConfigurationSection(sitStairsSectionPath);
                if (sitConfigStairsSection != null) {
                    this.stairsEnabled = sitConfigStairsSection.getBoolean(sitStairsEnabledPath, this.stairsEnabled);
                    this.stairsAutoRotate = sitConfigStairsSection.getBoolean(sitStairsRotatePath, this.stairsAutoRotate);
                    this.stairsMaxWidth = sitConfigStairsSection.getInt(sitStairsMaxWidthPath, this.stairsMaxWidth);
                    final ConfigurationSection sitConfigStairsSpecialEndSection = sitConfigStairsSection.getConfigurationSection(
                            sitStairsSpecialEndPath);
                    if (sitConfigStairsSpecialEndSection != null) {
                        this.stairsSpecialEndSign = sitConfigStairsSpecialEndSection.getBoolean(
                                sitStairsSpecialEndSignPath,
                                this.stairsSpecialEndSign
                        );
                        this.stairsSpecialEndCornerStairs = sitConfigStairsSpecialEndSection.getBoolean(
                                sitStairsSpecialEndCornerStairsPath,
                                this.stairsSpecialEndCornerStairs
                        );
                        this.stairsSpecialEndEnabled = this.stairsSpecialEndSign || this.stairsSpecialEndCornerStairs;
                    }
                    this.stairsHeight = sitConfigStairsSection.getDouble(sitStairsHeight, this.stairsHeight);
                }

                final ConfigurationSection sitConfigAdditionalBlocksSection = sitConfigSection.getConfigurationSection(
                        sitAdditionalChairsPath);
                if (sitConfigAdditionalBlocksSection != null) {
                    for (final String materialName : sitConfigAdditionalBlocksSection.getKeys(false)) {
                        final Material material = Material.getMaterial(materialName);
                        if (material != null) {
                            this.additionalChairs.put(material, sitConfigAdditionalBlocksSection.getDouble(materialName));
                        }
                    }
                }
            }

            final ConfigurationSection msgSection = config.getConfigurationSection(msgSectionPath);
            if (msgSection != null) {
                this.msgEnabled = msgSection.getBoolean(msgEnabledPath, this.msgEnabled);
                final ConfigurationSection msgSitSection = msgSection.getConfigurationSection(msgSitSectionPath);
                if (msgSitSection != null) {
                    this.msgSitEnter = msgSitSection.getString(msgSitEnterPath, this.msgSitEnter);
                    this.msgSitLeave = msgSitSection.getString(msgSitLeavePath, this.msgSitLeave);
                    this.msgSitEnabled = msgSitSection.getString(msgSitEnabledPath, this.msgSitEnabled);
                    this.msgSitDisabled = msgSitSection.getString(msgSitDisabledPath, this.msgSitDisabled);
                }
            }
        }

        {
            final FileConfiguration config = new YamlConfiguration();

            final ConfigurationSection sitConfigSection = config.createSection(sitSectionPath);
            {
                sitConfigSection.set(sitDisabledWorldsPath, new ArrayList<>(this.sitDisabledWorlds));
                sitConfigSection.set(sitRequireEmptyHandPath, this.sitRequireEmptyHand);
                sitConfigSection.set(sitMaxDistancePath, this.sitMaxDistance);
                sitConfigSection.set(sitChairEntityTypePath, this.sitChairEntityType.name());
                sitConfigSection.set(sitArrowResitIntervalPath, this.sitArrowResitInterval);

                final ConfigurationSection sitConfigStairsSection = sitConfigSection.createSection(sitStairsSectionPath);
                {
                    sitConfigStairsSection.set(sitStairsEnabledPath, this.stairsEnabled);
                    sitConfigStairsSection.set(sitStairsRotatePath, this.stairsAutoRotate);
                    sitConfigStairsSection.set(sitStairsMaxWidthPath, this.stairsMaxWidth);
                    final ConfigurationSection sitConfigStairsSpecialEndSection = sitConfigStairsSection.createSection(
                            sitStairsSpecialEndPath);
                    {
                        sitConfigStairsSpecialEndSection.set(sitStairsSpecialEndSignPath, this.stairsSpecialEndSign);
                        sitConfigStairsSpecialEndSection.set(sitStairsSpecialEndCornerStairsPath, this.stairsSpecialEndCornerStairs);
                    }
                    sitConfigStairsSection.set(sitStairsHeight, this.stairsHeight);
                }

                final ConfigurationSection sitConfigAdditionalBlocksSection = sitConfigSection.createSection(sitAdditionalChairsPath);
                {
                    for (final Entry<Material, Double> entry : this.additionalChairs.entrySet()) {
                        sitConfigAdditionalBlocksSection.set(entry.getKey().toString(), entry.getValue());
                    }
                }
            }

            final ConfigurationSection msgSection = config.createSection(msgSectionPath);
            {
                msgSection.set(msgEnabledPath, this.msgEnabled);
                final ConfigurationSection msgSitSection = msgSection.createSection(msgSitSectionPath);
                {
                    msgSitSection.set(msgSitEnterPath, this.msgSitEnter);
                    msgSitSection.set(msgSitLeavePath, this.msgSitLeave);
                    msgSitSection.set(msgSitEnabledPath, this.msgSitEnabled);
                    msgSitSection.set(msgSitDisabledPath, this.msgSitDisabled);
                }
            }

            try {
                config.save(file);
            } catch (final IOException e) {
            }
        }
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
