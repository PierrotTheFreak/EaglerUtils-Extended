package net.minecraft.client.gui.uiutils;

import net.minecraft.network.IPacket;
import net.minecraft.network.PacketDirection;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Live packet inspector used by the UI Utils network tools. */
public final class UiUtilsPacketInspector {
    public static final int MAX_ENTRIES = 200;

    public static final class Entry {
        public final String name;
        public final PacketDirection direction;
        public final long timestamp;
        public final int size;

        private Entry(String name, PacketDirection direction, int size) {
            this.name = name;
            this.direction = direction;
            this.timestamp = System.currentTimeMillis();
            this.size = size;
        }
    }

    private static final Deque<Entry> ENTRIES = new ArrayDeque<>();
    private static boolean enabled = true;
    private static boolean paused;

    private UiUtilsPacketInspector() {}

    public static synchronized void record(IPacket<?> packet, PacketDirection direction) {
        if (!enabled || paused || packet == null) return;
        int size = estimateSize(packet);
        ENTRIES.addFirst(new Entry(UiUtilsPacketManager.getPacketName(packet), direction, size));
        while (ENTRIES.size() > MAX_ENTRIES) ENTRIES.removeLast();
    }

    private static int estimateSize(IPacket<?> packet) {
        try {
            String text = String.valueOf(packet);
            return text.length();
        } catch (Throwable ignored) {
            return 0;
        }
    }

    public static synchronized List<Entry> getEntries() {
        return new ArrayList<>(ENTRIES);
    }

    public static synchronized void clear() { ENTRIES.clear(); }
    public static boolean isEnabled() { return enabled; }
    public static void setEnabled(boolean value) { enabled = value; }
    public static boolean isPaused() { return paused; }
    public static void setPaused(boolean value) { paused = value; }
    public static void togglePaused() { paused = !paused; }
}
