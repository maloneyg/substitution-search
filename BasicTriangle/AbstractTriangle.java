/**
*    This interface represents the properties of a triangle.
*    The triangle is located somewhere in space, although we don't 
*    specify how its points are represented.  
*    It has three angles, three vertices, and three edge orientations.
*/

import com.google.common.collect.*;

public interface AbstractTriangle<A extends AbstractAngle, L extends AbstractEdgeLength<A,L>, E extends AbstractEdge<A,L,E>, T extends AbstractTriangle> {

    /**
    * getter methods that return the points, angles, 
    * orientations, and edge lengths.  
    * These things must implement various interfaces
    * representing their abstract idealizations.  
    */
    public AbstractPoint[] getVertices();

    /**
    * Given two vertices that (presumably) lie on the triangle,
    * return the third vertex.
    */
    public AbstractPoint getOtherVertex(AbstractPoint vertex1, AbstractPoint vertex2);

    public A[] getAngles();

    public Orientation[] getOrientations();

    public E[] getEdges();

    /**
    * Incidence test methods.  
    */

    public boolean incidentPoint(AbstractPoint point);

    public boolean incidentEdge(AbstractPoint point1, AbstractPoint point2, Orientation arrow);

} // end of interface AbstractTriangle
