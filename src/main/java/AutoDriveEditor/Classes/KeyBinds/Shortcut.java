package AutoDriveEditor.Classes.KeyBinds;

import AutoDriveEditor.GUI.Buttons.BaseButton;
import AutoDriveEditor.Managers.ShortcutManager;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogShortcutInfoMenu.bDebugLogShortcutInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ShortcutManager.getDefaultShortcutList;

public class Shortcut {

    private Object owner;
    private Object callbackObject;
    private final ShortcutManager.ShortcutID id;
    private final String propertiesString;
    private final String localeString;
    private int keyCode;
    private int modifiers;
    private InputMap inputMap;
    private boolean isEnabled = true;

    public Object getCallbackObject() {
        return callbackObject;
    }

    public void setCallbackObject(Object callbackObject) {
        this.callbackObject = callbackObject;
    }


    /**
     * Interface not is use currently, for future use.
     */
    public interface ShortcutEventInterface {
        @SuppressWarnings("unused")
        void onShortcutChanged();
    }

    /**
     * Constructor for Shortcut.
     *
     * @param id identifier for the shortcut, in Integer format.
     * @param propertiesString The displayed name for the shortcut, in String format.
     * @param keyCode The keycode for the shortcut, in integer format.
     * @param modifiers The modifier for the shortcut, in integer format.
     */
    public Shortcut(ShortcutManager.ShortcutID id, String propertiesString, int keyCode, int modifiers) {
        // Owner can be set later if needed
        this.owner = null;
        this.id = id; // Use ordinal() to get the integer value of the enum
        this.propertiesString = propertiesString;
        this.localeString = getLocaleString(propertiesString);
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.inputMap = null;
    }

    /**
     * Updates the input map with the new key code and modifiers.
     *
     * @param keyCode The new key code.
     * @param modifiers The new modifiers.
     */
    @SuppressWarnings("MagicConstant")
    public void updateInputMap(int keyCode, int modifiers) {
        if (this.inputMap != null ) {
            KeyStroke oldKeyStroke = KeyStroke.getKeyStroke(this.keyCode, this.modifiers);
            if (bDebugLogShortcutInfo) LOG.info("## Shortcut.updateInputMap() ## Current shortcut for '{}' : {} ( {} ) , {} ( {} )", this.id, this.keyCode, getKeyVK(), oldKeyStroke.getModifiers(), InputEvent.getModifiersExText(oldKeyStroke.getModifiers()));
            for (KeyStroke keyStroke : this.inputMap.allKeys()) {
                if (keyStroke.getKeyCode() == oldKeyStroke.getKeyCode() && keyStroke.getModifiers() == oldKeyStroke.getModifiers()) {
                    if (bDebugLogShortcutInfo) LOG.info("## Shortcut.updateInputMap() ## Removing old keyStroke: {}", keyStroke);
                    this.inputMap.remove(keyStroke);
                }
            }
            KeyStroke newKeyStroke = KeyStroke.getKeyStroke(keyCode, modifiers);
            this.inputMap.put(newKeyStroke, this.id);
            this.keyCode = keyCode;
            this.modifiers = modifiers;
            if (bDebugLogShortcutInfo) {
                for (KeyStroke k : this.inputMap.allKeys()) {
                    if (k.getKeyCode() == this.keyCode && k.getModifiers() == this.modifiers) {
                        LOG.info("Shortcut update success : '{}' keyCode: '{}' , modifier '{}'", this.id, this.keyCode, this.modifiers);
                    }
                }
            }
        } else {
            if (bDebugLogShortcutInfo) LOG.info("## Shortcut.updateInputMap() ## InputMap is null for shortcut '{}', cannot updateWidget", this.propertiesString);
            this.setKeyCode(keyCode);
            this.setModifiers(modifiers);
        }
    }

    @SuppressWarnings("MagicConstant")
    public void clear() {
        if (bDebugLogShortcutInfo) LOG.info("## Shortcut.clear() ## Clearing shortcut '{}'", this.localeString);
        if (this.inputMap != null) {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(this.keyCode, this.modifiers);
            this.inputMap.remove(keyStroke);
        } else {
            if (bDebugLogShortcutInfo) LOG.info("## Shortcut.clear() ## InputMap is null for shortcut '{}', cannot clear", this.localeString);
        }
        this.keyCode = 0;
        this.modifiers = 0;
        this.isEnabled = false;
        updateOwnerToolTip();
    }

