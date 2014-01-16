/**
*    This class implements a triangle.
*    Let's say right now that every triangle has its vertices, etc. 
*    listed in counterclockwise order.
*    The triangle is located somewhere in space. 
*    It has three angles, three vertices, and 
*    three edge orientations.  
*    Oh yeah, it also has edge lengths.  
*    Every triangle has a type.  Are you sure about that?
*/

import com.google.common.collect.*;
import com.google.common.cache.*;
import java.io.Serializable;
import org.apache.commons.math3.linear.*;
import java.util.*;
import java.util.Arrays;
import java.util.concurrent.*;

public final class BasicTriangle implements AbstractTriangle<BasicAngle, BytePoint, BasicEdgeLength, BasicEdge, BasicTriangle>, Serializable {

    // make it Serializable
    static final long serialVersionUID = 2896067610461604622L;

    private final BasicAngle[] angles;
    private final BytePoint[] vertices;
    private final Orientation[] orientations;
    private final BasicEdgeLength[] edgeLengths;

    // the prototile of which this is an instance
    private final BasicPrototile prototile;
    // is it reflected or not?
    private final boolean flip;

    // helper fields for the contains(BytePoint) method
    // direction vectors for the edges
    private final BytePoint[] directions;

    // constructor methods.
    private BasicTriangle(BasicAngle[] a, BytePoint[] p, Orientation[] o, BasicEdgeLength[] e, BasicPrototile P, boolean f) {
        angles = a;
        BytePoint[] tempVertices = new BytePoint[p.length];
        for (int i = 0; i < p.length; i++) tempVertices[i] = p[i];
        vertices = tempVertices;
        orientations = o;
        edgeLengths = e;
        prototile = P;
        flip = f;
        directions = new BytePoint[] { //
                        BytePoint.unitize(e[2],p[1].subtract(p[0])),//
                        BytePoint.unitize(e[0],p[2].subtract(p[1])),//
                        BytePoint.unitize(e[1],p[0].subtract(p[2])) //
                                     };
    }

    // public static factory methods.
    public static BasicTriangle createBasicTriangle(BasicAngle[] a, BytePoint[] p, Orientation[] o, BasicEdgeLength[] e, BasicPrototile P, boolean f) {
        return new BasicTriangle(a,p,o,e,P,f);
    }

    public BasicPrototile getPrototile() {
        return prototile;
    }

    public boolean getFlip() {
        return flip;
    }

    // unsafe: passes final variable to outside world
    public BasicAngle[] getAngles() {
        return angles;
    }

    // unsafe: passes final variable to outside world
    public BytePoint[] getVertices() {
        return vertices;
    }

    // unsafe: passes final variable to outside world
    public Orientation[] getOrientations() {
        return orientations;
    }

    // Don't call this method too often; it's time-consuming.
    public BasicEdge[] getEdges() {
        BytePoint[] vertexPair0 = {vertices[0],vertices[1]};
        BytePoint[] vertexPair1 = {vertices[1],vertices[2]};
        BytePoint[] vertexPair2 = {vertices[2],vertices[0]};
        BasicEdge e2 = BasicEdge.createBasicEdge(edgeLengths[2],orientations[2],vertexPair0);
        BasicEdge e0 = BasicEdge.createBasicEdge(edgeLengths[0],orientations[0],vertexPair1);
        BasicEdge e1 = BasicEdge.createBasicEdge(edgeLengths[1],orientations[1],vertexPair2);
        BasicEdge[] output = {e0,e1,e2};
        return output;
    }

    // Given two points on the triangle, return the third.  
    public BytePoint getOtherVertex(BytePoint vertex1, BytePoint vertex2) {

        // First make sure the input points are on the triangle.  
        if (!(incidentPoint(vertex1) && incidentPoint(vertex2))) {
            throw new IllegalArgumentException("One of the input points does not lie on the triangle.");
        }

        // Now return the vertex that is different from both of them.  
        for (int i = 0; i < 3; i++) {
            if (!(vertices[i].equals(vertex1) || vertices[i].equals(vertex2))) {
                return vertices[i];
            }
        }

        // If we get this far, that means the triangle only has two vertices.  
        throw new IllegalStateException("There is a triangle with only two vertices.");

    }

