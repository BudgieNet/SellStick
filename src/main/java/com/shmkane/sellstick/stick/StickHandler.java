package com.shmkane.sellstick.stick;

import com.shmkane.sellstick.configs.SellstickConfig;
import com.shmkane.sellstick.utilities.ChatUtils;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StickHandler {

    /**
     * Creates a SellStick item with a specified number of uses.
     *
     * @param uses The number of uses the SellStick should have.
     * @return The created {@code ItemStack} representing a SellStick, or {@code null} if the item
     *         could not be created due to an invalid configuration.
     */
    @NotNull
    public static ItemStack createSellStick(int uses) {
        ItemStack itemStack;

        itemStack = new ItemStack(SellstickConfig.material);

        ItemMeta itemMeta = itemStack.getItemMeta();

        // Set display name
        itemMeta.displayName(SellstickConfig.displayName);

        // Set lore
        itemMeta.lore((uses == Integer.MAX_VALUE) ? SellstickConfig.loreInfinite : SellstickConfig.loreFinite);

        // Add glow if required
        if (SellstickConfig.glow) {
            itemStack.addUnsafeEnchantment(Enchantment.FORTUNE, 1);
            itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemMeta.setEnchantmentGlintOverride(true);
        }

        // Apply meta to item stack
        itemStack.setItemMeta(itemMeta);

        // Set NBT, uses and lore
        StickHandler.setUses(itemStack, uses);

        return itemStack;
    }

    /**
     * Gives the specified player a SellStick with a defined number of uses.
     * The SellStick will be added to the player's inventory or dropped at their location
     * if the inventory is full.
     *
     * @param target The player who will receive the SellStick.
     * @param uses   The number of uses the SellStick will have.
     */
    public static void giveSellStickToPlayer(Player target, int uses) {

        ItemStack sellStick = createSellStick(uses);

        // Check if target inventory is full
        if (target.getInventory().firstEmpty() == -1) {
            Location l = target.getLocation();
            // Drop item on player
            target.getWorld().dropItem(l, sellStick);
            ChatUtils.sendMsg(target, "&cInventory full! Sellstick was dropped on the ground!");

            String locationString = l.getWorld().getName() + " [" + Math.round(l.x()) + " "  + Math.round(l.y()) + " "  + Math.round(l.z()) + "]" ;
            ChatUtils.log("Gave " + target.displayName() + " a sellstick but inv was full. Item dropped at " + locationString);
        } else {
            // Add to inventory
            target.getInventory().addItem(sellStick);
        }
    }

    /**
     * Check if a Sellstick is infinite
     * @param itemStack The sellstick to check.
     * @return true if infinite, false if not.
     */
    public static boolean isInfinite(ItemStack itemStack) {

        ReadableNBT nbtItemStack = NBT.readNbt(itemStack);

        return nbtItemStack.getBoolean("Infinite");
    }

    /**
     * Retrieves the remaining uses of the given ItemStack from its NBT data.
     *
     * @param itemStack The ItemStack whose remaining uses will be retrieved.
     * @return The number of uses remaining for the ItemStack, as stored in its NBT data.
     */
    public static int getUses(ItemStack itemStack) {
        ReadableNBT nbtItemStack = NBT.readNbt(itemStack);
        return nbtItemStack.getInteger("UsesRemaining");
    }

    /**
     * Sets the remaining uses of the given ItemStack by updating its NBT data and lore.
     * When the uses are set to the maximum integer value, the item is considered infinite.
     * This method modifies the item's metadata and updates its lore accordingly.
     *
     * @param itemStack The ItemStack to modify by setting its remaining uses.
     * @param uses The number of remaining uses to set for the ItemStack. If this value is
     *             {@code Integer.MAX_VALUE}, the item will be marked as infinite.
     */
    public static void setUses(ItemStack itemStack, int uses) {
        // NBT
        NBT.modify(itemStack, nbt -> {
            nbt.setInteger("UsesRemaining", uses);
            nbt.setBoolean("Infinite", (uses == Integer.MAX_VALUE));
        });

        // Update lore
        updateRemainingLore(itemStack, uses);
    }

    /**
     * Decreases the remaining uses of the provided ItemStack by 1.
     * Updates the item's NBT data and lore accordingly to the new uses.
     *
     * @param itemStack The ItemStack whose remaining uses will be reduced.
     */
    public static void subtractUses(ItemStack itemStack) {

        // Skip if infinite
        if (isInfinite(itemStack)) return;

        // Subtract use
        int uses = getUses(itemStack) - 1;
        NBT.modify(itemStack, nbt -> {
            nbt.setInteger("UsesRemaining", uses);
        });

        // Update lore
        updateRemainingLore(itemStack, uses);
    }

    /**
     * Check if a valid sellstick based on if it has Uses and Infinite NBT tags.
     * @param itemStack The itemstack to check.
     * @return true if sellstick, false if not.
     */
    public static boolean hasSellStickNBT(ItemStack itemStack) {
        ReadableNBT nbtItemStack = NBT.readNbt(itemStack);
        return (nbtItemStack.hasTag("UsesRemaining") && nbtItemStack.hasTag("Infinite"));
    }

    /**
     * Check if a sellstick is old by:
     * - Must be a stick
     * - Must have an enchant
     * - Must contain sellstick in name
     * - Must not match current name
     * - Must not match current lore
     *
     * @param itemStack The ItemStack to be checked.
     * @return {@code true} if the provided ItemStack is an old SellStick; {@code false} otherwise.
     */
    public static boolean isOldSellStick(ItemStack itemStack) {
        if (itemStack == null) return false;
        // Must be stick
        if (itemStack.getType() != Material.STICK) return false;
        // Must have an enchant
        if (itemStack.getEnchantments().isEmpty()) return false;
        // Must have sellstick in the name
        if (!PlainTextComponentSerializer.plainText().serialize(itemStack.displayName()).toLowerCase().contains("sellstick")) return false;
        // Must not be current
        ChatUtils.log("Checking if " + (itemStack.displayName() == SellstickConfig.displayName));
        if (itemStack.displayName() == SellstickConfig.displayName) return false;
        if (itemStack.getItemMeta().lore() == SellstickConfig.loreFinite) return false;

        return true;
    }

    /**
     * Converts an old SellStick item into the current version if it matches the requirements
     * for being considered an old SellStick.
     *
     * @param itemStack The ItemStack to be converted. This should be validated before being
     *                  passed to ensure it is a potential old SellStick.
     * @return The converted SellStick ItemStack, or {@code null} if the provided ItemStack
     *         is not an old SellStick.
     */
    public static ItemStack convertOldSellStick(ItemStack itemStack) {
        if (!isOldSellStick(itemStack)) return null;
        List<Component> lore = itemStack.getItemMeta().lore();

        int uses = 0;
        int amount = itemStack.getAmount();

        for (Component line : lore) {
            String text = PlainTextComponentSerializer.plainText().serialize(line).toLowerCase();
            if (text.contains("infinite")) {
                uses = Integer.MAX_VALUE;
                break;
            }
            ChatUtils.log("Checking for uses: " + text);
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                ChatUtils.log("Found uses: " + matcher.group());
                uses = Integer.parseInt(matcher.group());
                break;
            }
        }

        ItemStack newStick = StickHandler.createSellStick(uses);
        newStick.setAmount(amount);
        return newStick;
    }

    /**
     * Used to update the remaining uses on the items lore most efficiently.
     * Must be an up-to-date config sellstick.
     * @param itemStack The itemstack to update the lore for.
     */
    public static void updateRemainingLore(ItemStack itemStack, int uses) {

        String newStrLine = MiniMessage.miniMessage().serialize(SellstickConfig.loreFinite.get(SellstickConfig.loreLine))
                .replace("%remaining%", String.valueOf(uses));
        Component newLine = MiniMessage.miniMessage().deserialize(newStrLine);

        ItemMeta itemMeta = itemStack.getItemMeta();
        List<Component> lore = itemMeta.lore();
        lore.set(SellstickConfig.loreLine, newLine);
        itemMeta.lore(lore);
        itemStack.setItemMeta(itemMeta);
    }

}
