package net.minecraft.client.gui.uiutils;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Central packet manager for UI Utils.
 *
 * Managed packet categories currently include:
 *
 * - Container click packets
 * - Container button packets
 * - Chat / command packets
 * - Container close packets
 *
 * Container opening packets are routed through this manager at their
 * actual call sites so normal block-use packets are not accidentally
 * delayed.
 */
public class UiUtilsPacketManager {

    private static boolean sendPackets = true;
    private static boolean delayPackets = false;

    private static final Queue<IPacket<?>> delayedPackets =
            new LinkedList<>();

    private UiUtilsPacketManager() {
    }

    public static boolean isSendPacketsEnabled() {
        return sendPackets;
    }

    public static boolean isDelayPacketsEnabled() {
        return delayPackets;
    }

    public static int getDelayedPacketCount() {
        return delayedPackets.size();
    }

    public static void setSendPackets(boolean enabled) {
        sendPackets = enabled;
    }

    public static void setDelayPackets(
            boolean enabled,
            NetworkManager networkManager
    ) {
        boolean wasEnabled = delayPackets;

        delayPackets = enabled;

        /*
         * Turning Delay Packets off releases everything that was
         * waiting in the queue.
         */
        if (wasEnabled && !enabled) {
            flush(networkManager);
        }
    }

    public static boolean toggleSendPackets() {
        sendPackets = !sendPackets;
        return sendPackets;
    }

    public static boolean toggleDelayPackets(
            NetworkManager networkManager
    ) {
        delayPackets = !delayPackets;

        if (!delayPackets) {
            flush(networkManager);
        }

        return delayPackets;
    }

    /**
     * Returns true for packet classes that UI Utils can directly
     * manage.
     */
    public static boolean isManagedPacket(IPacket<?> packet) {
        return packet instanceof CClickWindowPacket
                || packet instanceof CEnchantItemPacket
                || packet instanceof CChatMessagePacket
                || packet instanceof CCloseWindowPacket;
    }

    /**
     * Handles a managed outgoing packet.
     *
     * @return true if UI Utils consumed the packet and the caller
     *         must NOT send it normally.
     */
    public static boolean handleOutgoingPacket(
            IPacket<?> packet,
            NetworkManager networkManager
    ) {
        if (!isManagedPacket(packet)) {
            return false;
        }

        /*
         * Send Packets disabled:
         *
         * Drop the packet completely.
         */
        if (!sendPackets) {
            return true;
        }

        /*
         * Delay Packets enabled:
         *
         * Store the packet instead of sending it.
         */
        if (delayPackets) {
            delayedPackets.add(packet);
            return true;
        }

        /*
         * Neither feature is active.
         *
         * Let the original caller send normally.
         */
        return false;
    }

    /**
     * Same handling method used by packet types that are only
     * managed in a specific call site, such as container-opening
     * interaction packets.
     *
     * This intentionally does not require the packet itself to be
     * one of the normal managed classes.
     */
    public static boolean handleSpecialOutgoingPacket(
            IPacket<?> packet,
            NetworkManager networkManager
    ) {
        if (!sendPackets) {
            return true;
        }

        if (delayPackets) {
            delayedPackets.add(packet);
            return true;
        }

        return false;
    }

    /**
     * Sends all delayed packets in FIFO order.
     */
    public static void flush(
            NetworkManager networkManager
    ) {
        if (networkManager == null) {
            delayedPackets.clear();
            return;
        }

        while (!delayedPackets.isEmpty()) {
            IPacket<?> packet = delayedPackets.poll();

            networkManager.sendPacket(packet);
        }
    }

    /**
     * Discards every queued packet without sending them.
     */
    public static void clearQueue() {
        delayedPackets.clear();
    }

    /**
     * Flushes the delayed packet queue and then closes the connection.
     */
    public static void disconnectAndSend(
            ClientPlayerEntity player
    ) {
        if (player == null || player.connection == null) {
            clearQueue();
            return;
        }

        NetworkManager networkManager =
                player.connection.getNetworkManager();

        if (networkManager == null) {
            clearQueue();
            return;
        }

        /*
         * Release anything waiting first.
         */
        flush(networkManager);

        clearQueue();

        /*
         * The network manager will handle the actual disconnect.
         */
        networkManager.closeChannel(
                new net.minecraft.util.text.StringTextComponent(
                        "UI Utils: Disconnect and Send"
                )
        );
    }
}