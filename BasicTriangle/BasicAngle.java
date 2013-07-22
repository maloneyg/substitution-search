/**
*    This class implements an angle.
*/


final public class BasicAngle implements AbstractAngle<BasicAngle> {

    // Every angle is implemented as an integer.
    private final int a;

    // The angle sum is a constant, shared across all triangles.
    private final static int angleSum = 7;

    // Constructor method.
    private BasicAngle(int i) {
        a = i;
    }

    // public factory method.
    public BasicAngle createBasicAngle(int i) {
        return new BasicAngle(i);
    }

    // toString method.  
    public String toString() {
        return "" + a;
    }

    // equals method.  
    public boolean equals(BasicAngle angle) {
        return (this.a == angle.a);
    }



} // end of class BasicAngle
