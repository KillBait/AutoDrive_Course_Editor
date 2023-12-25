package AutoDriveEditor.Managers;

import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedo;
import static AutoDriveEditor.GUI.Menus.EditMenu.RedoMenu.menu_Redo;
import static AutoDriveEditor.GUI.Menus.EditMenu.UndoMenu.menu_Undo;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

/**
 * Manages a Queue of Changables to perform undo and/or redo operations. Clients can add implementations of the Changeable
 * class to this class, and it will manage undo/redo as a Queue.
 *
 * @author Greg Cope
 *
 */

public class ChangeManager {

    // Interface to implement a Changeable type of action - either undo or redo.
    // @author Greg COpe

    public interface Changeable {
        // Undoes an action
        void undo();

        // Redoes an action
        void redo();
    }

    //the current index node
    private Node currentIndex;
    //the parent node far left node.
    private final Node parentNode = new Node();
    /**
     * Creates a new ChangeManager object which is initially empty.
     */
    public ChangeManager(){
        LOG.info("Initializing new ChangeManager");
        menu_Undo.setEnabled(false);
        menu_Redo.setEnabled(false);
        currentIndex = parentNode;
    }

     // Creates a new ChangeManager which is a duplicate of the parameter in both contents and current index.

    @SuppressWarnings("unused")
    public ChangeManager(ChangeManager manager){
        this();
        currentIndex = manager.currentIndex;
    }

     // Clears all Changables contained in this manager.

    @SuppressWarnings("unused")
    public void clear(){
        currentIndex = parentNode;
    }

     // Add a Changeable to manage.

    public void addChangeable(Changeable changeable){
        Node node = new Node(changeable);
        currentIndex.right = node;
        node.left = currentIndex;
        currentIndex = node;
        if (bDebugLogUndoRedo) LOG.info("addChangeable");
        menu_Undo.setEnabled(true);
    }

     // Return if undo can be performed.

    public boolean canUndo() { return currentIndex != parentNode;}

     // Return if redo can be performed.

    public boolean canRedo() { return currentIndex.right != null;}

     // Undoes the Changeable at the current index.

    public void undo(){
        //validate
        suspendAutoSaving();

        if ( !canUndo() ){
            LOG.info("Reached Beginning of Undo History.");
            menu_Undo.setEnabled(false);
            resumeAutoSaving();
            return;
            //throw new IllegalStateException("Cannot undo. Index is out of range.");
        }
        //undo
        if (currentIndex.changeable != null) {
            currentIndex.changeable.undo();
        } else {
            LOG.info("Unable to Undo");
        }
        //set index
        moveLeft();
        resumeAutoSaving();
    }

    /**
     * Moves the internal pointer of the backed linked list to the left.
     * @throws IllegalStateException If the left index is null.
     */

    private void moveLeft(){
        if ( currentIndex.left == null ){
            throw new IllegalStateException("Internal index set to null.");
        }
        currentIndex = currentIndex.left;
        menu_Undo.setEnabled(canUndo());
        menu_Redo.setEnabled(canRedo());

    }

    /**
     * Moves the internal pointer of the backed linked list to the right.
     * @throws IllegalStateException If the right index is null.
     */

    private void moveRight(){
        if ( currentIndex.right == null ){
            throw new IllegalStateException("Internal index set to null.");
        }
        currentIndex = currentIndex.right;
        menu_Undo.setEnabled(canUndo());
        menu_Redo.setEnabled(canRedo());
    }

    /**
     * Redoes the Changable at the current index.
     * @throws IllegalStateException if canRedo returns false.
     */

    public void redo(){
        //validate
        suspendAutoSaving();
        if ( !canRedo() ){
            LOG.info("Reached End of Undo History.");
            menu_Redo.setEnabled(false);
            resumeAutoSaving();
            return;
        }
        //reset index
        moveRight();
        //redo
        if (currentIndex.changeable != null) {
            currentIndex.changeable.redo();
        } else {
            LOG.info("Unable to Redo");
        }

        resumeAutoSaving();
    }

    /**
     * Inner class to implement a doubly linked list for our queue of changeables.
     * @author Greg Cope
     *
     */

    private static class Node {
        private Node left = null;
        private Node right = null;
        private final Changeable changeable;

        public Node(Changeable c){
            changeable = c;
        }

        public Node(){
            changeable = null;
        }
    }
}