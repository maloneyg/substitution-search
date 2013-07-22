/**
*    This class implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/


public final class BasicOrientation implements AbstractOrientation<BasicOrientation> {

    private final BasicOrientation opposite;

    public BasicOrientation getOpposite() {
        return opposite;
    }

    private BasicOrientation(BasicOrientation theOpposite) {
        this.opposite = theOpposite;
    }

    private BasicOrientation() {
        BasicOrientation theOpposite = new BasicOrientation(this);
        this.opposite = theOpposite;
    }

    public BasicOrientation createBasicOrientation() {
        return new BasicOrientation();
    }

} // end of class BasicOrientation
