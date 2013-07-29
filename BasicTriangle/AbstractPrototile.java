/**
*    This interface represents the properties of a prototile.
*    It is not located in space.  
*    It should provide a way to place AbstractTriangles of its 
*    own type, and also pointers connecting the edges of this
*    prototile to the edges of such a placed triangle.  
*/


public abstract class AbstractPrototile<A extends AbstractAngle, O extends AbstractOrientation<O>, P extends AbstractPoint<P,A>, L extends AbstractEdgeLength, E extends AbstractEdge<P,L,O,E>, T extends AbstractTriangle> {

    private final A[] angles;

    // L[i] is the length of the side opposite A[i].
    private final L[] lengths;

    // O[i] is the orientation of the side opposite A[i].
    private final O[] orientations;

    /**
    * getter methods that return the 
    * orientations and edge lengths.  
    */
    public L[] getEdgeLengths() {
        return lengths;
    }

    public O[] getOrientations() {
        return orientations;
    }

    /*
    * Place the prototile in space.
    * p says where to place the root vertex.
    * a says how to orient it.
    * reflect says whether or not it's reflected.  
    */
    public abstract T place(P p, A a, boolean reflect);

    /**
    * reset method for orientations.
    */
    public void reset(O from, O to) {
        for (O o : orientations) {
            o = o.reset(from, to);
        }
    }

} // end of abstract class AbstractPrototile
