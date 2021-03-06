/**
*    This class implements a point.
*    It uses a representation as a vector of integers.  
*    -- modified to use bytes to save memory
*/

import com.google.common.collect.*;
import com.google.common.base.*;
import com.google.common.cache.*;
import java.io.Serializable;
import java.lang.Math;

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

    public static final double[] COS_POWERS = Initializer.COS_LIST;
    public static final double[] SIN_POWERS = Initializer.SIN_LIST;

    // a threshold value that says when two points are too close
    public static final double TOO_CLOSE = 0.95;

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
        point = vector;
    }

    private BytePoint() {
        point = new byte[length];
    }

    // public static factory method 
    static public BytePoint createBytePoint(byte[] vector) {
        return new BytePoint(vector);
        //return POOL.getCanonicalVersion(vector);
    }

    // public static factory method
    static public BytePoint createBytePoint(BytePoint p, boolean flip, BasicAngle a, BytePoint shift) {
        byte[] vector = p.pointAsArray();
        if (flip) vector = REF.rowTimes(vector);
        vector = a.getRotation().rowTimes(vector);
        byte[] copyMe = shift.pointAsArray();
        for (int i = 0; i < vector.length; i++) vector[i] = (byte)(vector[i]+copyMe[i]);
        return new BytePoint(vector);
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

    // toString method for writing gap files.
    public String gapString() {
        String outString = "[";
        for (int i = 0; i < point.length - 1; i++) {
            outString = outString + point[i] + ",";
        }
        outString = outString + point[point.length-1] + "]";
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

    // return a vector with the same angle, but unit length
    // we assume that the BytePoint p has length l
    public static BytePoint unitize(BasicEdgeLength l, BytePoint p) {
        BasicAngle output = BasicAngle.createBasicAngle(0);
        for (int i = 0; i < 2*BasicAngle.ANGLE_SUM; i++) {
            output = BasicAngle.createBasicAngle(i);
            if (l.getAsVector(output).equals(p)) break;
        }
        return BasicEdgeLength.createBasicEdgeLength(0).getAsVector(output);
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
        byte[] result  = a.getRotation().rowTimes(pointAsArray());
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

    // return true if this is too close to p
    public boolean tooClose(BytePoint p) {
        BytePoint diff = subtract(p);
        return (diff.dotProduct(diff) < TOO_CLOSE);
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
    * If the cross-product isn't 0, return false.
    *
    * WARNING: this only works for prime N right now.
    * WARNING: this only works for odd N right now.
    */
    public boolean parallel(BytePoint p) {
        int l = length/2;
        byte[] p0 = this.point;
        byte[] p1 = p.point;
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
    public double crossProduct(BytePoint p) {
        int l = length/2;
        byte[] p0 = this.point;
        byte[] p1 = p.point;
        // here we store the shoelace products
        double coeffs = 0.0f;
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < length; j++) {
                if (j+i != length-1)
//                    coeffs += COS_POWERS[i]*(p0[j] * p1[(j+i+1)%(length+1)] * ((j+i+1>length)? -1 : 1));
                    coeffs += SIN_POWERS[i]*(p0[j] * p1[(j+i+1)%(length+1)] * ((j+i+1>length)? -1 : 1));
                if (j != i)
//                    coeffs -= COS_POWERS[i]*(p0[j] * p1[(j-i-1 < 0)? length+j-i : j-i-1] * ((j-i-1<0)? -1 : 1));
                    coeffs -= SIN_POWERS[i]*(p0[j] * p1[(j-i-1 < 0)? length+j-i : j-i-1] * ((j-i-1<0)? -1 : 1));
            }
        }
        return coeffs;
    }

    /*
    * a test version of the cross product
    */
    public double testCross(BytePoint p) {
        int l = length/2;
        byte[] p0 = this.point;
        byte[] p1 = p.point;
        // here we store the shoelace products
        double coeffs = 0.0;
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < length; j++) {
                if (j != i)
                    coeffs += SIN_POWERS[((Math.abs(i-j)<l+1)? Math.abs(i-j) : length + 1 - Math.abs(i-j))-1]*(p0[j] * p1[i] * ((j<i)? -1 : 1));
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
    public double dotProduct(BytePoint p) {
        int l = length/2+1;
        byte[] p0 = point;
        byte[] p1 = p.point;
        
        // here we store the shoelace products
        double coeffs = 0.0f;
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

} // end of class BytePoint
