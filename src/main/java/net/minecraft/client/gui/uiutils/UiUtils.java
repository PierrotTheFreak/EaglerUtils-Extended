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

    /*
     * ------------------------------------------------------------
     * Main panel layout
     * ------------------------------------------------------------
     */

    private static final int PANEL_X = 6;
    private static final int PANEL_Y = 6;

    private static final int PANEL_WIDTH = 210;
    private static final int PANEL_PADDING = 6;

    private static final int CONTROL_X =
            PANEL_X + PANEL_PADDING;

    private static final int CONTROL_WIDTH =
            PANEL_WIDTH - PANEL_PADDING * 2;

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

    /*
     * ------------------------------------------------------------
     * Chat input
     * ------------------------------------------------------------
     */

    private static TextFieldWidget chatInput;

    /*
     * ------------------------------------------------------------
     * Buttons
     * ------------------------------------------------------------
     */

    private static final String[] BUTTON_NAMES = {
            "Close Without Packet",
            "De-sync",
            "Send Packets",
            "Delay Packets",
            "Save GUI",
            "Disconnect + Send",
            "Fabricate Packet",
            "Copy GUI Title JSON"
    };

    private UiUtils() {
    }

    /*
     * ------------------------------------------------------------
     * Visibility
     * ------------------------------------------------------------
     */

    public static boolean shouldShow(Screen screen) {
        return screen instanceof ContainerScreen
                || screen instanceof ChatScreen;
    }

    /*
     * ------------------------------------------------------------
     * Initialization
     * ------------------------------------------------------------
     */

    public static void init(Screen screen) {
        chatInput = null;

        /*
         * The chat field is only needed while a container GUI
         * is open. Keeping it out of ChatScreen prevents us from
         * duplicating Minecraft's normal chat input.
         */
        if (screen instanceof ContainerScreen) {
            int panelHeight =
                    getPanelHeight();

            int inputY =
                    PANEL_Y
                            + panelHeight
                            - STATUS_HEIGHT
                            - STATUS_GAP
                            - CHAT_INPUT_HEIGHT;

            chatInput = new TextFieldWidget(
                    screen.mc.fontRenderer,
                    CONTROL_X,
                    inputY,
                    CONTROL_WIDTH,
                    CHAT_INPUT_HEIGHT,
                    new net.minecraft.util.text.StringTextComponent(
                            "Chat"
                    )
            );

            chatInput.setMaxStringLength(256);
            chatInput.setText("");
            chatInput.setFocused(false);
        }
    }

    public static void tick() {
        if (chatInput != null) {
            chatInput.tick();
        }
    }

    /*
     * ------------------------------------------------------------
     * Panel dimensions
     * ------------------------------------------------------------
     */

    private static int getButtonsHeight() {
        return BUTTON_NAMES.length * BUTTON_STEP
                - BUTTON_GAP;
    }

    private static int getPanelHeight() {
        return PANEL_Y
                + TITLE_HEIGHT
                + 4
                + getButtonsHeight()
                + CHAT_GAP
                + CHAT_LABEL_HEIGHT
                + CHAT_INPUT_HEIGHT
                + STATUS_GAP
                + STATUS_HEIGHT;
    }

    private static int getButtonY(int index) {
        return PANEL_Y
                + TITLE_HEIGHT
                + 4
                + index * BUTTON_STEP;
    }

    private static int getChatInputY() {
        return PANEL_Y
                + TITLE_HEIGHT
                + 4
                + getButtonsHeight()
                + CHAT_GAP
                + CHAT_LABEL_HEIGHT;
    }

    private static int getStatusY() {
        return getPanelHeight()
                - STATUS_HEIGHT;
    }

    /*
     * ------------------------------------------------------------
     * Button helpers
     * ------------------------------------------------------------
     */

    private static boolean isInside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x
                && mouseX < x + width
                && mouseY >= y
                && mouseY < y + height;
    }

    private static boolean isButtonHovered(
            int mouseX,
            int mouseY,
            int index
    ) {
        return isInside(
                mouseX,
                mouseY,
                CONTROL_X,
                getButtonY(index),
                CONTROL_WIDTH,
                BUTTON_HEIGHT
        );
    }

    private static void drawButton(
            Screen screen,
            FontRenderer font,
            String text,
            int x,
            int y,
            int width,
            int height,
            int mouseX,
            int mouseY
    ) {
        boolean hovered =
                isInside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        width,
                        height
                );

        int background =
                hovered
                        ? 0xFF555555
                        : 0xFF333333;

        int border =
                hovered
                        ? 0xFFFFFFFF
                        : 0xFF777777;

        /*
         * Background
         */
        screen.fill(
                x,
                y,
                x + width,
                y + height,
                background
        );

        /*
         * Top border
         */
        screen.fill(
                x,
                y,
                x + width,
                y + 1,
                border
        );

        /*
         * Bottom border
         */
        screen.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                border
        );

        /*
         * Left border
         */
        screen.fill(
                x,
                y,
                x + 1,
                y + height,
                border
        );

        /*
         * Right border
         */
        screen.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                border
        );

        /*
         * Centered button text
         */
        int textWidth =
                font.getStringWidth(text);

        screen.drawString(
                font,
                text,
                x + (width - textWidth) / 2,
                y + (height - 8) / 2,
                0xFFFFFF
        );
    }

    /*
     * ------------------------------------------------------------
     * Button text
     * ------------------------------------------------------------
     */

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

    /*
     * ------------------------------------------------------------
     * Button actions
     * ------------------------------------------------------------
     */

    private static void handleButtonClick(
            Screen screen,
            int index
    ) {
        switch (index) {

            case 0:
                closeWithoutPacket(screen);
                break;

            case 1:
                desync(screen);
                break;

            case 2:
                UiUtilsPacketManager
                        .toggleSendPackets();
                break;

            case 3:
                toggleDelayPackets(screen);
                break;

            case 4:
                UiUtilsSavedGui.save(screen);
                break;

            case 5:
                disconnectAndSend(screen);
                break;

            case 6:
                if (screen.mc != null) {
                    screen.mc.displayGuiScreen(
                            new UiUtilsFabricatePacketScreen(screen)
                    );
                }
                break;

            case 7:
                copyGuiTitleJson(screen);
                break;

            default:
                break;
        }
    }

    /*
     * ------------------------------------------------------------
     * Close Without Packet
     * ------------------------------------------------------------
     */

    private static void closeWithoutPacket(
            Screen screen
    ) {
        if (!(screen instanceof ContainerScreen)) {
            return;
        }

        if (
                screen.mc == null
                        || screen.mc.player == null
        ) {
            return;
        }

        if (screen.mc.player instanceof ClientPlayerEntity) {
            ((ClientPlayerEntity) screen.mc.player)
                    .closeScreenAndDropStack();
        }
    }

    /*
     * ------------------------------------------------------------
     * De-sync
     * ------------------------------------------------------------
     */

    public static void desync(
            Screen screen
    ) {
        if (!(screen instanceof ContainerScreen)) {
            return;
        }

        if (
                screen.mc == null
                        || screen.mc.player == null
        ) {
            return;
        }

        if (!(screen.mc.player instanceof ClientPlayerEntity)) {
            return;
        }

        ClientPlayerEntity player =
                (ClientPlayerEntity) screen.mc.player;

        if (player.connection == null) {
            return;
        }

        CCloseWindowPacket packet =
                new CCloseWindowPacket(
                        player.openContainer.windowId
                );

        if (
                !UiUtilsPacketManager
                        .handleOutgoingPacket(
                                packet,
                                player.connection
                                        .getNetworkManager()
                        )
        ) {
            player.connection.sendPacket(packet);
        }
    }

    /*
     * ------------------------------------------------------------
     * Delay Packets
     * ------------------------------------------------------------
     */

    private static void toggleDelayPackets(
            Screen screen
    ) {
        if (
                screen.mc == null
                        || screen.mc.player == null
        ) {
            return;
        }

        if (!(screen.mc.player instanceof ClientPlayerEntity)) {
            return;
        }

        ClientPlayerEntity player =
                (ClientPlayerEntity) screen.mc.player;

        if (player.connection == null) {
            return;
        }

        UiUtilsPacketManager
                .toggleDelayPackets(
                        player.connection
                                .getNetworkManager()
                );
    }

    /*
     * ------------------------------------------------------------
     * Disconnect + Send
     * ------------------------------------------------------------
     */

    private static void disconnectAndSend(
            Screen screen
    ) {
        if (
                screen.mc == null
                        || screen.mc.player == null
        ) {
            return;
        }

        if (!(screen.mc.player instanceof ClientPlayerEntity)) {
            return;
        }

        UiUtilsPacketManager.disconnectAndSend(
                (ClientPlayerEntity) screen.mc.player
        );
    }

    /*
     * ------------------------------------------------------------
     * Copy GUI title JSON
     * ------------------------------------------------------------
     */

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

    /*
     * ------------------------------------------------------------
     * Chat input access
     * ------------------------------------------------------------
     */

    public static TextFieldWidget getChatInput() {
        return chatInput;
    }

    /*
     * ------------------------------------------------------------
     * Keyboard handling
     * ------------------------------------------------------------
     */

    public static boolean keyPressed(
            Screen screen,
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (
                chatInput == null
                        || !chatInput.isFocused()
        ) {
            return false;
        }

        /*
         * Enter / Numpad Enter
         */
        if (keyCode == 257 || keyCode == 335) {

            String message =
                    chatInput.getText().trim();

            if (!message.isEmpty()) {
                screen.sendMessage(message);
                chatInput.setText("");
            }

            return true;
        }

        /*
         * Escape is allowed to remove focus from the
         * chat box rather than closing the container.
         */
        if (keyCode == 256) {
            chatInput.setFocused(false);

            if (screen.getFocused() == chatInput) {
                screen.setFocused(null);
            }

            return true;
        }

        /*
         * While the field is focused, consume the
         * keyboard event so keys like E do not reach
         * ContainerScreen and close/open things.
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
        if (
                chatInput == null
                        || !chatInput.isFocused()
        ) {
            return false;
        }

        return chatInput.charTyped(
                codePoint,
                modifiers
        );
    }

    /*
     * ------------------------------------------------------------
     * Mouse handling
     * ------------------------------------------------------------
     */

    public static boolean mouseClicked(
            Screen screen,
            double mouseX,
            double mouseY,
            int button
    ) {
        int mx = (int) mouseX;
        int my = (int) mouseY;

        /*
         * Chat field gets first priority.
         */
        if (chatInput != null) {

            if (
                    chatInput.mouseClicked(
                            mouseX,
                            mouseY,
                            button
                    )
            ) {
                chatInput.setFocused(true);
                return true;
            }
        }

        /*
         * UI Utils buttons.
         */
        if (
                button == 0
                        && screen instanceof ContainerScreen
        ) {
            for (
                    int i = 0;
                    i < BUTTON_NAMES.length;
                    ++i
            ) {
                if (
                        isButtonHovered(
                                mx,
                                my,
                                i
                        )
                ) {
                    handleButtonClick(
                            screen,
                            i
                    );

                    return true;
                }
            }

            /*
             * Click the chat input area manually as a fallback.
             */
            if (
                    chatInput != null
                            && isInside(
                            mx,
                            my,
                            chatInput.x,
                            chatInput.y,
                            chatInput.getWidth(),
                            chatInput.getHeight()
                    )
            ) {
                chatInput.setFocused(true);
                return true;
            }
        }

        /*
         * Clicking somewhere else removes focus.
         */
        if (
                chatInput != null
                        && chatInput.isFocused()
        ) {
            chatInput.setFocused(false);

            if (screen.getFocused() == chatInput) {
                screen.setFocused(null);
            }
        }

        return false;
    }

    /*
     * ------------------------------------------------------------
     * Rendering
     * ------------------------------------------------------------
     */

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

        /*
         * --------------------------------------------------------
         * ContainerScreen
         * --------------------------------------------------------
         */

        if (screen instanceof ContainerScreen) {

            int panelHeight =
                    getPanelHeight();

            /*
             * Main background panel.
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
             * Header separator.
             */
            screen.fill(
                    CONTROL_X,
                    PANEL_Y + TITLE_HEIGHT,
                    CONTROL_X + CONTROL_WIDTH,
                    PANEL_Y + TITLE_HEIGHT + 1,
                    0xFF444444
            );

            /*
             * Buttons.
             */
            for (
                    int i = 0;
                    i < BUTTON_NAMES.length;
                    ++i
            ) {
                String text =
                        BUTTON_NAMES[i];

                if (i == 2) {
                    text =
                            getSendPacketsText();
                } else if (i == 3) {
                    text =
                            getDelayPacketsText();
                }

                drawButton(
                        screen,
                        font,
                        text,
                        CONTROL_X,
                        getButtonY(i),
                        CONTROL_WIDTH,
                        BUTTON_HEIGHT,
                        mouseX,
                        mouseY
                );
            }

            /*
             * Chat label.
             */
            if (chatInput != null) {
                screen.drawString(
                        font,
                        "Chat",
                        CONTROL_X,
                        chatInput.y - CHAT_LABEL_HEIGHT,
                        0xAAAAAA
                );

                /*
                 * Draw the actual text field.
                 */
                chatInput.render(
                        (int) mouseX,
                        (int) mouseY,
                        partialTicks
                );
            }

            /*
             * Status separator.
             */
            int statusY =
                    getStatusY();

            screen.fill(
                    CONTROL_X,
                    statusY - 3,
                    CONTROL_X + CONTROL_WIDTH,
                    statusY - 2,
                    0xFF444444
            );

            /*
             * Send status.
             */
            boolean sendEnabled =
                    UiUtilsPacketManager
                            .isSendPacketsEnabled();

            String sendState =
                    "Send: "
                            + (
                            sendEnabled
                                    ? "ON"
                                    : "OFF"
                    );

            screen.drawString(
                    font,
                    sendState,
                    CONTROL_X,
                    statusY + 2,
                    sendEnabled
                            ? 0x55FF55
                            : 0xFF5555
            );

            /*
             * Delay status.
             */
            boolean delayEnabled =
                    UiUtilsPacketManager
                            .isDelayPacketsEnabled();

            String delayState =
                    "Delay: "
                            + (
                            delayEnabled
                                    ? "ON"
                                    : "OFF"
                    );

            screen.drawString(
                    font,
                    delayState,
                    CONTROL_X + 92,
                    statusY + 2,
                    delayEnabled
                            ? 0xFFFF55
                            : 0xAAAAAA
            );

            /*
             * Queue count.
             */
            screen.drawString(
                    font,
                    "Queued: "
                            + UiUtilsPacketManager
                            .getDelayedPacketCount(),
                    CONTROL_X,
                    statusY + 12,
                    0xFFFFFF
            );

            /*
             * Macro UI.
             *
             * This is deliberately delegated to the macro manager
             * so UiUtils itself does not need to know how macros
             * are stored or edited.
             */
            UiUtilsMacroManager.render(
                    screen,
                    font,
                    mouseX,
                    mouseY,
                    partialTicks
            );
        }

        /*
         * --------------------------------------------------------
         * ChatScreen
         * --------------------------------------------------------
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