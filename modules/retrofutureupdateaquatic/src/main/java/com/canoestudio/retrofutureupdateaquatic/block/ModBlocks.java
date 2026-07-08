package com.canoestudio.retrofutureupdateaquatic.block;

import com.canoestudio.retrofuturemccore.api.block.RetroSlabBlock;
import com.canoestudio.retrofuturemccore.api.block.RetroStairsBlock;
import com.canoestudio.retrofuturemccore.api.block.RetroButtonBlock;
import com.canoestudio.retrofuturemccore.api.block.RetroPressurePlateBlock;
import com.canoestudio.retrofuturemccore.api.block.RetroTrapDoorBlock;
import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.item.ItemSeaPickleBlock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSlab;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = RetroFutureUpdateAquatic.ID)
public final class ModBlocks {

    public static final BlockSeagrass SEAGRASS = new BlockSeagrass();
    public static final BlockKelp KELP = new BlockKelp();
    public static final BlockSeaPickle SEA_PICKLE = new BlockSeaPickle();
    public static final BlockBlueIce BLUE_ICE = new BlockBlueIce();
    public static final BlockBubbleColumn BUBBLE_COLUMN = new BlockBubbleColumn();
    public static final BlockConduit CONDUIT = new BlockConduit();
    public static final BlockTurtleEgg TURTLE_EGG = new BlockTurtleEgg();
    public static final Block DRIED_KELP_BLOCK = new BlockAquaticSimple("dried_kelp_block", Material.GRASS,
        MapColor.BROWN, SoundType.PLANT, 0.5F, 2.5F, CreativeTabs.BUILDING_BLOCKS);
    public static final Block PRISMARINE_STAIRS = new RetroStairsBlock(RetroFutureUpdateAquatic.ID,
        "prismarine_stairs", prismarineState(BlockPrismarine.EnumType.ROUGH), SoundType.STONE, 1.5F, 6.0F,
        CreativeTabs.BUILDING_BLOCKS);
    public static final Block PRISMARINE_BRICK_STAIRS = new RetroStairsBlock(RetroFutureUpdateAquatic.ID,
        "prismarine_brick_stairs", prismarineState(BlockPrismarine.EnumType.BRICKS), SoundType.STONE, 1.5F, 6.0F,
        CreativeTabs.BUILDING_BLOCKS);
    public static final Block DARK_PRISMARINE_STAIRS = new RetroStairsBlock(RetroFutureUpdateAquatic.ID,
        "dark_prismarine_stairs", prismarineState(BlockPrismarine.EnumType.DARK), SoundType.STONE, 1.5F, 6.0F,
        CreativeTabs.BUILDING_BLOCKS);
    public static final RetroSlabBlock PRISMARINE_SLAB = new RetroSlabBlock.Single(RetroFutureUpdateAquatic.ID,
        "prismarine_slab", Material.ROCK, MapColor.CYAN, SoundType.STONE, 1.5F, 6.0F,
        CreativeTabs.BUILDING_BLOCKS);
    public static final RetroSlabBlock DOUBLE_PRISMARINE_SLAB = new RetroSlabBlock.Double(RetroFutureUpdateAquatic.ID,
        "double_prismarine_slab", Material.ROCK, MapColor.CYAN, SoundType.STONE, 1.5F, 6.0F,
        CreativeTabs.BUILDING_BLOCKS);
    public static final RetroSlabBlock PRISMARINE_BRICK_SLAB = new RetroSlabBlock.Single(RetroFutureUpdateAquatic.ID,
        "prismarine_brick_slab", Material.ROCK, MapColor.DIAMOND, SoundType.STONE, 1.5F, 6.0F,
        CreativeTabs.BUILDING_BLOCKS);
    public static final RetroSlabBlock DOUBLE_PRISMARINE_BRICK_SLAB = new RetroSlabBlock.Double(
        RetroFutureUpdateAquatic.ID, "double_prismarine_brick_slab", Material.ROCK, MapColor.DIAMOND,
        SoundType.STONE, 1.5F, 6.0F, CreativeTabs.BUILDING_BLOCKS);
    public static final RetroSlabBlock DARK_PRISMARINE_SLAB = new RetroSlabBlock.Single(RetroFutureUpdateAquatic.ID,
        "dark_prismarine_slab", Material.ROCK, MapColor.DIAMOND, SoundType.STONE, 1.5F, 6.0F,
        CreativeTabs.BUILDING_BLOCKS);
    public static final RetroSlabBlock DOUBLE_DARK_PRISMARINE_SLAB = new RetroSlabBlock.Double(
        RetroFutureUpdateAquatic.ID, "double_dark_prismarine_slab", Material.ROCK, MapColor.DIAMOND,
        SoundType.STONE, 1.5F, 6.0F, CreativeTabs.BUILDING_BLOCKS);

