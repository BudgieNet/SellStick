package com.shmkane.sellstick.events;

import com.shmkane.sellstick.SellStick;
import com.shmkane.sellstick.configs.SellstickConfig;
import com.shmkane.sellstick.stick.StickHandler;
import com.shmkane.sellstick.utilities.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

import static com.shmkane.sellstick.utilities.ChatUtils.log;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSellstickUse(PlayerInteractEvent event) {
        // Execute quick checks
        if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getClickedBlock() == null) return;
        if (event.getMaterial() != SellstickConfig.material) return;
        if (event.getItem() == null || !event.getItem().hasItemMeta()) return;

        // Possibly a sellstick - continue

        if (!(event.getClickedBlock().getState() instanceof Container container)) return;

        Player player = event.getPlayer();
        ItemStack sellStick = player.getInventory().getItemInMainHand();

        // Convert old sellstick
        ItemStack newStick = StickHandler.convertOldSellStick(sellStick);
        if (newStick != null) {
            player.getInventory().setItemInMainHand(newStick);
            ChatUtils.sendMsg(player, "<green>Your old sell stick has been updated.", true);
            event.setCancelled(true);
            return;
        }

        // Check if another plugin is cancelling the event
        if (event.useInteractedBlock() == Event.Result.DENY) {
            ChatUtils.sendMsg(player, SellstickConfig.territoryMessage, true);
            return;
        }

        event.setCancelled(true); // Cancel opening the chest - confirmed player is using a sellstick

        // Check permission
        if (!player.hasPermission("sellstick.use")) {
            ChatUtils.sendMsg(player, SellstickConfig.noPerm, true);
            return;
        }

        // Check if player is only holding 1 stick
        if (sellStick.getAmount() != 1) {
            ChatUtils.sendMsg(player, SellstickConfig.holdOneMessage, true);
            return;
        }

        // Player preference for sell message
        boolean sendInChat = EventUtils.getPlayerPreference(player.getUniqueId());

        // Nothing worth selling
        if (EventUtils.getContainerWorth(container, false) <= 0d) {
            if (sendInChat) {
                ChatUtils.sendMsg(player, SellstickConfig.nothingWorth);
            } else {
                ChatUtils.sendActionBar(player, SellstickConfig.nothingWorth);
            }
            return;
        }

        // Remove items
        double total = EventUtils.getContainerWorth(container, true);

        // Subtract use
        if (!StickHandler.isInfinite(sellStick)) StickHandler.subtractUses(sellStick);

        double multiplier = EventUtils.getPlayersMultiplier(player);
        Economy econ = SellStick.getInstance().getEcon();
        int uses = StickHandler.getUses(sellStick);

        // Add funds to player
        EconomyResponse response = econ.depositPlayer(player, total * multiplier);
        if (!response.transactionSuccess()) {
            ChatUtils.sendMsg(player, String.format("An error occurred: " + SellstickConfig.prefix, response.errorMessage), true);
            return;
        }

        // Send message to player
        String[] send = SellstickConfig.sellMessage.split("\\\\n");
        for (String msg : send) {
            if (sendInChat) {
                ChatUtils.sendMsg(player, msg
                        .replace("%uses%", String.valueOf(uses))
                        .replace("%balance%", econ.format(response.balance))
                        .replace("%price%", econ.format(response.amount)), true);
            } else {
                ChatUtils.sendActionBar(player, msg
                        .replace("%uses%", String.valueOf(uses))
                        .replace("%balance%", econ.format(response.balance))
                        .replace("%price%", econ.format(response.amount)));
            }
        }

        // Send to log file
        String coords = Math.round(player.getLocation().x()) + " " + Math.round(player.getLocation().y()) + " " + Math.round(player.getLocation().z());
        ChatUtils.writeLog(player.getUniqueId() + " sold $" + Math.round(response.amount) + " ($" + Math.round(response.balance) + ") at " + coords + " in " + player.getWorld().getName());

        // Play sound
        if (SellstickConfig.sound) {
            player.playSound(Objects.requireNonNull(event.getInteractionPoint()), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.5f);
        }
        // Particles
        if (SellstickConfig.particles) {
            player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, Objects.requireNonNull(event.getInteractionPoint()), 5, 0.2, 0.2, 0.2, 0);
        }
        // Remove broken stick
        if (uses <= 0) {
            player.getInventory().removeItem(sellStick);
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            ChatUtils.sendMsg(player, SellstickConfig.brokenStick, true);
        }
    }
}
