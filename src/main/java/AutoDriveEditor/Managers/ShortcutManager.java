package AutoDriveEditor.Managers;

import AutoDriveEditor.Classes.CircularList;
import AutoDriveEditor.Classes.KeyBinds.Shortcut;
import AutoDriveEditor.Classes.KeyBinds.ShortcutGroup;
import AutoDriveEditor.GUI.Buttons.BaseButton;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogShortcutInfoMenu.bDebugLogShortcutInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.ShortcutManager.ShortcutID.*;

/**
 * The ShortcutManager is responsible for managing the shortcuts used.
 * It provides methods for adding, updating, and retrieving shortcuts.
 */

@SuppressWarnings("unused")
public class ShortcutManager {

    // Enum representing all shortcut identifiers and the associated XML descriptor.
    public enum ShortcutID {
        MOVE_NODE_SHORTCUT("MOVE_NODE"),
        DELETE_SHORTCUT("DELTE_NODE"),
        NEW_LINEAR_LINE_SHORTCUT("NEW_LINE"),
        NEW_NODE_SHORTCUT("NEW_NODE"),
        NODE_PRIORITY_SHORTCUT("SWAP_PRIORITY"),
        FLIP_CONNECTION_SHORTCUT("FLIP_CONNECT"),
        ROTATE_NODES_SHORTCUT("ROTATE_NODES"),
        BEZIER_CURVE_SHORTCUT("BEZIER_CURVE"),
        ADD_MARKER_SHORTCUT("ADD_MARKER"),
        EDIT_MARKER_SHORTCUT("EDIT_MARKER"),
        DELETE_MARKER_SHORTCUT("DELETE_MARKER"),
        ALIGN_NODES_HORIZONTALLY_SHORTCUT("ALIGN_HORIZ"),
        ALIGN_NODES_VERTICALLY_SHORTCUT("ALIGN_VERT"),
        FOCUS_NODE_SHORTCUT("FOCUS_NODE"),
        CUT_SHORTCUT("CUT"),
        COPY_SHORTCUT("COPY"),
        PASTE_SHORTCUT("PASTE"),
        PASTE_ORIGINAL_LOCATION_SHORTCUT("PASTE_ORIGINAL"),
        TOGGLE_SELECT_HIDDEN_SHORTCUT("SELECT_HIDDEN"),
        TOGGLE_SHOW_GRID_SHORTCUT("SHOW_GRID"),
        TOGGLE_GRID_SNAPPING_SHORTCUT("GRID_SNAP"),
        TOGGLE_SUBDIVISION_SNAPPING_SHORTCUT("SUB_SNAP"),
        //TOGGLE_HIDDEN_NODES_SHORTCUT("HIDE_NODES"),
        NODE_SIZE_UP_SHORTCUT("NODE_UP"),
        NODE_SIZE_DOWN_SHORTCUT("NODE_DOWN"),
        TOGGLE_CONTINUOUS_CONNECTION_SHORTCUT("CONTINUOUS_CONNECT"),
        TOGGLE_MARKER_NAMES_SHORTCUT("MARKER_NAMES"),
        TOGGLE_MARKER_ICONS_SHORTCUT("MARKER_ICONS"),
        TOGGLE_PARKING_ICONS_SHORTCUT("PARKING_ICONS"),
        SHOW_NODE_ID_ON_HOVER_SHORTCUT("NODE_ID_HOVER");

        private final String xmlDescriptor;

        ShortcutID(String ID) { this.xmlDescriptor = ID; }

        public String getXmlDescriptor() { return xmlDescriptor; }
    }

    // Lists to store shortcuts and default shortcuts
    private static List<Shortcut> shortcutList;
    private static List<Shortcut> defaultShortcutList;

    public static final String LINEAR_LINE_GROUP = "LinearLine Group";
    public static final String ADD_NODE_GROUP = "New Node Group";