    /** 
    * Return the index of a point on the triangle.  
    * If the point is not on the triangle, return -1.
    * Purely for internal use; never make this public.
    */
    private int indexOf(BytePoint point) {
        for (int i = 0; i < 3; i++) {
            if (vertices[i].equals(point))
                return i;
        }
        return -1;
    }

    // Tests for incidence.  
    public boolean incidentPoint(BytePoint point) {
        if (indexOf(point) == -1) {
            return false;
        } else {
            return true;
        }
    }

    // return the angle by which this has been rotated from standard position
    public BasicAngle angle() {
        BytePoint direction = directions[(flip) ? 0 : 1];
        BasicAngle output = BasicAngle.createBasicAngle(0);
        BasicEdgeLength one = BasicEdgeLength.createBasicEdgeLength(0);
        for (int i = 0; i < 2*BasicAngle.ANGLE_SUM; i++) {
            output = BasicAngle.createBasicAngle(i);
            if (one.getAsVector(output).equals(direction)) break;
        }
        return output;
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicTriangle t = (BasicTriangle) obj;
        return (this.angles.equals(t.angles)&&this.vertices.equals(t.vertices)&&this.orientations.equals(t.orientations));
    }

    // hashCode method
    public int hashCode() {
        int prime = 29;
        int result = 3;
        result = prime*result + angles.hashCode();
        result = prime*result + vertices.hashCode();
        result = prime*result + orientations.hashCode();
        return result;
    }

    // toString method
    public String toString() {
        return "Triangle\n    angles: (" + angles[0] + "," + angles[1] + "," + angles[2] + ")\n  vertices: " + vertices[0] + "\n            " + vertices[1] + "\n            " + vertices[2];
    }

    // toString method for writing the corresponding prototile in gap
    public String prototileGapString() {
        String output = "    rec( vertices := [ ";
        output += vertices[1].gapString() + ", ";
        output += vertices[2].gapString() + ", ";
        output += vertices[0].gapString() + " ],\n";
        output += "         angles   := [";
        output += angles[1].fractionString() + ", ";
        output += angles[2].fractionString() + ", ";
        output += angles[0].fractionString() + "] )";
        return output;
    }

    // toString method giving gap code representing this in a substitution rule
    // left tells us whether the rule is for a prototile or its reflection
    public String functionGapString(boolean left) {
        String output = "         MkSubtile" + Preinitializer.N + "( t, T, ";
        output += ((left)? vertices[1].reflect() : vertices[1]).gapString() + ", ";
        int tileIndex = 2 * BasicPrototile.ALL_PROTOTILES.indexOf(prototile) + ((left) ? 2 : 1);
        output += tileIndex + ", ";
        output += ((left) ? angle().supplement() : angle()).toString() + " )";
        return output;
    }

    // toArray method. For drawing
    public ArrayList<RealMatrix> toArray() {
        ArrayList<RealMatrix> output = new ArrayList<RealMatrix>(3);
        for (BytePoint p : vertices) {
            output.add((RealMatrix)new Array2DRowRealMatrix(p.arrayToDraw()));
        }
        return output;
    }

    /*
    * return true if p is inside the triangle, false otherwise.
    * this involves taking three inner products with vectors orthogonal
    * to the three sides.  Taking an inner product with an orthogonal
    * vector is the same as taking the 2d cross product with the 
    * original vector.
    */
//    public boolean contains(BytePoint p) {
//        BytePoint m; // the direction vector for a side
//        BytePoint v; // the other vertex
//        BytePoint t; // vertex on the given side, used to test cross product
//        for (int i = 0; i < 3; i++) {
//            m = vertices[(i+2)%3].subtract(vertices[(i+1)%3]);
//            v = vertices[i];
//            t = vertices[(i+1)%3];
//            if (Math.signum((v.subtract(t)).crossProduct(m)) != Math.signum((p.subtract(t)).crossProduct(m)))
//                return false;
//        }
//        return true;
//    }

