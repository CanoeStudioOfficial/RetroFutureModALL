package com.canoestudio.retrofutureupdateaquatic.event;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.block.ModBlocks;
import com.canoestudio.retrofutureupdateaquatic.item.ModItems;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import net.minecraft.world.storage.MapDecoration;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Block and item interactions belonging to the aquatic content module. */
@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class AquaticInteractionEvents {

    private AquaticInteractionEvents() {
    }

    @SubscribeEvent
    public static void onFurnaceFuel(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().getItem() == Item.getItemFromBlock(ModBlocks.DRIED_KELP_BLOCK)) {
            event.setBurnTime(4000);
        }
    }

    @SubscribeEvent
    public static void onItemFished(ItemFishedEvent event) {
        // 1.13's fishing table gives treasure a 5% roll and the treasure
        // pool gives the nautilus shell one of six equal entries. Forge's
        // 1.12 event exposes the final stacks but not the loot-table branch,
        // so preserve the same aggregate 1/120 chance.
        if (event.getEntityPlayer().world.rand.nextInt(120) == 0) {
            event.getDrops().add(new ItemStack(ModItems.NAUTILUS_SHELL));
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (tryMarkBannerOnMap(event, stack) || tryCarvePumpkin(event, stack)) {
            return;
        }

        if (stack.isEmpty() || !stack.getItem().getToolClasses(stack).contains("axe")) {
            return;
        }

        IBlockState strippedState = ModBlocks.getStrippedState(event.getWorld().getBlockState(event.getPos()));
        if (strippedState == null) {
            return;
        }

        if (!event.getWorld().isRemote) {
            event.getWorld().setBlockState(event.getPos(), strippedState, 11);
            event.getWorld().playSound(null, event.getPos(), SoundEvents.BLOCK_WOOD_BREAK,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                stack.damageItem(1, event.getEntityPlayer());
            }
        }
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
    }

    private static boolean tryCarvePumpkin(PlayerInteractEvent.RightClickBlock event, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.SHEARS
                || event.getWorld().getBlockState(event.getPos()).getBlock() != Blocks.PUMPKIN) {
            return false;
        }

        if (!event.getWorld().isRemote) {
            EnumFacing facing = event.getEntityPlayer().getHorizontalFacing().getOpposite();
            event.getWorld().setBlockState(event.getPos(), ModBlocks.CARVED_PUMPKIN.getDefaultState()
                .withProperty(BlockPumpkin.FACING, facing), 11);
            for (int i = 0; i < 4; i++) {
                event.getWorld().spawnEntity(new EntityItem(event.getWorld(), event.getPos().getX() + 0.5D,
                    event.getPos().getY() + 0.5D, event.getPos().getZ() + 0.5D,
                    new ItemStack(Items.PUMPKIN_SEEDS)));
            }
            if (!event.getEntityPlayer().capabilities.isCreativeMode) {
                stack.damageItem(1, event.getEntityPlayer());
            }
            event.getWorld().playSound(null, event.getPos(), SoundEvents.BLOCK_WOOD_BREAK,
                SoundCategory.BLOCKS, 1.0F, 1.0F);
        }
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
        return true;
    }

    private static boolean tryMarkBannerOnMap(PlayerInteractEvent.RightClickBlock event, ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != Items.FILLED_MAP) {
            return false;
        }
        IBlockState state = event.getWorld().getBlockState(event.getPos());
        if (state.getBlock() != Blocks.STANDING_BANNER && state.getBlock() != Blocks.WALL_BANNER) {
            return false;
        }

        MapData map = Items.FILLED_MAP.getMapData(stack, event.getWorld());
        if (map == null) {
            return false;
        }
        String id = "banner-" + event.getPos().getX() + "-" + event.getPos().getY() + "-"
            + event.getPos().getZ();
        if (!event.getWorld().isRemote && !hasMapDecoration(stack, id)) {
            MapData.addTargetDecoration(stack, event.getPos(), id, MapDecoration.Type.RED_MARKER);
        }
        event.setCancellationResult(EnumActionResult.SUCCESS);
        event.setCanceled(true);
        return true;
    }

    private static boolean hasMapDecoration(ItemStack map, String id) {
        if (!map.hasTagCompound() || !map.getTagCompound().hasKey("Decorations", 9)) {
            return false;
        }
        NBTTagList decorations = map.getTagCompound().getTagList("Decorations", 10);
        for (int i = 0; i < decorations.tagCount(); i++) {
            if (id.equals(decorations.getCompoundTagAt(i).getString("id"))) {
                return true;
            }
        }
        return false;
    }
}
