package net.minecraft.client.gui.uiutils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * One action inside a UI Utils macro.
 *
 * Every action executes in order.
 */
public class UiUtilsMacroAction {

    public enum Type {
        CHAT,
        WAIT,
        WAIT_FOR_PACKETS,
        DELAY_PACKETS,
        SEND_PACKETS,
        SEND_QUEUED_PACKETS,
        CLOSE_GUI,
        DESYNC
    }

    private Type type;

    /*
     * Used by CHAT.
     */
    private String text = "";

    /*
     * Used by WAIT / WAIT_FOR_PACKETS.
     */
    private int amount = 0;

    /*
     * Used by DELAY_PACKETS.
     */
    private boolean enabled = false;

    /*
     * Packet names selected specifically for this action.
     *
     * These are names from the dynamic ProtocolType registry.
     */
    private final Set<String> packetTypes =
            new LinkedHashSet<>();

    public UiUtilsMacroAction(Type type) {
        this.type = type;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
    }

    public int getAmount() {
        return this.amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getPacketTypes() {
        return this.packetTypes;
    }

    public void clearPacketTypes() {
        this.packetTypes.clear();
    }

    public void addPacketType(String packetName) {
        if (packetName != null
                && !UiUtilsPacketManager
                .isKeepAlivePacket(packetName)) {

            this.packetTypes.add(packetName);
        }
    }

    public void removePacketType(String packetName) {
        this.packetTypes.remove(packetName);
    }

    public boolean hasPacketType(String packetName) {
        return this.packetTypes.contains(packetName);
    }

    public String getDisplayText() {
        switch (this.type) {
            case CHAT:
                return "Chat: "
                        + this.text;

            case WAIT:
                return "Wait: "
                        + this.amount
                        + " ticks";

            case WAIT_FOR_PACKETS:
                return "Wait for "
                        + this.amount
                        + " packets";

            case DELAY_PACKETS:
                return "Delay Packets: "
                        + (this.enabled ? "ON" : "OFF")
                        + " ("
                        + this.packetTypes.size()
                        + " packet types)";

            case SEND_PACKETS:
                return "Send Packets: "
                        + (this.enabled ? "ON" : "OFF");

            case SEND_QUEUED_PACKETS:
                return "Send Queued Packets";

            case CLOSE_GUI:
                return "Close GUI";

            case DESYNC:
                return "De-sync";

            default:
                return this.type.name();
        }
    }
}