/**
*    This interface implements a point.
*    It contains static methods for manipulating points.
*/

public interface AbstractPoint<T extends AbstractPoint, A extends AbstractAngle> {

    public T add(T p);

    public T subtract(T p);

    public T rotate(A a);

    public T reflect();

    public T inflate();

    public boolean equals(T p);

} // end of interface AbstractPoint
