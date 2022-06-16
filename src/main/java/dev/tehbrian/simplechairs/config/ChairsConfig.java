package dev.tehbrian.simplechairs.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import dev.tehbrian.simplechairs.SimpleChairs;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ChairsConfig {

    protected final SimpleChairs plugin;

    public ChairsConfig(final SimpleChairs plugin) {
        this.plugin = plugin;
    }

    protected static final String sitConfigSectionPath = "sit-config";
    protected static final String sitConfigDisabledWorldsPath = "disabled-worlds";
    protected static final String sitConfigMaxDistancePath = "max-distance";
    protected static final String sitConfigRequireEmptyHandPath = "require-empty-hand";
    protected static final String sitConfigChairEntityType = "chair-entity-type";
    protected static final String sitConfigArrowResitInterval = "arrow-resit-interval";

    protected static final String sitConfigStairsSectionPath = "stairs";
    protected static final String sitConfigStairsEnabledPath = "enabled";
    protected static final String sitConfigStairsRotatePath = "rotate";
    protected static final String sitConfigStairsMaxWidthPath = "max-width";
    protected static final String sitConfigStairsSpecialEndPath = "special-end";
    protected static final String sitConfigStairsSpecialEndSignPath = "sign";
    protected static final String sitConfigStairsSpecialEndCornerStairsPath = "corner-stairs";
    protected static final String sitConfigStairsHeight = "height";

    protected static final String sitConfigAdditionalChairsPath = "additional-blocks";

    protected static final String sitEffectsSectionPath = "sit-effects";

    protected static final String sitEffectsHealingSectionPath = "healing";
    protected static final String sitEffectsHealingEnabledPath = "enabled";
    protected static final String sitEffectsHealingMaxPercentPath = "max-percent";
    protected static final String sitEffectsHealingIntervalPath = "interval";
    protected static final String sitEffectsHealingAmountPath = "amount";

    protected static final String sitEffectsItempickupPath = "itempickup";
    protected static final String sitEffectsItempickupEnabledPath = "enabled";

    protected static final String sitRestrictionsSectionPath = "sit-restrictions";
    protected static final String sitRestricitonsCommandsSectionPath = "commands";
    protected static final String sitRestrictionsCommandsBlockAllPath = "all";
    protected static final String sitRestrictionsCommandsBlockListPath = "list";

    protected static final String msgSectionPath = "messages";
    protected static final String msgEnabledPath = "enabled";
    protected static final String msgSitSectionPath = "sit";
    protected static final String msgSitEnterPath = "enter";
    protected static final String msgSitLeavePath = "leave";
    protected static final String msgSitEnabledPath = "enabled";
    protected static final String msgSitDisabledPath = "disabled";
    protected static final String msgSitCommandRestrictedPath = "commandrestricted";


    public final Set<String> sitDisabledWorlds = new HashSet<>();
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

    public final Map<Material, Double> additionalChairs = new EnumMap<>(Material.class);

    public boolean effectsHealEnabled = false;
    public int effectsHealMaxHealth = 100;
    public int effectsHealInterval = 20;
    public int effectsHealHealthPerInterval = 1;
    public boolean effectsItemPickupEnabled = false;

    public boolean restrictionsDisableAllCommands = false;
    public final Set<String> restrictionsDisabledCommands = new HashSet<>();

    public boolean msgEnabled = true;
    public String msgSitEnter = "&7You are now sitting.";
    public String msgSitLeave = "&7You are no longer sitting.";
    public String msgSitDisabled = "&7You have disabled chairs for yourself!";
    public String msgSitEnabled = "&7You have enabled chairs for yourself!";
    public String msgSitCommandRestricted = "&7You can't issue this command while sitting";

    public void reloadConfig() {
        final File file = new File(this.plugin.getDataFolder(), "config.yml");

        {
            final FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            final ConfigurationSection sitConfigSection = config.getConfigurationSection(sitConfigSectionPath);
            if (sitConfigSection != null) {
                this.sitDisabledWorlds.clear();
                this.sitDisabledWorlds.addAll(sitConfigSection.getStringList(sitConfigDisabledWorldsPath));
                this.sitRequireEmptyHand = sitConfigSection.getBoolean(sitConfigRequireEmptyHandPath, this.sitRequireEmptyHand);
                this.sitMaxDistance = sitConfigSection.getDouble(sitConfigMaxDistancePath, this.sitMaxDistance);
                this.sitChairEntityType = ChairEntityType.fromString(sitConfigSection.getString(sitConfigChairEntityType, this.sitChairEntityType.name()));
                this.sitArrowResitInterval = sitConfigSection.getInt(sitConfigArrowResitInterval, this.sitArrowResitInterval);
                if (this.sitArrowResitInterval > 1000) {
                    this.sitArrowResitInterval = 1000;
                }

                final ConfigurationSection sitConfigStairsSection = sitConfigSection.getConfigurationSection(sitConfigStairsSectionPath);
                if (sitConfigStairsSection != null) {
                    this.stairsEnabled = sitConfigStairsSection.getBoolean(sitConfigStairsEnabledPath, this.stairsEnabled);
                    this.stairsAutoRotate = sitConfigStairsSection.getBoolean(sitConfigStairsRotatePath, this.stairsAutoRotate);
                    this.stairsMaxWidth = sitConfigStairsSection.getInt(sitConfigStairsMaxWidthPath, this.stairsMaxWidth);
                    final ConfigurationSection sitConfigStairsSpecialEndSection = sitConfigStairsSection.getConfigurationSection(sitConfigStairsSpecialEndPath);
                    if (sitConfigStairsSpecialEndSection != null) {
                        this.stairsSpecialEndSign = sitConfigStairsSpecialEndSection.getBoolean(sitConfigStairsSpecialEndSignPath,
                                this.stairsSpecialEndSign
                        );
                        this.stairsSpecialEndCornerStairs = sitConfigStairsSpecialEndSection.getBoolean(sitConfigStairsSpecialEndCornerStairsPath,
                                this.stairsSpecialEndCornerStairs
                        );
                        this.stairsSpecialEndEnabled = this.stairsSpecialEndSign || this.stairsSpecialEndCornerStairs;
                    }
                    this.stairsHeight = sitConfigStairsSection.getDouble(sitConfigStairsHeight, this.stairsHeight);
                }

                final ConfigurationSection sitConfigAdditionalBlocksSection = sitConfigSection.getConfigurationSection(sitConfigAdditionalChairsPath);
                if (sitConfigAdditionalBlocksSection != null) {
                    for (final String materialName : sitConfigAdditionalBlocksSection.getKeys(false)) {
                        final Material material = Material.getMaterial(materialName);
                        if (material != null) {
                            this.additionalChairs.put(material, sitConfigAdditionalBlocksSection.getDouble(materialName));
                        }
                    }
                }
            }

            final ConfigurationSection sitEffectsSection = config.getConfigurationSection(sitEffectsSectionPath);
            if (sitEffectsSection != null) {
                final ConfigurationSection sitEffectsHealSection = sitEffectsSection.getConfigurationSection(sitEffectsHealingSectionPath);
                if (sitEffectsHealSection != null) {
                    this.effectsHealEnabled = sitEffectsHealSection.getBoolean(sitEffectsHealingEnabledPath, this.effectsHealEnabled);
                    this.effectsHealMaxHealth = sitEffectsHealSection.getInt(sitEffectsHealingMaxPercentPath, this.effectsHealMaxHealth);
                    this.effectsHealInterval = sitEffectsHealSection.getInt(sitEffectsHealingIntervalPath, this.effectsHealInterval);
                    this.effectsHealHealthPerInterval = sitEffectsHealSection.getInt(sitEffectsHealingAmountPath,
                            this.effectsHealHealthPerInterval
                    );
                }

                final ConfigurationSection sitEffectsItempickupSection = sitEffectsSection.getConfigurationSection(sitEffectsItempickupPath);
                if (sitEffectsItempickupSection != null) {
                    this.effectsItemPickupEnabled = sitEffectsItempickupSection.getBoolean(sitEffectsItempickupEnabledPath,
                            this.effectsItemPickupEnabled
                    );
                }
            }

            final ConfigurationSection sitRestirctionsSection = config.getConfigurationSection(sitRestrictionsSectionPath);
            if (sitRestirctionsSection != null) {
                final ConfigurationSection sitRestrictionsCommandsSection = sitRestirctionsSection.getConfigurationSection(sitRestricitonsCommandsSectionPath);
                if (sitRestrictionsCommandsSection != null) {
                    this.restrictionsDisableAllCommands = sitRestrictionsCommandsSection.getBoolean(sitRestrictionsCommandsBlockAllPath,
                            this.restrictionsDisableAllCommands
                    );
                    this.restrictionsDisabledCommands.clear();
                    this.restrictionsDisabledCommands.addAll(sitRestrictionsCommandsSection.getStringList(sitRestrictionsCommandsBlockListPath));
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
                    this.msgSitCommandRestricted = msgSitSection.getString(msgSitCommandRestrictedPath, this.msgSitCommandRestricted);
                }
            }
        }

        {
            final FileConfiguration config = new YamlConfiguration();

            final ConfigurationSection sitConfigSection = config.createSection(sitConfigSectionPath);
            {
                sitConfigSection.set(sitConfigDisabledWorldsPath, new ArrayList<>(this.sitDisabledWorlds));
                sitConfigSection.set(sitConfigRequireEmptyHandPath, this.sitRequireEmptyHand);
                sitConfigSection.set(sitConfigMaxDistancePath, this.sitMaxDistance);
                sitConfigSection.set(sitConfigChairEntityType, this.sitChairEntityType.name());
                sitConfigSection.set(sitConfigArrowResitInterval, this.sitArrowResitInterval);

                final ConfigurationSection sitConfigStairsSection = sitConfigSection.createSection(sitConfigStairsSectionPath);
                {
                    sitConfigStairsSection.set(sitConfigStairsEnabledPath, this.stairsEnabled);
                    sitConfigStairsSection.set(sitConfigStairsRotatePath, this.stairsAutoRotate);
                    sitConfigStairsSection.set(sitConfigStairsMaxWidthPath, this.stairsMaxWidth);
                    final ConfigurationSection sitConfigStairsSpecialEndSection = sitConfigStairsSection.createSection(sitConfigStairsSpecialEndPath);
                    {
                        sitConfigStairsSpecialEndSection.set(sitConfigStairsSpecialEndSignPath, this.stairsSpecialEndSign);
                        sitConfigStairsSpecialEndSection.set(sitConfigStairsSpecialEndCornerStairsPath, this.stairsSpecialEndCornerStairs);
                    }
                    sitConfigStairsSection.set(sitConfigStairsHeight, this.stairsHeight);
                }

                final ConfigurationSection sitConfigAdditionalBlocksSection = sitConfigSection.createSection(sitConfigAdditionalChairsPath);
                {
                    for (final Entry<Material, Double> entry : this.additionalChairs.entrySet()) {
                        sitConfigAdditionalBlocksSection.set(entry.getKey().toString(), entry.getValue());
                    }
                }
            }

            final ConfigurationSection sitEffectsSection = config.createSection(sitEffectsSectionPath);
            {
                final ConfigurationSection sitEffectsHealSection = sitEffectsSection.createSection(sitEffectsHealingSectionPath);
                {
                    sitEffectsHealSection.set(sitEffectsHealingEnabledPath, this.effectsHealEnabled);
                    sitEffectsHealSection.set(sitEffectsHealingMaxPercentPath, this.effectsHealMaxHealth);
                    sitEffectsHealSection.set(sitEffectsHealingIntervalPath, this.effectsHealInterval);
                    sitEffectsHealSection.set(sitEffectsHealingAmountPath, this.effectsHealHealthPerInterval);
                }

                final ConfigurationSection sitEffectsItempickupSection = sitEffectsSection.createSection(sitEffectsItempickupPath);
                {
                    sitEffectsItempickupSection.set(sitEffectsItempickupEnabledPath, this.effectsItemPickupEnabled);
                }
            }

            final ConfigurationSection sitRestirctionsSection = config.createSection(sitRestrictionsSectionPath);
            {
                final ConfigurationSection sitRestrictionsCommandsSection = sitRestirctionsSection.createSection(sitRestricitonsCommandsSectionPath);
                {
                    sitRestrictionsCommandsSection.set(sitRestrictionsCommandsBlockAllPath, this.restrictionsDisableAllCommands);
                    sitRestrictionsCommandsSection.set(sitRestrictionsCommandsBlockListPath, new ArrayList<>(this.restrictionsDisabledCommands));
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
                    msgSitSection.set(msgSitCommandRestrictedPath, this.msgSitCommandRestricted);
                }
            }

            try {config.save(file);} catch (final IOException e) {}
        }
    }

    public static enum ChairEntityType {
        ARROW, ARMOR_STAND;

        public static ChairEntityType fromString(final String string) {
            try {
                return ChairEntityType.valueOf(string);
            } catch (final IllegalArgumentException e) {
                return ChairEntityType.ARMOR_STAND;
            }
        }
    }

}
