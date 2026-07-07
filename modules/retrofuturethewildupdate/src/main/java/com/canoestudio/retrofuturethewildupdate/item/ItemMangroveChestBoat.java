package com.canoestudio.retrofuturethewildupdate.item;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.entity.EntityMangroveBoat;
import com.canoestudio.retrofuturethewildupdate.entity.EntityMangroveChestBoat;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.world.World;

public class ItemMangroveChestBoat extends ItemMangroveBoat {

    public ItemMangroveChestBoat() {
        super(RTWU.ID, "mangrove_chest_boat", CreativeTabs.TRANSPORTATION);
    }

    @Override
    protected EntityMangroveBoat createBoat(World world, double x, double y, double z) {
        return new EntityMangroveChestBoat(world, x, y, z);
    }
}
