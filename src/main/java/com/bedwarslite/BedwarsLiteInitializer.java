package com.bedwarslite;

import com.bedwarslite.combat.FriendlyFireHandler;
import com.bedwarslite.logic.BedBreakHandler;
import com.bedwarslite.logic.BedInteractionHandler;
import com.bedwarslite.logic.PlayerRespawnHandler;
import com.bedwarslite.team.TeamManager;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BedwarsLiteInitializer implements ModInitializer {
	public static final String MOD_ID = "bedwarslite";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final TeamManager TEAM_MANAGER = new TeamManager();

	@Override
	public void onInitialize() {

        BedInteractionHandler.register(TEAM_MANAGER);
        BedBreakHandler.register(TEAM_MANAGER);
        PlayerRespawnHandler.register(TEAM_MANAGER);
        FriendlyFireHandler.register(TEAM_MANAGER);

        BedwarsLiteCommandInitializer.registerCommands();

        LOGGER.info(MOD_ID + " Mod initialized successfully!");
	}
}