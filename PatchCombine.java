
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
    private PatchCombine(TriangleResults l1, TriangleResults l2) {
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
            for (PatchAndIndex p2 : patches.vertexSet()) {
                if (p1.getIndex()!=p2.getIndex()) {
                    if (p1.compatible(p2) != p2.compatible(p1)) System.out.println(p1.compatible(p2) + " " + p2.compatible(p1));
                    //if (p1.compatible(p2)) patches.addEdge(p1,p2,new IndexPair(p1.getIndex(),p2.getIndex()));
                    boolean yup = p1.compatible(p2);
//                    System.out.println(yup);
                    if (yup) patches.addEdge(p1,p2,new IndexPair(p1.getIndex(),p2.getIndex()));
                }
            }
        }
        System.out.println("done building edges. Built " + patches.edgeSet().size() + " edges.");
    }

    // public static factory method
    public static PatchCombine createPatchCombine(TriangleResults l1, TriangleResults l2) {
        PatchCombine output = new PatchCombine(l1,l2);
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
        testo.dropToNeighbours(ummm);
        testo.gapString("test11.g","test11");
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
