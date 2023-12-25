package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.ExceptionUtils;

import static AutoDriveEditor.AutoDriveEditor.changeManager;
import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.Managers.ScanManager.checkNodeOverlap;
import static AutoDriveEditor.RoadNetwork.RoadMap.createNewNetworkNode;
import static AutoDriveEditor.RoadNetwork.RoadMap.showMismatchedIDError;
import static AutoDriveEditor.XMLConfig.AutoSave.resumeAutoSaving;
import static AutoDriveEditor.XMLConfig.AutoSave.suspendAutoSaving;

public abstract class AddNodeBaseButton extends BaseButton {

    public static MapNode createNode(double worldX, double worldZ, int flag) {
        if (roadMap != null) {
            suspendAutoSaving();
            MapNode newNode = createNewNetworkNode(worldX, worldZ, flag, false, false);
            getMapPanel().repaint();
            changeManager.addChangeable(new AddNodeChanger(newNode));
            setStale(true);
            resumeAutoSaving();
            return newNode;
        }
        return null;
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
            for (MapNode mapNode : storeNode.getWarningNodes()) {
                mapNode.getWarningNodes().remove(storeNode);
                if (mapNode.getWarningNodes().size() == 0) mapNode.clearWarningNodes();
            }
            if (hoveredNode == storeNode) hoveredNode = null;
            RoadMap.removeMapNode(storeNode);
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            try {
                roadMap.insertMapNode(storeNode, null, null);
                checkNodeOverlap(storeNode);
                getMapPanel().repaint();
                setStale(true);
            } catch (ExceptionUtils.MismatchedIdException e) {
                showMismatchedIDError("AddNodeChanger redo()", e);
            }

        }
    }
}
