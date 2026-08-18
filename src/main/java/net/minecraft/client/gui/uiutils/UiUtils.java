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
import net.minecraft.client.gui.uiutils.UiUtilsSavedGui;
import net.minecraft.client.gui.uiutils.UiUtilsFabricatePacketScreen;

public class UiUtils {

    private static final int PANEL_X = 4;
    private static final int PANEL_Y = 4;
    private static final int PANEL_WIDTH = 150;

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

        /*
         * UI Utils container controls.
         */
        if (screen instanceof ContainerScreen) {
            int y = PANEL_Y + 18;

            addButton(screen, "Close Without Packet", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    closeWithoutPacket(screen);
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "De-sync", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    desync(screen);
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Send Packets", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    UiUtilsPacketManager.toggleSendPackets();
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Delay Packets", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    toggleDelayPackets(screen);
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Save GUI", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    UiUtilsSavedGui.save(screen);
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Disconnect + Send", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    disconnectAndSend(screen);
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Fabricate Packet", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    screen.mc.displayGuiScreen(
                            new UiUtilsFabricatePacketScreen(screen)
                    );
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Copy GUI Title JSON", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    copyGuiTitleJson(screen);
                }
            });
            y += BUTTON_SPACING;

            /*
             * Chat input.
             */
            y += CHAT_INPUT_SPACING;

            chatInput = new TextFieldWidget(
                    screen.mc.fontRenderer,
                    PANEL_X + 2,
                    y,
                    PANEL_WIDTH - 4,
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
                        PANEL_WIDTH,
                        BUTTON_HEIGHT,
                        PANEL_X,
                        y,
                        text,
                        action
                )
        );
    }

    /**
     * Closes the current container locally without sending CCloseWindowPacket.
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
     * Sends CCloseWindowPacket to the server while keeping the current
     * container GUI open on the client.
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
     * Toggles packet delaying. Turning it off flushes the packet queue.
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
     * Sends queued packets and then closes the network connection.
     */
    private static void disconnectAndSend(Screen screen) {
        if (screen.mc == null || screen.mc.player == null) {
            return;
        }

        UiUtilsPacketManager.disconnectAndSend(screen.mc.player);
    }

    /**
     * Serializes the current GUI title using the workspace's native
     * ITextComponent JSON serializer and copies it to the Eaglercraft
     * clipboard.
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

            String json = ITextComponent.Serializer.toJson(title);

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

        return chatInput.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
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
        if (chatInput != null) {
            if (chatInput.mouseClicked(
                    mouseX,
                    mouseY,
                    button
            )) {
                return true;
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

        int buttonCount = 0;

        if (screen instanceof ContainerScreen) {
            buttonCount = 8;
        }

        int panelHeight =
                22
                + (buttonCount * BUTTON_SPACING);

        if (screen instanceof ContainerScreen && chatInput != null) {
            panelHeight += CHAT_INPUT_SPACING + CHAT_INPUT_HEIGHT;
        }

        if (screen instanceof ContainerScreen) {
            panelHeight += STATUS_HEIGHT;
        }

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
                PANEL_X + 6,
                PANEL_Y + 6,
                0xFFFFFF
        );

        /*
         * Container status.
         */
        if (screen instanceof ContainerScreen) {
            int statusY = PANEL_Y + panelHeight - STATUS_HEIGHT + 2;

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
                    PANEL_X + 4,
                    statusY,
                    UiUtilsPacketManager.isSendPacketsEnabled()
                            ? 0x55FF55
                            : 0xFF5555
            );

            screen.drawString(
                    font,
                    delayState,
                    PANEL_X + 78,
                    statusY,
                    UiUtilsPacketManager.isDelayPacketsEnabled()
                            ? 0xFFFF55
                            : 0xAAAAAA
            );

            int queuedPackets =
                    UiUtilsPacketManager.getDelayedPacketCount();

            if (queuedPackets > 0) {
                String queued =
                        "Queued: "
                        + queuedPackets;

                screen.drawString(
                        font,
                        queued,
                        PANEL_X + 4,
                        statusY + 10,
                        0xFFFFFF
                );
            }
        }

        /*
         * Chat label.
         */
        if (chatInput != null) {
            screen.drawString(
                    font,
                    "Chat:",
                    PANEL_X + 4,
                    chatInput.y - 12,
                    0xFFFFFF
            );
        }
    }
}