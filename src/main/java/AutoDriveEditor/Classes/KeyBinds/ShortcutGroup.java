package AutoDriveEditor.Classes.KeyBinds;

import AutoDriveEditor.Classes.CircularList;
import AutoDriveEditor.GUI.Buttons.BaseButton;

import java.util.ArrayList;
import java.util.List;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogShortcutInfoMenu.bDebugLogShortcutInfo;

@SuppressWarnings("unused")
public class ShortcutGroup {

    // List to hold all shortcut groups
    private static final ArrayList<ShortcutGroup> shortcutGroupList = new ArrayList<>();

    // Group name and list of ButtonNodes, each group can contain multiple buttons
    private final String groupName;
    private final CircularList<BaseButton> buttonNodeList;


    public interface ShortcutGroups {
        ShortcutGroup getGroup();
        CircularList<BaseButton> getGroupMembers();
    }

    /**
     * Constructor for ShortCutGroup
     * Initializes the group with a name and an empty CircularList for ButtonNodes.
     * @param groupName Name of shortcut group in String format
     */
    public ShortcutGroup(String groupName) {
        this.groupName = groupName;
        this.buttonNodeList = new CircularList<>();
    }

    /**
     * Creates a new Shortcut Group with the specified name.
     * @param name Name of the shortcut group to be created in String format.
     * @return Reference to the created group
     */
    public static ShortcutGroup createShortcutGroup(String name) {
        if (bDebugLogShortcutInfo) LOG.info("Creating new Shortcut Group with name: {}", name);
        ShortcutGroup newGroup = new ShortcutGroup(name);
        shortcutGroupList.add(newGroup);
        return newGroup;
    }

    /**
     * Adds a ButtonNode to the current Shortcut Group.
     * @param buttonNode ButtonNode to be added to the group
     */
    public void addButton(BaseButton buttonNode) {
        if (bDebugLogShortcutInfo) LOG.info("Adding button '{}' to shortcut group {}", buttonNode.getButtonID(), groupName);
        buttonNodeList.add(buttonNode);
    }

    /**
     * * Adds a ButtonNode to the current Shortcut Group at a specific index.
     * @param index The index number the ButtonNode is inserted into the list
     * @param buttonNode ButtonNode to be added to the group
     */
    public void addButton(int index, BaseButton buttonNode) {
        if (bDebugLogShortcutInfo) LOG.info("Adding button {} to group {} at index {}", buttonNode.getButtonID(), groupName, index);
        if (buttonNode != null) {
            buttonNodeList.add(buttonNode, index);
        } else {
            LOG.info("ButtonNode is null, cannot add to group {}", groupName);
        }
    }

    /**
     * * Returns the list of all Shortcut Groups.
     * @return List of all Shortcut Groups
     */
    public List<ShortcutGroup> getButtonGroups() {
        return shortcutGroupList;
    }

    /**
     * Returns a Shortcut Group by its name.
     * @param name Name of the Shortcut Group to be retrieved
     * @return ShortCutGroup with the specified name, or null if not found
     */
    public static ShortcutGroup getButtonGroup(String name) {
        for (ShortcutGroup group : shortcutGroupList) {
            if (group.groupName.equals(name)) {
                return group;
            }
        }
        return null;
    }

    /**
     * Returns the name of the Shortcut Group.
     * @return Name of the Shortcut Group
     */
    public String getGroupName() { return groupName; }

    /**
     * Returns the CircularList of the specified Shortcut Group.
     * @return CircularList of ButtonNodes
     */
    public CircularList<BaseButton> getButtonNodeList() { return buttonNodeList; }

    /**
     * Returns the current selected ButtonNode in the CircularList.
     * @return Current ButtonNode
     */
    public BaseButton getCurrentButton() { return buttonNodeList.get(); }

    /**
     * Returns the next ButtonNode in the CircularList.
     * @return Next ButtonNode
     */
    public BaseButton getNextButton() { return buttonNodeList.next(); }

    /**
     * Returns the previous ButtonNode in the CircularList.
     * @return Previous ButtonNode
     */
    public BaseButton getPreviousButton() { return buttonNodeList.previous(); }



}
