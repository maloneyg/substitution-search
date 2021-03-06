
/*************************************************************************
 *  Compilation:  javac PuzzleBoundary.java
 *  Execution:    java PuzzleBoundary
 *
 *  A class representing the boundary of an inflated triangle.
 *  It contains static arrays with all the possible points on the 
 *  edges where a tile might touch it.
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.io.Serializable;
import java.io.*;
import java.util.*;

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

    // vertices of the big triangle
    private static final BytePoint[] VERTICES;

    // direction vectors of the edges of the big triangle
    private static final BytePoint VECTOR0;
    private static final BytePoint VECTOR1;
    private static final BytePoint VECTOR2;

    // angles that the edges of the big triangle make with the positive x-axis
    private static final BasicAngle[] ANGLES;

    // minimum allowable distance from a point to an edge
    public static final double TOO_CLOSE = BasicEdge.TOO_CLOSE;
    // we need a different number for edge 1
    // edge 1 is special because the other two edges pass through
    // the origin, whereas edge 1 doesn't
    public static final double EDGE_ONE_TOO_CLOSE;

    // the edge breakdowns we're using in this search
    // for annoying reasons having to do with initialization, I can't make 
    // it final
    public static EdgeBreakdownTree BREAKDOWNS;

    // the instance variables appear here

    // the blocks keep track of which points on the 
    // boundaries have been covered by tile edges
    private boolean[] block0;
    private boolean[] block1;
    private boolean[] block2;

    // the blocks keep track of which points on the 
    private EdgeBreakdownTree breakdown;

    // lists of triangle edges that have been placed
    // along the boundaries
    private Stack<BasicEdge> placed0 = new Stack<>();
    private Stack<BasicEdge> placed1 = new Stack<>();
    private Stack<BasicEdge> placed2 = new Stack<>();

    // BytePoints indicating how far we've covered along each edge
    // the main purpose is to make sure we don't add things to
    // the end instead of the beginning
    private BytePoint frontier0;
    private BytePoint frontier1;
    private BytePoint frontier2;

    static { // load the previous edge breakdown for use here

            // deserialize data
            String filename = Preinitializer.BREAKDOWN_INPUT_FILENAME;
            if ( ! new File(filename).isFile() )
                {
                    BREAKDOWNS = EdgeBreakdownTree.FULL_BREAKDOWNS;
                }
            else
                {
                try
                    {
                        FileInputStream fileIn = new FileInputStream(filename);
                        ObjectInputStream in = new ObjectInputStream(fileIn);
                        BREAKDOWNS = (EdgeBreakdownTree)in.readObject();
                        System.out.println("Edge breakdowns have been read.");
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                        BREAKDOWNS = null;
                        System.exit(1);
                    }
                }

    } // finished loading previous edge breakdowns

    static { // figure out where the points can go on the edges

        // figure out which tile we're searching by looking in Preinitializer
        // if SEARCH_TILE is null, we're searching a prototile
        // otherwise we're searching SEARCH_TILE
        int tileNum = Preinitializer.MY_TILE;
        List<Integer> search = Preinitializer.SEARCH_TILE;

        BasicTriangle placed = BasicPrototile.ALL_PROTOTILES.get(tileNum).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false);

        BasicAngle[] angles = (search == null) ? placed.getAngles() : new BasicAngle[] {BasicAngle.createBasicAngle(search.get(0)),BasicAngle.createBasicAngle(search.get(1)),BasicAngle.createBasicAngle(search.get(2))};
        BytePoint[] preVertices = (search == null) ? placed.getVertices() : new BytePoint[] {//
            BasicEdgeLength.lengthOpposite(angles[2]).getAsVector(angles[1]),//
            BytePoint.ZERO_VECTOR,//
            BasicEdgeLength.lengthOpposite(angles[0]).getAsVector(BasicAngle.createBasicAngle(0))//
            };

        BytePoint[] vertices = new BytePoint[3];
        for (int i = 0; i < 3; i++) vertices[i] = preVertices[i].inflate();
        VERTICES = vertices;

        BasicAngle[] preAngles = new BasicAngle[3];
        BasicEdge[] tempEdges = placed.getEdges();
        if (search == null) {
            for (int i = 0; i < 3; i++) preAngles[i] = tempEdges[i].angle().piPlus();
        } else {
            preAngles[0] = BasicAngle.createBasicAngle(0);
            preAngles[1] = BasicAngle.createBasicAngle(search.get(2)).supplement();
            preAngles[2] = BasicAngle.createBasicAngle(search.get(1));
        }
        ANGLES = preAngles;

        // in order to avoid a compile-time error, I can't assign
        // E0, E1, E2, VECTOR0, VECTOR1, and VECTOR2 inside a loop
        // so I assign temps instead, and assign these constants to them later
        BytePoint[] preE0 = new BytePoint[1];
        BytePoint[] preE1 = new BytePoint[1];
        BytePoint[] preE2 = new BytePoint[1];
        BytePoint preVECTOR0 = BytePoint.ZERO_VECTOR;
        BytePoint preVECTOR1 = BytePoint.ZERO_VECTOR;
        BytePoint preVECTOR2 = BytePoint.ZERO_VECTOR;

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
            BytePoint unit = BasicEdge.UNIT_LENGTH.getAsVector(rot);

            // assign temps for VECTOR0, VECTOR1, VECTOR2
            if (i==0) {
                preVECTOR0 = unit;
            } else if (i==1) {
                preVECTOR1 = unit;
            } else {
                preVECTOR2 = unit;
            }

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

            // assign temps for E0, E1, E2
            if (i==0) {
                preE0 = almostE;
            } else if (i==1) {
                preE1 = almostE;
            } else {
                preE2 = almostE;
            }
        }

        // now assign values to E0, E1, E2, VECTOR0, VECTOR1, VECTOR2
        // Take that, java compiler! I can assign values in a loop after all!
        E0 = preE0;
        E1 = preE1;
        E2 = preE2;
        VECTOR0 = preVECTOR0;
        VECTOR1 = preVECTOR1;
        VECTOR2 = preVECTOR2;
        // compute EDGE_ONE_TOO_CLOSE
        EDGE_ONE_TOO_CLOSE = VECTOR1.crossProduct(VERTICES[0]) + TOO_CLOSE;

    } // static initialization ends here

    // private constructor
    private PuzzleBoundary() {
        block0 = new boolean[E0.length];
        block1 = new boolean[E1.length];
        block2 = new boolean[E2.length];
        breakdown = null;//EdgeBreakdownTree.createEdgeBreakdownTree(BREAKDOWNS);
        frontier0 = E0[E0.length-1];
        frontier1 = E1[E1.length-1];
        frontier2 = E2[E2.length-1];
    }

    // private constructor
    private PuzzleBoundary(BasicEdge e) {
        block0 = new boolean[E0.length];
        block1 = new boolean[E1.length];
        block2 = new boolean[E2.length];
        //add(e);
        breakdown = null;//EdgeBreakdownTree.createEdgeBreakdownTree(BREAKDOWNS,e.getLength());
        frontier0 = E0[E0.length-1];
        frontier1 = E1[E1.length-1];
        frontier2 = E2[E2.length-1];
    }

    // private constructor
    private PuzzleBoundary(boolean[] b0,boolean[] b1,boolean[] b2,Stack<BasicEdge> e0,Stack<BasicEdge> e1,Stack<BasicEdge> e2,EdgeBreakdownTree t,BytePoint f0,BytePoint f1,BytePoint f2) {
        block0 = b0;
        block1 = b1;
        block2 = b2;
        placed0 = e0;
        placed1 = e1;
        placed2 = e2;
        breakdown = t;
        frontier0 = f0;
        frontier1 = f1;
        frontier2 = f2;
    }

    // public static factory method
    public static PuzzleBoundary createPuzzleBoundary() {
        return new PuzzleBoundary();
    }

    // public static factory method
    public static PuzzleBoundary createPuzzleBoundary(BasicEdge e) {
        return new PuzzleBoundary(e);
    }

    // deep copy
    public PuzzleBoundary deepCopy() {
        boolean[] b0 = new boolean[block0.length];
        boolean[] b1 = new boolean[block1.length];
        boolean[] b2 = new boolean[block2.length];
        for (int i = 0; i < b0.length; i++) b0[i] = block0[i];
        for (int i = 0; i < b1.length; i++) b1[i] = block1[i];
        for (int i = 0; i < b2.length; i++) b2[i] = block2[i];
        Stack<BasicEdge> e0 = new Stack<>();
        Stack<BasicEdge> e1 = new Stack<>();
        Stack<BasicEdge> e2 = new Stack<>();
        for (int i = 0; i < placed0.size(); i++) e0.push(placed0.get(i));
        for (int i = 0; i < placed1.size(); i++) e1.push(placed1.get(i));
        for (int i = 0; i < placed2.size(); i++) e2.push(placed2.get(i));
        EdgeBreakdownTree t = (breakdown == null) ? null : breakdown.deepCopy();
        return new PuzzleBoundary(b0,b1,b2,e0,e1,e2,t,frontier0,frontier1,frontier2);
    }

    // increment an array of bytes, wrapping around if
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

    // return true if the given point is outside of the inflated
    // triangle, or inside, but too close to the edge
    // this will return true for points that are on the edge, so
    // we need to check for incidence separately
    public static boolean overTheEdge(BytePoint p) {
        return (VECTOR0.crossProduct(p) < TOO_CLOSE || VECTOR2.crossProduct(p) < TOO_CLOSE || VECTOR1.crossProduct(p) < EDGE_ONE_TOO_CLOSE);
    }

    // a triple-valued function
    // 0 if p is not on the edge at all
    //  1 if p is on the edge, at a position that isn't covered
    // -1 if p is on the edge, at a position that is covered
    public int incident(BytePoint p) {
        for (int i = 0; i < E0.length; i++) {
            if (p.equals(E0[i])) {
                if (block0[i]) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
        for (int i = 0; i < E1.length; i++) {
            if (p.equals(E1[i])) {
                if (block1[i]) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
        for (int i = 0; i < E2.length; i++) {
            if (p.equals(E2[i])) {
                if (block2[i]) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
        return 0;
    }

    // a triple-valued function
    // 0 if e is not on the edge at all
    //  1 if e is on the edge, and not already covered
    // -1 if e is on the edge, and already covered
    // we assume that the edge is actually inside the
    // triangle or on its boundary; weird things happen
    // otherwise
    public int incident(BasicEdge e) {
        BytePoint[] ends = e.getEnds();
        boolean hit = false;
        for (int i = 0; i < E0.length; i++) {
            if (ends[0].equals(E0[i])&&i!=E0.length-1) {
                hit = true;
            }
            if (hit) {
                if (block0[i]) return -1;
                if (ends[1].equals(E0[i])) {
                    if (breakdown!=null&&ends[1].equals(frontier0)&&(!breakdown.precedesLength(0,e.getLength()))) return -1;
                    return 1;
                }
            }
        }
        hit = false;
        for (int i = 0; i < E1.length; i++) {
            if (ends[0].equals(E1[i])&&i!=E1.length-1) {
                hit = true;
            }
            if (hit) {
                if (block1[i]) return -1;
                if (ends[1].equals(E1[i])) {
                    if (breakdown!=null&&ends[1].equals(frontier1)&&(!breakdown.precedesLength(1,e.getLength()))) return -1;
                    return 1;
                }
            }
        }
        hit = false;
        for (int i = 0; i < E2.length; i++) {
            if (ends[0].equals(E2[i])&&i!=E2.length-1) {
                hit = true;
            }
            if (hit) {
                if (block2[i]) return -1;
                if (ends[1].equals(E2[i])) {
                    if (breakdown!=null&&ends[1].equals(frontier2)&&(!breakdown.precedesLength(2,e.getLength()))) return -1;
                    return 1;
                }
            }
        }
        return 0;
    }

    // flip all the blocks on this boundary between
    // the beginning and end of the given edge
    // return true if any flips were made
    private boolean flip(BytePoint[] boundary, boolean[] blocks, BasicEdge e) {
        BytePoint[] ends = e.getEnds();
        int start = -1;
        int end = -1;
        for (int i = 0; i < boundary.length; i++) {
            if (ends[0].equals(boundary[i])) start = i;
            if (start > -1 && ends[1].equals(boundary[i])) end = i;
        }
        if (start > -1 && end > -1) {
            for (int i = start + 1; i < end; i++) blocks[i] = !blocks[i];
            return true;
        }
        return false;
    }

    // add this edge to the appropriate list if it's incident
    public void add(BasicEdge e) {
        if (flip(E0,block0,e)) {
            placed0.push(e);
            if (e.getEnds()[1].equals(frontier0)) {
                if (!(breakdown==null)) breakdown.place(0,e.getLength());
                frontier0 = e.getEnds()[0];
            }
            return;
        }
        if (flip(E1,block1,e)) {
            placed1.push(e);
            if (e.getEnds()[1].equals(frontier1)) {
                if (!(breakdown==null)) breakdown.place(1,e.getLength());
                frontier1 = e.getEnds()[0];
            }
            return;
        }
        if (flip(E2,block2,e)) {
            placed2.push(e);
            if (e.getEnds()[1].equals(frontier2)) {
                if (!(breakdown==null)) breakdown.place(2,e.getLength());
                frontier2 = e.getEnds()[0];
            }
            return;
        }
    }

    // remove this edge from the appropriate list if it's incident
    public void remove(BasicEdge e) {
        if (flip(E0,block0,e)) {
            placed0.pop();
            if (e.getEnds()[0].equals(frontier0)) {
                if (!(breakdown==null)) breakdown.remove(0);
                frontier0 = e.getEnds()[1];
            }
            return;
        }
        if (flip(E1,block1,e)) {
            placed1.pop();
            if (e.getEnds()[0].equals(frontier1)) {
                if (!(breakdown==null)) breakdown.remove(1);
                frontier1 = e.getEnds()[1];
            }
            return;
        }
        if (flip(E2,block2,e)) {
            placed2.pop();
            if (e.getEnds()[0].equals(frontier2)) {
                if (!(breakdown==null)) breakdown.remove(2);
                frontier2 = e.getEnds()[1];
            }
            return;
        }
    }

    // get an EdgeBreakdown
    // the int i should be 0, 1, or 2 depending on which edge we want
    public EdgeBreakdown getBreakdown(int i) {
        if (i < 0||i > 2) throw new IllegalArgumentException("Can't get breakdown number " + i + ".");
        Stack<BasicEdge> E = (i==0)? placed0 : ((i==1)? placed1 : placed2);
        BytePoint lastVertex = VERTICES[(i+1)%3];
        boolean notDone = false;
        List<BasicEdgeLength> l = new ArrayList<>();
        List<Orientation> o = new ArrayList<>();
        do {
            notDone = false;
            for (BasicEdge e : E) {
                if (e.getEnds()[0].equals(lastVertex)) {
                    l.add(e.getLength());
                    o.add(e.getOrientation()); // I hope this is the right way
                    lastVertex = e.getEnds()[1];
                    notDone = true;
                    break;
                }
            }
        } while (notDone);
        return EdgeBreakdown.createEdgeBreakdown(l,o);
    }

    // get an edge breakdown (ImmutableList<Integer>)
    // the int i should be 0, 1, or 2 depending on which edge we want
    public BytePoint[] getVertices() {
        return VERTICES;
    }

    // somewhat complicated.  
    // find the boundary edge with which this edge is incident
    // at its base (first vertex). 
    // then return the angle that this boundary edge makes with
    // the positive x-axis.
    public BasicAngle incidenceAngle(BasicEdge e) {
        BytePoint base = e.getEnds()[0];
        for (int i = 0; i < E0.length; i++) {
            if (base.equals(E0[i])) return ANGLES[0];
        }
        for (int i = 0; i < E1.length; i++) {
            if (base.equals(E1[i])) return ANGLES[1];
        }
        for (int i = 0; i < E2.length; i++) {
            if (base.equals(E2[i])) return ANGLES[2];
        }
        throw new IllegalArgumentException(e + " is not incident with the puzzle boundary " + this);
    }

    // output a String
    public String toString() {
        String output = "Edge 0:\n";
        for (int i = 0; i < E0.length; i++) output += E0[i] + " " + block0[i] + "\n";
        output += "Edge 1:\n";
        for (int i = 0; i < E1.length; i++) output += E1[i] + " " + block1[i] + "\n";
        output += "Edge 2:\n";
        for (int i = 0; i < E2.length; i++) output += E2[i] + " " + block2[i] + "\n";
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
        System.out.println(VECTOR0.crossProduct(VERTICES[0]));
        System.out.println(VECTOR1.crossProduct(VERTICES[1]));
        System.out.println(VECTOR2.crossProduct(VERTICES[2]));
        System.out.println("crossProduct of edge 1.");
        System.out.println(VECTOR1.crossProduct(VERTICES[0]));
        System.out.println(VECTOR1.crossProduct(VERTICES[2]));

        boolean[] testbool = new boolean[3];
        for (int i = 0; i < 3; i++) System.out.println(testbool[i]);
        System.out.println("ANGLES:");
        for (int i = 0; i < 3; i++) System.out.println(ANGLES[i]);
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