    @SuppressWarnings("MagicConstant")
    public void reset() {
        for (Shortcut defaultShortcut : getDefaultShortcutList()) {
            if (bDebugLogShortcutInfo) LOG.info("## Shortcut.reset() ## Checking default shortcut '{}' against this '{}'", defaultShortcut.id, this.id);
            if (defaultShortcut.id == this.id) {
                if (this.inputMap != null) {
                    KeyStroke oldKeyStroke = KeyStroke.getKeyStroke(this.keyCode, this.modifiers);
                    if (bDebugLogShortcutInfo) LOG.info("## Shortcut.reset() ## Removing keystroke '{}'", oldKeyStroke);
                    this.inputMap.remove(KeyStroke.getKeyStroke(this.keyCode, this.modifiers));
                } else {
                    if (bDebugLogShortcutInfo) LOG.info("## Shortcut.reset() ## InputMap is null for shortcut '{}', cannot reset", this.localeString);
                }
                this.keyCode = defaultShortcut.keyCode;
                this.modifiers = defaultShortcut.modifiers;
                KeyStroke defaultShortcutKeyStroke = KeyStroke.getKeyStroke(this.keyCode, this.modifiers);
                if (this.inputMap != null) {
                    this.inputMap.put(defaultShortcutKeyStroke, this.id);
                } else {
                    if (bDebugLogShortcutInfo) LOG.info("## Shortcut.reset() ## InputMap is null for shortcut '{}', cannot add new keyStroke", this.localeString);
                }
                if (bDebugLogShortcutInfo) LOG.info("## Shortcut.reset() ## Resetting shortcut '{}' to default '{}'", this.localeString, this.getShortcutString());

                updateOwnerToolTip();
                return;
            }
        }
    }

    public void updateOwnerToolTip() {
        if (this.owner instanceof BaseButton) {
            BaseButton button = (BaseButton) this.owner;
            if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.updateShortcut() ## Updating button tooltip for '{}'", button.getButtonID());
            button.updateTooltip();
        }
    }

    //
    // Getters
    //


    public boolean isEnabled() {
        return isEnabled;
    }
    /**
     * Retrieves the owner of the shortcut.
     * @return owner The owner of the shortcut, can be null if not set.
     */
    public Object getOwner() {
        return owner;
    }

    /**
     * Retrieves the shortcut text.
     *
     * @return The identifier name as a string.
     */
    public ShortcutManager.ShortcutID getId() { return this.id; }

    /**
     * Returns the input map associated with the shortcut.
     * @return The InputMap associated with the shortcut, can be null if not set.
     */
    public InputMap getInputMap() { return this.inputMap; }

    /**
     * Retrieves the shortcut String.
     *
     * @return The locale string identifier.
     */
    public String getPropertiesString() { return this.propertiesString; }

    public String getLocalizedString() { return localeString; }

    /**
     * Retrieves the key code as an integer.
     *
     * @return The key code as an integer.
     */
    public int getKeyCode() { return keyCode; }

    /**
     * Retrieves the key code as a KeyStroke.
     *
     * @return The KeyStroke representing the key code and modifiers.
     */
    @SuppressWarnings("MagicConstant")
    public KeyStroke getKeyStroke() { return KeyStroke.getKeyStroke(keyCode, modifiers); }

    /**
     * Retrieves the key code as a string.
     *
     * @return The key code as a string.
     */
    public String getKeyCodeString() { return (keyCode != 0) ? KeyEvent.getKeyText(keyCode) : ""; }

    /**
     * Retrieves the VK_ constant name for the key code.
     *
     * @return The VK_ constant name.
     */
    public String getKeyVK() { return getVKString(keyCode); }

    /**
     * Retrieves the modifiers as an integer.
     *
     * @return The modifiers as an integer.
     */
    public int getModifier() { return modifiers; }

    /**
     * Retrieves the modifiers as a string.
     *
     * @return The modifiers as a string.
     */
    public String getModifierString() { return InputEvent.getModifiersExText(modifiers); }

    /**
     * Retrieves the shortcut as a string.
     *
     * @return The shortcut as a string.
     */
    public String getShortcutString() { return (getModifierString().isEmpty()) ? getKeyCodeString() : getModifierString() + " + " + getKeyCodeString(); }

    /**
     * Retrieves the string representation of a key code.
     *
     * @param keyCode The key code.
     * @return The string representation of the key code.
     */
    private static String getVKString(int keyCode) {
        // use reflection to get the VK_ constant name from the supplied integer value
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getName().startsWith("VK_")) {
                try {
                    if (field.getInt(null) == keyCode) {
                        return field.getName();
                    }
                } catch (IllegalAccessException e1) {
                    e1.printStackTrace();
                    LOG.info("getVKString() : IllegalAccessException: VK_{} not found", keyCode);
                    return "VK_UNDEFINED";
                }
            }
        }
        LOG.info("getVKString() : VK_{} not found", keyCode);
        return "VK_UNDEFINED";
    }


    //
    // Setters
    //

    /**
     * Sets the owner of the shortcut.
     * @param owner The owner of the shortcut, can be null
     */
    public void setOwner(Object owner) {
        this.owner = owner;
    }

    public void setEnabled(boolean enabled) { this.isEnabled = enabled; }

    /**
     * Sets the key code.
     *
     * @param keyCode The key code.
     */
    public void setKeyCode(int keyCode) { this.keyCode = keyCode; }

    /**
     * Sets the modifiers.
     *
     * @param modifiers The modifiers.
     */
    public void setModifiers(int modifiers) { this.modifiers = modifiers; }

    /**
     * Sets the input map.
     *
     * @param inputMap The input map.
     */
    public void setInputMap(InputMap inputMap) { this.inputMap = inputMap; }

    /**
     * Returns a string representation of the shortcut.
     *
     * @return A string representation of the shortcut.
     */
    @Override
    public String toString() {
        return "Shortcut {" +
                " id='" + id + '\'' +
                ", Name='" + localeString + '\'' +
                ", KeyCode=" + keyCode +
                ", Modifiers=" + modifiers +
                " }";
    }
}
