package com.bedwarslite;

import com.bedwarslite.team.TeamManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class BedwarsLiteCommandInitializer {

    /**
     * Used to register commands for the lucky block mod using the
     * Command Manager and Command Callback.
     */
    public static void registerCommands() {

        BedwarsLiteInitializer.LOGGER.info("Registering Mod Commands for " + BedwarsLiteInitializer.MOD_ID);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal(BedwarsLiteInitializer.MOD_ID)
                            .then(CommandManager.literal("reset")
                                    .executes(ctx -> resetTeamManager(ctx.getSource()))
                            )
            );
        });
    }

    /* ---------------- Command Functions ---------------- */

    private static int resetTeamManager(ServerCommandSource source) {

        BedwarsLiteInitializer.TEAM_MANAGER.reset();

        StringBuilder sb = new StringBuilder("Bedwars Lite Mod has been reset!");
        source.sendFeedback(() -> Text.literal(sb.toString()), true);

        return 1;
    }
}
