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

    // the number of the triangle we're searching
    private static final int myTile = Preinitializer.MY_TILE;
    
    // the triangle we're searching
    private static final BasicPrototile P0 = BasicPrototile.createBasicPrototile(Preinitializer.PROTOTILES.get(myTile));
    
    // the numbers of the different prototiles that fit in INFL.P0
    private static final PrototileList tiles = PrototileList.createPrototileList(BasicPrototile.getPrototileList(Initializer.SUBSTITUTION_MATRIX.getColumn(myTile)));
    
    // vertices of INFL.P0
    private static final AbstractPoint[] vertices = P0.place(Preinitializer.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false).getVertices();
    private static final AbstractPoint[] bigVertices = new AbstractPoint[] {vertices[0].inflate(),vertices[1].inflate(),vertices[2].inflate()};
    
    // iterators for producing new edge breakdowns
    private static MultiSetLinkedList edge0;
    private static MultiSetLinkedList edge1;
    private static MultiSetLinkedList edge2;
    
    // the starting edge breakdowns
    private static final ImmutableList<Integer> start0;
    private static final ImmutableList<Integer> start1;
    private static final ImmutableList<Integer> start2;
    
    // the starting edge breakdowns of P0
    private static ImmutableList<Integer> BD0;
    private static ImmutableList<Integer> BD1;
    private static ImmutableList<Integer> BD2;
    
    // two Orientations to be identified.
    // only relevant if P0 is isosceles.
    private static final Orientation o1;
    private static final Orientation o2;
    
    // a boolean that tells us if o1 = o2 or o1 = -o2.
    // only relevant if P0 is isosceles.
    private static boolean flip = false;

    // true if we haven't created all edge breakdowns yet
    public static boolean notDoneYet = true;

    static { // initialize the edge breakdown iterators
        ImmutableList<Integer> first0 = P0.getLengths()[0].getBreakdown();
        ImmutableList<Integer> first1 = P0.getLengths()[1].getBreakdown();
        ImmutableList<Integer> first2 = P0.getLengths()[2].getBreakdown();
        edge0 = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList<Integer>(first0));
        edge1 = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList<Integer>(first1));
        edge2 = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList<Integer>(first2));
        start0 = edge0.getImmutableList();
        start1 = edge1.getImmutableList();
        start2 = edge2.getImmutableList();
        BD0 = start0;
        BD1 = start1;
        BD2 = start2;
    } // initialization of edge breakdown iterators ends here

    static { // initialize o1 and o2
        BasicAngle[] a = P0.getAngles();
        Orientation[] o = P0.getOrientations();
        // we want to identify the Orientations on the two equal edges.
        // to do that we need to know which Orientations those are.
        o1 = o[1];
        if (a[1].equals(a[0])) {
            o2 = o[0];
        } else {
            o2 = o[2];
        }
    } // initialization of o1 and o2 ends here

    private static void iterateEdgeBreakdown() {
        edge0.iterate();
        BD0 = edge0.getImmutableList();
        if (BD0.equals(start0)) {
            if (P0.isosceles()) { // then we only need two breakdowns
                edge2.iterate();
                BD2 = edge2.getImmutableList();
                if (BD2.equals(start2)) notDoneYet = false;
            } else { // if it's not isosceles, use all three breakdowns
                edge1.iterate();
                BD1 = edge1.getImmutableList();
                if (BD1.equals(start1)) {
                    edge2.iterate();
                    BD2 = edge2.getImmutableList();
                    if (BD2.equals(start2)) notDoneYet = false;
                }
            }
        }
    }

    public static MutableWorkUnit nextWorkUnit() {

        if (P0.isosceles()) {
        // how we submit BasicWorkUnits
        // depends on whether P0 is isosceles.

            BasicEdge[] edgeList = P0.createSkeleton(BD0, BD2, flip);
            MutablePatch patch = MutablePatch.createMutablePatch(edgeList,bigVertices,tiles.dumpMutablePrototileList());
            // identify the Orientations on the two equal edges
            if (flip) {
                patch.addInstructions(o1,o2.getOpposite());
            } else {
                patch.addInstructions(o1,o2);
            }

            if (flip) iterateEdgeBreakdown();
            flip = !flip;

            // create a new unit of work
            return MutableWorkUnit.createMutableWorkUnit(patch);

        } else {

            BasicEdge[] edgeList = P0.createSkeleton(BD0, BD1, BD2);

            MutablePatch patch = MutablePatch.createMutablePatch(edgeList,bigVertices,tiles.dumpMutablePrototileList());

            iterateEdgeBreakdown();

            return MutableWorkUnit.createMutableWorkUnit(patch);

        } // end of BasicWorkUnit submissions
    }

    // the main data on which MutableWorkUnit works
    private final MutablePatch patch;
    private final AtomicInteger count = new AtomicInteger(0);
    private int originalHashCode = -1;

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

    public int getOriginalHashCode()
    {
        return originalHashCode;
    }

    public void setOriginalHashCode(int originalHashCode)
    {
        this.originalHashCode = originalHashCode;
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
        System.out.println("finished work unit " + originalHashCode);
        threadService.getExecutor().deregisterCounter(count);
        PatchResult thisResult = new PatchResult(this);
        MutableParadigmClient.sendResult(thisResult);
        return thisResult;
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

    public String getID()
    {
        return "job " + hashCode();
    }

    // destroy this method! It is unsafe
    public MutablePatch getPatch()
    {
        return patch;
    }

    public static void main(String[] args) {

        for (Integer i : BD0) System.out.print(i + " ");
        System.out.println();
        for (Integer i : BD1) System.out.print(i + " ");
        System.out.println();
        for (Integer i : BD2) System.out.print(i + " ");
        System.out.println();
        System.out.println(2*180*1260);

    }

} // end of class MutableWorkUnit
