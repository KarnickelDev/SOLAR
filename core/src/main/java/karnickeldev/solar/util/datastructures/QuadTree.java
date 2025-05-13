package karnickeldev.solar.util.datastructures;

import karnickeldev.solar.physics.PhysicsObject;

import java.util.*;

public class QuadTree<T extends PhysicsObject> {

    private QuadTreeNode<T> root;


    public QuadTree() {

    }

    public void insert(T element) {
        if (root == null) {
            root = new QuadTreeNode<>(element.getX(), element.getY(), element.getMass());
            root.element = element;
        } else {
            insert(root, element);
        }
    }

    public boolean remove(T element) {
        return remove(this.root, element);
    }

    public List<T> queryInArea(double xMin, double yMin, double xMax, double yMax) {
        List<T> results = new ArrayList<>();
        queryInArea(root, xMin, yMin, xMax, yMax, results);
        return results;
    }

    private boolean isLeaf(QuadTreeNode<T> node) {
        for (int i = 0; i < node.children.length; i++) {
            if (node.children[i] != null) return false;
        }
        return true;
    }

    private int getQuadrant(QuadTreeNode<T> node, T element) {
        boolean north = element.getY() < node.y;
        boolean west = element.getX() < node.x;
        if (north && west) return 0; // NW
        if (north) return 1; // NE
        if (west) return 2; // SW
        return 3; // SE
    }

    private void subdivide(QuadTreeNode<T> node) {
        double halfSize = node.size / 2d;
        node.children[0] = new QuadTreeNode<>(node.x - halfSize / 2, node.y - halfSize / 2, halfSize); // NW
        node.children[1] = new QuadTreeNode<>(node.x + halfSize / 2, node.y - halfSize / 2, halfSize); // NE
        node.children[2] = new QuadTreeNode<>(node.x - halfSize / 2, node.y + halfSize / 2, halfSize); // SW
        node.children[3] = new QuadTreeNode<>(node.x + halfSize / 2, node.y + halfSize / 2, halfSize); // SE
    }

    private void insert(QuadTreeNode<T> node, T element) {
        if (node == null || element == null) return;

        boolean isLeaf = isLeaf(node);
        if (node.element == null && isLeaf) {
            // insert into this empty node
            node.element = element;
            node.mass = element.getMass();
            node.x = element.getX();
            node.y = element.getY();
        } else {
            if (isLeaf) {
                subdivide(node);

                // move old element into child
                if (node.element != null) {
                    int quadrant = getQuadrant(node, node.element);
                    insert(node.children[quadrant], node.element);
                    node.element = null;
                }
            }

            // update center of mass
            node.mass += element.getMass();
            node.x = (node.x * node.mass + element.getX() * element.getMass()) / (node.mass + element.getMass());
            node.y = (node.y * node.mass + element.getY() * element.getMass()) / (node.mass + element.getMass());

            // insert
            int index = getQuadrant(node, element);
            insert(node.children[index], element);
        }
    }

    private boolean remove(QuadTreeNode<T> node, T element) {
        if (node == null || element == null) return false;

        if (node.element != null && node.element.equals(element)) {
            // remove from leaf
            node.element = null;
            node.mass = 0;
            return true;
        }

        int quadrant = getQuadrant(node, element);
        if (node.children[quadrant] != null) {
            // remove from child
            if (remove(node.children[quadrant], element)) {
                if (isCollapsible(node)) {
                    collapse(node);
                }
                return true;
            }
        }

        return false;
    }

    private boolean isCollapsible(QuadTreeNode<T> node) {
        int numChildren = 0;
        QuadTreeNode<T> lastChild = null;

        for (QuadTreeNode<T> child : node.children) {
            if (child != null) {
                numChildren++;
                lastChild = child;
            }
        }

        // Collapsible if there is at most one non-null child and no children have their own children
        return numChildren <= 1 && (lastChild == null || isLeaf(lastChild));
    }

    private void collapse(QuadTreeNode<T> node) {
        // If there is one child with an object, move it up to the parent
        for (QuadTreeNode<T> child : node.children) {
            if (child != null && child.element != null) {
                node.element = child.element;
                node.mass = child.mass;
                node.x = child.x;
                node.y = child.y;
            }
        }

        // Clear all children
        Arrays.fill(node.children, null);
    }

    private void queryInArea(QuadTreeNode<T> node, double xMin, double yMin, double xMax, double yMax, List<T> results) {
        if (node == null) return;

        // Check if this node is in range
        if (node.element != null) {
            double x = node.element.getX();
            double y = node.element.getY();
            if (x >= xMin && x <= xMax && y >= yMin && y <= yMax) {
                results.add(node.element);
            }
        }

        double halfSize = node.size / 2;

        // Check if children might be in range
        if (node.children[0] != null) {
            for (QuadTreeNode<T> child : node.children) {
                if (child != null && !(
                    ((node.x + halfSize) < xMin)
                        || ((node.x - halfSize) > xMin)
                        || ((node.y + halfSize) < yMin)
                        || ((node.y - halfSize) > yMax))
                ) {
                    queryInArea(child, xMin, yMin, xMax, yMax, results);
                }
            }
        }
    }

    private static class QuadTreeNode<T extends PhysicsObject> {
        private double x, y;
        private float mass;
        private double size;

        private T element;

        private QuadTreeNode<T>[] children;

        private QuadTreeNode(double x, double y, double size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.mass = 0;
            this.element = null;

            this.children = new QuadTreeNode[4];
        }
    }

    public static class QuadTreeIterator<T extends PhysicsObject> implements Iterator<T> {
        private final Stack<QuadTreeNode<T>> stack;
        private T nextObject;

        public QuadTreeIterator(QuadTree<T> quadTree) {
            stack = new Stack<>();
            if (quadTree != null && quadTree.root != null) {
                stack.push(quadTree.root);
            }
            advance(); // Prepare the first object
        }

        @Override
        public boolean hasNext() {
            return nextObject != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T currentObject = nextObject;
            advance();
            return currentObject;
        }

        // Prepares the next object by advancing the state
        private void advance() {
            nextObject = null; // Reset nextObject
            while (!stack.isEmpty()) {
                QuadTreeNode<T> node = stack.pop();
                if (node.element != null) {
                    nextObject = node.element; // next object
                    return;
                }
                for (int i = 0; i < node.children.length; i++) {
                    if (node.children[i] != null) {
                        stack.push(node.children[i]);
                    }
                }
            }
        }
    }

}
