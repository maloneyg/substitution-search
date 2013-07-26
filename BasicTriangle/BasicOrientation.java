/**
*    This class implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/


public final class BasicOrientation extends AbstractOrientation<BasicOrientation> {

    // An integer serial number for troubleshooting purposes only.
    private final int code;

    // The next code number.
    private static int nextCode = 1;

    // constructor methods.
    private BasicOrientation(BasicOrientation theOpposite) {
        this.opposite = theOpposite;
        this.code = nextCode;
        nextCode = nextCode + 1;
    }

    private BasicOrientation() {
        this.opposite = new BasicOrientation(this);
        this.code = 1 - nextCode;
    }

    // public static factory method.
    static public BasicOrientation createBasicOrientation() {
        return new BasicOrientation();
    }

    // implementation of equals method.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicOrientation o = (BasicOrientation) obj;
        return this == o;
    }

    // hashCode override.
    public int hashCode() {
        int prime = 23;
        int result = 3;
        result = prime*result + code;
        return result;
    }

} // end of class BasicOrientation
