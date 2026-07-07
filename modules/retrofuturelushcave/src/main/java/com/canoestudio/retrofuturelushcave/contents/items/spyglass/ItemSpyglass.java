package com.canoestudio.retrofuturelushcave.contents.items.spyglass;

import com.canoestudio.retrofuturelushcave.contents.items.ModItems;
import com.canoestudio.retrofuturelushcave.retrofuturelushcave.Tags;
import com.canoestudio.retrofuturelushcave.sounds.ModSoundHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

import static com.canoestudio.retrofuturelushcave.contents.tab.CreativeTab.CREATIVE_TABS;

public class ItemSpyglass extends Item {
    public static final int USE_DURATION = 1200;

    public ItemSpyglass(String name)
    {
        setTranslationKey(Tags.MOD_ID + "." + name.toLowerCase());
        setRegistryName(name.toLowerCase());
        setMaxStackSize(1);

        setCreativeTab(CREATIVE_TABS);

        this.addPropertyOverride(new ResourceLocation("model"), new IItemPropertyGetter()
        {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
            {
                if (entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack)
                {
                    return 2.0F;
                }

                return entityIn == null ? 1.0F : 0.0F;
            }
        });

        ModItems.ITEMS.add(this);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (!world.isRemote) {
            world.playSound(null, player.posX, player.posY, player.posZ, ModSoundHandler.ITEM_SPYGLASS_USE, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }


    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase entityLiving, int timeLeft) {
        if (!world.isRemote) {
            world.playSound(null, entityLiving.posX, entityLiving.posY, entityLiving.posZ, ModSoundHandler.ITEM_SPYGLASS_STOP_USING, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_DURATION;
    }
}

