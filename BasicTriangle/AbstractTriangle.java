/**
*    This interface implements a triangle.
*    The triangle is located somewhere in space, although we don't 
*    specify how its points are represented.  
*    It has three angles, three vertices, and three edge orientations.
*/


public interface AbstractTriangle {

    /**
    * getter methods that return the points, angles, 
    * orientations, and edge lengths.  
    * These things must implement various interfaces
    * representing their abstract idealizations.  
    */
//    public <T extends AbstractPoint> T[] getVertices();

    /**
    * Given two vertices that (presumably) lie on the triangle,
    * return the third vertex.
    */
//    public <T extends AbstractPoint> T getOtherVertex(T vertex1, T vertex2);

//    public <T extends AbstractAngle> T[] getAngles();

//    public <T extends AbstractOrientation> T[] getOrientations();

//    public <T extends AbstractEdgeLength> T[] getEdgeLengths();

    /**
    * setter method for orientations.
    * setter methods for vertices, angles, and edge lengths 
    * probably shouldn't even exist.  
    */

//    public <T extends AbstractOrientation> void setOrientation(T arrow, int i);

    /**
    * Incidence test methods.  
    */

//    public <T extends AbstractPoint> boolean incidentPoint(T point);

//    public <T extends AbstractPoint, S extends AbstractOrientation> boolean incidentEdge(T point1, T point2, S arrow);

} // end of interface AbstractTriangle
