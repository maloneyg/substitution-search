/**
*    This interface implements a point.
*    It contains static methods for manipulating points.
*/

public interface AbstractPoint {

    public <T extends AbstractPoint> T add(T p);

    public <T extends AbstractPoint> T subtract(T p);

    public <T extends AbstractPoint> T rotate(int i);

    public <T extends AbstractPoint> T reflect();

    public <T extends AbstractPoint> T inflate();

} // end of interface AbstractPoint
