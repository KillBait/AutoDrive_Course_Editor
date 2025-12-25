package AutoDriveEditor.Classes.UI_Components.HeaderList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A panel that contains a list of header entries, separators, and empty spaces.
 * This panel allows adding different types of entries with customizable styles.
 * Created 14/09/2024 by KillBait!
 */
@SuppressWarnings("unused")
public class HeaderListPanel extends JPanel {
    private final JList<HeaderListEntry> entryList;
    private final DefaultListModel<HeaderListEntry> listModel;

    /**
     * Constructs a new HeaderListPanel.
     * Initializes the list and its model, and sets up the layout and scroll pane.
     */

    public HeaderListPanel() {
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        entryList = new JList<>(listModel) {
            @Override
            public void setSelectedIndex(int index) {
                int oldIndex = getSelectedIndex();
                if (index >= 0 && index < listModel.size()) {
                    HeaderListEntry entry = listModel.getElementAt(index);
                    if (!entry.isHeader() && !entry.isSeparator() && !entry.isSpacer()) {
                        super.setSelectedIndex(index);
                    }
                }
            }
        };
        entryList.setCellRenderer(new HeaderListCellRenderer());
        entryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(entryList);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // Add key listener to allow moving selection up and down with arrow keys and to
        // make sure we skip over any headers, separators, and spacers
        entryList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int selectedIndex = entryList.getSelectedIndex();
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    moveSelectionUp(selectedIndex);
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    moveSelectionDown(selectedIndex);
                }
                // consume the key press so the list doesn't do its own selection
                e.consume();
                // as we consumed the key press, the ScrollPane won't get any notification,
                // we have to manually make sure the selected item is visible
                entryList.ensureIndexIsVisible(entryList.getSelectedIndex());
            }
        });


        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Moves the selection up in the list. Skipping headers, separators, and spacers.
     *
     * @param selectedIndex the index of the currently selected item
     */
    private void moveSelectionUp(int selectedIndex) {
        for (int i = selectedIndex - 1; i >= 0; i--) {
            HeaderListEntry entry = listModel.getElementAt(i);
            if (!entry.isHeader() && !entry.isSeparator() && !entry.isSpacer()) {
                entryList.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Moves the selection down in the list. Skipping headers, separators, and spacers.
     *
     * @param selectedIndex the index of the currently selected item
     */
    private void moveSelectionDown(int selectedIndex) {
        for (int i = selectedIndex + 1; i < listModel.size(); i++) {
            HeaderListEntry entry = listModel.getElementAt(i);
            if (!entry.isHeader() && !entry.isSeparator() && !entry.isSpacer()) {
                entryList.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Adds a regular entry to the list.
     *
     * @param displayString the text to display for the entry
     * @param value the value associated with the entry
     */
    public void addEntry(String displayString, String value) {
        listModel.addElement(new HeaderListEntry(displayString, value));
    }

    /**
     * Adds an entry with an icon to the list.
     *
     * @param icon the icon to display for the entry
     * @param displayString the text to display for the entry
     * @param value the value associated with the entry
     */
    public void addIconEntry(Icon icon, String displayString, String value) {
        listModel.addElement(new HeaderListEntry(icon, displayString, value));
    }

    /**
     * Adds an entry with a color block to the list.
     *
     * @param color the color to display for the entry
     * @param displayString the text to display for the entry
     * @param value the value associated with the entry
     */
    public void addColorEntry(Color color, String displayString, String value) {
        listModel.addElement(new HeaderListEntry(color, displayString, value));
    }

    /**
     * Adds an empty space to the list.
     * The empty space is not selectable.
     */
    public void addSpacer(int height) {
        listModel.addElement(new HeaderListEntry(height));
    }

    /**
     * Adds a separator to the list.
     *
     * @param text the text to display for the separator
     * @param textColor the color of the separator text
     * @param bold whether the separator text should be bold
     * @param italic whether the separator text should be italic
     * @param separatorColor the color of the separator line
     */
    public void addSeparator(String text, Color textColor, boolean bold, boolean italic, Color separatorColor) {
        listModel.addElement(new HeaderListEntry(text, textColor, bold, italic, separatorColor));
    }

    /**
     * Adds a header to the list.
     *
     * @param text the text to display for the header
     * @param textColor the color of the header text
     * @param bold whether the header text should be bold
     * @param italic whether the header text should be italic
     */
    public void addHeader(String text, Color textColor, boolean bold, boolean italic) {
        listModel.addElement(new HeaderListEntry(text, true, bold, italic, textColor));
    }

    /**
     * Gets the value of the selected entry in the list.
     *
     * @return the value of the selected entry, or null if no entry is selected
     */
    public String getSelectedValue() {
        HeaderListEntry selectedEntry = entryList.getSelectedValue();
        return selectedEntry != null ? selectedEntry.getValue() : null;
    }

    /**
     * Gets the JList component used in this panel.
     *
     * @return the JList component
     */
    public JList<HeaderListEntry> getEntryList() {
        return entryList;
    }
}