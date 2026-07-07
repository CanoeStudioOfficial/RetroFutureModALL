package com.canoestudio.retrofuturethewildupdate.block;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;

public class TileEntityHangingSign extends TileEntitySign {

    private boolean attached;
    private EnumFacing wallFacing = EnumFacing.NORTH;

    public boolean isAttached() {
        return this.attached;
    }

    public void setAttached(boolean attached) {
        this.attached = attached;
        this.markDirty();
    }

    public void setAttachedSilently(boolean attached) {
        this.attached = attached;
    }

    public EnumFacing getWallFacing() {
        return this.wallFacing;
    }

    public void setWallFacing(EnumFacing facing) {
        if (facing != null && facing.getAxis().isHorizontal()) {
            this.wallFacing = facing;
            this.markDirty();
        }
    }

    public void setWallFacingSilently(EnumFacing facing) {
        if (facing != null && facing.getAxis().isHorizontal()) {
            this.wallFacing = facing;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("Attached", this.attached);
        compound.setByte("WallFacing", (byte) this.wallFacing.getHorizontalIndex());
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.attached = compound.getBoolean("Attached");
        this.wallFacing = EnumFacing.byHorizontalIndex(compound.getByte("WallFacing"));
        if (!this.wallFacing.getAxis().isHorizontal()) {
            this.wallFacing = EnumFacing.NORTH;
        }
    }
}
