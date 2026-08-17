package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;

public class UiUtils {

    private static final int PANEL_X = 4;
    private static final int PANEL_Y = 4;
    private static final int PANEL_WIDTH = 150;
    private static final int PANEL_HEIGHT = 48;

    public static void render(Screen screen, FontRenderer font, int mouseX, int mouseY, float partialTicks) {
        if (!(screen instanceof ContainerScreen) && !(screen instanceof ChatScreen)) {
            return;
        }

        // Panel background
        screen.fill(
                PANEL_X,
                PANEL_Y,
                PANEL_X + PANEL_WIDTH,
                PANEL_Y + PANEL_HEIGHT,
                0xCC111111
        );

        // Title
        screen.drawString(
                font,
                "UI Utils",
                PANEL_X + 6,
                PANEL_Y + 6,
                0xFFFFFF
        );

        // Placeholder buttons
        drawButton(screen, font, "Close Without Packet", PANEL_X + 4, PANEL_Y + 20);
        drawButton(screen, font, "De-sync", PANEL_X + 4, PANEL_Y + 34);
    }

    private static void drawButton(Screen screen, FontRenderer font, String text, int x, int y) {
        screen.fill(
                x,
                y,
                x + 142,
                y + 12,
                0xFF333333
        );

        screen.drawString(
                font,
                text,
                x + 4,
                y + 2,
                0xFFFFFF
        );
    }
}