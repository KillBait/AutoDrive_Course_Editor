package AutoDriveEditor.Managers;

import AutoDriveEditor.Classes.UI_Components.DropdownToggleButton;
import AutoDriveEditor.Classes.UI_Components.JToggleStateButton;
import AutoDriveEditor.Classes.UI_Components.ScaleAnimIcon;
import AutoDriveEditor.Classes.UI_Components.ToolbarButton;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogGUIInfoMenu.bDebugLogGUIInfo;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

/**
 * The IconManager is responsible for managing the icons used in the
 * editor and any user selected overrides.
 */
@SuppressWarnings("unused")
public class IconManager {

    private static BufferedImage tractorImage;

    private static ImageIcon updateIcon;
    private static ImageIcon markerIcon;
    private static ImageIcon gameIcon;
    private static ImageIcon routeIcon;


    // Initialize the icon list
    private static List<EditorIcon> iconList;

    // Names that are used to access and identify icons
    public static final String TRACTOR_ICON = "Tractor";
    public static final String LOGO = "Logo";
    public static final String FLATLAF_ICON = "FlatLaf";
    public static final String UPDATE_DIALOG_ICON = "Update_Dialog_Icon";
    public static final String MARKER_DIALOG_ICON = "Marker_Dialog_Icon";

    public static final String MARKER_ICON = "Marker";
    public static final String PARKING_ICON = "Parking_Icon";
    public static final String WARNING_ICON = "Warning_Icon";
    public static final String WARNING_Y_ICON = "Warning_Y_Icon";
    public static final String NODE_ICON = "Node_Icon";
    public static final String CONTROL_NODE_ICON = "Control_Node_Icon";
    public static final String CONTROL_NODE_SELECTED_ICON = "Control_Node_Selection";
    public static final String ROTATE_NODE_ICON = "Rotate_Node";
    public static final String ROTATE_NODE_SELECTED_ICON = "Rotate_Node_Selection";
    public static final String NODE_SELECTION_ICON = "Node_Selection";
    public static final String RADIUS_NODE_ICON = "Radius_Selection";
    public static final String CONFIRM_ICON = "Confirm";
    public static final String CANCEL_ICON = "Cancel";
    public static final String RESET_ICON = "Reset";
    public static final String GAME_ICON = "Game_Icon";
    public static final String ROUTE_ICON = "Route_Icon";

    public static final String MENU_ICON = "Menu";
    public static final String VERTICAL_SEPERATOR_ICON = "Vertical_Separator";
    public static final String UNDO_ICON = "Undo";
    public static final String REDO_ICON = "Redo";
    public static final String SELECTION_ICON = "Selection";
    public static final String SELECTION_FREEFORM_ICON = "Selection_Freeform";
    public static final String CUT_ICON = "Cut";
    public static final String COPY_ICON = "Copy";
    public static final String PASTE_ICON = "Paste";
    public static final String MENU_DROPDOWN_ICON = "Menu_Dropdown";
    public static final String MENU_DROPDOWN_ROTATED_ICON = "Menu_Dropdown_Rotated";
    public static final String OPEN_CONFIG_ICON = "Open_Config";
    public static final String CONTINIOUS_CONNECT_ON_ICON = "Continuous_Connect_ON";
    public static final String CONTINIOUS_CONNECT_OFF_ICON = "Continuous_Connect_OFF";
    public static final String NODE_UP_ICON = "Node_Up";
    public static final String NODE_DOWN_ICON = "Node_Down";
    public static final String GRID_SNAP_ON_ICON = "Grid_Snapping_On";
    public static final String GRID_SNAP_OFF_ICON = "Grid_Snapping_Off";
    public static final String SUB_SNAP_ON_ICON = "Subdivision_Snapping_On";
    public static final String SUB_SNAP_OFF_ICON = "Subdivision_Snapping_Off";
    public static final String GRID_ON_ICON = "Grid_On";
    public static final String GRID_OFF_ICON = "Grid_Off";
    public static final String ROTATE_SNAP_ON_ICON = "Rotate_Snap_ON";
    public static final String ROTATE_SNAP_OFF_ICON = "Rotate_Snap_OFF";
    public static final String ROTATE_CONFIRM_ICON = "Confirm";
    public static final String TEXT_X_ICON = "Text_X";
    public static final String TEXT_Y_ICON = "Text_Y";
    public static final String TEXT_S_ICON = "Text_S";
    public static final String TEXT_PERCENT_ICON = "Text_Percent";
    public static final String AUTOSAVE_ON_ICON = "Autosave_On";
    public static final String AUTOSAVE_OFF_ICON = "Autosave_Off";

