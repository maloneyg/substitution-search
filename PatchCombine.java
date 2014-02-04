
/*************************************************************************
 *  Compilation:  javac PatchCombine.java
 *  Execution:    java PatchCombine
 *
 *  A class representing a collection of substitution rules for
 *  a complete set of prototiles.  
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import org.jgrapht.graph.*;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.io.PrintWriter;
import java.io.*;

public class PatchCombine implements Serializable {

    // a graph of patches
    // two patches are joined by an edge if they are compatible
    private SimpleGraph<PatchAndIndex,IndexPair> patches;

    // private constructor
    // we assume the TriangleResults are entered in the same order
    // as the prototiles to which they correspond
    private PatchCombine(TriangleResults l1, TriangleResults l2, int edge1, int edge2) {
        System.out.println("Building graph.");

        // add all the patches
        System.out.print("Loading vertices ... ");
        patches = new SimpleGraph<>(IndexPair.class);
        for (int i = 0; i < l1.size(); i++) {
            patches.addVertex(new PatchAndIndex(l1.getPatches().get(i),1));
        }
        for (int i = 0; i < l2.size(); i++) {
            patches.addVertex(new PatchAndIndex(l2.getPatches().get(i),2));
        }
        System.out.println("done loading vertices. Loaded " + patches.vertexSet().size() + " vertices.");

        System.out.print("Building edges ... ");
        for (PatchAndIndex p1 : patches.vertexSet()) {
            if (p1.getIndex()==1) {
                for (PatchAndIndex p2 : patches.vertexSet()) {
                    if (p2.getIndex()==2&&(!patches.containsEdge(p1,p2))) {
                        boolean yup = p1.edgeMatch(p2,edge1,edge2);
                        if (yup) patches.addEdge(p1,p2,new IndexPair(p1.getIndex(),p2.getIndex()));
                    }
                }
            }
        }
        System.out.println("done building edges. Built " + patches.edgeSet().size() + " edges.");
    }

    // public static factory method
    public static PatchCombine createPatchCombine(TriangleResults l1, TriangleResults l2) {
        PatchCombine output = new PatchCombine(l1,l2);
        return output;
    }

    // size of the graph
    public int size() {
        return patches.vertexSet().size();
    }

    // if it is known that p1 and p2 match, then combine them to get a new patch
    public ImmutablePatch combine(ImmutablePatch p1, ImmutablePatch p2) {
    public static ImmutablePatch createImmutablePatch(BasicTriangle[] t, BasicEdge[] e1, BasicEdge[] e2, OrientationPartition o, BytePoint[] v,EdgeBreakdown bd0,EdgeBreakdown bd1,EdgeBreakdown bd2) {
        return patches.vertexSet().size();
    }

    // equals method.
    // very broken. for now they're all equal.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        PatchCombine l = (PatchCombine) obj;
        return true;
    }

    // hashCode override.
    public int hashCode() {
        int prime = 439;
        int result = 3;
        result = prime*patches.hashCode();
        return result;
    }

    public static void main(String[] args) {


        List<TriangleResults> resultsList = new LinkedList<>();
        String[] files = new String[Preinitializer.PROTOTILES.size()];
//        files[0] = "results/seven1a-0.chk";
//        files[1] = "results/seven1a-1.chk";
//        files[2] = "results/seven1a-2.chk";
        files[0] = "results/tile0-105.chk";
        files[1] = "results/tile1-105.chk";
        files[2] = "results/tile2-105.chk";
        files[3] = "results/tile3-105.chk";
        files[4] = "results/tile4-105.chk";

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

        PatchCombine testo = createPatchCombine(resultsList.get(0), resultsList.get(1));
        System.out.println(testo.size());
        PatchAndIndex ummm = null;
        for (PatchAndIndex pp: testo.patches.vertexSet()) { if (pp.getIndex() == 0) { ummm = pp; break; }}
        System.out.println(testo.size());


        // write TriangleResult files with a selection of vertices that remain

        for (int i = 0; i < Preinitializer.PROTOTILES.size(); i++) { // for loop
            for (PatchAndIndex pp : testo.patches.vertexSet()) {
                if (pp.getIndex()==i) { // choose the first one with this index
                    List<ImmutablePatch> completedPatches = new ArrayList<>(1);
                    completedPatches.add(pp.getPatch());
                    try
                        {
                            TriangleResults triangleResults = new TriangleResults(completedPatches);
                            FileOutputStream fileOut = new FileOutputStream("vertex-test" + i + ".chk");
                            ObjectOutputStream out = new ObjectOutputStream(fileOut);
                            out.writeObject(triangleResults);
                            out.close();
                            fileOut.close();
                            System.out.println("wrote " + completedPatches.size() + " results to " + "vertex-test" + i + ".chk.");
                        }
                    catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                } // here ends if statement
            } // here ends loop through vertices
        } // here ends big for loop



    }

} // end of class PatchCombine
