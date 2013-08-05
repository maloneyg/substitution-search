/**
*    This class implements an edge length.
*/

import java.lang.Math.*;
import com.google.common.collect.*;
import com.google.common.base.*;

final public class BasicEdgeLength implements AbstractEdgeLength<BasicPoint> {

    /*
    * A list of enums representing the allowable edge lengths.
    * This list comes from Initializer.
    */
    static final private ImmutableSet<Initializer.EDGE_LENGTH> LENGTHS = Initializer.LENGTHS;

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
        preReps[0] = BasicPoint.unitVector();
        preReps[1] = BasicPoint.unitVector().timesA();
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
        UnmodifiableIterator<Initializer.EDGE_LENGTH> itr = LENGTHS.iterator();
        Initializer.EDGE_LENGTH tempLength = itr.next();
        for (int j = 0; j < i; j++) {
            if (itr.hasNext()) {
                if (j == i)
                    tempLength = itr.next();
                else
                    itr.next();
            }
        }
        rep = REPS.get(i);
        length = tempLength;
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

    public BasicPoint getAsVector() {
        return rep;
    }

    public static void main(String[] args) {

        BasicEdgeLength EL0 = createBasicEdgeLength(0);
        BasicEdgeLength EL1 = createBasicEdgeLength(1);
        BasicEdgeLength EL2 = createBasicEdgeLength(2);
        System.out.println(EL0);
        System.out.println(EL1);
        System.out.println(EL2);

    }

} // end of class BasicEdgeLength