    public static final CoralSet TUBE_CORAL = new CoralSet("tube", MapColor.BLUE);
    public static final CoralSet BRAIN_CORAL = new CoralSet("brain", MapColor.PINK);
    public static final CoralSet BUBBLE_CORAL = new CoralSet("bubble", MapColor.PURPLE);
    public static final CoralSet FIRE_CORAL = new CoralSet("fire", MapColor.RED);
    public static final CoralSet HORN_CORAL = new CoralSet("horn", MapColor.YELLOW);

    public static final WoodSet OAK_WOOD_SET = new WoodSet("oak");
    public static final WoodSet SPRUCE_WOOD_SET = new WoodSet("spruce");
    public static final WoodSet BIRCH_WOOD_SET = new WoodSet("birch");
    public static final WoodSet JUNGLE_WOOD_SET = new WoodSet("jungle");
    public static final WoodSet ACACIA_WOOD_SET = new WoodSet("acacia");
    public static final WoodSet DARK_OAK_WOOD_SET = new WoodSet("dark_oak");

    private static final List<Block> BLOCKS = new ArrayList<Block>();
    private static final List<CoralSet> CORALS = new ArrayList<CoralSet>();
    private static final List<WoodSet> WOODS = new ArrayList<WoodSet>();

    static {
        Collections.addAll(CORALS, TUBE_CORAL, BRAIN_CORAL, BUBBLE_CORAL, FIRE_CORAL, HORN_CORAL);
        Collections.addAll(WOODS, OAK_WOOD_SET, SPRUCE_WOOD_SET, BIRCH_WOOD_SET, JUNGLE_WOOD_SET,
            ACACIA_WOOD_SET, DARK_OAK_WOOD_SET);
        Collections.addAll(BLOCKS, SEAGRASS, KELP, SEA_PICKLE, BLUE_ICE, BUBBLE_COLUMN, CONDUIT, TURTLE_EGG,
            DRIED_KELP_BLOCK, PRISMARINE_STAIRS, PRISMARINE_BRICK_STAIRS, DARK_PRISMARINE_STAIRS,
            PRISMARINE_SLAB, DOUBLE_PRISMARINE_SLAB, PRISMARINE_BRICK_SLAB, DOUBLE_PRISMARINE_BRICK_SLAB,
            DARK_PRISMARINE_SLAB, DOUBLE_DARK_PRISMARINE_SLAB);
        for (CoralSet coral : CORALS) {
            coral.addTo(BLOCKS);
        }
        for (WoodSet wood : WOODS) {
            wood.addTo(BLOCKS);
        }
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(BLOCKS.toArray(new Block[0]));
    }

    @SubscribeEvent
    public static void registerItemBlocks(RegistryEvent.Register<Item> event) {
        for (Block block : BLOCKS) {
            if (block == DOUBLE_PRISMARINE_SLAB || block == DOUBLE_PRISMARINE_BRICK_SLAB
                    || block == DOUBLE_DARK_PRISMARINE_SLAB) {
                continue;
            }
            event.getRegistry().register(block == SEA_PICKLE
                ? new ItemSeaPickleBlock(block)
                : createItemBlock(block));
        }
    }

