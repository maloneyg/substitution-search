/**
*    This class implements an angle.
*/

public final class BasicAngle implements AbstractAngle, Comparable<BasicAngle> {

    // Every angle is implemented as an integer.
    private final int a;

    // The angle sum is a constant, shared across all triangles.
    private final static int angleSum = Initializer.N;

    // Constructor method.
    private BasicAngle(int i) {
        a = i % (2*angleSum);
    }

    // public factory method.
    public BasicAngle createBasicAngle(int i) {
        return new BasicAngle(i);
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

} // end of class BasicAngle
