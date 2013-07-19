/**
*    This interface implements all the operations that
*    we can do on points.
*    This includes addition, subtraction, rotation, reflection,
*    and inflation.  
*/


public interface AbstractPointOperations {

    public static <T extends AbstractPoint> T add(T p1, T p2);

    public static <T extends AbstractPoint> T subtract(T p1, T p2);

    public static <T extends AbstractPoint> T rotate(T p1, int i);

    public static <T extends AbstractPoint> T reflect(T p1);

    public static <T extends AbstractPoint> T inflate(T p1);

} // end of interface AbstractPointOperations
