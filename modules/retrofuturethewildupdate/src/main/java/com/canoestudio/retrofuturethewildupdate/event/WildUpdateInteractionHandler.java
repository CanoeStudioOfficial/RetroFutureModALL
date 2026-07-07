package com.canoestudio.retrofuturethewildupdate.event;

import com.canoestudio.retrofuturethewildupdate.RTWU;
import com.canoestudio.retrofuturethewildupdate.block.BlockMangrovePropagule;
import com.canoestudio.retrofuturethewildupdate.block.ModBlocks;
import com.canoestudio.retrofuturethewildupdate.entity.EntityAllay;
import java.util.List;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RTWU.ID)
public final class WildUpdateInteractionHandler {

    private WildUpdateInteractionHandler() {
    }

    @SubscribeEvent
    public static void onNoteBlockPlay(NoteBlockEvent.Play event) {
        World world = event.getWorld();
        if (world.isRemote) {
            return;
        }
        BlockPos pos = event.getPos();
        List<EntityAllay> allays = world.getEntitiesWithinAABB(EntityAllay.class,
            new AxisAlignedBB(pos).grow(16.0D));
        for (EntityAllay allay : allays) {
            allay.hearNoteBlock(pos);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        World world = event.getWorld();
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        ItemStack stack = event.getItemStack();

        if (tryWaterBottleMudConversion(event, world, pos, state, stack)) {
            return;
        }
        tryJukeboxAllayDance(event, world, pos, state, stack);
    }

    @SubscribeEvent
    public static void onBonemeal(BonemealEvent event) {
        World world = event.getWorld();
        if (world.isRemote || event.getBlock().getBlock() != ModBlocks.MANGROVE_LEAVES) {
            return;
        }

        BlockPos pos = findPropaguleAttachPos(world, event.getPos());
        if (pos == null) {
            return;
        }

        world.setBlockState(pos, ModBlocks.MANGROVE_PROPAGULE.getDefaultState()
            .withProperty(BlockMangrovePropagule.HANGING, true)
            .withProperty(BlockMangrovePropagule.AGE, 0), 3);
        event.setResult(Event.Result.ALLOW);
    }

    private static boolean tryWaterBottleMudConversion(PlayerInteractEvent.RightClickBlock event, World world,
                                                       BlockPos pos, IBlockState state, ItemStack stack) {
        if (world.isRemote || stack.isEmpty() || stack.getItem() != Items.POTIONITEM
            || PotionUtils.getPotionFromItem(stack) != PotionTypes.WATER || !canConvertToMud(state)) {
            return false;
        }

        world.setBlockState(pos, ModBlocks.MUD.getDefaultState(), 3);
        consumeBottle(event.getEntityPlayer(), event.getHand(), stack);
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        return true;
    }

    private static boolean canConvertToMud(IBlockState state) {
        return state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.GRASS;
    }

    private static void consumeBottle(EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (player.capabilities.isCreativeMode) {
            return;
        }
        stack.shrink(1);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        if (stack.isEmpty()) {
            player.setHeldItem(hand, bottle);
        } else if (!player.inventory.addItemStackToInventory(bottle)) {
            player.dropItem(bottle, false);
        }
        player.inventoryContainer.detectAndSendChanges();
    }

    private static void tryJukeboxAllayDance(PlayerInteractEvent.RightClickBlock event, World world, BlockPos pos,
                                             IBlockState state, ItemStack stack) {
        if (world.isRemote || state.getBlock() != Blocks.JUKEBOX) {
            return;
        }

        boolean hasRecord = state.getValue(BlockJukebox.HAS_RECORD);
        boolean insertingRecord = !hasRecord && !stack.isEmpty() && stack.getItem() instanceof ItemRecord;
        boolean stoppingRecord = hasRecord && (stack.isEmpty() || !(stack.getItem() instanceof ItemRecord));
        if (!insertingRecord && !stoppingRecord) {
            return;
        }

        notifyAllaysOfJukebox(world, pos, insertingRecord);
    }

    private static void notifyAllaysOfJukebox(World world, BlockPos pos, boolean playing) {
        List<EntityAllay> allays = world.getEntitiesWithinAABB(EntityAllay.class, new AxisAlignedBB(pos).grow(16.0D));
        for (EntityAllay allay : allays) {
            allay.setJukeboxPlaying(pos, playing);
        }
    }

    private static BlockPos findPropaguleAttachPos(World world, BlockPos leafPos) {
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos sideLeaf = leafPos.offset(facing);
            BlockPos hangPos = sideLeaf.down();
            if (world.getBlockState(sideLeaf).getBlock() == ModBlocks.MANGROVE_LEAVES && world.isAirBlock(hangPos)) {
                return hangPos;
            }
        }
        BlockPos below = leafPos.down();
        return world.isAirBlock(below) ? below : null;
    }
}
