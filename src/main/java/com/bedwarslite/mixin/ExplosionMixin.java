package com.bedwarslite.mixin;

import com.bedwarslite.BedwarsLiteInitializer;
import com.bedwarslite.logic.BedBreakHandler;
import com.bedwarslite.team.TeamManager;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.ExplosionImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ExplosionImpl.class)
public abstract class ExplosionMixin {

    @Final
    @Shadow
    private ServerWorld world;

    @Final
    @Shadow
    private Entity entity;

    @Inject(method = "getBlocksToDestroy", at = @At("RETURN"))
    private void bedwars$afterGetBlocksToDestroy(CallbackInfoReturnable<List<BlockPos>> cir) {

        // Get the list of blocks to destroy
        List<BlockPos> blocks = cir.getReturnValue();
        // Nothing has been destroyed
        if (blocks == null || blocks.isEmpty()) {
            return;
        }

        // Something has been destroyed, check for team beds
        TeamManager teamManager = BedwarsLiteInitializer.TEAM_MANAGER;
        BedBreakHandler.checkForTeamBedExplosion(world, this.entity, teamManager, blocks);
    }
}