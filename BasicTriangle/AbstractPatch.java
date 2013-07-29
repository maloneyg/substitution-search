/**
*    This abstract class represents the properties of a collection of triangles.
*/


public abstract class AbstractPatch<P extends AbstractPoint<P>, A extends AbstractAngle, O extends AbstractOrientation<O>, E extends AbstractEdgeLength<E>, T extends AbstractTriangle, X extends AbstractPatch> {

    // The edges that haven't been covered yet.
    protected SimpleList<E> openEdges;

    // The triangles that have been placed.
    protected SimpleList<T> placedTiles;

    /**
    * methods for adding triangles and edges.
    */

    // If the edge is open, close it. If it's closed, open it.
    public void placeEdge(E edge) {
        openEdges.add(edge);
    }

    /*
    * If the triangle is placed already, remove it. 
    * If it isn't placed, place it.  
    */
    public void placeTriangle(T triangle) {
        placedTriangles.add(triangle);
    }

    /*
    * Get the next edge.
    * Presumably, the word `next' implies some kind of order,
    * probably from shortest to longest, although other ideas 
    * are conceivable.  We really want to return the edge 
    * that is most difficult to cover, because that will lead
    * to a quicker rejection of the configuration, if it is
    * already uncompletable.
    */
    public abstract E getNextEdge();

} // end of abstract class AbstractPatch
