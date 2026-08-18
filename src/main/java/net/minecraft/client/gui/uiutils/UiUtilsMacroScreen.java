package net.minecraft.client.gui.uiutils;

import net.lax1dude.eaglercraft.KeyboardConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;
import java.util.ArrayList;
import java.util.List;

/** Center-screen macro editor with per-macro hotkey capture. */
public class UiUtilsMacroScreen extends Screen {
    private static final int PANEL_WIDTH = 420;
    private static final int PANEL_HEIGHT = 300;
    private static final int ACTION_HEIGHT = 22;
    private final Screen parent;
    private TextFieldWidget nameField;
    private final List<UiUtilsMacroAction> actions = new ArrayList<>();
    private int selectedKey = KeyboardConstants.KEY_NONE;
    private boolean waitingForKey;

    public UiUtilsMacroScreen(Screen parent) { super(new StringTextComponent("Create UI Utils Macro")); this.parent = parent; }
    @Override protected void init() {
        int left = getPanelLeft();
        nameField = new TextFieldWidget(font, left + 10, 30, PANEL_WIDTH - 20, 20, "Macro name");
        nameField.setMaxStringLength(64);
        addButton(nameField);
    }
    private int getPanelLeft() { return width / 2 - PANEL_WIDTH / 2; }
    private void addDefaultAction(UiUtilsMacroAction.Type type) {
        UiUtilsMacroAction action = new UiUtilsMacroAction(type);
        switch (type) { case CHAT: action.setText("Hello!"); break; case WAIT: action.setAmount(20); break; case WAIT_FOR_PACKETS: action.setAmount(1); break; case DELAY_PACKETS: case SEND_PACKETS: action.setEnabled(true); break; default: break; }
        actions.add(action);
    }
    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (nameField != null && nameField.mouseClicked(mouseX, mouseY, button)) return true;
        if (button == 0) {
            int left = getPanelLeft();
            if (inside(mouseX, mouseY, left + 10, 260, 130, 24)) { mc.displayGuiScreen(new UiUtilsMacroActionScreen(this, actions)); return true; }
            if (inside(mouseX, mouseY, left + 150, 260, 130, 24)) { waitingForKey = true; return true; }
            if (inside(mouseX, mouseY, left + 290, 260, 120, 24)) { saveMacro(); return true; }
            for (int i = 0; i < actions.size(); ++i) {
                int y = 65 + i * ACTION_HEIGHT;
                if (inside(mouseX, mouseY, left + 380, y, 30, ACTION_HEIGHT)) { actions.remove(i); return true; }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitingForKey) {
            if (keyCode == 256) { selectedKey = KeyboardConstants.KEY_NONE; waitingForKey = false; return true; }
            selectedKey = keyCode;
            waitingForKey = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    private void saveMacro() {
        String name = nameField == null ? "Unnamed Macro" : nameField.getText().trim();
        UiUtilsMacro macro = new UiUtilsMacro(name.isEmpty() ? "Unnamed Macro" : name);
        macro.getActions().addAll(actions);
        if (selectedKey != KeyboardConstants.KEY_NONE) macro.setKeyCode(selectedKey);
        UiUtilsMacroManager.addMacro(macro);
        mc.displayGuiScreen(parent);
    }
    @Override public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();
        int left = getPanelLeft();
        fill(left, 0, left + PANEL_WIDTH, PANEL_HEIGHT, 0xEE111111);
        drawCenteredString(font, "Create UI Utils Macro", width / 2, 10, 0xFFFFFF);
        drawString(font, "Macro Name:", left + 10, 20, 0xAAAAAA);
        drawString(font, "Hotkey: " + (waitingForKey ? "PRESS A KEY..." : keyName(selectedKey)), left + 10, 54, waitingForKey ? 0xFFFF55 : 0xFFFFFF);
        for (int i = 0; i < actions.size(); ++i) {
            UiUtilsMacroAction action = actions.get(i); int y = 65 + i * ACTION_HEIGHT;
            fill(left + 10, y, left + 370, y + ACTION_HEIGHT - 2, 0xFF333333);
            drawString(font, (i + 1) + ". " + action.getDisplayText(), left + 15, y + 6, 0xFFFFFF);
            drawString(font, "X", left + 390, y + 6, 0xFF5555);
        }
        drawButton(left + 10, 260, 130, 24, "Add Action", mouseX, mouseY);
        drawButton(left + 150, 260, 130, 24, "Set Hotkey", mouseX, mouseY);
        drawButton(left + 290, 260, 120, 24, "Save", mouseX, mouseY);
        super.render(mouseX, mouseY, partialTicks);
    }
    private String keyName(int key) { return key == KeyboardConstants.KEY_NONE ? "NONE" : Integer.toString(key); }
    private void drawButton(int x,int y,int width,int height,String text,int mouseX,int mouseY) { boolean hovered=inside(mouseX,mouseY,x,y,width,height); fill(x,y,x+width,y+height,hovered?0xFF555555:0xFF333333); int tw=font.getStringWidth(text); drawString(font,text,x+(width-tw)/2,y+7,0xFFFFFF); }
    private boolean inside(double mx,double my,int x,int y,int w,int h) { return mx>=x&&mx<x+w&&my>=y&&my<y+h; }
}