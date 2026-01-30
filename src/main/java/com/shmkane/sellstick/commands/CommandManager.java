package com.shmkane.sellstick.commands;

import com.shmkane.sellstick.SellStick;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;

public class CommandManager {

    public CommandManager() {

        String cmd = "sellstick";

        AdminCommands adminCommands = new AdminCommands();
        PlayerCommands playerCommands = new PlayerCommands();

        new CommandAPICommand(cmd)
                .withPermission(cmd)
                .withSubcommand(new CommandAPICommand("reload")
                        .withPermission(cmd + ".reload")
                        .executes(adminCommands::reloadSellStick))

                .withSubcommand(new CommandAPICommand("convert")
                        .withPermission(cmd + ".convert")
                        .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                        .executes(adminCommands::convert))

                .withSubcommand(new CommandAPICommand("toggle")
                        .withPermission(cmd + ".toggle")
                        .executes(playerCommands::toggle))

                .withSubcommand(new CommandAPICommand("merge")
                        .withPermission(cmd + ".merge")
                        .executes(playerCommands::merge))

                .withSubcommand(new CommandAPICommand("give")
                        .withPermission(cmd + ".give")
                        .withArguments(new EntitySelectorArgument.OnePlayer("target"))
                        .withArguments(new IntegerArgument("amount"))
                        .withArguments(new StringArgument("uses")
                                .replaceSuggestions(ArgumentSuggestions.strings("1", "10", "100", "i")))
                        .executes(adminCommands::give))

                .register(SellStick.getInstance());
    }
}
