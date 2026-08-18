package net.minecraft.client.gui.uiutils;

import net.lax1dude.eaglercraft.KeyboardConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

/** Global UI Utils macro hotkey using the client's normal KeyBinding event path. */
public final class UiUtilsMacroKeyBinding {
    public static final KeyBinding KEY_BINDING = new KeyBinding(
            "key.uiutils.macros",
            KeyboardConstants.KEY_RSHIFT,
            "key.categories.ui"
    );

    private UiUtilsMacroKeyBinding() {}

    public static void onPressed() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        if (mc.currentScreen instanceof UiUtilsMacroScreen) return;
        if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ControlsScreen) return;
        mc.displayGuiScreen(new UiUtilsMacroScreen(mc.currentScreen));
    }
}
