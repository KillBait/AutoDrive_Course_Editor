package AutoDriveEditor.Utils.Classes;

import AutoDriveEditor.RoadNetwork.MapNode;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class KDTree3D {
    private static final int K = 3; // 3-dimensional KD tree for x, y, z
    private KDTree3DNode root;

    public KDTree3D() {
        root = null;
    }

    public void insert(MapNode node) {
        root = insert(root, node, 0);
    }

    private KDTree3DNode insert(KDTree3DNode node, MapNode data, int depth) {
        if (node == null) return new KDTree3DNode(data);

        int axis = depth % K;

        if (axis == 0) { // Compare x
            if (data.x < node.mapNode.x) {
                node.left = insert(node.left, data, depth + 1);
            } else {
                node.right = insert(node.right, data, depth + 1);
            }
        } else if (axis == 1) { // Compare y
            if (data.y < node.mapNode.y) {
                node.left = insert(node.left, data, depth + 1);
            } else {
                node.right = insert(node.right, data, depth + 1);
            }
        } else { // Compare z
            if (data.z < node.mapNode.z) {
                node.left = insert(node.left, data, depth + 1);
            } else {
                node.right = insert(node.right, data, depth + 1);
            }
        }

        return node;
    }

    public void remove(MapNode node) {
        root = remove(root, node, 0);
    }

    private KDTree3DNode remove(KDTree3DNode node, MapNode data, int depth) {
        if (node == null) {
            return null;
        }

        int axis = depth % K;
        if (axis == 0) { // Compare x
            if (data.x < node.mapNode.x) {
                node.left = remove(node.left, data, depth + 1);
            } else if (data.x > node.mapNode.x) {
                node.right = remove(node.right, data, depth + 1);
            } else {
                if (data.equals(node.mapNode)) {
                    if (node.right == null) {
                        return node.left;
                    } else if (node.left == null) {
                        return node.right;
                    }
                    node.mapNode = findMin(node.right, 0);
                    node.right = remove(node.right, node.mapNode, depth + 1);
                } else {
                    node.left = remove(node.left, data, depth + 1);
                }
            }
        } else if (axis == 1) { // Compare y
            if (data.y < node.mapNode.y) {
                node.left = remove(node.left, data, depth + 1);
            } else if (data.y > node.mapNode.y) {
                node.right = remove(node.right, data, depth + 1);
            } else {
                if (data.equals(node.mapNode)) {
                    if (node.right == null) {
                        return node.left;
                    } else if (node.left == null) {
                        return node.right;
                    }
                    node.mapNode = findMin(node.right, 0);
                    node.right = remove(node.right, node.mapNode, depth + 1);
                } else {
                    node.left = remove(node.left, data, depth + 1);
                }
            }
        } else { // Compare z
            if (data.z < node.mapNode.z) {
                node.left = remove(node.left, data, depth + 1);
            } else if (data.z > node.mapNode.z) {
                node.right = remove(node.right, data, depth + 1);
            } else {
                if (data.equals(node.mapNode)) {
                    if (node.right == null) {
                        return node.left;
                    } else if (node.left == null) {
                        return node.right;
                    }
                    node.mapNode = findMin(node.right, 0);
                    node.right = remove(node.right, node.mapNode, depth + 1);
                } else {
                    node.left = remove(node.left, data, depth + 1);
                }
            }
        }

        return node;
    }

    private MapNode findMin(KDTree3DNode node, int depth) {
        int axis = depth % K;
        if (axis == 0) {
            if (node.left == null) return node.mapNode;
            return findMin(node.left, depth + 1);
        } else if (axis == 1) {
            if (node.left == null) return node.mapNode;
            return findMin(node.left, depth + 1);
        } else {
            if (node.left == null) return node.mapNode;
            return findMin(node.left, depth + 1);
        }
    }

    public MapNode withinRadius(double centerX, double centerY, double centerZ, double radius) {
        MapNode tempNode = new MapNode(-1, centerX, centerY, centerZ, MapNode.NODE_FLAG_REGULAR, false, false);
        List<MapNode> result = new ArrayList<>();
        withinRadiusExcludingY(root, tempNode, radius, 0, result);
       /*if (result.size() >0 ) {
            for (MapNode node: result) {
                LOG.info("result {}", node.id);
            }
        }*/
        if (result.size() > 0 )  {
            return result.get(0);
        } else {
            return null;
        }
    }

    public List<MapNode> withinRadius(MapNode center, double radius) {
        List<MapNode> result = new ArrayList<>();
        withinRadius(root, center, radius, 0, result);
        result.remove(center); // Remove the center node if found
        return result;
    }

    private void withinRadiusExcludingY(KDTree3DNode node, MapNode center, double radius, int depth, List<MapNode> result) {
        if (node == null) {
            return;
        }

        // Calculate the squared distance between the node and the center (ignoring y-coordinate)
        double distanceSquared = 0;
        for (int i = 0; i < K; i++) {
            if (i != 1) { // Skip y-coordinate (index 1)
                double diff = getAxisValue(center, depth, i) - getAxisValue(node.mapNode, depth, i);
                //if (i == 0) LOG.info(" X , centre={}, Tree mapNode({}) = {}", getAxisValueNoZ(center, depth, i), node.mapNode.id, getAxisValueNoZ(node.mapNode, depth, i));
                //if (i == 2) LOG.info(" Z , centre={}, Tree mapNode({}) = {}", getAxisValueNoZ(center, depth, i), node.mapNode.id, getAxisValueNoZ(node.mapNode, depth, i));

                distanceSquared += diff * diff;
            }
        }

        //double distSquared = Math.pow(node.mapNode.x - center.x, 2) + Math.pow(node.mapNode.z - center.z, 2);

        if (distanceSquared <= radius * radius) {
            result.add(node.mapNode);
        }

        int axis = depth % K;

        if (axis == 0) {
            if (center.x - radius <= node.mapNode.x)
                withinRadiusExcludingY(node.left, center, radius, depth + 1, result);
            if (center.x + radius >= node.mapNode.x)
                withinRadiusExcludingY(node.right, center, radius, depth + 1, result);
        } else if (axis == 1) {
            withinRadiusExcludingY(node.left, center, radius, depth + 1, result);
            withinRadiusExcludingY(node.right, center, radius, depth + 1, result);
        } else {
            if (center.z - radius <= node.mapNode.z)
                withinRadiusExcludingY(node.left, center, radius, depth + 1, result);
            if (center.z + radius >= node.mapNode.z)
                withinRadiusExcludingY(node.right, center, radius, depth + 1, result);
        }
    }

    private void withinRadius(KDTree3DNode node, MapNode center, double radius, int depth, List<MapNode> result) {
        if (node == null) {
            return;
        }

        // Calculate the squared distance between the node and the center
        double distanceSquared = 0;
        for (int i = 0; i < K; i++) {
            double diff = getAxisValue(center, depth, i) - getAxisValue(node.mapNode, depth, i);
            distanceSquared += diff * diff;
        }

        if (distanceSquared <= radius * radius) {
            if (!node.mapNode.equals(center)) result.add(node.mapNode); // Exclude the center node
        }

        int axis = depth % K;
        if (getAxisValue(center, depth, axis) - radius <= getAxisValue(node.mapNode, depth, axis)) {
            withinRadius(node.left, center, radius, depth + 1, result);
        }
        if (getAxisValue(center, depth, axis) + radius >= getAxisValue(node.mapNode, depth, axis)) {
            withinRadius(node.right, center, radius, depth + 1, result);
        }
    }

    public List<MapNode> withinDistance(MapNode center, double distance) {
        List<MapNode> result = new ArrayList<>();
        withinDistance(root, center, distance, 0, result);
        result.remove(center); // Remove the center node if found
        return result;
    }

    private void withinDistance(KDTree3DNode node, MapNode center, double distance, int depth, List<MapNode> result) {
        if (node == null) {
            return;
        }

        // Calculate the squared distance between the node and the center
        double distanceSquared = 0;
        for (int i = 0; i < K; i++) {
            double diff = getAxisValue(center, depth, i) - getAxisValue(node.mapNode, depth, i);
            distanceSquared += diff * diff;
        }

        if (distanceSquared <= distance * distance) {
            if (!node.mapNode.equals(center)) { // Exclude the center node
                result.add(node.mapNode);
            }
        }

        int axis = depth % K;
        if (getAxisValue(center, depth, axis) - distance <= getAxisValue(node.mapNode, depth, axis)) {
            withinDistance(node.left, center, distance, depth + 1, result);
        }
        if (getAxisValue(center, depth, axis) + distance >= getAxisValue(node.mapNode, depth, axis)) {
            withinDistance(node.right, center, distance, depth + 1, result);
        }
    }

    public List<MapNode> withinRectangle(MapNode min, MapNode max) {
        List<MapNode> result = new ArrayList<>();
        withinRectangle(root, min, max, 0, result);
        return result;
    }

    private void withinRectangle(KDTree3DNode node, MapNode min, MapNode max, int depth, List<MapNode> result) {
        if (node == null) {
            return;
        }

        boolean isInsideRectangle = true;
        for (int i = 0; i < K; i++) {
            double nodeValue = getAxisValue(node.mapNode, depth, i);
            if (nodeValue < getAxisValue(min, depth, i) || nodeValue > getAxisValue(max, depth, i)) {
                isInsideRectangle = false;
                break;
            }
        }

        if (isInsideRectangle) {
            result.add(node.mapNode);
        }

        int axis = depth % K;
        if (getAxisValue(node.mapNode, depth, axis) >= getAxisValue(min, depth, axis)) {
            withinRectangle(node.left, min, max, depth + 1, result);
        }
        if (getAxisValue(node.mapNode, depth, axis) <= getAxisValue(max, depth, axis)) {
            withinRectangle(node.right, min, max, depth + 1, result);
        }
    }

    // Helper method to get the value of a specific axis (x, y, or z) for a node

    private double getAxisValue(MapNode node, int depth, int axis) {
        switch (axis) {
            case 0:
                return node.x;
            case 1:
                return node.y;
            case 2:
                return node.z;
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }

    public ArrayList<MapNode> withinWorldCoordinateRectangle(double minX, double minZ, double maxX, double maxZ) {
        ArrayList<MapNode> result = new ArrayList<>();
        withinWorldCoordinateRectangle(root, minX, minZ, maxX, maxZ, 0, result);
        return result;
    }

    private void withinWorldCoordinateRectangle(KDTree3DNode root, double minX, double minZ, double maxX, double maxZ, int depth, List<MapNode> result) {
        if (root == null) {
            return;
        }

        if (root.mapNode.x >= minX && root.mapNode.x <= maxX && root.mapNode.z >= minZ && root.mapNode.z <= maxZ) {
            result.add(root.mapNode);
        }

        int axis = depth % K;
        if (axis == 0) {
            if (root.mapNode.x >= minX) {
                withinWorldCoordinateRectangle(root.left, minX, minZ, maxX, maxZ, depth + 1, result);
            }
            if (root.mapNode.x <= maxX) {
                withinWorldCoordinateRectangle(root.right, minX, minZ, maxX, maxZ, depth + 1, result);
            }
        } else if (axis == 1) {
            //if (root.mapNode.y >= minZ) {
                withinWorldCoordinateRectangle(root.left, minX, minZ, maxX, maxZ, depth + 1, result);
            //}
            //if (root.mapNode.y <= maxZ) {
                withinWorldCoordinateRectangle(root.right, minX, minZ, maxX, maxZ, depth + 1, result);
            //}
        } else {
            if (root.mapNode.z >= minZ) {
                withinWorldCoordinateRectangle(root.left, minX, minZ, maxX, maxZ, depth + 1, result);
            }
            if (root.mapNode.z <= maxZ) {
                withinWorldCoordinateRectangle(root.right, minX, minZ, maxX, maxZ, depth + 1, result);
            }
        }
    }

    public List<MapNode> getAllNodesSortedById() {
        List<MapNode> result = new ArrayList<>();
        getAllNodesSortedById(root, result);
        return result;
    }

    private void getAllNodesSortedById(KDTree3DNode node, List<MapNode> result) {
        if (node == null) { return; }
        getAllNodesSortedById(node.left, result);
        result.add(node.mapNode);
        getAllNodesSortedById(node.right, result);
    }


    // Get the number of nodes in the KDTree

    public int size() {
        return getTreeSize(root);
    }

    private int getTreeSize(KDTree3DNode node) {
        if (node == null) {
            return 0;
        }

        int leftSize = getTreeSize(node.left);
        int rightSize = getTreeSize(node.right);

        return 1 + leftSize + rightSize;
    }

    // Empty the KDTree

    public void empty() {
        root = null;
    }
}
