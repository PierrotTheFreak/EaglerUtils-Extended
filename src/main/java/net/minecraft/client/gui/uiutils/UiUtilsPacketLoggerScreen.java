package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

/** UI for controlling and exporting the packet logger. */
public class UiUtilsPacketLoggerScreen extends Screen {
    private final Screen parent;

    public UiUtilsPacketLoggerScreen(Screen parent) {
        super(new StringTextComponent("UI Utils Packet Logger"));
        this.parent = parent;
    }

    private int left() { return width / 2 - 190; }
    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void button(int x, int y, int w, String text, int mx, int my) {
        boolean hover = inside(mx, my, x, y, w, 22);
        fill(x, y, x + w, y + 22, hover ? 0xFF555555 : 0xFF333333);
        fill(x, y, x + w, y + 1, 0xFF777777);
        fill(x, y + 21, x + w, y + 22, 0xFF777777);
        drawCenteredString(font, text, x + w / 2, y + 7, 0xFFFFFF);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        int x = left();
        if (inside(mouseX, mouseY, x + 10, 48, 110, 22)) {
            UiUtilsPacketLogger.toggleLogging();
            return true;
        }
        if (inside(mouseX, mouseY, x + 128, 48, 110, 22)) {
            UiUtilsPacketLogger.clear();
            return true;
        }
        if (inside(mouseX, mouseY, x + 10, 78, 110, 22)) {
            UiUtilsPacketLogger.setClientToServer(!UiUtilsPacketLogger.isClientToServerEnabled());
            return true;
        }
        if (inside(mouseX, mouseY, x + 128, 78, 110, 22)) {
            UiUtilsPacketLogger.setServerToClient(!UiUtilsPacketLogger.isServerToClientEnabled());
            return true;
        }
        if (inside(mouseX, mouseY, x + 246, 48, 124, 22)) {
            UiUtilsPacketLogger.importInspectorEntries();
            return true;
        }
        if (inside(mouseX, mouseY, x + 10, 108, 110, 22)) {
            UiUtilsPacketLogger.copyToClipboard();
            return true;
        }
        if (inside(mouseX, mouseY, x + 128, 108, 110, 22)) {
            if (mc != null) mc.displayGuiScreen(parent);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (mc != null) mc.displayGuiScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        int x = left();
        fill(x, 4, x + 380, 274, 0xEE111111);
        drawCenteredString(font, "UI Utils Packet Logger", x + 190, 10, 0xFFFFFF);
        drawString(font, "Logs packet traffic independently of the inspector's clear button.", x + 10, 28, 0xAAAAAA);
        button(x + 10, 48, 110, UiUtilsPacketLogger.isLogging() ? "Logging: ON" : "Logging: OFF", mouseX, mouseY);
        button(x + 128, 48, 110, "Clear Log", mouseX, mouseY);
        button(x + 246, 48, 124, "Import Inspector", mouseX, mouseY);
        button(x + 10, 78, 110, UiUtilsPacketLogger.isClientToServerEnabled() ? "C -> S: ON" : "C -> S: OFF", mouseX, mouseY);
        button(x + 128, 78, 110, UiUtilsPacketLogger.isServerToClientEnabled() ? "S -> C: ON" : "S -> C: OFF", mouseX, mouseY);
        button(x + 10, 108, 110, "Copy Log", mouseX, mouseY);
        button(x + 128, 108, 110, "Done", mouseX, mouseY);
        drawString(font, "Entries: " + UiUtilsPacketLogger.getEntryCount(), x + 10, 145, 0xFFFFFF);
        String[] lines = UiUtilsPacketLogger.getText().split("\\n");
        int max = Math.min(lines.length, 6);
        for (int i = 0; i < max; ++i) {
            String line = lines[lines.length - 1 - i];
            if (line.length() > 54) line = line.substring(0, 51) + "...";
            drawString(font, line, x + 10, 162 + i * 15, 0xCCCCCC);
        }
    }
}
