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

public class BasicTriangle implements AbstractTriangle<BasicAngle, Orientation.g, BasicPoint, BasicEdgeLength, BasicEdge, BasicTriangle> {

    private final BasicAngle[] angles;
    private final BasicPoint[] vertices;
    private final Orientation.g[] orientations;
    private final BasicEdgeLength[] edgeLengths;

    // constructor methods.
    private BasicTriangle(BasicAngle[] a, BasicPoint[] p, Orientation.g[] o, BasicEdgeLength[] e) {
        angles = a;
        vertices = p;
        orientations = o;
        edgeLengths = e;
    }

    // public static factory methods.
    public BasicTriangle createBasicTriangle(BasicAngle[] a, BasicPoint[] p, Orientation.g[] o, BasicEdgeLength[] e) {
        return new BasicTriangle(a,p,o,e);
    }

    public BasicAngle[] getAngles() {
        return angles;
    }

    public BasicPoint[] getVertices() {
        return vertices;
    }

    public Orientation.g[] getOrientations() {
        return orientations;
    }

    // Don't call this method too often; it's time-consuming.
    public BasicEdge[] getEdges() {
        BasicPoint[] vertexPair0 = {vertices[0],vertices[1]};
        BasicPoint[] vertexPair1 = {vertices[1],vertices[2]};
        BasicPoint[] vertexPair2 = {vertices[2],vertices[0]};
        BasicEdge e0 = BasicEdge.createBasicEdge(edgeLengths[0],orientations[0],vertexPair0);
        BasicEdge e1 = BasicEdge.createBasicEdge(edgeLengths[1],orientations[1],vertexPair1);
        BasicEdge e2 = BasicEdge.createBasicEdge(edgeLengths[2],orientations[2],vertexPair2);
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
            if (!(vertices[i].equals(vertex1) || vertices[i].equals(vertex2))) {
                return vertices[i];
            }
        }

        // If we get this far, that means the triangle only has two vertices.  
        throw new IllegalStateException("There is a triangle with only two vertices.");

    }

    // Sets the orientation of edge i.
    public void setOrientation(Orientation.g arrow, int i) {
        orientations[i] = arrow;
    }

    /** 
    * Return the index of a point on the triangle.  
    * If the point is not on the triangle, return -1.
    */
    private int indexOf(BasicPoint point) {
        for (int i = 0; i < 3; i++) {
            if (vertices[i].equals(point))
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

    /** 
    * Tricky incidence test.
    * First checks if point1 and point2 are both vertices of the triangle.  
    * If not, returns false.  If yes, then the function checks the 
    * orientation of the edge between point1 and point2 in the triangle.  
    * If point1 and point2 appear in that (cyclic) order in vertices[] then
    * this orientation has to match arrow; if they appear in the other 
    * (cyclic) order, then the orientation has to match arrow.opposite().
    */
    public boolean incidentEdge(BasicPoint point1, BasicPoint point2, Orientation.g arrow) {
        int i = indexOf(point1);
        int j = indexOf(point2);
        int other = -1;
        if (i == -1 || j == -1) {
            return false;
        }
        if (i == j) {
            throw new IllegalArgumentException("The two points in the edge incidence test must be different.");
        }

        for (int k = 0; k < 3; k++) {
           if (k != i && k != j) {
                other = k;
                break; 
            }
        }

        if (j - i == 1 || j - i == -2) {
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
} // end of class BasicTriangle
