/**
*    This class implements a point.
*    It uses a representation as a vector of integers.  
*/


final public class BasicPoint implements AbstractPoint<BasicPoint> {

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

    // toString method.
    public String toString() {
        String outString = "(";
        for (int i = 0; i < length - 1; i++) {
            outString = outString + point[i] + ",";
        }
        outString = outString + point[length] + ")";
        return outString;
    }

    // equals method.
    public boolean equals(BasicPoint p) {
        for (int i = 0; i < length; i++) {
            if (p.point[i] != this.point[i])
                return false;
        }
        return true;
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
