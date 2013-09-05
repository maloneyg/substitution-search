/**
*    This class implements a point.
*    It uses a representation as a vector of integers.  
*/

import com.google.common.collect.*;
import com.google.common.base.*;
import java.io.Serializable;

final public class BasicPoint implements AbstractPoint<BasicPoint, BasicAngle>, Serializable {

    // make is Serializable
    static final long serialVersionUID = -6462075103242603792L;

    // static variables for all points.
    public static final int length = Initializer.N - 1;

    public static final IntMatrix A = Initializer.A;

    public static final IntMatrix ROT = Initializer.ROT;

    public static final IntMatrix REF = Initializer.REF;

    public static final IntMatrix INFL = Initializer.INFL;

    public static final BasicPoint ZERO_VECTOR = new BasicPoint();

    public static final BasicPoint UNIT_VECTOR;

    static { // initialize the unit vector

        int[] preUnit = new int[length];
        preUnit[0] = 1;
        for (int i = 1; i < length; i++) {
            preUnit[i] = 0;
        }
        UNIT_VECTOR = new BasicPoint(preUnit);

    }

    // A vector identifying the point.  
    private final ImmutableList<Integer> point;

    // Constructor methods.

    private BasicPoint(int[] vector) {
        if (vector.length != length) {
            throw new IllegalArgumentException("Point length is incorrect.");
        }
        Integer[] tempVector = new Integer[length];
        for (int i = 0; i < length; i++)
            tempVector[i] = Integer.valueOf(vector[i]);
        this.point = ImmutableList.copyOf(tempVector);
    }

    private BasicPoint() {
        Integer[] vector = new Integer[length];
        for (int i = 0; i < length; i++) {
            vector[i] = 0;
        }
        point = ImmutableList.copyOf(vector);
    }

    // public static factory method
    static public BasicPoint createBasicPoint(int[] vector) {
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
    private int[] pointAsArray() {
        int[] output = new int[length];
        for (int i = 0; i < length; i++)
            output[i] = point.get(i);
        return output;
    }

    // a method for drawing
    public double[] arrayToDraw() {
        double[] output = new double[length];
        for (int i = 0; i < length; i++)
            output[i] = (double) point.get(i);
        return output;
    }

    // Manipulation methods.  
    public BasicPoint add(BasicPoint p) {
        int[] q = new int[length];
        for (int i = 0; i < length; i++) {
            q[i] = this.point.get(i) + p.point.get(i);
        }
        return new BasicPoint(q);
    }

    public BasicPoint scalarMultiple(int c) {
        int[] q = new int[length];
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

        int[] result  = pointAsArray();
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

    /*
    * calculate the 2d cross-product of this with p, after 
    * they have both been projected down to the plane
    * using the standard projection (i.e., the ith
    * standard basis vector goes to (cos(i pi/N), sin(i pi/N))).
    *
    * If the cross-product isn't 0, return false.
    *
    * WARNING: this only works for prime N right now.
    * WARNING: this only works for odd N right now.
    */
    public boolean colinear(BasicPoint p) {
        int l = length/2;
        int[] p0 = this.pointAsArray();
        int[] p1 = p.pointAsArray();
        // here we store the shoelace products
        int[] coeffs = new int[l];
        for (int i = 0; i < l; i++) {
            coeffs[i] = 0;
            for (int j = 0; j < length; j++) {
                if (j+i != length-1) coeffs[i] += p0[j]*p1[(j+i+1)%(length+1)]*((j+i+1>length)? -1 : 1);
                if (j != i) coeffs[i] -= p0[j]*p1[(j-i-1 < 0)? length+j-i : j-i-1]*((j-i-1<0)? -1 : 1);
            }
            if (coeffs[i] != 0) return false;
        }
        return true;
    }

    /*
    * calculate the 2d dot-product of this with p, after 
    * they have both been projected down to the plane
    * using the standard projection (i.e., the ith
    * standard basis vector goes to (cos(i pi/N), sin(i pi/N))).
    *
    * WARNING: this only works for prime N right now.
    * WARNING: this only works for odd N right now.
    */
    public int[] dotProduct(BasicPoint p) {
        int l = length/2+1;
        int[] p0 = this.pointAsArray();
        int[] p1 = p.pointAsArray();
        // here we store the shoelace products
        int[] coeffs = new int[l];
        for (int i = 0; i < l; i++) {
            coeffs[i] = 0;
            for (int j = 0; j < length; j++) {
                if (j+i != length) coeffs[i] += p0[j]*p1[(j+i)%(length+1)]*((j+i>length)? -1 : 1);
                if (i != 0 && j-i != -1) coeffs[i] += p0[j]*p1[(j-i < 0)? length+1+j-i : j-i]*((j-i<0)? -1 : 1);
            }
        }
        return coeffs;
    }

} // end of class BasicPoint
