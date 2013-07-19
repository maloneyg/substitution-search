/**
*    This interface implements an edge orientation.
*    The main property is that every orientation has an opposite.
*/


public interface AbstractOrientation {

    public <T extends AbstractOrientation> T getOpposite();

} // end of interface AbstractOrientation
