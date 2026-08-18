package net.minecraft.client.gui.uiutils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.util.text.StringTextComponent;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Central packet state for UI Utils, including selectable directional delay queues. */
public final class UiUtilsPacketManager {
    private static boolean sendPackets=true;
    private static boolean delayPackets;
    private static final Queue<IPacket<?>> delayedPackets=new LinkedList<>();
    private static final Queue<Runnable> delayedIncomingTasks=new LinkedList<>();
    private static final Set<Class<?>> serverboundSelection=new LinkedHashSet<>();
    private static final Set<Class<?>> clientboundSelection=new LinkedHashSet<>();
    private UiUtilsPacketManager(){}
    public static boolean isSendPacketsEnabled(){return sendPackets;}
    public static boolean isDelayPacketsEnabled(){return delayPackets;}
    public static int getDelayedPacketCount(){return delayedPackets.size();}
    public static int getDelayedIncomingPacketCount(){return delayedIncomingTasks.size();}
    public static long getDelayMilliseconds(){return 0L;}
    public static void setDelayMilliseconds(long milliseconds){}
    public static void setSendPackets(boolean enabled){sendPackets=enabled;}
    public static boolean toggleSendPackets(){sendPackets=!sendPackets;return sendPackets;}
    public static void setDelayPackets(boolean enabled,NetworkManager networkManager){boolean old=delayPackets;delayPackets=enabled;if(old&&!enabled){flush(networkManager);flushIncomingPackets();}}
    public static boolean toggleDelayPackets(NetworkManager networkManager){delayPackets=!delayPackets;if(!delayPackets){flush(networkManager);flushIncomingPackets();}return delayPackets;}
    public static List<Class<? extends IPacket<?>>> getAllPacketClasses(){return getAllPacketClasses(PacketDirection.SERVERBOUND);}
    public static List<Class<? extends IPacket<?>>> getAllPacketClasses(PacketDirection direction){return ProtocolType.PLAY.getPacketClasses(direction);}
    public static String[] getAllPacketTypes(){return getAllPacketTypes(PacketDirection.SERVERBOUND);}
    public static String[] getAllPacketTypes(PacketDirection direction){List<Class<? extends IPacket<?>>> classes=getAllPacketClasses(direction);String[] result=new String[classes.size()];for(int i=0;i<classes.size();++i)result[i]=classes.get(i).getSimpleName();return result;}
    private static Class<?> findPacketClass(String name,PacketDirection direction){if(name==null)return null;for(Class<? extends IPacket<?>> c:getAllPacketClasses(direction))if(c.getSimpleName().equals(name))return c;return null;}
    private static Set<Class<?>> selection(PacketDirection direction){return direction==PacketDirection.CLIENTBOUND?clientboundSelection:serverboundSelection;}
    public static boolean isPacketDelayed(String name){return isPacketDelayed(name,PacketDirection.SERVERBOUND);}
    public static boolean isPacketDelayed(String name,PacketDirection direction){Class<?> c=findPacketClass(name,direction);return c!=null&&selection(direction).contains(c);}
    public static Set<String> getSelectedPacketTypes(){return getSelectedPacketTypes(PacketDirection.SERVERBOUND);}
    public static Set<String> getSelectedPacketTypes(PacketDirection direction){Set<String> result=new LinkedHashSet<>();for(String name:getAllPacketTypes(direction))if(isPacketDelayed(name,direction))result.add(name);return result;}
    public static void setPacketDelayed(String name,boolean delayed){setPacketDelayed(name,delayed,PacketDirection.SERVERBOUND);}
    public static void setPacketDelayed(String name,boolean delayed,PacketDirection direction){Class<?> c=findPacketClass(name,direction);if(c==null||isKeepAlivePacket(c))return;if(delayed)selection(direction).add(c);else selection(direction).remove(c);}
    public static boolean togglePacketDelayed(String name){return togglePacketDelayed(name,PacketDirection.SERVERBOUND);}
    public static boolean togglePacketDelayed(String name,PacketDirection direction){Class<?> c=findPacketClass(name,direction);if(c==null||isKeepAlivePacket(c))return false;Set<Class<?>> set=selection(direction);if(!set.add(c)){set.remove(c);return false;}return true;}
    public static void delayAllPackets(){delayAllPackets(PacketDirection.SERVERBOUND);}
    public static void delayAllPackets(PacketDirection direction){Set<Class<?>> set=selection(direction);set.clear();for(Class<? extends IPacket<?>> c:getAllPacketClasses(direction))if(!isKeepAlivePacket(c))set.add(c);}
    public static void clearDelayedPacketTypes(){clearDelayedPacketTypes(PacketDirection.SERVERBOUND);}
    public static void clearDelayedPacketTypes(PacketDirection direction){selection(direction).clear();}
    public static boolean isKeepAlivePacket(Class<?> c){return c!=null&&"CKeepAlivePacket".equals(c.getSimpleName());}
    public static boolean isKeepAlivePacket(String name){return "CKeepAlivePacket".equals(name);}
    public static String getPacketName(IPacket<?> packet){return packet==null?"":packet.getClass().getSimpleName();}
    public static boolean shouldDelayPacket(IPacket<?> packet){return shouldDelayPacket(packet,PacketDirection.SERVERBOUND);}
    public static boolean shouldDelayPacket(IPacket<?> packet,PacketDirection direction){return packet!=null&&!isKeepAlivePacket(packet.getClass())&&selection(direction).contains(packet.getClass());}
    public static boolean handleIncomingPacket(IPacket<?> packet){UiUtilsPacketInspector.record(packet,PacketDirection.CLIENTBOUND);UiUtilsPacketReplay.record(packet,PacketDirection.CLIENTBOUND);return delayPackets&&shouldDelayPacket(packet,PacketDirection.CLIENTBOUND);}
    public static boolean handleIncomingPacket(IPacket<?> packet,Runnable processLater){if(!handleIncomingPacket(packet))return false;if(processLater!=null)delayedIncomingTasks.add(processLater);return true;}
    public static IPacket<?> pollDelayedIncomingPacket(){return null;}
    public static boolean handleOutgoingPacket(IPacket<?> packet,NetworkManager networkManager){
        if(packet instanceof CChatMessagePacket && handleLocalCommand(((CChatMessagePacket)packet).getMessage(),networkManager)) return true;
        if(packet==null||isKeepAlivePacket(packet.getClass()))return false;
        UiUtilsPacketInspector.record(packet,PacketDirection.SERVERBOUND);UiUtilsPacketReplay.record(packet,PacketDirection.SERVERBOUND);
        if(!sendPackets&&shouldDelayPacket(packet))return true;
        if(delayPackets&&shouldDelayPacket(packet)){delayedPackets.add(packet);return true;}
        return false;
    }
    private static boolean handleLocalCommand(String message,NetworkManager networkManager){
        if(message==null)return false;
        String trimmed=message.trim();
        if(!trimmed.regionMatches(true,0,"/UiUtils",0,8))return false;
        String rest=trimmed.length()>8?trimmed.substring(8).trim():"";
        String[] args=rest.isEmpty()?new String[0]:rest.split("\\s+");
        String command=args.length>0?args[0].toLowerCase():"help";
        Minecraft mc=Minecraft.getInstance();
        ClientPlayerEntity player=mc.player;
        NetworkManager nm=networkManager!=null?networkManager:(player!=null&&player.connection!=null?player.connection.getNetworkManager():null);
        if("delay_packets".equals(command)){Boolean value=parseBoolean(args,1);setDelayPackets(value==null?!delayPackets:value,nm);notifyCommand("Delay Packets: "+delayPackets);return true;}
        if("send_packets".equals(command)){Boolean value=parseBoolean(args,1);setSendPackets(value==null?!sendPackets:value);notifyCommand("Send Packets: "+sendPackets);return true;}
        if("close_without_packet".equals(command)){if(player!=null)player.closeScreenAndDropStack();else mc.displayGuiScreen(null);return true;}
        if("flush_packets".equals(command)||"send_queued_packets".equals(command)){flush(nm);flushIncomingPackets();notifyCommand("Queued packets flushed.");return true;}
        if("clear_packets".equals(command)){clearQueue();notifyCommand("Packet queues cleared.");return true;}
        if("delay_all".equals(command)){delayAllPackets();delayAllPackets(PacketDirection.CLIENTBOUND);notifyCommand("All delayable packets selected.");return true;}
        if("clear_delay_selection".equals(command)){clearDelayedPacketTypes();clearDelayedPacketTypes(PacketDirection.CLIENTBOUND);notifyCommand("Delay packet selection cleared.");return true;}
        if("disconnect_and_send".equals(command)){disconnectAndSend(player);return true;}
        notifyCommand("/UiUtils delay_packets <true|false>, send_packets <true|false>, close_without_packet, flush_packets, clear_packets, delay_all, clear_delay_selection, disconnect_and_send");
        return true;
    }
    private static Boolean parseBoolean(String[] args,int index){if(args.length<=index)return null;String s=args[index].toLowerCase();if("true".equals(s)||"t".equals(s))return true;if("false".equals(s)||"f".equals(s))return false;return null;}
    private static void notifyCommand(String message){Minecraft mc=Minecraft.getInstance();if(mc.ingameGUI!=null&&mc.ingameGUI.getChatGUI()!=null)mc.ingameGUI.getChatGUI().printChatMessage(new StringTextComponent("[UiUtils] "+message));}
    public static boolean handleSpecialOutgoingPacket(IPacket<?> packet,NetworkManager networkManager){return handleOutgoingPacket(packet,networkManager);}
    public static void tick(NetworkManager networkManager){UiUtilsMacroManager.tick();}
    public static void flush(NetworkManager networkManager){if(networkManager==null){delayedPackets.clear();return;}while(!delayedPackets.isEmpty())networkManager.sendPacket(delayedPackets.poll());}
    public static void flushIncomingPackets(){while(!delayedIncomingTasks.isEmpty()){Runnable task=delayedIncomingTasks.poll();if(task!=null)task.run();}}
    public static void clearQueue(){delayedPackets.clear();delayedIncomingTasks.clear();}
    public static void disconnectAndSend(ClientPlayerEntity player){if(player==null||player.connection==null){clearQueue();return;}NetworkManager nm=player.connection.getNetworkManager();if(nm==null){clearQueue();return;}flush(nm);flushIncomingPackets();clearQueue();nm.closeChannel(new StringTextComponent("UI Utils: Disconnect and Send"));}
    public static boolean packetMatchesSearch(String packetName,String search){return packetName!=null&&(search==null||search.trim().isEmpty()||packetName.toLowerCase().contains(search.trim().toLowerCase()));}
}