    public static final String SELECT_HIDDEN_ON_ICON = "Select_Hidden_On";
    public static final String SELECT_HIDDEN_OFF_ICON = "Select_Hidden_Off";
    public static final String GLOBE_HEIGHTMAP_ICON = "Globe_Heightmap";
    public static final String GLOBE_MAP_ICON = "Globe_Map";
    public static final String GLOBE_MANUAL_ICON = "Globe_Manual";
    public static final String GLOBE_IMPORT_ICON = "Globe_Import";
    public static final String GLOBE_ERROR_ICON = "Globe_Error";
    public static final String DOWNLOAD_ICON = "Download_Icon";
    public static final String MONITOR_ICON = "Monitor_Icon";
    public static final String AUTOSAVE_ICON = "Autosave_Icon";
    public static final String TIME_ICON = "Time_Icon";
    public static final String MOVEMENT_ICON = "Movement_Icon";
    public static final String CURVE_ICON = "Curve_Icon";
    public static final String CONNECT_ICON = "Connect_Icon";
    public static final String HIDDEN_ICON = "Hidden_Icon";

    // Colour Wheel Icons
    public static final String COLOURWHEEL_POINT = "ColourWheel_Point";
    public static final String COLOURTRIANGE_POINT = "Colour_Triangle_Point";
    public static final String SWATCH_ICON = "Swatch_Icon";
    public static final String SWATCH_SELECTED_ICON = "Swatch_Selected_Icon";

    // Toolbar Icons
    public static final String NODE_PANEL_ICON = "Node_Panel_Icon";
    public static final String CURVE_PANEL_ICON = "Curves_Panel_Icon";
    public static final String MARKER_PANEL_ICON = "Marker_Panel_Icon";
    public static final String ALIGNMENT_PANEL_ICON = "Alignment_Panel_Icon";
    public static final String VISIBILITY_PANEL_ICON = "Visibility_Panel_Icon";
    public static final String EXPERIMENTAL_PANEL_ICON = "Experimental_Panel_Icon";

    public static final String BUTTON_MOVE_NODE_ICON = "Move_Node_Icon";
    public static final String BUTTON_ADD_NODE_REGULAR_ICON = "Add_Regular_Node_Icon";
    public static final String BUTTON_ADD_NODE_SUBPRIO_ICON = "Add_Subprio_Node_Icon";
    public static final String BUTTON_ADD_NORMAL_CONNECTION_ICON = "Add_Normal_Connection_Icon";
    public static final String BUTTON_ADD_SUBPRIO_NORMAL_CONNECTION_ICON = "Add_Subprio_Normal_Connection_Icon";
    public static final String BUTTON_ADD_DUAL_CONNECTION_ICON = "Add_Dual_Connection_Icon";
    public static final String BUTTON_ADD_SUBPRIO_DUAL_CONNECTION_ICON = "Add_Subprio_Dual_Connection_Icon";
    public static final String BUTTON_ADD_REVERSE_CONNECTION_ICON = "Add_Reverse_Connection_Icon";
    public static final String BUTTON_ADD_SUBPRIO_REVERSE_CONNECTION_ICON = "Add_Subprio_Reverse_Connection_Icon";


    public static final String BUTTON_SWAP_PRIORITY_ICON = "Swap_Priority_Icon";
    public static final String BUTTON_FLIP_CONNECTION_ICON = "Swap_Direction_Icon";
    public static final String CURVEPANEL_SWAP_DIRECTION_ICON = "Swap_Direction_Icon2";

    public static final String BUTTON_ROTATE_NODE_ICON = "Rotate_Node_Icon";
    public static final String BUTTON_DELETE_ICON = "Delete_Icon";
    public static final String BUTTON_BEZIER_CURVE_ICON = "Bezier_Curve_Icon";
    public static final String BUTTON_ARC_SPLINE_ICON = "Arc_Spline_Icon";
    public static final String BUTTON_ADD_MARKER_ICON = "Add_Marker_Icon";
    public static final String BUTTON_EDIT_MARKER_ICON = "Edit_Marker_Icon";
    public static final String BUTTON_REMOVE_MARKER_ICON = "Remove_Marker_Icon";
    public static final String BUTTON_ALIGN_HORIZONTAL_ICON = "Align_Horizontal_Icon";
    public static final String BUTTON_ALIGN_VERTICAL_ICON = "Align_Vertical_Icon";
    public static final String BUTTON_ALIGN_TERRAIN_ICON = "Align_Terrain_Icon";
    public static final String BUTTON_ALIGN_EDIT_ICON = "Align_Edit_Icon";
    public static final String BUTTON_VISIBILITY_NODE_ICON = "Visibility_Node_Icon";
    public static final String BUTTON_VISIBILITY_CONNECT_REGULAR_ICON = "Visibility_Connect_Regular_Icon";
    public static final String BUTTON_VISIBILITY_CONNECT_SUBPRIO_ICON = "Visibility_Connect_Subprio_Icon";
    public static final String BUTTON_VISIBILITY_CONNECT_DUAL_ICON = "Visibility_Connect_Dual_Icon";
    public static final String BUTTON_VISIBILITY_CONNECT_REVERSE_ICON = "Visibility_Connect_Reverse_Icon";

