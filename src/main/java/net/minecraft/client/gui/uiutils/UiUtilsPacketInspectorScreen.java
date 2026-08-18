package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.network.PacketDirection;
import net.minecraft.util.text.StringTextComponent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Live packet inspector screen. */
public class UiUtilsPacketInspectorScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget searchField;
    private int scroll;

    public UiUtilsPacketInspectorScreen(Screen parent) {
        super(new StringTextComponent("UI Utils Packet Inspector"));
        this.parent = parent;
    }
    private int left() { return width / 2 - 190; }
    @Override protected void init() {
        scroll = 0;
        searchField = new TextFieldWidget(font, left() + 10, 38, 250, 20, "Search packets...");
        searchField.setMaxStringLength(128);
        addButton(searchField);
    }
    private boolean inside(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    private List<UiUtilsPacketInspector.Entry> filteredEntries() {
        String search = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        List<UiUtilsPacketInspector.Entry> result = new ArrayList<>();
        for (UiUtilsPacketInspector.Entry entry : UiUtilsPacketInspector.getEntries()) if (search.isEmpty() || entry.name.toLowerCase().contains(search)) result.add(entry);
        return result;
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (searchField != null && inside(mouseX, mouseY, searchField.x, searchField.y, searchField.getWidth(), 20)) { searchField.mouseClicked(mouseX, mouseY, button); setFocused(searchField); return true; }
        int x = left();
        if (button == 0) {
            if (inside(mouseX, mouseY, x + 270, 38, 100, 20) || inside(mouseX, mouseY, x + 10, 245, 80, 22)) { UiUtilsPacketInspector.clear(); scroll = 0; return true; }
            if (inside(mouseX, mouseY, x + 98, 245, 80, 22)) { UiUtilsPacketInspector.togglePaused(); return true; }
            if (inside(mouseX, mouseY, x + 186, 245, 80, 22)) { if (mc != null) mc.displayGuiScreen(parent); return true; }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean mouseScrolled(double mouseX, double mouseY, double delta) { List<UiUtilsPacketInspector.Entry> entries = filteredEntries(); scroll -= (int) delta; int max = Math.max(0, entries.size() - 10); scroll = Math.max(0, Math.min(scroll, max)); return true; }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { if (keyCode == 256) { if (mc != null) mc.displayGuiScreen(parent); return true; } if (searchField != null && searchField.isFocused() && searchField.keyPressed(keyCode, scanCode, modifiers)) return true; return super.keyPressed(keyCode, scanCode, modifiers); }
    @Override public boolean charTyped(char codePoint, int modifiers) { if (searchField != null && searchField.isFocused() && searchField.charTyped(codePoint, modifiers)) return true; return super.charTyped(codePoint, modifiers); }
    private void button(int x, int y, int w, String text, int mx, int my) { boolean hover = inside(mx, my, x, y, w, 22); fill(x, y, x + w, y + 22, hover ? 0xFF555555 : 0xFF333333); fill(x, y, x + w, y + 1, 0xFF777777); fill(x, y + 21, x + w, y + 22, 0xFF777777); drawCenteredString(font, text, x + w / 2, y + 7, 0xFFFFFF); }
    @Override public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground(); int x = left(); fill(x, 4, x + 380, 274, 0xEE111111); drawCenteredString(font, "UI Utils Packet Inspector", x + 190, 10, 0xFFFFFF); drawString(font, "Live packet traffic", x + 10, 30, 0xAAAAAA); drawString(font, UiUtilsPacketInspector.isPaused() ? "PAUSED" : "CAPTURING", x + 300, 30, UiUtilsPacketInspector.isPaused() ? 0xFFAA55 : 0x55FF55);
        button(x + 270, 38, 100, "Clear", mouseX, mouseY); List<UiUtilsPacketInspector.Entry> entries = filteredEntries(); int maxScroll = Math.max(0, entries.size() - 10); scroll = Math.max(0, Math.min(scroll, maxScroll)); int start = Math.min(scroll, Math.max(0, entries.size() - 1)); int end = Math.min(entries.size(), start + 10); SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        for (int i = start; i < end; ++i) { UiUtilsPacketInspector.Entry e = entries.get(i); int y = 64 + (i - start) * 18; fill(x + 10, y, x + 370, y + 17, 0xFF333333); String dir = e.direction == PacketDirection.CLIENTBOUND ? "S -> C" : "C -> S"; drawString(font, dir, x + 14, y + 5, e.direction == PacketDirection.CLIENTBOUND ? 0x55AAFF : 0x55FF55); drawString(font, e.name, x + 62, y + 5, 0xFFFFFF); drawString(font, timeFormat.format(new Date(e.timestamp)), x + 220, y + 5, 0x888888); drawString(font, e.size + " B", x + 332, y + 5, 0xAAAAAA); }
        button(x + 10, 245, 80, "Clear", mouseX, mouseY); button(x + 98, 245, 80, UiUtilsPacketInspector.isPaused() ? "Resume" : "Pause", mouseX, mouseY); button(x + 186, 245, 80, "Done", mouseX, mouseY); drawString(font, "Showing: " + entries.size() + "/" + UiUtilsPacketInspector.MAX_ENTRIES, x + 275, 252, 0xAAAAAA); super.render(mouseX, mouseY, partialTicks);
    }
}
