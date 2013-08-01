/**
*    This class implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/


public final class Orientation {

    private final Orientation opposite;

    public Orientation getOpposite() {
        return opposite;
    }

    // constructor methods.
    private Orientation(Orientation theOpposite) {
        this.opposite = theOpposite;
    }

    private Orientation() {
        this.opposite = new Orientation(this);
    }

    // public static factory method.
    static public Orientation createOrientation() {
        return new Orientation();
    }

    // implementation of equals method.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        Orientation o = (Orientation) obj;
        return this == o;
    }

    // hashCode override.
    public int hashCode() {
        return super.hashCode();
    }

    // return true if the two orientations are not opposites.
    public boolean compatible(Orientation o) {
        return !this.equals(o.getOpposite());
    }

} // end of class Orientation