    public static final String TEMPLATE_ICON = "Template_Icon";
    public static final String ROAD_ICON = "Road_Icon";
    public static final String SCALE_ICON = "Scale_Icon";

    /**
     * Initialize all the editor icons and set the default icons.
     */
    public IconManager() {
        LOG.info("  Initializing Icon Manager");
        // Initialize the icon list
        iconList = new ArrayList<>();
        // Register the default icons
        registerIcons();
        // Load the user icons
        updateIcon = getSVGIcon(UPDATE_DIALOG_ICON, 64, 64);
        markerIcon = getSVGIcon(MARKER_DIALOG_ICON, 45, 60);
        // Recent menu icons
        gameIcon = getSVGIcon(GAME_ICON, 40, 15);
        routeIcon = getSVGIcon(ROUTE_ICON, 40, 15);
    }

    private void registerIcons() {

        //
        // Apply the global colour filter for all FlatSVG icons, modify the default colours for dark and light themes
        //
        FlatSVGIcon.ColorFilter.getInstance().add(Color.BLACK, null, Color.LIGHT_GRAY);


        //
        // Initialize the default icons
        //

        // Window Icon
        iconList.add(new EditorIcon(TRACTOR_ICON,"editor/tractor_icon.svg", 25, 25));
        // Map Panel
        iconList.add(new EditorIcon(LOGO,"editor/logo.svg", 300, 100));
        iconList.add(new EditorIcon(UPDATE_DIALOG_ICON,"editor/update_dialog_icon.svg", 20, 20));
        iconList.add(new EditorIcon(MARKER_DIALOG_ICON,"editor/marker_dialog_icon.svg", 20, 20));
        iconList.add(new EditorIcon(FLATLAF_ICON,"editor/flatlaf_icon.svg", 16, 16));
        iconList.add(new EditorIcon(MARKER_ICON, "editor/nodes/marker_icon.svg", 20, 20));
        iconList.add(new EditorIcon(PARKING_ICON, "editor/nodes/parking_icon.svg", 20, 20));
        iconList.add(new EditorIcon(WARNING_ICON, "editor/nodes/warning_icon.svg", 20, 20));
        iconList.add(new EditorIcon(WARNING_Y_ICON, "editor/nodes/warning_y_icon.svg", 20, 20));
        iconList.add(new EditorIcon(NODE_ICON, "editor/nodes/node_icon.svg", 20, 20));
        iconList.add(new EditorIcon(CONTROL_NODE_ICON, "editor/nodes/control_node_icon.svg", 20, 20));
        iconList.add(new EditorIcon(CONTROL_NODE_SELECTED_ICON, "editor/nodes/control_node_selected_icon.svg", 20, 20));
        iconList.add(new EditorIcon(ROTATE_NODE_ICON, "editor/nodes/rotate_node_icon.svg", 20, 20));
        iconList.add(new EditorIcon(ROTATE_NODE_SELECTED_ICON, "editor/nodes/rotate_node_selected_icon.svg", 20, 20));
        iconList.add(new EditorIcon(RADIUS_NODE_ICON, "editor/nodes/radius_node_icon.svg", 20, 20));
        iconList.add(new EditorIcon(NODE_SELECTION_ICON, "editor/nodes/node_selection_ring.svg", 15, 15));
        iconList.add(new EditorIcon(CONFIRM_ICON, "editor/toolbar/buttons/confirm_icon.svg", 20, 20));
        iconList.add(new EditorIcon(CANCEL_ICON, "editor/toolbar/buttons/cancel_icon.svg", 20, 20));
        iconList.add(new EditorIcon(RESET_ICON, "editor/toolbar/buttons/reset_icon.svg", 20, 20));
        iconList.add(new EditorIcon(GAME_ICON,"editor/game_icon.svg", 30, 15));
        iconList.add(new EditorIcon(ROUTE_ICON,"editor/route_icon.svg", 30, 15));


        //
        // Actionbar Icons
        //

        // Popup Menu Icon
        iconList.add(new EditorIcon(MENU_ICON,"editor/actionbar/menu_icon.svg", 15, 20));

        // Separator Icon
        iconList.add(new EditorIcon(VERTICAL_SEPERATOR_ICON,"editor/actionbar/vertical_separator_icon.svg", 2, 10));

        // Undo/Redo Panel Icons
        iconList.add(new EditorIcon(UNDO_ICON,"editor/actionbar/undo_icon.svg", 20, 20));
        iconList.add(new EditorIcon(REDO_ICON,"editor/actionbar/redo_icon.svg", 20, 20));

        // Selection Panel Icons
        iconList.add(new EditorIcon(SELECTION_ICON,"editor/actionbar/select_icon.svg", 20, 20));
        iconList.add(new EditorIcon(SELECTION_FREEFORM_ICON,"editor/actionbar/select_freeform_icon.svg", 20, 20));
        iconList.add(new EditorIcon(CUT_ICON,"editor/actionbar/cut_icon.svg", 20, 20));
        ColorFilter copyButtonFilter = ColorFilter.getInstance()
                .add( new Color(210,210,210), null, new Color(95,95,95) )
                .add( new Color(245,245,245), null, new Color(120,120,120));
        iconList.add(new EditorIcon(COPY_ICON,"editor/actionbar/copy_icon.svg", 20, 20, copyButtonFilter));
        ColorFilter pasteButtonFilter = ColorFilter.getInstance()
                .add( new Color(239,217,145), null, new Color(139,117,45))
                .add( new Color(245,245,245), null, new Color(120,120,120));
        iconList.add(new EditorIcon(PASTE_ICON,"editor/actionbar/paste_icon.svg", 20, 20, pasteButtonFilter));

        // Autosave Panel Icons
        iconList.add(new EditorIcon(AUTOSAVE_ON_ICON,"editor/actionbar/autosave_on_icon.svg", 20, 20));
        iconList.add(new EditorIcon(AUTOSAVE_OFF_ICON,"editor/actionbar/autosave_off_icon.svg", 20, 20));

        // ConfigBar Icons
        iconList.add(new EditorIcon(MENU_DROPDOWN_ICON,"editor/actionbar/menu_dropdown_icon.svg", 6, 4));
        iconList.add(new EditorIcon(MENU_DROPDOWN_ROTATED_ICON, "editor/actionbar/menu_dropdown_rotated_icon.svg", 6, 4));
        iconList.add(new EditorIcon(OPEN_CONFIG_ICON,"editor/actionbar/settings_icon.svg", 20, 20));
        iconList.add(new EditorIcon(CONTINIOUS_CONNECT_ON_ICON,"editor/actionbar/continuous_connect_on_icon.svg", 20, 20));
        iconList.add(new EditorIcon(CONTINIOUS_CONNECT_OFF_ICON,"editor/actionbar/continuous_connect_off_icon.svg", 20, 20));
        iconList.add(new EditorIcon(NODE_UP_ICON,"editor/actionbar/node_up_icon.svg", 20, 20));
        iconList.add(new EditorIcon(NODE_DOWN_ICON,"editor/actionbar/node_down_icon.svg", 20, 20));
        iconList.add(new EditorIcon(GRID_SNAP_ON_ICON,"editor/actionbar/snap_grid_on_icon.svg", 20, 20));
        iconList.add(new EditorIcon(GRID_SNAP_OFF_ICON,"editor/actionbar/snap_grid_off_icon.svg", 20, 20));
        iconList.add(new EditorIcon(SUB_SNAP_ON_ICON,"editor/actionbar/snap_sub_on_icon.svg", 20, 20));
        iconList.add(new EditorIcon(SUB_SNAP_OFF_ICON,"editor/actionbar/snap_sub_off_icon.svg", 20, 20));
        iconList.add(new EditorIcon(GRID_ON_ICON,"editor/actionbar/grid_on_icon.svg", 20, 20));
        iconList.add(new EditorIcon(GRID_OFF_ICON,"editor/actionbar/grid_off_icon.svg", 20, 20));
        iconList.add(new EditorIcon(ROTATE_SNAP_ON_ICON,"editor/actionbar/rotate_snap_on_icon.svg", 20, 20));
        iconList.add(new EditorIcon(ROTATE_SNAP_OFF_ICON,"editor/actionbar/rotate_snap_off_icon.svg", 20, 20));
        iconList.add(new EditorIcon(ROTATE_CONFIRM_ICON, "editor/toolbar/buttons/confirm_icon.svg", 20, 20));


        iconList.add(new EditorIcon(TEXT_X_ICON,"editor/actionbar/x_icon.svg", 10, 12));
        iconList.add(new EditorIcon(TEXT_Y_ICON,"editor/actionbar/y_icon.svg", 10, 12));
        iconList.add(new EditorIcon(TEXT_S_ICON,"editor/actionbar/s_icon.svg", 10, 12));
        iconList.add(new EditorIcon(TEXT_PERCENT_ICON,"editor/actionbar/percent_icon.svg", 12, 12));
        iconList.add(new EditorIcon(GLOBE_HEIGHTMAP_ICON,"editor/actionbar/globe_heightmap_icon.svg", 20, 20));
        iconList.add(new EditorIcon(GLOBE_MAP_ICON,"editor/actionbar/globe_map_icon.svg", 20, 20));
        iconList.add(new EditorIcon(GLOBE_MANUAL_ICON,"editor/actionbar/globe_map_manual_icon.svg", 20, 20));
        iconList.add(new EditorIcon(GLOBE_IMPORT_ICON,"editor/actionbar/globe_map_imported_icon.svg", 20, 20));
        iconList.add(new EditorIcon(GLOBE_ERROR_ICON,"editor/actionbar/globe_map_error_icon.svg", 20, 20));
        iconList.add(new EditorIcon(SELECT_HIDDEN_ON_ICON,"editor/actionbar/select_hidden_on_icon.svg", 20, 20));
        iconList.add(new EditorIcon(SELECT_HIDDEN_OFF_ICON,"editor/actionbar/select_hidden_off_icon.svg", 20, 20));

        // Config Panel icons
        iconList.add(new EditorIcon(DOWNLOAD_ICON,"editor/config/download_icon.svg", 20, 25));
        iconList.add(new EditorIcon(MONITOR_ICON,"editor/config/monitor_icon.svg", 40, 25));
        iconList.add(new EditorIcon(AUTOSAVE_ICON,"editor/config/autosave_icon.svg", 30, 30));
        iconList.add(new EditorIcon(TIME_ICON,"editor/config/time_icon.svg", 35, 35));
        iconList.add(new EditorIcon(MOVEMENT_ICON,"editor/config/scale_speed_icon.svg", 30, 27));
        iconList.add(new EditorIcon(CURVE_ICON, "editor/config/curve_points_icon.svg", 40, 40));
        iconList.add(new EditorIcon(CONNECT_ICON,"editor/config/line_ruler_icon.svg", 30, 30));
        iconList.add(new EditorIcon(HIDDEN_ICON,"editor/config/visibility_icon.svg", 35, 35));
        iconList.add(new EditorIcon(RESET_ICON, "editor/toolbar/buttons/reset_icon.svg", 35, 35));

        // ColourWheel icons
        iconList.add(new EditorIcon(COLOURWHEEL_POINT,"editor/colourwheel/point_icon.svg", 20, 20));
        iconList.add(new EditorIcon(COLOURTRIANGE_POINT,"editor/colourwheel/point_icon.svg", 20, 20));
        iconList.add(new EditorIcon(SWATCH_ICON,"editor/colourwheel/swatch_icon.svg", 20, 20));
        iconList.add(new EditorIcon(SWATCH_SELECTED_ICON,"editor/colourwheel/swatch_selected_icon.svg", 20, 20));


        // Toolbar icons

        iconList.add(new EditorIcon(NODE_PANEL_ICON, "editor/toolbar/node_panel_icon.svg", 20, 20));
        iconList.add(new EditorIcon(CURVE_PANEL_ICON, "editor/toolbar/curve_panel_icon.svg", 20, 20));
        iconList.add(new EditorIcon(MARKER_PANEL_ICON, "editor/toolbar/marker_panel_icon.svg", 20, 20));
        iconList.add(new EditorIcon(ALIGNMENT_PANEL_ICON, "editor/toolbar/alignment_panel_icon.svg", 20, 20));
        iconList.add(new EditorIcon(VISIBILITY_PANEL_ICON, "editor/toolbar/visibility_panel_icon.svg", 20, 20));
        iconList.add(new EditorIcon(EXPERIMENTAL_PANEL_ICON,"editor/toolbar/experimental_panel_icon.svg", 19, 19));

        // Node Panel
        iconList.add(new EditorIcon(BUTTON_MOVE_NODE_ICON, "editor/toolbar/buttons/move_node_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ADD_NODE_REGULAR_ICON, "editor/toolbar/buttons/add_node_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ADD_NODE_SUBPRIO_ICON, "editor/toolbar/buttons/add_subprio_node_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ADD_NORMAL_CONNECTION_ICON, "editor/toolbar/buttons/connection_regular_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ADD_SUBPRIO_NORMAL_CONNECTION_ICON, "editor/toolbar/buttons/connection_regular_alt_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ADD_DUAL_CONNECTION_ICON, "editor/toolbar/buttons/connection_dual_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ADD_SUBPRIO_DUAL_CONNECTION_ICON, "editor/toolbar/buttons/connection_dual_alt_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ADD_REVERSE_CONNECTION_ICON, "editor/toolbar/buttons/connection_reverse_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ADD_SUBPRIO_REVERSE_CONNECTION_ICON, "editor/toolbar/buttons/connection_reverse_alt_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_SWAP_PRIORITY_ICON, "editor/toolbar/buttons/swap_priority_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_FLIP_CONNECTION_ICON, "editor/toolbar/buttons/swap_direction_icon.svg", 20, 20));
        iconList.add(new EditorIcon(CURVEPANEL_SWAP_DIRECTION_ICON, "editor/toolbar/buttons/curve_swap_direction.svg", 20, 20));

        iconList.add(new EditorIcon(BUTTON_ROTATE_NODE_ICON, "editor/toolbar/buttons/rotate_node_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_DELETE_ICON, "editor/toolbar/buttons/delete_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_BEZIER_CURVE_ICON, "editor/toolbar/buttons/bezier_curve_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ARC_SPLINE_ICON, "editor/toolbar/buttons/arc_spline_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ADD_MARKER_ICON, "editor/toolbar/buttons/marker_add_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_EDIT_MARKER_ICON, "editor/toolbar/buttons/marker_edit_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_REMOVE_MARKER_ICON, "editor/toolbar/buttons/marker_remove_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ALIGN_HORIZONTAL_ICON, "editor/toolbar/buttons/align_horizontal_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ALIGN_VERTICAL_ICON, "editor/toolbar/buttons/align_vertical_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ALIGN_TERRAIN_ICON, "editor/toolbar/buttons/align_terrain_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_ALIGN_EDIT_ICON, "editor/toolbar/buttons/align_edit_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_VISIBILITY_NODE_ICON, "editor/toolbar/buttons/visibility_node_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_VISIBILITY_CONNECT_REGULAR_ICON, "editor/toolbar/buttons/visibility_connect_regular_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_VISIBILITY_CONNECT_SUBPRIO_ICON, "editor/toolbar/buttons/visibility_connect_subprio_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_VISIBILITY_CONNECT_DUAL_ICON, "editor/toolbar/buttons/visibility_connect_dual_icon.svg", 20, 20));
        iconList.add(new EditorIcon(BUTTON_VISIBILITY_CONNECT_REVERSE_ICON, "editor/toolbar/buttons/visibility_connect_reverse_icon.svg", 20, 20));

        // Experimental Panel
        iconList.add(new EditorIcon(TEMPLATE_ICON, "editor/toolbar/buttons/template_icon.svg", 20, 20));
        iconList.add(new EditorIcon(ROAD_ICON, "editor/toolbar/buttons/road_icon.svg", 20, 20));
        iconList.add(new EditorIcon(SCALE_ICON, "editor/toolbar/buttons/scale_icon.svg", 20, 20));



    }

