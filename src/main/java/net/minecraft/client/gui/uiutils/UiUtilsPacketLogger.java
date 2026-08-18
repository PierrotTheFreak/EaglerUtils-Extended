package net.minecraft.client.gui.uiutils;

import net.lax1dude.eaglercraft.EagRuntime;
import net.minecraft.network.PacketDirection;

import java.util.List;

/** Persistent text logger for the live packet inspector. */
public final class UiUtilsPacketLogger {
    private static boolean logging;
    private static boolean logClientToServer = true;
    private static boolean logServerToClient = true;
    private static final StringBuilder BUFFER = new StringBuilder(8192);
    private static int writtenEntries;

    private UiUtilsPacketLogger() {}

    public static synchronized void setLogging(boolean value) { logging = value; }
    public static synchronized boolean isLogging() { return logging; }
    public static synchronized void toggleLogging() { logging = !logging; }
    public static synchronized void setClientToServer(boolean value) { logClientToServer = value; }
    public static synchronized void setServerToClient(boolean value) { logServerToClient = value; }
    public static synchronized boolean isClientToServerEnabled() { return logClientToServer; }
    public static synchronized boolean isServerToClientEnabled() { return logServerToClient; }

    public static synchronized void append(UiUtilsPacketInspector.Entry entry) {
        if (!logging || entry == null) return;
        if (entry.direction == PacketDirection.SERVERBOUND && !logClientToServer) return;
        if (entry.direction == PacketDirection.CLIENTBOUND && !logServerToClient) return;
        if (writtenEntries > 0) BUFFER.append('\n');
        BUFFER.append(entry.timestamp).append(" | ")
                .append(entry.direction == PacketDirection.CLIENTBOUND ? "S -> C" : "C -> S")
                .append(" | ").append(entry.name)
                .append(" | ").append(entry.size).append(" B");
        ++writtenEntries;
    }

    public static synchronized void clear() {
        BUFFER.setLength(0);
        writtenEntries = 0;
    }

    public static synchronized int getEntryCount() { return writtenEntries; }

    public static synchronized String getText() { return BUFFER.toString(); }

    /** Copies the current log to the browser/client clipboard. */
    public static synchronized void copyToClipboard() {
        try { EagRuntime.setClipboard(BUFFER.toString()); } catch (Throwable ignored) {}
    }

    /** Appends all currently captured inspector entries to the log. */
    public static synchronized void importInspectorEntries() {
        List<UiUtilsPacketInspector.Entry> entries = UiUtilsPacketInspector.getEntries();
        for (int i = entries.size() - 1; i >= 0; --i) append(entries.get(i));
    }
}
