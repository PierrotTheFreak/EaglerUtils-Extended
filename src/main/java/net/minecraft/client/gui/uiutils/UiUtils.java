package net.minecraft.client.gui.uiutils;

import net.lax1dude.eaglercraft.EagRuntime;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.util.text.ITextComponent;

public class UiUtils {
    private static final int PANEL_X = 6;
    private static final int PANEL_Y = 6;
    private static final int PANEL_WIDTH = 210;
    private static final int PANEL_PADDING = 6;
    private static final int CONTROL_X = PANEL_X + PANEL_PADDING;
    private static final int CONTROL_WIDTH = PANEL_WIDTH - PANEL_PADDING * 2;
    private static final int TITLE_HEIGHT = 18;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 3;
    private static final int BUTTON_STEP = BUTTON_HEIGHT + BUTTON_GAP;
    private static final int CHAT_GAP = 5;
    private static final int CHAT_LABEL_HEIGHT = 11;
    private static final int CHAT_INPUT_HEIGHT = 20;
    private static final int STATUS_GAP = 5;
    private static final int STATUS_HEIGHT = 24;
    private static TextFieldWidget chatInput;

    private static final String[] BUTTON_NAMES = {
            "Close Without Packet", "De-sync", "Send Packets", "Delay Packets",
            "Save GUI", "Disconnect + Send", "Fabricate Packet", "Packet Inspector",
            "Copy GUI Title JSON", "Macros"
    };

    private UiUtils() {}

    public static boolean shouldShow(Screen screen) { return screen instanceof ContainerScreen || screen instanceof ChatScreen; }

    public static void init(Screen screen) {
        chatInput = null;
        if (screen instanceof ContainerScreen) {
            int inputY = PANEL_Y + getPanelHeight() - STATUS_HEIGHT - STATUS_GAP - CHAT_INPUT_HEIGHT;
            chatInput = new TextFieldWidget(screen.mc.fontRenderer, CONTROL_X, inputY, CONTROL_WIDTH, CHAT_INPUT_HEIGHT, "Chat");
            chatInput.setMaxStringLength(256);
            chatInput.setText("");
            chatInput.setFocused(false);
        }
    }

    public static void tick() { if (chatInput != null) chatInput.tick(); }
    private static int getButtonsHeight() { return BUTTON_NAMES.length * BUTTON_STEP - BUTTON_GAP; }
    private static int getPanelHeight() { return PANEL_Y + TITLE_HEIGHT + 4 + getButtonsHeight() + CHAT_GAP + CHAT_LABEL_HEIGHT + CHAT_INPUT_HEIGHT + STATUS_GAP + STATUS_HEIGHT; }
    private static int getButtonY(int index) { return PANEL_Y + TITLE_HEIGHT + 4 + index * BUTTON_STEP; }
    private static int getChatInputY() { return PANEL_Y + TITLE_HEIGHT + 4 + getButtonsHeight() + CHAT_GAP + CHAT_LABEL_HEIGHT; }
    private static int getStatusY() { return getPanelHeight() - STATUS_HEIGHT; }

