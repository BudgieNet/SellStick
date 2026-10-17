package com.shmkane.sellstick.configs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.budgienet.chatutil.ChatUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

// Handles Prices.YML
public class PriceConfig extends Config {

    public PriceConfig(String configName, File dataFolder) {
        super(configName, dataFolder);
    }

    public static Map<Material, Float> prices = new HashMap<>();

    @Override
    void loadValues(FileConfiguration config) {

        ConfigurationSection pricesSection = config.getConfigurationSection("prices");

        if (pricesSection == null) {
            ChatUtil.log(Level.WARNING, "Prices section was not found in prices.yml.");
            return;
        }

        // Parse values into map
        for (Map.Entry<String, Object> entry : pricesSection.getValues(false).entrySet()) {
            Material material = Material.getMaterial(entry.getKey());
            if (material == null) {
                ChatUtil.log(Level.WARNING, "Material " + entry.getKey() + " was not found in prices.yml.");
                continue;
            }
            try {
                prices.put(material, Float.parseFloat(entry.getValue().toString()));
            } catch (NumberFormatException e) {
                ChatUtil.log(Level.WARNING, "Price for " + entry.getKey() + " was not a valid number in prices.yml.");
            }
        }
    }
}
