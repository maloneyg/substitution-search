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

    static { // initialize ALL_ANGLES

        BasicAngle[] preAllAngles = new BasicAngle[2*ANGLE_SUM];
        for (int i = 0; i < 2*ANGLE_SUM; i++)
            preAllAngles[i] = new BasicAngle(i);
        ALL_ANGLES = ImmutableList.copyOf(preAllAngles);

    }

    // Constructor method.
    private BasicAngle(int i) {
        a = i % (2*ANGLE_SUM);
    }

    // static public factory method.
    public static BasicAngle createBasicAngle(int i) {
        return ALL_ANGLES.get(i % (2*ANGLE_SUM));
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
