/**
*    This interface represents the properties of a collection of triangles.
*/


public interface AbstractPatch<P extends AbstractPoint<P>, A extends AbstractAngle<A>, O extends AbstractOrientation<O>, E extends AbstractEdgeLength<E>, T extends AbstractTriangle, X extends AbstractPatch> {

    /**
    * getter methods that return the points, angles, 
    * orientations, and edge lengths.  
    * These things must implement various interfaces
    * representing their abstract idealizations.  
    */
    public P[] getVertices();

    /**
    * setter method for orientations.
    * setter methods for vertices, angles, and edge lengths 
    * probably shouldn't even exist.  
    */

    public void setOrientation(O arrow, int i);

    /**
    * Incidence test methods.  
    */

    public boolean incidentPoint(P point);

    public boolean incidentEdge(P point1, P point2, O arrow);

} // end of interface AbstractPatch
