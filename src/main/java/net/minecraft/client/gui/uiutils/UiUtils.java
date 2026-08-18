package net.minecraft.client.gui.uiutils;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.util.text.StringTextComponent;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;

public class UiUtilsPacketManager {

    private static boolean sendPackets = true;
    private static boolean delayPackets = false;

    /*
     * Packets currently waiting to be sent.
     */
    private static final Queue<IPacket<?>> delayedPackets =
            new LinkedList<>();

    /*
     * Packet classes selected for Delay Packets.
     *
     * IMPORTANT:
     * Starts completely empty.
     *
     * We use Class<?> instead of Class<? extends IPacket<?>> because
     * Java's generic type information for packet.getClass() is raw
     * in this workspace.
     */
    private static final Set<Class<?>> delayedPacketTypes =
            new LinkedHashSet<>();

    private UiUtilsPacketManager() {
    }

    /*
     * ------------------------------------------------------------
     * Global Send / Delay state
     * ------------------------------------------------------------
     */

    public static boolean isSendPacketsEnabled() {
        return sendPackets;
    }

    public static boolean isDelayPacketsEnabled() {
        return delayPackets;
    }

    public static int getDelayedPacketCount() {
        return delayedPackets.size();
    }

    public static void setSendPackets(
            boolean enabled
    ) {
        sendPackets = enabled;
    }

    public static boolean toggleSendPackets() {
        sendPackets = !sendPackets;
        return sendPackets;
    }

    public static void setDelayPackets(
            boolean enabled,
            NetworkManager networkManager
    ) {
        boolean wasEnabled = delayPackets;

        delayPackets = enabled;

        /*
         * Turning Delay Packets OFF releases everything waiting.
         */
        if (wasEnabled && !enabled) {
            flush(networkManager);
        }
    }

    public static boolean toggleDelayPackets(
            NetworkManager networkManager
    ) {
        delayPackets = !delayPackets;

        /*
         * Turning Delay Packets OFF flushes the queue.
         */
        if (!delayPackets) {
            flush(networkManager);
        }

        return delayPackets;
    }

    /*
     * ------------------------------------------------------------
     * Dynamic packet discovery
     * ------------------------------------------------------------
     */

    /**
     * Returns every packet registered by the actual PLAY protocol
     * as SERVERBOUND.
     *
     * This is the actual protocol registry, not a hard-coded list.
     */
    public static List<Class<? extends IPacket<?>>>
    getAllPacketClasses() {

        return ProtocolType.PLAY.getPacketClasses(
                PacketDirection.SERVERBOUND
        );
    }

    /**
     * Returns the dynamically discovered packet names.
     */
    public static String[] getAllPacketTypes() {

        List<Class<? extends IPacket<?>>> packetClasses =
                getAllPacketClasses();

        String[] result =
                new String[packetClasses.size()];

        for (int i = 0; i < packetClasses.size(); ++i) {
            result[i] =
                    packetClasses.get(i).getSimpleName();
        }

        return result;
    }

    /**
     * Finds a packet class by its simple class name.
     */
    private static Class<?> findPacketClass(
            String packetName
    ) {
        List<Class<? extends IPacket<?>>> packetClasses =
                getAllPacketClasses();

        for (
                Class<? extends IPacket<?>> packetClass
                        : packetClasses
        ) {

            if (
                    packetClass
                            .getSimpleName()
                            .equals(packetName)
            ) {
                return packetClass;
            }
        }

        return null;
    }

    /*
     * ------------------------------------------------------------
     * Packet selection
     * ------------------------------------------------------------
     */

    /**
     * Returns whether a packet type is currently selected.
     */
    public static boolean isPacketDelayed(
            String packetName
    ) {
        Class<?> packetClass =
                findPacketClass(packetName);

        return packetClass != null
                && delayedPacketTypes.contains(
                packetClass
        );
    }

    /**
     * Explicitly selects or deselects a packet.
     */
    public static void setPacketDelayed(
            String packetName,
            boolean delayed
    ) {
        Class<?> packetClass =
                findPacketClass(packetName);

        if (packetClass == null) {
            return;
        }

        /*
         * Keep-alive can NEVER be delayed.
         */
        if (isKeepAlivePacket(packetClass)) {
            delayedPacketTypes.remove(packetClass);
            return;
        }

        if (delayed) {
            delayedPacketTypes.add(packetClass);
        } else {
            delayedPacketTypes.remove(packetClass);
        }
    }

    /**
     * Toggles a packet selection.
     *
     * @return true when the packet is now selected
     */
    public static boolean togglePacketDelayed(
            String packetName
    ) {
        Class<?> packetClass =
                findPacketClass(packetName);

        if (packetClass == null) {
            return false;
        }

        /*
         * Keep-alive can NEVER be delayed.
         */
        if (isKeepAlivePacket(packetClass)) {
            return false;
        }

        if (delayedPacketTypes.contains(packetClass)) {
            delayedPacketTypes.remove(packetClass);
            return false;
        }

        delayedPacketTypes.add(packetClass);
        return true;
    }

    /**
     * Select every dynamically registered PLAY SERVERBOUND
     * packet except keep-alive.
     */
    public static void delayAllPackets() {

        delayedPacketTypes.clear();

        List<Class<? extends IPacket<?>>> packetClasses =
                getAllPacketClasses();

        for (
                Class<? extends IPacket<?>> packetClass
                        : packetClasses
        ) {

            if (!isKeepAlivePacket(packetClass)) {
                delayedPacketTypes.add(packetClass);
            }
        }
    }

