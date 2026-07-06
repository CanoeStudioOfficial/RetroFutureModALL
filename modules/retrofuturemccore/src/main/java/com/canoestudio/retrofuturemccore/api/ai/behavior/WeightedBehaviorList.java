package com.canoestudio.retrofuturemccore.api.ai.behavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.EntityLivingBase;

final class WeightedBehaviorList<E extends EntityLivingBase> {

    private final List<Entry<E>> entries = new ArrayList<Entry<E>>();

    void add(BehaviorControl<? super E> behavior, int weight) {
        this.entries.add(new Entry<E>(behavior, weight));
    }

    List<BehaviorControl<? super E>> getBehaviors() {
        List<BehaviorControl<? super E>> behaviors = new ArrayList<BehaviorControl<? super E>>();
        for (Entry<E> entry : this.entries) {
            behaviors.add(entry.behavior);
        }
        return behaviors;
    }

    void shuffle(Random random) {
        for (Entry<E> entry : this.entries) {
            double value = Math.max(random.nextDouble(), 1.0E-7D);
            entry.shuffleScore = -Math.pow(value, 1.0D / (double) entry.weight);
        }
        Collections.sort(this.entries, new Comparator<Entry<E>>() {
            @Override
            public int compare(Entry<E> first, Entry<E> second) {
                return Double.compare(first.shuffleScore, second.shuffleScore);
            }
        });
    }

    private static final class Entry<E extends EntityLivingBase> {

        private final BehaviorControl<? super E> behavior;
        private final int weight;
        private double shuffleScore;

        private Entry(BehaviorControl<? super E> behavior, int weight) {
            this.behavior = behavior;
            this.weight = Math.max(1, weight);
        }
    }
}
