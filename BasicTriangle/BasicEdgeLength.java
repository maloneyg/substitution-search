/**
*    This class implements an edge length.
*/

import java.lang.Math.*;
import com.google.common.collect.*;
import com.google.common.base.*;

final public class BasicEdgeLength implements AbstractEdgeLength<BasicAngle, BasicPoint, BasicEdgeLength> {

    private static final int N = Initializer.N;

    /*
    * A list of enums representing the allowable edge lengths.
    * This list comes from Initializer.
    */
    static final private ImmutableList<Initializer.EDGE_LENGTH> LENGTHS = Initializer.LENGTHS;

    /*
    * A list of all possible BasicEdgeLength objects.
    * When someone asks for a BasicEdgeLength, we just give 
    * his one of these.
    */
    static final private ImmutableList<BasicEdgeLength> ALL_EDGE_LENGTHS;

    /*
    * A list of vector representatives of the allowable edge lengths.
    * Each one starts at the origin and lies on the positive x-axis.
    */
    static final private ImmutableList<BasicPoint> REPS;

    static { // initialize REPS. Use recursion.
        BasicPoint[] preReps = new BasicPoint[Math.max(2,LENGTHS.size())];
        preReps[0] = BasicPoint.UNIT_VECTOR;
        preReps[1] = BasicPoint.UNIT_VECTOR.timesA();
        for (int i = 2; i < preReps.length; i++)
            preReps[i] = preReps[i-1].timesA().subtract(preReps[i-2]);
        REPS = ImmutableList.copyOf(preReps);

        // initialize ALL_EDGE_LENGTHS.
        BasicEdgeLength[] preAllEdgeLengths = new BasicEdgeLength[LENGTHS.size()];
        for (int j = 0; j < LENGTHS.size(); j++)
            preAllEdgeLengths[j] = new BasicEdgeLength(j);
        ALL_EDGE_LENGTHS = ImmutableList.copyOf(preAllEdgeLengths);
    }

    /*
    * The type of this edge length.
    * It is one of the elements of LENGTHS.
    */
    final private Initializer.EDGE_LENGTH length;

    /*
    * The vector representation of this edge length.
    * It is one of the vectors in REPS.
    */
    final private BasicPoint rep;

    // private constructor
    private BasicEdgeLength(int i) {
        rep = REPS.get(i);
        length = LENGTHS.get(i);
    }

    // public static factory method
    static public BasicEdgeLength createBasicEdgeLength(int i) {
        if (i < 0 || i > ALL_EDGE_LENGTHS.size()-1)
            throw new IllegalArgumentException("Incorrect edge length index.");
        return ALL_EDGE_LENGTHS.get(i);
    }

    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicEdgeLength l = (BasicEdgeLength) obj;
        return this.length.equals(l.length);
    }

    public int hashCode() {
        return length.hashCode();
    }

    public String toString() {
        return "Edge length " + rep;
    }

    /*
    * return a vector with length equal to this edge length.
    * The vector should lie on the positive x-axis, if that
    * notion makes any sense.
    */
    public BasicPoint getAsVector() {
        return rep;
    }

    /*
    * return the edge length opposite the given angle in a triangle.
    */
    public static BasicEdgeLength lengthOpposite(BasicAngle a) {
        int angleModN = a.getAsInt() % N;
        if (angleModN == 0 || angleModN == N)
            throw new IllegalArgumentException("There is no edge length opposite an angle of 0.");
        return createBasicEdgeLength(Math.min(angleModN, N - angleModN)- 1);
    }

    public static void main(String[] args) {


        System.out.println(Initializer.ROT);
        for (int i = 0; i < ALL_EDGE_LENGTHS.size(); i++) {
            System.out.println(ALL_EDGE_LENGTHS.get(i).getAsVector());
            System.out.println(ALL_EDGE_LENGTHS.get(i).getAsVector().rotate(BasicAngle.createBasicAngle(1)));
        }

    }

} // end of class BasicEdgeLength
