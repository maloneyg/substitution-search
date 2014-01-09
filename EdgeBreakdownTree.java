
/*************************************************************************
 *  Compilation:  javac EdgeBreakdownTree.java
 *  Execution:    java EdgeBreakdownTree
 *
 *  A class representing all edge breakdowns that have been found so far.
 *  Also useful for storing possible edge breakdowns in a PuzzleBoundary.
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.io.Serializable;

public class EdgeBreakdownTree implements Serializable {

    private MultiTree<BasicEdgeLength>[] breakdowns;

    // these pointers are only used if we are using
    // this edge to restrict possibilities in the
    // puzzle boundary
    private TreeNode<BasicEdgeLength>[] pointers;

    // full list of edge breakdowns for all edges
    public static final EdgeBreakdownTree FULL_BREAKDOWNS;

    static { // initialize FULL_BREAKDOWNS

        EdgeBreakdownTree output = new EdgeBreakdownTree();
        for (int i = 0; i < output.breakdowns.length; i++) {
            MultiSetLinkedList m = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList<Integer>(BasicEdgeLength.createBasicEdgeLength(i).getBreakdown()));
            ImmutableList<Integer> starter = m.getImmutableList();
            do {
                output.addBreakdown(i,m.getImmutableList());
                m.iterate();
            } while (!starter.equals(m.getImmutableList()));
        }
        FULL_BREAKDOWNS = output;

    } // here ends initialization of FULL_BREAKDOWNS

    // private constructor
    private EdgeBreakdownTree() {
        breakdowns = new MultiTree[Preinitializer.N/2];
        for (int i = 0; i < breakdowns.length; i++) {
            breakdowns[i] = new MultiTree<BasicEdgeLength>();
        }
        pointers = new TreeNode[] { breakdowns[0].getHead(),breakdowns[0].getHead(),breakdowns[0].getHead() };
    }

    // private constructor
    // three edge breakdowns--one for each edge in the prototile
    // that we're searching
    private EdgeBreakdownTree(EdgeBreakdownTree base, BasicEdgeLength l) {
        breakdowns = new MultiTree[3];
        List<Integer> angles = Preinitializer.PROTOTILES.get(Preinitializer.MY_TILE);
        for (int i = 0; i < 3; i++) {
            breakdowns[i] = base.breakdowns[Initializer.acute(angles.get(i))-1];
        }
        List<List<BasicEdgeLength>> replacements = breakdowns[2].getChains();
        MultiTree<BasicEdgeLength> newTree = new MultiTree<>();
        for (List<BasicEdgeLength> a : replacements) {
            if (a.get(a.size()-1).equals(l)) newTree.addChain(a);
        }
        breakdowns[2] = newTree;
        pointers = new TreeNode[] {breakdowns[0].getHead(), breakdowns[1].getHead(), breakdowns[2].getHead()};
    }

    // private constructor
    // same as above, but without the starter
    private EdgeBreakdownTree(EdgeBreakdownTree base) {
        breakdowns = new MultiTree[3];
        List<Integer> angles = Preinitializer.PROTOTILES.get(Preinitializer.MY_TILE);
        for (int i = 0; i < 3; i++) {
            breakdowns[i] = base.breakdowns[Initializer.acute(angles.get(i))-1];
        }
        pointers = new TreeNode[] {breakdowns[0].getHead(), breakdowns[1].getHead(), breakdowns[2].getHead()};
    }

    // private constructor
    // designed for cloning base
    private EdgeBreakdownTree(EdgeBreakdownTree base, boolean dummyVariable) {
        breakdowns = base.breakdowns;
        pointers = new TreeNode[] {base.pointers[0],base.pointers[1],base.pointers[2]};
    }

    // public static factory methods
    public static EdgeBreakdownTree createEdgeBreakdownTree() {
        return new EdgeBreakdownTree();
    }

    public static EdgeBreakdownTree createEdgeBreakdownTree(EdgeBreakdownTree t, BasicEdgeLength l) {
        return new EdgeBreakdownTree(t,l);
    }

    public static EdgeBreakdownTree createEdgeBreakdownTree(EdgeBreakdownTree t) {
        return new EdgeBreakdownTree(t);
    }
    // here end the public static factory methods

    // (somewhat) deep copy method
    public EdgeBreakdownTree deepCopy() {
        return new EdgeBreakdownTree(this,true);
    }

    // merge two EdgeBreakdownTrees.
    // we assume that they are totally ordered, in the sense that
    // for each edge, the tree from one edge breakdown (this) is
    // a subtree of the tree from the other edge breakdown (older)
    public EdgeBreakdownTree merge(EdgeBreakdownTree older) {
        if (this.breakdowns.length != Preinitializer.N/2) throw new IllegalArgumentException("Trying to merge an edge breakdown created using different initialization data.");
        if (this.breakdowns.length != older.breakdowns.length) throw new IllegalArgumentException("Cannot merge two edge breakdowns with different numbers of edges.");
        EdgeBreakdownTree output = new EdgeBreakdownTree();
        for (int i = 1; i < breakdowns.length; i++) {
            if (this.breakdowns[i].isEmpty()) {
                output.breakdowns[i] = older.breakdowns[i];
            } else {
                output.breakdowns[i] = this.breakdowns[i];
            }
        }
        return output;
    }

    // add a new edge breakdown for length i
    public void addBreakdown(int i, List<BasicEdgeLength> breakdown, int dummy) {
        breakdowns[i].addChain(breakdown);
    }

    // add a new edge breakdown for length i
    public void addBreakdown(int i, ImmutableList<Integer> breakdown) {
        ArrayList<BasicEdgeLength> forward = new ArrayList<>(breakdown.size());
        ArrayList<BasicEdgeLength> reverse = new ArrayList<>(breakdown.size());
        for (int j = 0; j < breakdown.size(); j++) {
            forward.add(BasicEdgeLength.createBasicEdgeLength(breakdown.get(j)));
        }
        for (int j = 0; j < breakdown.size(); j++) {
            reverse.add(forward.get(forward.size()-j-1));
        }
        addBreakdown(i,forward,0);
        addBreakdown(i,reverse,0);
    }

    // return the descendent of pointers[i] that contains l
    // if there is none, return null
    public TreeNode<BasicEdgeLength> getDescendent(int i, BasicEdgeLength l) {
        if (i < 0 || i >= pointers.length) throw new IllegalArgumentException("We don't have a breakdown numbered " + i + ".");
        return pointers[i].getDescendent(l);
    }

    // return true if pointers[i] has a descendent that contains l
    public boolean precedesLength(int i, BasicEdgeLength l) {
        if (i < 0 || i >= pointers.length) throw new IllegalArgumentException("We don't have a breakdown numbered " + i + ".");
        for (TreeNode<BasicEdgeLength> n : pointers[i].getNext()) {
            if (l.equals(n.getData())) return true;
        }
        return false;
    }

    // set pointers[i] to the given TreeNode
    public void setPointer(int i, TreeNode<BasicEdgeLength> n) {
        pointers[i] = n;
    }

    // set pointers[i] to the descendent of pointers[i] that contains l
    public void place(int i, BasicEdgeLength l) {
        if (i < 0 || i >= pointers.length) throw new IllegalArgumentException("We don't have a breakdown numbered " + i + ".");
        for (TreeNode<BasicEdgeLength> n : pointers[i].getNext()) {
            if (l.equals(n.getData())) {
                pointers[i] = n;
                return;
            }
        }
        throw new IllegalArgumentException(l + " doesn't follow " + pointers[i].getData() + ".");
    }

    // set pointers[i] to the parent of pointers[i]
    public void remove(int i) {
        if (i < 0 || i >= pointers.length) throw new IllegalArgumentException("We don't have a breakdown numbered " + i + ".");
        pointers[i] = pointers[i].getPrevious();
    }

    // output a String
    public String toString() {
        String output = "";
        for (int i = 0; i < breakdowns.length; i++) {
            int level = 0;
            output += "Edge " + i + ":\n" + breakdowns[i] + "\n";
        }
        return output;
    }

    // output a String of chains
    public String chainString() {
        String output = "";
        for (int i = 0; i < breakdowns.length; i++) {
            output += "Edge " + i + ":\n" + breakdowns[i].chainString() + "\n";
        }
        return output;
    }

    // equals method.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        EdgeBreakdownTree l = (EdgeBreakdownTree) obj;
        if (this.breakdowns.length!=l.breakdowns.length) return false;
        for (int i = 0; i < breakdowns.length; i++) {
            if (!this.breakdowns[i].equals(l.breakdowns[i])) return false;
        }
        return true;
    }

    // hashCode override.
    public int hashCode() {
        int prime = 7;
        int result = 13;
        for (int i = 0; i < breakdowns.length; i++)
            result = prime*result + breakdowns[i].hashCode();
        return result;
    }

    // test client
    public static void main(String[] args) {

        System.out.println(FULL_BREAKDOWNS.toString());

    }

} // end of class EdgeBreakdownTree
