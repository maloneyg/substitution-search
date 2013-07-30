/**
*    This interface represents the properties of a triangle.
*    The triangle is located somewhere in space, although we don't 
*    specify how its points are represented.  
*    It has three angles, three vertices, and three edge orientations.
*/


public interface AbstractTriangle<A extends AbstractAngle, Orientation, P extends AbstractPoint<P,A>, L extends AbstractEdgeLength, E extends AbstractEdge<A,P,L,O,E>, T extends AbstractTriangle> {

    /**
    * getter methods that return the points, angles, 
    * orientations, and edge lengths.  
    * These things must implement various interfaces
    * representing their abstract idealizations.  
    */
    public P[] getVertices();

    /**
    * Given two vertices that (presumably) lie on the triangle,
    * return the third vertex.
    */
    public P getOtherVertex(P vertex1, P vertex2);

    public A[] getAngles();

    public Orientation[] getOrientations();

    public E[] getEdges();

    /**
    * setter method for orientations.
    * setter methods for vertices, angles, and edge lengths 
    * probably shouldn't even exist.  
    */

    public void setOrientation(Orientation arrow, int i);

    /**
    * Incidence test methods.  
    */

    public boolean incidentPoint(P point);

    public boolean incidentEdge(P point1, P point2, Orientation arrow);

} // end of interface AbstractTriangle