    // alternative to the above method
    public boolean contains(BytePoint p) {
        for (int i = 0; i < 3; i++) {
            //System.out.println((vertices[i].subtract(p)).crossProduct(directions[i]));
            if ((vertices[i].subtract(p)).crossProduct(directions[i]) < -BasicEdge.TOO_CLOSE)
                return false;
        }
        return true;
    }

    // same as contains, but returns false for this triangle's vertices
    public boolean covers(BytePoint p) {
        if (p.equals(vertices[0])||p.equals(vertices[1])||p.equals(vertices[2])) return false;
        for (int i = 0; i < 3; i++) {
            //System.out.println((vertices[i].subtract(p)).crossProduct(directions[i]));
            if ((vertices[i].subtract(p)).crossProduct(directions[i]) < 0)//-BasicEdge.TOO_CLOSE)
                return false;
        }
        return true;
    }

    /*
    * old version
    */
//    public boolean contains(BytePoint p) {
//        BytePoint m; // the direction vector for a side
//        BytePoint v; // the other vertex
//        BytePoint t; // vertex on the given side, used to test cross product
//        for (int i = 0; i < 3; i++) {
//            m = vertices[(i+2)%3].subtract(vertices[(i+1)%3]);
//            v = vertices[i];
//            t = vertices[(i+1)%3];
////            if (Math.signum((v.subtract(t)).crossProduct(m)) != Math.signum((p.subtract(t)).crossProduct(m)))
//            if (Math.signum((v.subtract(t)).crossProduct(m).evaluate(Initializer.COS)) != Math.signum((p.subtract(t)).crossProduct(m).evaluate(Initializer.COS)))
//                return false;
//        }
//        return true;
//    }

