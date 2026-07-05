package com.canoestudio.retrofuturemc.contents.world.command;

import com.canoestudio.retrofuturemc.contents.world.gen.RetroFutureWorldGenerator;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainAPI;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainCaveSample;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainConfig;
import com.lonelyxiya.minecraft.moderncaveterrain.api.ModernCaveTerrainUndergroundBiomeSample;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class CommandLocateRetroFutureLushCave extends CommandBase {
    private static final int DEFAULT_RADIUS_CHUNKS = 48;
    private static final int MAX_RADIUS_CHUNKS = 192;
    private static final int SAMPLE_STEP = 4;

    @Override
    public String getName() {
        return "locateretrofuturelushcave";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/locateretrofuturelushcave [lush|dripstone] [chunkRadius]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!Loader.isModLoaded(ModernCaveTerrainAPI.MOD_ID)) {
            throw new CommandException("Modern Cave Terrain is required for this locator.");
        }

        World world = sender.getEntityWorld();
        if (world.provider.getDimension() != 0) {
            throw new CommandException("RetroFuture lush/dripstone cave locating only supports the Overworld.");
        }

        RetroFutureWorldGenerator.CaveStyle target = getTargetStyle(args);
        int radius = getRadius(args);
        BlockPos origin = sender.getPosition();
        sender.sendMessage(new TextComponentString("Searching for nearest RetroFuture " + getStyleName(target)
                + " cave within " + radius + " chunks..."));

        LocatedCave cave = findNearestCave(world, origin, target, radius);
        if (cave == null) {
            throw new CommandException("Could not find a RetroFuture " + getStyleName(target)
                    + " cave within " + radius + " chunks.");
        }

        int distance = (int)Math.round(Math.sqrt(origin.distanceSq(cave.pos)));
        sender.sendMessage(new TextComponentString("Nearest RetroFuture " + getStyleName(target) + " cave: "
                + cave.pos.getX() + " " + cave.pos.getY() + " " + cave.pos.getZ()
                + " (" + distance + " blocks away, sample strength " + String.format("%.2f", cave.strength) + ")"));
    }

    private RetroFutureWorldGenerator.CaveStyle getTargetStyle(String[] args) throws CommandException {
        if (args.length == 0 || isInteger(args[0]) || "lush".equalsIgnoreCase(args[0]) || "lush_caves".equalsIgnoreCase(args[0])) {
            return RetroFutureWorldGenerator.CaveStyle.LUSH;
        }
        if ("dripstone".equalsIgnoreCase(args[0]) || "dripstone_caves".equalsIgnoreCase(args[0])
                || "cave".equalsIgnoreCase(args[0])) {
            return RetroFutureWorldGenerator.CaveStyle.DRIPSTONE;
        }
        throw new CommandException("Unknown cave type '" + args[0] + "'. Use lush or dripstone.");
    }

    private int getRadius(String[] args) throws CommandException {
        if (args.length == 0) {
            return DEFAULT_RADIUS_CHUNKS;
        }
        if (args.length == 1 && isInteger(args[0])) {
            return parseInt(args[0], 1, MAX_RADIUS_CHUNKS);
        }
        if (args.length >= 2) {
            return parseInt(args[1], 1, MAX_RADIUS_CHUNKS);
        }
        return DEFAULT_RADIUS_CHUNKS;
    }

    @Nullable
    private LocatedCave findNearestCave(World world, BlockPos origin, RetroFutureWorldGenerator.CaveStyle target, int radiusChunks) {
        ModernCaveTerrainConfig config = ModernCaveTerrainAPI.getConfigForDimension(world.provider.getDimension());
        int minY = Math.max(4, config.getMojang118StyleCaveBottom());
        int maxY = Math.min(world.getActualHeight() - 1, config.getMojang118StyleCaveTop());
        if (maxY <= minY) {
            maxY = Math.min(96, world.getActualHeight() - 1);
            minY = 4;
        }

        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        LocatedCave nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        for (int radius = 0; radius <= radiusChunks; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }

                    LocatedCave cave = sampleChunk(world, config, originChunkX + dx, originChunkZ + dz, minY, maxY, origin, target);
                    if (cave == null) {
                        continue;
                    }

                    double distance = origin.distanceSq(cave.pos);
                    if (distance < nearestDistance) {
                        nearest = cave;
                        nearestDistance = distance;
                    }
                }
            }

            if (nearest != null && radius > 2 && nearestDistance < ((radius - 1) * 16) * ((radius - 1) * 16)) {
                return nearest;
            }
        }

        return nearest;
    }

    @Nullable
    private LocatedCave sampleChunk(World world, ModernCaveTerrainConfig config, int chunkX, int chunkZ, int minY, int maxY,
                                    BlockPos origin, RetroFutureWorldGenerator.CaveStyle target) {
        LocatedCave best = null;
        double bestDistance = Double.MAX_VALUE;
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;

        for (int localX = 2; localX < 16; localX += SAMPLE_STEP) {
            for (int localZ = 2; localZ < 16; localZ += SAMPLE_STEP) {
                for (int y = minY; y <= maxY; y += SAMPLE_STEP) {
                    int x = baseX + localX;
                    int z = baseZ + localZ;
                    ModernCaveTerrainUndergroundBiomeSample biomeSample = ModernCaveTerrainAPI.sampleUndergroundBiome(world, config, x, y, z);
                    if (RetroFutureWorldGenerator.classifyModernCaveStyle(biomeSample) != target) {
                        continue;
                    }

                    ModernCaveTerrainCaveSample caveSample = ModernCaveTerrainAPI.sampleCave(world, config, x, y, z);
                    if (!caveSample.isOpen()) {
                        continue;
                    }
                    if (caveSample.getFluidState() != null) {
                        continue;
                    }

                    BlockPos pos = new BlockPos(x, y, z);
                    double distance = origin.distanceSq(pos);
                    if (distance < bestDistance) {
                        best = new LocatedCave(pos, getStrength(biomeSample, target));
                        bestDistance = distance;
                    }
                }
            }
        }

        return best;
    }

    private double getStrength(ModernCaveTerrainUndergroundBiomeSample sample, RetroFutureWorldGenerator.CaveStyle target) {
        if (target == RetroFutureWorldGenerator.CaveStyle.LUSH) {
            return clamp((sample.getHumidity() - 0.7D) / 0.3D, 0.0D, 1.0D);
        }
        return clamp((sample.getContinentalness() - 0.8D) / 0.2D, 0.0D, 1.0D);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private String getStyleName(RetroFutureWorldGenerator.CaveStyle style) {
        return style == RetroFutureWorldGenerator.CaveStyle.DRIPSTONE ? "dripstone" : "lush";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "lush", "dripstone");
        }
        return Collections.emptyList();
    }

    private boolean isInteger(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = value.charAt(0) == '-' ? 1 : 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return value.charAt(0) != '-' || value.length() > 1;
    }

    private static class LocatedCave {
        private final BlockPos pos;
        private final double strength;

        private LocatedCave(BlockPos pos, double strength) {
            this.pos = pos;
            this.strength = strength;
        }
    }
}
