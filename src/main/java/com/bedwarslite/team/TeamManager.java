package com.bedwarslite.team;

import net.minecraft.util.math.BlockPos;

import java.util.*;

public class TeamManager {

    private final Map<UUID, String> playerTeams = new HashMap<>();
    private final Map<String, Set<UUID>> teamPlayers = new HashMap<>();
    private final Map<String, BlockPos> teamBeds = new HashMap<>();

    public void addPlayerToTeam(UUID playerId, String teamId, BlockPos bedPos) {

        String oldTeam = playerTeams.get(playerId);

        // If player is already in a different team, change the team
        if (oldTeam != null && !oldTeam.equals(teamId)) {
            var oldSet = teamPlayers.get(oldTeam);
            if (oldSet != null) oldSet.remove(playerId);
        }

        // Add player to the team (of teamId)
        playerTeams.put(playerId, teamId);
        teamPlayers.computeIfAbsent(teamId, k -> new HashSet<>()).add(playerId);
        // Add/Update the bed position)
        teamBeds.put(teamId, bedPos);
    }


    public String getPlayerTeam(UUID playerId) {
        return playerTeams.get(playerId);
    }

    public Set<UUID> getPlayersInTeam(String teamId) {
        return teamPlayers.getOrDefault(teamId, Set.of());
    }

    public BlockPos getTeamBed(String teamId) {
        return teamBeds.get(teamId);
    }

    public String getTeamByBedPos(BlockPos pos) {
        for (var entry : teamBeds.entrySet()) {
            if (entry.getValue().equals(pos)) {
                return entry.getKey();
            }
        }
        return null;
    }


    public void removeTeamBedById(String teamId) {
        teamBeds.remove(teamId);
    }

    public void removePlayerFromTeam(UUID playerId) {
        String teamId = playerTeams.get(playerId);

        playerTeams.remove(playerId);
        teamPlayers.remove(teamId);
    }


    public void reset() {
        teamBeds.clear();
        teamPlayers.clear();
        playerTeams.clear();
    }
}
