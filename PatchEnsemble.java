
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
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

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

        // now identify Orientations over and over until you can't identify more
        boolean done = true;
        int k = 0;
        int i = 0;
        int j = 0;
        do {
            if (!done) shared.remove(k);
            done = true;
            for (k = 0; k < shared.size(); k++) {
                i = shared.get(i).getIndices()[0];
                j = shared.get(i).getIndices()[1];

                // if they're equivalent
                if (part.equivalent(o1[i],o2[j])) {
                    Orientation[] list1 = bd1[i].getOrientations();
                    Orientation[] list2 = bd2[j].getOrientations();
                    if (list1.length!=list2.length) throw new IllegalArgumentException("Trying to match edge breakdowns with differing lengths.");
                    for (int l = 0; l < bd1[i].size(); l++)
                        part.identify(list1[l],list2[l]);
                    // breaking here causes shared(k) to be removed
                    done = false;
                    break;
                }

                // if they're opposite
                if (part.equivalent(o1[i],o2[j].getOpposite())) {
                    Orientation[] list1 = bd1[i].getOrientations();
                    Orientation[] list2 = bd2[j].reverse().getOrientations();
                    if (list1.length!=list2.length) throw new IllegalArgumentException("Trying to match edge breakdowns with differing lengths.");
                    for (int l = 0; l < bd1[i].size(); l++)
                        part.identify(list1[l],list2[l]);
                    // breaking here causes shared(k) to be removed
                    done = false;
                    break;
                }

                k++;
            }
        } while (!done);

        return true;

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

    public int[] getIndices() {
        return new int[] {(i1<i2)? i1 : i2, (i1<i2)? i2 : i1};
    }

    // equals method.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        IndexPair l = (IndexPair) obj;
        return ((this.i1==l.i1&&this.i2==l.i2)||(this.i1==l.i2&&this.i2==l.i1));
    }

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

    // private constructor
    // we assume the TriangleResults are entered in the same order
    // as the prototiles to which they correspond
    private PatchEnsemble(List<TriangleResults> inList, EdgeBreakdownTree bd) {
        breakdown = bd;
        patches = new SimpleGraph<>(IndexPair.class);
        for (int i = 0; i < inList.size(); i++) {
            for (ImmutablePatch p : bd.cull(i,inList.get(i))) {
                patches.addVertex(new PatchAndIndex(p,i));
            }
        }

        for (PatchAndIndex p1 : patches.vertexSet()) {
            MutableOrientationPartition part1 = p1.getPatch().getOrientationPartition().dumpMutableOrientationPartition();
            for (PatchAndIndex p2 : patches.vertexSet()) {
                MutableOrientationPartition part2 = p1.getPatch().getOrientationPartition().dumpMutableOrientationPartition();
                if (part1.consistent(part2)) patches.addEdge(p1,p2,new IndexPair(p1.getIndex(),p2.getIndex()));
            }
        }
    }

    // public static factory method
    public static PatchEnsemble createPatchEnsemble(List<TriangleResults> inList, EdgeBreakdownTree bd) {
        return new PatchEnsemble(inList,bd);
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

} // end of class PatchEnsemble
