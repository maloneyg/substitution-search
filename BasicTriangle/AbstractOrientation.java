/**
*    This interface implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/


public interface AbstractOrientation<T extends AbstractOrientation> {

    public T getOpposite();

    // return true if the two orientations are equal.
    public boolean equals(T o);

    // return true if the two orientations are not opposites.
    public boolean compatible(T o);

} // end of interface AbstractOrientation
