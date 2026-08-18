package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.util.text.StringTextComponent;

import java.util.Locale;

public class UiUtilsFabricatePacketScreen extends Screen {

    private static final int FIELD_WIDTH = 180;
    private static final int FIELD_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 180;
    private static final int BUTTON_HEIGHT = 20;

    private final Screen parent;

    private TextFieldWidget windowIdField;
    private TextFieldWidget slotIdField;
    private TextFieldWidget buttonField;
    private TextFieldWidget actionNumberField;

    private Button packetTypeButton;
    private Button clickTypeButton;
    private Button sendButton;

    private int packetType = 0;
    private ClickType clickType = ClickType.PICKUP;

    public UiUtilsFabricatePacketScreen(Screen parent) {
        super(new StringTextComponent("Fabricate Packet"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2 - FIELD_WIDTH / 2;
        int y = 30;

        /*
         * Window ID
         */
        this.windowIdField = new TextFieldWidget(
                this.font,
                centerX,
                y,
                FIELD_WIDTH,
                FIELD_HEIGHT,
                "Window ID"
        );

        this.windowIdField.setMaxStringLength(3);
        this.windowIdField.setText("0");
        this.addButton(this.windowIdField);

        y += 28;

        /*
         * Slot ID
         */
        this.slotIdField = new TextFieldWidget(
                this.font,
                centerX,
                y,
                FIELD_WIDTH,
                FIELD_HEIGHT,
                "Slot ID"
        );

        this.slotIdField.setMaxStringLength(6);
        this.slotIdField.setText("0");
        this.addButton(this.slotIdField);

        y += 28;

        /*
         * Button / mouse button.
         */
        this.buttonField = new TextFieldWidget(
                this.font,
                centerX,
                y,
                FIELD_WIDTH,
                FIELD_HEIGHT,
                "Button"
        );

        this.buttonField.setMaxStringLength(3);
        this.buttonField.setText("0");
        this.addButton(this.buttonField);

        y += 28;

        /*
         * Action number.
         */
        this.actionNumberField = new TextFieldWidget(
                this.font,
                centerX,
                y,
                FIELD_WIDTH,
                FIELD_HEIGHT,
                "Action Number"
        );

        this.actionNumberField.setMaxStringLength(6);
        this.actionNumberField.setText("0");
        this.addButton(this.actionNumberField);

        y += 28;

        /*
         * Packet type selector.
         */
        this.packetTypeButton = new Button(
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                centerX,
                y,
                getPacketTypeText(),
                new Button.IPressable() {
                    @Override
                    public void onPress(Button button) {
                        packetType++;

                        if (packetType > 1) {
                            packetType = 0;
                        }

                        button.setMessage(getPacketTypeText());
                        updateFieldVisibility();
                    }
                }
        );

        this.addButton(this.packetTypeButton);

        y += 24;

        /*
         * Click type selector.
         *
         * Only used for CClickWindowPacket.
         */
        this.clickTypeButton = new Button(
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                centerX,
                y,
                getClickTypeText(),
                new Button.IPressable() {
                    @Override
                    public void onPress(Button button) {
                        ClickType[] values = ClickType.values();

                        int index = clickType.ordinal();
                        index++;

                        if (index >= values.length) {
                            index = 0;
                        }

                        clickType = values[index];
                        button.setMessage(getClickTypeText());
                    }
                }
        );

        this.addButton(this.clickTypeButton);

        y += 24;

        /*
         * Send button.
         */
        this.sendButton = new Button(
                BUTTON_WIDTH,
                BUTTON_HEIGHT,
                centerX,
                y,
                "Send Packet",
                new Button.IPressable() {
                    @Override
                    public void onPress(Button button) {
                        sendPacket();
                    }
                }
        );

        this.addButton(this.sendButton);

        y += 24;

        /*
         * Back button.
         */
        this.addButton(
                new Button(
                        BUTTON_WIDTH,
                        BUTTON_HEIGHT,
                        centerX,
                        y,
                        "Back",
                        new Button.IPressable() {
                            @Override
                            public void onPress(Button button) {
                                closeScreen();
                            }
                        }
                )
        );

        updateFieldVisibility();
    }

    private String getPacketTypeText() {
        if (packetType == 0) {
            return "Packet: CClickWindowPacket";
        }

        return "Packet: CEnchantItemPacket";
    }

    private String getClickTypeText() {
        return "Click Type: " + clickType.name();
    }

    private void updateFieldVisibility() {
        /*
         * CClickWindowPacket uses:
         *  - window ID
         *  - slot ID
         *  - button
         *  - click type
         *  - action number
         *
         * CEnchantItemPacket only uses:
         *  - window ID
         *  - button
         *
         * Keep the fields present but disable the irrelevant ones.
         */
        boolean clickWindow = packetType == 0;

        if (slotIdField != null) {
            slotIdField.setEnabled(clickWindow);
        }

        if (actionNumberField != null) {
            actionNumberField.setEnabled(clickWindow);
        }

        if (clickTypeButton != null) {
            clickTypeButton.active = clickWindow;
        }

        if (buttonField != null) {
            buttonField.setEnabled(true);
        }
    }

    private int parseInt(TextFieldWidget field, int fallback) {
        try {
            return Integer.parseInt(field.getText().trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private short parseShort(TextFieldWidget field, short fallback) {
        try {
            int value = Integer.parseInt(field.getText().trim());

            if (value < Short.MIN_VALUE) {
                value = Short.MIN_VALUE;
            }

            if (value > Short.MAX_VALUE) {
                value = Short.MAX_VALUE;
            }

            return (short) value;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private void sendPacket() {
        if (this.mc == null || this.mc.player == null) {
            return;
        }

        int windowId = parseInt(windowIdField, 0);
        int button = parseInt(buttonField, 0);

        if (packetType == 0) {
            int slotId = parseInt(slotIdField, 0);
            short actionNumber = parseShort(actionNumberField, (short) 0);

            CClickWindowPacket packet = new CClickWindowPacket(
                    windowId,
                    slotId,
                    button,
                    clickType,
                    ItemStack.EMPTY,
                    actionNumber
            );

            /*
             * Route fabricated packets through the exact same packet
             * manager used by normal inventory clicks.
             *
             * This means:
             *
             * Send Packets OFF  -> packet is discarded.
             * Delay Packets ON  -> packet is queued.
             * Otherwise         -> packet is sent immediately.
             */
            if (!UiUtilsPacketManager.handleOutgoingPacket(
                    packet,
                    this.mc.player.connection.getNetworkManager()
            )) {
                this.mc.player.connection.sendPacket(packet);
            }

        } else {
            CEnchantItemPacket packet = new CEnchantItemPacket(
                    windowId,
                    button
            );

            if (!UiUtilsPacketManager.handleOutgoingPacket(
                    packet,
                    this.mc.player.connection.getNetworkManager()
            )) {
                this.mc.player.connection.sendPacket(packet);
            }
        }
    }

    private void closeScreen() {
        if (this.mc != null) {
            this.mc.displayGuiScreen(this.parent);
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();

        int centerX = this.width / 2;

        this.drawCenteredString(
                this.font,
                "Fabricate Packet",
                centerX,
                10,
                0xFFFFFF
        );

        /*
         * Field labels.
         */
        this.drawString(
                this.font,
                "Window ID",
                this.width / 2 - FIELD_WIDTH / 2,
                20,
                0xAAAAAA
        );

        this.drawString(
                this.font,
                "Slot ID",
                this.width / 2 - FIELD_WIDTH / 2,
                48,
                0xAAAAAA
        );

        this.drawString(
                this.font,
                "Button",
                this.width / 2 - FIELD_WIDTH / 2,
                76,
                0xAAAAAA
        );

        this.drawString(
                this.font,
                "Action Number",
                this.width / 2 - FIELD_WIDTH / 2,
                104,
                0xAAAAAA
        );

        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        /*
         * Escape returns to the previous screen.
         */
        if (keyCode == 256) {
            closeScreen();
            return true;
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }
}