
/*************************************************************************
 *  Compilation:  javac MutablePartition.java
 *  Execution:    java MutablePartition
 *
 *  A class representing a partition of objects of type E.
 *  It is assumed to contain no duplicate objects, although
 *  it has no way to guarantee this. 
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import java.io.Serializable;
import org.jgrapht.graph.*;
import org.jgrapht.graph.SimpleGraph;
import java.util.List;

// an unordered pair of edge lengths.
class DoubleEdgeLength implements Serializable {

    private final BasicEdgeLength e1;
    private final BasicEdgeLength e2;

    // private constructor
    private DoubleEdgeLength(BasicEdgeLength e1,BasicEdgeLength e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    // public static factory method
    public static DoubleEdgeLength createDoubleEdgeLength(BasicEdgeLength e1,BasicEdgeLength e2) {
        return new DoubleEdgeLength(e1,e2);
    }

    // return true if this contains the edge length e
    public boolean contains(BasicEdgeLength e) {
        return (e1.equals(e)||e2.equals(e));
    }

    // equals method
    // returns true for any DoubleEdgeLength having the same two
    // edge lengths, in any order
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        DoubleEdgeLength t = (DoubleEdgeLength) obj;
        return ((this.e1.equals(t.e1)&&this.e2.equals(t.e2))||(this.e1.equals(t.e2)&&this.e2.equals(t.e1)));
    }

    // hashCode method
    public int hashCode() {
        int prime = 13;
        int result = 29;
        int h = e1.hashCode()-e2.hashCode();
        result = prime*result + e1.hashCode() + e2.hashCode();
        result = prime*result + ((h>0)? h : 0-h);
        return result;
    }

    // output a String
    public String toString() {
        return "DoubleEdgeLength:\n" + e1.toString() + "\n" + e2.toString();
    }

} // end of class PartitionNode

public class EdgeBreakdownGraph implements Serializable {

    private SimpleGraph<List<Integer>,DoubleEdgeLength> G = new SimpleGraph(DoubleEdgeLength.class);
    private List<List<Integer>>[] sorted = (List<List<Integer>>[]) new ArrayList[BasicEdgeLength.ALL_EDGE_LENGTHS.size()];

    // private constructor
    private EdgeBreakdownGraph() {
    }

    // public static factory method
    public static EdgeBreakdownGraph createEdgeBreakdownGraph() {
        return new EdgeBreakdownGraph();
    }


    // test client
    public static void main(String[] args) {

        String s1 = "good";
        String s2 = "bad";
        String s3 = "ugly";
        String s4 = "evil";
        String s5 = "great";

    }

} // end of class EdgeBreakdownGraph
