/**
*    This interface represents the properties of an edge.
*/

public interface AbstractEdge<P extends AbstractPoint, L extends AbstractEdgeLength, O extends AbstractOrientation, E extends AbstractEdge> {

    public L getLength();

    public P[] getEnds();

    /* 
    * Given an edge with a (possibly different) orientation,
    * get the orientation of this edge, using the direction
    * convention established by the other edge.  
    */ 
    public O getOrientation(E e);

    // Check if two edges are the same, with identical orientations. 
    public boolean equals(E e);

    // Check if two edges are the same, with non-opposite orientations. 
    public boolean compatible(E e);

} // end of interface AbstractEdge
