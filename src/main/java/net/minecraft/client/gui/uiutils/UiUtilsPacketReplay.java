package net.minecraft.client.gui.uiutils;

import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Records packet metadata and packet objects for controlled replay. */
public final class UiUtilsPacketReplay {
    public static final class Record {
        public final String name;
        public final PacketDirection direction;
        public final long timestamp;
        public final int size;
        private final IPacket<?> packet;

        private Record(IPacket<?> packet, PacketDirection direction, int size) {
            this.packet = packet;
            this.name = UiUtilsPacketManager.getPacketName(packet);
            this.direction = direction;
            this.timestamp = System.currentTimeMillis();
            this.size = size;
        }

        public IPacket<?> getPacket() { return packet; }
    }

    private static final List<Record> records = new ArrayList<>();
    private static boolean recording;
    private static int maxRecords = 1000;

    private UiUtilsPacketReplay() {}
    public static synchronized boolean isRecording() { return recording; }
    public static synchronized void setRecording(boolean value) { recording = value; }
    public static synchronized void toggleRecording() { recording = !recording; }
    public static synchronized void clear() { records.clear(); }
    public static synchronized int size() { return records.size(); }
    public static synchronized List<Record> getRecords() { return Collections.unmodifiableList(new ArrayList<>(records)); }
    public static synchronized void setMaxRecords(int max) { maxRecords = Math.max(1, max); trim(); }

    public static synchronized void record(IPacket<?> packet, PacketDirection direction) {
        if (!recording || packet == null) return;
        records.add(new Record(packet, direction, estimateSize(packet)));
        trim();
    }

    private static int estimateSize(IPacket<?> packet) {
        try { return String.valueOf(packet).length(); } catch (Throwable ignored) { return 0; }
    }

    private static void trim() { while (records.size() > maxRecords) records.remove(0); }

    /** Replays only recorded client-to-server packets. */
    public static synchronized int replayOutgoing(NetworkManager networkManager) {
        if (networkManager == null) return 0;
        int sent = 0;
        for (Record record : records) {
            if (record.direction == PacketDirection.SERVERBOUND && record.packet != null) {
                networkManager.sendPacket(record.packet);
                ++sent;
            }
        }
        return sent;
    }
}
