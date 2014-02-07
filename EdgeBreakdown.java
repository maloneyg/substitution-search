
/*************************************************************************
 *  Compilation:  javac EdgeBreakdown.java
 *  Execution:    java EdgeBreakdown
 *
 *  A class representing an edge breakdown, complete with Orientations
 *
 *************************************************************************/

import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class EdgeBreakdown implements Serializable {

    static final long serialVersionUID = 2939710449067677120L;

    private BasicEdgeLength[] lengths;
    private Orientation[] orientations;

    // private constructor
    private EdgeBreakdown(BasicEdgeLength[] l, Orientation[] o) {
        if (l.length!=o.length) throw new IllegalArgumentException("Trying to construct an EdgeBreakdown with " + l.length + " edge lengths and " + o.length + " orientations.");
        lengths = l;
        orientations = o;
    }

    private EdgeBreakdown(List<BasicEdgeLength> l, List<Orientation> o) {
        if (l.size()!=o.size()) throw new IllegalArgumentException("Trying to construct an EdgeBreakdown with " + l.size() + " edge lengths and " + o.size() + " orientations.");
        lengths = new BasicEdgeLength[l.size()];
        orientations = new Orientation [o.size()];
        for (int i = 0; i < lengths.length; i++) lengths[i] = l.get(i);
        for (int i = 0; i < lengths.length; i++) orientations[i] = o.get(i);
    }

    // public static factory method
    public static EdgeBreakdown createEdgeBreakdown(BasicEdgeLength[] l, Orientation[] o) {
        return new EdgeBreakdown(l,o);
    }

    public static EdgeBreakdown createEdgeBreakdown(List<BasicEdgeLength> l, List<Orientation> o) {
        return new EdgeBreakdown(l,o);
    }

    // reverse the order of lengths and orientations
    // change orientations to their opposites
    public EdgeBreakdown reverse() {
        BasicEdgeLength[] l = new BasicEdgeLength[lengths.length];
        Orientation[] o = new Orientation[orientations.length];
        for (int i = 0; i < l.length; i++) l[i] = lengths[l.length-i-1];
        for (int i = 0; i < o.length; i++) o[i] = orientations[o.length-i-1].getOpposite();
        return new EdgeBreakdown(l,o);
    }

    // getters
    public BasicEdgeLength[] getLengths() {
        return lengths;
    }

    public Orientation[] getOrientations() {
        return orientations;
    }

    public int size() {
        return lengths.length;
    }

    // output a String
    public String toString() {
        String output = "";
        for (int i = 0; i < lengths.length; i++) {
            output += lengths[i] + " " + orientations[i] + "\n";
        }
        return output;
    }

    // equals method.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        EdgeBreakdown l = (EdgeBreakdown) obj;
        if (this.lengths.length!=l.lengths.length) return false;
        for (int i = 0; i < this.lengths.length; i++) {
            if (!this.lengths[i].equals(l.lengths[i])) return false;
            if (!this.orientations[i].equals(l.orientations[i])) return false;
        }
        return true;
    }

    // return true if these two breakdowns have the same length sequences
    public boolean compatible(EdgeBreakdown l) {
        if (this.lengths.length!=l.lengths.length) return false;
        for (int i = 0; i < this.lengths.length; i++) {
            if (!this.lengths[i].equals(l.lengths[i])) return false;
        }
        return true;
    }

    // hashCode method.
    public int hashCode() {
        int prime = 97;
        int result = 19;
        for (int i = 0; i < lengths.length; i++) {
            result = prime*result + lengths[i].hashCode();
            result = prime*result + orientations[i].hashCode();
        }
        return result;
    }

    // test client
    public static void main(String[] args) {

    }

} // end of class EdgeBreakdown
