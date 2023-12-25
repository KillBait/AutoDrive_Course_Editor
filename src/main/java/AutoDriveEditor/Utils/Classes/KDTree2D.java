package AutoDriveEditor.Utils.Classes;

import AutoDriveEditor.RoadNetwork.MapNode;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class KDTree2D {
    private KDTree2DNode root;
    private int size;

    public KDTree2D() {
        root = null;
        size = 0;
    }

    public void insert(MapNode mapNode) {
        root = insertToTree(root, mapNode, 0);
        size++;
    }

    private KDTree2DNode insertToTree(KDTree2DNode root, MapNode mapNode, int depth) {
        if (root == null) return new KDTree2DNode(mapNode);

        // Calculate the current dimension (0 for x, 1 for y)
        int cd = depth % 2;

        // Compare the new point with the current node based on the current dimension
        if (cd == 0) {
            if (mapNode.x < root.mapNode.x)
                root.left = insertToTree(root.left, mapNode,depth + 1);
            else
                root.right = insertToTree(root.right, mapNode, depth + 1);
        } else {
            if (mapNode.z < root.mapNode.z)
                root.left = insertToTree(root.left, mapNode, depth + 1);
            else
                root.right = insertToTree(root.right, mapNode, depth + 1);
        }

        return root;
    }

    // Search for all nodes within a specified distance of a given point
    @SuppressWarnings("unused")
    public List<KDTree2DNode> searchDistance(MapNode mapNode, double distance) {
        List<KDTree2DNode> result = new ArrayList<>();
        searchDist(root, mapNode, distance, 0, result);
        return result;
    }

    private void searchDist(KDTree2DNode root, MapNode mapNode, double distance, int depth, List<KDTree2DNode> result) {
        if (root == null) return;

        // Calculate the current dimension (0 for x, 1 for y)
        int cd = depth % 2;

        // Calculate the squared distance between the current node and the target point
        double distSquared = Math.pow(root.mapNode.x - mapNode.x, 2) + Math.pow(root.mapNode.z - mapNode.z, 2);

        // If the current node is within the specified distance and not exactly on the target point, add it to the result
        if (distSquared < distance * distance && distSquared > 0) {
            result.add(root);
        }

        // Compare the target point with the current node based on the current dimension
        if (cd == 0) {
            if (mapNode.x - distance <= root.mapNode.x)
                searchDist(root.left, mapNode, distance, depth + 1, result);
            if (mapNode.x + distance >= root.mapNode.x)
                searchDist(root.right, mapNode, distance, depth + 1, result);
        } else {
            if (mapNode.z - distance <= root.mapNode.z)
                searchDist(root.left, mapNode, distance, depth + 1, result);
            if (mapNode.z + distance >= root.mapNode.z)
                searchDist(root.right, mapNode, distance, depth + 1, result);
        }
    }

    // Search for all nodes within a specified radius of a given point
    @SuppressWarnings("unused")
    public List<KDTree2DNode> searchRadius(double x, double y, double radius) {
        List<KDTree2DNode> result = new ArrayList<>();
        searchRad(root, x, y, radius, 0, result);
        return result;
    }

    private void searchRad(KDTree2DNode root, double x, double y, double radius, int depth, List<KDTree2DNode> result) {
        if (root == null) return;

        // Calculate the current dimension (0 for x, 1 for y)
        int cd = depth % 2;

        // Calculate the squared distance between the current node and the target point
        double distSquared = Math.pow(root.mapNode.x - x, 2) + Math.pow(root.mapNode.z - y, 2);

        // If the current node is within the specified radius, add it to the result
        if (distSquared <= radius * radius) {
            result.add(root);
        }

        // Compare the target point with the current node based on the current dimension
        if (cd == 0) {
            if (x - radius <= root.mapNode.x)
                searchRad(root.left, x, y, radius, depth + 1, result);
            if (x + radius >= root.mapNode.x)
                searchRad(root.right, x, y, radius, depth + 1, result);
        } else {
            if (y - radius <= root.mapNode.z)
                searchRad(root.left, x, y, radius, depth + 1, result);
            if (y + radius >= root.mapNode.z)
                searchRad(root.right, x, y, radius, depth + 1, result);
        }
    }


    // Get the number of nodes in the KDTree

    @SuppressWarnings("unused")
    public int size() {
        return size;
    }

    // Empty the KDTree

    @SuppressWarnings("unused")
    public void empty() {
        root = null;
        size = 0;
    }


    //
    // currently unused functions, have not been 100% tested and validated to behave as expected.
    //


    // Remove a specific point from the KDTree

    public boolean remove(double x, double y) {
        return removeFromTree(root, x, y, 0, null);
    }

    private boolean removeFromTree(KDTree2DNode root, double x, double y, int depth, KDTree2DNode parent) {
        if (root == null)
            return false;

        // Calculate the current dimension (0 for x, 1 for y)
        int cd = depth % 2;

        if (root.mapNode.x == x && root.mapNode.z == y) {
            // Found the point to remove

            // If the node has two children, replace it with the in-order successor
            if (root.left != null && root.right != null) {
                KDTree2DNode successor = findMin(root.right, cd, depth + 1);
                root.mapNode.x = successor.mapNode.x;
                root.mapNode.z = successor.mapNode.z;
                removeFromTree(root.right, successor.mapNode.x, successor.mapNode.z, depth + 1, root);
            } else {
                // If the node has one child or no children
                if (root == parent.left) {
                    if (root.left != null)
                        parent.left = root.left;
                    else
                        parent.left = root.right;
                } else if (root == parent.right) {
                    if (root.left != null)
                        parent.right = root.left;
                    else
                        parent.right = root.right;
                } else {
                    // If the root is the parent, set the new root
                    this.root = (root.left != null) ? root.left : root.right;
                }
                size--;
            }
            return true;
        }

        // Compare the target point with the current node based on the current dimension
        if (cd == 0) {
            if (x < root.mapNode.x)
                return removeFromTree(root.left, x, y, depth + 1, root);
            else
                return removeFromTree(root.right, x, y, depth + 1, root);
        } else {
            if (y < root.mapNode.z)
                return removeFromTree(root.left, x, y, depth + 1, root);
            else
                return removeFromTree(root.right, x, y, depth + 1, root);
        }
    }

    // Find the minimum node along the given dimension
    private KDTree2DNode findMin(KDTree2DNode node, int cd, int depth) {
        if (node == null)
            return null;

        // Calculate the current dimension (0 for x, 1 for y)
        int currentCD = depth % 2;

        if (currentCD == cd) {
            if (node.left == null)
                return node;
            return findMin(node.left, cd, depth + 1);
        } else {
            KDTree2DNode leftMin = findMin(node.left, cd, depth + 1);
            KDTree2DNode rightMin = findMin(node.right, cd, depth + 1);

            KDTree2DNode minNode = (leftMin != null && leftMin.mapNode.x < node.mapNode.x) ? leftMin : node;
            return (rightMin != null && rightMin.mapNode.x < minNode.mapNode.x) ? rightMin : minNode;
        }
    }

    //
    // Search for all nodes within a specified rectangular region
    //

    public List<KDTree2DNode> searchRectangle(double x1, double y1, double x2, double y2) {
        List<KDTree2DNode> result = new ArrayList<>();
        searchRectangleRec(root, x1, y1, x2, y2, 0, result);
        return result;
    }

    private void searchRectangleRec(KDTree2DNode root, double x1, double y1, double x2, double y2, int depth, List<KDTree2DNode> result) {
        if (root == null)
            return;

        // Calculate the current dimension (0 for x, 1 for y)
        int cd = depth % 2;

        // Check if the current node is within the specified rectangular region
        if (root.mapNode.x >= x1 && root.mapNode.x <= x2 && root.mapNode.z >= y1 && root.mapNode.z <= y2) {
            result.add(root);
        }

        // Compare the target point with the current node based on the current dimension
        if (cd == 0) {
            // If the current dimension is x, compare x values
            if (root.mapNode.x >= x1)
                searchRectangleRec(root.left, x1, y1, x2, y2, depth + 1, result);
            if (root.mapNode.x <= x2)
                searchRectangleRec(root.right, x1, y1, x2, y2, depth + 1, result);
        } else {
            // If the current dimension is y, compare y values
            if (root.mapNode.z >= y1)
                searchRectangleRec(root.left, x1, y1, x2, y2, depth + 1, result);
            if (root.mapNode.z <= y2)
                searchRectangleRec(root.right, x1, y1, x2, y2, depth + 1, result);
        }
    }
}
