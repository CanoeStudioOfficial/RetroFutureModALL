package com.canoestudio.retrofutureupdateaquatic.world.gen;

import com.canoestudio.retrofutureupdateaquatic.RetroFutureUpdateAquatic;
import com.canoestudio.retrofutureupdateaquatic.entity.EntityDrowned;
import com.canoestudio.retrofutureupdateaquatic.world.AquaticLootTables;
import java.util.Map;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.IWorldGenerator;

public class AquaticStructureGenerator implements IWorldGenerator {

    private static final ResourceLocation[] SHIPWRECKS = new ResourceLocation[] {
        id("shipwreck_full"),
        id("shipwreck_up1"),
        id("shipwreck_up2"),
        id("shipwreck_upft1"),
        id("shipwreck_upbk1"),
        id("shipwreck_side1"),
        id("shipwreck_sideft1"),
        id("shipwreck_sidebk1"),
        id("shipwreck_down1"),
        id("shipwreck_downft1"),
        id("shipwreck_downbk1")
    };

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator,
            IChunkProvider chunkProvider) {
        if (world.provider.getDimension() != 0) {
            return;
        }

        int blockX = chunkX * 16;
        int blockZ = chunkZ * 16;
        BlockPos center = new BlockPos(blockX + 8, 0, blockZ + 8);
        Biome biome = world.getBiome(center);

        if (isOcean(biome)) {
            if (random.nextInt(300) == 0) {
                generateShipwreck(world, random, blockX + 8, blockZ + 8);
            }
            if (random.nextInt(180) == 0) {
                generateOceanRuin(world, random, blockX + 4 + random.nextInt(8), blockZ + 4 + random.nextInt(8),
                    false);
            }
        } else if (isBeach(biome)) {
            if (random.nextInt(260) == 0) {
                generateBuriedTreasure(world, random, blockX + 4 + random.nextInt(8),
                    blockZ + 4 + random.nextInt(8));
            }
        } else if (isRiver(biome) && random.nextInt(260) == 0) {
            generateOceanRuin(world, random, blockX + 4 + random.nextInt(8), blockZ + 4 + random.nextInt(8), true);
        }
    }

    private void generateShipwreck(World world, Random random, int x, int z) {
        MinecraftServer server = world.getMinecraftServer();
        if (server == null) {
            return;
        }

        TemplateManager manager = world.getSaveHandler().getStructureTemplateManager();
        Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
        PlacementSettings settings = new PlacementSettings()
            .setReplacedBlock(Blocks.STRUCTURE_VOID)
            .setRotation(rotation);
        Template template = manager.get(server, SHIPWRECKS[random.nextInt(SHIPWRECKS.length)]);
        BlockPos size = template.getSize();
        if (size.getX() <= 0 || size.getZ() <= 0) {
            return;
        }

        BlockPos check = new BlockPos(x, 1, z).add(Template.transformedBlockPos(settings,
            new BlockPos(size.getX() / 2, 0, size.getZ() / 2)));
        BlockPos floor = findSeaFloor(world, check.getX(), check.getZ());
        if (floor == null || floor.getY() <= 8) {
            return;
        }

        BlockPos origin = new BlockPos(x, floor.getY(), z).down(random.nextInt(5));
        template.addBlocksToWorld(world, origin, settings);
        for (Map.Entry<BlockPos, String> entry : template.getDataBlocks(origin, settings).entrySet()) {
            handleShipwreckDataBlock(world, random, entry.getKey(), entry.getValue());
        }
    }

    private void handleShipwreckDataBlock(World world, Random random, BlockPos pos, String marker) {
        if ("map_chest".equals(marker)) {
            placeLootChest(world, random, pos, AquaticLootTables.SHIPWRECK_MAP);
        } else if ("supply_chest".equals(marker)) {
            placeLootChest(world, random, pos, AquaticLootTables.SHIPWRECK_SUPPLY);
        } else if ("tresure_chest".equals(marker) || "treasure_chest".equals(marker)) {
            placeLootChest(world, random, pos, AquaticLootTables.SHIPWRECK_TREASURE);
        } else if ("drowned".equals(marker)) {
            spawnDrownedGroup(world, random, pos, 1 + random.nextInt(4));
        }
    }

    private void generateOceanRuin(World world, Random random, int x, int z, boolean small) {
        BlockPos floor = findSeaFloor(world, x, z);
        if (floor == null || floor.getY() < 8 || (!small && floor.getY() > world.getSeaLevel())) {
            return;
        }

        int radius = small ? 2 : 3 + random.nextInt(2);
        BlockPos center = floor.up();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.abs(dx) == radius && Math.abs(dz) == radius && random.nextBoolean()) {
                    continue;
                }
                BlockPos pos = center.add(dx, 0, dz);
                if (world.getBlockState(pos).getMaterial() == Material.WATER || world.isAirBlock(pos)) {
                    world.setBlockState(pos, randomRuinBlock(random), 2);
                }
                if (random.nextInt(5) == 0) {
                    BlockPos wall = pos.up();
                    if (world.getBlockState(wall).getMaterial() == Material.WATER || world.isAirBlock(wall)) {
                        world.setBlockState(wall, randomRuinBlock(random), 2);
                    }
                }
            }
        }

        if (random.nextInt(3) != 0) {
            BlockPos chest = center.up();
            if (world.getBlockState(chest).getMaterial() == Material.WATER || world.isAirBlock(chest)) {
                placeLootChest(world, random, chest, AquaticLootTables.OCEAN_RUIN);
            }
        }
        spawnDrownedGroup(world, random, center.up(), small ? 1 : 1 + random.nextInt(3));
    }

    private IBlockState randomRuinBlock(Random random) {
        switch (random.nextInt(6)) {
            case 0:
                return Blocks.MOSSY_COBBLESTONE.getDefaultState();
            case 1:
                return Blocks.COBBLESTONE.getDefaultState();
            case 2:
                return Blocks.STONEBRICK.getDefaultState();
            case 3:
                return Blocks.PRISMARINE.getDefaultState();
            case 4:
                return Blocks.SANDSTONE.getDefaultState();
            default:
                return Blocks.GRAVEL.getDefaultState();
        }
    }

    private void generateBuriedTreasure(World world, Random random, int x, int z) {
        BlockPos surface = world.getTopSolidOrLiquidBlock(new BlockPos(x, 0, z));
        if (surface.getY() <= 4 || surface.getY() > world.getSeaLevel() + 8) {
            return;
        }

        BlockPos chest = surface.down(2 + random.nextInt(3));
        IBlockState state = world.getBlockState(chest);
        if (state.getMaterial() != Material.SAND && state.getBlock() != Blocks.GRAVEL
                && state.getBlock() != Blocks.DIRT) {
            return;
        }
        placeLootChest(world, random, chest, AquaticLootTables.BURIED_TREASURE);
    }

    private void placeLootChest(World world, Random random, BlockPos pos, ResourceLocation lootTable) {
        world.setBlockState(pos, Blocks.CHEST.getDefaultState()
            .withProperty(BlockChest.FACING, EnumFacing.Plane.HORIZONTAL.random(random)), 3);
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof TileEntityChest) {
            ((TileEntityChest)tileEntity).setLootTable(lootTable, random.nextLong());
        }
    }

    private void spawnDrownedGroup(World world, Random random, BlockPos origin, int count) {
        for (int i = 0; i < count; i++) {
            BlockPos pos = origin.add(random.nextInt(7) - 3, random.nextInt(5) - 2, random.nextInt(7) - 3);
            if (world.getBlockState(pos).getMaterial() != Material.WATER
                    || world.getBlockState(pos.up()).getMaterial() != Material.WATER) {
                continue;
            }
            EntityDrowned drowned = new EntityDrowned(world);
            drowned.enablePersistence();
            drowned.moveToBlockPosAndAngles(pos, random.nextFloat() * 360.0F, 0.0F);
            world.spawnEntity(drowned);
        }
    }

    private BlockPos findSeaFloor(World world, int x, int z) {
        BlockPos pos = new BlockPos(x, Math.max(world.getSeaLevel() - 1, 1), z);
        while (pos.getY() > 1) {
            IBlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (!block.isReplaceable(world, pos) && state.getMaterial() != Material.LEAVES
                    && state.getMaterial() != Material.ICE && state.getMaterial() != Material.WATER) {
                return pos;
            }
            pos = pos.down();
        }
        return null;
    }

    private boolean isOcean(Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN);
    }

    private boolean isRiver(Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.RIVER);
    }

    private boolean isBeach(Biome biome) {
        return BiomeDictionary.hasType(biome, BiomeDictionary.Type.BEACH);
    }

    private static ResourceLocation id(String path) {
        return RetroFutureUpdateAquatic.prefix(path);
    }
}
