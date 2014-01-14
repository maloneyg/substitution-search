
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
        MutableOrientationPartition part1 = this.patch.getOrientationPartition().dumpMutableOrientationPartition();
        MutableOrientationPartition part2 = p.patch.getOrientationPartition().dumpMutableOrientationPartition();
        if (!part1.consistent(part2)) return false;
        ImmutableList<Integer> e0 = p.patch.getEdge0();
        return true;
    }

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
