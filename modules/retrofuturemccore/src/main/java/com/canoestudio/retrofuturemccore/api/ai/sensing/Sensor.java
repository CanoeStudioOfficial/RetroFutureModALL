package com.canoestudio.retrofuturemccore.api.ai.sensing;

import com.canoestudio.retrofuturemccore.api.ai.Brain;
import com.canoestudio.retrofuturemccore.api.ai.memory.MemoryModuleType;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldServer;

public abstract class Sensor<E extends EntityLivingBase> {

    public static final int DEFAULT_SCAN_RATE = 20;

    private final int scanRate;
    private long timeToTick;

    protected Sensor() {
        this(DEFAULT_SCAN_RATE);
    }

    protected Sensor(int scanRate) {
        if (scanRate <= 0) {
            throw new IllegalArgumentException("scanRate must be positive");
        }
        this.scanRate = scanRate;
    }

    public void randomlyDelayStart(Random random) {
        this.timeToTick = random.nextInt(this.scanRate);
    }

    public final void tick(WorldServer world, Brain<? extends E> brain, E entity) {
        if (--this.timeToTick <= 0L) {
            this.timeToTick = this.scanRate;
            this.doTick(world, brain, entity);
        }
    }

    protected abstract void doTick(WorldServer world, Brain<? extends E> brain, E entity);

    public abstract Set<MemoryModuleType<?>> requires();

    public static <T, U> BiPredicate<T, U> rememberPositives(final int invocations, final BiPredicate<T, U> predicate) {
        final AtomicInteger positivesLeft = new AtomicInteger(0);
        return new BiPredicate<T, U>() {
            @Override
            public boolean test(T first, U second) {
                if (predicate.test(first, second)) {
                    positivesLeft.set(invocations);
                    return true;
                }
                return positivesLeft.decrementAndGet() >= 0;
            }
        };
    }
}