    public static FlatSVGIcon getSVGIcon(String iconName, int width, int height) {
        for (EditorIcon iconEntry : iconList) {
            if (iconEntry.iconName.equals(iconName)) {
                if (iconEntry.userIcon == null) {
                    return new FlatSVGIcon(iconEntry.defaultIconPath, width, height);
                } else {
                    return new FlatSVGIcon(iconEntry.userIconPath, width, height);
                }
            }
        }
        return null;
    }

    public static FlatSVGIcon getSVGIcon(String iconName) {
        for (EditorIcon iconEntry : iconList) {
            if (iconEntry.iconName.equals(iconName)) {
                if (iconEntry.userIcon == null) {
                    return iconEntry.defaultIcon;
                } else {
                    return iconEntry.userIcon;
                }
            }
        }
        return null;
    }

    public static Image getIconImage(String iconName) {
        for (EditorIcon iconEntry : iconList) {
            if (iconEntry.iconName.equals(iconName)) {
                if (iconEntry.userIcon == null) {
                    return iconEntry.defaultIcon.getImage();
                } else {
                    return iconEntry.userIcon.getImage();
                }
            }
        }
        return null;
    }

    public static BufferedImage getSVGBufferImage(String iconName, int width, int height) {
        return getSVGBufferImage(iconName, width, height, null);
    }

