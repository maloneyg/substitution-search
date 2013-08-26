/**
*    This class implements a partition of a set of edge orientations.
*    The main rule is that an orientation and its opposite cannot
*    lie in the same subset.
*    It can produce new OrientationPartitions by merging subsets.
*/

import com.google.common.collect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

public final class OrientationPartition implements Serializable {

    private final ImmutableSet<ImmutableSet<Orientation>> partition;

    // make it Serializable
    static final long serialVersionUID = 1267821834624463132L;

    // constructor methods.
    private OrientationPartition(ImmutableSet<ImmutableSet<Orientation>> partition) {
        this.partition = partition;
    }

    // constructs the maximal partition on an array of Orientations.
    private OrientationPartition(Orientation[] orientations) {
        ImmutableSet<Orientation>[] preSet = new ImmutableSet[orientations.length];
        for (int i = 0; i < orientations.length; i++)
            preSet[i] = ImmutableSet.of(orientations[i]);
        partition = ImmutableSet.copyOf(preSet);
    }

    // public static factory method.
    static public OrientationPartition createOrientationPartition(Orientation[] orientations) {
        return new OrientationPartition(orientations);
    }

    // implementation of equals method.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        OrientationPartition o = (OrientationPartition) obj;
        int s1 = this.partition.size();
        int s2 = o.partition.size();
        if (s1 != s2) return false;
        // now we go through and check each set in this.partition.
        // if it has an equal in o.partition, keep going.
        // if not, return false.
        boolean foundMatch = false;
        for (ImmutableSet<Orientation> s : this.partition) {
            foundMatch = false;
            for (ImmutableSet<Orientation> t : o.partition) {
                if (s.equals(t)) {
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) return false;
        }
        return true;
    }

    // hashCode implementation.
    public int hashCode() {
        ArrayList<Integer> codes = new ArrayList(partition.size());
        for (ImmutableSet<Orientation> s : partition)
            codes.add(s.hashCode());
        Collections.sort(codes);
        int prime = 71;
        int result = 5;
        for (Integer i : codes)
            result = prime*result + i;
        return result;
    }

    // check if a partition is valid.
    // return false if any Orientation lies in the same
    // subset as its opposite, otherwise return true.
    public boolean valid() {
        for (ImmutableSet<Orientation> s : partition) {
            for (Orientation o : s) {
                if (s.contains(o.getOpposite())) return false;
            }
        }
        return true;
    }

    // create a new OrientationPartition by identifying
    // two Orientations.  Take the union of the two sets
    // containing them, and do the same for their 
    // opposites.  
    // Be careful here: we don't check for validity, so
    // that will have to be done externally after 
    // identify().
    public OrientationPartition identify(Orientation o1, Orientation o2) {
        ArrayList<ImmutableSet<Orientation>> preSet = new ArrayList(partition.size()-2);
        ArrayList<Orientation> pluses = new ArrayList(1);
        ArrayList<Orientation> minuses = new ArrayList(1);
        boolean combine = false;
        for (ImmutableSet<Orientation> s : partition) {
            if (s.contains(o1)||s.contains(o2)) {
                for (Orientation o : s) pluses.add(o);
                if (s.contains(o1.getOpposite())||s.contains(o2.getOpposite()))
                    combine = true;
            } else if (s.contains(o1.getOpposite())||s.contains(o2.getOpposite())) {
                for (Orientation o : s) minuses.add(o);
            } else {
                preSet.add(s);
            }
        }
        if (combine) pluses.addAll(minuses);
        preSet.add(ImmutableSet.copyOf(pluses));
        if (!combine) preSet.add(ImmutableSet.copyOf(minuses));
        return new OrientationPartition(ImmutableSet.copyOf(preSet));
    }

} // end of class OrientationPartition
