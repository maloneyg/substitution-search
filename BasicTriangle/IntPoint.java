/**
*    This class implements a point.
*    It uses a representation as a vector of integers.  
*    -- modified to use ints to save memory
*/

import com.google.common.collect.*;
import com.google.common.base.*;
import com.google.common.cache.*;
import java.io.Serializable;
import java.lang.Math;

final public class IntPoint implements AbstractPoint<IntPoint, BasicAngle>, Serializable {

    // static variables for all points.
    public static final int length = Initializer.N - 1;

    public static final IntMatrix A = Initializer.iA;

    public static final IntMatrix ROT = Initializer.iROT;

    public static final IntMatrix REF = Initializer.iREF;

    public static final IntMatrix INFL = Initializer.iINFL;

    public static final IntPoint ZERO_VECTOR;

    public static final IntPoint UNIT_VECTOR;

    public static final float[] COS_POWERS = Initializer.COS_LIST;

    // a pool containing all the IntPoints that have been created
    //private static final IntPointPool POOL = IntPointPool.getInstance();

    static { // initialize the unit vector

        int[] preUnit = new int[length];
        int[] preZero = new int[length];
        preUnit[0] = 1;
        preZero[0] = 0;
        for (int i = 1; i < length; i++) {
            preUnit[i] = 0;
            preZero[i] = 0;
        }
        UNIT_VECTOR = createIntPoint(preUnit);
        ZERO_VECTOR = createIntPoint(preZero);

    }

    // A vector identifying the point.  
    private final int[] point;

    // Constructor methods.
    private IntPoint(int[] vector) {
        point = vector;
    }

    private IntPoint() {
        point = new int[length];
    }

    // public static factory method 
    static public IntPoint createIntPoint(int[] vector) {
        return new IntPoint(vector);
    }

    // public static factory method
    static public IntPoint createIntPoint(IntPoint p, boolean flip, BasicAngle a, IntPoint shift) {
        int[] vector = p.pointAsArray();
        if (flip) vector = REF.rowTimes(vector);
        vector = a.getIntRotation().rowTimes(vector);
        int[] copyMe = shift.pointAsArray();
        for (int i = 0; i < vector.length; i++) vector[i] = (vector[i]+copyMe[i]);
        return new IntPoint(vector);
    }

    // toString method.
    public String toString() {
        String outString = "(";
        for (int i = 0; i < point.length - 1; i++) {
            outString = outString + point[i] + ",";
        }
        outString = outString + point[point.length-1] + ")";
        return outString;
    }

    // equals method.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        IntPoint p = (IntPoint) obj;
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
        for (int i = 0; i < length; i++) {
            result = prime*result + point[i];
        }
        return result;
    }

    // return a deep copy of the contents of this IntPoint
    protected int[] pointAsArray() {
        int[] newArray = new int[length];
        for (int i=0; i < point.length; i++)
            newArray[i] = point[i];
        return newArray;
    }

    // a method for drawing
    public double[] arrayToDraw() {
        double[] output = new double[length];
        for (int i = 0; i < length; i++)
            output[i] = (double) point[i];
        return output;
    }

    // Manipulation methods.  
    public IntPoint add(IntPoint p) {
        int[] q = new int[length];
        for (int i = 0; i < length; i++) {
            q[i] = (point[i] + p.point[i]);
        }
        return createIntPoint(q);
    }

    public IntPoint scalarMultiple(int c) {
        int[] q = new int[length];
        for (int i = 0; i < length; i++) {
            q[i] = (c * point[i]);
        }
        return createIntPoint(q);
    }

    public IntPoint subtract(IntPoint p) {
        return this.add(p.scalarMultiple(-1));
    }

    public IntPoint rotate(BasicAngle a) {
        int[] result  = a.getIntRotation().rowTimes(pointAsArray());
        return createIntPoint(result);
    }

    public IntPoint reflect() {
        return createIntPoint(REF.rowTimes(this.pointAsArray()));
    }

    public IntPoint inflate() {
        return createIntPoint(INFL.rowTimes(this.pointAsArray()));
    }

    public IntPoint timesA() {
        return createIntPoint(A.rowTimes(this.pointAsArray()));
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
    public boolean colinear(IntPoint p) {
        int l = length/2;
        int[] p0 = this.point;
        int[] p1 = p.point;
        // here we store the shoelace products
        int[] coeffs = new int[l];
        for (int i = 0; i < l; i++) {
            coeffs[i] = 0;
            for (int j = 0; j < length; j++) {
                if (j+i != length-1)
                    coeffs[i] += ( p0[j] * p1[(j+i+1)%(length+1)] * ((j+i+1>length)? -1 : 1));
                if (j != i)
                    coeffs[i] -= ( p0[j] * p1[(j-i-1 < 0)? length+j-i : j-i-1]*((j-i-1<0)? -1 : 1) );
            }
            if (coeffs[i] != 0)
                return false;
        }
        return true;
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
    public boolean parallel(IntPoint p) {
        int l = length/2;
        int[] p0 = this.point;
        int[] p1 = p.point;
        // calculate the shoelace products
        int coeffs = 0;
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < length; j++) {
                if (j+i != length-1)
                    coeffs += ( p0[j] * p1[(j+i+1)%(length+1)] * ((j+i+1>length)? -1 : 1));
                if (j != i)
                    coeffs -= ( p0[j] * p1[(j-i-1 < 0)? length+j-i : j-i-1]*((j-i-1<0)? -1 : 1) );
            }
            if (coeffs != 0)
                return false;
        }
        return true;
    }

    /*
    * calculate the 2d cross-product of this with p, after 
    * they have both been projected down to the plane
    * using the standard projection (i.e., the ith
    * standard basis vector goes to (cos(i pi/N), sin(i pi/N))).
    *
    * WARNING: this only works for odd N right now.
    */
    public float crossProduct(IntPoint p) {
        int l = length/2;
        int[] p0 = this.point;
        int[] p1 = p.point;
        // here we store the shoelace products
        float coeffs = 0.0f;
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < length; j++) {
                if (j+i != length-1)
                    coeffs += COS_POWERS[i]*(p0[j] * p1[(j+i+1)%(length+1)] * ((j+i+1>length)? -1 : 1));
                if (j != i)
                    coeffs -= COS_POWERS[i]*(p0[j] * p1[(j-i-1 < 0)? length+j-i : j-i-1] * ((j-i-1<0)? -1 : 1));
            }
        }
        return coeffs;
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
    public float dotProduct(IntPoint p) {
        int l = length/2+1;
        int[] p0 = point;
        int[] p1 = p.point;
        
        // here we store the shoelace products
        float coeffs = 0.0f;
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < length; j++) {
                if (j+i != length)
                    coeffs += COS_POWERS[i]*( p0[j] *p1[(j+i)%(length+1)] * ((j+i>length)? -1 : 1) );
                if (i != 0 && j-i != -1)
                    coeffs += COS_POWERS[i]*( p0[j] *p1[(j-i < 0)? length+1+j-i : j-i] * ((j-i<0)? -1 : 1) );
            }
        }
        return coeffs;
    }

} // end of class IntPoint
