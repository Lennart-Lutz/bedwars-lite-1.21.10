package com.bedwarslite.logic;

import com.bedwarslite.team.TeamManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.*;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BedBreakHandler {

    public static void register(TeamManager teamManager) {

        /*
         * Handle bed breaks by "mining" it:
         *
         * 1. When a player is in the same team - not allowed
         * 2. When a player of a different team - destroy the bed
         */
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return true;

            if (!(state.getBlock() instanceof BedBlock)) {
                return true; // If no bed, just proceed as usual
            }

            // Does the bed belong to a team
            BlockPos bedBase = BedUtils.getBedBasePos((ServerWorld) world, pos); // Normalize to FOOT for comparison (teamManager saves the FOOT)
            String teamOfBed = teamManager.getTeamByBedPos(bedBase);
            if (teamOfBed == null) {
                return true; // Is no registered team bed -> player can proceed breaking the bed
            }

            // To which team does the player (that breaks the bed) belong
            String playerTeam = teamManager.getPlayerTeam(player.getUuid());

            // If the bed is from the same team, cancel the breaking process
            if (playerTeam != null && playerTeam.equals(teamOfBed)) {
                if (player instanceof ServerPlayerEntity spe) {
                    spe.sendMessage(Text.literal("You cannot destroy your own team's bed!").formatted(Formatting.RED), false);
                }
                return false;
            }

            return true;
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            if (world.isClient()) return;

            if (!(state.getBlock() instanceof BedBlock)) {
                return;
            }

            // Does the bed belong to a team?
            BlockPos bedBase = BedUtils.getBedBasePos((ServerWorld) world, pos); // Normalize to FOOT for comparison (teamManager saves the FOOT)
            String destroyedTeam = teamManager.getTeamByBedPos(bedBase);
            if (destroyedTeam == null) {
                return; // No registered team bed
            }

            // At this point, we already know the player has destroyed a bed of a different team

            teamManager.removeTeamBedById(destroyedTeam); // Just remove the bed, not the team yet (is done later upon death)

            String message = "The bed of team "
                    + destroyedTeam.replace("bed_", "").toUpperCase()
                    + " has been destroyed by "
                    + player.getName().getString()
                    + "!";

            if (world instanceof ServerWorld serverWorld) {
                MinecraftServer server = serverWorld.getServer();
                server.getPlayerManager()
                        .broadcast(Text.literal(message).formatted(Formatting.RED), false);
            }
        });

        /*
         * Make the bed fireproof, so it cant be destroyed
         */
    }

    /*
     * Handle bed breaks through explosions (any explosion will destroy the bed):
     *
     * 1. When a non player entity destroys the bed
     * 2. When a player of a different team destroys the bed
     * 3. When a player of the same team destroys the bed (yes really)
     *
     * Since the Fabric API does not provide a hook into the minecraft code for explosions, we use a Mixin.
     * The mixin calls this function to check whether a block after the explosion is a bed block.
     */
    public static void checkForTeamBedExplosion(ServerWorld serverWorld, Entity causingEntity, TeamManager teamManager, List<BlockPos> blocks) {

        // Check if a team bed has been destroyed
        for (BlockPos pos : blocks) {

            BlockState state = serverWorld.getBlockState(pos);
            if (!(state.getBlock() instanceof BedBlock)) { // Block not a bed block
                continue;
            }

            // Normalize to FOOT
            BlockPos baseBedPos = BedUtils.getBedBasePos(serverWorld, pos);

            // Does the bed belong to a team
            String bedTeamId = teamManager.getTeamByBedPos(baseBedPos);
            if (bedTeamId == null) { // No, continue with next block
                continue;
            }

            teamManager.removeTeamBedById(bedTeamId);

            // Check which entity caused the explosion
            String causingPlayerName = null;
            String causingPlayerTeam = null;

            if (causingEntity instanceof ServerPlayerEntity serverPlayer) {
                // The player itself created the explosion (however this is posible)
                causingPlayerName = serverPlayer.getName().getString();
                causingPlayerTeam = teamManager.getPlayerTeam(serverPlayer.getUuid());

            } else if (causingEntity instanceof net.minecraft.entity.TntEntity tnt) {
                // TNT caused the explosion due to a player
                Entity owner = tnt.getOwner();  // ggf. getCausingEntity() je nach Mapping
                if (owner instanceof ServerPlayerEntity ownerPlayer) {
                    causingPlayerName = ownerPlayer.getName().getString();
                    causingPlayerTeam = teamManager.getPlayerTeam(ownerPlayer.getUuid());
                }
            } // Everything else is "unknown"

            Text msg;
            if (causingPlayerName == null) {
                msg = Text.literal("The bed of team " + bedTeamId + " exploded!")
                        .formatted(Formatting.RED);
            } else {
                if (causingPlayerTeam != null && causingPlayerTeam.equals(bedTeamId)) {
                    msg = Text.literal("The bed of team " + bedTeamId + " exploded due to friendly fire...")
                            .formatted(Formatting.RED);
                } else {
                    msg = Text.literal("The bed of team " + bedTeamId + " exploded due to " + causingPlayerName + "!")
                            .formatted(Formatting.RED);
                }
            }

            serverWorld.getServer().getPlayerManager().broadcast(msg, false);
        }
    }
}

