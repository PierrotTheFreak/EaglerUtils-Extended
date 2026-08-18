package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * Center-screen macro editor.
 *
 * Used to:
 *
 * - Name a macro.
 * - Add actions.
 * - Edit action values.
 * - Configure packet selections.
 * - Save the macro.
 */
public class UiUtilsMacroScreen extends Screen {

    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 300;

    private static final int ACTION_HEIGHT = 22;

    private final Screen parent;

    private TextFieldWidget nameField;

    private final List<UiUtilsMacroAction> actions =
            new ArrayList<>();

    public UiUtilsMacroScreen(Screen parent) {
        super(
                new StringTextComponent(
                        "Create UI Utils Macro"
                )
        );

        this.parent = parent;
    }

    @Override
    protected void init() {

        int left =
                getPanelLeft();

        /*
         * Macro name.
         */
        this.nameField =
                new TextFieldWidget(
                        this.font,
                        left + 10,
                        30,
                        PANEL_WIDTH - 20,
                        20,
                        "Macro name"
                );

        this.nameField.setMaxStringLength(
                64
        );

        this.addButton(
                this.nameField
        );
    }

    private int getPanelLeft() {
        return this.width / 2
                - PANEL_WIDTH / 2;
    }

    /*
     * ------------------------------------------------------------
     * Actions
     * ------------------------------------------------------------
     */

    private void addDefaultAction(
            UiUtilsMacroAction.Type type
    ) {

        UiUtilsMacroAction action =
                new UiUtilsMacroAction(
                        type
                );

        switch (type) {

            case CHAT:
                action.setText(
                        "Hello!"
                );
                break;

            case WAIT:
                action.setAmount(
                        20
                );
                break;

            case WAIT_FOR_PACKETS:
                action.setAmount(
                        1
                );
                break;

            case DELAY_PACKETS:
                action.setEnabled(
                        true
                );
                break;

            case SEND_PACKETS:
                action.setEnabled(
                        true
                );
                break;

            default:
                break;
        }

        this.actions.add(action);
    }

    /*
     * ------------------------------------------------------------
     * Mouse
     * ------------------------------------------------------------
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (
                this.nameField != null
                        && this.nameField.mouseClicked(
                        mouseX,
                        mouseY,
                        button
                )
        ) {
            return true;
        }

        if (button == 0) {

            int left =
                    getPanelLeft();

            /*
             * Add Action.
             */
            if (
                    inside(
                            mouseX,
                            mouseY,
                            left + 10,
                            260,
                            130,
                            24
                    )
            ) {

                this.mc.displayGuiScreen(
                        new UiUtilsMacroActionScreen(
                                this,
                                this.actions
                        )
                );

                return true;
            }

            /*
             * Save.
             */
            if (
                    inside(
                            mouseX,
                            mouseY,
                            left + 290,
                            260,
                            120,
                            24
                    )
            ) {

                saveMacro();

                return true;
            }

            /*
             * Delete individual actions.
             */
            for (
                    int i = 0;
                    i < this.actions.size();
                    ++i
            ) {

                int y =
                        65
                                + i
                                * ACTION_HEIGHT;

                if (
                        inside(
                                mouseX,
                                mouseY,
                                left + 380,
                                y,
                                30,
                                ACTION_HEIGHT
                        )
                ) {

                    this.actions.remove(i);
                    return true;
                }
            }
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    private void saveMacro() {

        String name =
                this.nameField == null
                        ? "Unnamed Macro"
                        : this.nameField
                        .getText()
                        .trim();

        if (name.isEmpty()) {
            name = "Unnamed Macro";
        }

        UiUtilsMacro macro =
                new UiUtilsMacro(
                        name
                );

        macro.getActions().addAll(
                this.actions
        );

        UiUtilsMacroManager.addMacro(
                macro
        );

        this.mc.displayGuiScreen(
                this.parent
        );
    }

    /*
     * ------------------------------------------------------------
     * Rendering
     * ------------------------------------------------------------
     */

    @Override
    public void render(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {

        this.renderBackground();

        int left =
                getPanelLeft();

        /*
         * Panel.
         */
        this.fill(
                left,
                0,
                left + PANEL_WIDTH,
                PANEL_HEIGHT,
                0xEE111111
        );

        this.drawCenteredString(
                this.font,
                "Create UI Utils Macro",
                this.width / 2,
                10,
                0xFFFFFF
        );

        /*
         * Name label.
         */
        this.drawString(
                this.font,
                "Macro Name:",
                left + 10,
                20,
                0xAAAAAA
        );

        /*
         * Actions.
         */
        for (
                int i = 0;
                i < this.actions.size();
                ++i
        ) {

            UiUtilsMacroAction action =
                    this.actions.get(i);

            int y =
                    65
                            + i
                            * ACTION_HEIGHT;

            this.fill(
                    left + 10,
                    y,
                    left + 370,
                    y + ACTION_HEIGHT - 2,
                    0xFF333333
            );

            this.drawString(
                    this.font,
                    (i + 1)
                            + ". "
                            + action.getDisplayText(),
                    left + 15,
                    y + 6,
                    0xFFFFFF
            );

            this.drawString(
                    this.font,
                    "X",
                    left + 390,
                    y + 6,
                    0xFF5555
            );
        }

        /*
         * Add Action.
         */
        drawButton(
                left + 10,
                260,
                130,
                24,
                "Add Action",
                mouseX,
                mouseY
        );

        /*
         * Save.
         */
        drawButton(
                left + 290,
                260,
                120,
                24,
                "Save",
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

        int textWidth =
                this.font.getStringWidth(
                        text
                );

        this.drawString(
                this.font,
                text,
                x + (width - textWidth) / 2,
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