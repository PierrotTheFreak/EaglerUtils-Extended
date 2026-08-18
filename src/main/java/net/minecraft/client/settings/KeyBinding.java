package net.minecraft.client.settings;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.gui.uiutils.UiUtilsMacroKeyBinding;
import net.minecraft.client.gui.uiutils.UiUtilsMacroManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KeyBinding implements Comparable<KeyBinding> {
   private static final Map<String, KeyBinding> KEYBIND_ARRAY = Maps.newHashMap();
   private static final Map<InputMappings.Input, KeyBinding> HASH = Maps.newHashMap();
   private static final Set<String> KEYBIND_SET = Sets.newHashSet();
   private static final Map<String, Integer> CATEGORY_ORDER = Util.make(Maps.newHashMap(), (p) -> {
      p.put("key.categories.movement", 1); p.put("key.categories.gameplay", 2); p.put("key.categories.inventory", 3);
      p.put("key.categories.creative", 4); p.put("key.categories.multiplayer", 5); p.put("key.categories.ui", 6); p.put("key.categories.misc", 7);
   });
   private final String keyDescription;
   private final InputMappings.Input keyCodeDefault;
   private final String keyCategory;
   private InputMappings.Input keyCode;
   private boolean pressed;
   private int pressTime;

   public static void onTick(InputMappings.Input key) {
      KeyBinding keybinding = HASH.get(key);
      if (keybinding != null) {
         ++keybinding.pressTime;
         if (keybinding == UiUtilsMacroKeyBinding.KEY_BINDING) UiUtilsMacroKeyBinding.onPressed();
         else UiUtilsMacroManager.handleKeyBinding(keybinding);
      }
   }
   public static void setKeyBindState(InputMappings.Input key, boolean held) { KeyBinding k = HASH.get(key); if (k != null) k.pressed = held; }
   public static void updateKeyBindState() { for (KeyBinding k : KEYBIND_ARRAY.values()) if (k.keyCode.getType() == InputMappings.Type.KEYSYM && k.keyCode.getKeyCode() != InputMappings.INPUT_INVALID.getKeyCode()) k.pressed = InputMappings.isKeyDown(k.keyCode.getKeyCode()); }
   public static void unPressAllKeys() { for (KeyBinding k : KEYBIND_ARRAY.values()) k.unpressKey(); }
   public static void resetKeyBindingArrayAndHash() { HASH.clear(); for (KeyBinding k : KEYBIND_ARRAY.values()) HASH.put(k.keyCode, k); }
   public KeyBinding(String description, int keyCode, String category) { this(description, InputMappings.Type.KEYSYM, keyCode, category); }
   public KeyBinding(String description, InputMappings.Type type, int code, String category) { keyDescription=description; keyCode=type.getOrMakeInput(code); keyCodeDefault=keyCode; keyCategory=category; KEYBIND_ARRAY.put(description,this); HASH.put(this.keyCode,this); KEYBIND_SET.add(category); }
   public boolean isKeyDown() { return pressed; }
   public String getKeyCategory() { return keyCategory; }
   public boolean isPressed() { if (pressTime == 0) return false; --pressTime; return true; }
   private void unpressKey() { pressTime=0; pressed=false; }
   public String getKeyDescription() { return keyDescription; }
   public InputMappings.Input getDefault() { return keyCodeDefault; }
   public void bind(InputMappings.Input key) { keyCode=key; }
   public int compareTo(KeyBinding other) { return keyCategory.equals(other.keyCategory) ? I18n.format(keyDescription).compareTo(I18n.format(other.keyDescription)) : CATEGORY_ORDER.get(keyCategory).compareTo(CATEGORY_ORDER.get(other.keyCategory)); }
   public static Supplier<String> getDisplayString(String key) { KeyBinding k=KEYBIND_ARRAY.get(key); return k==null ? () -> key : k::getLocalizedName; }
   public boolean conflicts(KeyBinding binding) { return keyCode.equals(binding.keyCode); }
   public boolean isInvalid() { return keyCode.equals(InputMappings.INPUT_INVALID); }
   public boolean matchesKey(int keysym,int scancode) { return keysym==InputMappings.INPUT_INVALID.getKeyCode() ? keyCode.getType()==InputMappings.Type.SCANCODE&&keyCode.getKeyCode()==scancode : keyCode.getType()==InputMappings.Type.KEYSYM&&keyCode.getKeyCode()==keysym; }
   public boolean matchesMouseKey(int key) { return keyCode.getType()==InputMappings.Type.MOUSE&&keyCode.getKeyCode()==key; }
   public String getLocalizedName() { String s=keyCode.getTranslationKey(); int i=keyCode.getKeyCode(); String s1=null; switch(keyCode.getType()) { case KEYSYM: s1=InputMappings.func_216507_a(i); break; case SCANCODE: s1=InputMappings.func_216502_b(i); break; case MOUSE: String s2=I18n.format(s); s1=Objects.equals(s2,s)?I18n.format(InputMappings.Type.MOUSE.func_216500_a(),i+1):s2; } return s1==null?I18n.format(s):s1; }
   public boolean isDefault() { return keyCode.equals(keyCodeDefault); }
   public String getTranslationKey() { return keyCode.getTranslationKey(); }
}