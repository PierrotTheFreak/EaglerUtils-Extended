package net.minecraft.client.gui.uiutils;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import java.util.ArrayList;
import java.util.List;

/** One saved UI Utils macro, including its optional hotkey. */
public class UiUtilsMacro {
    private final String name;
    private final List<UiUtilsMacroAction> actions = new ArrayList<>();
    private int keyCode = InputMappings.INPUT_INVALID.getKeyCode();
    private KeyBinding keyBinding;

    public UiUtilsMacro(String name) { this.name = name == null || name.trim().isEmpty() ? "Unnamed Macro" : name.trim(); }
    public String getName() { return name; }
    public List<UiUtilsMacroAction> getActions() { return actions; }
    public void addAction(UiUtilsMacroAction action) { if (action != null) actions.add(action); }
    public void removeAction(int index) { if (index >= 0 && index < actions.size()) actions.remove(index); }
    public int getKeyCode() { return keyCode; }
    public KeyBinding getKeyBinding() { return keyBinding; }
    public String getKeyName() { return keyBinding == null || keyBinding.isInvalid() ? "NONE" : keyBinding.getLocalizedName(); }

    public void setKeyCode(int code) {
        keyCode = code;
        if (keyBinding == null) keyBinding = new KeyBinding("key.uiutils.macro." + name, code, "key.categories.ui");
        else keyBinding.bind(InputMappings.Type.KEYSYM.getOrMakeInput(code));
        KeyBinding.resetKeyBindingArrayAndHash();
    }

    public void clearKeyBinding() { setKeyCode(InputMappings.INPUT_INVALID.getKeyCode()); }
}