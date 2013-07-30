/**
*    This class implements a point.
*    It uses a representation as a vector of integers.  
*/


final public class BasicPoint implements AbstractPoint<BasicPoint, BasicAngle> {

    // static variables for all points.
    private static final int length = Initializer.n - 1;

    private static final IntMatrix rot = Initializer.rot;

    private static final IntMatrix ref = Initializer.ref;

    private static final IntMatrix infl = Initializer.infl;

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
        for (int i = 0; i < length; i++) {
            vector[i] = 0;
        }
        point = vector;
    }

    // public static factory method
    static public BasicPoint createBasicPoint(int[] vector) {
        return new BasicPoint(vector);
    }

    static public BasicPoint zeroVector() {
        return new BasicPoint();
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
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicPoint p = (BasicPoint) obj;
        for (int i = 0; i < length; i++) {
            if (p.point[i] != this.point[i])
                return false;
        }
        return true;
    }

    // hashCode override.
    public int hashCode() {
        int prime = 53;
        int result = 11;
        result = prime*result + ref.hashCode();
        result = prime*result + rot.hashCode();
        result = prime*result + infl.hashCode();
        for (int i = 0; i < length; i++) {
            result = prime*result + point[i];
        }
        return result;
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

    public BasicPoint rotate(BasicAngle a) {
        int i = a.getAsInt();
        if (i < 0)
            throw new RuntimeException("You must perform a positive number of rotations.");

        int[] result  = new int[length];
        for (int j = 0; j < i; j++)
            result = rot.rowTimes(result);
        return new BasicPoint(result);
    }

    public BasicPoint reflect() {
        return new BasicPoint(ref.rowTimes(this.point));
    }

    public BasicPoint inflate() {
        return new BasicPoint(infl.rowTimes(this.point));
    }

} // end of class BasicPoint
