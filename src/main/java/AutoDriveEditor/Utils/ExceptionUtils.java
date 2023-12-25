package AutoDriveEditor.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import static AutoDriveEditor.AutoDriveEditor.editor;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;

public class ExceptionUtils {

    public static String getStackTraceAsString(Throwable e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        StringBuilder stringBuilder = new StringBuilder();
        for (StackTraceElement element : stackTraceElements) {
            stringBuilder.append(element.toString()).append("\n");
        }
        return stringBuilder.toString();
    }

    private static void copyTextToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, null);
    }

    public static void createExceptionDialog(Throwable throwable, String title, String exception, String functionName, String errorDetailText, String footerText) {

        String exceptionText = "<html><b>## " + exception + " ##</b><br><br>" +
                "<i>Function Name</i>:- " + functionName + "<br>" +
                "<i>Error:-</i> " + getLocaleString("dialog_id_mismatch_error") + "<br>" +
                "<i>Details:-</i> " + errorDetailText + "<br><br>";

        String traceText = getStackTraceAsString(throwable);

        JPanel textAreaPanel = new JPanel();
        textAreaPanel.setLayout(new BoxLayout(textAreaPanel, BoxLayout.Y_AXIS));

        JTextArea jTextArea = new JTextArea();
        jTextArea.setEditable(false);
        jTextArea.setText(traceText);
        jTextArea.setCaretPosition(0);
        Font currentFont = jTextArea.getFont();
        Font smallerFont = currentFont.deriveFont(currentFont.getSize() - 2.0f);
        jTextArea.setFont(smallerFont);
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        textAreaPanel.add(jScrollPane);
        textAreaPanel.setMaximumSize(new Dimension(500,200));
        textAreaPanel.setPreferredSize(new Dimension(500,200));
        textAreaPanel.setMinimumSize(new Dimension(500,200));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton copyButton = new JButton("Copy Crash Trace To Clipboard");
        buttonPanel.add(copyButton, BorderLayout.CENTER);

        Object[] fields = {exceptionText, textAreaPanel, buttonPanel, footerText};

        copyButton.addActionListener(e -> copyTextToClipboard(traceText));

        throwable.printStackTrace();
        JOptionPane.showMessageDialog(editor, fields, title, JOptionPane.ERROR_MESSAGE);
    }

    public static class MismatchedIdException extends Throwable {
        private final int value1;
        private final int value2;
        private final int value3;

        public MismatchedIdException(String message, int value1, int value2, int value3) {
            super(message);
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
        }

        public void showExceptionDialog(Throwable exception, String title, String functionName, String errorDetailText, String footerText) {
            createExceptionDialog(exception, title, exception.getMessage(), functionName, errorDetailText, footerText);
        }

        public int getValue1() {
            return value1;
        }

        public int getValue2() {
            return value2;
        }

        public int getValue3() {
            return value3;
        }
    }

    public static class SequenceException extends Throwable {
        public SequenceException() {}
        public SequenceException(String message) {
            super(message);
        }
    }
}