    /**
     * Deselect every packet.
     */
    public static void clearDelayedPacketTypes() {
        delayedPacketTypes.clear();
    }

    /*
     * ------------------------------------------------------------
     * Keep-alive protection
     * ------------------------------------------------------------
     */

    /**
     * Checks a packet class for keep-alive.
     */
    public static boolean isKeepAlivePacket(
            Class<?> packetClass
    ) {
        return packetClass != null
                && "CKeepAlivePacket".equals(
                packetClass.getSimpleName()
        );
    }

    /**
     * Checks a packet name for keep-alive.
     */
    public static boolean isKeepAlivePacket(
            String packetName
    ) {
        return "CKeepAlivePacket".equals(
                packetName
        );
    }

    /*
     * ------------------------------------------------------------
     * Packet identification
     * ------------------------------------------------------------
     */

    public static String getPacketName(
            IPacket<?> packet
    ) {
        if (packet == null) {
            return "";
        }

        return packet.getClass()
                .getSimpleName();
    }

    /**
     * Returns whether this actual packet instance has been selected
     * for delay.
     */
    public static boolean shouldDelayPacket(
            IPacket<?> packet
    ) {
        if (packet == null) {
            return false;
        }

        Class<?> packetClass =
                packet.getClass();

        /*
         * Keep-alive always gets through.
         */
        if (isKeepAlivePacket(packetClass)) {
            return false;
        }

        return delayedPacketTypes.contains(
                packetClass
        );
    }

    /*
     * ------------------------------------------------------------
     * Outgoing packet interception
     * ------------------------------------------------------------
     */

    /**
     * Handles a packet at a UI Utils interception point.
     *
     * @return true if UI Utils consumed the packet and the caller
     *         must NOT send it normally.
     */
    public static boolean handleOutgoingPacket(
            IPacket<?> packet,
            NetworkManager networkManager
    ) {
        if (packet == null) {
            return false;
        }

        /*
         * Keep-alive is NEVER blocked or delayed.
         */
        if (
                isKeepAlivePacket(
                        packet.getClass()
                )
        ) {
            return false;
        }

        /*
         * Send Packets OFF:
         *
         * Only selected packet types are blocked.
         */
        if (
                !sendPackets
                        && shouldDelayPacket(packet)
        ) {
            return true;
        }

        /*
         * Delay Packets ON:
         *
         * Only selected packet types are queued.
         */
        if (
                delayPackets
                        && shouldDelayPacket(packet)
        ) {

            delayedPackets.add(packet);

            return true;
        }

        /*
         * Packet wasn't consumed.
         * Let the normal caller send it.
         */
        return false;
    }

    /**
     * Compatibility method for PlayerController's special
     * interaction packets.
     *
     * Right now it uses the exact same selection logic as normal
     * outgoing packets.
     */
    public static boolean handleSpecialOutgoingPacket(
            IPacket<?> packet,
            NetworkManager networkManager
    ) {
        return handleOutgoingPacket(
                packet,
                networkManager
        );
    }

    /*
     * ------------------------------------------------------------
     * Queue management
     * ------------------------------------------------------------
     */

    /**
     * Sends every queued packet in FIFO order.
     */
    public static void flush(
            NetworkManager networkManager
    ) {
        if (networkManager == null) {
            delayedPackets.clear();
            return;
        }

        while (!delayedPackets.isEmpty()) {

            IPacket<?> packet =
                    delayedPackets.poll();

            /*
             * IMPORTANT:
             *
             * NetworkManager.sendPacket() may itself be hooked later.
             * This direct path exists specifically so queue flushing
             * does not immediately put the same packet back into the
             * queue.
             *
             * For the current Eagler workspace, sendPacket() is still
             * the transport entry point, so this remains the actual
             * send call used by the workspace.
             */
            networkManager.sendPacket(packet);
        }
    }

    /**
     * Discards the current queue.
     */
    public static void clearQueue() {
        delayedPackets.clear();
    }

    /*
     * ------------------------------------------------------------
     * Disconnect + Send
     * ------------------------------------------------------------
     */

    public static void disconnectAndSend(
            ClientPlayerEntity player
    ) {

        if (
                player == null
                        || player.connection == null
        ) {
            clearQueue();
            return;
        }

        NetworkManager networkManager =
                player.connection
                        .getNetworkManager();

        if (networkManager == null) {
            clearQueue();
            return;
        }

        /*
         * Send everything currently waiting.
         */
        flush(networkManager);

        clearQueue();

        /*
         * Disconnect after flushing.
         */
        networkManager.closeChannel(
                new StringTextComponent(
                        "UI Utils: Disconnect + Send"
                )
        );
    }

    /*
     * ------------------------------------------------------------
     * Search
     * ------------------------------------------------------------
     */

    /**
     * Used by the packet settings screen.
     */
    public static boolean packetMatchesSearch(
            String packetName,
            String search
    ) {
        if (
                search == null
                        || search.trim().isEmpty()
        ) {
            return true;
        }

        return packetName
                .toLowerCase()
                .contains(
                        search.trim()
                                .toLowerCase()
                );
    }
}