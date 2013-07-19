/**
*    This class implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/


public final class BasicOrientation implements AbstractOrientation {

    private final BasicOrientation opposite;

    public BasicOrientation getOpposite() {
        return opposite;
    }

    public BasicOrientation(BasicOrientation theOpposite) {
        this.opposite = theOpposite;
    }

    public BasicOrientation() {
        BasicOrientation theOpposite = new BasicOrientation(this);
        this.opposite = theOpposite;
    }

} // end of class BasicOrientation
