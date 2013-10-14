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
/*    private static Cache<AbstractMap.SimpleEntry<BytePoint[],Orientation[]>, BasicTriangle> cache = CacheBuilder.newBuilder()
        .maximumSize(1000)// we may want to change this later
        .build(); // maintain a cache of existing BasicTriangles
*/
    // the prototile of which this is an instance
    private final BasicPrototile prototile;
    // is it reflected or not?
    private final boolean flip;

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
    }

    // public static factory methods.
    public static BasicTriangle createBasicTriangle(BasicAngle[] a, BytePoint[] p, Orientation[] o, BasicEdgeLength[] e, BasicPrototile P, boolean f) {
        /*final BasicAngle[] aa = a;
        final BytePoint[] pp = p;
        final Orientation[] oo = o;
        final BasicEdgeLength[] ee = e;
        try {
            return cache.get(new AbstractMap.SimpleEntry(p,o), new Callable<BasicTriangle>() {
                @Override
                public BasicTriangle call() throws IllegalArgumentException {
                  return new BasicTriangle(aa,pp,oo,ee);
                }
              });
        } catch (ExecutionException ex) {
            throw new IllegalArgumentException(ex.getCause());
        }*/
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
    public boolean contains(BytePoint p) {
        BytePoint m; // the direction vector for a side
        BytePoint v; // the other vertex
        BytePoint t; // vertex on the given side, used to test cross product
        for (int i = 0; i < 3; i++) {
            m = vertices[(i+2)%3].subtract(vertices[(i+1)%3]);
            v = vertices[i];
            t = vertices[(i+1)%3];
            if (Math.signum((v.subtract(t)).crossProduct(m)) != Math.signum((p.subtract(t)).crossProduct(m)))
//            if (Math.signum((v.subtract(t)).crossProduct(m).evaluate(Initializer.COS)) != Math.signum((p.subtract(t)).crossProduct(m).evaluate(Initializer.COS)))
                return false;
        }
        return true;
    }

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

} // end of class BasicTriangle
