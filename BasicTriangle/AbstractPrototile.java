/**
*    This interface represents the properties of a prototile.
*    It is not located in space.  
*    It should provide a way to place AbstractTriangles of its 
*    own type.
*/


public interface AbstractPrototile<A extends AbstractAngle, L extends AbstractEdgeLength, E extends AbstractEdge<A,L,E>, T extends AbstractTriangle> {

    /*
    * return true if this has an edge with length l
    */
    public boolean compatible(L l);

    /*
    * Place the prototile in space.
    * p says where to place the root vertex.
    * a says how to orient it.
    * flip says whether or not to reflect it.
    */
//    public T place(P p, A a, boolean flip);

} // end of interface AbstractPrototile
