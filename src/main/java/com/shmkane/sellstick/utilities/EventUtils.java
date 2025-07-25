package com.shmkane.sellstick.utilities;

import com.earth2me.essentials.IEssentials;
import com.shmkane.sellstick.configs.PriceConfig;
import com.shmkane.sellstick.configs.SellstickConfig;
import com.shmkane.sellstick.SellStick;
import net.brcdev.shopgui.ShopGuiPlusApi;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.UUID;

public class EventUtils {
    private static final HashMap<UUID, Boolean> playerPreferences = new HashMap<>();

    /**
     * Get the player's preference for receiving sell messages (true for chat, false for action bar)
     * @param playerUUID The players UUID.
     * @return true if the player wants chat, false if action bar
     */
    public static boolean getPlayerPreference(UUID playerUUID) {
        return playerPreferences.getOrDefault(playerUUID, true); // Default to true (chat)
    }

    /**
     * Toggle the player's preference for receiving sell messages
     * @param playerUUID The players UUID.
     */
    public static void togglePlayerPreference(UUID playerUUID) {
        boolean currentPreference = getPlayerPreference(playerUUID);
        playerPreferences.put(playerUUID, !currentPreference);
    }

    /**
     * Calculates the total worth of items inside a container using various pricing sources,
     * removes those items from the container, and returns the calculated total value.
     *
     * @param container The container whose contents will be evaluated for their worth.
     * @return The total value of all sellable items in the container.
     */
    public static double getContainerWorth(Container container, boolean removeItems) {

        ItemStack[] containerContents = container.getInventory().getContents();

        double total = 0;

        for (ItemStack itemstack : containerContents) {
            if (itemstack == null) continue;
            if (itemstack.getItemMeta().hasDisplayName()) continue;

            double price = 0;

            switch (SellstickConfig.priceSource) {
                case PRICESYML:

                    // Get price from config
                    Float configPrice = PriceConfig.prices.get(itemstack.getType());
                    if (configPrice == null || configPrice <= 0f) continue;
                    price = configPrice.doubleValue();

                    break;
                case SHOPGUI:

                    price = ShopGuiPlusApi.getItemStackPriceSell(itemstack);
                    if (price <= 0d) continue;

                    break;
                case ESSWORTH:

                    IEssentials ess = SellStick.getInstance().getEssentials();
                    BigDecimal essPrice = ess.getWorth().getPrice(ess, itemstack);

                    if (essPrice == null || essPrice.doubleValue() <= 0d) continue;

                    price = essPrice.doubleValue();

                    break;
            }

            int amount = itemstack.getAmount();

            // ShopGUI includes amount in price already
            if (SellstickConfig.priceSource == SellstickConfig.PriceSource.SHOPGUI) { amount = 1; }

            if (removeItems) container.getInventory().remove(itemstack);

            total += price * amount;
        }
        return total;
    }

    /**
     * Get the multiplier from the players permission node sellstick.mutiplier.x.
     * If the player does not have a multiplier, defaults to 1.
     *
     * @param player The player to check the multiplier for.
     * @return The multiplier as a double.
     */
    public static double getPlayersMultiplier(Player player) {
        double multiplier = 1d;

        for (PermissionAttachmentInfo perm : player.getEffectivePermissions()) {
            if (perm.getPermission().startsWith("sellstick.multiplier")) {
                String stringPerm = perm.getPermission();
                String permSection = stringPerm.replaceAll("sellstick.multiplier.", "");
                if (Double.parseDouble(permSection) > multiplier) {
                    multiplier = Double.parseDouble(permSection);
                }
            }
        }
        return multiplier;
    }
}
