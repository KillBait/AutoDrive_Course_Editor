package AutoDriveEditor.GUI.Config.Tabs;

import AutoDriveEditor.Classes.UI_Components.HeaderList.HeaderListEntry;
import AutoDriveEditor.Classes.UI_Components.HeaderList.HeaderListPanel;
import AutoDriveEditor.Managers.ThemeManager;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.util.Comparator;
import java.util.Objects;

import static AutoDriveEditor.Managers.IconManager.FLATLAF_ICON;
import static AutoDriveEditor.Managers.IconManager.getSVGIcon;
import static AutoDriveEditor.Managers.ThemeManager.*;

public class ThemeTab extends JPanel {


    private final JLabel nameLabel;
    private final JLabel darkLabel;
    private final JLabel authorLabel;
    private JPanel southPanel = new JPanel();

    public ThemeTab() {

        // Set the layout
        setLayout(new MigLayout("", "[grow]", "[grow]"));

        // Theme Info Panel
        southPanel = new JPanel(new MigLayout("wrap 2"));
        southPanel.putClientProperty("FlatLaf.style", "border: 0,0,0,0,@disabledForeground,1,16; background: darken($Panel.background,5%)");
        southPanel.add(new JLabel("Name:"));
        southPanel.add(nameLabel = new JLabel());
        southPanel.add(new JLabel("Type:"));
        southPanel.add(darkLabel = new JLabel());
        southPanel.add(new JLabel("Author:"));
        southPanel.add(authorLabel = new JLabel());

        add(southPanel, "south, gap 10 10 10 10");

        // Create the header list panel
        HeaderListPanel myHeaderListPanel = new HeaderListPanel();

        // Add FlatLaf themes to the list
        myHeaderListPanel.addSeparator("FlatLaf", null, true, true, Color.LIGHT_GRAY);
        addFlatLafThemes(myHeaderListPanel);

        // createSetting a comparator to sort the themes by name
        Comparator<? super ThemeInfo> comparator = (t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName());

        // Add External Themes if any detected by the ThemeManager
        externalThemeInfoList.sort(comparator);
        if (externalThemeInfoList != null && !externalThemeInfoList.isEmpty()) {
            myHeaderListPanel.addSeparator("User", null,true, true, Color.LIGHT_GRAY);
            for (ThemeManager.ThemeInfo themeInfo : externalThemeInfoList) {
                myHeaderListPanel.addEntry(themeInfo.getName(), themeInfo.getPath());
            }
        }

        // Sort the bundled themes by name
        bundledThemeInfoList.sort(comparator);

        // add IntelliJ Themes
        myHeaderListPanel.addSeparator("IntelliJ", null,true, true, Color.LIGHT_GRAY);
        for (ThemeManager.ThemeInfo themeInfo : bundledThemeInfoList) {
            if (!themeInfo.isMaterialTheme()) myHeaderListPanel.addEntry(themeInfo.getName(), themeInfo.getPath());
        }

        // add Material Themes
        myHeaderListPanel.addSeparator("Material", null,true, true, Color.LIGHT_GRAY);
        for (ThemeManager.ThemeInfo themeInfo : bundledThemeInfoList) {
            if (themeInfo.isMaterialTheme()) myHeaderListPanel.addEntry(themeInfo.getName(), themeInfo.getPath());
        }

        // Add a listener to updateVisibility the theme info panel when a theme is selected
        myHeaderListPanel.getEntryList().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                HeaderListEntry selectedEntry = myHeaderListPanel.getEntryList().getSelectedValue();
                if (selectedEntry ==null || selectedEntry.isHeader() || selectedEntry.isSeparator() || selectedEntry.isSpacer()) return;
                ThemeManager.saveCurrentTheme(selectedEntry.getValue());
                updateThemeInfo(selectedEntry.getDisplayString());
                updateTheme(selectedEntry);
            }
        });

        // Set the selected theme and ensure it is visible
        String currentTheme = ThemeManager.getCurrentTheme();
        if (currentTheme != null) {
            int listSize = myHeaderListPanel.getEntryList().getModel().getSize();
            for (int i = 0; i < listSize; i++) {
                HeaderListEntry theme = myHeaderListPanel.getEntryList().getModel().getElementAt(i);
                if (Objects.equals(theme.getValue(), currentTheme)) {
                    myHeaderListPanel.getEntryList().setSelectedIndex(i);
                    int visibleRowCount = myHeaderListPanel.getEntryList().getVisibleRowCount();
                    int visibleIndex = Math.max(0, i - visibleRowCount / 2);
                    visibleIndex = Math.min(visibleIndex, listSize - visibleRowCount);
                    myHeaderListPanel.getEntryList().ensureIndexIsVisible(visibleIndex);
                    break;
                }
            }
        }

        // Add the header list panel to the west portion of the layout
        add(myHeaderListPanel, "growy, pushy, gap 10 10 10 0");

        // Add the FlatLaf logo to the east portion of the layout
        JPanel eastPanel = new JPanel(new MigLayout());
        //FlatSVGIcon flatLafIcon = getSVGIcon(FLATLAF_ICON, 100,100);
        JLabel flatLafLabel = new JLabel(getSVGIcon(FLATLAF_ICON, 100,100));
        eastPanel.add(flatLafLabel, "center, wrap, gaptop 20");
        //eastPanel.add(new JLabel("Theme support added using FlatLaf"), "center");
        String mainText = "<html><center>Theme support by FlatLaf.<br><br>" +
                "For more information about theme<br>" +
                " creations and modification, see<br>" +
                "<a href='https://www.formdev.com/flatlaf/'>FormDev Software</a>";
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(mainText);
        editorPane.setEditable(false);
        editorPane.setOpaque(false);
        editorPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(e.getURL().toURI());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
