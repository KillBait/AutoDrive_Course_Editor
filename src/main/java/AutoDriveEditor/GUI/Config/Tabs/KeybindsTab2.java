package AutoDriveEditor.GUI.Config.Tabs;

import AutoDriveEditor.Utils.Classes.ButtonColumn;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static AutoDriveEditor.AutoDriveEditor.bIsDebugEnabled;
import static AutoDriveEditor.GUI.Config.ConfigGUI.getConfigGUI;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class KeybindsTab2 extends JPanel implements ActionListener {

    public KeybindsTab2() {

        JButton renderButton;

       //setLayout(new GridLayout(3,2,0,5));

        //JPanel panel = new JPanel(new BorderLayout());
        setLayout(new BorderLayout());
        /*DefaultTableModel dm = new DefaultTableModel();
        dm.setDataVector(new Object[][] { { "button 1", "foo" },
                { "button 2", "bar" },
                { "button 3", "bar" },
                { "button 4", "bar" },
                { "button 5", "bar" },
                { "button 6", "bar" },
                { "button 7", "bar" },
                { "button 8", "bar" },
                { "button 9", "bar" },
                { "button 10", "bar" },
                { "button 11", "bar" },
                { "button 12", "bar" },
                { "button 13", "bar" },
                { "button 14", "bar" },
                { "button 15", "bar" },
                { "button 16", "bar" },
                { "button 17", "bar" },
                { "button 18", "bar" },
                { "button 19", "bar" },
                { "button 20", "bar" },
                { "button 21", "bar" }}, new Object[] { "Button", "String" });*/

        DefaultCellEditor cellEditor = new DefaultCellEditor(new JTextField()) {
            @Override
            public boolean stopCellEditing() {
                boolean edited = super.stopCellEditing();
                if (edited) {
                    //int row = getTable().getSelectedRow();
                    Object value = getCellEditorValue();
                    JOptionPane.showMessageDialog(getConfigGUI(), "Cell edited in row : " + value);
                }
                return edited;
            }
        };

        JTable table = new JTable(new CustomTableModel());
        new ButtonColumn(table, null, 2);
        /*table.getTableHeader().setReorderingAllowed(false);
        table.getColumn("Reset").setCellRenderer(new JTableButtonRenderer());
        table.addMouseListener(new JTableButtonMouseListener(table));*/

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);


    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }


    static class CustomTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Action", "Key", ""};
        private final Class<?>[] columnTypes = new Class<?>[] {String.class, String.class, JButton.class};
        private final Object[][] data = { { "Key 1", "A", "" },
                { "Key 2", "B", ""  },
                { "Key 3", "C", ""  },
                { "Key 4", "D", ""  },
                { "Key 5", "E", ""  },
                { "Key 6", "F", ""  },
                { "Key 7", "G", ""  },
                { "Key 8", "H", ""  },
                { "Key 9", "I", ""  },
                { "Key 10", "J", ""  },
                { "Key 11", "K", ""  },
                { "Key 12", "L", ""  },
                { "Key 13", "M", ""  },
                { "Key 14", "N", ""  },
                { "Key 15", "O", ""  },
                { "Key 16", "P", ""  },
                { "Key 17", "Q", ""  },
                { "Key 18", "R", ""  },
                { "Key 19", "S", ""  },
                { "Key 20", "T", ""  },
                { "Key 21", "U", ""  }};

        public int getRowCount() {
            return data.length;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnTypes[columnIndex];
        }

        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return col == 1;
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            //Adding components
            switch (columnIndex) {
                case 0:
                    return data[rowIndex][0];
                case 1:
                    return data[rowIndex][columnIndex];
                //Adding button and creating click listener
                /*case 2:
                    JButton button = makeButton(null, "tooltip", "Reset", null, null, false, null, true);
                //case 2: final JButton button = new JButton("Reset");
                    button.addActionListener(arg0 -> JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(getConfigGUI()),
                            "Button clicked for row "+rowIndex));
                    return button;*/
                default:
                    return "Error";
            }
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            if (bIsDebugEnabled) LOG.info("Setting value at row {}, column {} to {} (is instance of {})", row, col, value, value.getClass());

            data[row][col] = value;
            fireTableCellUpdated(row, col);

            if (bIsDebugEnabled) {
                LOG.info("New value of data:");
                printDebugData();
            }
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }

    class JTableButtonRenderer implements TableCellRenderer {
        public JTableButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return (JButton)value;
        }
    }

    private static class JTableButtonMouseListener extends MouseAdapter {
        private final JTable table;

        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }

        public void mouseClicked(MouseEvent e) {

            // get the column of the button
            int column = table.getColumnModel().getColumnIndexAtX(e.getX());
            //get the row of the button
            int row    = e.getY()/table.getRowHeight();

            // Check if the row/column is valid
            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                Object value = table.getValueAt(row, column);
                if (value instanceof JButton) {
                    // generate a click event on the button
                    ((JButton)value).doClick();
                }
            }
        }
    }
}
