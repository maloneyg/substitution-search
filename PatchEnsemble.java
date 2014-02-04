
/*************************************************************************
 *  Compilation:  javac PatchEnsemble.java
 *  Execution:    java PatchEnsemble
 *
 *  A class representing a collection of substitution rules for
 *  a complete set of prototiles.  
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import org.jgrapht.graph.*;
import java.io.Serializable;
import java.io.PrintWriter;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

// a class that stores a patch and an index
// the index tells us which prototile it represents
class PatchAndIndex implements Serializable {

    private final ImmutablePatch patch;
    private final int index;

    // public constructor
    public PatchAndIndex(ImmutablePatch p, int i) {
        patch = p;
        index = i;
    }

    // getters
    public ImmutablePatch getPatch() {
        return patch;
    }

    public int getIndex() {
        return index;
    }

    // check if the partition and edge breakdowns are compatible
    public boolean compatible(PatchAndIndex p) {
        MutableOrientationPartition part = this.patch.getOrientationPartition().dumpMutableOrientationPartition().deepCopy().refine(p.patch.getOrientationPartition().dumpMutableOrientationPartition());
        // do the easy test first: make sure their Orientations are compatible
        //return part.valid();
        if (!part.valid()) return false;
        // now pull out the relevant data from the patches
        EdgeBreakdown[] bd1 = new EdgeBreakdown[3];
        bd1[0] = this.patch.getEdge0();
        bd1[1] = this.patch.getEdge1();
        bd1[2] = this.patch.getEdge2();
        EdgeBreakdown[] bd2 = new EdgeBreakdown[3];
        bd2[0] = p.patch.getEdge0();
        bd2[1] = p.patch.getEdge1();
        bd2[2] = p.patch.getEdge2();
        BasicPrototile t1 = BasicPrototile.ALL_PROTOTILES.get(this.getIndex());
        BasicPrototile t2 = BasicPrototile.ALL_PROTOTILES.get(p.getIndex());
        Orientation[] o1 = t1.getOrientations();
        Orientation[] o2 = t2.getOrientations();
        BasicEdgeLength[] e1 = t1.getLengths();
        BasicEdgeLength[] e2 = t2.getLengths();

        // now identify Orientations based on EdgeBreakdowns
        // for this purpose, first determine which edge lengths they share
        List<IndexPair> shared = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (e1[i].equals(e2[j])) shared.add(new IndexPair(i,j));
            }
        }

        // the following is for debugging
//        System.out.println(t1); //
//        System.out.println(t2); //
//        System.out.println(part); //
//        for (int i = 0; i < 3; i++) System.out.println(e1[i] + " " + o1[i]); // 
//        for (int i = 0; i < 3; i++) System.out.println(e2[i] + " " + o2[i]); // 
//        System.out.println(shared.size()); // 

        // now identify Orientations over and over until you can't identify more
        boolean done = true;
        int k = 0;
        int i = 0;
        int j = 0;
        do {
            if (!done) shared.remove(k);
            done = true;
            for (k = 0; k < shared.size(); k++) {
                i = shared.get(k).getIndices()[0];
                j = shared.get(k).getIndices()[1];

                // debugging output
//                System.out.println("checking k");
//                System.out.println(e1[i] + " " + o1[i] + " " + e2[j] + " " + o2[j]);

                // if they're equivalent
                if (part.equivalent(o1[i],o2[j])) {
//                    System.out.println("checking breakdowns."); //
//                    System.out.println(bd1[i]); //
//                    System.out.println(bd2[j]); //
                    Orientation[] list1 = bd1[i].getOrientations();
                    Orientation[] list2 = bd2[j].getOrientations();
                    BasicEdgeLength[] lengths1 = bd1[i].getLengths();
                    BasicEdgeLength[] lengths2 = bd2[j].getLengths();
                    if (list1.length!=list2.length) throw new IllegalArgumentException("Trying to match edge breakdowns with differing lengths.");
                    for (int l = 0; l < bd1[i].size(); l++) {
                        if (!lengths1[l].equals(lengths2[l])) return false;
                        part.identify(list1[l],list2[l]);
                    }
                    // breaking here causes shared(k) to be removed
                    done = false;
                    break;
                }

                // if they're opposite
                if (part.equivalent(o1[i],o2[j].getOpposite())) {
//                    System.out.println("checking breakdowns (opposite)."); //
//                    System.out.println(bd1[i]); //
//                    System.out.println(bd2[j].reverse()); //
                    Orientation[] list1 = bd1[i].getOrientations();
                    Orientation[] list2 = bd2[j].reverse().getOrientations();
                    BasicEdgeLength[] lengths1 = bd1[i].getLengths();
                    BasicEdgeLength[] lengths2 = bd2[j].reverse().getLengths();
                    if (list1.length!=list2.length) throw new IllegalArgumentException("Trying to match edge breakdowns with differing lengths.");
                    for (int l = 0; l < bd1[i].size(); l++) {
                        if (!lengths1[l].equals(lengths2[l])) return false;
                        part.identify(list1[l],list2[l]);
                    }
                    // breaking here causes shared(k) to be removed
                    done = false;
                    break;
                }

                //k++;
            }
        } while (!done);

        return part.valid();

    } // compatible method ends here

    // equals method.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        PatchAndIndex l = (PatchAndIndex) obj;
        return (this.patch.equals(l.patch)&&this.index==l.index);
    }

    // hashCode method.
    public int hashCode() {
        int prime = 37;
        int result = patch.hashCode();
        return prime*result + index;
    }

} // end of class PatchAndIndex

// a class that stores two indices
class IndexPair implements Serializable {

    private final int i1;
    private final int i2;

    // public constructor
    public IndexPair(int a, int b) {
        i1 = a;
        i2 = b;
    }

    public boolean hasIndex(int i) {
        return (i1==i||i2==i);
    }

//    public int[] getIndices() {
//        return new int[] {(i1<i2)? i1 : i2, (i1<i2)? i2 : i1};
//    }

    public int[] getIndices() {
        return new int[] {i1, i2};
    }

    // toString method.
    public String toString() {
        return "IndexPair (" + i1 + ", " + i2 + ")";
    }

    // equals method.
//    public boolean equals(Object obj) {
//        if (obj == null || getClass() != obj.getClass())
//            return false;
//        IndexPair l = (IndexPair) obj;
//        return ((this.i1==l.i1&&this.i2==l.i2)||(this.i1==l.i2&&this.i2==l.i1));
//    }

    // hashCode method.
    public int hashCode() {
        return (i1<i2)? 43*i1 + i2 : 43*i2 + i1;
    }

} // end of class IndexPair

public class PatchEnsemble implements Serializable {

    // a graph of patches
    // two patches are joined by an edge if they are compatible
    private SimpleGraph<PatchAndIndex,IndexPair> patches;
    // the edge breakdowns that appear in at least one substitution rule for
    // each prototile that contains an edge of the corresponding size
    private EdgeBreakdownTree breakdown;

    private class PatchEnsembleWorkUnit implements WorkUnit
    {
        public static final int BATCH_SIZE = 10000; // how many IndexPairs can go into one of these
        private int i_start;
        private int j_start;
        private List<PatchAndIndex> patchList;
        private HashMap<Integer,Integer> compatible = new HashMap<Integer,Integer>();

        public PatchEnsembleWorkUnit(int i_start, int j_start, List<PatchAndIndex> patchList)
        {
            this.i_start = i_start;
            this.j_start = j_start;
            this.patchList = patchList;
        }

        public PatchEnsembleResult call()
        {
            int count = 0;
            i_loop:
            for (int i=i_start; i < patchList.size(); i++)
                {
                    for (int j=j_start; j < patchList.size(); j++)
                        {
                            count++;
                            if ( count > BATCH_SIZE )
                                break i_loop;
                            //System.out.println(count + " : " + i + ", " + j);
                            PatchAndIndex p1 = patchList.get(i);
                            PatchAndIndex p2 = patchList.get(j);
                            if ( p1.compatible(p2) )
                                compatible.put(i,j);
                        }
                }
            
            PatchEnsembleResult result = new PatchEnsembleResult(compatible);
            return result;
        }
    }

    private class PatchEnsembleResult implements Result
    {
        private Map<Integer,Integer> compatible; // only compatible pairs will be stored

        public PatchEnsembleResult(Map<Integer,Integer> compatible)
        {
            this.compatible = compatible;
        }

        public Map<Integer,Integer> getCompatible()
        {
            return compatible;
        }

        public String toString()
        {
            return compatible.size() + " pairs";
        }
    }

    // private constructor
    // we assume the TriangleResults are entered in the same order
    // as the prototiles to which they correspond
    private PatchEnsemble(List<TriangleResults> inList, EdgeBreakdownTree bd) {
        System.out.println("Building PatchEnsemble.");
        System.out.print("Loading vertices ... ");
        breakdown = bd;
        patches = new SimpleGraph<>(IndexPair.class);
        for (int i = 0; i < inList.size(); i++) {
            for (ImmutablePatch p : bd.cull(i,inList.get(i))) {
            //for (ImmutablePatch p : inList.get(i).getPatches()) {
                patches.addVertex(new PatchAndIndex(p,i));
            }
        }

        System.out.println("done loading vertices. Loaded " + patches.vertexSet().size() + " vertices.");

        // convert set to list and iterate the upper triangle
        List<PatchAndIndex> patchList = new LinkedList<PatchAndIndex>(patches.vertexSet());

        // create work units
        System.out.print("Populating work units...");
        LinkedList<PatchEnsembleWorkUnit> listOfUnits = new LinkedList<PatchEnsembleWorkUnit>();
        int i_start = 0;
        int j_start = 0;
        int currentTotal = 0;
        listOfUnits.add(new PatchEnsembleWorkUnit(0, 1, patchList));
        for (int i=0; i < patchList.size(); i++)
            {
                for (int j=i+1; j < patchList.size(); j++)
                    {
                        currentTotal++;
                        //System.out.println(currentTotal + " : " + i + " " + j);
                        if ( currentTotal > PatchEnsembleWorkUnit.BATCH_SIZE ||
                             ( i == patchList.size()-2 && j == patchList.size()-1 ) )
                            {
                                i_start = i;
                                j_start = j;
                                //System.out.println(String.format("unit #%d   i: %d j: %d", listOfUnits.size()+1, i_start, j_start));
                                PatchEnsembleWorkUnit thisUnit = new PatchEnsembleWorkUnit(i_start, j_start, patchList);
                                listOfUnits.add(thisUnit);
                                currentTotal = 0;
                            }
                    }
            }
        System.out.print(listOfUnits.size() + " units created...submitting...");

        // submit jobs
        LinkedList<Future<Result>> listOfFutures = new LinkedList<>();
        for (PatchEnsembleWorkUnit u : listOfUnits)
            {
                Future<Result> thisFuture = GeneralThreadService.INSTANCE.getExecutor().submit(u);
                listOfFutures.add(thisFuture);
            }
        System.out.println("done.");

        // poll the futures
        while (true)
            {
                // poll every 250 milliseconds
                try
                    {
                        Thread.sleep(250L);
                    }
                catch (InterruptedException e)
                    {
                    }

                int numberComplete = 0;
                for (Future<Result> thisFuture : listOfFutures)
                    {
                        if ( thisFuture.isDone() )
                            numberComplete++;
                    }
                System.out.print(String.format("%d of %d work units complete\r", numberComplete, listOfFutures.size()));
                if ( numberComplete == listOfFutures.size() )
                    break;
            }
        System.out.println();

        // concatenate all results
        Map<Integer,Integer> allCompatible = new HashMap<Integer,Integer>();
        for (Future<Result> thisFuture : listOfFutures)
            {
                try
                    {
                        PatchEnsembleResult thisResult = (PatchEnsembleResult)thisFuture.get();
                        allCompatible.putAll(thisResult.getCompatible());
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
            }

        // add the new edges to the graph
        System.out.print("Adding " + allCompatible.size() + " edges to graph...");
        for (Integer i : allCompatible.keySet())
            {
                Integer j = allCompatible.get(i);
                PatchAndIndex p1 = patchList.get(i);
                PatchAndIndex p2 = patchList.get(j);
                IndexPair indexPair = new IndexPair(p1.getIndex(),p2.getIndex());
                patches.addEdge(p1,p2,indexPair);
            }
        System.out.println("done!");

        System.out.println("All done.  Built " + patches.edgeSet().size() + " edges.");
    }

    // public static factory method
    public static PatchEnsemble createPatchEnsemble(List<TriangleResults> inList, EdgeBreakdownTree bd) {
        PatchEnsemble output = new PatchEnsemble(inList,bd);
        System.out.print("Expunging lone vertices ... ");
        output.dropLoners();
        System.out.println("done expunging lone vertices.");
        return output;
    }

    // remove all vertices that don't have edges connected to all indices
    // removing one such vertex might produce another one, so loop until
    // no more are created
    public void dropLoners() {
        // we have to create this outside of the loop, I think
        PatchAndIndex drop = null;
        boolean done = true;
        do {
            if (!done) patches.removeVertex(drop);
            done = true;
            // now we loop through all vertices and check for loners
            for (PatchAndIndex p : patches.vertexSet()) {
                // check boxes to see if p has neighbours of all indices
                boolean[] check = new boolean[Preinitializer.PROTOTILES.size()];
                for (IndexPair i : patches.edgesOf(p)) {
                    for (int j = 0; j < 2; j ++) check[i.getIndices()[j]] = true;
                }
                // if we missed any index, we're not done
                for (int j = 0; j < check.length; j++) done = (done&&check[j]);
                // drop this one and start again
                if (!done) {
                    drop = p;
                    break;
                }
            }
        } while (!done);
    }

    // drop down to the set of vertices adjacent to root
    public void dropToNeighbours(PatchAndIndex root) {
        List<PatchAndIndex> neighbours = new ArrayList<>();
        neighbours.add(root);
        for (IndexPair ip : patches.edgesOf(root)) {
            neighbours.add(patches.getEdgeTarget(ip));
            neighbours.add(patches.getEdgeSource(ip));
        }
        List<PatchAndIndex> remove = new ArrayList<>();
        for (PatchAndIndex pp : patches.vertexSet()) if (!neighbours.contains(pp)) remove.add(pp);
        System.out.println(remove.size());
        patches.removeAllVertices(remove);
    }

    // size of the graph
    public int size() {
        return patches.vertexSet().size();
    }

    // equals method.
    // very broken. for now they're all equal.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        PatchEnsemble l = (PatchEnsemble) obj;
        return true;
    }

    // hashCode override.
    public int hashCode() {
        int prime = 439;
        int result = 3;
        result = prime*patches.hashCode() + breakdown.hashCode();
        return result;
    }

    // output a String of gap-readable code
    // name is the name of the gap record we will produce
    public String gapString(String fileName, String name) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(fileName);
            out.write(Initializer.gapPreambleString(name));
            out.write(BasicPrototile.allPrototilesGapString());
            out.write("  subst_tiles := [\n");

            for (int i = 0; i < Preinitializer.PROTOTILES.size(); i++) {
                // dump all the right-handed substitution rules
                boolean first = true;
                out.write("               [\n");
                for (PatchAndIndex pi : patches.vertexSet()) {
                    if (pi.getIndex()==i) {
                        if (!first) out.write(",\n");
                        first = false;
                        out.write(pi.getPatch().functionGapString(false));
                    }
                }
                out.write("\n               ],\n");

                // dump all the left-handed substitution rules
                first = true;
                out.write("               [\n");
                for (PatchAndIndex pi : patches.vertexSet()) {
                    if (pi.getIndex()==i) {
                        if (!first) out.write(",\n");
                        first = false;
                        out.write(pi.getPatch().functionGapString(true));
                    }
                }
                out.write("\n               ]");
                out.write((i==Preinitializer.PROTOTILES.size()-1)? "\n  ],\n" : ",\n");

            } // end of substitution rule dump
            out.write(BasicPrototile.drawAllPrototilesGapString());
            out.write("\n\n  drawdot := function( v, psfile )\n");
            out.write("    AppendTo(psfile, ");
            for (int m = 1; m < Preinitializer.N; m++) {
                out.write("v[" + m + "], ");
                out.write((m==Preinitializer.N-1) ? "" : "\" \", ");
            }
            out.write("\" dot\\n\" );\n  end");
            out.write("\n\n);");

        } catch ( Exception e ) {
        } finally {
            try {
                if ( out != null)
                out.close( );
            } catch ( Exception e) {
            }
        }
        return name;
    }

    public static void main(String[] args) {


        List<TriangleResults> resultsList = new LinkedList<>();
        String[] files = new String[Preinitializer.PROTOTILES.size()];
        files[0] = "results/seven-tile0-1plusa.chk";
        files[1] = "results/seven-tile1-1plusa.chk";
        files[2] = "results/seven-tile2-1plusa.chk";
//        files[0] = "results/tile0-105.chk";
//        files[1] = "results/tile1-105.chk";
//        files[2] = "results/tile2-105.chk";
//        files[3] = "results/tile3-105.chk";
//        files[4] = "results/tile4-105.chk";

        for (String filename : files) {
            // deserialize data
            if ( ! new File(filename).isFile() )
                {
                    System.out.println(filename + " not found!");
                    return;
                }
            try
                {
                    FileInputStream fileIn = new FileInputStream(filename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    resultsList.add((TriangleResults)in.readObject());
                    System.out.println(filename + " has been read. " + resultsList.get(resultsList.size()-1).size() + " patches found.");
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }
        }

        PatchEnsemble testo = createPatchEnsemble(resultsList, PuzzleBoundary.BREAKDOWNS);
        System.out.println(testo.size());
        PatchAndIndex ummm = null;
        for (PatchAndIndex pp: testo.patches.vertexSet()) { if (pp.getIndex() == 0) { ummm = pp; break; }}
        testo.dropToNeighbours(ummm);
        testo.gapString("test7.g","test7");
        System.out.println(testo.size());


        // write TriangleResult files with a selection of vertices that remain

//        for (int i = 0; i < Preinitializer.PROTOTILES.size(); i++) { // for loop
//            for (PatchAndIndex pp : testo.patches.vertexSet()) {
//                if (pp.getIndex()==i) { // choose the first one with this index
//                    List<ImmutablePatch> completedPatches = new ArrayList<>(1);
//                    completedPatches.add(pp.getPatch());
//                    try
//                        {
//                            TriangleResults triangleResults = new TriangleResults(completedPatches);
//                            FileOutputStream fileOut = new FileOutputStream("vertex-test" + i + ".chk");
//                            ObjectOutputStream out = new ObjectOutputStream(fileOut);
//                            out.writeObject(triangleResults);
//                            out.close();
//                            fileOut.close();
//                            System.out.println("wrote " + completedPatches.size() + " results to " + "vertex-test" + i + ".chk.");
//                        }
//                    catch (Exception e)
//                        {
//                            e.printStackTrace();
//                        }
//
//                } // here ends if statement
//            } // here ends loop through vertices
//        } // here ends big for loop

        System.exit(0);

    }

} // end of class PatchEnsemble
