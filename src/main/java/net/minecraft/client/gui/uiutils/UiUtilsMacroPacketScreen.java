package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

/**
 * Packet selector used by a macro's Delay Packets action.
 *
 * This does NOT modify global UI Utils packet settings.
 */
public class UiUtilsMacroPacketScreen extends Screen {

    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 270;

    private static final int ENTRY_HEIGHT = 18;
    private static final int LIST_TOP = 58;
    private static final int VISIBLE_ENTRIES = 9;

    private final Screen parent;

    private final UiUtilsMacroAction action;

    private final List<UiUtilsMacroAction> ignoredActions;

    private TextFieldWidget searchField;

    private int scrollOffset;

    public UiUtilsMacroPacketScreen(
            Screen parent,
            UiUtilsMacroAction action,
            List<UiUtilsMacroAction> ignoredActions
    ) {

        super(
                new StringTextComponent(
                        "Macro Packet Selection"
                )
        );

        this.parent = parent;
        this.action = action;
        this.ignoredActions = ignoredActions;
    }

    @Override
    protected void init() {

        int left =
                getPanelLeft();

        this.searchField =
                new TextFieldWidget(
                        this.font,
                        left + 10,
                        32,
                        PANEL_WIDTH - 20,
                        20,
                        "Search packets..."
                );

        this.searchField.setMaxStringLength(
                128
        );

        this.addButton(
                this.searchField
        );
    }

    private int getPanelLeft() {
        return this.width / 2
                - PANEL_WIDTH / 2;
    }

    private String[] getPackets() {

        String query =
                searchField == null
                        ? ""
                        : searchField.getText();

        String[] all =
                UiUtilsPacketManager
                        .getAllPacketTypes();

        String[] temp =
                new String[all.length];

        int count = 0;

        for (String packet : all) {

            if (
                    UiUtilsPacketManager
                            .packetMatchesSearch(
                                    packet,
                                    query
                            )
            ) {

                temp[count++] =
                        packet;
            }
        }

        String[] result =
                new String[count];

        System.arraycopy(
                temp,
                0,
                result,
                0,
                count
        );

        return result;
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        if (
                searchField != null
                        && searchField.mouseClicked(
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

            String[] packets =
                    getPackets();

            for (
                    int i = 0;
                    i < packets.length;
                    ++i
            ) {

                int y =
                        LIST_TOP
                                + i
                                * ENTRY_HEIGHT
                                - scrollOffset;

                if (
                        y < LIST_TOP
                                || y >= LIST_TOP
                                + VISIBLE_ENTRIES
                                * ENTRY_HEIGHT
                ) {
                    continue;
                }

                if (
                        inside(
                                mouseX,
                                mouseY,
                                left + 10,
                                y,
                                PANEL_WIDTH - 20,
                                ENTRY_HEIGHT
                        )
                ) {

                    String packet =
                            packets[i];

                    if (
                            !UiUtilsPacketManager
                                    .isKeepAlivePacket(
                                            packet
                                    )
                    ) {

                        if (
                                action.hasPacketType(
                                        packet
                                )
                        ) {
                            action.removePacketType(
                                    packet
                            );
                        } else {
                            action.addPacketType(
                                    packet
                            );
                        }
                    }

                    return true;
                }
            }

            /*
             * Select all except keep-alive.
             */
            if (
                    inside(
                            mouseX,
                            mouseY,
                            left + 10,
                            235,
                            100,
                            22
                    )
            ) {

                action.clearPacketTypes();

                for (
                        String packet :
                                UiUtilsPacketManager
                                        .getAllPacketTypes()
                ) {

                    if (
                            !UiUtilsPacketManager
                                    .isKeepAlivePacket(
                                            packet
                                    )
                    ) {
                        action.addPacketType(
                                packet
                        );
                    }
                }

                return true;
            }

            /*
             * Clear.
             */
            if (
                    inside(
                            mouseX,
                            mouseY,
                            left + 118,
                            235,
                            100,
                            22
                    )
            ) {

                action.clearPacketTypes();

                return true;
            }

            /*
             * Done.
             */
            if (
                    inside(
                            mouseX,
                            mouseY,
                            left + 226,
                            235,
                            110,
                            22
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

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {

        String[] packets =
                getPackets();

        int maximum =
                Math.max(
                        0,
                        (
                                packets.length
                                        - VISIBLE_ENTRIES
                        )
                                * ENTRY_HEIGHT
                );

        scrollOffset -=
                (int) delta
                        * ENTRY_HEIGHT;

        if (scrollOffset < 0) {
            scrollOffset = 0;
        }

        if (scrollOffset > maximum) {
            scrollOffset = maximum;
        }

        return true;
    }

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {

        if (keyCode == 256) {

            this.mc.displayGuiScreen(
                    this.parent
            );

            return true;
        }

        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
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
                "Macro Packet Selection",
                this.width / 2,
                10,
                0xFFFFFF
        );

        String[] packets =
                getPackets();

        for (
                int i = 0;
                i < packets.length;
                ++i
        ) {

            int y =
                    LIST_TOP
                            + i
                            * ENTRY_HEIGHT
                            - scrollOffset;

            if (
                    y < LIST_TOP
                            || y >= LIST_TOP
                            + VISIBLE_ENTRIES
                            * ENTRY_HEIGHT
            ) {
                continue;
            }

            String packet =
                    packets[i];

            boolean locked =
                    UiUtilsPacketManager
                            .isKeepAlivePacket(
                                    packet
                            );

            boolean selected =
                    action.hasPacketType(
                            packet
                    );

            int background =
                    locked
                            ? 0xFF222222
                            : selected
                            ? 0xFF335533
                            : 0xFF333333;

            this.fill(
                    left + 10,
                    y,
                    left + PANEL_WIDTH - 10,
                    y + ENTRY_HEIGHT - 1,
                    background
            );

            String prefix =
                    locked
                            ? "[-] "
                            : selected
                            ? "[x] "
                            : "[ ] ";

            this.drawString(
                    this.font,
                    prefix + packet,
                    left + 15,
                    y + 5,
                    locked
                            ? 0x777777
                            : selected
                            ? 0x55FF55
                            : 0xFFFFFF
            );
        }

        drawButton(
                left + 10,
                235,
                100,
                22,
                "Select All",
                mouseX,
                mouseY
        );

        drawButton(
                left + 118,
                235,
                100,
                22,
                "Clear",
                mouseX,
                mouseY
        );

        drawButton(
                left + 226,
                235,
                110,
                22,
                "Done",
                mouseX,
                mouseY
        );

        this.drawString(
                this.font,
                "Selected: "
                        + action.getPacketTypes().size(),
                left + 10,
                220,
                0xAAAAAA
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