    public static List<Block> allBlocks() {
        return Collections.unmodifiableList(BLOCKS);
    }

    public static List<CoralSet> corals() {
        return Collections.unmodifiableList(CORALS);
    }

    public static List<WoodSet> woods() {
        return Collections.unmodifiableList(WOODS);
    }

    public static IBlockState getStrippedState(IBlockState state) {
        WoodSet woodSet = getWoodSet(state);
        if (woodSet == null) {
            return null;
        }

        EnumFacing.Axis axis = getAxis(state);
        return isWoodBlock(state) ? woodSet.strippedWood.getDefaultState().withProperty(BlockRotatedPillar.AXIS, axis)
            : woodSet.strippedLog.getDefaultState().withProperty(BlockRotatedPillar.AXIS, axis);
    }

    private static boolean isWoodBlock(IBlockState state) {
        return state.getBlock() instanceof BlockAquaticPillar
            && state.getBlock().getRegistryName() != null
            && state.getBlock().getRegistryName().getPath().endsWith("_wood")
            || isVanillaBark(state);
    }

    private static boolean isVanillaBark(IBlockState state) {
        if (state.getBlock() != Blocks.LOG && state.getBlock() != Blocks.LOG2) {
            return false;
        }
        return state.getValue(BlockLog.LOG_AXIS) == BlockLog.EnumAxis.NONE;
    }

