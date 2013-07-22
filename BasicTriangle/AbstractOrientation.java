/**
*    This interface implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/


public interface AbstractOrientation<T extends AbstractOrientation> {

    public T getOpposite();

} // end of interface AbstractOrientation
