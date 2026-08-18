package net.minecraft.client.gui.uiutils;

import java.util.ArrayList;
import java.util.List;

/**
 * One saved UI Utils macro.
 */
public class UiUtilsMacro {

    private final String name;

    private final List<UiUtilsMacroAction> actions =
            new ArrayList<>();

    public UiUtilsMacro(String name) {
        this.name =
                name == null || name.trim().isEmpty()
                        ? "Unnamed Macro"
                        : name.trim();
    }

    public String getName() {
        return this.name;
    }

    public List<UiUtilsMacroAction> getActions() {
        return this.actions;
    }

    public void addAction(
            UiUtilsMacroAction action
    ) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    public void removeAction(int index) {
        if (index >= 0
                && index < this.actions.size()) {

            this.actions.remove(index);
        }
    }
}