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
     * Starts completely empty.
     *
     * Class<?> is intentional here. In this workspace,
     * packet.getClass() does not cleanly produce
     * Class<? extends IPacket<?>>.
     */
    private static final Set<Class<?>> delayedPacketTypes =
            new LinkedHashSet<>();

    private static final Set<Class<?>> delayedServerboundPacketTypes =
            delayedPacketTypes;

    private static final Set<Class<?>> delayedClientboundPacketTypes =
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

    public static void setSendPackets(boolean enabled) {
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
     * This is dynamically obtained from the protocol registry.
     */
    public static List<Class<? extends IPacket<?>>>
    getAllPacketClasses() {
        return getAllPacketClasses(PacketDirection.SERVERBOUND);
    }

    public static List<Class<? extends IPacket<?>>>
    getAllPacketClasses(PacketDirection direction) {
        return ProtocolType.PLAY.getPacketClasses(direction);
    }

    /**
     * Returns the dynamically discovered packet names.
     */
    public static String[] getAllPacketTypes() {
        return getAllPacketTypes(PacketDirection.SERVERBOUND);
    }

    public static String[] getAllPacketTypes(PacketDirection direction) {
        List<Class<? extends IPacket<?>>> packetClasses =
                getAllPacketClasses(direction);
        String[] result = new String[packetClasses.size()];
        for (int i = 0; i < packetClasses.size(); ++i) {
            result[i] = packetClasses.get(i).getSimpleName();
        }
        return result;
    }

    private static Class<?> findPacketClass(
            String packetName, PacketDirection direction
    ) {
        if (packetName == null) return null;
        for (Class<? extends IPacket<?>> packetClass
                : getAllPacketClasses(direction)) {
            if (packetClass.getSimpleName().equals(packetName)) {
                return packetClass;
            }
        }
        return null;
    }

    private static Set<Class<?>> getSelection(PacketDirection direction) {
        return direction == PacketDirection.CLIENTBOUND
                ? delayedClientboundPacketTypes
                : delayedServerboundPacketTypes;
    }

    /*
     * ------------------------------------------------------------
     * Packet selection
     * ------------------------------------------------------------
     */

    /**
     * Returns whether a packet type is currently selected.
     */
    public static boolean isPacketDelayed(String packetName) {
        return isPacketDelayed(packetName, PacketDirection.SERVERBOUND);
    }

    public static boolean isPacketDelayed(
            String packetName, PacketDirection direction
    ) {
        Class<?> packetClass = findPacketClass(packetName, direction);
        return packetClass != null && getSelection(direction).contains(packetClass);
    }

    public static Set<String> getSelectedPacketTypes() {
        return getSelectedPacketTypes(PacketDirection.SERVERBOUND);
    }

    public static Set<String> getSelectedPacketTypes(PacketDirection direction) {
        Set<String> result = new LinkedHashSet<>();
        for (String packet : getAllPacketTypes(direction)) {
            if (isPacketDelayed(packet, direction)) result.add(packet);
        }
        return result;
    }

    public static void setPacketDelayed(String packetName, boolean delayed) {
        setPacketDelayed(packetName, delayed, PacketDirection.SERVERBOUND);
    }

    public static void setPacketDelayed(
            String packetName, boolean delayed, PacketDirection direction
    ) {
        Class<?> packetClass = findPacketClass(packetName, direction);
        if (packetClass == null || isKeepAlivePacket(packetClass)) return;
        if (delayed) getSelection(direction).add(packetClass);
        else getSelection(direction).remove(packetClass);
    }

    public static boolean togglePacketDelayed(String packetName) {
        return togglePacketDelayed(packetName, PacketDirection.SERVERBOUND);
    }

    public static boolean togglePacketDelayed(
            String packetName, PacketDirection direction
    ) {
        Class<?> packetClass = findPacketClass(packetName, direction);
        if (packetClass == null || isKeepAlivePacket(packetClass)) return false;
        Set<Class<?>> selection = getSelection(direction);
        if (selection.contains(packetClass)) {
            selection.remove(packetClass);
            return false;
        }
        selection.add(packetClass);
        return true;
    }

    public static void delayAllPackets() {
        delayAllPackets(PacketDirection.SERVERBOUND);
    }

    public static void delayAllPackets(PacketDirection direction) {
        Set<Class<?>> selection = getSelection(direction);
        selection.clear();
        for (Class<? extends IPacket<?>> packetClass : getAllPacketClasses(direction)) {
            if (!isKeepAlivePacket(packetClass)) selection.add(packetClass);
        }
    }

    public static void clearDelayedPacketTypes() {
        clearDelayedPacketTypes(PacketDirection.SERVERBOUND);
    }

    public static void clearDelayedPacketTypes(PacketDirection direction) {
        getSelection(direction).clear();
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
     * Returns whether this actual packet instance is selected
     * for delay.
     */
    public static boolean shouldDelayPacket(
            IPacket<?> packet
    ) {
        return shouldDelayPacket(packet, PacketDirection.SERVERBOUND);
    }

    public static boolean shouldDelayPacket(
            IPacket<?> packet, PacketDirection direction
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

        return getSelection(direction).contains(packetClass);
    }

    /**
     * Handles a packet received from the server. Returns true when the
     * packet was consumed and should be processed later.
     */
    public static boolean handleIncomingPacket(IPacket<?> packet) {
        if (packet == null) return false;
        if (isKeepAlivePacket(packet.getClass())) return false;
        if (!delayPackets || !shouldDelayPacket(packet, PacketDirection.CLIENTBOUND)) {
            return false;
        }
        delayedIncomingPackets.add(packet);
        return true;
    }

    private static final Queue<IPacket<?>> delayedIncomingPackets =
            new LinkedList<>();

    public static int getDelayedIncomingPacketCount() {
        return delayedIncomingPackets.size();
    }

    public static IPacket<?> pollDelayedIncomingPacket() {
        return delayedIncomingPackets.poll();
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
        if (isKeepAlivePacket(packet.getClass())) {
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
     * Uses the same selection/delay logic as normal packets.
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
             * Send through the NetworkManager entry point.
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
                        "UI Utils: Disconnect and Send"
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
        if (packetName == null) {
            return false;
        }

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