package net.minecraft.client.gui.uiutils;

import net.lax1dude.eaglercraft.EagRuntime;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.util.text.ITextComponent;


public class UiUtils {

    private static final int PANEL_X = 6;
    private static final int PANEL_Y = 6;

    private static final int PANEL_WIDTH = 210;
    private static final int PANEL_PADDING = 6;

    private static final int CONTROL_X =
            PANEL_X + PANEL_PADDING;

    private static final int CONTROL_WIDTH =
            PANEL_WIDTH - (PANEL_PADDING * 2);

    private static final int TITLE_HEIGHT = 18;

    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_GAP = 3;

    private static final int BUTTON_STEP =
            BUTTON_HEIGHT + BUTTON_GAP;

    private static final int CHAT_GAP = 5;
    private static final int CHAT_LABEL_HEIGHT = 11;
    private static final int CHAT_INPUT_HEIGHT = 20;

    private static final int STATUS_GAP = 5;
    private static final int STATUS_HEIGHT = 24;

    private static TextFieldWidget chatInput;

    public static boolean shouldShow(Screen screen) {
        return screen instanceof ContainerScreen
                || screen instanceof ChatScreen;
    }

    public static void init(Screen screen) {
        chatInput = null;

        if (!shouldShow(screen)) {
            return;
        }

        if (screen instanceof ContainerScreen) {
            int y =
                    PANEL_Y
                    + TITLE_HEIGHT
                    + 4;

            addButton(
                    screen,
                    "Close Without Packet",
                    y,
                    new Button.IPressable() {
                        @Override
                        public void onPress(Button button) {
                            closeWithoutPacket(screen);
                        }
                    }
            );
            y += BUTTON_STEP;

            addButton(
                    screen,
                    "De-sync",
                    y,
                    new Button.IPressable() {
                        @Override
                        public void onPress(Button button) {
                            desync(screen);
                        }
                    }
            );
            y += BUTTON_STEP;

            addButton(
                    screen,
                    getSendPacketsText(),
                    y,
                    new Button.IPressable() {
                        @Override
                        public void onPress(Button button) {
                            UiUtilsPacketManager.toggleSendPackets();
                            button.setMessage(
                                    getSendPacketsText()
                            );
                        }
                    }
            );
            y += BUTTON_STEP;

            addButton(
                    screen,
                    getDelayPacketsText(),
                    y,
                    new Button.IPressable() {
                        @Override
                        public void onPress(Button button) {
                            toggleDelayPackets(screen);
                            button.setMessage(
                                    getDelayPacketsText()
                            );
                        }
                    }
            );
            y += BUTTON_STEP;

            addButton(
                    screen,
                    "Save GUI",
                    y,
                    new Button.IPressable() {
                        @Override
                        public void onPress(Button button) {
                            UiUtilsSavedGui.save(screen);
                        }
                    }
            );
            y += BUTTON_STEP;

            addButton(
                    screen,
                    "Disconnect + Send",
                    y,
                    new Button.IPressable() {
                        @Override
                        public void onPress(Button button) {
                            disconnectAndSend(screen);
                        }
                    }
            );
            y += BUTTON_STEP;

            addButton(
                    screen,
                    "Fabricate Packet",
                    y,
                    new Button.IPressable() {
                        @Override
                        public void onPress(Button button) {
                            screen.mc.displayGuiScreen(
                                    new UiUtilsFabricatePacketScreen(
                                            screen
                                    )
                            );
                        }
                    }
            );
            y += BUTTON_STEP;

            addButton(
                    screen,
                    "Copy GUI Title JSON",
                    y,
                    new Button.IPressable() {
                        @Override
                        public void onPress(Button button) {
                            copyGuiTitleJson(screen);
                        }
                    }
            );

            y += BUTTON_STEP;
            y += CHAT_GAP;

            /*
             * Chat label is drawn separately.
             */
            chatInput = new TextFieldWidget(
                    screen.mc.fontRenderer,
                    CONTROL_X,
                    y + CHAT_LABEL_HEIGHT,
                    CONTROL_WIDTH,
                    CHAT_INPUT_HEIGHT,
                    "Chat"
            );

            chatInput.setMaxStringLength(256);
            chatInput.setEnableBackgroundDrawing(true);

            screen.addUiUtilsWidget(chatInput);
        }
    }

