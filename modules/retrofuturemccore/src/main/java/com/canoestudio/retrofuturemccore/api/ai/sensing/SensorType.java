package com.canoestudio.retrofuturemccore.api.ai.sensing;

import java.util.function.Supplier;

public final class SensorType<S extends Sensor<?>> {

    private final Supplier<S> factory;

    private SensorType(Supplier<S> factory) {
        this.factory = factory;
    }

    public static <S extends Sensor<?>> SensorType<S> create(Supplier<S> factory) {
        return new SensorType<S>(factory);
    }

    public S create() {
        return this.factory.get();
    }
}
