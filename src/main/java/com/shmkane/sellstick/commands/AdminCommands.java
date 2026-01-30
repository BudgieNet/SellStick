package com.shmkane.sellstick.commands;

import com.shmkane.sellstick.SellStick;
import com.shmkane.sellstick.configs.SellstickConfig;
import com.shmkane.sellstick.utilities.ChatUtils;
import com.shmkane.sellstick.stick.StickHandler;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AdminCommands {

    /**
     * Converts an old SellStick held by a specified target player into the newer format.
     */
    public void convert(CommandSender sender, CommandArguments args) {
        final Player target = (Player) args.get("target");

        if (target == null) return;

        // Try convert all items in inventory
        for (ItemStack item : target.getInventory().getStorageContents()) {
            if (item == null || item.isEmpty()) continue;

            ItemStack newItem = StickHandler.convertOldSellStick(item);

            if (newItem == null) continue;

            target.getInventory().remove(item);
            target.getInventory().addItem(newItem);
        }
    }

    /**
     * Reloads the SellStick plugin, reloading its configurations and variables.
     */
    public void reloadSellStick(CommandSender sender, CommandArguments arguments) {
        try {
            ChatUtils.sendMsg(sender, "<green>Plugin reloading...");
            SellStick.getInstance().loadSellStick();
            ChatUtils.sendMsg(sender, "<green>Plugin reloaded!");
        } catch (Exception ex) {
            ChatUtils.sendMsg(sender, "<red>Something went wrong! Check console for errors!");
            ChatUtils.error(ex.getMessage());
        }
    }

    /**
     * Provides the ability to give a player a specified number of "sell sticks" with a defined number of uses.
     */
    public void give(CommandSender sender, CommandArguments args) {
        // Get target player
        final Player target = (Player) args.get("target");
        if (target == null) return;

        // Get string amount and convert if necessary
        String usesArg = (String) args.get("uses");
        if (usesArg == null || usesArg.equals("i") || usesArg.equals("infinite")) usesArg = "Infinite";

        // Argument "Infinite" will equal max integer
        try {
            final int uses = usesArg.equals("Infinite") ? Integer.MAX_VALUE : Integer.parseInt(usesArg);
            final int amount = args.get("amount") == null ? 1 : Integer.parseInt(args.get("amount").toString());

            // Give sell sticks
            for (int i = 0; i < amount; i++) {
                StickHandler.giveSellStickToPlayer(target, uses);
            }

            ChatUtils.sendMsg(target, SellstickConfig.receiveMessage
                    .replace("%player%", target.getName())
                    .replace("%amount%", Integer.toString(amount)));
            ChatUtils.sendMsg(sender, SellstickConfig.giveMessage
                    .replace("%player%", target.getName())
                    .replace("%amount%", Integer.toString(amount)));

        } catch (NumberFormatException e) {
            ChatUtils.sendMsg(sender, "<red>Invalid amount!");
        }
    }
}