    /**
     * Initialize the shortcut lists and sets default shortcuts.
     */
    public ShortcutManager() {
        LOG.info("  Initializing ShortcutManager");
        shortcutList = new ArrayList<>();
        defaultShortcutList = new ArrayList<>();

        // set default shortcuts

        defaultShortcutList.add(new Shortcut(MOVE_NODE_SHORTCUT, "panel_config_tab_keybinds_shortcut_move_node", KeyEvent.VK_M, 0));
        defaultShortcutList.add(new Shortcut(DELETE_SHORTCUT, "panel_config_tab_keybinds_shortcut_delete", KeyEvent.VK_DELETE, 0));
        defaultShortcutList.add(new Shortcut(NEW_LINEAR_LINE_SHORTCUT, "panel_config_tab_keybinds_shortcut_linear_line", KeyEvent.VK_L, 0));
        defaultShortcutList.add(new Shortcut(NEW_NODE_SHORTCUT, "panel_config_tab_keybinds_shortcut_new_node", KeyEvent.VK_N, 0));
        defaultShortcutList.add(new Shortcut(NODE_PRIORITY_SHORTCUT, "panel_config_tab_keybinds_shortcut_node_priority", KeyEvent.VK_P, 0));
        defaultShortcutList.add(new Shortcut(FLIP_CONNECTION_SHORTCUT, "panel_config_tab_keybinds_shortcut_flip_connection", KeyEvent.VK_F, 0));
        defaultShortcutList.add(new Shortcut(ROTATE_NODES_SHORTCUT, "panel_config_tab_keybinds_shortcut_rotate_nodes", KeyEvent.VK_R, 0));
        defaultShortcutList.add(new Shortcut(BEZIER_CURVE_SHORTCUT, "panel_config_tab_keybinds_shortcut_create_bezier_curve", KeyEvent.VK_B, 0));
        defaultShortcutList.add(new Shortcut(ADD_MARKER_SHORTCUT, "panel_config_tab_keybinds_shortcut_add_marker", KeyEvent.VK_W, 0));
        defaultShortcutList.add(new Shortcut(EDIT_MARKER_SHORTCUT, "panel_config_tab_keybinds_shortcut_edit_marker", KeyEvent.VK_E, 0));
        defaultShortcutList.add(new Shortcut(DELETE_MARKER_SHORTCUT, "panel_config_tab_keybinds_shortcut_delete_marker", KeyEvent.VK_D, KeyEvent.SHIFT_DOWN_MASK));
        defaultShortcutList.add(new Shortcut(ALIGN_NODES_HORIZONTALLY_SHORTCUT, "panel_config_tab_keybinds_shortcut_align_nodes_horizontally", KeyEvent.VK_X, KeyEvent.SHIFT_DOWN_MASK));
        defaultShortcutList.add(new Shortcut(ALIGN_NODES_VERTICALLY_SHORTCUT, "panel_config_tab_keybinds_shortcut_align_nodes_vertically", KeyEvent.VK_Y, KeyEvent.SHIFT_DOWN_MASK));
        defaultShortcutList.add(new Shortcut(FOCUS_NODE_SHORTCUT, "panel_config_tab_keybinds_shortcut_focus_node", KeyEvent.VK_F, KeyEvent.SHIFT_DOWN_MASK));
        defaultShortcutList.add(new Shortcut(CUT_SHORTCUT, "panel_config_tab_keybinds_shortcut_cut", KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        defaultShortcutList.add(new Shortcut(COPY_SHORTCUT, "panel_config_tab_keybinds_shortcut_copy", KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        defaultShortcutList.add(new Shortcut(PASTE_SHORTCUT, "panel_config_tab_keybinds_shortcut_paste", KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
        defaultShortcutList.add(new Shortcut(PASTE_ORIGINAL_LOCATION_SHORTCUT, "panel_config_tab_keybinds_shortcut_paste_original_location", KeyEvent.VK_V, KeyEvent.SHIFT_DOWN_MASK));
        defaultShortcutList.add(new Shortcut(TOGGLE_SELECT_HIDDEN_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_select_hidden", KeyEvent.VK_H, 0));
        defaultShortcutList.add(new Shortcut(TOGGLE_SHOW_GRID_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_grid", KeyEvent.VK_G, 0));
        defaultShortcutList.add(new Shortcut(TOGGLE_GRID_SNAPPING_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_grid_snapping", KeyEvent.VK_S, 0));
        defaultShortcutList.add(new Shortcut(TOGGLE_SUBDIVISION_SNAPPING_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_subdivision_snapping", KeyEvent.VK_D, 0));
        //defaultShortcutList.add(new Shortcut(TOGGLE_HIDDEN_NODES_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_hidden_nodes", KeyEvent.VK_H, 0));
        defaultShortcutList.add(new Shortcut(NODE_SIZE_UP_SHORTCUT, "panel_config_tab_keybinds_shortcut_node_size_up", KeyEvent.VK_ADD, 0));
        defaultShortcutList.add(new Shortcut(NODE_SIZE_DOWN_SHORTCUT, "panel_config_tab_keybinds_shortcut_node_size_down", KeyEvent.VK_SUBTRACT, 0));
        defaultShortcutList.add(new Shortcut(TOGGLE_CONTINUOUS_CONNECTION_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_continuous_connection", KeyEvent.VK_SEMICOLON, 0));
        defaultShortcutList.add(new Shortcut(TOGGLE_MARKER_NAMES_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_marker_names", KeyEvent.VK_F1, 0));
        defaultShortcutList.add(new Shortcut(TOGGLE_MARKER_ICONS_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_marker_icons", KeyEvent.VK_F2, 0));
        defaultShortcutList.add(new Shortcut(TOGGLE_PARKING_ICONS_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_parking_icons", KeyEvent.VK_F3, 0));
        defaultShortcutList.add(new Shortcut(SHOW_NODE_ID_ON_HOVER_SHORTCUT, "panel_config_tab_keybinds_shortcut_toggle_show_node_id", KeyEvent.VK_F4, 0));
    }

    /**
     * Adds a new shortcut to the list.
     *
     * @param shortcutID The ID number of the required shortcut.
     * @param keyCode The key code for the shortcut.
     * @param modifier The modifiers for the shortcut (e.g., CTRL, SHIFT).
     */
    public static void addShortcut(ShortcutID shortcutID, int keyCode, int modifier) {
        String localeText ="";
        for(Shortcut defaultShortcut : defaultShortcutList) {
            if (shortcutID == defaultShortcut.getId()) {
                if (bDebugLogShortcutInfo) LOG.info("Found identifier for shortcut ID {} '{}'", defaultShortcut.getId(), defaultShortcut.getPropertiesString());
                shortcutList.add(new Shortcut(shortcutID, defaultShortcut.getPropertiesString(), keyCode, modifier));
                return;
            }
        }
        LOG.info("## ShortcutManager.addShortcut() : No default shortcut found for ID {}", shortcutID);
    }

    /**
     * Adds a missing user-defined shortcut to the list.
     * @param defaultShortcut the default shortcut to be added.
     */
    public static void addMissingUserShortcut(Shortcut defaultShortcut) {
        shortcutList.add(new Shortcut(defaultShortcut.getId(), defaultShortcut.getPropertiesString(), defaultShortcut.getKeyCode(), defaultShortcut.getModifier()));
    }

    /**
     * Add all the default shortcuts to the usable list
     */
    public static void addAllDefaultShortcuts() {
        for (Shortcut defaultShortcut : defaultShortcutList) {
            LOG.info("    adding missing Shortcut: {}", defaultShortcut.getId());
            addMissingUserShortcut(defaultShortcut);
        }
    }

    /**
     * Registers a shortcut action to a given component.<br>
     * <b>NOTE:-</b> The owner of the shortcut can be null, but if yow want a callback to the
     * owner class, it is recommended to supply one, it is used to updateWidget the shortcut that is
     * displayed on the owner button tooltip.
     * @param owner The owner of the shortcut, can be null.
     * @param shortcut The Shortcut object containing the key code and modifiers for the shortcut.
     * @param action The Action to be performed when the shortcut is triggered.
     * @param component The JComponent to which the shortcut action will be registered.
     * @see Shortcut
     * @see Action
     */
    @SuppressWarnings("MagicConstant")
    public static void registerShortcut(Object owner, Shortcut shortcut, Action action, JComponent component) {

        if (bDebugLogShortcutInfo) {
            LOG.info("## ShortcutManager.registerShortcut() ## Registering shortcut: {} ( {} , {} )", shortcut.getId(), shortcut.getKeyCode(), shortcut.getModifier());
        }
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        shortcut.setInputMap(inputMap);
        shortcut.setOwner(owner);
        KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcut.getKeyCode(), shortcut.getModifier());
        if (bDebugLogShortcutInfo)LOG.info("## ShortcutManager.registerShortcut() ## Registering KeyStroke: {}", keyStroke);
        shortcut.setKeyCode(keyStroke.getKeyCode());
        shortcut.setModifiers(keyStroke.getModifiers());
        inputMap.put(keyStroke, shortcut.getId());
        actionMap.put(shortcut.getId(), action);
        shortcut.setEnabled(true);
    }

    @SuppressWarnings("MagicConstant")
    public static void registerMenuShortcut(JMenuItem owner, Shortcut shortcut, int keyCode, int modifier) {
        if (bDebugLogShortcutInfo) {
            LOG.info("## ShortcutManager.registerMenuShortcut() ## Registering shortcut: {} ( {} , {} )",
                    shortcut.getId(), shortcut.getKeyCode(), shortcut.getModifier());
        }
        KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcut.getKeyCode(), shortcut.getModifier());
        owner.setAccelerator(keyStroke);
        shortcut.setKeyCode(keyStroke.getKeyCode());
        shortcut.setModifiers(keyStroke.getModifiers());
        shortcut.setOwner(owner);
        shortcut.setEnabled(true);
        if (bDebugLogShortcutInfo) {
            LOG.info("## ShortcutManager.registerMenuShortcut() ## Registered KeyStroke: {}", keyStroke);
        }
    }

    public static void registerMenuShortcut(JMenuItem owner, String menuText, boolean isSelected, Shortcut shortcut) {
        if (bDebugLogShortcutInfo) {
            LOG.info("## ShortcutManager.registerMenuShortcut() ## Registering shortcut: {} ( {} , {} )",
                    shortcut.getId(), shortcut.getKeyCode(), shortcut.getModifier());
        }
        KeyStroke keyStroke = KeyStroke.getKeyStroke(shortcut.getKeyCode(), shortcut.getModifier());
        owner.setText(getLocaleString(menuText));
        owner.setAccelerator(keyStroke);
        owner.setSelected(isSelected);
        owner.addItemListener((ItemListener) owner);

        shortcut.setKeyCode(keyStroke.getKeyCode());
        shortcut.setModifiers(keyStroke.getModifiers());
        shortcut.setOwner(owner);

        shortcut.setEnabled(true);
        if (bDebugLogShortcutInfo) {
            LOG.info("## ShortcutManager.registerMenuShortcut() ## Registered KeyStroke: {}", keyStroke);
        }
    }

    /**
     * Updates an existing shortcut with a new key code and modifiers.
     *
     * @param id The id of the action to updateWidget.
     * @param newKeyCode The new key code for the shortcut.
     * @param newModifiers The new modifiers for the shortcut.
     */
    public static void updateShortcut(ShortcutID id, int newKeyCode, int newModifiers) {
        if (bDebugLogShortcutInfo) {
            LOG.info("updateShortcut() : Update '{}' to Keycode: {}, Modifiers: {}", id, newKeyCode, newModifiers);
        }
        for (Shortcut shortcut : shortcutList) {
            if (shortcut.getId().equals(id)) {
                if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.updateShortcut() ## Found '{}' in shortcut list, updating", id);
                // Update the shortcut with the new key code and modifiers
                shortcut.updateInputMap(newKeyCode, newModifiers);
                // Check if the owner uses the ShortcutGroupInterface
                if (shortcut.getOwner() instanceof ShortcutGroup.ShortcutGroups) {
                    // get the Owner group
                    ShortcutGroup g = ((ShortcutGroup.ShortcutGroups) shortcut.getOwner()).getGroup();
                    // get the list of group members
                    CircularList<BaseButton> groupMembers = ((ShortcutGroup.ShortcutGroups) shortcut.getOwner()).getGroupMembers();
                    // Update the tooltip for all buttons in the group
                    for (BaseButton button : groupMembers) {
                        if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.updateShortcut() ## Updating tooltips for {} buttons in group '{}'", groupMembers.getSize(), g.getGroupName() );
                        button.updateTooltip();
                    }
                } else if (shortcut.getOwner() instanceof BaseButton) {
                    // If the owner is a BaseButton, updateWidget its tooltip
                    BaseButton button = (BaseButton) shortcut.getOwner();
                    if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.updateShortcut() ## Updating button tooltip for '{}'", button.getButtonID());
                    button.updateTooltip();
                } else if (shortcut.getOwner() instanceof JMenuItem) {
                    if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.updateShortcut() ## Updating menu tooltip for '{}'", shortcut.getOwner().getClass().getSimpleName());
                    ((JMenuItem) shortcut.getOwner()).setAccelerator(shortcut.getKeyStroke());
                    if (shortcut.getCallbackObject() instanceof BaseButton) ((BaseButton) shortcut.getCallbackObject()).updateTooltip();
                } else {
                    LOG.info("## Shortcut = {}", shortcut);
                    LOG.info("## ShortcutManager.updateShortcut() ## Unknown condition met for '{}'", shortcut.getOwner().getClass().getSimpleName());
                }
                return;
            }
        }
        LOG.info("updateShortcut() : Shortcut not found for action '{}'", id);
    }

    /**
     * Retrieves the list of default shortcuts.
     *
     * @return A list of default shortcuts.
     */
    public static List<Shortcut> getDefaultShortcutList() { return defaultShortcutList;}

    /**
     * Retrieves the default shortcut for a given function name.
     *
     * @param functionName The name of the function.
     * @return The default Shortcut object for the function, or null if not found.
     */
    public static Shortcut getUserShortcutByName(String functionName) {
        if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.getUserShortcutByName() ## Find shortcut for '{}'", functionName);
        for (Shortcut shortcut : shortcutList) {
            if (shortcut.getLocalizedString().equals(functionName)) {
                if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.getUserShortcutByName() ## Found match for {} {}", functionName, shortcut);
                return shortcut;
            }
        }
        return null;
    }

    /**
     * Retrieves the shortcut for a given function name.
     *
     * @param shortcutID The id of the shortcut.
     * @return The Shortcut object for the function, or null if not found.
     */
    public static Shortcut getUserShortcutByID(ShortcutID shortcutID) {
        for (Shortcut shortcut : shortcutList) {
            if (shortcut.getId() == shortcutID) {
                return shortcut;
            }
        }
        LOG.info("## ShortcutManager.getUserShortcutByID() ## Shortcut not found for action '{}'", shortcutID.getXmlDescriptor());
        return null;
    }

    public static Shortcut getDefaultShortcutByName(String functionName) {
        if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.getDefaultShortcutByName() ## Searching for shortcut '{}'", functionName);
        for (Shortcut shortcut : ShortcutManager.getDefaultShortcutList()) {
            if (shortcut.getLocalizedString().equals(functionName) || shortcut.getId().getXmlDescriptor().equals(functionName)) {
                if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.getDefaultShortcutByName() ## Found match for {} {}", functionName, shortcut);
                return shortcut;
            }
        }
        return null;
    }

    public static Shortcut getDefaultShortcutByID(ShortcutID id) {
        if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.getDefaultShortcutByID() ## Find default shortcut for '{}'", id);
        for (Shortcut shortcut : ShortcutManager.getDefaultShortcutList()) {
            if (shortcut.getId().equals(id)) {
                if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.getDefaultShortcutByID() ## Found match for {} {}", id, shortcut);
                return shortcut;
            }
        }
        return null;
    }



    /**
     * Retrieves the list of all shortcuts.
     *
     * @return A list of all shortcuts.
     */
    public static List<Shortcut> getAllShortcuts() {
        return shortcutList;
    }

    /**
     * Resets all shortcuts to their default values.
     */
    public static void resetAllToDefault() {
        if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.resetAllToDefault() ## Reset all shortcuts to default");
        for (Shortcut shortcut : shortcutList) { shortcut.reset(); }
    }

    public static void clearAllShortcuts() {
        if (bDebugLogShortcutInfo) LOG.info("## ShortcutManager.clearAllShortcuts() ## Clearing all shortcuts");
        for (Shortcut shortcut : shortcutList) { shortcut.clear(); }
    }

    /**
     * Checks if a shortcut is already in use.
     *
     * @param keyCode The key code of the shortcut.
     * @param modifiers The modifiers of the shortcut.
     * @return True if the shortcut is in use, false otherwise.
     */
    public static boolean checkIfShortcutInUse(int keyCode, int modifiers) {
        for (Shortcut shortcut : shortcutList) {
            if (shortcut.getKeyCode() == keyCode && shortcut.getModifier() == modifiers) {
                return true;
            }
        }
        return false;
    }


    @SuppressWarnings("MagicConstant")
    public static void checkUnregisteredShortcuts() {
        LOG.info("Checking for unregistered shortcuts...");
        for (Shortcut shortcut : shortcutList) {
            KeyStroke k = KeyStroke.getKeyStroke(shortcut.getKeyCode(), shortcut.getModifier());
            if (shortcut.getInputMap() == null || shortcut.getInputMap().get(k) == null) {
                LOG.info("Shortcut '{}' Not Registered", shortcut.getLocalizedString());

            }
        }
    }

    /**
     * Converts a string representation of modifiers to an integer.
     *
     * @param modifiersString The string representation of the modifiers.
     * @return The integer representation of the modifiers.
     */
    public static int getModifiersIntFromString(String modifiersString) {
        int modifiers = 0;
        if (modifiersString.contains("Ctrl") || modifiersString.contains("CTRL") || modifiersString.contains("CONTROL") || modifiersString.contains("CONTROL_DOWN_MASK")) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
        }
        if (modifiersString.contains("Shift") || modifiersString.contains("SHIFT") || modifiersString.contains("SHIFT_DOWN_MASK")) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
        }
        if (modifiersString.contains("Alt") || modifiersString.contains("ALT") || modifiersString.contains("ALT_DOWN_MASK")) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
        }
        return modifiers;
    }

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

    /**
     * Retrieves the integer representation of a VK_ constant name.
     *
     * @param vkName The VK_ constant name.
     * @return The integer representation of the VK_ constant, or 0 if not found.
     */
    public static int getVKInteger(String vkName) {
        if (!vkName.isEmpty() ) {
            // use reflection to try and get the integer value of the supplied VK_ constant name
            try {
                Field field = KeyEvent.class.getField(vkName);
                return field.getInt(null);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOG.info("getVKInteger() : VK_{} not found", vkName);
                e.printStackTrace();
                return 0;// Return 0 if the field is not found or accessible
            }
        } else {
            return 0;
        }
    }
}
