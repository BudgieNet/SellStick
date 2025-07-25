package com.shmkane.sellstick.commands;

import com.shmkane.sellstick.configs.SellstickConfig;
import com.shmkane.sellstick.stick.StickHandler;
import com.shmkane.sellstick.utilities.ChatUtils;
import com.shmkane.sellstick.utilities.EventUtils;
import com.shmkane.sellstick.utilities.MergeUtils;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerCommands {

    /**
     * Toggles the player's preference for receiving sell messages in chat or the action bar.
     * If the preference is enabled, messages will be sent in chat; otherwise, they will be sent in the action bar.
     */
    public void toggle(CommandSender sender, CommandArguments arguments) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        UUID playerUUID = player.getUniqueId();
        EventUtils.togglePlayerPreference(playerUUID);

        boolean newPreference = EventUtils.getPlayerPreference(playerUUID);
        String message = newPreference ? "Sell messages will now be sent in chat."
                : "Sell messages will now be sent in the action bar.";
        ChatUtils.sendMsg(player, message, true);

    }

    /**
     * Merges all sellsticks in the inventory of the command sender into a single sellstick, combining their
     * uses, provided that the total number of uses does not exceed the maximum allowed value.
     */
    public void merge(CommandSender sender, CommandArguments args) {

        if (!(sender instanceof Player player)) return;

        // Get max amount of uses for a new sellstick
        int maxAmount = SellstickConfig.maxAmount;

        // Get all sellsticks in player inventory
        ItemStack[] sellsticks = MergeUtils.searchInventory(player);

        // Check if player has any sellsticks
        if (sellsticks.length == 0) {
            ChatUtils.sendMsg(player, "<red>You have no sellsticks in your inventory!", true);
            return;
        }

        // Check if player has at least 2 sellsticks
        if (sellsticks.length == 1) {
            ChatUtils.sendMsg(player, "<yellow>You need at least 2 sellsticks to merge!", true);
            return;
        }

        // Sort sellsticks by their uses
        ItemStack[] sortedSellsticks = MergeUtils.sortSellSticksByUses(sellsticks);

        // Sum the uses of all sellsticks
        int usesSum = MergeUtils.sumSellStickUses(sortedSellsticks, maxAmount);

        // Check if sellsticks exceed max cap.
        int totalUsesBeforeMerge = 0;
        for (ItemStack sellstick : sortedSellsticks) {
            totalUsesBeforeMerge += StickHandler.getUses(sellstick);
        }

        if (totalUsesBeforeMerge == usesSum) {
            // Remove all sellsticks from player inventory
            MergeUtils.removeSortedSellSticks(player, sortedSellsticks, maxAmount);

            // Give a new sellstick with a number of uses equalling usesSum
            StickHandler.giveSellStickToPlayer(player, usesSum);

            ChatUtils.sendMsg(player, "<green>All sellsticks merged successfully!", true);

        } else {
            ChatUtils.sendMsg(player, "<red>Sellsticks exceed the maximum allowed merged uses.", true);
        }
    }
}
