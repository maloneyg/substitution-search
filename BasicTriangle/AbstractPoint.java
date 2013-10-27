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

    public T timesA();

    public boolean colinear(T p);
    public boolean parallel(T p);
    public double crossProduct(T p);
    public double dotProduct(T p);
    
    // it must override equals.
    public boolean equals(Object obj);

    // it must override hashCode.
    public int hashCode();

} // end of interface AbstractPoint