    private static void addButton(
            Screen screen,
            String text,
            int y,
            Button.IPressable action
    ) {
        screen.addUiUtilsButton(
                new Button(
                        CONTROL_WIDTH,
                        BUTTON_HEIGHT,
                        CONTROL_X,
                        y,
                        text,
                        action
                )
        );
    }

    private static String getSendPacketsText() {
        return "Send Packets: "
                + (
                UiUtilsPacketManager
                        .isSendPacketsEnabled()
                        ? "ON"
                        : "OFF"
        );
    }

    private static String getDelayPacketsText() {
        return "Delay Packets: "
                + (
                UiUtilsPacketManager
                        .isDelayPacketsEnabled()
                        ? "ON"
                        : "OFF"
        );
    }

    private static void closeWithoutPacket(
            Screen screen
    ) {
        if (!(screen instanceof ContainerScreen)) {
            return;
        }

        if (screen.mc == null
                || screen.mc.player == null) {
            return;
        }

        if (screen.mc.player instanceof ClientPlayerEntity) {
            ((ClientPlayerEntity) screen.mc.player)
                    .closeScreenAndDropStack();
        }
    }

    private static void desync(
            Screen screen
    ) {
        if (!(screen instanceof ContainerScreen)) {
            return;
        }

        if (screen.mc == null
                || screen.mc.player == null) {
            return;
        }

        ClientPlayerEntity player =
                screen.mc.player;

        CCloseWindowPacket packet =
                new CCloseWindowPacket(
                        player.openContainer.windowId
                );

        if (!UiUtilsPacketManager.handleOutgoingPacket(
                packet,
                player.connection.getNetworkManager()
        )) {
            player.connection.sendPacket(packet);
        }
    }

    private static void toggleDelayPackets(
            Screen screen
    ) {
        if (screen.mc == null
                || screen.mc.player == null) {
            return;
        }

        UiUtilsPacketManager.toggleDelayPackets(
                screen.mc.player
                        .connection
                        .getNetworkManager()
        );
    }

    private static void disconnectAndSend(
            Screen screen
    ) {
        if (screen.mc == null
                || screen.mc.player == null) {
            return;
        }

        UiUtilsPacketManager.disconnectAndSend(
                screen.mc.player
        );
    }

    private static void copyGuiTitleJson(
            Screen screen
    ) {
        if (!(screen instanceof ContainerScreen)) {
            return;
        }

        if (screen.getTitle() == null) {
            return;
        }

        try {
            ITextComponent title =
                    screen.getTitle();

            String json =
                    ITextComponent.Serializer
                            .toJson(title);

            EagRuntime.setClipboard(json);
        } catch (Throwable throwable) {
            EagRuntime.debugPrintStackTrace(
                    throwable
            );
        }
    }

    public static TextFieldWidget getChatInput() {
        return chatInput;
    }

    public static void tick() {
        if (chatInput != null) {
            chatInput.tick();
        }
    }

    public static boolean keyPressed(
            Screen screen,
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (chatInput == null
                || !chatInput.isFocused()) {
            return false;
        }

        /*
         * Enter / numpad Enter.
         */
        if (keyCode == 257
                || keyCode == 335) {

            String message =
                    chatInput.getText().trim();

            if (!message.isEmpty()) {
                screen.sendMessage(message);
                chatInput.setText("");
            }

            return true;
        }

        /*
         * Consume the key while the text field owns
         * focus so it cannot fall through to Screen.
         */
        chatInput.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );

