package net.minecraft.client.gui.uiutils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Creates, stores and executes UI Utils macros. */
public class UiUtilsMacroManager {
    private static final List<UiUtilsMacro> macros = new ArrayList<>();
    private static UiUtilsMacro runningMacro;
    private static int actionIndex = -1;
    private static int waitTicks = 0;
    private static int savedWaitForPackets = -1;
    private static boolean savedDelayState = false;
    private static boolean savedSendState = true;
    private static final Set<String> savedPacketTypes = new LinkedHashSet<>();

    private UiUtilsMacroManager() {}
    public static List<UiUtilsMacro> getMacros() { return Collections.unmodifiableList(macros); }

    public static void addMacro(UiUtilsMacro macro) {
        if (macro != null) {
            macros.add(macro);
            if (macro.getKeyBinding() == null) macro.clearKeyBinding();
        }
    }

    public static void removeMacro(int index) {
        if (index >= 0 && index < macros.size()) {
            if (runningMacro == macros.get(index)) stopMacro();
            macros.remove(index);
            KeyBinding.resetKeyBindingArrayAndHash();
        }
    }

    /** Called by KeyBinding.onTick() for every key press. */
    public static boolean handleKeyBinding(KeyBinding binding) {
        if (binding == null) return false;
        for (UiUtilsMacro macro : macros) {
            if (macro.getKeyBinding() == binding) {
                runMacro(macro);
                return true;
            }
        }
        return false;
    }

    public static boolean isRunning() { return runningMacro != null; }
    public static String getRunningMacroName() { return runningMacro == null ? null : runningMacro.getName(); }
    public static void runMacro(UiUtilsMacro macro) {
        if (macro == null) return;
        stopMacro();
        runningMacro = macro;
        actionIndex = 0;
        waitTicks = 0;
        savedWaitForPackets = -1;
        savedDelayState = UiUtilsPacketManager.isDelayPacketsEnabled();
        savedSendState = UiUtilsPacketManager.isSendPacketsEnabled();
        savedPacketTypes.clear();
        savedPacketTypes.addAll(UiUtilsPacketManager.getSelectedPacketTypes());
    }

    public static void stopMacro() {
        if (runningMacro == null) return;
        Minecraft mc = Minecraft.getInstance();
        UiUtilsPacketManager.clearDelayedPacketTypes();
        for (String packet : savedPacketTypes) UiUtilsPacketManager.setPacketDelayed(packet, true);
        UiUtilsPacketManager.setSendPackets(savedSendState);
        UiUtilsPacketManager.setDelayPackets(savedDelayState, mc.player == null ? null : mc.player.connection.getNetworkManager());
        runningMacro = null;
        actionIndex = -1;
        waitTicks = 0;
        savedWaitForPackets = -1;
        savedPacketTypes.clear();
    }

    public static void tick() {
        if (runningMacro == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) { stopMacro(); return; }
        if (waitTicks > 0) { --waitTicks; return; }
        if (savedWaitForPackets >= 0) {
            if (UiUtilsPacketManager.getDelayedPacketCount() < savedWaitForPackets) return;
            savedWaitForPackets = -1;
        }
        if (actionIndex < 0 || actionIndex >= runningMacro.getActions().size()) { stopMacro(); return; }
        if (executeAction(mc, runningMacro.getActions().get(actionIndex))) ++actionIndex;
    }

    public static boolean handleMouseClick(Screen screen, double mouseX, double mouseY, int button) {
        if (!UiUtils.shouldShow(screen)) return false;
        int panelWidth = 190, x = screen.width - panelWidth - 6, y = 6;
        if (button == 0 && mouseX >= x && mouseX < x + panelWidth && mouseY >= y && mouseY < y + 22) {
            screen.mc.displayGuiScreen(new UiUtilsMacroScreen(screen)); return true;
        }
        int rowY = y + 28;
        for (UiUtilsMacro macro : macros) {
            if (button == 0 && mouseX >= x + 90 && mouseX < x + 130 && mouseY >= rowY && mouseY < rowY + 20) { runMacro(macro); return true; }
            if (button == 0 && mouseX >= x + 132 && mouseX < x + 155 && mouseY >= rowY && mouseY < rowY + 20) { removeMacro(macros.indexOf(macro)); return true; }
            rowY += 22;
        }
        return false;
    }

    private static boolean executeAction(Minecraft mc, UiUtilsMacroAction action) {
        if (action == null) return true;
        switch (action.getType()) {
            case CHAT: if (mc.currentScreen != null) mc.currentScreen.sendMessage(action.getText()); else if (mc.player != null) mc.player.sendChatMessage(action.getText()); return true;
            case WAIT: waitTicks = action.getAmount(); return true;
            case WAIT_FOR_PACKETS: savedWaitForPackets = action.getAmount(); return true;
            case DELAY_PACKETS: applyMacroDelayAction(mc, action); return true;
            case SEND_PACKETS: UiUtilsPacketManager.setSendPackets(action.isEnabled()); return true;
            case SEND_QUEUED_PACKETS: if (mc.player != null) UiUtilsPacketManager.flush(mc.player.connection.getNetworkManager()); return true;
            case CLOSE_GUI: if (mc.currentScreen != null) { if (mc.player != null) mc.player.closeScreenAndDropStack(); else mc.displayGuiScreen(null); } return true;
            case DESYNC: if (mc.currentScreen instanceof net.minecraft.client.gui.screen.inventory.ContainerScreen && mc.player != null) UiUtils.desync(mc.currentScreen); return true;
            default: return true;
        }
    }

    private static void applyMacroDelayAction(Minecraft mc, UiUtilsMacroAction action) {
        UiUtilsPacketManager.clearDelayedPacketTypes();
        for (String packet : action.getPacketTypes()) UiUtilsPacketManager.setPacketDelayed(packet, true);
        UiUtilsPacketManager.setDelayPackets(action.isEnabled(), mc.player == null ? null : mc.player.connection.getNetworkManager());
    }
}