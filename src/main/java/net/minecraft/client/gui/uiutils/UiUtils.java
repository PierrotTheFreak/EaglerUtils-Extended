package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;

public class UiUtils {

    private static final int PANEL_X = 4;
    private static final int PANEL_Y = 4;
    private static final int PANEL_WIDTH = 150;

    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 22;

    private static final int CHAT_INPUT_HEIGHT = 20;
    private static final int CHAT_INPUT_SPACING = 6;

    private static TextFieldWidget chatInput;

    public static boolean shouldShow(Screen screen) {
        return screen instanceof ContainerScreen || screen instanceof ChatScreen;
    }

    public static void init(Screen screen) {
        chatInput = null;

        if (!shouldShow(screen)) {
            return;
        }

        int y = PANEL_Y + 18;

        /*
         * Container buttons
         */
        if (screen instanceof ContainerScreen) {

            addButton(screen, "Close Without Packet", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    // TODO: Close current container without sending close packet
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "De-sync", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    // TODO: Implement server/client container desync
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Send Packets", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    // TODO: Implement packet sending
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Delay Packets", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    // TODO: Implement packet delay
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Save GUI", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    // TODO: Implement GUI saving
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Disconnect + Send", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    // TODO: Implement disconnect + packet sending
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Fabricate Packet", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    // TODO: Implement packet fabrication
                }
            });
            y += BUTTON_SPACING;

            addButton(screen, "Copy GUI Title JSON", y, new Button.IPressable() {
                @Override
                public void onPress(Button button) {
                    // TODO: Implement GUI title JSON copying
                }
            });
            y += BUTTON_SPACING;

            /*
             * Chat input
             */
            y += CHAT_INPUT_SPACING;

            chatInput = new TextFieldWidget(
                    screen.getFontRenderer(),
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
         * Enter / Numpad Enter
         */
        if (keyCode == 257 || keyCode == 335) {
            String message = chatInput.getText().trim();

            if (!message.isEmpty()) {
                screen.sendMessage(message);
                chatInput.setText("");
            }

            return true;
        }

        return chatInput.keyPressed(keyCode, scanCode, modifiers);
    }

    public static boolean charTyped(
            Screen screen,
            char codePoint,
            int modifiers
    ) {
        if (chatInput == null || !chatInput.isFocused()) {
            return false;
        }

        return chatInput.charTyped(codePoint, modifiers);
    }

    public static boolean mouseClicked(
            Screen screen,
            double mouseX,
            double mouseY,
            int button
    ) {
        if (chatInput != null) {
            if (chatInput.mouseClicked(mouseX, mouseY, button)) {
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

        int panelHeight = 22 + (buttonCount * BUTTON_SPACING);

        if (screen instanceof ContainerScreen && chatInput != null) {
            panelHeight += CHAT_INPUT_SPACING + CHAT_INPUT_HEIGHT;
        }

        /*
         * Panel background
         */
        screen.fill(
                PANEL_X - 2,
                PANEL_Y - 2,
                PANEL_X + PANEL_WIDTH + 2,
                PANEL_Y + panelHeight,
                0xCC111111
        );

        /*
         * Title
         */
        screen.drawString(
                font,
                "UI Utils",
                PANEL_X + 6,
                PANEL_Y + 6,
                0xFFFFFF
        );

        /*
         * Chat label
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