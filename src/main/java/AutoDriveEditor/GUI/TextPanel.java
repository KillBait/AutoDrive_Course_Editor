package AutoDriveEditor.GUI;

import javax.swing.*;
import java.awt.*;

import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;

public class TextPanel extends JPanel {

    // Text Area
    public static JPanel textPanel;

    public static JTextArea textArea;

    public TextPanel() {
        textPanel = this;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        //
        // Set up the text area
        //

        textArea = new JTextArea("Welcome to the AutoDrive Editor... Load a config to start editing..\n", 3, 20);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        scrollPane.setPreferredSize(new Dimension((int) getPreferredSize().getWidth(), 60));
        add(scrollPane);
    }

    public static void showInTextArea(String text, boolean clearAll, boolean outputToLogFile) {
        if (clearAll) {
            textArea.selectAll();
            textArea.replaceSelection(null);
        }
        if (outputToLogFile) LOG.info(text);
        if (!textArea.getText().isEmpty()) {
            textArea.append("\n" + text);
        } else {
            textArea.append(text);
        }
    }
}