    private static boolean isInside(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
    private static boolean isButtonHovered(int mouseX, int mouseY, int index) { return isInside(mouseX, mouseY, CONTROL_X, getButtonY(index), CONTROL_WIDTH, BUTTON_HEIGHT); }

    private static void drawButton(Screen screen, FontRenderer font, String text, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean hovered = isInside(mouseX, mouseY, x, y, width, height);
        int background = hovered ? 0xFF555555 : 0xFF333333;
        int border = hovered ? 0xFFFFFFFF : 0xFF777777;
        screen.fill(x, y, x + width, y + height, background);
        screen.fill(x, y, x + width, y + 1, border);
        screen.fill(x, y + height - 1, x + width, y + height, border);
        screen.fill(x, y, x + 1, y + height, border);
        screen.fill(x + width - 1, y, x + width, y + height, border);
        int textWidth = font.getStringWidth(text);
        screen.drawString(font, text, x + (width - textWidth) / 2, y + (height - 8) / 2, 0xFFFFFF);
    }

    private static String getSendPacketsText() { return "Send Packets: " + (UiUtilsPacketManager.isSendPacketsEnabled() ? "ON" : "OFF"); }
    private static String getDelayPacketsText() { return "Delay Packets: " + (UiUtilsPacketManager.isDelayPacketsEnabled() ? "ON" : "OFF"); }

    private static void handleButtonClick(Screen screen, int index) {
        switch (index) {
            case 0: closeWithoutPacket(screen); break;
            case 1: desync(screen); break;
            case 2: UiUtilsPacketManager.toggleSendPackets(); break;
            case 3: toggleDelayPackets(screen); break;
            case 4: UiUtilsSavedGui.save(screen); break;
            case 5: disconnectAndSend(screen); break;
            case 6: if (screen.mc != null) screen.mc.displayGuiScreen(new UiUtilsFabricatePacketScreen(screen)); break;
            case 7: if (screen.mc != null) screen.mc.displayGuiScreen(new UiUtilsPacketInspectorScreen(screen)); break;
            case 8: copyGuiTitleJson(screen); break;
            case 9: if (screen.mc != null) screen.mc.displayGuiScreen(new UiUtilsMacroScreen(screen)); break;
            default: break;
        }
    }

    private static void closeWithoutPacket(Screen screen) {
        if (!(screen instanceof ContainerScreen) || screen.mc == null || screen.mc.player == null) return;
        if (screen.mc.player instanceof ClientPlayerEntity) ((ClientPlayerEntity) screen.mc.player).closeScreenAndDropStack();
    }

    public static void desync(Screen screen) {
        if (!(screen instanceof ContainerScreen) || screen.mc == null || screen.mc.player == null || !(screen.mc.player instanceof ClientPlayerEntity)) return;
        ClientPlayerEntity player = (ClientPlayerEntity) screen.mc.player;
        if (player.connection == null) return;
        CCloseWindowPacket packet = new CCloseWindowPacket(player.openContainer.windowId);
        if (!UiUtilsPacketManager.handleOutgoingPacket(packet, player.connection.getNetworkManager())) player.connection.sendPacket(packet);
    }

    private static void toggleDelayPackets(Screen screen) {
        if (screen.mc == null || screen.mc.player == null || !(screen.mc.player instanceof ClientPlayerEntity)) return;
        ClientPlayerEntity player = (ClientPlayerEntity) screen.mc.player;
        if (player.connection != null) screen.mc.displayGuiScreen(new UiUtilsPacketSettingsScreen(screen));
    }

    private static void disconnectAndSend(Screen screen) {
        if (screen.mc == null || !(screen.mc.player instanceof ClientPlayerEntity)) return;
        UiUtilsPacketManager.disconnectAndSend((ClientPlayerEntity) screen.mc.player);
    }

    private static void copyGuiTitleJson(Screen screen) {
        try {
            ITextComponent title = screen.getTitle();
            String json = ITextComponent.Serializer.toJson(title);
            EagRuntime.setClipboard(json);
        } catch (Throwable ignored) {}
    }

    public static boolean mouseClicked(Screen screen, int mouseX, int mouseY, int button) {
        if (chatInput != null && isInside(mouseX, mouseY, CONTROL_X, getChatInputY(), CONTROL_WIDTH, CHAT_INPUT_HEIGHT)) {
            chatInput.mouseClicked(mouseX, mouseY, button);
            chatInput.setFocused(true);
            return true;
        }
        for (int i = 0; i < BUTTON_NAMES.length; ++i) if (isButtonHovered(mouseX, mouseY, i) && button == 0) {
            handleButtonClick(screen, i);
            return true;
        }
        return false;
    }

    public static boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return chatInput != null && chatInput.isFocused() && chatInput.keyPressed(keyCode, scanCode, modifiers);
    }

    public static boolean charTyped(char codePoint, int modifiers) {
        return chatInput != null && chatInput.isFocused() && chatInput.charTyped(codePoint, modifiers);
    }

    public static void render(Screen screen, int mouseX, int mouseY, float partialTicks) {
        FontRenderer font = screen.mc.fontRenderer;
        int panelHeight = getPanelHeight();
        screen.fill(PANEL_X, PANEL_Y, PANEL_X + PANEL_WIDTH, PANEL_Y + panelHeight, 0xEE111111);
        screen.drawString(font, "UI Utils", CONTROL_X, PANEL_Y + 5, 0xFFFFFF);
        for (int i = 0; i < BUTTON_NAMES.length; ++i) {
            String text = BUTTON_NAMES[i];
            if (i == 2) text = getSendPacketsText();
            if (i == 3) text = getDelayPacketsText();
            drawButton(screen, font, text, CONTROL_X, getButtonY(i), CONTROL_WIDTH, BUTTON_HEIGHT, mouseX, mouseY);
        }
        screen.drawString(font, "Chat", CONTROL_X, getChatInputY() - CHAT_LABEL_HEIGHT, 0xAAAAAA);
        if (chatInput != null) chatInput.render(mouseX, mouseY, partialTicks);
        screen.fill(CONTROL_X, getStatusY(), CONTROL_X + CONTROL_WIDTH, getStatusY() + STATUS_HEIGHT, 0xFF222222);
        screen.drawString(font, "Delayed: " + (UiUtilsPacketManager.getDelayedPacketCount() + UiUtilsPacketManager.getDelayedIncomingPacketCount()), CONTROL_X + 5, getStatusY() + 5, 0xFFFFFF);
        screen.drawString(font, "Inspector: " + UiUtilsPacketInspector.getEntries().size(), CONTROL_X + 5, getStatusY() + 14, 0xAAAAAA);
    }
}