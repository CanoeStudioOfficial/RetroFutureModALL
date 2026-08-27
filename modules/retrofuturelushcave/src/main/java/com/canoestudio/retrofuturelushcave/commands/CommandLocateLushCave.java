package com.canoestudio.retrofuturelushcave.commands;

import com.canoestudio.retrofuturelushcave.worldgen.cave.DensityCave118Generator;
import com.canoestudio.retrofuturelushcave.worldgen.lushcave.UndergroundRegionSelector;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public final class CommandLocateLushCave extends CommandBase {
    private static final int[] SEARCH_RADII_CHUNKS = {256, 512, 768, 1024};
    private static final int[] SAMPLE_Y = {24, 40, 56, 72, 88, 104, 120, 136, 152, 168};

    @Override
    public String getName() {
        return "locatelushcave";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.retrofuturelushcave.locatelushcave.usage";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 0) throw new CommandException(getUsage(sender));

        World world = sender.getEntityWorld();
        if (world == null || world.provider.getDimension() != 0) {
            throw new CommandException("commands.retrofuturelushcave.locatelushcave.overworld_only");
        }

        BlockPos origin = sender.getPosition();
        Candidate best = findNearest(world.getSeed(), origin.getX() >> 4, origin.getZ() >> 4);
        if (best == null) {
            sender.sendMessage(new TextComponentTranslation(
                    "commands.retrofuturelushcave.locatelushcave.not_found"));
            return;
        }

        sender.sendMessage(new TextComponentTranslation(
                "commands.retrofuturelushcave.locatelushcave.success", best.x, best.z));
    }

    private static Candidate findNearest(long seed, int originChunkX, int originChunkZ) {
        UndergroundRegionSelector regions = new UndergroundRegionSelector(new DensityCave118Generator(seed));
        int scannedRadius = 0;
        for (int searchRadius : SEARCH_RADII_CHUNKS) {
            Candidate found = findNearestInRange(regions, originChunkX, originChunkZ, scannedRadius, searchRadius);
            if (found != null) return found;
            scannedRadius = searchRadius + 1;
        }
        return null;
    }

    private static Candidate findNearestInRange(UndergroundRegionSelector regions, int originChunkX,
                                                int originChunkZ, int minimumRadius, int maximumRadius) {
        for (int radius = minimumRadius; radius <= maximumRadius; radius++) {
            Candidate ringBest = null;
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                    int worldX = ((originChunkX + dx) << 4) + 8;
                    int worldZ = ((originChunkZ + dz) << 4) + 8;
                    for (int y : SAMPLE_Y) {
                        if (!regions.isLushAt(worldX, y, worldZ)) continue;
                        Candidate candidate = new Candidate(worldX, worldZ, Math.abs(dx) + Math.abs(dz));
                        if (ringBest == null || candidate.distance < ringBest.distance) ringBest = candidate;
                    }
                }
            }
            if (ringBest != null) return ringBest;
        }
        return null;
    }

    private static final class Candidate {
        final int x;
        final int z;
        final int distance;

        Candidate(int x, int z, int distance) {
            this.x = x;
            this.z = z;
            this.distance = distance;
        }
    }
}
