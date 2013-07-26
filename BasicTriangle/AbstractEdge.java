/**
*    This interface represents the properties of an edge.
*/

public interface AbstractEdge<A extends AbstractAngle, P extends AbstractPoint, L extends AbstractEdgeLength, O extends AbstractOrientation, E extends AbstractEdge> {

    public L getLength();

    public P[] getEnds();

    /* 
    * Given an edge with a (possibly different) orientation,
    * get the orientation of this edge, using the direction
    * convention established by the other edge.  
    */ 
    public O getOrientation(E e);

    /*
    * return the edge obtained by rotating this one by an angle
    * of a and then shifting it by the vector v.
    */
    public E transform(A a, P v);

    // Check if two edges are the same, with identical orientations. 
    public boolean equals(Object obj);

    // Check if two edges are the same, with non-opposite orientations. 
    public boolean compatible(E e);

} // end of interface AbstractEdge
