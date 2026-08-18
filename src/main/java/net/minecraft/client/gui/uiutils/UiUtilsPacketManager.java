package net.minecraft.client.gui.uiutils;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.util.text.StringTextComponent;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Handles the packet controls used by UI Utils.
 *
 * The 1.14 workspace sends inventory slot clicks as CClickWindowPacket
 * and server-side container button clicks as CEnchantItemPacket.
 *
 * Send Packets:
 *   Controls whether those packets are allowed to leave the client.
 *
 * Delay Packets:
 *   Stores those packets in a queue instead of sending them immediately.
 *
 * When Delay Packets is switched off, the queued packets should be
 * flushed through flush().
 */
public class UiUtilsPacketManager {

    private static boolean sendPackets = true;
    private static boolean delayPackets = false;

    private static final Queue<IPacket<?>> delayedPackets = new LinkedList<>();

    private UiUtilsPacketManager() {
    }

    /**
     * Returns whether UI Utils is currently allowing GUI packets to be sent.
     */
    public static boolean isSendPacketsEnabled() {
        return sendPackets;
    }

    /**
     * Returns whether GUI packets are currently being delayed.
     */
    public static boolean isDelayPacketsEnabled() {
        return delayPackets;
    }

    /**
     * Returns the number of packets currently waiting in the queue.
     */
    public static int getDelayedPacketCount() {
        return delayedPackets.size();
    }

    /**
     * Toggles Send Packets.
     *
     * When disabled, intercepted GUI packets are discarded.
     */
    public static void setSendPackets(boolean enabled) {
        sendPackets = enabled;

        /*
         * Turning packet sending back on does not automatically flush
         * delayed packets. Delay Packets controls that behavior.
         */
    }

    /**
     * Toggles Delay Packets.
     *
     * Turning Delay Packets off immediately sends all queued packets,
     * matching UI Utils behavior.
     */
    public static void setDelayPackets(
            boolean enabled,
            NetworkManager networkManager
    ) {
        boolean wasEnabled = delayPackets;
        delayPackets = enabled;

        if (wasEnabled && !enabled) {
            flush(networkManager);
        }
    }

    /**
     * Toggles Send Packets and returns the new state.
     */
    public static boolean toggleSendPackets() {
        sendPackets = !sendPackets;
        return sendPackets;
    }

    /**
     * Toggles Delay Packets.
     *
     * If this turns the setting off, the queued packets are flushed.
     */
    public static boolean toggleDelayPackets(NetworkManager networkManager) {
        delayPackets = !delayPackets;

        if (!delayPackets) {
            flush(networkManager);
        }

        return delayPackets;
    }

    /**
     * Determines whether a packet is one of the GUI packets controlled
     * by UI Utils.
     */
    public static boolean isManagedPacket(IPacket<?> packet) {
        return packet instanceof CClickWindowPacket
                || packet instanceof CEnchantItemPacket;
    }

    /**
     * Handles an outgoing GUI packet.
     *
     * Returns true when UI Utils consumed the packet and the caller
     * should NOT send it normally.
     *
     * Returns false when the caller should continue with its normal
     * networkManager.sendPacket(...) call.
     */
    public static boolean handleOutgoingPacket(
            IPacket<?> packet,
            NetworkManager networkManager
    ) {
        if (!isManagedPacket(packet)) {
            return false;
        }

        /*
         * Send Packets = false:
         *
         * Consume the packet and do not send it.
         */
        if (!sendPackets) {
            return true;
        }

        /*
         * Delay Packets = true:
         *
         * Store it until Delay Packets is disabled or another action
         * explicitly flushes the queue.
         */
        if (delayPackets) {
            delayedPackets.add(packet);
            return true;
        }

        /*
         * Neither control is active, so let the caller send the packet
         * normally.
         */
        return false;
    }

    /**
     * Sends every delayed packet in FIFO order.
     */
    public static void flush(NetworkManager networkManager) {
        if (networkManager == null) {
            delayedPackets.clear();
            return;
        }

        while (!delayedPackets.isEmpty()) {
            IPacket<?> packet = delayedPackets.poll();

            /*
             * Use the NetworkManager directly so the packets are sent
             * exactly as they would have been normally.
             */
            networkManager.sendPacket(packet);
        }
    }

    /**
     * Removes every delayed packet without sending anything.
     */
    public static void clearQueue() {
        delayedPackets.clear();
    }

    /**
     * Disconnects from the current server after sending all delayed
     * packets.
     *
     * The queue is flushed first, then the network connection is closed.
     */
    public static void disconnectAndSend(ClientPlayerEntity player) {
        if (player == null || player.connection == null) {
            clearQueue();
            return;
        }

        NetworkManager networkManager = player.connection.getNetworkManager();

        if (networkManager == null) {
            clearQueue();
            return;
        }

        /*
         * Send any packets waiting in Delay Packets first.
         */
        flush(networkManager);

        /*
         * Clear the queue so there are no stale packets if something
         * keeps the client alive after the disconnect.
         */
        clearQueue();

        /*
         * Disconnect immediately after the queued packets have been
         * handed to the network manager.
         */
        networkManager.closeChannel(
                new StringTextComponent("UI Utils: Disconnect and Send")
        );
    }
}