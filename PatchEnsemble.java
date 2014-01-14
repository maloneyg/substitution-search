
/*************************************************************************
 *  Compilation:  javac PatchEnsemble.java
 *  Execution:    java PatchEnsemble
 *
 *  A class representing a collection of substitution rules for
 *  a complete set of prototiles.  
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class PatchEnsemble implements Serializable {

    // a list of patches for each prototile
    private TriangleResults[] patches;
    // the edge breakdowns that appear in at least one substitution rule for
    // each prototile that contains an edge of the corresponding size
    private EdgeBreakdownTree breakdown;

    // private constructor
    // we assume the TriangleResults are entered in the same order
    // as the prototiles to which they correspond
    private PatchEnsemble(List<TriangleResults> inList, EdgeBreakdownTree bd) {
        breakdown = bd;
        TriangleResults[] eventualPatches = new TriangleResults[inList.size()];
        for (int i = 0; i < eventualPatches.length; i++) eventualPatches[i] = bd.cull(i,inList.get(i));
        patches = eventualPatches;
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
        for (TriangleResults tr : patches)
            result = prime*result + tr.hashCode();
        result = prime*result + breakdown.hashCode();
        return result;
    }

} // end of class PatchEnsemble