//        String linkText = "<br><br>Visit AutoDrive Editor HomePage</b>";
//        JEditorPane editorLink = createHyperLink(mainText,linkText, "https://github.com/KillBait/AutoDrive_Course_Editor");
        eastPanel.add(editorPane, "center");
        add(eastPanel, "east, center, push, gap 0 10 0 0");

    }

    private void updateThemeInfo(String themeName) {
        if (bundledThemeInfoList != null) {
            boolean found = false;
            for (ThemeManager.ThemeInfo themeInfo : allThemeList) {
                if (themeInfo.getName().equals(themeName)) {
                    nameLabel.setText(themeInfo.getName());
                    darkLabel.setText((themeInfo.isDark()) ? "Dark" : "Light");
                    authorLabel.setText(themeInfo.getAuthor());
                    found = true;
                    break;
                }
            }
            if (!found) {
                nameLabel.setText("N/A");
                darkLabel.setText("N/A");
                authorLabel.setText("N/A");
            }
        }
        southPanel.revalidate();
        southPanel.repaint();
    }

    // Update to the selected theme
    private void updateTheme(HeaderListEntry themeClassName) {
        EventQueue.invokeLater(() -> {
            try {
                FlatAnimatedLafChange.showSnapshot();
                ThemeManager.applyTheme(themeClassName.getValue());
                if (!(UIManager.getLookAndFeel() instanceof FlatLaf)) UIManager.put("defaultFont", null);
                FlatLaf.updateUI();
                FlatAnimatedLafChange.hideSnapshotWithAnimation();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    // Add FlatLaf themes to the list
    private void addFlatLafThemes(HeaderListPanel panel) {
        panel.addEntry("Flat Light", "com.formdev.flatlaf.FlatLightLaf");
        panel.addEntry("Flat Dark", "com.formdev.flatlaf.FlatDarkLaf");
        panel.addEntry("Flat IntelliJ", "com.formdev.flatlaf.FlatIntelliJLaf");
        panel.addEntry("Flat Darcula", "com.formdev.flatlaf.FlatDarculaLaf");
        panel.addEntry("Flat macOS Light", "com.formdev.flatlaf.themes.FlatMacLightLaf");
        panel.addEntry("Flat macOS Dark", "com.formdev.flatlaf.themes.FlatMacDarkLaf");
    }
}
