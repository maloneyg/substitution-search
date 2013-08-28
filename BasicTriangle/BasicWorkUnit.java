import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.logging.*;
import com.google.common.collect.*;

// this class represents a unit of work
// calling get() on the Future for a WorkUnit returns a Result
public class BasicWorkUnit implements Callable<Result>, Serializable {

    private final BasicPatch patch;

    private final ImmutableList<BasicPrototile> availableTiles;

    private final ThreadPoolExecutor localThreadService;

    private final Logger log;

    private final int ID;

    // private constructor
    private BasicWorkUnit(BasicPatch p, ImmutableList<BasicPrototile> l, int i, ThreadPoolExecutor t, Logger log) {
        patch = p;
        availableTiles = l;
        ID = i;
        localThreadService = t;
        this.log = log;
    }

    // public static factory method
    public static createBasicWorkUnit(BasicPatch p, ImmutableList<BasicPrototile> l, int i, ThreadPoolExecutor t, Logger log) {
        return new BasicWorkUnit(p, l, i, t, log);
    }

    // this is the main method in BasicWorkUnit.
    // it produces the Result.
    public Result call() {
        if (availableTiles.size() == 0) {
            log.log(Level.INFO, Thread.currentThread().getName() + " found a hit");
            // presumably we'll eventually want to return a BasicPatch as part of this result instead.  So this next line is temporary.
            return new Result("finished unit " + ID + " with a hit.");
        } else {
            log.log(Level.INFO, Thread.currentThread().getName() + " doing work");
        }
        try {
            String outcome = "WorkUnit " + ID + ":\n"; // add to this to get Result
            BasicEdge nextEdge = patch.getNextEdge(); // the edge we try to cover
            ImmutableSet<Orientation> equivalenceClass = partition.getEquivalenceClass(nextEdge.getOrientation()); // the Orientations declared to be equivalent to the Orientation of nextEdge
            for (BasicPrototile p : BasicPrototile.ALL_PROTOTILES) {
                if (availableTiles.contains(p)) { 
                    // the geometric work happens here.
                    // we try to place p, and 
                    // outcome is a String that tells us whether
                    // or not we were successful.
                    if (p.compatible(e.getLength()))
                        outcome += tryAndPlace(p, nextEdge, equivalenceClass) + "\n";
                }
            }

            }
            log.log(Level.INFO, Thread.currentThread().getName() + " finished work (" + ID + ")");
            // temporary. We might want to return more than
            // just a String in this Result.
            return new Result(outcome + "finished unit " + ID);
        } catch (InterruptedException e) {
            log.log(Level.WARNING, Thread.currentThread().getName() + " has been interrupted");
        }
            return Result.JOB_INTERRUPTED;
    } // method call() ends here

    // here we go
    private String tryAndPlace(BasicPrototile p, BasicEdge e, ImmutableSet<Orientation> o) {
        
    }

    public String toString()
    {
        return "job " + ID;
    }

} // end of class BasicWorkUnit
