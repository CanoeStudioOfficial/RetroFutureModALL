package com.canoestudio.retrofuturemccore.api.gameevent;

import com.canoestudio.retrofuturemccore.internal.gameevent.RetroGameEventDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class RetroGameEvents {

    private RetroGameEvents() {
    }

    public static void emit(World world, RetroGameEvent event, BlockPos pos) {
        emit(world, event, centered(pos), GameEventContext.empty());
    }

    public static void emit(World world, RetroGameEvent event, BlockPos pos, GameEventContext context) {
        emit(world, event, centered(pos), context);
    }

    public static void emit(World world, RetroGameEvent event, Entity source) {
        emit(world, event, source.getPositionVector(), GameEventContext.of(source));
    }

    public static void emit(World world, RetroGameEvent event, Entity source, Entity projectileOwner) {
        emit(world, event, source.getPositionVector(), GameEventContext.of(source, projectileOwner, null));
    }

    public static void emit(World world, RetroGameEvent event, Vec3d position, GameEventContext context) {
        RetroGameEventDispatcher.emit(world, event, position, context);
    }

    public static void registerListener(World world, GameEventListener listener) {
        RetroGameEventDispatcher.register(world, listener);
    }

    public static void unregisterListener(World world, GameEventListener listener) {
        RetroGameEventDispatcher.unregister(world, listener);
    }

    private static Vec3d centered(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }
}
