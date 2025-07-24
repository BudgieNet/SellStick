package com.shmkane.sellstick.commands;

import com.shmkane.sellstick.SellStick;
import com.shmkane.sellstick.configs.SellstickConfig;
import com.shmkane.sellstick.utilities.ChatUtils;
import com.shmkane.sellstick.utilities.CommandUtils;
import com.shmkane.sellstick.utilities.ConvertUtils;
import com.shmkane.sellstick.utilities.ItemUtils;
import com.shmkane.sellstick.utilities.EventUtils;
import com.shmkane.sellstick.utilities.MergeUtils;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.logging.Level;

public class AdminCommands {

    public void convert(CommandSender sender, CommandArguments args) {
        final Player target = (Player) args.get("target");

        if (target == null) return;

        ConvertUtils.convertSellStick(target);
    }

    public void reload(CommandSender sender, CommandArguments arguments) {
        try {
            SellStick.getInstance().reload();
        } catch (Exception ex) {
            ChatUtils.sendMsg(sender, "<red>Something went wrong! Check console for errors!", true);
            ChatUtils.log(Level.SEVERE, ex.getMessage());
        }
    }

    public void toggle(CommandSender sender, CommandArguments arguments) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();
        EventUtils.togglePlayerPreference(playerUUID);

        boolean newPreference = EventUtils.getPlayerPreference(playerUUID);
        String message = newPreference ? "Sell messages will now be sent in chat."
                : "Sell messages will now be sent in the action bar.";
        ChatUtils.sendMsg(player, message, true);

    }

    public void give(CommandSender sender, CommandArguments args) {
        final Player target = (Player) args.get("target");
        final Integer numSticks = (Integer) args.get("amount");
        final String usesArg = (String) args.get("uses");

        if (target == null || numSticks == null || usesArg == null) return;

        final int uses = Integer.parseInt(usesArg);

        // Give sell sticks
        for (int i = 0; i < numSticks; i++) {
            // TODO: Check if inventory is full or has enough slots??
            CommandUtils.giveSellStick(target, uses);
        }

        ChatUtils.sendMsg(target, SellstickConfig.receiveMessage
                .replace("%player%", target.getName())
                .replace("%amount%", numSticks.toString()), true);
        ChatUtils.sendMsg(sender, SellstickConfig.giveMessage
                .replace("%player%", target.getName())
                .replace("%amount%", numSticks.toString()), true);
    }

    public void merge(CommandSender sender, CommandArguments args) {
        final Player player = (Player) args.get("target");

        if (player == null) return;

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
        ItemStack[] sortedSellsticks = MergeUtils.sortSellsticksByUses(sellsticks);

        // Sum the uses of all sellsticks
        int usesSum = MergeUtils.sumSellStickUses(sortedSellsticks, maxAmount);

        // Check if sellsticks exceed max cap.
        int totalUsesBeforeMerge = 0;
        for (ItemStack sellstick : sortedSellsticks) {
            totalUsesBeforeMerge += ItemUtils.getUses(sellstick);
        }

        if (totalUsesBeforeMerge == usesSum) {
            // Remove all sellsticks from player inventory
            MergeUtils.removeSortedSellsticks(player, sortedSellsticks, maxAmount);

            // Give a new sellstick with a number of uses equalling usesSum
            CommandUtils.giveSellStick(player, usesSum);

            ChatUtils.sendMsg(player, "<green>All sellsticks merged successfully!", true);

        } else {
            ChatUtils.sendMsg(player, "<red>Sellsticks exceed the maximum allowed merged uses.", true);
        }
    }
}