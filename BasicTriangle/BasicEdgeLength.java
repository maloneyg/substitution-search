/**
*    This class implements an edge length.
*/

import java.lang.Math.*;
import com.google.common.collect.*;

final public class BasicEdgeLength implements AbstractEdgeLength<BasicPoint> {

    /*
    * A list of enums representing the allowable edge lengths.
    * This list comes from Initializer.
    */
    static final private ImmutableSet<Initializer.EDGE_LENGTH> LENGTHS = Initializer.LENGTHS;

    /*
    * A list of vector representatives of the allowable edge lengths.
    * Each one starts at the origin and lies on the positive x-axis.
    */
    static final private ImmutableList<BasicPoint> REPS;

    static { // initialize REPS. Use recursion.
        BasicPoint[] preReps = new BasicPoint[Math.max(2,LENGTHS.size())];
        preReps[0] = BasicPoint.unitVector();
        preReps[1] = BasicPoint.unitVector().inflate();
        for (int i = 2; i < preReps.length; i++)
            preReps[i] = preReps[i-1].inflate().subtract(preReps[i-2]);
        REPS = ImmutableList.copyOf(preReps);
    }

    // private constructor
    private BasicEdgeLength(int i) {
        UnmodifiableIterator<Initializer.EDGE_LENGTH> itr = LENGTHS.iterator();
        for (int j = 0; j < i+1; j++) {
            if (itr.hasNext()) {
                if (j == i)
                    length = itr.next();
                else
                    itr.next();
            }
        }
        rep = REPS.get(i);
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

    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicEdgeLength l = (BasicEdgeLength) obj;
        return this.length.equals(l.length);
    }

    public BasicPoint getAsVector() {
        return rep;
    }

} // end of class BasicEdgeLength
