import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.logging.*;
import com.google.common.collect.*;
import java.util.concurrent.atomic.*;

// this class produces work units.
// you set it to an initial value, and then crank out as many 
// work units as you need.
public class EmptyBoundaryWorkUnitFactory implements Serializable {

    // the number of the triangle we're searching
    private static final int myTile = Preinitializer.MY_TILE;

    // the triangle we're searching
    private static final BasicPrototile P0 = BasicPrototile.createBasicPrototile(Preinitializer.PROTOTILES.get(myTile));

    // the numbers of the different prototiles that fit in INFL.P0
    private static final PrototileList tiles = PrototileList.createPrototileList(BasicPrototile.getPrototileList(Initializer.SUBSTITUTION_MATRIX.getColumn(myTile)));

    // vertices of INFL.P0
    private static final BytePoint[] vertices = P0.place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false).getVertices();
    private static final BytePoint[] bigVertices = new BytePoint[] {vertices[0].inflate(),vertices[1].inflate(),vertices[2].inflate()};

    // the starting edge breakdowns
    private static final ImmutableList<Integer> start0;
    private static final ImmutableList<Integer> start1;
    private static final ImmutableList<Integer> start2;

    // a list of starters
    private static final ImmutableList<BasicEdge> STARTERS;

    static { // initialize starters
        start0 = P0.getLengths()[0].getBreakdown();
        start1 = P0.getLengths()[1].getBreakdown();
        start2 = P0.getLengths()[2].getBreakdown();
        BasicAngle a = P0.getAngles[1].piPlus();
        List<BasicEdge> preStarters = new ArrayList<>();
        for (int i = 0; i < BasicEdgeLength.ALL_EDGE_LENGTHS.size(); i++) {
            BasicEdgeLength l = BasicEdgeLength.ALL_EDGE_LENGTHS.get(i);
            Orientation o = l.getOrientation(0);
            if (start2.get(i) > 0) preStarters.add(BasicEdge.createBasicEdge(l,o,new BytePoint[] {bigVertices[0],bigVertices[0].add(l.getAsVector(a.piPlus()))}));
        }
        STARTERS = ImmutableList.copyOf(preStarters);
    } // static initialization of starters ends here

    // true if we haven't created all edge breakdowns yet
    private boolean notDoneYet = true;

    public boolean notDone()
    {
        return notDoneYet;
    }

    // private constructor
    private EmptyBoundaryWorkUnitFactory() { // initialize the edge breakdown iterators

    } // constructor

    // private constructor
    private EmptyBoundaryWorkUnitFactory(ImmutableList<Integer> s0,ImmutableList<Integer> s1,ImmutableList<Integer> s2) { // initialize the edge breakdown iterators
    } // constructor

    // deep copy
    public EmptyBoundaryWorkUnitFactory deepCopy() {
        return new EmptyBoundaryWorkUnitFactory(start0,start1,start2);
    } // deep copy

    // iterate through instructions to make sure we don't hit the end.
    // then return the instructions, along with an int representing
    // the number of work units to be produced.  
    //
    // BATCH_SIZE: how many WorkUnits to try and make from this set of instructions
    // ID: a number identifynig how many instructions have been parceled out
    public WorkUnitInstructions getInstructions(int BATCH_SIZE, int ID) {
        ImmutableList<Integer> output0 = BD0;
        ImmutableList<Integer> output1 = BD1;
        ImmutableList<Integer> output2 = BD2;
        boolean reflect = flip;
        int j = 0;
        while (notDoneYet && j<BATCH_SIZE) {
            iterateEdgeBreakdown();
            j++;
        }
        return WorkUnitInstructions.createWorkUnitInstructions(output0,output1,output2,reflect,j,ID);
    }

    // follow a set of instructions, returning the resulting WorkUnits in a List
    public List<EmptyBoundaryWorkUnit> followInstructions(WorkUnitInstructions i) {
        LinkedList<EmptyBoundaryWorkUnit> l = new LinkedList<EmptyBoundaryWorkUnit>();
        //System.out.print("advancing to breakdown...");
        advanceToBreakdown(i.getZero(),i.getOne(),i.getTwo(),i.getFlip());
        //System.out.print("done...");
//        if (flip != i.getFlip())
//            iterateEdgeBreakdown();
        for (int k = 0; k < i.getNum(); k++) {
            l.add(nextWorkUnit());
//            iterateEdgeBreakdown();
        }
        return l;
    }

    private void iterateEdgeBreakdown() {
        if (P0.isosceles()) flip = !flip;
        if (flip==false) {
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
    }

    // advance to the work unit matching this edge breakdown
    private void advanceToBreakdown(List<Integer> b0, List<Integer> b1, List<Integer> b2, boolean f) {
        //while (notDoneYet) {
        while (true) {
            if (compareBreakdown(b0,b1,b2)) break;
            iterateEdgeBreakdown();
        }
        flip = f;
    } 

    private EmptyBoundaryWorkUnit nextWorkUnit() {

        EmptyBoundaryPatch patch = EmptyBoundaryPatch.createEmptyBoundaryPatch(starter,bigVertices,tiles.dumpMutablePrototileList());

        iterateEdgeBreakdown();

        return EmptyBoundaryWorkUnit.createEmptyBoundaryWorkUnit(patch);

    }

    // public factory method
    public static EmptyBoundaryWorkUnitFactory createEmptyBoundaryWorkUnitFactory() {
        return new EmptyBoundaryWorkUnitFactory();
    }

} // end of class EmptyBoundaryWorkUnitFactory
