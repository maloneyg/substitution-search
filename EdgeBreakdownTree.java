
/*************************************************************************
 *  Compilation:  javac EdgeBreakdownTree.java
 *  Execution:    java EdgeBreakdownTree
 *
 *  A class representing all edge breakdowns that have been found so far.
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;
import java.io.Serializable;

public class EdgeBreakdownTree implements Serializable {

    private MultiTree<BasicEdgeLength>[] breakdowns;

    // private constructor
    private EdgeBreakdownTree() {
        breakdowns = new MultiTree[Preinitializer.N/2];
        for (int i = 0; i < breakdowns.length; i++) {
            breakdowns[i] = new MultiTree<BasicEdgeLength>();
        }
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
