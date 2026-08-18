package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

/**
 * UI Utils Packet Delay Settings
 *
 * Opened by right-clicking the "Delay Packets" button.
 *
 * Features:
 *
 * - Search every dynamically registered PLAY -> SERVERBOUND packet.
 * - Click a packet to toggle whether it is delayed.
 * - Delay All selects every packet except CKeepAlivePacket.
 * - Clear All removes every packet from the delay selection.
 * - CKeepAlivePacket is always protected and cannot be delayed.
 * - Scroll through the packet list.
 */
public class UiUtilsPacketSettingsScreen extends Screen {

    /*
     * ------------------------------------------------------------
     * Window layout
     * ------------------------------------------------------------
     */

    private static final int PANEL_WIDTH = 340;
    private static final int PANEL_HEIGHT = 270;

    private static final int PANEL_TOP = 4;

    private static final int SEARCH_X = 10;
    private static final int SEARCH_Y = 38;
    private static final int SEARCH_HEIGHT = 20;

    private static final int LIST_X = 10;
    private static final int LIST_TOP = 66;

    private static final int LIST_WIDTH = PANEL_WIDTH - 20;
    private static final int LIST_ENTRY_HEIGHT = 18;

    /*
     * Only this many packet entries are visible at once.
     */
    private static final int VISIBLE_ENTRIES = 8;

    /*
     * Bottom controls.
     */
    private static final int ACTION_BUTTON_Y = 230;
    private static final int ACTION_BUTTON_HEIGHT = 22;

    private final Screen parent;

    private TextFieldWidget searchField;

    /*
     * Vertical scroll amount in pixels.
     */
    private int scrollOffset = 0;

    public UiUtilsPacketSettingsScreen(Screen parent) {
        super(
                new StringTextComponent(
                        "UI Utils Packet Delay Settings"
                )
        );

        this.parent = parent;
    }

    /*
     * ------------------------------------------------------------
     * Screen initialization
     * ------------------------------------------------------------
     */

    @Override
    protected void init() {

        int left = getPanelLeft();

        /*
         * Search box.
         *
         * This filters the dynamically discovered packet list by
         * packet class name.
         */
        this.searchField =
                new TextFieldWidget(
                        this.font,
                        left + SEARCH_X,
                        SEARCH_Y,
                        LIST_WIDTH,
                        SEARCH_HEIGHT,
                        "Search packets..."
                );

        this.searchField.setMaxStringLength(128);

        this.addButton(
                this.searchField
        );

this.searchField.setFocused(true);

this.scrollOffset = 0;

        /*
         * Start at the top whenever the settings screen is opened.
         */
        this.scrollOffset = 0;
    }

    /*
     * ------------------------------------------------------------
     * Layout helpers
     * ------------------------------------------------------------
     */

    private int getPanelLeft() {
        return this.width / 2
                - PANEL_WIDTH / 2;
    }

    /*
     * ------------------------------------------------------------
     * Dynamic packet list
     * ------------------------------------------------------------
     */

    /**
     * Gets every PLAY -> SERVERBOUND packet currently registered
     * by ProtocolType and filters it using the search box.
     *
     * There is intentionally NO hard-coded packet list here.
     */
    private String[] getFilteredPackets() {

        String search =
                this.searchField == null
                        ? ""
                        : this.searchField.getText();

        String[] allPackets =
                UiUtilsPacketManager
                        .getAllPacketTypes();

        String[] results =
                new String[allPackets.length];

        int count = 0;

        for (String packetName : allPackets) {

            if (
                    UiUtilsPacketManager
                            .packetMatchesSearch(
                                    packetName,
                                    search
                            )
            ) {
                results[count] =
                        packetName;

                ++count;
            }
        }

        String[] filtered =
                new String[count];

        System.arraycopy(
                results,
                0,
                filtered,
                0,
                count
        );

        return filtered;
    }

    /*
     * ------------------------------------------------------------
     * Scroll calculations
     * ------------------------------------------------------------
     */

    private int getMaximumScroll(
            String[] packets
    ) {

        int totalHeight =
                packets.length
                        * LIST_ENTRY_HEIGHT;

        int visibleHeight =
                VISIBLE_ENTRIES
                        * LIST_ENTRY_HEIGHT;

        return Math.max(
                0,
                totalHeight - visibleHeight
        );
    }

    private void clampScroll(
            String[] packets
    ) {

        int maximum =
                getMaximumScroll(
                        packets
                );

        if (this.scrollOffset < 0) {
            this.scrollOffset = 0;
        }

        if (this.scrollOffset > maximum) {
            this.scrollOffset = maximum;
        }
    }

