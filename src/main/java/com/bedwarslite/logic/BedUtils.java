package com.bedwarslite.logic;

import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.BedPart;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BedUtils {

    /*
     * Function used to normalize initial bed positions to the FOOT part of the bed.
     * With this, it doesnt matter whether we click on the HEAD or FOOT, we always know its "that" bed.
     */
    public static BlockPos getBedBasePos(ServerWorld world, BlockPos pos) {

        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof BedBlock bed)) {
            return pos;
        }

        BedPart part = state.get(BedBlock.PART);
        var facing = state.get(BedBlock.FACING);

        // We always want to save/interact with the FOOT
        if (part == BedPart.HEAD) {
            return pos.offset(facing.getOpposite());
        } else {
            return pos;
        }
    }
}
