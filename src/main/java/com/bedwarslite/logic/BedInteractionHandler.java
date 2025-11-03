package com.bedwarslite.logic;

import com.bedwarslite.team.TeamManager;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BedBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class BedInteractionHandler {

    public static void register(TeamManager teamManager) {

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {

            if (world.isClient()) return ActionResult.PASS;
            if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

            BlockPos pos = hitResult.getBlockPos();
            var state = world.getBlockState(pos);
            var block = state.getBlock();

            // Check if block is a bed
            if (block instanceof BedBlock bedBlock) {

                // The color of a bed is used as the team id
                String bedTeamId = bedBlock.getColor().toString().toUpperCase();
                String currentTeamId = teamManager.getPlayerTeam(player.getUuid());

                // Player is in no team: add to the team
                if (currentTeamId == null) {
                    BlockPos bedBase = BedUtils.getBedBasePos((ServerWorld) world, pos); // Save the FOOT of the bed
                    teamManager.addPlayerToTeam(player.getUuid(), bedTeamId, bedBase);
                    if (player instanceof ServerPlayerEntity spe) {
                        spe.sendMessage(Text.literal("You joined team: " + bedTeamId), false);
                    }
                } else { // Player is in a team

                    if (currentTeamId.equals(bedTeamId)) { // Player is in the same team, do nothing
                        if (player instanceof ServerPlayerEntity spe) {
                            spe.sendMessage(Text.literal("You are already in team: " + currentTeamId), false);
                        }
                    } else { // Player is in a different team, do not switch
                        if (player instanceof ServerPlayerEntity spe) {
                            spe.sendMessage(Text.literal("You can not switch the team"), false);
                        }
                    }
                    return ActionResult.FAIL; // Cancel interaction
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });
    }
}

