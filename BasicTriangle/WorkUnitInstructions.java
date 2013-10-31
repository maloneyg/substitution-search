import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.logging.*;
import com.google.common.collect.*;
import java.util.concurrent.atomic.*;

// this class is a wrapper containing instructions
// for the production of work units.
public class WorkUnitInstructions implements Serializable {

    // the edge breakdowns
    private final ImmutableList<Integer> BD0;
    private final ImmutableList<Integer> BD1;
    private final ImmutableList<Integer> BD2;

    // a boolean that tells us if o1 = o2 or o1 = -o2.
    // only relevant for isosceles triangles
    private final boolean flip;

    // the number of work units to be made
    private final int num;

    // private constructor
    private WorkUnitInstructions(ImmutableList<Integer> b0, ImmutableList<Integer> b1, ImmutableList<Integer> b2, boolean tf, int i) {
        BD0 = b0;
        BD1 = b1;
        BD2 = b2;
        flip = tf;
        num = i;
    } // constructor

    // getter methods
    public List<Integer> getZero() {
        return BD0;
    }

    public List<Integer> getOne() {
        return BD1;
    }

    public List<Integer> getTwo() {
        return BD2;
    }

    public boolean getFlip() {
        return flip;
    }

    public int getNum() {
        return num;
    }
    // end of getter methods

    // public static factory method
    public static WorkUnitInstructions createWorkUnitInstructions(ImmutableList<Integer> b0, ImmutableList<Integer> b1, ImmutableList<Integer> b2, boolean tf, int i) {
        return new WorkUnitInstructions(b0,b1,b2,tf,i);
    }

} // end of class WorkUnitInstructions
