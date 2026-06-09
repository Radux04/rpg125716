package it.unicam.cs.mpgc.rpg125716.event;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class GameEventDispatcher {
    private final Set<GameEventListener> listeners = new LinkedHashSet<>();

    public GameEventDispatcher registerListener(GameEventListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener cannot be null"));
        return this;
    }

    public boolean unregisterListener(GameEventListener listener) {
        return listeners.remove(Objects.requireNonNull(listener, "listener cannot be null"));
    }

    public void dispatch(GameEvent gameEvent) {
        GameEvent safeGameEvent = Objects.requireNonNull(gameEvent, "gameEvent cannot be null");
        listeners.forEach(listener -> listener.onGameEvent(safeGameEvent));
    }
}
