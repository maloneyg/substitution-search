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

    // a unique identifier
    private final int ID;

    // private constructor
    private WorkUnitInstructions(ImmutableList<Integer> b0, ImmutableList<Integer> b1, ImmutableList<Integer> b2, boolean tf, int i, int ID) {
        BD0 = b0;
        BD1 = b1;
        BD2 = b2;
        flip = tf;
        num = i;
        this.ID = ID;
    } // constructor

    public String toString()
    {
        return BD0.toString() + " / " + BD1.toString() + " / " + BD2.toString() + " / " + flip + " / " + num + " / " + flip;
    }

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

    public int getID() {
        return ID;
    }
    // end of getter methods

    public int hashCode()
    {
        return Objects.hash(BD0, BD1, BD2);
    }

    // public static factory method
    public static WorkUnitInstructions createWorkUnitInstructions(ImmutableList<Integer> b0, ImmutableList<Integer> b1, ImmutableList<Integer> b2, boolean tf, int i, int ID) {
        return new WorkUnitInstructions(b0,b1,b2,tf,i,ID);
    }

} // end of class WorkUnitInstructions
