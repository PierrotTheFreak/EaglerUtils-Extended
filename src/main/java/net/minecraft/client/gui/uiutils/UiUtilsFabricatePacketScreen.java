package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.util.text.StringTextComponent;
import java.util.List;

/** Packet fabricator backed by the actual PLAY SERVERBOUND registry. */
public class UiUtilsFabricatePacketScreen extends Screen {
    private final Screen parent;
    private String[] packetNames = new String[0];
    private int selected;
    private TextFieldWidget windowId, slotId, button, action;
    private Button packetButton, clickTypeButton;
    private ClickType clickType = ClickType.PICKUP;

    public UiUtilsFabricatePacketScreen(Screen parent) {
        super(new StringTextComponent("Packet Fabricator"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        List<Class<? extends IPacket<?>>> classes = ProtocolType.PLAY.getPacketClasses(PacketDirection.SERVERBOUND);
        packetNames = new String[classes.size()];
        for (int i = 0; i < classes.size(); ++i) packetNames[i] = classes.get(i).getSimpleName();
        selected = find("CClickWindowPacket");
        int x = width / 2 - 90, y = 34;
        packetButton = new Button(180, 20, x, y, packetLabel(), b -> { if (packetNames.length > 0) { selected = (selected + 1) % packetNames.length; b.setMessage(packetLabel()); updateFields(); } });
        addButton(packetButton);
        windowId = field(x, y + 28, "Window ID", "0");
        slotId = field(x, y + 56, "Slot ID", "0");
        button = field(x, y + 84, "Button", "0");
        action = field(x, y + 112, "Action Number", "0");
        clickTypeButton = new Button(180, 20, x, y + 140, clickTypeLabel(), b -> { ClickType[] v = ClickType.values(); clickType = v[(clickType.ordinal() + 1) % v.length]; b.setMessage(clickTypeLabel()); });
        addButton(clickTypeButton);
        addButton(new Button(180, 20, x, y + 166, "Fabricate / Send", b -> sendSelected()));
        addButton(new Button(180, 20, x, y + 192, "Back", b -> closeScreen()));
        updateFields();
    }

    private int find(String name) { for (int i = 0; i < packetNames.length; ++i) if (name.equals(packetNames[i])) return i; return 0; }
    private String packetLabel() { return packetNames.length == 0 ? "No SERVERBOUND packets" : "Packet: " + packetNames[selected]; }
    private String clickTypeLabel() { return "Click Type: " + clickType.name(); }
    private TextFieldWidget field(int x, int y, String hint, String value) { TextFieldWidget f = new TextFieldWidget(font, x, y, 180, 20, hint); f.setMaxStringLength(8); f.setText(value); addButton(f); return f; }
    private boolean current(String name) { return packetNames.length > 0 && name.equals(packetNames[selected]); }
    private void updateFields() { boolean click = current("CClickWindowPacket"), enchant = current("CEnchantItemPacket"); windowId.setEnabled(click || enchant); slotId.setEnabled(click); action.setEnabled(click); button.setEnabled(click || enchant); clickTypeButton.active = click; }
    private int number(TextFieldWidget f) { try { return Integer.parseInt(f.getText().trim()); } catch (NumberFormatException e) { return 0; } }
    private short shortNumber(TextFieldWidget f) { int n = number(f); if (n < Short.MIN_VALUE) n = Short.MIN_VALUE; if (n > Short.MAX_VALUE) n = Short.MAX_VALUE; return (short)n; }

    private void sendSelected() {
        if (mc == null || mc.player == null || mc.player.connection == null) return;
        IPacket<?> packet = null;
        if (current("CClickWindowPacket")) packet = new CClickWindowPacket(number(windowId), number(slotId), number(button), clickType, ItemStack.EMPTY, shortNumber(action));
        else if (current("CEnchantItemPacket")) packet = new CEnchantItemPacket(number(windowId), number(button));
        else return;
        if (!UiUtilsPacketManager.handleOutgoingPacket(packet, mc.player.connection.getNetworkManager())) mc.player.connection.sendPacket(packet);
    }

    private void closeScreen() { if (mc != null) mc.displayGuiScreen(parent); }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) { if (keyCode == 256) { closeScreen(); return true; } return super.keyPressed(keyCode, scanCode, modifiers); }
    @Override public void render(int mouseX, int mouseY, float partialTicks) { renderBackground(); drawCenteredString(font, "Packet Fabricator", width / 2, 10, 0xFFFFFF); drawCenteredString(font, current("CClickWindowPacket") || current("CEnchantItemPacket") ? "Editable packet template" : "Discovered packet - no constructor template yet", width / 2, 22, 0xAAAAAA); super.render(mouseX, mouseY, partialTicks); }
}
