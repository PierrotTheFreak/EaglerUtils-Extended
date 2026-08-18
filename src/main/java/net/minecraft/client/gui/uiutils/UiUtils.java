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

    private static TextFieldWidget chatInput;

    /*
     * UI Utils buttons.
     *
     * We render these ourselves instead of using Minecraft's Button
     * renderer. This avoids the Eagler 1.14 widget texture behavior
     * that was causing the visible buttons to appear compressed.
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

    public static boolean shouldShow(Screen screen) {
        return screen instanceof ContainerScreen
                || screen instanceof ChatScreen;
    }

    public static void init(Screen screen) {
        chatInput = null;
    }

    /*
     * ------------------------------------------------------------
     * Button layout
     * ------------------------------------------------------------
     */

    private static int getButtonY(int index) {
        return PANEL_Y
                + TITLE_HEIGHT
                + 4
                + index * BUTTON_STEP;
    }

    private static boolean isButtonHovered(
            int mouseX,
            int mouseY,
            int index
    ) {
        int x = CONTROL_X;
        int y = getButtonY(index);

        return mouseX >= x
                && mouseX < x + CONTROL_WIDTH
                && mouseY >= y
                && mouseY < y + BUTTON_HEIGHT;
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
                mouseX >= x
                        && mouseX < x + width
                        && mouseY >= y
                        && mouseY < y + height;

        /*
         * Button background.
         */
        int backgroundColor =
                hovered
                        ? 0xFF555555
                        : 0xFF333333;

        screen.fill(
                x,
                y,
                x + width,
                y + height,
                backgroundColor
        );

        /*
         * Thin border.
         */
        int borderColor =
                hovered
                        ? 0xFFFFFFFF
                        : 0xFF777777;

        screen.fill(
                x,
                y,
                x + width,
                y + 1,
                borderColor
        );

        screen.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                borderColor
        );

        screen.fill(
                x,
                y,
                x + 1,
                y + height,
                borderColor
        );

        screen.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                borderColor
        );

        /*
         * Center text.
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
                UiUtilsPacketManager.toggleSendPackets();
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
     * Container actions
     * ------------------------------------------------------------
     */

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

    /*
     * ------------------------------------------------------------
     * Chat field
     * ------------------------------------------------------------
     */

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
         * Enter / Numpad Enter.
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
         * The text field owns all other keyboard input while focused.
         * This prevents keys like E from falling through to Screen.
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
        /*
         * Chat input gets first priority.
         */
        if (chatInput != null) {
            if (chatInput.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            )) {
                return true;
            }
        }

        /*
         * Only UI Utils left click is handled here.
         */
        if (button == 0
                && screen instanceof ContainerScreen) {

            int mx = (int) mouseX;
            int my = (int) mouseY;

            for (int i = 0;
                 i < BUTTON_NAMES.length;
                 ++i) {

                if (isButtonHovered(
                        mx,
                        my,
                        i
                )) {
                    handleButtonClick(
                            screen,
                            i
                    );

                    return true;
                }
            }
        }

        /*
         * Clicking outside the chat field removes its focus.
         */
        if (chatInput != null
                && chatInput.isFocused()) {

            chatInput.setFocused(false);

            if (screen.getFocused()
                    == chatInput) {
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
         * Container UI Utils.
         */
        if (screen instanceof ContainerScreen) {

            int buttonsHeight =
                    BUTTON_NAMES.length
                            * BUTTON_STEP
                            - BUTTON_GAP;

            int chatHeight =
                    CHAT_GAP
                            + CHAT_LABEL_HEIGHT
                            + CHAT_INPUT_HEIGHT;

            int panelHeight =
                    PANEL_Y
                            + TITLE_HEIGHT
                            + 4
                            + buttonsHeight
                            + chatHeight
                            + STATUS_GAP
                            + STATUS_HEIGHT;

            /*
             * Main panel.
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
             * Separator.
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
            for (int i = 0;
                 i < BUTTON_NAMES.length;
                 ++i) {

                String text =
                        BUTTON_NAMES[i];

                if (i == 2) {
                    text = getSendPacketsText();
                }

                if (i == 3) {
                    text = getDelayPacketsText();
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
            }

            /*
             * Status separator.
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
             * Status line 1.
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
                    CONTROL_X + 92,
                    statusY + 2,
                    UiUtilsPacketManager
                            .isDelayPacketsEnabled()
                            ? 0xFFFF55
                            : 0xAAAAAA
            );

            /*
             * Queue status.
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
        }

        /*
         * ChatScreen gets only the small header.
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