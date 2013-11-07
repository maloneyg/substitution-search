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
public class WorkUnitFactory implements Serializable {

    // the number of the triangle we're searching
    private static final int myTile = Preinitializer.MY_TILE;
    
    // the triangle we're searching
    private static final BasicPrototile P0 = BasicPrototile.createBasicPrototile(Preinitializer.PROTOTILES.get(myTile));
    
    // the numbers of the different prototiles that fit in INFL.P0
    private static final PrototileList tiles = PrototileList.createPrototileList(BasicPrototile.getPrototileList(Initializer.SUBSTITUTION_MATRIX.getColumn(myTile)));
    
    // vertices of INFL.P0
    private static final BytePoint[] vertices = P0.place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false).getVertices();
    private static final BytePoint[] bigVertices = new BytePoint[] {vertices[0].inflate(),vertices[1].inflate(),vertices[2].inflate()};
    
    // iterators for producing new edge breakdowns
    private MultiSetLinkedList edge0;
    private MultiSetLinkedList edge1;
    private MultiSetLinkedList edge2;
    
    // the starting edge breakdowns
    private final ImmutableList<Integer> start0;
    private final ImmutableList<Integer> start1;
    private final ImmutableList<Integer> start2;
    
    // the starting edge breakdowns of P0
    private ImmutableList<Integer> BD0;
    private ImmutableList<Integer> BD1;
    private ImmutableList<Integer> BD2;
    
    // two Orientations to be identified.
    // only relevant if P0 is isosceles.
    private final Orientation o1;
    private final Orientation o2;
    
    // a boolean that tells us if o1 = o2 or o1 = -o2.
    // only relevant if P0 is isosceles.
    private boolean flip = false;

    // true if we haven't created all edge breakdowns yet
    private boolean notDoneYet = true;

    public boolean notDone()
    {
        return notDoneYet;
    }

    // private constructor
    private WorkUnitFactory() { // initialize the edge breakdown iterators
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

    } // constructor

    // private constructor
    private WorkUnitFactory(MultiSetLinkedList e0,MultiSetLinkedList e1,MultiSetLinkedList e2,ImmutableList<Integer> i0,ImmutableList<Integer> i1,ImmutableList<Integer> i2,ImmutableList<Integer> s0,ImmutableList<Integer> s1,ImmutableList<Integer> s2,Orientation oo1,Orientation oo2) { // initialize the edge breakdown iterators
        edge0 = e0;
        edge1 = e1;
        edge2 = e2;
        start0 = s0;
        start1 = s1;
        start2 = s2;
        BD0 = i0;
        BD1 = i1;
        BD2 = i2;
        o1 = oo1;
        o2 = oo2;
    } // constructor

    // deep copy
    public WorkUnitFactory deepCopy() {
        return new WorkUnitFactory(edge0.deepCopy(),edge1.deepCopy(),edge2.deepCopy(),BD0,BD1,BD2,start0,start1,start2,o1,o2);
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
    public List<MutableWorkUnit> followInstructions(WorkUnitInstructions i) {
        LinkedList<MutableWorkUnit> l = new LinkedList<MutableWorkUnit>();
        advanceToBreakdown(i.getZero(),i.getOne(),i.getTwo(),i.getFlip());
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

    // for debug purposes
    private String breakdownString() {
        String output = "";
        for (Integer i : BD0) output += i + " ";
        output += "\n";
        for (Integer i : BD1) output += i + " ";
        output += "\n";
        for (Integer i : BD2) output += i + " ";
        output += "\n";
        return output;
    }

    // compare a list of lists of integers to BD0, BD1, and BD2 for equality
    private boolean compareBreakdown(List<Integer> b0, List<Integer> b1, List<Integer> b2) {
        for (int i = 0; i < 3; i++) {
            List<Integer> intList = (i==0)? BD0 : ((i==1)? BD1 : BD2);
            List<Integer> nother = (i==0)? b0 : ((i==1)? b1 : b2);
            if (nother.size()!=intList.size()) throw new IllegalArgumentException("We are comparing edge breakdowns of different sizes: " + nother.size() + " and " + intList.size() + ".");
            for (int j = 0; j < intList.size(); j++) {
                if (intList.get(j).intValue()!=nother.get(j).intValue()) return false;
            }
        }
        return true;
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

    private MutableWorkUnit nextWorkUnit() {

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

            iterateEdgeBreakdown();

            // create a new unit of work
            return MutableWorkUnit.createMutableWorkUnit(patch);

        } else {

            BasicEdge[] edgeList = P0.createSkeleton(BD0, BD1, BD2);

            MutablePatch patch = MutablePatch.createMutablePatch(edgeList,bigVertices,tiles.dumpMutablePrototileList());

            iterateEdgeBreakdown();

            return MutableWorkUnit.createMutableWorkUnit(patch);

        } // end of nextWorkUnit
    }

    // public factory method
    public static WorkUnitFactory createWorkUnitFactory() {
        return new WorkUnitFactory();
    }

} // end of class WorkUnitFactory
