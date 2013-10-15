import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.logging.*;
import com.google.common.collect.*;
import java.util.concurrent.atomic.*;

// this class represents a unit of work.
// calling get() on the Future for a WorkUnit returns a TestResult.
// the TestResult is just a String.
// the main thing that a MutableWorkUnit does is put completed 
// patches into completedPatches.
public class MutableWorkUnit implements WorkUnit, Serializable {

    // the main data on which MutableWorkUnit works
    private final MutablePatch patch;
    private final AtomicInteger count = new AtomicInteger(0);

    private static final ThreadService threadService;

    private static final Logger log;

    static { // initialize threadService, log, and completedPatches
        threadService = ThreadService.INSTANCE;
        log = threadService.getLogger();
    } // static initialization ends here

    // private constructor
    private MutableWorkUnit(MutablePatch p) {
        patch = p;
    }

    // public static factory method
    public static MutableWorkUnit createMutableWorkUnit(MutablePatch p) {
        return new MutableWorkUnit(p);
    }

    // this is the main method in MutableWorkUnit.
    // it produces the TestResult.
    public Result call() {
        threadService.getExecutor().registerCounter(count);
        patch.setCount(count);
        patch.solve();
        threadService.getExecutor().deregisterCounter(count);
        return new TestResult("completed " + patch.getNumCompleted() + " puzzles");
    } // method call() ends here

    public int getCount()
    {
        return count.get();
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

} // end of class MutableWorkUnit
