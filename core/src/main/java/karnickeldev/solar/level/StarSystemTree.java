package karnickeldev.solar.level;

import karnickeldev.solar.core.Logger;
import karnickeldev.solar.physics.OrbitalObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class StarSystemTree {

    protected static class Node {

        private OrbitalObject element;

        private List<Node> children;

        protected Node(OrbitalObject element) {
            this.element = element;
        }

        protected void addChild(OrbitalObject object) {
            if(children == null) children = new ArrayList<>(2);
            for(Node child: children) {
                if(child.element.equals(object)) {
                    Logger.log(Logger.GENERAL, "Tried to add duplicate element to Tree");
                    return;
                }
            }
            children.add(new Node(object));
        }

    }


    private final Node root;

    public StarSystemTree() {
        this(null);
    }

    public StarSystemTree(OrbitalObject root) {
        this.root = new Node(root);
    }

    public boolean insert(OrbitalObject object) {
        if(root.element == null) {
            root.element = object;
            return true;
        } else {
            return insert(root, object);
        }
    }

    private static boolean insert(Node node, OrbitalObject object) {
        if(node == null || object == null || object.getOrbitData().getCentralBody() == null) return false;

        // Detect circular reference
        if (object.equals(node.element)) {
            Logger.log(Logger.GENERAL, "Circular reference detected: " + object);
            return false;
        }

        if(object.getOrbitData().getCentralBody().equals(node.element)) {
            // found objects parent, add to it
            node.addChild(object);
            return true;
        } else if(node.children != null) {
            for(Node child: node.children) {
                if(insert(child, object)) return true;
            }
        }

        return false;
    }

    public List<OrbitalObject> getAllObjects() {
        List<OrbitalObject> result = new ArrayList<>();
        Iterator iterator = new Iterator(this);

        while(iterator.hasNext()) {
            result.add(iterator.next());
        }

        return result;
    }

    public static class Iterator implements java.util.Iterator<OrbitalObject> {

        private final StarSystemTree system;
        private final Stack<Node> stack;

        public Iterator(StarSystemTree system) {
            this.stack = new Stack<>();
            this.system = system;
            reset();
        }

        @Override
        public boolean hasNext() {
            return !stack.isEmpty();
        }

        @Override
        public OrbitalObject next() {
            if(!hasNext()) return null;

            Node curr = stack.pop();
            if(curr.children != null) {
                for(Node children : curr.children) {
                    stack.push(children);
                }
            }

            return curr.element;
        }

        public void reset() {
            stack.clear();
            stack.push(system.root);
        }
    }


}
