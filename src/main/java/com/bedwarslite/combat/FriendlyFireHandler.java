package com.bedwarslite.combat;

import com.bedwarslite.team.TeamManager;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public class FriendlyFireHandler {

    public static void register(TeamManager teamManager) {

        AttackEntityCallback.EVENT.register((player, world, hand, target, hitResult) -> {

            if (world.isClient()) {
                return ActionResult.PASS;
            }

            // We are just interested in PVP
            if (!(target instanceof PlayerEntity targetPlayer)) {
                return ActionResult.PASS;
            }

            // Get teams of both players
            String attackerTeam = teamManager.getPlayerTeam(player.getUuid());
            String targetTeam   = teamManager.getPlayerTeam(targetPlayer.getUuid());

            // If one of them does not have a team, proceed with the attack
            if (attackerTeam == null || targetTeam == null) {
                return ActionResult.PASS;
            }

            // Same team block the attack
            if (attackerTeam.equals(targetTeam)) {
                return ActionResult.FAIL; // verhindert den Angriff
            }

            return ActionResult.PASS; // Different Team, proceed with the attack (TODO: stats?)
        });
    }
}
