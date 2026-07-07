package com.canoestudio.retrofuturethewildupdate.entity;

import com.canoestudio.retrofuturethewildupdate.item.ModItems;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class EntityMangroveBoat extends EntityBoat {

    public EntityMangroveBoat(World world) {
        super(world);
        this.setBoatType(Type.OAK);
    }

    public EntityMangroveBoat(World world, double x, double y, double z) {
        super(world, x, y, z);
        this.setBoatType(Type.OAK);
    }

    @Override
    public Item getItemBoat() {
        return ModItems.MANGROVE_BOAT;
    }
}
