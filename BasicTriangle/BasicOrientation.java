/**
*    This class implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/


public final class BasicOrientation implements AbstractOrientation<BasicOrientation> {

    private final BasicOrientation opposite;

    // An integer serial number for troubleshooting purposes only.
    private final int code;

    // The next code number.
    private static int nextCode = 1;

    public BasicOrientation getOpposite() {
        return opposite;
    }

    private BasicOrientation(BasicOrientation theOpposite) {
        this.opposite = theOpposite;
        this.code = 0 - theOpposite.code;
    }

    private BasicOrientation() {
        this.code = nextCode;
        BasicOrientation theOpposite = new BasicOrientation(this);
        this.opposite = theOpposite;
        nextCode = nextCode + 1;
    }

    public BasicOrientation createBasicOrientation() {
        return new BasicOrientation();
    }

    public boolean equals(BasicOrientation o) {
        return this == o;
    }

    public boolean compatible(BasicOrientation o) {
        return !(this.equals(o.opposite));
    }

} // end of class BasicOrientation
