package AutoDriveEditor.GUI.Buttons;

import AutoDriveEditor.Managers.ChangeManager;
import AutoDriveEditor.RoadNetwork.MapNode;
import AutoDriveEditor.RoadNetwork.RoadMap;
import AutoDriveEditor.Utils.ExceptionUtils;

import java.awt.event.MouseEvent;
import java.util.LinkedList;

import static AutoDriveEditor.AutoDriveEditor.getMapPanel;
import static AutoDriveEditor.GUI.Buttons.LinerLineBaseButton.*;
import static AutoDriveEditor.GUI.Curves.CurvePanel.*;
import static AutoDriveEditor.GUI.MapPanel.*;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogUndoRedoMenu.bDebugLogUndoRedo;
import static AutoDriveEditor.GUI.TextPanel.showInTextArea;
import static AutoDriveEditor.Locale.LocaleManager.getLocaleString;
import static AutoDriveEditor.RoadNetwork.RoadMap.showMismatchedIDError;
import static AutoDriveEditor.Utils.LoggerUtils.LOG;
import static AutoDriveEditor.XMLConfig.EditorXML.curveSliderDefault;

public abstract class CurveBaseButton extends BaseButton {

    public MapNode curveStartNode;
    public boolean controlNodeSelected = false;
    public int curvePanelNodeTypeStore = 0;
    public boolean curvePanelReverseStore = false;
    public boolean curvePanelDualStore = false;
    public int curvePanelIntPointsStore = curveSliderDefault;


    protected abstract void setCurvePreviewStartNode(MapNode startNode);
    protected abstract void setCurvePreviewEndAndDisplay(MapNode endNode);
    protected abstract boolean isCurveCreated();
    protected abstract void storeCurvePanelSettings();
    protected abstract void restoreCurvePanelSettings();

    @Override
    public void setSelected(boolean selected) {
        if (selected) {
            curvePathRegular.setEnabled(true);
            restoreCurvePanelSettings();
            button.setSelected(true);
            showInTextArea(getInfoText(), true, false);
            setCurvePanelCurrentButton(this);
        } else {
            storeCurvePanelSettings();
            button.setSelected(false);
        }
        curveOptionsPanel.repaint();
    }

    public String getInfoText() {  return button.getToolTipText(); }

    public void commitCurve() {}
    public void cancelCurve() {}

    public void setNodeType(int nodeType) {}

    public void setReversePath(boolean isSelected) {}

    public void setDualPath(boolean isDualPath) {}

    public void setNumInterpolationPoints(int numPoints) {}

    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            MapNode selected = getNodeAtScreenPosition(e.getX(), e.getY());
            if (selected != null && !selected.isControlNode()) {
                if (curveStartNode == null) {
                    setCurvePreviewStartNode(selected);
                } else if (selected == curveStartNode && !isCurveCreated()) {
                    curveStartNode = null;
                    cancelCurve();
                    showInTextArea(getLocaleString("infopanel_curve_canceled"), true, false);
                } else {
                    setCurvePreviewEndAndDisplay(selected);
                }
                getMapPanel().repaint();
            }
        }
        controlNodeSelected = false;
    }

    //
    // Curve Changer
    //

    public static class CurveChanger implements ChangeManager.Changeable {

        private final LinkedList<MapNodeStore> storedCurveNodeList;
        private final boolean isReversePath;
        private final boolean isDualPath;
        private final boolean isStale;

        public CurveChanger(LinkedList<MapNode> curveNodes, boolean isReverse, boolean isDual){
            super();

            this.storedCurveNodeList = new LinkedList<>();
            this.isReversePath = isReverse;
            this.isDualPath = isDual;
            this.isStale = isStale();

            for (int i = 0; i <= curveNodes.size() -1 ; i++) {
                MapNode mapNode = curveNodes.get(i);
                if (bDebugLogUndoRedo) LOG.info("## QuadCurveChanger ## Adding ID {} to storedCurveNodeList", mapNode.id);
                this.storedCurveNodeList.add(new MapNodeStore(mapNode));
            }
        }

        public void undo(){
            for (int i = 1; i <= this.storedCurveNodeList.size() - 2 ; i++) {
                MapNodeStore curveNode = this.storedCurveNodeList.get(i);
                if (bDebugLogUndoRedo) LOG.info("## QuadCurveChanger.undo ## Removing node ID {}", curveNode.getMapNode().id);
                RoadMap.removeMapNode(curveNode.getMapNode());
                if (curveNode.hasChangedID()) {
                    if (bDebugLogUndoRedo) LOG.info("## QuadCurveChanger.undo ## ID {} changed", curveNode.getMapNode().id);
                    curveNode.resetID();
                    if (bDebugLogUndoRedo) LOG.info("## QuadCurveChanger.undo ## Reset ID to {}", curveNode.getMapNode().id);
                }
            }
            getMapPanel().repaint();
            setStale(this.isStale);
        }

        public void redo(){
            try {
                for (int i = 1; i <= this.storedCurveNodeList.size() - 2 ; i++) {
                    MapNodeStore curveNode = this.storedCurveNodeList.get(i);
                    curveNode.clearConnections();
                    if (bDebugLogUndoRedo) LOG.info("## QuadCurveChanger ## Inserting mapNode ID {}", curveNode.getMapNode().id);
                    roadMap.insertMapNode(curveNode.getMapNode(), null,null);
                    if (curveNode.hasChangedID()) curveNode.resetID();
                }
                connectNodes(getCurveLinkedList(), this.isReversePath, this.isDualPath);
                getMapPanel().repaint();
                setStale(true);
            } catch (ExceptionUtils.MismatchedIdException e) {
                showMismatchedIDError("CurveChanger redo()", e);
            }

        }

        public static void connectNodes(LinkedList<MapNode> mergeNodesList, boolean reversePath, boolean dualPath)  {
            for (int j = 0; j < mergeNodesList.size() - 1; j++) {
                MapNode startNode = mergeNodesList.get(j);
                MapNode endNode = mergeNodesList.get(j+1);
                if (reversePath) {
                    createConnectionBetween(startNode,endNode,CONNECTION_REVERSE);
                } else if (dualPath) {
                    createConnectionBetween(startNode,endNode,CONNECTION_DUAL);
                } else {
                    createConnectionBetween(startNode,endNode,CONNECTION_STANDARD);
                }
            }
        }

        public LinkedList<MapNode> getCurveLinkedList() {
            LinkedList<MapNode> list = new LinkedList<>();
            for (int i = 0; i <= this.storedCurveNodeList.size() - 1 ; i++) {
                MapNodeStore nodeBackup = this.storedCurveNodeList.get(i);
                list.add(nodeBackup.getMapNode());
            }
            return list;
        }
    }
}
