/**
*    This interface represents the properties of a point.
*    It contains static methods for manipulating points.
*/

public interface AbstractPoint<T extends AbstractPoint, A extends AbstractAngle> {

    public T add(T p);

    public T subtract(T p);

    public T rotate(A a);

    public T reflect();

    public T inflate();

    // it must override equals.
    public boolean equals(Object obj);

    // it must override hashCode.
    public int hashCode();

} // end of interface AbstractPoint
