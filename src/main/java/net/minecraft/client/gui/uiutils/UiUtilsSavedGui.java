package net.minecraft.client.gui.uiutils;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.util.text.ITextComponent;

public class UiUtilsSavedGui {

    private static boolean saved;

    private static Screen savedScreen;
    private static ITextComponent savedTitle;

    private static int savedWindowId = -1;

    private UiUtilsSavedGui() {
    }

    public static void save(Screen screen) {
        if (!(screen instanceof ContainerScreen)) {
            clear();
            return;
        }

        if (screen.mc == null || screen.mc.player == null) {
            clear();
            return;
        }

        savedScreen = screen;
        savedTitle = screen.getTitle();
        savedWindowId = screen.mc.player.openContainer.windowId;
        saved = true;
    }

    public static boolean hasSavedGui() {
        return saved && savedScreen != null;
    }

    public static Screen getSavedScreen() {
        return savedScreen;
    }

    public static ITextComponent getSavedTitle() {
        return savedTitle;
    }

    public static int getSavedWindowId() {
        return savedWindowId;
    }

    /**
     * Restores the saved client-side screen.
     *
     * This intentionally does NOT attempt to reopen the server-side
     * container. The server may have already closed or invalidated it.
     */
    public static boolean restore() {
        if (!hasSavedGui()) {
            return false;
        }

        if (savedScreen.mc == null) {
            return false;
        }

        savedScreen.mc.displayGuiScreen(savedScreen);
        return true;
    }

    public static void clear() {
        saved = false;
        savedScreen = null;
        savedTitle = null;
        savedWindowId = -1;
    }
}