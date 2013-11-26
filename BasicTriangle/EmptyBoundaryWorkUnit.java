import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.logging.*;
import com.google.common.collect.*;
import java.util.concurrent.atomic.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.*;

// this class represents a unit of work.
// calling get() on the Future for a WorkUnit returns a TestResult.
// the TestResult is just a String.
// the main thing that a EmptyBoundaryWorkUnit does is put completed 
// patches into completedPatches.
public class EmptyBoundaryWorkUnit implements WorkUnit, Serializable {

    class KillSignal extends TimerTask {

        private AtomicBoolean killSwitch;
        private Timer timer;
        private static final int MAX_SIZE = 500; // max size of queue to spawn more units

        public KillSignal(AtomicBoolean killSwitch, Timer timer) {
            this.killSwitch = killSwitch;
            this.timer = timer;
        }

        public void run() {
            if ( ThreadService.INSTANCE.getExecutor().getQueue().size() < MAX_SIZE )
                {
                    timer.cancel();                  
                    killSwitch.lazySet(true);
                }
        }
    }

    // the main data on which EmptyBoundaryWorkUnit works
    private static ThreadService executorService = ThreadService.INSTANCE;
    private final EmptyBoundaryPatch patch;
    private final AtomicInteger count = new AtomicInteger(0);
    private AtomicInteger counter;
    private List<ImmutablePatch> resultTarget;
    private AtomicBoolean die;
    private static final int KILL_TIME = 5000; // in ms, how long to wait before killing a work unit and spawning more

    private static final ThreadService threadService;

    private static final Logger log;

    static { // initialize threadService, log, and completedPatches
        threadService = ThreadService.INSTANCE;
        log = threadService.getLogger();
    } // static initialization ends here

    // private constructor
    private EmptyBoundaryWorkUnit(EmptyBoundaryPatch p, AtomicBoolean die) {
        patch = p;
        this.die = die;
    }

    public int hashCode()
    {
        return Objects.hash(patch, count, counter, resultTarget);
    }

    // public static factory method
    public static EmptyBoundaryWorkUnit createEmptyBoundaryWorkUnit(EmptyBoundaryPatch p, AtomicBoolean die) {
        return new EmptyBoundaryWorkUnit(p,die);
    }

    // this is the main method in EmptyBoundaryWorkUnit.
    // it produces the TestResult.
    public Result call() {
        threadService.getExecutor().registerCounter(count);
        patch.setCount(count);
        Timer timer = new Timer();
        timer.schedule(new KillSignal(die,timer), KILL_TIME);
        List<EmptyBoundaryPatch> descendents = patch.solve();
        threadService.getExecutor().deregisterCounter(count);

        if ( resultTarget != null )
            {
                synchronized(resultTarget)
                    {
                        resultTarget.addAll(patch.getLocalCompletedPatches());
                    }
                counter.getAndIncrement();
            }

        for (EmptyBoundaryPatch p : descendents) {
            AtomicBoolean kill = new AtomicBoolean();
            p.setKillSwitch(kill);
            EmptyBoundaryWorkUnit spawnedUnit = new EmptyBoundaryWorkUnit(p,kill);
            executorService.getExecutor().submit(spawnedUnit);
            System.out.println("Spawned a new unit " + spawnedUnit.hashCode());
        }

        EmptyWorkUnitResult thisResult = new EmptyWorkUnitResult(this.hashCode(), patch.getLocalCompletedPatches());
        System.out.println("\n" + thisResult);
        timer.cancel(); //Terminate the timer thread
        return thisResult;
    } // method call() ends here

    public void debugCall()
    {
        patch.solve();
        resultTarget.addAll(patch.getLocalCompletedPatches());
        counter.getAndIncrement();
    }

    public void setCounter(AtomicInteger counter)
    {
        this.counter = counter;
    }

    public void setResultTarget(List<ImmutablePatch> resultTarget)
    {
        this.resultTarget = resultTarget;
    }

    public int getCount()
    {
        return count.get();
    }

    public EmptyBoundaryPatch getPatch()
    {
        return patch;
    }

} // end of class EmptyBoundaryWorkUnit
