
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
    private TreeNode<BasicEdgeLength> pointer0;
    private TreeNode<BasicEdgeLength> pointer1;
    private TreeNode<BasicEdgeLength> pointer2;

    // private constructor
    private EdgeBreakdownTree() {
        breakdowns = new MultiTree[Preinitializer.N/2];
        for (int i = 0; i < breakdowns.length; i++) {
            breakdowns[i] = new MultiTree<BasicEdgeLength>();
        }
        pointer0 = breakdowns[0].getHead();
        pointer1 = breakdowns[0].getHead();
        pointer2 = breakdowns[0].getHead();
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
        pointer0 = breakdowns[0].getHead();
        pointer1 = breakdowns[1].getHead();
        pointer2 = breakdowns[2].getHead();
    }

    // public static factory method
    public static EdgeBreakdownTree createEdgeBreakdownTree() {
        return new EdgeBreakdownTree();
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

    }

} // end of class EdgeBreakdownTree
