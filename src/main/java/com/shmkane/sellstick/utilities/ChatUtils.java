package com.shmkane.sellstick.utilities;

import com.shmkane.sellstick.configs.SellstickConfig;
import com.shmkane.sellstick.SellStick;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;


public class ChatUtils {
    // Send player messages
    public static void sendMsg(CommandSender sender, String message) {
        sendMsg(sender, message, true);
    }
    public static void sendMsg(CommandSender sender, String message, boolean showPrefix) { sendMsg(sender, toTextComp(message), showPrefix); }
    public static void sendMsg(CommandSender sender, TextComponent message, boolean showPrefix) {
        if (showPrefix) message = toTextComp(SellstickConfig.prefix).append(message);
        sendMsg(sender, message);
    }
    public static void sendMsg(CommandSender sender, TextComponent message) {
        if (sender instanceof Player) {
            sender.sendMessage(message);
            return;
        }
        log(Level.INFO, message);
    }

    // Send Action Bar Messages
    public static void sendActionBar(CommandSender sender, String string) {
        Component msg = MiniMessage.miniMessage().deserialize(string);
        sender.sendActionBar(msg);
    }

    // Server console messages
    public static void error(String message) { log(Level.SEVERE, message); }
    public static void error(TextComponent message) { log(Level.SEVERE, message); }
    public static void log(String message) { log(Level.INFO, message); }
    public static void log(TextComponent message) { log(Level.INFO, message); }
    public static void log(Level level, TextComponent message) { log(level, PlainTextComponentSerializer.plainText().serializeOrNull(message)); }
    public static void log(Level level, String message) {
        if (message == null || message.isBlank()) return;
        SellStick.getInstance().getLogger().log(level, message);
    }

    /**
     * Converts a string to TextComponent and parses & for colour, # for hex.
     * @param string The string to parse.
     * @return The formated TextComponent.
     */
    static TextComponent toTextComp(String string) {
        if (string.contains("&"))
            return LegacyComponentSerializer.builder().character('&').hexCharacter('#').build().deserialize(string);
        else
            return (TextComponent) MiniMessage.miniMessage().deserialize(string);
    }
}
