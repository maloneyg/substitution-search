/**
*    This class implements a point.
*    It uses a representation as a vector of integers.  
*/


public class BasicPoint implements AbstractPoint {

    // Static variables for all points.
    private static final int length = 6;


    // A vector identifying the point.  
    private final int[] point;

    // Constructor methods.

    private BasicPoint(int[] vector) {
        if (vector.length != length) {
            throw new IllegalArgumentException("Point length is incorrect.");
        }
        this.point = vector;
    }

    private BasicPoint() {
        int[] vector = new int[length];
        point = vector;
    }

    // Manipulation methods.  
    public BasicPoint add(BasicPoint p) {
        int[] q = new int[length];
        for (int i = 0; i < length; i++) {
            q[i] = this.point[i] + p.point[i];
        }
        return new BasicPoint(q);
    }

    public BasicPoint scalarMultiple(int c) {
        int[] q = new int[length];
        for (int i = 0; i < length; i++) {
            q[i] = c * this.point[i];
        }
        return new BasicPoint(q);
    }

    public BasicPoint subtract(BasicPoint p) {
        return this.add(p.scalarMultiple(-1));
    }

    public BasicPoint rotate(int i) {
        return this;
    }

    public BasicPoint reflect() {
        return this;
    }

    public BasicPoint inflate() {
        return this;
    }

} // end of class BasicPoint
