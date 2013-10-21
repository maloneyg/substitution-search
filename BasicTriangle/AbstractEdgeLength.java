/**
*    This interface represents the properties of an edge length.
*/


public interface AbstractEdgeLength<A extends AbstractAngle, L extends AbstractEdgeLength> {

    public boolean equals(Object obj);

    /*
    * return a vector with length equal to this edge length.
    * The vector should lie on the positive x-axis, if that
    * notion makes any sense.
    */
    public AbstractPoint getAsVector(A a);

} // end of interface AbstractEdgeLength
