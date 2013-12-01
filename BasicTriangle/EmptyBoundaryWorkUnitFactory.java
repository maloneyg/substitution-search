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

    // the seed for an OpenBoundaryPatch
    // it tells us the index in STARTERS to use
    private int starter = 0;

    static { // initialize starters
        start0 = Initializer.INFLATED_LENGTHS.getColumn(Initializer.acute(P0.getAngles()[0].getAsInt())-1);
        start1 = Initializer.INFLATED_LENGTHS.getColumn(Initializer.acute(P0.getAngles()[1].getAsInt())-1);
        start2 = Initializer.INFLATED_LENGTHS.getColumn(Initializer.acute(P0.getAngles()[2].getAsInt())-1);
        BasicAngle a = P0.getAngles()[1].piPlus();
        List<BasicEdge> preStarters = new ArrayList<>();
        for (int i = 0; i < BasicEdgeLength.ALL_EDGE_LENGTHS.size(); i++) {
            BasicEdgeLength l = BasicEdgeLength.ALL_EDGE_LENGTHS.get(i);
            Orientation o = l.getOrientation(0);
            if (start2.get(i) > 0) preStarters.add(BasicEdge.createBasicEdge(l,o,new BytePoint[] {bigVertices[0],bigVertices[0].add(l.getAsVector(a))}));
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
    private EmptyBoundaryWorkUnitFactory(int starter) {
        this.starter = starter;
    } // constructor

    // deep copy
    public EmptyBoundaryWorkUnitFactory deepCopy(int starter) {
        return new EmptyBoundaryWorkUnitFactory(starter);
    } // deep copy

    // iterate through instructions to make sure we don't hit the end.
    // then return the instructions, along with an int representing
    // the number of work units to be produced.  
    //
    // BATCH_SIZE: how many WorkUnits to try and make from this set of instructions
    // ID: a number identifynig how many instructions have been parceled out
    public EmptyBoundaryWorkUnitInstructions getInstructions(int BATCH_SIZE, int ID) {
        int j = 0;
        while (notDoneYet && j<BATCH_SIZE) {
            iterateEdgeBreakdown();
            j++;
        }
        return EmptyBoundaryWorkUnitInstructions.createEmptyBoundaryWorkUnitInstructions(starter,j,ID);
    }

    // follow a set of instructions, returning the resulting WorkUnits in a List
    public List<EmptyBoundaryWorkUnit> followInstructions(EmptyBoundaryWorkUnitInstructions i) {
        LinkedList<EmptyBoundaryWorkUnit> l = new LinkedList<EmptyBoundaryWorkUnit>();
        advanceToBreakdown(i.getStarter());
        for (int k = 0; k < i.getNum(); k++) {
            l.add(nextWorkUnit());
        }
        return l;
    }

    public void iterateEdgeBreakdown() {
        starter = (starter + 1) % STARTERS.size();
        if (starter==0) notDoneYet = false;
    }

    // advance to the work unit matching this edge breakdown
    private void advanceToBreakdown(int i) {
        starter = i;
    } 

    public EmptyBoundaryWorkUnit nextWorkUnit() {

        EmptyBoundaryPatch patch = EmptyBoundaryPatch.createEmptyBoundaryPatch(STARTERS.get(starter),bigVertices,tiles.dumpMutablePrototileList());

        iterateEdgeBreakdown();

        AtomicBoolean kill = new AtomicBoolean();
        patch.setKillSwitch(kill);

        return EmptyBoundaryWorkUnit.createEmptyBoundaryWorkUnit(patch,kill);

    }

    // public factory method
    public static EmptyBoundaryWorkUnitFactory createEmptyBoundaryWorkUnitFactory() {
        return new EmptyBoundaryWorkUnitFactory();
    }

} // end of class EmptyBoundaryWorkUnitFactory
