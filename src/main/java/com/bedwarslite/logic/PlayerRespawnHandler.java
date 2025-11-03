package com.bedwarslite.logic;

import com.bedwarslite.team.TeamManager;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

import java.util.ArrayList;
import java.util.List;

public class PlayerRespawnHandler {

    private static final int SPAWN_SEARCH_RADIUS = 10; // +- 10 -> 20x20x20 search cube

    public static void register(TeamManager teamManager) {

        /*
         * Teleport the player to the team wool block after spawning
         */
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {

            if (alive) {
                return;
            }

            // Check if the player is in a team, if not do not teleport
            var playerId = newPlayer.getUuid();
            String teamId = teamManager.getPlayerTeam(playerId);

            if (teamId == null) {
                return;
            }

            ServerWorld world = newPlayer.getEntityWorld();

            // When the player is in a team, check if the bed is present
            BlockPos bedPos = teamManager.getTeamBed(teamId);

            if (bedPos == null) { // Bed was destroyed, do not teleport, remove player form the team and update the player to spectator
                teamManager.removePlayerFromTeam(newPlayer.getUuid());
                newPlayer.changeGameMode(GameMode.SPECTATOR);

                if (teamManager.getPlayersInTeam(teamId).isEmpty()) { // If no member of the team is alive anymore after the bed has been destroyed, notify in chat
                    if (world instanceof ServerWorld serverWorld) {
                        MinecraftServer server = serverWorld.getServer();
                        server.getPlayerManager()
                                .broadcast(Text.literal("Team " + teamId + " has been eliminated!").formatted(Formatting.RED), false);
                    }
                }
                return;
            }

            // When the bed is still present, teleport the player to a valid block around the bed
            BlockPos spawnPos = findRandomSpawnAroundBed(world, bedPos);

            if (spawnPos == null) { // If no spawn point has been found, eliminate the player/team
                teamManager.removePlayerFromTeam(newPlayer.getUuid());
                newPlayer.changeGameMode(GameMode.SPECTATOR);

                if (teamManager.getPlayersInTeam(teamId).isEmpty()) { // If no player remains in the team, eliminate the team (destroying the bed)
                    teamManager.removeTeamBedById(teamId);

                    if (world instanceof ServerWorld serverWorld) {
                        MinecraftServer server = serverWorld.getServer();
                        server.getPlayerManager()
                                .broadcast(Text.literal("Team " + teamId + " has been eliminated!").formatted(Formatting.RED), false);
                    }
                }
                return;
            }

            // Teleport the player
            newPlayer.requestTeleport(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        });
    }

    /*
     * Searches around the bed position for position to spawn.
     */
    private static BlockPos findRandomSpawnAroundBed(ServerWorld world, BlockPos bedPos) {

        var random = world.getRandom();

        int baseX = bedPos.getX();
        int baseY = bedPos.getY();
        int baseZ = bedPos.getZ();

        List<BlockPos> candidates = new ArrayList<>();

        for (int dx = -SPAWN_SEARCH_RADIUS; dx <= SPAWN_SEARCH_RADIUS; dx++) {
            for (int dz = -SPAWN_SEARCH_RADIUS; dz <= SPAWN_SEARCH_RADIUS; dz++) {
                for (int dy = -SPAWN_SEARCH_RADIUS; dy <= SPAWN_SEARCH_RADIUS; dy++) {

                    BlockPos feet = new BlockPos(baseX + dx, baseY + dy, baseZ + dz);

                    if (feet.getY() <= world.getBottomY() + 1) continue;

                    BlockPos head = feet.up();
                    BlockPos below = feet.down();

                    // No spawn when not 2 Blocks of air above
                    if (!world.isAir(feet)) continue;
                    if (!world.isAir(head)) continue;
                    // No spawn on air below
                    if (world.isAir(below)) continue;
                    // No spawn on fluids
                    if (!world.getBlockState(below).getFluidState().isEmpty()) continue;

                    // Add possible valid spawn position
                    candidates.add(feet);
                }
            }
        }

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(random.nextInt(candidates.size()));
    }
}