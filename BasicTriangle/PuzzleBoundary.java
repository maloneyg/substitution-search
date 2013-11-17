
/*************************************************************************
 *  Compilation:  javac PuzzleBoundary.java
 *  Execution:    java PuzzleBoundary
 *
 *  A class representing a partition of objects of type E.
 *  It is assumed to contain no duplicate objects, although
 *  it has no way to guarantee this. 
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.io.Serializable;

class PointAndLength implements Comparable<PointAndLength>, Serializable {

    // NOTE: This class has a natural ordering that is
    // incompatible with equals.
    // Confusing the matter further is the fact that
    // two PointAndLength objects are only `locally comparable'
    // --it doesn't make sense to compare them if they come 
    // from different lists.

    // this class holds a single BytePoint and a magnitude.
    // the magnitude represents a distance from some other BytePoint.
    // its purpose is to sort the BytePoints by this magnitude.
    private final BytePoint p;
    private final double d;

    // private constructor
    protected PointAndLength(BytePoint p, double d) {
        this.p = p;
        this.d = d;
    }

    // public static factory method 
    public static PointAndLength createPointAndLength(BytePoint p, double d) {
        return new PointAndLength(p,d);
    }

    // get the data
    public BytePoint getP() {
        return p;
    }

    // output a String
    public String toString() {
        return p.toString();
    }

    // compare on the basis of the double d
    public int compareTo(PointAndLength other) {
        if (this.d < other.d) return -1;
        if (this.d > other.d) return 1;
        return 0;
    }

} // end of class PointAndLength

public class PuzzleBoundary implements Serializable {

    // lists of possible positions of points on the three edges
    private static final BytePoint[] E0;
    private static final BytePoint[] E1;
    private static final BytePoint[] E2;

    static { // figure out where the points can go on the edges

        int tileNum = Preinitializer.MY_TILE;
        BasicTriangle placed = BasicPrototile.ALL_PROTOTILES.get(tileNum).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false);
        BasicAngle[] angles = placed.getAngles();
        BytePoint[] vertices = placed.getVertices();
        for (int i = 0; i < 3; i++) vertices[i] = vertices[i].inflate();

        // in order to avoid a compile-time error, I can't assign
        // E0, E1, and E2 inside a loop
        // so I assign temps instead, and assign E0, E1, E2 to them later
        BytePoint[] preE0 = new BytePoint[1];
        BytePoint[] preE1 = new BytePoint[1];
        BytePoint[] preE2 = new BytePoint[1];

        // loop through the three edges
        for (int i = 0; i < 3; i++) {
            ImmutableList<Integer> lengthList = Initializer.INFLATED_LENGTHS.getColumn(Initializer.acute(angles[i].getAsInt())-1);
            // this is what we iterate.
            // use it as a vector of scalar multiples for lengthList
            byte[] lengthCount = new byte[lengthList.size()];
            // compare with this to see if we're back at the start
            byte[] allZeroes = new byte[lengthList.size()];
            // this will become E0, E1, or E2, after sorting and getting points
            List<PointAndLength> preE = new ArrayList<>();
            // the angle by which we rotate to align with each edge
            BasicAngle rot = (i==0)? BasicAngle.createBasicAngle(0) : ((i==1)? angles[2].supplement() : angles[1].piPlus());
            // get the length vectors and rotate them all
            BytePoint[] diagonals = new BytePoint[BasicEdgeLength.ALL_EDGE_LENGTHS.size()];
            for (int j = 0; j < diagonals.length; j++) 
                diagonals[j] = BasicEdgeLength.ALL_EDGE_LENGTHS.get(j).getAsVector(rot);

            // fill up the list of edge points
            do {
                BytePoint nextPoint = combination(lengthCount,diagonals);
                preE.add(PointAndLength.createPointAndLength(vertices[(i+1)%3].add(nextPoint),nextPoint.dotProduct(nextPoint)));
                odometerIncrement(lengthCount,lengthList);
            } while (!Arrays.equals(lengthCount,allZeroes));
            // totally order the edge points
            Collections.sort(preE);
            // make an array for E0, E1, E2
            BytePoint[] almostE = new BytePoint[preE.size()];
            for (int j = 0; j < almostE.length; j++) almostE[j] = preE.get(j).getP();
            if (i==0) {
                preE0 = almostE;
            } else if (i==1) {
                preE1 = almostE;
            } else {
                preE2 = almostE;
            }
        }

        // now assign values to E0, E1, E2
        E0 = preE0;
        E1 = preE1;
        E2 = preE2;

    } // static initialization ends here

    // private constructor
    private PuzzleBoundary() {

    }

    // increment an array of ints, wrapping around if
    // the ith entry exceeds the ith entry of maxList
    public static void odometerIncrement(byte[] digits, ImmutableList<Integer> maxList) {
        for (int i = 0; i < digits.length; i++) {
            digits[i]++;
            if ((int) digits[i] > maxList.get(i)) {
                digits[i] = (byte) 0;
            } else {
                break;
            }
        }
    }

    // add together scalar multiples of points
    private static BytePoint combination(byte[] scalars, BytePoint[] points) {
        BytePoint output = BytePoint.ZERO_VECTOR;
        for (int i = 0; i < scalars.length; i++) {
            output = output.add(points[i].scalarMultiple(scalars[i]));
        }
        return output;
    }

    // output a String
    public String toString() {
        String output = "Edge 0:\n";
        for (int i = 0; i < E0.length; i++) output += E0[i] + "\n";
        output += "Edge 1:\n";
        for (int i = 0; i < E1.length; i++) output += E1[i] + "\n";
        output += "Edge 2:\n";
        for (int i = 0; i < E2.length; i++) output += E2[i] + "\n";
        return output;
    }

    // equals method.
    // currently broken
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        PuzzleBoundary l = (PuzzleBoundary) obj;
        return true;
    }

    // hashCode override.
    // currently broken
    public int hashCode() {
        int prime = 59;
        int result = 19;
        return result;
    }


    // test client
    public static void main(String[] args) {

        PuzzleBoundary test = new PuzzleBoundary();
        System.out.println(test);
        System.out.println(E0.length);
        System.out.println(E1.length);
        System.out.println(E2.length);
//        Integer i0 = 2;
//        Integer i1 = 3;
//        Integer i2 = 3;
//        ImmutableList<Integer> maxes = ImmutableList.of(i0,i1,i2);
//        byte[] start = new byte[maxes.size()];
//        byte[] count = new byte[maxes.size()];
//        do {
//            for (int i = 0; i < count.length; i++) System.out.print(count[i] + " ");
//            System.out.println();
//            odometerIncrement(count,maxes);
//        } while (!Arrays.equals(count,start));

    }

} // end of class PuzzleBoundary
