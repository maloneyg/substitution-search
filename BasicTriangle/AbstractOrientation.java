/**
*    This abstract class represents the properties of an edge orientation.
*    The main property is that every orientation has an opposite.
*/


public abstract class AbstractOrientation<T extends AbstractOrientation<T>> {

    protected T opposite;

    public T getOpposite() {
        return opposite;
    }

    protected final T self() {
        return (T) this;
    }

    // return true if the two orientations are equal.
    public abstract boolean equals(Object obj);

    // it must override hashCode.
    public abstract int hashCode();

    // return true if the two orientations are not opposites.
    public boolean compatible(T o) {
        return !this.equals(o.getOpposite());
    }

    /*
    * return to if this orientation equals from, and 
    * -to if this orientation equals -from.
    * Otherwise return this.
    */
    public T reset(T from, T to) {
        if (this.equals(from)) {
            return to;
        } else if (this.equals(from.getOpposite())) {
            return to.getOpposite();
        } else {
            return self();
        }
    }

} // end of abstract class AbstractOrientation
