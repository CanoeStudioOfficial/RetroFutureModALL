package com.canoestudio.retrofutureupdateaquatic.world;

import com.canoestudio.retrofutureupdateaquatic.Tags;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.WorldSavedData;

public class AquaticStructureData extends WorldSavedData {

    private static final String DATA_NAME = Tags.MOD_ID + "_structures";
    private static final int MAX_ENTRIES = 4096;

    private final Set<BlockPos> dolphinLocated = new HashSet<BlockPos>();

    public AquaticStructureData(String name) {
        super(name);
    }

    public void addDolphinLocated(BlockPos pos) {
        if (this.dolphinLocated.size() >= MAX_ENTRIES) {
            this.dolphinLocated.clear();
        }
        if (this.dolphinLocated.add(pos.toImmutable())) {
            this.markDirty();
        }
    }

    @Nullable
    public BlockPos findNearestDolphinLocated(BlockPos origin, int radius) {
        double radiusSq = radius * radius;
        double bestDistanceSq = Double.MAX_VALUE;
        BlockPos best = null;

        for (BlockPos candidate : this.dolphinLocated) {
            double distanceSq = candidate.distanceSq(origin);
            if (distanceSq <= radiusSq && distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                best = candidate;
            }
        }

        return best;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.dolphinLocated.clear();
        NBTTagList list = compound.getTagList("DolphinLocated", 10);
        for (NBTBase tag : list) {
            this.dolphinLocated.add(NBTUtil.getPosFromTag((NBTTagCompound)tag));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        for (BlockPos pos : this.dolphinLocated) {
            list.appendTag(NBTUtil.createPosTag(pos));
        }
        compound.setTag("DolphinLocated", list);
        return compound;
    }

    public static void recordDolphinLocated(World world, BlockPos pos) {
        if (!world.isRemote && world instanceof WorldServer) {
            get((WorldServer)world).addDolphinLocated(pos);
        }
    }

    @Nullable
    public static BlockPos findNearestDolphinLocated(World world, BlockPos origin, int radius) {
        if (world.isRemote || !(world instanceof WorldServer)) {
            return null;
        }
        return get((WorldServer)world).findNearestDolphinLocated(origin, radius);
    }

    private static AquaticStructureData get(WorldServer world) {
        AquaticStructureData data = (AquaticStructureData)world.getMapStorage()
            .getOrLoadData(AquaticStructureData.class, DATA_NAME);
        if (data == null) {
            data = new AquaticStructureData(DATA_NAME);
            world.getMapStorage().setData(DATA_NAME, data);
        }
        return data;
    }
}
