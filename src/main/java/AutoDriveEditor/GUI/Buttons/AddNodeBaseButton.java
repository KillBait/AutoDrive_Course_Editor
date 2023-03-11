package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.MapPanel.MapImage.mapPanelImage;
import static AutoDriveEditor.MapPanel.MapPanel.*;

public abstract class AddNodeBaseButton extends BaseButton {

    public static MapNode createNode(double worldX, double worldZ, int flag) {
        canAutoSave = false;
        if ((roadMap == null) || (mapPanelImage == null)) {
            return null;
        }
        double heightMapY = getYValueFromHeightMap(worldX, worldZ);
        MapNode mapNode = new MapNode(RoadMap.networkNodesList.size()+1, worldX, heightMapY, worldZ, flag, false, false); //flag = 0 causes created node to be regular by default
        RoadMap.networkNodesList.add(mapNode);
        getMapPanel().repaint();
        changeManager.addChangeable( new AddNodeChanger(mapNode) );
        setStale(true);
        canAutoSave = true;
        return mapNode;
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
