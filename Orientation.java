/**
*    This class implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/

import java.io.Serializable;

public final class Orientation implements Serializable {

    private final Orientation opposite;
    private static int i = 1;
    private final int ID;

    // a pool of Orientations to be used on prototile edges
    private static final Orientation[] POOL;
    // an index to tell us where we are in the pool
    private static int poolDepth = 0;

    // make it Serializable
    static final long serialVersionUID = -8541351746816390922L;

    public Orientation getOpposite() {
        return opposite;
    }

    static { // initialize the Orientation pool
        Orientation[] prePool = new Orientation[3*Preinitializer.PROTOTILES.size()];
        for (int i = 0; i < prePool.length; i++) prePool[i] = new Orientation();
        POOL = prePool;
    } // here ends the initialization of the Orientation pool

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

    static public Orientation getPooledOrientation() {
        Orientation output = POOL[poolDepth];
        poolDepth = (poolDepth + 1)%POOL.length;
        return output;
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
