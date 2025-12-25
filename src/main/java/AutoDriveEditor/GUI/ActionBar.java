package AutoDriveEditor.GUI;

import AutoDriveEditor.GUI.Buttons.ActionBar.ChangePanel.*;
import AutoDriveEditor.GUI.Buttons.ActionBar.ConfigPanel.*;
import AutoDriveEditor.Managers.IconManager;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.*;
import static AutoDriveEditor.GUI.TextPanel.textPanel;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.Managers.IconManager.MENU_ICON;
import static AutoDriveEditor.XMLConfig.EditorXML.*;

public class ActionBar extends JPanel {

    private final JPanel historyPanel;
    private final JPanel clipPanel;

    public ActionBar() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder());

        // createSetting the action panel to hold the menu popup, undo/redo, copy/paste buttons
        JPanel actionPanel = new JPanel(new MigLayout("gap 0, hidemode 3"));
        actionPanel.setBorder(BorderFactory.createEmptyBorder());
        add(actionPanel, BorderLayout.WEST);

        //
        // Popup menu/button
        //

        // createSetting the popup menu
        JPopupMenu popupMenu = new JPopupMenu();

        // add the toggleable text JPanel menu item
        JCheckBoxMenuItem showTextPanel = new JCheckBoxMenuItem(getLocaleString("actionbar_popup_infopanel"));
        showTextPanel.addActionListener(e -> {
            boolean visible = showTextPanel.isSelected();
            textPanel.setVisible(visible);
            editorSplitPane.resetToPreferredSizes();
            bTextPanelVisible = visible;
            getMapPanel().repaint();
        });
        showTextPanel.setSelected(bTextPanelVisible);

        // add the toggleable undo/redo JPanel menu item
        JCheckBoxMenuItem showUndoRedoMenu = new JCheckBoxMenuItem(getLocaleString("actionbar_popup_history"), bUndoRedoVisible);
        showUndoRedoMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isVisible = showUndoRedoMenu.isSelected();
                historyPanel.setVisible(showUndoRedoMenu.isSelected());
                bUndoRedoVisible = isVisible;
                revalidate();
                repaint();
            }});

        // add the toggleable copy/paste JPanel menu item
        JCheckBoxMenuItem showCopyPasteMenu = new JCheckBoxMenuItem(getLocaleString("actionbar_popup_copypaste"), bCopyPasteVisible);
        showCopyPasteMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isVisible = showCopyPasteMenu.isSelected();
                clipPanel.setVisible(showCopyPasteMenu.isSelected());
                bCopyPasteVisible = isVisible;
                revalidate();
                repaint();
            }});

        // add the menu items to the popup menu
        popupMenu.add(showUndoRedoMenu);
        popupMenu.add(showCopyPasteMenu);
        popupMenu.add(showTextPanel);

        // createSetting the popup menu button
        JButton popupMenuButton = getPopupMenuButton(popupMenu);
        actionPanel.add(popupMenuButton);

        // add a vertical separator
        actionPanel.add(new JLabel(IconManager.getSVGIcon(IconManager.VERTICAL_SEPERATOR_ICON)), "gapleft 5");

        //
        // Undo/Redo Panel
        //

        // createSetting the undo/redo JPanel
        historyPanel = new JPanel(new MigLayout("insets 0 10 0 0"));
        historyPanel.setBorder(BorderFactory.createEmptyBorder());
        // add the undo/redo buttons
        buttonManager.addButton(new UndoButton(historyPanel));
        buttonManager.addButton(new RedoButton(historyPanel));
        historyPanel.add(new JLabel(IconManager.getSVGIcon(IconManager.VERTICAL_SEPERATOR_ICON)), "gap 5");

        actionPanel.add(historyPanel);
        if (!bUndoRedoVisible) historyPanel.setVisible(false);

        //
        // Cut/Copy/Paste Panel
        //

        // createSetting the cut/copy/paste JPanel
        clipPanel = new JPanel(new MigLayout("insets 0 10 0 "));
        clipPanel.setBorder(BorderFactory.createEmptyBorder());
        // add the cut/copy/paste buttons
        buttonManager.addButton(new MultiSelectButton(clipPanel));
        buttonManager.addButton(new CutButton(clipPanel));
        buttonManager.addButton(new CopyButton(clipPanel));
        buttonManager.addButton(new PasteButton(clipPanel));
        clipPanel.add(new JLabel(IconManager.getSVGIcon(IconManager.VERTICAL_SEPERATOR_ICON)), "gap 5");

        actionPanel.add(clipPanel);
        if (!bCopyPasteVisible) clipPanel.setVisible(false);

        //
        // Config Panel
        //

        // createSetting the config JPanel
        JPanel configPanel = new JPanel(new MigLayout());
        configPanel.setBorder(BorderFactory.createEmptyBorder());

        // add buttons to the config panel
        buttonManager.addButton(new MapInfoButton(configPanel));
        configPanel.add(new JLabel(IconManager.getSVGIcon(IconManager.VERTICAL_SEPERATOR_ICON)));
        buttonManager.addButton(new AutosaveButton(configPanel));
        configPanel.add(new JLabel(IconManager.getSVGIcon(IconManager.VERTICAL_SEPERATOR_ICON)));
        buttonManager.addButton(new SelectHiddenButton(configPanel));
        buttonManager.addButton(new GridDisplayButton(configPanel));
        buttonManager.addButton(new GridSnapButton(configPanel));
        buttonManager.addButton(new NodeSizeUpButton(configPanel));
        buttonManager.addButton(new NodeSizeDownButton(configPanel));
        buttonManager.addButton(new RotationSnapButton(configPanel));
        buttonManager.addButton(new ContinuousConnectButton(configPanel));
        configPanel.add(new JLabel(IconManager.getSVGIcon(IconManager.VERTICAL_SEPERATOR_ICON)));
        buttonManager.addButton(new ConfigButton(configPanel));

        add(configPanel, BorderLayout.EAST);
    }

    private static JButton getPopupMenuButton(JPopupMenu popupMenu) {
        FlatSVGIcon menuIcon = IconManager.getSVGIcon(MENU_ICON);
        JButton menuButton = new JButton(menuIcon);
        menuButton.setPreferredSize(new Dimension(20, 20));
        menuButton.setToolTipText(getLocaleString("actionbar_popup_tooltip"));
        menuButton.setSelected(false);
        menuButton.setBorderPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setFocusable(false);

        // Add a listener to the button for any left click action
        menuButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    popupMenu.show(menuButton, e.getX(), e.getY());
                }
            }
        });

        return menuButton;
    }
}