    public static BufferedImage getSVGBufferImage(String iconName, int width, int height, ColorFilter filter) {
        FlatSVGIcon icon = null;
        for (EditorIcon iconEntry : iconList) {
            if (iconEntry.iconName.equals(iconName)) {
                if (iconEntry.userIcon == null) {
                    icon = iconEntry.defaultIcon;
                } else {
                    icon = iconEntry.userIcon;
                }
            }
        }
        return getSVGBufferImage(icon, width, height, filter);
    }

    public static BufferedImage getSVGBufferImage(FlatSVGIcon icon, int width, int height, FlatSVGIcon.ColorFilter colorFilter) {
        if (icon != null) {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB );
            Graphics2D g2 = image.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            try {
                // scale from base size to passed size
                double sx = (image.getWidth() > 0) ? (float) image.getWidth() / icon.getWidth() : 1;
                double sy = (image.getHeight() > 0) ? (float) image.getHeight() / icon.getHeight() : 1;
                if( sx != 1 || sy != 1 )
                    g2.scale( sx, sy );

                icon.paintIcon(null, g2, 0, 0 );
            } finally {
                g2.dispose();
            }
            g2.dispose();
            return image;
        }
        return null;
    }

    public static String getIconName(String iconName) {
        for (EditorIcon icon : iconList) {
            if (icon.iconName.equals(iconName)) {
                if (icon.userIcon == null) {
                    return icon.defaultIcon.getName();
                } else {
                    return icon.userIcon.getName();
                }
            }
        }
        return null;
    }

    public static String getIconPath(String iconName) {
        for (EditorIcon icon : iconList) {
            if (icon.iconName.equals(iconName)) return icon.getIconPath();
        }
        return null;
    }

    public static void addColourFilter(FlatSVGIcon icon, Color fromColour, Color toColour) {
        if (icon != null) {
            FlatSVGIcon.ColorFilter customFilter = new FlatSVGIcon.ColorFilter((color) -> {
                if (color.equals(fromColour)) {
                    return toColour;
                }
                return color;
            });
            icon.setColorFilter(customFilter);
        }
    }

    public static void addDarkColourFilter(boolean isDark, FlatSVGIcon icon, Color fromColour, Color toColour) {
        if (icon != null) {
            FlatSVGIcon.ColorFilter customFilter = new FlatSVGIcon.ColorFilter((color) -> {
                if (color.equals(fromColour) && FlatLaf.isLafDark() == isDark) {
                    return toColour;
                }
                return color;
            });
            icon.setColorFilter(customFilter);
        }
    }

    public static ToolbarButton createAnimToolbarButton(Icon icon, JPanel panel, String toolTipText, String altText, boolean isSelected, boolean isEnabled, ActionListener actionListener) {
        return (ToolbarButton) createButton(new ToolbarButton(panel, icon), icon, panel, toolTipText, altText, isSelected, isEnabled, actionListener);
    }

    public static DropdownToggleButton createAnimDropdownToggleButton(Icon icon, JPanel panel, String toolTipText, String altText, boolean isSelected, boolean isEnabled, ActionListener actionListener) {
        return (DropdownToggleButton) createButton(new DropdownToggleButton(panel, icon), icon, panel, toolTipText, altText, isSelected, isEnabled, actionListener);
    }

    public static JToggleButton createAnimToggleButton(ScaleAnimIcon animIcon, JPanel panel, String toolTipText, String altText, boolean isSelected, boolean enabled, ActionListener actionListener) {
        return (JToggleButton) createButton(new JToggleButton(), animIcon, panel, toolTipText, altText, isSelected, enabled, actionListener);
    }

    public static JToggleStateButton createAnimToggleStateButton(ScaleAnimIcon animIcon, JPanel panel, String toolTipText, String altText, boolean isSelected, boolean enabled, ActionListener actionListener) {
        return (JToggleStateButton) createButton(new JToggleStateButton(animIcon, panel, toolTipText, altText, isSelected, enabled, actionListener), animIcon, panel, toolTipText, altText, isSelected, enabled, actionListener);
    }

    public static JButton createAnimButton(ScaleAnimIcon animIcon, JPanel panel, String toolTipText, String altText, boolean isSelected, boolean enabled, ActionListener actionListener) {
        return (JButton) createButton(new JButton(), animIcon, panel, toolTipText, altText, isSelected, enabled, actionListener);
    }

    public static AbstractButton createButton(AbstractButton button, Icon icon, JPanel panel, String toolTipText, String altText, boolean isSelected, boolean enabled, ActionListener actionListener) {
        if (button != null) {
            if (actionListener != null) button.addActionListener(actionListener);
            if (toolTipText != null) button.setToolTipText(getLocaleString(toolTipText));
            if (altText != null) button.getAccessibleContext().setAccessibleDescription(getLocaleString(altText));
            button.setFocusable(false);
            button.setSelected(isSelected);
            button.setEnabled(enabled);
            if (icon instanceof ScaleAnimIcon) {
                //((ScaleAnimIcon)icon).setSelected(isSelected);
                button.addActionListener(e -> {
                    if (bDebugLogGUIInfo) LOG.info("AnimButton action performed: {}", button.getActionCommand());
                    //((ScaleAnimIcon)icon).setSelected(button.isSelected());
                    ((ScaleAnimIcon) icon).startAnimation(button);
                });
            }
            button.setIcon(icon);
            panel.add(button);
            return button;
        } else {
            return null;
        }
    }

    public static ScaleAnimIcon createScaleAnimIcon(String iconName, boolean isSelected, int width, int height) {
        for (EditorIcon icon : iconList) {
            if (icon.iconName.equals(iconName)) {
                icon.increaseUseCount();
                return new ScaleAnimIcon(icon.getIcon(), isSelected, width, height);
            }
        }
        return null;
    }

    public static ScaleAnimIcon createScaleAnimIcon(String iconName, boolean isSelected) {
        for (EditorIcon icon : iconList) {
            if (icon.iconName.equals(iconName)) {
                icon.increaseUseCount();
                return new ScaleAnimIcon(icon.getIcon(), isSelected, icon.getWidth(), icon.getHeight());
            }
        }
        return null;
    }

    public static ScaleAnimIcon createScaleAnimIcon(FlatSVGIcon icon, boolean isSelected) {
        return new ScaleAnimIcon(icon, isSelected, icon.getIconWidth(), icon.getIconHeight());
    }

    public static ScaleAnimIcon createScaleAnimIcon(FlatSVGIcon icon, boolean isSelected, int width, int height) {
        return new ScaleAnimIcon(icon, isSelected, width, height);
    }

    public static ScaleAnimIcon createToggleScalingAnimatedIcon(String iconName, String selectedIconName, boolean isSelected, int width, int height, float startScale, float endScale, int timeMilliseconds) {
        FlatSVGIcon defaultIcon = null;
        FlatSVGIcon selectedIcon = null;

        for (EditorIcon icon : iconList) {
            if (icon.iconName.equals(iconName)) {
                defaultIcon = icon.getIcon();
            }
            if (icon.iconName.equals(selectedIconName)) {
                selectedIcon = icon.getIcon();
            }
        }
        if (defaultIcon != null && selectedIcon != null) {
            return new ScaleAnimIcon(defaultIcon, selectedIcon, isSelected, width, height);
        }
        return null;
    }


    public static ImageIcon getUpdateIcon() { return updateIcon; }
    public static ImageIcon getMarkerIcon() { return markerIcon; }
    public static ImageIcon getGameIcon() { return gameIcon; }
    public static ImageIcon getRouteIcon() { return routeIcon; }


    public static BufferedImage getTractorImage() { return tractorImage; }





    @SuppressWarnings("unused")
    private static class EditorIcon {
        // Icon name
        private final String iconName;

        // Default Icon
        private final FlatSVGIcon defaultIcon;
        private final String defaultIconPath;
        private final int defaultIconWidth;
        private final int defaultIconHeight;
        private final FlatSVGIcon.ColorFilter defaultColorFilter;

        // User Icon
        private FlatSVGIcon userIcon;
        private String userIconPath;
        private int userIconWidth;
        private int userIconHeight;
        private FlatSVGIcon.ColorFilter userColorFilter;
        //Debug only
        private int useCount = 0;

        public EditorIcon(String iconName, String defaultIconPath, int iconWidth, int iconHeight) {
            this(iconName, defaultIconPath, iconWidth, iconHeight, null);
        }

        public EditorIcon(String iconName, String defaultIconPath, int iconWidth, int iconHeight, FlatSVGIcon.ColorFilter colorFilter) {
            this.iconName = iconName;
            // Create the default icon
            this.defaultIcon = new FlatSVGIcon(defaultIconPath, iconWidth, iconHeight);
            if (!this.defaultIcon.hasFound()) {
                LOG.error("    IconManager: Icon not found: {} - '{}'", iconName, defaultIconPath);
            }
            this.defaultIconPath = defaultIconPath;
            this.defaultIconWidth = iconWidth;
            this.defaultIconHeight = iconHeight;
            this.defaultColorFilter = colorFilter;
            // Initialize the user icon
            this.userIcon = null;
            this.userIconPath = null;
            this.userIconWidth = 0;
            this.userIconHeight = 0;
            this.userColorFilter = null;
        }

        public FlatSVGIcon getIcon() { return (userIcon != null) ? userIcon : defaultIcon;}

        public String getIconPath() { return (userIcon != null) ? this.userIconPath : this.defaultIconPath;}

        //
        // getters
        //

        public int getWidth() {
            return (userIcon != null) ? userIconWidth : defaultIconWidth;
        }

        public int getHeight() {
            return (userIcon != null) ? userIconHeight : defaultIconHeight;
        }

        public String getDefaultIconPath() {
            return defaultIconPath;
        }


        public int getDefaultIconWidth() {
            return defaultIconWidth;
        }

        public int getDefaultIconHeight() {
            return defaultIconHeight;
        }

        public FlatSVGIcon.ColorFilter getDefaultColorFilter() {
            return defaultColorFilter;
        }

        public FlatSVGIcon.ColorFilter getUserColorFilter() {
            return userColorFilter;
        }

        public int getUserIconHeight() {
            return userIconHeight;
        }

        public int getUserIconWidth() {
            return userIconWidth;
        }

        public String getUserIconPath() {
            return userIconPath;
        }

        public FlatSVGIcon getUserIcon() {
            return userIcon;
        }

        //
        // setters
        //

        public void setUserIcon(String userIconPath, int iconWidth, int iconHeight, FlatSVGIcon.ColorFilter colorFilter) {
            // Create the user icon
            this.userIcon = new FlatSVGIcon(userIconPath, iconWidth, iconHeight);
            this.userIconPath = userIconPath;
            this.userIconWidth = iconWidth;
            this.userIconHeight = iconHeight;
            this.userColorFilter = colorFilter;
        }

        public void setUserIcon(FlatSVGIcon userIcon) {
            this.userIcon = userIcon;
        }

        public void setUserIconPath(String userIconPath) {
            this.userIconPath = userIconPath;
        }

        public void setUserIconWidth(int userIconWidth) {
            this.userIconWidth = userIconWidth;
        }

        public void setUserIconHeight(int userIconHeight) {
            this.userIconHeight = userIconHeight;
        }

        public void setUserColorFilter(FlatSVGIcon.ColorFilter userColorFilter) {
            this.userColorFilter = userColorFilter;
        }

        public void increaseUseCount() {
            useCount++;
        }
    }
}
