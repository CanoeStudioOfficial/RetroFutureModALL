package com.canoestudio.retrofuturemccore.api.ai.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface LookTarget {

    Vec3d currentPosition();

    final class EntityLookTarget implements LookTarget {

        private final Entity entity;
        private final boolean useEyeHeight;

        public EntityLookTarget(Entity entity, boolean useEyeHeight) {
            this.entity = entity;
            this.useEyeHeight = useEyeHeight;
        }

        @Override
        public Vec3d currentPosition() {
            return this.useEyeHeight
                    ? new Vec3d(this.entity.posX, this.entity.posY + this.entity.getEyeHeight(), this.entity.posZ)
                    : this.entity.getPositionVector();
        }

        public Entity getEntity() {
            return this.entity;
        }
    }

    final class BlockLookTarget implements LookTarget {

        private final Vec3d position;

        public BlockLookTarget(BlockPos pos) {
            this(new Vec3d(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
        }

        public BlockLookTarget(Vec3d position) {
            this.position = position;
        }

        @Override
        public Vec3d currentPosition() {
            return this.position;
        }
    }
}
