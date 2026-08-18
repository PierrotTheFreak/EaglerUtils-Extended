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

    private static final Queue<IPacket<?>> delayedPackets =
            new LinkedList<>();

    /*
     * Packet classes selected for Delay Packets.
     *
     * Starts EMPTY.
     */
    private static final Set<Class<? extends IPacket<?>>>
            delayedPacketTypes =
            new LinkedHashSet<>();

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
        boolean wasEnabled =
                delayPackets;

        delayPackets =
                enabled;

        if (wasEnabled && !enabled) {
            flush(networkManager);
        }
    }

    public static boolean toggleDelayPackets(
            NetworkManager networkManager
    ) {
        delayPackets =
                !delayPackets;

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
     * Returns every packet registered by the actual 1.14 PLAY
     * protocol as SERVERBOUND.
     *
     * No hard-coded packet list.
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

        List<Class<? extends IPacket<?>>> classes =
                getAllPacketClasses();

        String[] result =
                new String[classes.size()];

        for (int i = 0; i < classes.size(); ++i) {
            result[i] =
                    classes.get(i).getSimpleName();
        }

        return result;
    }

    /**
     * Finds a registered packet class by its simple name.
     */
    private static Class<? extends IPacket<?>>
    findPacketClass(
            String packetName
    ) {

        for (
                Class<? extends IPacket<?>> packetClass
                        : getAllPacketClasses()
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
     * Selection
     * ------------------------------------------------------------
     */

    public static boolean isPacketDelayed(
            String packetName
    ) {

        Class<? extends IPacket<?>> packetClass =
                findPacketClass(packetName);

        return packetClass != null
                && delayedPacketTypes.contains(
                packetClass
        );
    }

    public static void setPacketDelayed(
            String packetName,
            boolean delayed
    ) {

        Class<? extends IPacket<?>> packetClass =
                findPacketClass(packetName);

        if (packetClass == null) {
            return;
        }

        if (isKeepAlivePacket(packetClass)) {
            delayedPacketTypes.remove(
                    packetClass
            );
            return;
        }

        if (delayed) {
            delayedPacketTypes.add(
                    packetClass
            );
        } else {
            delayedPacketTypes.remove(
                    packetClass
            );
        }
    }

    public static boolean togglePacketDelayed(
            String packetName
    ) {

        Class<? extends IPacket<?>> packetClass =
                findPacketClass(packetName);

        if (packetClass == null) {
            return false;
        }

        if (isKeepAlivePacket(packetClass)) {
            return false;
        }

        if (delayedPacketTypes.contains(
                packetClass
        )) {
            delayedPacketTypes.remove(
                    packetClass
            );

            return false;
        }

        delayedPacketTypes.add(
                packetClass
        );

        return true;
    }

    /**
     * Select every registered PLAY SERVERBOUND packet except
     * CKeepAlivePacket.
     */
    public static void delayAllPackets() {

        delayedPacketTypes.clear();

        for (
                Class<? extends IPacket<?>> packetClass
                        : getAllPacketClasses()
        ) {

            if (!isKeepAlivePacket(
                    packetClass
            )) {
                delayedPacketTypes.add(
                        packetClass
                );
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

    public static boolean isKeepAlivePacket(
            Class<? extends IPacket<?>> packetClass
    ) {

        return "CKeepAlivePacket".equals(
                packetClass.getSimpleName()
        );
    }

    public static boolean isKeepAlivePacket(
            String packetName
    ) {

        return "CKeepAlivePacket".equals(
                packetName
        );
    }

    /*
     * ------------------------------------------------------------
     * Packet handling
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

    public static boolean shouldDelayPacket(
            IPacket<?> packet
    ) {

        if (packet == null) {
            return false;
        }

        Class<? extends IPacket<?>> packetClass =
                packet.getClass();

        if (isKeepAlivePacket(
                packetClass
        )) {
            return false;
        }/

        return delayedPacketTypes.contains(
                packetClass
        );
    }

    /**
     * Handles packets at the current UI Utils interception points.
     *
     * Selected packets are delayed.
     * Unselected packets are allowed through.
     * Keep-alive is always allowed through.
     */
    public static boolean handleOutgoingPacket(
            IPacket<?> packet,
            NetworkManager networkManager
    ) {

        if (packet == null) {
            return false;
        }

        if (isKeepAlivePacket(
                packet.getClass()
        )) {
            return false;
        }

        /*
         * Send Packets OFF only suppresses packets that are
         * selected in the UI.
         */
        if (
                !sendPackets
                        && shouldDelayPacket(packet)
        ) {
            return true;
        }

        /*
         * Delay only selected packet classes.
         */
        if (
                delayPackets
                        && shouldDelayPacket(packet)
        ) {
            delayedPackets.add(
                    packet
            );

            return true;
        }

        return false;
    }

    /*
     * ------------------------------------------------------------
     * Queue
     * ------------------------------------------------------------
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
             * This is intentionally a direct send so the packet
             * doesn't immediately get queued again.
             */
            networkManager.sendPacket(
                    packet
            );
        }
    }

    public static void clearQueue() {
        delayedPackets.clear();
    }

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

        flush(networkManager);

        clearQueue();

        networkManager.closeChannel(
                new StringTextComponent(
                        "UI Utils: Disconnect and Send"
                )
        );
    }


    public static Set<String> getSelectedPacketTypes() {
        Set<String> result =
                new LinkedHashSet<>();
    
        for (
                String packet :
                        getAllPacketTypes()
        ) {
            if (isPacketDelayed(packet)) {
                result.add(packet);
            }
        }
    
        return result;
    }








    /*
     * ------------------------------------------------------------
     * Search
     * ------------------------------------------------------------
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