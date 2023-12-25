package AutoDriveEditor.GUI.Config.Tabs;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class KeybindsTab extends JPanel implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {

    }

    static class Shortcut {
        private final String name;
        private final KeyStroke defaultValue;
        private KeyStroke currentValue;
        private boolean shiftQualifier;
        private boolean ctrlQualifier;
        private boolean altQualifier;

        public Shortcut(String name, KeyStroke defaultValue) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.currentValue = defaultValue;
        }

        public KeyStroke getDefaultValue() {
            return defaultValue;
        }

        public KeyStroke getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(KeyStroke currentValue) {
            this.currentValue = currentValue;
        }

        @Override
        public String toString() {
            return name + " : " + currentValue.toString();
        }
    }

    static class ShortcutTableModel extends DefaultTableModel {
        public ShortcutTableModel() {
            addColumn("Action");
            addColumn("Key");
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            Class cl = String.class;
            switch (columnIndex) {

            }
            return cl;
        }

        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return false;
        }
    }


    public KeybindsTab() {

        JTable actionTable;
        JTable KeyTable;

        DefaultListModel<Shortcut> shortcutListModel = new DefaultListModel<>();

        shortcutListModel.addElement(new Shortcut("Save", KeyStroke.getKeyStroke("ctrl alt S")));
        shortcutListModel.addElement(new Shortcut("Cut", KeyStroke.getKeyStroke("ctrl X")));
        shortcutListModel.addElement(new Shortcut("Copy", KeyStroke.getKeyStroke("ctrl C")));

        actionTable = new JTable(new ShortcutTableModel());
        actionTable.getTableHeader().setReorderingAllowed(false);
        actionTable.getTableHeader().setResizingAllowed(false);

        ShortcutTableModel tm = (ShortcutTableModel) actionTable.getModel();

        for (int i=0; i<40;i++) {
            tm.addRow(new Object[]{"test "+i,"Ctrl Alt S"});
        }



        JScrollPane sp = new JScrollPane(actionTable);
        sp.setPreferredSize(new Dimension(200,175));

        add(sp);



        // Create a list model for shortcuts (you can use DefaultListModel)
        /*DefaultListModel<Shortcut> shortcutListModel = new DefaultListModel<>();

        // Create JList with the list model
        JList<Shortcut> shortcutList = new JList<>(shortcutListModel);
        JScrollPane scrollPane = new JScrollPane(shortcutList);

        // Add shortcuts to the list model
        shortcutListModel.addElement(new Shortcut("Save", KeyStroke.getKeyStroke("ctrl alt S")));
        shortcutListModel.addElement(new Shortcut("Cut", KeyStroke.getKeyStroke("ctrl X")));
        shortcutListModel.addElement(new Shortcut("Copy", KeyStroke.getKeyStroke("ctrl C")));
        // Add more shortcuts as needed...

        // Create an editable text box for editing shortcuts
        JTextField editTextField = new JTextField();
        editTextField.setPreferredSize(new Dimension(200,40));
        editTextField.setEditable(false); // Initially, make it non-editable
        AbstractDocument doc = (AbstractDocument) editTextField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (length == 1) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });

        // Add a ListSelectionListener to the JList to update the text box
        shortcutList.addListSelectionListener(e -> {
            Shortcut selectedShortcut = shortcutList.getSelectedValue();
            if (selectedShortcut != null) {
                editTextField.setText(selectedShortcut.getCurrentValue().toString());
                editTextField.setEditable(true);
            } else {
                editTextField.setText("");
                editTextField.setEditable(false);
            }
        });

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        JButton resetButton = new JButton("Reset to Default");
        buttonPanel.add(resetButton);

        // Add an ActionListener to the Reset button
        resetButton.addActionListener(e -> {
            Shortcut selectedShortcut = shortcutList.getSelectedValue();
            if (selectedShortcut != null) {
                selectedShortcut.setCurrentValue(selectedShortcut.getDefaultValue());
                editTextField.setText(selectedShortcut.getCurrentValue().toString());
            }
        });

        // Create the main content pane
        JPanel contentPane = new JPanel(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(editTextField, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add a key listener to check for Shift or Ctrl in the shortcut
        editTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String shortcutText = editTextField.getText();
                boolean hasCtrl = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
                boolean hasShift = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;

                LOG.info("Text = '{}' , shift {}, ctrl = {}", shortcutText,hasShift, hasCtrl);
                if (hasCtrl && !shortcutText.contains("Ctrl + ")) {
                    shortcutText = "Ctrl + " + shortcutText;
                }
                if (hasShift && !shortcutText.contains("Shift + ")) {
                    shortcutText = "Shift + " + shortcutText;
                }

                // Display the modified shortcut text
                editTextField.setText(shortcutText);
            }
        });*/
    }
}
