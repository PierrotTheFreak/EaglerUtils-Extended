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

    /*
     * Main panel layout.
     *
     * The old 150px width was too cramped for several of the button
     * labels. Give the controls some room to breathe.
     */
    private static final int PANEL_X = 4;
    private static final int PANEL_Y = 4;
    private static final int PANEL_WIDTH = 190;

    private static final int PANEL_PADDING = 4;

    private static final int BUTTON_WIDTH = PANEL_WIDTH - (PANEL_PADDING * 2);
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 22;

    private static final int CHAT_INPUT_HEIGHT = 20;
    private static final int CHAT_INPUT_SPACING = 6;

    private static final int STATUS_HEIGHT = 24;

    private static TextFieldWidget chatInput;

    public static boolean shouldShow(Screen screen) {
        return screen instanceof ContainerScreen || screen instanceof ChatScreen;
    }

    public static void init(Screen screen) {
        chatInput = null;

        if (!shouldShow(screen)) {
            return;
        }

        if (screen instanceof ContainerScreen) {
            int y = PANEL_Y + 18;

            /*
             * Close Without Packet
             */
            addButton(screen, "Close Without Packet", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    closeWithoutPacket(screen);
                }
            });
            y += BUTTON_SPACING;

            /*
             * De-sync
             */
            addButton(screen, "De-sync", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    desync(screen);
                }
            });
            y += BUTTON_SPACING;

            /*
             * Send Packets
             */
            addButton(screen, getSendPacketsText(), y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    UiUtilsPacketManager.toggleSendPackets();
                    button.setMessage(getSendPacketsText());
                }
            });
            y += BUTTON_SPACING;

            /*
             * Delay Packets
             */
            addButton(screen, getDelayPacketsText(), y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    toggleDelayPackets(screen);
                    button.setMessage(getDelayPacketsText());
                }
            });
            y += BUTTON_SPACING;

            /*
             * Save GUI
             */
            addButton(screen, "Save GUI", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    UiUtilsSavedGui.save(screen);
                }
            });
            y += BUTTON_SPACING;

            /*
             * Disconnect + Send
             */
            addButton(screen, "Disconnect + Send", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    disconnectAndSend(screen);
                }
            });
            y += BUTTON_SPACING;

            /*
             * Fabricate Packet
             */
            addButton(screen, "Fabricate Packet", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    screen.mc.displayGuiScreen(
                            new UiUtilsFabricatePacketScreen(screen)
                    );
                }
            });
            y += BUTTON_SPACING;

            /*
             * Copy GUI Title JSON
             */
            addButton(screen, "Copy GUI Title JSON", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    copyGuiTitleJson(screen);
                }
            });
            y += BUTTON_SPACING;

            /*
             * Chat input
             */
            y += CHAT_INPUT_SPACING;

            chatInput = new TextFieldWidget(
                    screen.mc.fontRenderer,
                    PANEL_X + PANEL_PADDING,
                    y,
                    BUTTON_WIDTH,
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
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        PANEL_X + PANEL_PADDING,
                        y,
                        text,
                        action
                )
        );
    }

    private static String getSendPacketsText() {
        return "Send Packets: "
                + (UiUtilsPacketManager.isSendPacketsEnabled()
                ? "ON"
                : "OFF");
    }

    private static String getDelayPacketsText() {
        return "Delay Packets: "
                + (UiUtilsPacketManager.isDelayPacketsEnabled()
                ? "ON"
                : "OFF");
    }

    /**
     * Closes the current container locally without sending
     * CCloseWindowPacket.
     */
    private static void closeWithoutPacket(Screen screen) {
        if (!(screen instanceof ContainerScreen)) {
            return;
        }

        if (screen.mc == null || screen.mc.player == null) {
            return;
        }

        if (screen.mc.player instanceof ClientPlayerEntity) {
            ((ClientPlayerEntity) screen.mc.player).closeScreenAndDropStack();
        }
    }

    /**
     * Sends CCloseWindowPacket to the server while deliberately
     * keeping the current GUI open on the client.
     */
    private static void desync(Screen screen) {
        if (!(screen instanceof ContainerScreen)) {
            return;
        }

        if (screen.mc == null || screen.mc.player == null) {
            return;
        }

        ClientPlayerEntity player = screen.mc.player;

        player.connection.sendPacket(
                new CCloseWindowPacket(
                        player.openContainer.windowId
                )
        );
    }

    /**
     * Toggles packet delaying.
     */
    private static void toggleDelayPackets(Screen screen) {
        if (screen.mc == null || screen.mc.player == null) {
            return;
        }

        UiUtilsPacketManager.toggleDelayPackets(
                screen.mc.player.connection.getNetworkManager()
        );
    }

    /**
     * Sends queued packets and disconnects.
     */
    private static void disconnectAndSend(Screen screen) {
        if (screen.mc == null || screen.mc.player == null) {
            return;
        }

        UiUtilsPacketManager.disconnectAndSend(
                screen.mc.player
        );
    }

    /**
     * Copies the current container title to the clipboard as JSON.
     */
    private static void copyGuiTitleJson(Screen screen) {
        if (!(screen instanceof ContainerScreen)) {
            return;
        }

        if (screen.getTitle() == null) {
            return;
        }

        try {
            ITextComponent title = screen.getTitle();

            String json =
                    ITextComponent.Serializer.toJson(title);

            EagRuntime.setClipboard(json);
        } catch (Throwable throwable) {
            EagRuntime.debugPrintStackTrace(throwable);
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
        if (chatInput == null || !chatInput.isFocused()) {
            return false;
        }

        /*
         * Enter / Numpad Enter.
         */
        if (keyCode == 257 || keyCode == 335) {
            String message = chatInput.getText().trim();

            if (!message.isEmpty()) {
                screen.sendMessage(message);
                chatInput.setText("");
            }

            return true;
        }

        /*
         * Let the text field handle arrows, backspace, home/end,
         * Ctrl+A/C/V/X, etc.
         */
        chatInput.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );

        /*
         * IMPORTANT:
         *
         * TextFieldWidget.keyPressed() returns false for ordinary
         * letters such as E. We still need to consume the key while
         * the input is focused so Screen.keyPressed() does not treat
         * E as the Minecraft close-screen key.
         *
         * The actual character is handled by charTyped().
         */
        return true;
    }

    public static boolean charTyped(
            Screen screen,
            char codePoint,
            int modifiers
    ) {
        if (chatInput == null || !chatInput.isFocused()) {
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

        /*
         * First allow the text field to handle clicks inside itself.
         */
        if (chatInput.mouseClicked(
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        /*
         * We clicked somewhere else.
         *
         * Remove focus from the input so keyboard keys once again
         * belong to the normal screen.
         */
        if (chatInput.isFocused()) {
            chatInput.setFocused2(false);

            if (screen.getFocused() == chatInput) {
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

            int panelHeight =
                    22
                    + (buttonCount * BUTTON_SPACING);

            if (chatInput != null) {
                panelHeight +=
                        CHAT_INPUT_SPACING
                        + CHAT_INPUT_HEIGHT;
            }

            panelHeight += STATUS_HEIGHT;

            /*
             * Panel background.
             */
            screen.fill(
                    PANEL_X - 2,
                    PANEL_Y - 2,
                    PANEL_X + PANEL_WIDTH + 2,
                    PANEL_Y + panelHeight,
                    0xCC111111
            );

            /*
             * Title.
             */
            screen.drawString(
                    font,
                    "UI Utils",
                    PANEL_X + PANEL_PADDING,
                    PANEL_Y + 6,
                    0xFFFFFF
            );

            /*
             * Packet status.
             */
            int statusY =
                    PANEL_Y
                    + panelHeight
                    - STATUS_HEIGHT
                    + 2;

            String sendState =
                    "Send: "
                    + (UiUtilsPacketManager.isSendPacketsEnabled()
                    ? "ON"
                    : "OFF");

            String delayState =
                    "Delay: "
                    + (UiUtilsPacketManager.isDelayPacketsEnabled()
                    ? "ON"
                    : "OFF");

            screen.drawString(
                    font,
                    sendState,
                    PANEL_X + PANEL_PADDING,
                    statusY,
                    UiUtilsPacketManager.isSendPacketsEnabled()
                            ? 0x55FF55
                            : 0xFF5555
            );

            screen.drawString(
                    font,
                    delayState,
                    PANEL_X + 90,
                    statusY,
                    UiUtilsPacketManager.isDelayPacketsEnabled()
                            ? 0xFFFF55
                            : 0xAAAAAA
            );

            int queuedPackets =
                    UiUtilsPacketManager.getDelayedPacketCount();

            if (queuedPackets > 0) {
                screen.drawString(
                        font,
                        "Queued: " + queuedPackets,
                        PANEL_X + PANEL_PADDING,
                        statusY + 10,
                        0xFFFFFF
                );
            }
        }

        /*
         * Chat screen still gets the UI Utils title/panel.
         */
        else if (screen instanceof ChatScreen) {
            screen.fill(
                    PANEL_X - 2,
                    PANEL_Y - 2,
                    PANEL_X + PANEL_WIDTH + 2,
                    PANEL_Y + 26,
                    0xCC111111
            );

            screen.drawString(
                    font,
                    "UI Utils",
                    PANEL_X + PANEL_PADDING,
                    PANEL_Y + 6,
                    0xFFFFFF
            );
        }

        /*
         * Chat label for the container overlay.
         */
        if (chatInput != null) {
            screen.drawString(
                    font,
                    "Chat:",
                    PANEL_X + PANEL_PADDING,
                    chatInput.y - 12,
                    0xFFFFFF
            );
        }
    }
}