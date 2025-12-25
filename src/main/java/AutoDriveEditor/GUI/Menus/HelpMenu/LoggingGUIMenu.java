package AutoDriveEditor.GUI.Menus.HelpMenu;

import AutoDriveEditor.AutoDriveEditor;
import AutoDriveEditor.GUI.Menus.JCheckBoxMenuItemBase;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

import static AutoDriveEditor.Classes.Util_Classes.FileUtils.getCurrentLocation;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.Managers.IconManager.getTractorImage;

public class LoggingGUIMenu extends JCheckBoxMenuItemBase {

    JCheckBoxMenuItem debugCheckbox;

    private JFrame loggingGUI;

    private long lastFileSize = 0;
    private volatile boolean running = true;
    private Thread monitorThread;
    private File debugFile;
    private JTextArea textArea;
    private JProgressBar progressBar;

    public LoggingGUIMenu() {
        debugCheckbox = makeCheckBoxMenuItem("menu_help_show_editor_log_gui", false, true);
    }

    private void showLoggingGUI() {

        loggingGUI = new JFrame();
        loggingGUI.dispatchEvent(new WindowEvent(loggingGUI, WindowEvent.WINDOW_CLOSING));
        loggingGUI.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                stopFileMonitor();
                loggingGUI.dispose();
            }
        });

        //JPanel loggingPanel = new JPanel(new BorderLayout(5, 5));

        debugFile = new File(getCurrentLocation() + "AutoDriveEditor.log");

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane sc = new JScrollPane(textArea);

        loggingGUI.add(sc);

        loggingGUI.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        loggingGUI.setTitle("Editor Log");
        loggingGUI.setIconImage(getTractorImage());
        loggingGUI.setResizable(true);
        loggingGUI.pack();
        loggingGUI.setSize(new Dimension(1024, 600));
        loggingGUI.setLocationRelativeTo(AutoDriveEditor.editor);
        loggingGUI.setVisible(true);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        AbstractButton selectionPanelButton = (AbstractButton) e.getItem();
        if (selectionPanelButton.isSelected()) {
            showLoggingGUI();
            String text = loadInitialContent();
            textArea.setText(text);
            textArea.setCaretPosition(text.length());
            startFileMonitor();
        } else {
            loggingGUI.dispose();
            stopFileMonitor();

        }
    }

    private String loadInitialContent() {
        progressBar = new JProgressBar();
        progressBar.setMinimum(0);
        progressBar.setMaximum((int) debugFile.length());
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        // Add the progress bar to the logging GUI
        loggingGUI.add(progressBar, BorderLayout.SOUTH);
        loggingGUI.revalidate();
        loggingGUI.repaint();

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(debugFile))) {
            String line;
            long bytesRead = 0;
            long totalBytes = debugFile.length();
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                bytesRead += line.length() + 1; // +1 for the newline character
                int progress = (int) ((bytesRead * 100) / totalBytes);
                progressBar.setValue(progress);
            }
            lastFileSize = debugFile.length();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Remove the progress bar after loading is complete
        loggingGUI.remove(progressBar);
        loggingGUI.revalidate();
        loggingGUI.repaint();
        return content.toString();
    }

    private void startFileMonitor() {
        if (monitorThread == null || !monitorThread.isAlive()) {
            running = true;
            monitorThread = new Thread(() -> {
                try {
                    LOG.info("Starting Log Monitoring Thread");
                    while (running) {
                        long fileSize = debugFile.length();
                        if (fileSize > lastFileSize) {
                            readNewLines();
                            lastFileSize = fileSize;
                        }
                        Thread.sleep(1000); // Adjust the sleep time as necessary
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            });
            monitorThread.start();
        }
    }

    private void stopFileMonitor() {
        running = false;
        if (monitorThread != null) {
            try {
                LOG.info("Stopping Log Monitoring Thread");
                monitorThread.join();
                LOG.info("Monitoring thread closed");
                debugCheckbox.setState(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void readNewLines() throws IOException {
        RandomAccessFile raf = new RandomAccessFile(debugFile, "r");
        raf.seek(lastFileSize);
        String line;
        StringBuilder content = new StringBuilder();
        while ((line = raf.readLine()) != null) {
            content.append(line).append("\n");
        }
        SwingUtilities.invokeLater(() -> textArea.append(content.toString()));
        raf.close();
    }
}