    /** 
    * Tricky incidence test.
    * First checks if point1 and point2 are both vertices of the triangle.  
    * If not, returns false.  If yes, then the function checks the 
    * orientation of the edge between point1 and point2 in the triangle.  
    * If point1 and point2 appear in that (cyclic) order in vertices[] then
    * this orientation has to match arrow; if they appear in the other 
    * (cyclic) order, then the orientation has to match arrow.opposite().
    */
    public boolean incidentEdge(BytePoint point1, BytePoint point2, Orientation arrow) {
        int i = indexOf(point1);
        int j = indexOf(point2);
        /* 
        * The orientation of the edge from the vertex at index i
        * to the vertex at index j is the orientation at the 
        * other index. The field other will be this index.
        */
        int other = -1;
        if (i == -1 || j == -1) {
            return false;
        }
        if (i == j) {
            throw new IllegalArgumentException("The two points in the edge incidence test must be different.");
        }

        // Find the index not corresponding to point1 or point2.
        for (int k = 0; k < 3; k++) {
           if (k != i && k != j) {
                other = k;
                break; 
            }
        }

        /*
        * Take orientation[other] or its opposite depending
        * on whether point1, point2 appear in cw or ccw order
        * in the listing of vertices.
        */
        if (j - i == 1 || j - i == -2) { // This tests for cw order.
            if (orientations[other].equals(arrow)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (orientations[other].getOpposite().equals(arrow)) {
                return true;
            } else {
                return false;
            }
        } 
    }

    // simple incidence test
    public boolean simpleIncidentEdge(BasicEdge e) {
        BytePoint[] ends = e.getEnds();
        List<BytePoint> points = Arrays.asList(vertices);
//        return (points.contains(ends[0])&&points.contains(ends[1]));
        return (points.contains(ends[0])&&points.indexOf(ends[1])==(points.indexOf(ends[0])+1)%3);
    }

    // reverse incidence test
    public boolean reverseIncidentEdge(BasicEdge e) {
        BytePoint[] ends = e.getEnds();
        List<BytePoint> points = Arrays.asList(vertices);
        return (points.contains(ends[1])&&points.indexOf(ends[0])==(points.indexOf(ends[1])+1)%3);
    }

    // return true if e is the second edge of its length in this
    public boolean isSecondEdge(BasicEdge e) {
        if (!prototile.hasTwo(e.getLength())) return false;
        for (BasicEdge f: getEdges()) {
            if (f.getLength().equals(e.getLength())) return (e.congruent(f) == flip);
        }
        return false;
    }

    public static void main(String[] args) {

        BasicTriangle test = BasicPrototile.createBasicPrototile(new int[] {1, 5, 5}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false);
        BytePoint p1 = BasicEdgeLength.createBasicEdgeLength(0).getAsVector(BasicAngle.createBasicAngle(20));
        BytePoint p2 = BasicEdgeLength.createBasicEdgeLength(1).getAsVector(BasicAngle.createBasicAngle(20));
        BytePoint p3 = BasicEdgeLength.createBasicEdgeLength(0).getAsVector(BasicAngle.createBasicAngle(1));
        BytePoint p4 = p1.add(p1.add(p1.add(p1.add(p1))));
        System.out.println(test);
        System.out.println("first vertex: " + test.covers(test.vertices[0]) + ". Expected: false.");
        System.out.println("second vertex: " + test.covers(test.vertices[1]) + ". Expected: false.");
        System.out.println("third vertex: " + test.covers(test.vertices[2]) + ". Expected: false.");
        System.out.println("p1: " + test.covers(p1) + ". Expected: true.");
        System.out.println("p2: " + test.covers(p2) + ". Expected: false.");
        System.out.println("p3: " + test.covers(p3) + ". Expected: true.");
        System.out.println("p4: " + test.covers(p4) + ". Expected: false.");
        System.out.println("TOO_CLOSE: " + BasicEdge.TOO_CLOSE);
        System.out.println("directions: " + test.directions[0] + " " + test.directions[1] + " " + test.directions[2]);
        BytePoint p5 = BasicEdgeLength.createBasicEdgeLength(3).getAsVector(BasicAngle.createBasicAngle(7));
        BytePoint p6 = BasicEdgeLength.createBasicEdgeLength(2).getAsVector(BasicAngle.createBasicAngle(6)).add(test.vertices[0]);
        System.out.println("p5: " + test.covers(p5) + ". Expected: false.");
        System.out.println("p6: " + test.covers(p6) + ". Expected: false.");

        // a printout to help identify the vertices of the standard triangles
        System.out.println("\nTiles in standard position:");
        BasicTriangle t0r = BasicPrototile.createBasicPrototile(new int[] {1, 4, 6}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false);
        BasicTriangle t0l = BasicPrototile.createBasicPrototile(new int[] {1, 4, 6}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),true);
        BasicTriangle t1r = BasicPrototile.createBasicPrototile(new int[] {1, 5, 5}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false);
        BasicTriangle t1l = BasicPrototile.createBasicPrototile(new int[] {1, 5, 5}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),true);
        BasicTriangle t2r = BasicPrototile.createBasicPrototile(new int[] {2, 4, 5}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false);
        BasicTriangle t2l = BasicPrototile.createBasicPrototile(new int[] {2, 4, 5}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),true);
        BasicTriangle t3r = BasicPrototile.createBasicPrototile(new int[] {2, 3, 6}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false);
        BasicTriangle t3l = BasicPrototile.createBasicPrototile(new int[] {2, 3, 6}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),true);
        BasicTriangle t4r = BasicPrototile.createBasicPrototile(new int[] {3, 3, 5}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false);
        BasicTriangle t4l = BasicPrototile.createBasicPrototile(new int[] {3, 3, 5}).place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),true);

        // now print them all
        System.out.println("t0r:");
        System.out.println(t0r);
        System.out.println("t0l:");
        System.out.println(t0l);
        System.out.println("t1r:");
        System.out.println(t1r);
        System.out.println("t1l:");
        System.out.println(t1l);
        System.out.println("t2r:");
        System.out.println(t2r);
        System.out.println("t2l:");
        System.out.println(t2l);
        System.out.println("t3r:");
        System.out.println(t3r);
        System.out.println("t3l:");
        System.out.println(t3l);
        System.out.println("t4r:");
        System.out.println(t4r);
        System.out.println("t4l:");
        System.out.println(t4l);
        System.out.println(t4l.prototileGapString());

    }

} // end of class BasicTriangle
