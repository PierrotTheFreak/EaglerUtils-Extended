package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.PacketDirection;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

/** Live packet inspector screen. */
public class UiUtilsPacketInspectorScreen extends Screen {
    private final Screen parent;
    private int scroll;

    public UiUtilsPacketInspectorScreen(Screen parent) {
        super(new StringTextComponent("UI Utils Packet Inspector"));
        this.parent = parent;
    }

    private int left() { return width / 2 - 190; }

    @Override
    protected void init() {
        scroll = 0;
    }

    private boolean inside(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = left();
        if (button == 0) {
            if (inside(mouseX, mouseY, x + 10, 245, 80, 22)) {
                UiUtilsPacketInspector.clear();
                return true;
            }
            if (inside(mouseX, mouseY, x + 98, 245, 80, 22)) {
                UiUtilsPacketInspector.togglePaused();
                return true;
            }
            if (inside(mouseX, mouseY, x + 186, 245, 80, 22)) {
                if (mc != null) mc.displayGuiScreen(parent);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scroll -= (int) delta;
        int max = Math.max(0, UiUtilsPacketInspector.getEntries().size() - 10);
        scroll = Math.max(0, Math.min(scroll, max));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            if (mc != null) mc.displayGuiScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void button(int x, int y, int w, String text, int mx, int my) {
        boolean hover = inside(mx, my, x, y, w, 22);
        fill(x, y, x + w, y + 22, hover ? 0xFF555555 : 0xFF333333);
        drawCenteredString(font, text, x + w / 2, y + 7, 0xFFFFFF);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        int x = left();
        fill(x, 4, x + 380, 274, 0xEE111111);
        drawCenteredString(font, "UI Utils Packet Inspector", x + 190, 10, 0xFFFFFF);
        drawString(font, "Live packet traffic", x + 10, 30, 0xAAAAAA);
        drawString(font, UiUtilsPacketInspector.isPaused() ? "PAUSED" : "CAPTURING", x + 300, 30, UiUtilsPacketInspector.isPaused() ? 0xFFAA55 : 0x55FF55);

        List<UiUtilsPacketInspector.Entry> entries = UiUtilsPacketInspector.getEntries();
        int start = Math.min(scroll, Math.max(0, entries.size() - 1));
        int end = Math.min(entries.size(), start + 10);
        for (int i = start; i < end; ++i) {
            UiUtilsPacketInspector.Entry e = entries.get(i);
            int y = 48 + (i - start) * 19;
            fill(x + 10, y, x + 370, y + 18, 0xFF333333);
            String dir = e.direction == PacketDirection.CLIENTBOUND ? "S -> C" : "C -> S";
            drawString(font, dir, x + 15, y + 5, e.direction == PacketDirection.CLIENTBOUND ? 0x55AAFF : 0x55FF55);
            drawString(font, e.name, x + 62, y + 5, 0xFFFFFF);
            drawString(font, e.size + " B", x + 320, y + 5, 0xAAAAAA);
        }

        button(x + 10, 245, 80, "Clear", mouseX, mouseY);
        button(x + 98, 245, 80, UiUtilsPacketInspector.isPaused() ? "Resume" : "Pause", mouseX, mouseY);
        button(x + 186, 245, 80, "Done", mouseX, mouseY);
        drawString(font, "Packets: " + entries.size() + "/" + UiUtilsPacketInspector.MAX_ENTRIES, x + 275, 252, 0xAAAAAA);
    }
}