    private static EnumFacing.Axis getAxis(IBlockState state) {
        if (state.getBlock() instanceof BlockAquaticPillar) {
            return state.getValue(BlockRotatedPillar.AXIS);
        }
        if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2) {
            BlockLog.EnumAxis axis = state.getValue(BlockLog.LOG_AXIS);
            if (axis == BlockLog.EnumAxis.X) {
                return EnumFacing.Axis.X;
            }
            if (axis == BlockLog.EnumAxis.Z) {
                return EnumFacing.Axis.Z;
            }
        }
        return EnumFacing.Axis.Y;
    }

    private static WoodSet getWoodSet(IBlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.LOG) {
            BlockPlanks.EnumType variant = state.getValue(BlockOldLog.VARIANT);
            if (variant == BlockPlanks.EnumType.OAK) {
                return OAK_WOOD_SET;
            }
            if (variant == BlockPlanks.EnumType.SPRUCE) {
                return SPRUCE_WOOD_SET;
            }
            if (variant == BlockPlanks.EnumType.BIRCH) {
                return BIRCH_WOOD_SET;
            }
            if (variant == BlockPlanks.EnumType.JUNGLE) {
                return JUNGLE_WOOD_SET;
            }
        } else if (block == Blocks.LOG2) {
            BlockPlanks.EnumType variant = state.getValue(BlockNewLog.VARIANT);
            return variant == BlockPlanks.EnumType.ACACIA ? ACACIA_WOOD_SET : DARK_OAK_WOOD_SET;
        }

        for (WoodSet wood : WOODS) {
            if (block == wood.wood || block == wood.strippedLog || block == wood.strippedWood) {
                return block == wood.strippedLog || block == wood.strippedWood ? null : wood;
            }
        }
        return null;
    }

    private static IBlockState prismarineState(BlockPrismarine.EnumType type) {
        return Blocks.PRISMARINE.getDefaultState().withProperty(BlockPrismarine.VARIANT, type);
    }

    private static Item createItemBlock(Block block) {
        if (block == PRISMARINE_SLAB) {
            return new ItemSlab(block, PRISMARINE_SLAB, DOUBLE_PRISMARINE_SLAB).setRegistryName(block.getRegistryName());
        }
        if (block == PRISMARINE_BRICK_SLAB) {
            return new ItemSlab(block, PRISMARINE_BRICK_SLAB, DOUBLE_PRISMARINE_BRICK_SLAB)
                .setRegistryName(block.getRegistryName());
        }
        if (block == DARK_PRISMARINE_SLAB) {
            return new ItemSlab(block, DARK_PRISMARINE_SLAB, DOUBLE_DARK_PRISMARINE_SLAB)
                .setRegistryName(block.getRegistryName());
        }
        return new ItemBlock(block).setRegistryName(block.getRegistryName());
    }

    private ModBlocks() {
    }

    public static final class CoralSet {
        public final String name;
        public final BlockCoralBlock deadBlock;
        public final BlockCoralBlock liveBlock;
        public final BlockCoralPlant deadPlant;
        public final BlockCoralPlant livePlant;
        public final BlockCoralFan deadFan;
        public final BlockCoralFan liveFan;

        private CoralSet(String name, MapColor color) {
            this.name = name;
            this.deadBlock = new BlockCoralBlock("dead_" + name + "_coral_block", MapColor.GRAY);
            this.liveBlock = new BlockCoralBlock(name + "_coral_block", color).deadVersion(this.deadBlock);
            this.deadPlant = new BlockCoralPlant("dead_" + name + "_coral", MapColor.GRAY);
            this.livePlant = new BlockCoralPlant(name + "_coral", color).deadVersion(this.deadPlant);
            this.deadFan = new BlockCoralFan("dead_" + name + "_coral_fan", MapColor.GRAY);
            this.liveFan = new BlockCoralFan(name + "_coral_fan", color).deadVersion(this.deadFan);
        }

        private void addTo(List<Block> blocks) {
            Collections.addAll(blocks, this.deadBlock, this.liveBlock, this.deadPlant, this.livePlant,
                this.deadFan, this.liveFan);
        }
    }

    public static final class WoodSet {
        public final String name;
        public final BlockAquaticPillar wood;
        public final BlockAquaticPillar strippedLog;
        public final BlockAquaticPillar strippedWood;
        public final Block trapdoor;
        public final Block pressurePlate;
        public final Block button;

        private WoodSet(String name) {
            this.name = name;
            this.wood = new BlockAquaticPillar(name + "_wood", Material.WOOD, SoundType.WOOD, 2.0F, 2.0F,
                CreativeTabs.BUILDING_BLOCKS);
            this.strippedLog = new BlockAquaticPillar("stripped_" + name + "_log", Material.WOOD, SoundType.WOOD,
                2.0F, 2.0F, CreativeTabs.BUILDING_BLOCKS);
            this.strippedWood = new BlockAquaticPillar("stripped_" + name + "_wood", Material.WOOD, SoundType.WOOD,
                2.0F, 2.0F, CreativeTabs.BUILDING_BLOCKS);
            boolean vanillaOakEquivalent = "oak".equals(name);
            this.trapdoor = vanillaOakEquivalent ? null : new RetroTrapDoorBlock(RetroFutureUpdateAquatic.ID,
                name + "_trapdoor", Material.WOOD, SoundType.WOOD, 3.0F, 3.0F, CreativeTabs.REDSTONE);
            this.pressurePlate = vanillaOakEquivalent ? null : new RetroPressurePlateBlock(RetroFutureUpdateAquatic.ID,
                name + "_pressure_plate", Material.WOOD, BlockPressurePlate.Sensitivity.EVERYTHING, SoundType.WOOD,
                0.5F, 0.5F, CreativeTabs.REDSTONE);
            this.button = vanillaOakEquivalent ? null : new RetroButtonBlock(RetroFutureUpdateAquatic.ID,
                name + "_button", true, SoundType.WOOD, 0.5F, 0.5F, CreativeTabs.REDSTONE);
        }

        private void addTo(List<Block> blocks) {
            Collections.addAll(blocks, this.wood, this.strippedLog, this.strippedWood);
            if (this.trapdoor != null) {
                Collections.addAll(blocks, this.trapdoor, this.pressurePlate, this.button);
            }
        }
    }
}
