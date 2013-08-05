/**
*    This class implements a point.
*    It uses a representation as a vector of integers.  
*/

import com.google.common.collect.*;
import com.google.common.base.*;

final public class BasicPoint implements AbstractPoint<BasicPoint, BasicAngle> {

    // static variables for all points.
    private static final int length = Initializer.N - 1;

    private static final IntMatrix A = Initializer.A;

    private static final IntMatrix ROT = Initializer.ROT;

    private static final IntMatrix REF = Initializer.REF;

    private static final IntMatrix INFL = Initializer.INFL;

    // A vector identifying the point.  
    private final ImmutableList<Integer> point;

    // Constructor methods.

    private BasicPoint(Integer[] vector) {
        if (vector.length != length) {
            throw new IllegalArgumentException("Point length is incorrect.");
        }
        this.point = ImmutableList.copyOf(vector);
    }

    private BasicPoint() {
        Integer[] vector = new Integer[length];
        for (int i = 0; i < length; i++) {
            vector[i] = 0;
        }
        point = ImmutableList.copyOf(vector);
    }

    // public static factory method
    static public BasicPoint createBasicPoint(Integer[] vector) {
        return new BasicPoint(vector);
    }

    static final public BasicPoint zeroVector() {
        return new BasicPoint();
    }

    static final public BasicPoint unitVector() {
        Integer[] vector = new Integer[length];
        vector[0] = 1;
        for (int i = 1; i < length; i++) {
            vector[i] = 0;
        }
        return new BasicPoint(vector);
    }

    // toString method.
    public String toString() {
        String outString = "(";
        for (int i = 0; i < point.size() - 1; i++) {
            outString = outString + point.get(i) + ",";
        }
        outString = outString + point.get(length-1) + ")";
        return outString;
    }

    // equals method.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicPoint p = (BasicPoint) obj;
        for (int i = 0; i < length; i++) {
            if (p.point.get(i) != this.point.get(i))
                return false;
        }
        return true;
    }

    // hashCode override.
    public int hashCode() {
        int prime = 53;
        int result = 11;
        for (int i = 0; i < length; i++) {
            result = prime*result + point.get(i);
        }
        return result;
    }

    // a private helper method to turn point into an array of Integers.
    private Integer[] pointAsArray() {
        Integer[] output = new Integer[length];
        for (int i = 0; i < length; i++)
            output[i] = point.get(i);
        return output;
    }

    // Manipulation methods.  
    public BasicPoint add(BasicPoint p) {
        Integer[] q = new Integer[length];
        for (int i = 0; i < length; i++) {
            q[i] = this.point.get(i) + p.point.get(i);
        }
        return new BasicPoint(q);
    }

    public BasicPoint scalarMultiple(int c) {
        Integer[] q = new Integer[length];
        for (int i = 0; i < length; i++) {
            q[i] = c * this.point.get(i);
        }
        return new BasicPoint(q);
    }

    public BasicPoint subtract(BasicPoint p) {
        return this.add(p.scalarMultiple(-1));
    }

    public BasicPoint rotate(BasicAngle a) {
        int i = a.getAsInt();
        if (i < 0)
            throw new IllegalArgumentException("You must perform a positive number of rotations.");

        Integer[] result  = new Integer[length];
        for (int j = 0; j < i; j++)
            result = ROT.rowTimes(result);
        return new BasicPoint(result);
    }

    public BasicPoint reflect() {
        return new BasicPoint(REF.rowTimes(this.pointAsArray()));
    }

    public BasicPoint inflate() {
        return new BasicPoint(INFL.rowTimes(this.pointAsArray()));
    }

    protected BasicPoint timesA() {
        return new BasicPoint(A.rowTimes(this.pointAsArray()));
    }

} // end of class BasicPoint
