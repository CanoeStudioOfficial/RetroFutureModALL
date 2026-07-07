package com.canoestudio.retrofuturelushcave.utils;

import com.canoestudio.retrofuturelushcave.contents.blocks.LightningRodBlock;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashSet;
import java.util.Set;

public class LightningRodData extends WorldSavedData {
    private static final String DATA_NAME = Tags.MOD_ID + "_lightning_rods";
    private final Set<BlockPos> positions = new HashSet<>();

    public LightningRodData(String name) {
        super(name);
    }

    public void add(BlockPos pos) {
        if (positions.add(pos.toImmutable())) {
            markDirty();
        }
    }

    public void remove(BlockPos pos) {
        if (positions.remove(pos)) {
            markDirty();
        }
    }

    public BlockPos getClosest(WorldServer world, BlockPos pos, int radius) {
        double radiusSq = radius * radius;
        double bestDistanceSq = Double.MAX_VALUE;
        BlockPos bestPos = null;

        for (BlockPos candidate : new HashSet<>(positions)) {
            if (!world.isBlockLoaded(candidate, false)) {
                continue;
            }

            IBlockState state = world.getBlockState(candidate);
            if (!(state.getBlock() instanceof LightningRodBlock)) {
                positions.remove(candidate);
                markDirty();
                continue;
            }

            if (!world.canBlockSeeSky(candidate)) {
                continue;
            }

            double distanceSq = candidate.distanceSq(pos);
            if (distanceSq <= radiusSq && distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                bestPos = candidate;
            }
        }

        return bestPos;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        positions.clear();
        NBTTagList list = compound.getTagList("positions", 10);
        for (NBTBase tag : list) {
            positions.add(NBTUtil.getPosFromTag((NBTTagCompound) tag));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : positions) {
            list.appendTag(NBTUtil.createPosTag(pos));
        }

        compound.setTag("positions", list);
        return compound;
    }

    public static LightningRodData get(WorldServer world) {
        LightningRodData data = (LightningRodData) world.getMapStorage().getOrLoadData(LightningRodData.class, DATA_NAME);
        if (data == null) {
            data = new LightningRodData(DATA_NAME);
            world.getMapStorage().setData(DATA_NAME, data);
        }

        return data;
    }
}