    /*
     * ------------------------------------------------------------
     * Mouse interaction
     * ------------------------------------------------------------
     */

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {

        /*
         * Let the search box handle its own clicks first.
         */
        if (
                this.searchField != null
                        && this.searchField.mouseClicked(
                        mouseX,
                        mouseY,
                        button
                )
        ) {
            return true;
        }

        int left =
                getPanelLeft();

        /*
         * --------------------------------------------------------
         * Left mouse button
         * --------------------------------------------------------
         */
        if (button == 0) {

            /*
             * Delay All
             */
            if (
                    isInside(
                            mouseX,
                            mouseY,
                            left + 10,
                            8,
                            100,
                            22
                    )
            ) {

                UiUtilsPacketManager
                        .delayAllPackets();

                return true;
            }

            /*
             * Clear All
             */
            if (
                    isInside(
                            mouseX,
                            mouseY,
                            left + 118,
                            8,
                            100,
                            22
                    )
            ) {

                UiUtilsPacketManager
                        .clearDelayedPacketTypes();

                return true;
            }

            /*
             * Packet list.
             */
            String[] packets =
                    getFilteredPackets();

            for (
                    int i = 0;
                    i < packets.length;
                    ++i
            ) {

                int y =
                        LIST_TOP
                                + i * LIST_ENTRY_HEIGHT
                                - this.scrollOffset;

                /*
                 * Ignore entries outside the visible list.
                 */
                if (
                        y < LIST_TOP
                                || y >= LIST_TOP
                                + VISIBLE_ENTRIES
                                * LIST_ENTRY_HEIGHT
                ) {
                    continue;
                }

                if (
                        isInside(
                                mouseX,
                                mouseY,
                                left + LIST_X,
                                y,
                                LIST_WIDTH,
                                LIST_ENTRY_HEIGHT
                        )
                ) {

                    UiUtilsPacketManager
                            .togglePacketDelayed(
                                    packets[i]
                            );

                    return true;
                }
            }

            /*
             * Done
             */
            if (
                    isInside(
                            mouseX,
                            mouseY,
                            left + 10,
                            ACTION_BUTTON_Y,
                            140,
                            ACTION_BUTTON_HEIGHT
                    )
            ) {

                closeScreen();

                return true;
            }
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    /*
     * ------------------------------------------------------------
     * Mouse wheel scrolling
     * ------------------------------------------------------------
     */

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double delta
    ) {

        String[] packets =
                getFilteredPackets();

        /*
         * One wheel step = one packet row.
         */
        this.scrollOffset -=
                (int) delta
                        * LIST_ENTRY_HEIGHT;

        clampScroll(
                packets
        );

        return true;
    }

    /*
     * ------------------------------------------------------------
     * Keyboard
     * ------------------------------------------------------------
     */

    @Override
    public boolean keyPressed(
            int keyCode,
            int scanCode,
            int modifiers
    ) {

        /*
         * Escape closes the settings screen.
         */
        if (keyCode == 256) {
            closeScreen();
            return true;
        }

        /*
         * Let the normal Screen handling process everything else.
         */
        return super.keyPressed(
                keyCode,
                scanCode,
                modifiers
        );
    }

    /*
     * ------------------------------------------------------------
     * Close / return
     * ------------------------------------------------------------
     */

    private void closeScreen() {

        if (this.mc != null) {

            this.mc.displayGuiScreen(
                    this.parent
            );
        }
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
         * --------------------------------------------------------
         * Main settings panel
         * --------------------------------------------------------
         */
        this.fill(
                left,
                PANEL_TOP,
                left + PANEL_WIDTH,
                PANEL_TOP + PANEL_HEIGHT,
                0xEE111111
        );

        /*
         * --------------------------------------------------------
         * Title
         * --------------------------------------------------------
         */
        this.drawCenteredString(
                this.font,
                "UI Utils Packet Delay Settings",
                this.width / 2,
                10,
                0xFFFFFF
        );

        /*
         * --------------------------------------------------------
         * Delay All button
         * --------------------------------------------------------
         */
        drawButton(
                left + 10,
                8,
                100,
                22,
                "Delay All",
                mouseX,
                mouseY
        );

        /*
         * --------------------------------------------------------
         * Clear All button
         * --------------------------------------------------------
         */
        drawButton(
                left + 118,
                8,
                100,
                22,
                "Clear All",
                mouseX,
                mouseY
        );

        /*
         * --------------------------------------------------------
         * Search label
         * --------------------------------------------------------
         */
        this.drawString(
                this.font,
                "Search packets:",
                left + SEARCH_X,
                29,
                0xAAAAAA
        );

        /*
         * --------------------------------------------------------
         * Packet list
         * --------------------------------------------------------
         */

        String[] packets =
                getFilteredPackets();

        /*
         * Draw the visible packet entries.
         */
        for (
                int i = 0;
                i < packets.length;
                ++i
        ) {

            int y =
                    LIST_TOP
                            + i * LIST_ENTRY_HEIGHT
                            - this.scrollOffset;

            /*
             * Don't draw entries outside the visible list.
             */
            if (
                    y < LIST_TOP
                            || y >= LIST_TOP
                            + VISIBLE_ENTRIES
                            * LIST_ENTRY_HEIGHT
            ) {
                continue;
            }

            String packetName =
                    packets[i];

            /*
             * Keep-alive is permanently protected.
             */
            boolean keepAlive =
                    UiUtilsPacketManager
                            .isKeepAlivePacket(
                                    packetName
                            );

            /*
             * Is this packet selected for delay?
             */
            boolean selected =
                    UiUtilsPacketManager
                            .isPacketDelayed(
                                    packetName
                            );

            /*
             * Background state.
             */
            int backgroundColor;

            if (keepAlive) {

                backgroundColor =
                        0xFF222222;

            } else if (selected) {

                backgroundColor =
                        0xFF335533;

            } else {

                backgroundColor =
                        0xFF333333;
            }

            this.fill(
                    left + LIST_X,
                    y,
                    left + LIST_X + LIST_WIDTH,
                    y + LIST_ENTRY_HEIGHT - 1,
                    backgroundColor
            );

            /*
             * Checkbox / lock indicator.
             */
            String prefix;

            if (keepAlive) {

                prefix = "[-] ";

            } else if (selected) {

                prefix = "[x] ";

            } else {

                prefix = "[ ] ";
            }

            /*
             * Text color.
             */
            int textColor;

            if (keepAlive) {

                textColor =
                        0x777777;

            } else if (selected) {

                textColor =
                        0x55FF55;

            } else {

                textColor =
                        0xFFFFFF;
            }

            this.drawString(
                    this.font,
                    prefix + packetName,
                    left + LIST_X + 5,
                    y + 5,
                    textColor
            );

            /*
             * Explain why keep-alive cannot be selected.
             */
            if (keepAlive) {

                String locked =
                        "protected";

                int lockedWidth =
                        this.font.getStringWidth(
                                locked
                        );

                this.drawString(
                        this.font,
                        locked,
                        left
                                + LIST_X
                                + LIST_WIDTH
                                - lockedWidth
                                - 5,
                        y + 5,
                        0x777777
                );
            }
        }

        /*
         * --------------------------------------------------------
         * Bottom controls
         * --------------------------------------------------------
         */

        drawButton(
                left + 10,
                ACTION_BUTTON_Y,
                140,
                ACTION_BUTTON_HEIGHT,
                "Done",
                mouseX,
                mouseY
        );

        /*
         * Selected packet count.
         */
        this.drawString(
                this.font,
                "Selected: "
                        + getSelectedCount()
                        + " / "
                        + getSelectableCount(),
                left + 165,
                ACTION_BUTTON_Y + 7,
                0xAAAAAA
        );

        /*
         * Scroll information.
         */
        int maximumScroll =
                getMaximumScroll(
                        packets
                );

        if (maximumScroll > 0) {

            this.drawString(
                    this.font,
                    "Scroll for more packets",
                    left + 165,
                    ACTION_BUTTON_Y + 17,
                    0x777777
            );
        }

        /*
         * Let the search TextFieldWidget render itself.
         */
        super.render(
                mouseX,
                mouseY,
                partialTicks
        );
    }

    /*
     * ------------------------------------------------------------
     * Button rendering helper
     * ------------------------------------------------------------
     */

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
                isInside(
                        mouseX,
                        mouseY,
                        x,
                        y,
                        width,
                        height
                );

        /*
         * Background.
         */
        this.fill(
                x,
                y,
                x + width,
                y + height,
                hovered
                        ? 0xFF555555
                        : 0xFF333333
        );

        /*
         * Border.
         */
        this.fill(
                x,
                y,
                x + width,
                y + 1,
                0xFF777777
        );

        this.fill(
                x,
                y + height - 1,
                x + width,
                y + height,
                0xFF777777
        );

        this.fill(
                x,
                y,
                x + 1,
                y + height,
                0xFF777777
        );

        this.fill(
                x + width - 1,
                y,
                x + width,
                y + height,
                0xFF777777
        );

        /*
         * Center the label.
         */
        int textWidth =
                this.font.getStringWidth(
                        text
                );

        this.drawString(
                this.font,
                text,
                x + (width - textWidth) / 2,
                y + (height - 8) / 2,
                0xFFFFFF
        );
    }

    /*
     * ------------------------------------------------------------
     * Utility
     * ------------------------------------------------------------
     */

    private boolean isInside(
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

    /*
     * ------------------------------------------------------------
     * Selection statistics
     * ------------------------------------------------------------
     */

    private int getSelectedCount() {

        int count = 0;

        for (
                String packet :
                        UiUtilsPacketManager
                                .getAllPacketTypes()
        ) {

            if (
                    UiUtilsPacketManager
                            .isPacketDelayed(
                                    packet
                            )
            ) {
                ++count;
            }
        }

        return count;
    }

    private int getSelectableCount() {

        int count = 0;

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
                ++count;
            }
        }

        return count;
    }
}