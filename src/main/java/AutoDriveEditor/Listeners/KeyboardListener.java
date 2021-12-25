package AutoDriveEditor.Listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static AutoDriveEditor.Utils.LoggerUtils.LOG;

public class KeyboardListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {
        //LOG.info("Key Typed : {}", e.getKeyChar());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        //LOG.info("Key Pressed : {}", e.getKeyChar());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //LOG.info("Key Released : {}", e.getKeyChar());

    }
}