        return true;
    }

    public static boolean charTyped(
            Screen screen,
            char codePoint,
            int modifiers
    ) {
        if (chatInput == null
                || !chatInput.isFocused()) {
            return false;
        }

        return chatInput.charTyped(
                codePoint,
                modifiers
        );
    }

    public static boolean mouseClicked(
            Screen screen,
            double mouseX,
            double mouseY,
            int button
    ) {
        if (chatInput == null) {
            return false;
        }

        if (chatInput.mouseClicked(
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        /*
         * Clicking elsewhere removes chat focus.
         */
        if (chatInput.isFocused()) {
            chatInput.setFocused(false);

            if (screen.getFocused()
                    == chatInput) {
                screen.setFocused(null);
            }
        }

        return false;
    }

    public static void render(
            Screen screen,
            FontRenderer font,
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        if (!shouldShow(screen)) {
            return;
        }

        if (screen instanceof ContainerScreen) {
            int buttonCount = 8;

            int buttonsHeight =
                    buttonCount * BUTTON_STEP
                    - BUTTON_GAP;

            int chatHeight = 0;

            if (chatInput != null) {
                chatHeight =
                        CHAT_GAP
                        + CHAT_LABEL_HEIGHT
                        + CHAT_INPUT_HEIGHT;
            }

            int panelHeight =
                    PANEL_Y
                    + TITLE_HEIGHT
                    + 4
                    + buttonsHeight
                    + chatHeight
                    + STATUS_GAP
                    + STATUS_HEIGHT;

            /*
             * Panel.
             */
            screen.fill(
                    PANEL_X,
                    PANEL_Y,
                    PANEL_X + PANEL_WIDTH,
                    panelHeight,
                    0xCC111111
            );

            /*
             * Title.
             */
            String title =
                    "UI Utils";

            int titleWidth =
                    font.getStringWidth(title);

            screen.drawString(
                    font,
                    title,
                    PANEL_X
                            + (PANEL_WIDTH - titleWidth) / 2,
                    PANEL_Y + 5,
                    0xFFFFFF
            );

            /*
             * Title separator.
             */
            screen.fill(
                    CONTROL_X,
                    PANEL_Y + TITLE_HEIGHT,
                    CONTROL_X + CONTROL_WIDTH,
                    PANEL_Y + TITLE_HEIGHT + 1,
                    0xFF444444
            );

            /*
             * Chat label.
             */
            if (chatInput != null) {
                screen.drawString(
                        font,
                        "Chat",
                        CONTROL_X,
                        chatInput.y
                                - CHAT_LABEL_HEIGHT,
                        0xAAAAAA
                );
            }

            /*
             * Bottom status separator.
             */
            int statusY =
                    panelHeight
                    - STATUS_HEIGHT;

            screen.fill(
                    CONTROL_X,
                    statusY - 3,
                    CONTROL_X + CONTROL_WIDTH,
                    statusY - 2,
                    0xFF444444
            );

            /*
             * Status text.
             */
            String sendState =
                    "Send: "
                    + (
                    UiUtilsPacketManager
                            .isSendPacketsEnabled()
                            ? "ON"
                            : "OFF"
            );

            String delayState =
                    "Delay: "
                    + (
                    UiUtilsPacketManager
                            .isDelayPacketsEnabled()
                            ? "ON"
                            : "OFF"
            );

            screen.drawString(
                    font,
                    sendState,
                    CONTROL_X,
                    statusY + 2,
                    UiUtilsPacketManager
                            .isSendPacketsEnabled()
                            ? 0x55FF55
                            : 0xFF5555
            );

            screen.drawString(
                    font,
                    delayState,
                    CONTROL_X + 90,
                    statusY + 2,
                    UiUtilsPacketManager
                            .isDelayPacketsEnabled()
                            ? 0xFFFF55
                            : 0xAAAAAA
            );

            int queued =
                    UiUtilsPacketManager
                            .getDelayedPacketCount();

            screen.drawString(
                    font,
                    "Queued: " + queued,
                    CONTROL_X,
                    statusY + 12,
                    0xFFFFFF
            );
        }

        /*
         * Small header when ChatScreen itself is open.
         */
        else if (screen instanceof ChatScreen) {
            screen.fill(
                    PANEL_X,
                    PANEL_Y,
                    PANEL_X + PANEL_WIDTH,
                    PANEL_Y + 26,
                    0xCC111111
            );

            String title =
                    "UI Utils";

            int titleWidth =
                    font.getStringWidth(title);

            screen.drawString(
                    font,
                    title,
                    PANEL_X
                            + (PANEL_WIDTH - titleWidth) / 2,
                    PANEL_Y + 7,
                    0xFFFFFF
            );
        }
    }
}