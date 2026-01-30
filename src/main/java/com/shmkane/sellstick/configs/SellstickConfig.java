package com.shmkane.sellstick.configs;

import com.shmkane.sellstick.SellStick;
import com.shmkane.sellstick.utilities.ChatUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

// Handles config.YML
public class SellstickConfig extends Config {

    public static String PriceInterface, receiveMessage, giveMessage, nonSellingRelated, brokenStick,
            nothingWorth, territoryMessage, noPerm, sellMessage, prefix, holdOneMessage;
    public static boolean sound, glow, particles;
    public static Material material;
    public static int maxAmount;
    public static PriceSource priceSource;
    public static List<Component> loreFinite = new ArrayList<>(), loreInfinite = new ArrayList<>();
    public static Component displayName;
    public static int loreLine;

    public SellstickConfig(String configName, File dataFolder) {
        super(configName, dataFolder);
    }

    // Load configuration values
    @Override
    void loadValues(FileConfiguration config) {
        // Price Interface Configuration
        PriceInterface = tryGetString(conf, "PriceSource", "PricesYML");
        priceSource = setPriceSource(PriceInterface);

        // Item Configuration
        material = tryGetMaterial(conf, "Item.Material", Material.STICK);
        glow = config.getBoolean("Item.Glow", true);
        maxAmount = config.getInt("Item.MaxAmount", 2000);
        sound = config.getBoolean("Item.UseSound", true);
        particles = config.getBoolean("Item.UseParticles", true);

        // Name / Lore
        displayName = MiniMessage.miniMessage().deserialize(tryGetString(conf, "Item.DisplayName", "<gold>SellStick"));
        // Infinite lore
        config.getStringList("Item.InfiniteLore").forEach(line -> loreInfinite.add(MiniMessage.miniMessage().deserialize(line)));
        // Finite lore
        List<String> finiteLore = config.getStringList("Item.FiniteLore");
        for (String line : finiteLore) { loreFinite.add(MiniMessage.miniMessage().deserialize(line)); }
        // Lore line
        for (int i = 0; i < loreFinite.size(); i++) {
            if (loreFinite.get(i).toString().contains("%remaining%")) loreLine = i;
        }

        // Messages
        holdOneMessage = tryGetString(conf, "Messages.OnlyHoldOne", "<red>Please use 1 sell stick at a time!");
        prefix = tryGetString(conf, "Messages.PluginPrefix", "<gold>[<yellow>SellStick<gold>] ");
        sellMessage = tryGetString(conf, "Messages.SellMessage",
                "<red>You sold items for %price% and now have %balance%");
        noPerm = tryGetString(conf, "Messages.NoPermissionMessage", "<red>Sorry, you don''t have permission for this!");
        territoryMessage = tryGetString(conf, "Messages.InvalidTerritoryMessage",
                "<red>You can''t use sell stick here!");
        nothingWorth = tryGetString(conf, "Messages.NotWorthMessage", "<red>Nothing worth selling inside");
        brokenStick = tryGetString(conf, "Messages.BrokenStick", "<red>Your sellstick broke! (Ran out of uses)");
        nonSellingRelated = tryGetString(conf, "Messages.NonSellingRelated",
                "<red>Oak''s words echoed... There''s a time and place for everything but not now! (Right click a chest!)");
        receiveMessage = tryGetString(conf, "Messages.ReceiveMessage", "<green>You gave %player% %amount% SellSticks!");
        giveMessage = tryGetString(conf, "Messages.GiveMessage", "<green>You''ve received %amount% SellSticks!");
    }

    private PriceSource setPriceSource(String priceString) {
        if (priceString != null) {
            if (priceString.equalsIgnoreCase("ShopGUI") && SellStick.getInstance().ShopGUIEnabled) {
                return PriceSource.SHOPGUI;
            }
            if (priceString.equalsIgnoreCase("Essentials") && SellStick.getInstance().EssentialsEnabled) {
                return PriceSource.ESSWORTH;
            }
            if (priceString.equalsIgnoreCase("PricesYML")) {
                return PriceSource.PRICESYML;
            }
        } else {
            ChatUtils.log(Level.WARNING, "PriceSource did not match any option. Defaulting to prices.yml.");
            return PriceSource.PRICESYML;
        }
        return PriceSource.PRICESYML;
    }

    public enum PriceSource {
        PRICESYML,
        ESSWORTH,
        SHOPGUI
    }

}
