package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

/**
 * Action picker/editor for a macro.
 *
 * The user chooses an action type here and then fills in the
 * action's values.
 */
public class UiUtilsMacroActionScreen extends Screen {

    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 300;

    private final Screen parent;

    private final List<UiUtilsMacroAction> actions;

    private UiUtilsMacroAction.Type selectedType;

    private TextFieldWidget valueField;

    private TextFieldWidget amountField;

    public UiUtilsMacroActionScreen(
            Screen parent,
            List<UiUtilsMacroAction> actions
    ) {

        super(
                new StringTextComponent(
                        "Add Macro Action"
                )
        );

        this.parent = parent;
        this.actions = actions;

        this.selectedType =
                UiUtilsMacroAction.Type.CHAT;
    }

    @Override
    protected void init() {

        int left =
                getPanelLeft();

        this.valueField =
                new TextFieldWidget(
                        this.font,
                        left + 120,
                        55,
                        280,
                        20,
                        "Value"
                );

        this.valueField.setMaxStringLength(
                256
        );

        this.amountField =
                new TextFieldWidget(
                        this.font,
                        left + 120,
                        80,
                        120,
                        20,
                        "Amount"
                );

        this.amountField.setMaxStringLength(
                8
        );

        this.addButton(
                this.valueField
        );

        this.addButton(
                this.amountField
        );
    }

    private int getPanelLeft() {
        return this.width / 2
                - PANEL_WIDTH / 2;
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (this.valueField != null
                && this.valueField.mouseClicked(
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        if (this.amountField != null
                && this.amountField.mouseClicked(
                mouseX,
                mouseY,
                button
        )) {
            return true;
        }

        if (button == 0) {

            int left =
                    getPanelLeft();

            /*
             * Cycle through action types.
             */
            if (
                    inside(
                            mouseX,
                            mouseY,
                            left + 10,
                            105,
                            200,
                            24
                    )
            ) {

                UiUtilsMacroAction.Type[]
                        values =
                        UiUtilsMacroAction.Type
                                .values();

                int next =
                        selectedType.ordinal()
                                + 1;

                if (
                        next
                                >= values.length
                ) {
                    next = 0;
                }

                selectedType =
                        values[next];

                return true;
            }

            /*
             * Configure packets.
             */
            if (
                    selectedType
                            == UiUtilsMacroAction.Type
                            .DELAY_PACKETS
                            && inside(
                            mouseX,
                            mouseY,
                            left + 220,
                            105,
                            180,
                            24
                    )
            ) {

                UiUtilsMacroAction action =
                        createCurrentAction();

                this.mc.displayGuiScreen(
                        new UiUtilsMacroPacketScreen(
                                this,
                                action,
                                this.actions
                        )
                );

                return true;
            }

            /*
             * Add action.
             */
            if (
                    inside(
                            mouseX,
                            mouseY,
                            left + 10,
                            250,
                            130,
                            24
                    )
            ) {

                UiUtilsMacroAction action =
                        createCurrentAction();

                this.actions.add(
                        action
                );

                this.mc.displayGuiScreen(
                        this.parent
                );

                return true;
            }

            /*
             * Cancel.
             */
            if (
                    inside(
                            mouseX,
                            mouseY,
                            left + 280,
                            250,
                            130,
                            24
                    )
            ) {

                this.mc.displayGuiScreen(
                        this.parent
                );

                return true;
            }
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    private UiUtilsMacroAction
    createCurrentAction() {

        UiUtilsMacroAction action =
                new UiUtilsMacroAction(
                        selectedType
                );

        String value =
                valueField == null
                        ? ""
                        : valueField
                        .getText();

        String amountText =
                amountField == null
                        ? ""
                        : amountField
                        .getText();

        action.setText(
                value
        );

        int amount = 0;

        try {
            amount =
                    Integer.parseInt(
                            amountText
                    );
        } catch (NumberFormatException ignored) {
        }

        action.setAmount(
                amount
        );

        if (
                selectedType
                        == UiUtilsMacroAction.Type
                        .DELAY_PACKETS
        ) {
            action.setEnabled(
                    true
            );
        }

        if (
                selectedType
                        == UiUtilsMacroAction.Type
                        .SEND_PACKETS
        ) {
            action.setEnabled(
                    "on".equalsIgnoreCase(
                            value
                    )
                    || "true".equalsIgnoreCase(
                            value
                    )
            );
        }

        return action;
    }

    @Override
    public void render(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {

        this.renderBackground();

        int left =
                getPanelLeft();

        this.fill(
                left,
                0,
                left + PANEL_WIDTH,
                PANEL_HEIGHT,
                0xEE111111
        );

        this.drawCenteredString(
                this.font,
                "Add Macro Action",
                this.width / 2,
                10,
                0xFFFFFF
        );

        this.drawString(
                this.font,
                "Action Type:",
                left + 10,
                63,
                0xAAAAAA
        );

        drawButton(
                left + 10,
                105,
                200,
                24,
                selectedType.name(),
                mouseX,
                mouseY
        );

        if (
                selectedType
                        == UiUtilsMacroAction.Type
                        .DELAY_PACKETS
        ) {

            drawButton(
                    left + 220,
                    105,
                    180,
                    24,
                    "Configure Packets",
                    mouseX,
                    mouseY
            );

            this.drawString(
                    this.font,
                    "Click to choose packet types",
                    left + 10,
                    140,
                    0xAAAAAA
            );
        }

        /*
         * Value / amount explanations.
         */
        this.drawString(
                this.font,
                "Text:",
                left + 10,
                61,
                0xAAAAAA
        );

        this.drawString(
                this.font,
                "Amount:",
                left + 10,
                86,
                0xAAAAAA
        );

        drawButton(
                left + 10,
                250,
                130,
                24,
                "Add Action",
                mouseX,
                mouseY
        );

        drawButton(
                left + 280,
                250,
                130,
                24,
                "Cancel",
                mouseX,
                mouseY
        );

        super.render(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    private void drawButton(
            int x,
            int y,
            int width,
            int height,
            String text,
            int mouseX,
            int mouseY
    ) {

        boolean hovered =
                inside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        width,
                        height
                );

        this.fill(
                x,
                y,
                x + width,
                y + height,
                hovered
                        ? 0xFF555555
                        : 0xFF333333
        );

        this.drawString(
                this.font,
                text,
                x + 8,
                y + 7,
                0xFFFFFF
        );
    }

    private boolean inside(
            double mouseX,
            double mouseY,
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
}