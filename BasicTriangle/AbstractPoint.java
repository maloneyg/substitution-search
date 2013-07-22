/**
*    This interface implements a point.
*    It contains static methods for manipulating points.
*/

public interface AbstractPoint<T extends AbstractPoint> {

    public T add(T p);

    public T subtract(T p);

    public T rotate(int i);

    public T reflect();

    public T inflate();

} // end of interface AbstractPoint
