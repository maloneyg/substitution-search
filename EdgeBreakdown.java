
/*************************************************************************
 *  Compilation:  javac EdgeBreakdown.java
 *  Execution:    java EdgeBreakdown
 *
 *  A class representing all edge breakdowns that have been found so far.
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.io.Serializable;

public class EdgeBreakdown implements Serializable {

    private MultiTree<BasicEdgeLength>[] breakdowns;

    // private constructor
    private EdgeBreakdown() {
        breakdowns = new MultiTree[Preinitializer.N/2];
        for (int i = 0; i < breakdowns.length; i++) {
            breakdowns[i] = new MultiTree<BasicEdgeLength>();
        }
    }

    // add a new edge breakdown for length i
    public void addBreakdown(int i, Iterable<BasicEdgeLength> breakdown) {
        breakdowns[i].addChain(breakdown);
    }

    // add a new edge breakdown for length i
    public void addBreakdown(int i, ImmutableList<Integer> breakdown) {
        ArrayList<BasicEdgeLength> forward = new ArrayList<>(breakdown.size());
        ArrayList<BasicEdgeLength> reverse = new ArrayList<>(breakdown.size());
        for (int j = 0; j < forward.size(); j++) {
            forward.add(BasicEdgeLength.createBasicEdgeLength(breakdown.get(j)));
        }
        for (int j = 0; j < forward.size(); j++) {
            reverse.add(forward.get(forward.size()-j-1));
        }
        addBreakdown(i,forward);
        addBreakdown(i,reverse);
    }

    // output a String
    public String toString() {
        String output = "Boilerplate ToString method for EdgeBreakdown";
        return output + "\n";
    }

    // equals method.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        EdgeBreakdown l = (EdgeBreakdown) obj;
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

} // end of class EdgeBreakdown
