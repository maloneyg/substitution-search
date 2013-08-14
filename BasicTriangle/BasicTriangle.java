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

public final class BasicTriangle implements AbstractTriangle<BasicAngle, BasicPoint, BasicEdgeLength, BasicEdge, BasicTriangle> {

    private final ImmutableList<BasicAngle> angles;
    private final ImmutableList<BasicPoint> vertices;
    private final ImmutableList<Orientation> orientations;
    private final ImmutableList<BasicEdgeLength> edgeLengths;

    // constructor methods.
    private BasicTriangle(BasicAngle[] a, BasicPoint[] p, Orientation[] o, BasicEdgeLength[] e) {
        angles = ImmutableList.copyOf(a);
        vertices = ImmutableList.copyOf(p);
        orientations = ImmutableList.copyOf(o);
        edgeLengths = ImmutableList.copyOf(e);
    }

    // public static factory methods.
    public static BasicTriangle createBasicTriangle(BasicAngle[] a, BasicPoint[] p, Orientation[] o, BasicEdgeLength[] e) {
        return new BasicTriangle(a,p,o,e);
    }

    public ImmutableList<BasicAngle> getAngles() {
        return angles;
    }

    public ImmutableList<BasicPoint> getVertices() {
        return vertices;
    }

    public ImmutableList<Orientation> getOrientations() {
        return orientations;
    }

    // Don't call this method too often; it's time-consuming.
    public BasicEdge[] getEdges() {
        BasicPoint[] vertexPair0 = {vertices.get(0),vertices.get(1)};
        BasicPoint[] vertexPair1 = {vertices.get(1),vertices.get(2)};
        BasicPoint[] vertexPair2 = {vertices.get(2),vertices.get(0)};
        BasicEdge e0 = BasicEdge.createBasicEdge(edgeLengths.get(0),orientations.get(0),vertexPair0);
        BasicEdge e1 = BasicEdge.createBasicEdge(edgeLengths.get(1),orientations.get(1),vertexPair1);
        BasicEdge e2 = BasicEdge.createBasicEdge(edgeLengths.get(2),orientations.get(2),vertexPair2);
        BasicEdge[] output = {e0,e1,e2};
        return output;
    }

    // Given two points on the triangle, return the third.  
    public BasicPoint getOtherVertex(BasicPoint vertex1, BasicPoint vertex2) {

        // First make sure the input points are on the triangle.  
        if (!(incidentPoint(vertex1) && incidentPoint(vertex2))) {
            throw new IllegalArgumentException("One of the input points does not lie on the triangle.");
        }

        // Now return the vertex that is different from both of them.  
        for (int i = 0; i < 3; i++) {
            if (!(vertices.get(i).equals(vertex1) || vertices.get(i).equals(vertex2))) {
                return vertices.get(i);
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
    private int indexOf(BasicPoint point) {
        for (int i = 0; i < 3; i++) {
            if (vertices.get(i).equals(point))
                return i;
        }
        return -1;
    }

    // Tests for incidence.  
    public boolean incidentPoint(BasicPoint point) {
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
        return "Triangle\n    angles: (" + angles.get(0) + "," + angles.get(1) + "," + angles.get(2) + ")\n  vertices: " + vertices.get(0) + "\n            " + vertices.get(1) + "\n            " + vertices.get(2);
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
    public boolean incidentEdge(BasicPoint point1, BasicPoint point2, Orientation arrow) {
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
            if (orientations.get(other).equals(arrow)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (orientations.get(other).getOpposite().equals(arrow)) {
                return true;
            } else {
                return false;
            }
        } 
    }
} // end of class BasicTriangle
