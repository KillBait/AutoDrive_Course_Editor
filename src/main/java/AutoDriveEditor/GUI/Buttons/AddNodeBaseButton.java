package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.MapPanel.MapImage.mapPanelImage;
import static AutoDriveEditor.MapPanel.MapPanel.*;
import static AutoDriveEditor.RoadNetwork.RoadMap.createNewNetworkNode;

public abstract class AddNodeBaseButton extends BaseButton {

    public static MapNode createNode(double worldX, double worldZ, int flag) {
        canAutoSave = false;
        if ((roadMap == null) || (mapPanelImage == null)) {
            return null;
        }
        MapNode newNode = createNewNetworkNode(worldX, worldZ, flag, false, false);
        getMapPanel().repaint();
        changeManager.addChangeable( new AddNodeChanger(newNode));
        setStale(true);
        canAutoSave = true;
        return newNode;
    }

    //
    // Add node
    //

    public static class AddNodeChanger implements ChangeManager.Changeable {
        private final MapNode storeNode;
        private final boolean isStale;

        public AddNodeChanger(MapNode node){
            super();
            this.storeNode = node;
            this.isStale = isStale();
        }

        public void undo(){
            RoadMap.removeMapNode(storeNode);
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            roadMap.insertMapNode(storeNode, null, null);
            getMapPanel().repaint();
            setStale(true);
        }
    }
}
