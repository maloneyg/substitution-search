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
        private static final int MAX_SIZE = Preinitializer.SPAWN_MAX_SIZE; // don't spawn any more units if the queue is bigger than this

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
    private AtomicInteger count = new AtomicInteger(0); // keeps track of solve calls
    private AtomicBoolean die;

    public static final AtomicInteger IDgenerator = new AtomicInteger(0);
    public final int uniqueID = IDgenerator.incrementAndGet();

    public int uniqueID()
    {
        return uniqueID;
    }

    // required data to get all patches from the descendents of the initial work units
    private transient final EmptyBoundaryWorkUnit initialWorkUnit; // if this is not an initial work unit, this points to this unit's eventual ancestor
    private List<ImmutablePatch> eventualPatches; // only exists in initial work units; stores all patch results from descendents 
    
    private static final int KILL_TIME = Preinitializer.SPAWN_MIN_TIME; // in ms, how long to wait before killing a work unit and spawning more

    private static final ThreadService threadService;
    private static final Logger log;

    static { // initialize threadService, log, and completedPatches
        threadService = ThreadService.INSTANCE;
        log = threadService.getLogger();
    } // static initialization ends here

    // private constructors
    
    // create an initial work unit
    private EmptyBoundaryWorkUnit(EmptyBoundaryPatch patch, AtomicBoolean die) {
        this.patch = patch;
        this.die = die;
        initialWorkUnit = this;
        
        if ( Preinitializer.MAIN_CLASS_NAME.equals("Server") )
            eventualPatches = Server.completedPatches;
        else
            eventualPatches = new LinkedList<ImmutablePatch>();
    }

    // create a descendent work unit
    private EmptyBoundaryWorkUnit(EmptyBoundaryPatch patch, AtomicBoolean die, EmptyBoundaryWorkUnit parentUnit) {
        this.patch = patch;
        this.die = die;
        if ( parentUnit == null )
            throw new IllegalArgumentException("parent unit must not be null");
        initialWorkUnit = parentUnit.initialWorkUnit;
        eventualPatches = parentUnit.eventualPatches;
    }

    public List<ImmutablePatch> getEventualPatches()
    {
        return eventualPatches;
    }

    public void newEventualPatches()
    {
        eventualPatches = new LinkedList<ImmutablePatch>();
    }

    public void serverEventualPatches()
    {
         eventualPatches = Server.completedPatches;
    }

    public int hashCode()
    {
        int hash = 0;
        // avoid self-referential hashcode calls
        // synchronization is necessary because calling hashCode() on eventualPatches
        // will result an iteration over the collection; if the content of eventualPatches changes during
        // iteration, a ConcurrentModificationException will be thrown
        // I've checked and hashCode() is not called that frequently, so this should not be a huge performance problem
        synchronized(eventualPatches)
            {
                if ( initialWorkUnit == this )
                    hash = Objects.hash(patch, count, die, eventualPatches);
                else
                    hash = Objects.hash(patch, count, die, initialWorkUnit, eventualPatches);
            }
        return hash;
    }

    // public static factory method
    public static EmptyBoundaryWorkUnit createEmptyBoundaryWorkUnit(EmptyBoundaryPatch p, AtomicBoolean die) {
        // ensures all externally created work units are marked as initial
        return new EmptyBoundaryWorkUnit(p,die);
    }

    // this is the main method in EmptyBoundaryWorkUnit.
    // it produces the TestResult.

    public Result call() {
        //System.out.println("running job ID " + uniqueID);
        threadService.getExecutor().registerCounter(count);
        patch.setCount(count);
        
        Timer timer = null;
        if ( ! Preinitializer.MAIN_CLASS_NAME.equals("Client") )
            {
                timer = new Timer();
                timer.schedule(new KillSignal(die,timer), KILL_TIME, KILL_TIME);
            }
        
        List<EmptyBoundaryPatch> descendents = patch.solve();
        threadService.getExecutor().deregisterCounter(count);
        
        if ( timer != null )
            {
                timer.cancel(); //Terminate the timer thread
                timer = null;
            }

        // update eventual ancestor's list of results
        synchronized ( eventualPatches )
            {
                eventualPatches.addAll( patch.getLocalCompletedPatches() );
            }

        for (EmptyBoundaryPatch p : descendents) {
            AtomicBoolean kill = new AtomicBoolean();
            p.setKillSwitch(kill);
            EmptyBoundaryWorkUnit spawnedUnit = new EmptyBoundaryWorkUnit(p,kill,this);
            executorService.getExecutor().submit(spawnedUnit);
            //System.out.println("Spawned a new unit " + spawnedUnit.hashCode());
        }
        
        
        if ( descendents.size() > 0 )
            System.out.println("\nWork unit " + this.hashCode() + " spawned " + descendents.size() + " more units.");
        
        EmptyWorkUnitResult thisResult = new EmptyWorkUnitResult(this.hashCode(), patch.getLocalCompletedPatches(), eventualPatches);
        thisResult.setUniqueID(uniqueID);

        if ( patch.getLocalCompletedPatches().size() > 0 )
            System.out.println("\n" + thisResult.toString());

        return thisResult;
    } // method call() ends here

    public int getCount()
    {
        return count.get();
    }

    public void setCounter(AtomicInteger count)
    {
        this.count = count;
    }

    public void setResultTarget(List<ImmutablePatch> eventualPatches)
    {
        this.eventualPatches = eventualPatches;
    }

    public EmptyBoundaryPatch getPatch()
    {
        return patch;
    }

} // end of class EmptyBoundaryWorkUnit
