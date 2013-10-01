import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.logging.*;
import com.google.common.collect.*;

// this class represents a unit of work.
// calling get() on the Future for a WorkUnit returns a TestResult.
// the TestResult is just a String.
// the main thing that a BasicWorkUnit does is put completed 
// patches into completedPatches.
public class BasicWorkUnit implements WorkUnit, Serializable {

    // the main data on which BasicWorkUnit works
    private final BasicPatch patch;

    // the edge breakdown used to initialize patch.  
    // remains inert; gets passed down to subsequent BasicWorkUnits
    private final ImmutableList<ImmutableList<Integer>> edgeBreakdown;

    // a list of tiles that haven't been placed yet.
    // after placing a tile, create a new list without it
    // and pass that new list to the next BasicWorkUnit.
    private final PrototileList availableTiles;

    private static final ThreadService threadService;

    private static final Logger log;

    private static final ConcurrentHashMap<BasicPatch, ImmutableList<ImmutableList<Integer>>> completedPatches;

    static { // initialize threadService, log, and completedPatches
        threadService = ThreadService.INSTANCE;
        log = threadService.getLogger();
        HashMap<BasicPatch, ImmutableList<ImmutableList<Integer>>> temp = new HashMap<>();
        completedPatches = new ConcurrentHashMap<>(temp);
    } // static initialization ends here

    // private constructor
    private BasicWorkUnit(BasicPatch p, ImmutableList<ImmutableList<Integer>> b, PrototileList l) {
        patch = p;
        edgeBreakdown = b;
        availableTiles = l;
    }

    // public static factory method
    public static BasicWorkUnit createBasicWorkUnit(BasicPatch p, ImmutableList<ImmutableList<Integer>> b, PrototileList l) {
        return new BasicWorkUnit(p, b, l);
    }

    // return the results of the search
    public static ConcurrentHashMap<BasicPatch, ImmutableList<ImmutableList<Integer>>> output() {
        return completedPatches;
    }

    // this is the main method in BasicWorkUnit.
    // it produces the TestResult.
    public Result call() {
        ThreadService.INSTANCE.getExecutor().incrementNumberOfRunningJobs();

//        System.out.println("doing a BasicWorkUnit with " + availableTiles.size() + " tiles.");
        if (availableTiles.size() == 0) {
            log.log(Level.INFO, Thread.currentThread().getName() + " found a hit while working on BasicWorkUnit " + hashCode());
            // presumably we'll eventually want to return a BasicPatch as part of this result instead.  So this next line is temporary.
            completedPatches.put(patch,edgeBreakdown);
            return TestResult.JOB_COMPLETE;
        } else {
            // log.log(Level.INFO, Thread.currentThread().getName() + " BasicWorkUnit " + hashCode() + " doing work");
        }
        int numPlaced = 0;
        BasicEdge nextEdge = patch.getNextEdge(); // the edge we try to cover
        ImmutableSet<Orientation> equivalenceClass = patch.getEquivalenceClass(nextEdge.getOrientation()); // the Orientations declared to be equivalent to the Orientation of nextEdge
        for (BasicPrototile p : BasicPrototile.ALL_PROTOTILES) {
            if (availableTiles.contains(p)) { 
                // the geometric work happens here.
                // we try to place p, and 
                // outcome is a String that tells us whether
                // or not we were successful.
                if (p.compatible(nextEdge.getLength()))
                    numPlaced += tryAndPlace(p, nextEdge, equivalenceClass);
                if (Thread.interrupted()) {
                    log.log(Level.WARNING, Thread.currentThread().getName() + " was interrupted while working on BasicWorkUnit " + hashCode());
                    return TestResult.JOB_INTERRUPTED;
                }
            }
        }
        //log.log(Level.INFO, Thread.currentThread().getName() + " finished work on BasicWorkUnit " + hashCode() + "; placed " + numPlaced + " tiles");
        ThreadService.INSTANCE.getExecutor().decrementNumberOfRunningJobs();
        return new TestResult("placed " + numPlaced + " tiles");
    } // method call() ends here

    // here we go
    // We try and place the prototile p in this configuration on open edge e.
    // The method returns the number of successful placements.
    private int tryAndPlace(BasicPrototile p, BasicEdge e, ImmutableSet<Orientation> o) {
        int numPlaced = 0;
        BasicWorkUnit nextUnit;
        ImmutableList<BasicTriangle> triangles = p.placements(e, o);
        for (BasicTriangle t : triangles) {
            if (patch.compatible(t)) {
                nextUnit = new BasicWorkUnit(patch.placeTriangle(t),edgeBreakdown,availableTiles.remove(p));
                checkIfBusy();
                threadService.getExecutor().submit(nextUnit);
                numPlaced++;
            }
        }
        return numPlaced;
    }

    // pause if the queue is getting too full
    private static void checkIfBusy()
    {
        /*ThreadService executorService = ThreadService.INSTANCE;
        int THRESHOLD = 100;
        while (true)
            {
                int queueSize = executorService.getExecutor().getQueue().size();
                if ( queueSize > THRESHOLD )
                    {
                        try
                            {
                                Thread.sleep(1*1000);
                            }
                        catch (InterruptedException e)
                            {
                                continue;
                            }
                    }
                else
                    break;
            }*/
    }

    public String toString()
    {
        return "job " + hashCode();
    }

    // check if this is a dud
    public boolean dud() {
        if (availableTiles.size() == 0) return false;
        BasicEdge nextEdge = patch.getNextEdge(); // the edge we try to cover
        ImmutableSet<Orientation> equivalenceClass = patch.getEquivalenceClass(nextEdge.getOrientation()); // the Orientations declared to be equivalent to the Orientation of nextEdge
        for (BasicPrototile p : BasicPrototile.ALL_PROTOTILES) {
            if (availableTiles.contains(p)) { 
                // the geometric work happens here.
                // we try to place p, and 
                // if we can we return false
                if (p.compatible(nextEdge.getLength())) {
                    ImmutableList<BasicTriangle> triangles = p.placements(nextEdge, equivalenceClass);
                    for (BasicTriangle t : triangles) {
                        if (patch.compatible(t)) return false;
                    }
                }
            }
        }
        return true; // if we couldn't place anything, it's a dud
    } // method dud() ends here

} // end of class BasicWorkUnit
