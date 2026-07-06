package com.canoestudio.retrofuturemccore.internal.gameevent;

import com.canoestudio.retrofuturemccore.api.gameevent.GameEventContext;
import com.canoestudio.retrofuturemccore.api.gameevent.GameEventListener;
import com.canoestudio.retrofuturemccore.api.gameevent.RetroGameEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public final class RetroGameEventDispatcher {

    private static final WeakHashMap<World, List<GameEventListener>> LISTENERS =
            new WeakHashMap<World, List<GameEventListener>>();

    private RetroGameEventDispatcher() {
    }

    public static synchronized void register(World world, GameEventListener listener) {
        if (world == null || listener == null) {
            return;
        }
        List<GameEventListener> listeners = LISTENERS.get(world);
        if (listeners == null) {
            listeners = new ArrayList<GameEventListener>();
            LISTENERS.put(world, listeners);
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public static synchronized void unregister(World world, GameEventListener listener) {
        List<GameEventListener> listeners = LISTENERS.get(world);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                LISTENERS.remove(world);
            }
        }
    }

    public static void emit(World world, RetroGameEvent event, Vec3d sourcePosition, GameEventContext context) {
        if (!(world instanceof WorldServer) || event == null || sourcePosition == null) {
            return;
        }

        List<GameEventListener> snapshot = getListeners(world);
        double radiusSq = event.getNotificationRadius() * event.getNotificationRadius();
        for (GameEventListener listener : snapshot) {
            Vec3d listenerPos = listener.getListenerPosition();
            int listenerRadius = listener.getListenerRadius();
            double effectiveRadius = Math.min(event.getNotificationRadius(), listenerRadius);
            double effectiveRadiusSq = effectiveRadius * effectiveRadius;
            if (listenerPos != null
                    && sourcePosition.squareDistanceTo(listenerPos) <= radiusSq
                    && sourcePosition.squareDistanceTo(listenerPos) <= effectiveRadiusSq) {
                listener.handleGameEvent((WorldServer) world, event,
                        context == null ? GameEventContext.empty() : context, sourcePosition);
            }
        }
    }

    public static synchronized void clear(World world) {
        LISTENERS.remove(world);
    }

    private static synchronized List<GameEventListener> getListeners(World world) {
        List<GameEventListener> listeners = LISTENERS.get(world);
        if (listeners == null) {
            return new ArrayList<GameEventListener>();
        }

        Iterator<GameEventListener> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == null) {
                iterator.remove();
            }
        }
        return new ArrayList<GameEventListener>(listeners);
    }
}
