
/*************************************************************************
 *  Compilation:  javac MultiTree.java
 *  Execution:    java MultiTree
 *
 *  A class representing a tree of objects of type E.
 *  We assume that no two descendents of a common ancestor
 *  contain the same data.
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Arrays.*;
import java.util.Stack;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

class TreeNode<E> implements Serializable {

    private final E data;

    // the nodes that come after this one
    private ArrayList<TreeNode<E>> next;

    // the node that comes before this one
    private TreeNode<E> previous;

    // protected constructor
    protected TreeNode(E data) {
        this.data = data;
        this.next = new ArrayList<>();
        this.previous = null;
    }

    // protected constructor
    protected TreeNode() {
        this.data = null;
        this.next = new ArrayList<>();
        this.previous = null;
    }

    // protected constructor
    protected TreeNode(E data, ArrayList<TreeNode<E>> next, TreeNode<E> previous) {

        this.data = data;
        this.next = next;
        this.previous = previous;
    }

    // set the previous node
    public void setPrevious(TreeNode<E> nextOne) {
        previous = nextOne;
    }

    // get the previous node
    public TreeNode<E> getPrevious() {
        return previous;
    }

    // add a descendant node
    public void addNext(TreeNode<E> nextOne) {
        next.add(nextOne);
        nextOne.setPrevious(this);
    }

    // add a newly-created descendant node with the given data
    public void addNext(E newData) {
        next.add(new TreeNode<E>(newData, new ArrayList<TreeNode<E>>(), this));
    }

    // remove a descendant node
    public void removeNext(TreeNode<E> nextOne) {
        next.remove(nextOne);
        nextOne = null;
    }

    // get a list of all immediate descendants
    public ArrayList<TreeNode<E>> getNext() {
        return next;
    }

    // get the data
    public E getData() {
        return data;
    }

    // output a String
    public String toString() {
        return data.toString();
    }

    // if this has a descendent containing e, return that descendent
    // otherwise return null
    public TreeNode<E> getDescendent(E e) {
        for (TreeNode<E> n : next) {
            if (e.equals(n.getData())) return n;
        }
        return null;
    }

} // end of class TreeNode

public class MultiTree<E> implements Serializable, Iterable<E> {

    private interface BetterIterator<E> extends Iterator<E> {
        public int getLevel();
        public boolean top();
    }

    private TreeNode<E> head;

    // public constructor
    public MultiTree() {
        head = new TreeNode<>();
    }

    // private constructor
    private MultiTree(E data) {
        head = new TreeNode<>();
        head.addNext(new TreeNode<>(data));
    }

    // return true if this MultiTree is empty
    public boolean isEmpty() {
        return head.getNext().isEmpty();
    }

    // return the head node
    public TreeNode<E> getHead() {
        return head;
    }

    // return the TreeNode at the top level containing the given data
    // return null if no TreeNode at the top contains the given data
    public TreeNode<E> nodeContaining(E data) {
        return nodeContaining(data, head);
    }

    // return the immediate descendent of thisNode that contains the given data
    // return null if no immediate descendent contains the given data
    public TreeNode<E> nodeContaining(E data, TreeNode<E> thisNode) {
        for (TreeNode<E> node : thisNode.getNext()) if (node.getData().equals(data)) return node;
        return null;
    }

    // add a list of data to the tree.
    // add the first element of the list to the top level, and then descend.
    // don't add an element if it's already there
    public void addChain(Iterable<E> chain) {
        TreeNode<E> currentNode = head;
        TreeNode<E> newNode = null;
        for (E data : chain) {
            TreeNode<E> nextNode = nodeContaining(data,currentNode);
            if (nextNode == null) {
                newNode = new TreeNode<>(data);
                currentNode.addNext(newNode);
                currentNode = newNode;
            } else {
                currentNode = nextNode;
            }
        }
    }

    // same as above, but with an array
    public void addChain(E[] chain) {
        addChain(Arrays.asList(chain));
    }

    // return true if this contains chain
    public boolean containsChain(List<E> chain) {
        TreeNode<E> currentNode = head;
        for (E e : chain) {
            if (!currentNode.getNext().contains(e)) return false;
            for (TreeNode<E> n : currentNode.getNext()) {
                if (n.getData().equals(e)) currentNode = n;
            }
        }
        return currentNode.getNext().isEmpty();
    }

    // same as above, but with an array
    public boolean containsChain(E[] chain) {
        containsChain(Arrays.asList(chain));
    }

    // output a list of chains represented by this tree
    public List<List<E>> getChains() {
        BetterIterator<E> i = iterator();
        List<List<E>> output = new LinkedList<>();
        List<E> fill = new LinkedList<>();
        while (i.hasNext()) {
            E data = i.next();
            if (i.getLevel()<=fill.size()) {
                fill.set(i.getLevel()-1,data);
//            } else if (i.getLevel()>fill.size()) {
//                throw new IllegalArgumentException("Trying to add to level " + fill.size() + " of a chain using data from level " + i.getLevel() + ".");
            } else {
                fill.add(data);
            }
            if (i.top()) {
                output.add(ImmutableList.copyOf(fill));
            }
        }
        return output;
    }

    // output a String
    public String toString() {
        String output = "";
        BetterIterator<E> it = iterator();
        while (it.hasNext()) {
            E nexto = it.next();
            output += "Level: " + it.getLevel() + ".  " + nexto + "\n";
        }
        return output;
    }

    // output a String of chains
    public String chainString() {
        String output = "";
        for (List<E> l : getChains()) {
            for (E e : l) {
                output += e + " ";
            }
            output += "\n";
        }
        return output;
    }

    // equals method.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        MultiTree l = (MultiTree) obj;
        BetterIterator<E> it1 = this.iterator();
        BetterIterator<E> it2 = l.iterator();
        while (it1.hasNext()) {
            if (!it2.hasNext()) return false;
            if (!it2.next().equals(it1.next())) return false;
        }
        if (it2.hasNext()) return false;
        return true;
    }

    // hashCode override.
    public int hashCode() {
        int prime = 59;
        int result = 19;
        for (E data : this)
            result = prime*result + data.hashCode();
        return result;
    }

    public BetterIterator<E> iterator() { // iterator begins here
        return new BetterIterator<E>() {

            private TreeNode<E> current = head;
            private int level = 0;

            public boolean hasNext() {
                return hasNext(current);
            }

            private boolean hasNext(TreeNode<E> node) {
                if (!node.getNext().isEmpty()) {
                    return true;
                } else {
                    return hasNext(node.getPrevious(),node);
                }
            }

            private boolean hasNext(TreeNode<E> parent, TreeNode<E> child) {
                if (parent == null) {
                    return false;
                } else if (parent.getNext().indexOf(child) < parent.getNext().size()-1) {
                    return true;
                } else {
                    return hasNext(parent.getPrevious(),parent);
                }
            }

            public E next() {
                current = next(current);
                return current.getData();
            }

            private TreeNode<E> next(TreeNode<E> node) {
                if (!node.getNext().isEmpty()) {
                    level++;
                    return node.getNext().get(0);
                } else {
                    level--;
                    return next(node.getPrevious(),node);
                }
            }

            private TreeNode<E> next(TreeNode<E> parent, TreeNode<E> child) {
                int j = parent.getNext().indexOf(child);
                if (j < parent.getNext().size()-1) {
                    level++;
                    return parent.getNext().get(j+1);
                } else {
                    level--;
                    return next(parent.getPrevious(),parent);
                }
            }

            public void remove() { // do nothing
            }

            public int getLevel() {
                return level; 
            }

            public boolean top() {
                return current.getNext().isEmpty(); 
            }

        };
    } // first iterator ends here

    // test client
    public static void main(String[] args) {

    }

} // end of class MultiTree
