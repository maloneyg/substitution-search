/**
*    This class implements an angle.
*/

final public class BasicAngle implements AbstractAngle {

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

    // equals override.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicAngle angle = (BasicAngle) obj;
        return (this.a == angle.a);
    }

    // hashCode override.
    public int hashCode() {
        int prime = 37;
        int result = 5;
        result = prime*result + a;
        return result;
    }

    // return the angle as an integer.  
    protected int getAsInt() {
        return this.a;
    }

} // end of class BasicAngle
