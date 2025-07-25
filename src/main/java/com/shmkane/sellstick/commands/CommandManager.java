package com.shmkane.sellstick.commands;

import com.shmkane.sellstick.SellStick;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;

public class CommandManager {

    public CommandManager() {

        String cmd = "sellstick";

        AdminCommands adminCommands = new AdminCommands();
        PlayerCommands playerCommands = new PlayerCommands();

        new CommandAPICommand(cmd)
                .withPermission(cmd)
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission(cmd + ".reload")
                        .executes(adminCommands::reload))

                .withSubcommand(new CommandAPICommand("convert")
                        .withPermission(cmd + ".convert")
                        .withArguments(new PlayerArgument("target")
                                .replaceSafeSuggestions(SafeSuggestions.suggest(info ->
                                        Bukkit.getOnlinePlayers().toArray(new Player[0]))))
                        .executes(adminCommands::convert))

                .withSubcommand(new CommandAPICommand("give")
                        .withPermission(cmd + ".give")
                        .withArguments(new PlayerArgument("target")
                                .replaceSafeSuggestions(SafeSuggestions.suggest(info ->
                                        Bukkit.getOnlinePlayers().toArray(new Player[0]))))
                        .withArguments(new IntegerArgument("amount"))
                        .withArguments(new IntegerArgument("uses"))
                        .executes(adminCommands::give))

                .withSubcommand(new CommandAPICommand("toggle")
                        .withPermission(cmd + ".toggle")
                        .executes(playerCommands::toggle))

                .withSubcommand(new CommandAPICommand("merge")
                        .withPermission(cmd + ".merge")
                        .executes(playerCommands::merge))

                .withSubcommand(new CommandAPICommand("give")
                        .withPermission(cmd + ".give")
                        .withArguments(new PlayerArgument("target")
                                .replaceSafeSuggestions(SafeSuggestions.suggest(info ->
                                        Bukkit.getOnlinePlayers().toArray(new Player[0]))))
                        .withArguments(new IntegerArgument("amount"))
                        .withArguments(new IntegerArgument("uses"))
                        .executes(adminCommands::give))

                .register(SellStick.getInstance());
    }
}
