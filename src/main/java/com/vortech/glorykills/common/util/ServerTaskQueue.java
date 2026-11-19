package com.vortech.glorykills.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Cola de tareas que se ejecutan al final del tick del servidor.
 *
 * Minecraft recorre la lista de efectos de cada entidad mientras los actualiza.
 * Si dentro de ese recorrido se anade o quita un efecto (por ejemplo al expirar
 * Stimulated, o cuando el veneno hace dano) el juego crashea con
 * ConcurrentModificationException. Por eso esas acciones se aplazan aqui.
 *
 * Solo se usa desde el hilo del servidor.
 */
public final class ServerTaskQueue {

    private static final List<Runnable> PENDING = new ArrayList<>();

    public static void enqueue(Runnable task) {
        PENDING.add(task);
    }

    public static void runPending() {
        if (PENDING.isEmpty()) {
            return;
        }
        List<Runnable> tasks = new ArrayList<>(PENDING);
        PENDING.clear();
        for (Runnable task : tasks) {
            task.run();
        }
    }

    public static void clear() {
        PENDING.clear();
    }

    private ServerTaskQueue() {
    }
}
