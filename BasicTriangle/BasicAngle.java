/**
*    This class implements an angle.
*/

import com.google.common.collect.*;
import java.io.Serializable;

public final class BasicAngle implements AbstractAngle, Comparable<BasicAngle>, Serializable {

    // Every angle is implemented as an integer.
    private final int a;

    // The angle sum is a constant, shared across all triangles.
    public static final int ANGLE_SUM = Initializer.N;

    // All possible angles
    private static final ImmutableList<BasicAngle> ALL_ANGLES;

    // we need this to make it Serializable
    static final long serialVersionUID = -552924021062179242L;

    // the rotation matrix associated to this angle
    private final ByteMatrix rot;

    static { // initialize ALL_ANGLES

        BasicAngle[] preAllAngles = new BasicAngle[2*ANGLE_SUM];
        for (int i = 0; i < 2*ANGLE_SUM; i++)
            preAllAngles[i] = new BasicAngle(i);
        ALL_ANGLES = ImmutableList.copyOf(preAllAngles);

    }

    // Constructor method.
    private BasicAngle(int i) {
        int shift = (i < 0)?(2*ANGLE_SUM):0;
        a = i % (2*ANGLE_SUM) + shift;
        ByteMatrix preRot = ByteMatrix.identity(ANGLE_SUM-1);
        for (int k = 0; k < i; k++)
            preRot = preRot.times(Initializer.ROT);
        rot = preRot;
    }

    // static public factory method.
    public static BasicAngle createBasicAngle(int i) {
        int shift = (i < 0)?(2*ANGLE_SUM):0;
        return ALL_ANGLES.get(i % (2*ANGLE_SUM) + shift);
    }

    // compareTo
    public int compareTo(BasicAngle x) {
        return this.a - x.a;
    }

    // toString method.  
    public String toString() {
        return "" + a;
    }

    // equals override.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicAngle angle = (BasicAngle) obj;
        return (this.a == angle.a);
    }

    // hashCode override.
    public int hashCode() {
        return a;
    }

    // return the angle as an integer.  
    protected int getAsInt() {
        return this.a;
    }

    // return the rotation matrix associated to this angle
    public ByteMatrix getRotation() {
        return this.rot;
    }

    // return this angle plus a
    public BasicAngle plus(BasicAngle other) {
        return createBasicAngle(this.a + other.a);
    }

    // return this angle minus a
    public BasicAngle minus(BasicAngle other) {
        return createBasicAngle(this.a - other.a);
    }

    // return pi minus this angle.
    protected BasicAngle supplement() {
        return createBasicAngle(ANGLE_SUM - (a % (2*ANGLE_SUM)));
    }

    // return pi plus this angle.
    protected BasicAngle piPlus() {
        return createBasicAngle(ANGLE_SUM + (a % (2*ANGLE_SUM)));
    }

    public static void main(String[] args) {

        BasicAngle A0 = createBasicAngle(0);
        BasicAngle A1 = createBasicAngle(5);
        BasicAngle A2 = createBasicAngle(73);
        System.out.println(A0);
        System.out.println(A1);
        System.out.println(A2);

    }

} // end of class BasicAngle
