/**
*    This class implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/

import java.io.Serializable;

public final class Orientation implements Serializable {

    private final Orientation opposite;
    private static int i = 1;
    private final int ID;

    // make it Serializable
    static final long serialVersionUID = -8541351746816390922L;

    public Orientation getOpposite() {
        return opposite;
    }

    // constructor methods.
    private Orientation(Orientation theOpposite) {
        this.opposite = theOpposite;
        this.ID = -theOpposite.ID;
    }

    private Orientation() {
        this.ID = i;
        i++;
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
        return this.ID == o.ID;
    }

    // hashCode override.
    public int hashCode() {
        return ID;
    }

    // hashCode override.
    public String toString() {
        return "Orientation " + ID;
    }

    // return true if the two orientations are not opposites.
    public boolean compatible(Orientation o) {
        return !this.equals(o.getOpposite());
    }

} // end of class Orientation