package com.shmkane.sellstick.commands;

import com.shmkane.sellstick.SellStick;
import com.shmkane.sellstick.configs.SellstickConfig;
import com.shmkane.sellstick.utilities.ChatUtils;
import com.shmkane.sellstick.stick.StickHandler;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Level;

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
    public void reload(CommandSender sender, CommandArguments arguments) {
        try {
            SellStick.getInstance().reload();
            ChatUtils.sendMsg(sender, "<green>SellStick plugin reloaded successfully!");
        } catch (Exception ex) {
            ChatUtils.sendMsg(sender, "<red>Something went wrong! Check console for errors!");
            ChatUtils.log(Level.SEVERE, ex.getMessage());
        }
    }

    /**
     * Provides the ability to give a player a specified number of "sell sticks" with a defined number of uses.
     */
    public void give(CommandSender sender, CommandArguments args) {
        final Player target = (Player) args.get("target");
        final Integer numSticks = (Integer) args.get("amount");
        Integer uses = (Integer) args.get("uses");

        if (target == null || numSticks == null || uses == null) return;

        // Give sell sticks
        for (int i = 0; i < numSticks; i++) {
            StickHandler.giveSellStickToPlayer(target, uses);
        }

        ChatUtils.sendMsg(target, SellstickConfig.receiveMessage
                .replace("%player%", target.getName())
                .replace("%amount%", numSticks.toString()));
        ChatUtils.sendMsg(sender, SellstickConfig.giveMessage
                .replace("%player%", target.getName())
                .replace("%amount%", numSticks.toString()));
    }
}