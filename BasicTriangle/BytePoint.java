/**
*    This class implements a point.
*    It uses a representation as a vector of integers.  
*    -- modified to use bytes to save memory
*/

import com.google.common.collect.*;
import com.google.common.base.*;
import com.google.common.cache.*;
import java.io.Serializable;

final public class BytePoint implements AbstractPoint<BytePoint, BasicAngle>, Serializable {

    // make is Serializable
    static final long serialVersionUID = -6462075103242603792L;

    // static variables for all points.
    public static final int length = Initializer.N - 1;

    public static final ByteMatrix A = Initializer.A;

    public static final ByteMatrix ROT = Initializer.ROT;

    public static final ByteMatrix REF = Initializer.REF;

    public static final ByteMatrix INFL = Initializer.INFL;

    public static final BytePoint ZERO_VECTOR;

    public static final BytePoint UNIT_VECTOR;

    // a pool containing all the BytePoints that have been created
    //private static final BytePointPool POOL = BytePointPool.getInstance();

    static { // initialize the unit vector

        byte[] preUnit = new byte[length];
        byte[] preZero = new byte[length];
        preUnit[0] = (byte)1;
        preZero[0] = (byte)0;
        for (int i = 1; i < length; i++) {
            preUnit[i] = (byte)0;
            preZero[i] = (byte)0;
        }
        UNIT_VECTOR = createBytePoint(preUnit);
        ZERO_VECTOR = createBytePoint(preZero);

    }

    // A vector identifying the point.  
    private final byte[] point;

    // Constructor methods.

    private BytePoint(byte[] vector) {
        if (vector.length != length) {
            throw new IllegalArgumentException("Point length is incorrect.");
        }
        point = vector;
    }

    private BytePoint() {
        point = new byte[length];
    }

    // public static factory method for getting a recycled point
    static public BytePoint createBytePoint(byte[] vector) {
        return new BytePoint(vector);
        //return POOL.getCanonicalVersion(vector);
    }

    // public static factory method for creating a brand new point
    //static protected BytePoint createExNihilo(int[] vector) {
   //     return new BytePoint(vector);
    //}

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
        BytePoint p = (BytePoint) obj;
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
            result = prime*result + (int)point[i];
        }
        return result;
    }

    // return a deep copy of the contents of this BytePoint
    protected byte[] pointAsArray() {
        byte[] newArray = new byte[length];
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
    public BytePoint add(BytePoint p) {
        byte[] q = new byte[length];
        for (int i = 0; i < length; i++) {
            q[i] = (byte)(point[i] + p.point[i]);
        }
        return createBytePoint(q);
    }

    public BytePoint scalarMultiple(byte c) {
        byte[] q = new byte[length];
        for (int i = 0; i < length; i++) {
            q[i] = (byte)(c * point[i]);
        }
        return createBytePoint(q);
    }

    public BytePoint subtract(BytePoint p) {
        return this.add(p.scalarMultiple((byte)-1));
    }

    public BytePoint rotate(BasicAngle a) {
        int i = a.getAsInt();
        if (i < 0)
            throw new IllegalArgumentException("You must perform a positive number of rotations.");

        byte[] result  = pointAsArray();
        for (int j = 0; j < i; j++)
            result = ROT.rowTimes(result);
        return createBytePoint(result);
    }

    public BytePoint reflect() {
        return createBytePoint(REF.rowTimes(this.pointAsArray()));
    }

    public BytePoint inflate() {
        return createBytePoint(INFL.rowTimes(this.pointAsArray()));
    }

    protected BytePoint timesA() {
        return createBytePoint(A.rowTimes(this.pointAsArray()));
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
    public boolean colinear(BytePoint p) {
        int l = length/2;
        byte[] p0 = this.point;
        byte[] p1 = p.point;
        // here we store the shoelace products
        byte[] coeffs = new byte[l];
        for (int i = 0; i < l; i++) {
            coeffs[i] = (byte)0;
            for (int j = 0; j < length; j++) {
                if (j+i != length-1)
                    coeffs[i] += (byte)( p0[j] * p1[(j+i+1)%(length+1)] * ((j+i+1>length)? -1 : 1));
                if (j != i)
                    coeffs[i] -= (byte)( p0[j] * p1[(j-i-1 < 0)? length+j-i : j-i-1]*((j-i-1<0)? -1 : 1) );
            }
            if (coeffs[i] != (byte)0)
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
    * the output is a polynomial P such that the cross product is
    * sin(x) * P(cos(x)).
    * so if we just want to compare signs of cross products
    * we can just plug in COS and ignore the factor of sin(x).
    *
    * WARNING: this only works for odd N right now.
    */
    public ShortPolynomial crossProduct(BytePoint p) {
        int l = length/2;
        byte[] p0 = this.point;
        byte[] p1 = p.point;
        // here we store the shoelace products
        byte[] coeffs = new byte[l];
        for (int i = 0; i < l; i++) {
            coeffs[i] = (byte)0;
            for (int j = 0; j < length; j++) {
                if (j+i != length-1)
                    coeffs[i] += (byte)(p0[j] * p1[(j+i+1)%(length+1)] * ((j+i+1>length)? -1 : 1));
                if (j != i)
                    coeffs[i] -= (byte)(p0[j] * p1[(j-i-1 < 0)? length+j-i : j-i-1] * ((j-i-1<0)? -1 : 1));
            }
        }
        ShortPolynomial output = ShortPolynomial.ZERO;
        for (int i = 0; i < l; i++)
            output = output.plus(LengthAndAreaCalculator.SIN_LIST.get(i).scalarMultiple((short)coeffs[i]));
        return output;
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
    public ShortPolynomial dotProduct(BytePoint p) {
        int l = length/2+1;
        byte[] p0 = point;
        byte[] p1 = p.point;
        
        // here we store the shoelace products
        byte[] coeffs = new byte[l];
        for (int i = 0; i < l; i++) {
            coeffs[i] = (byte)0;
            for (int j = 0; j < length; j++) {
                if (j+i != length)
                    coeffs[i] += (byte)( p0[j] *p1[(j+i)%(length+1)] * ((j+i>length)? -1 : 1) );
                if (i != 0 && j-i != -1)
                    coeffs[i] += (byte)( p0[j] *p1[(j-i < 0)? length+1+j-i : j-i] * ((j-i<0)? -1 : 1) );
            }
        }
        ShortPolynomial output = ShortPolynomial.ZERO;
        for (int i = 0; i < l; i++)
            output = output.plus(LengthAndAreaCalculator.COS_LIST.get(i).scalarMultiple((short)coeffs[i]));
        return output;
    }

} // end of class BytePoint
