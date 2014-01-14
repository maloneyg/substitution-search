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
import java.util.HashSet;
import java.util.ArrayList;

public final class OrientationPartition implements Serializable {

    private final HashSet<HashSet<Orientation>> partition;

    // a pool of existing HashSet<Orientation> objects for recycling
    //private static OrientationClassPool POOL = OrientationClassPool.getInstance();

    // make it Serializable
    static final long serialVersionUID = 1267821834624463132L;

    // constructor methods.
    private OrientationPartition(HashSet<HashSet<Orientation>> partition) {
        this.partition = partition;
    }

    // constructs the maximal partition on an array of Orientations.
    private OrientationPartition(Orientation[] orientations) {
        HashSet<HashSet<Orientation>> output = new HashSet<>(orientations.length);
        for (int i = 0; i < orientations.length; i++) {
            HashSet<Orientation> thisSet = new HashSet<>(1);
            thisSet.add(orientations[i]);
            output.add(thisSet); // POOL
        }
        partition = output;
    }

    // public static factory method.
    static public OrientationPartition createOrientationPartition(Orientation[] orientations) {
        return new OrientationPartition(orientations);
    }

    // public static factory method.
    static public OrientationPartition createOrientationPartition(HashSet<HashSet<Orientation>> partition) {
        return new OrientationPartition(partition);
    }

    // implementation of equals method.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        OrientationPartition o = (OrientationPartition) obj;
        return this.partition.equals(o.partition);
    }

    // hashCode implementation.
    public int hashCode() {
        ArrayList<Integer> codes = new ArrayList<>(partition.size());
        for (HashSet<Orientation> s : partition)
            codes.add(s.hashCode());
        Collections.sort(codes);
        int prime = 71;
        int result = 5;
        for (Integer i : codes)
            result = prime*result + i;
        return result;
    }

    // return a mutable version of this
    // the mutable version contains only Orientations that appear on 
    // edges of prototiles
    public MutableOrientationPartition dumpMutableOrientationPartition() {
        Orientation oo = BasicPrototile.ALL_PROTOTILES.get(0).getOrientations()[0];
        MutableOrientationPartition output = MutableOrientationPartition.createMutableOrientationPartition(oo);
        output.add(oo.getOpposite());
        for (BasicPrototile p : BasicPrototile.ALL_PROTOTILES) {
            for (Orientation o : p.getOrientations()) {
                if (!o.equals(oo)) {
                    output.add(o);
                    output.add(o.getOpposite());
                }
            }
        }

        for (HashSet<Orientation> s : partition) {
            Orientation previous = oo;
            boolean onFirst = true;
            for (Orientation o : s) {
                if (output.contains(o)) {
                    if (!onFirst) {
                        output.identify(o,previous);
                    }
                    previous = o;
                    onFirst = false;
                }
            }
        }
        return output;
    }

    // check if a partition is valid.
    // return false if any Orientation lies in the same
    // subset as its opposite, otherwise return true.
    public boolean valid() {
        for (HashSet<Orientation> s : partition) {
            for (Orientation o : s) {
                if (s.contains(o.getOpposite())) return false;
            }
        }
        return true;
    }

    // return the set of Orientations that have been declared
    // equivalent to o.
    public ImmutableSet<Orientation> getEquivalenceClass(Orientation o) {
        for (HashSet<Orientation> s : partition) {
            if (s.contains(o)) return ImmutableSet.copyOf(s);
        }
        throw new IllegalArgumentException("Orientation " + o + " isn't on the list.");
    }

    // split up the set p into subsets the elements of any one of which
    // lie in the same set in this.partition.
//    private OrientationPartition split(ImmutableSet<Orientation> p) {
//        ArrayList<ImmutableSet<Orientation>> output = new ArrayList<>();
//        ArrayList<Orientation> current;
//        for (ImmutableSet<Orientation> s : partition) {
//            current = new ArrayList<>();
//            for (Orientation o : s) {
//                if (p.contains(o)) current.add(o);
//            }
//            if (current.size() > 0) output.add(ImmutableSet.copyOf(current));
//        }
//        return new OrientationPartition(ImmutableSet.copyOf(output));
//    }

    // return the refinement of this and s
//    public OrientationPartition refinement(OrientationPartition s) {
//        ArrayList<ImmutableSet<Orientation>> output = new ArrayList<>(partition.size());
//        for (ImmutableSet<Orientation> p : s.partition) {
//            for (ImmutableSet<Orientation> l : split(p).partition)
//                output.add(l);
//        }
//        return new OrientationPartition(ImmutableSet.copyOf(output));
//    }

    // create a new OrientationPartition by identifying
    // two Orientations.  Take the union of the two sets
    // containing them, and do the same for their 
    // opposites.  
    // Be careful here: we don't check for validity, so
    // that will have to be done externally before or after 
    // identify().
    public OrientationPartition identify(Orientation o1, Orientation o2) {
        HashSet<HashSet<Orientation>> output = new HashSet<>(partition.size()-2);
        HashSet<Orientation> pluses = new HashSet<>(1);
        HashSet<Orientation> minuses = new HashSet<>(1);
        boolean combine = false;
        for (HashSet<Orientation> s : partition) {
            if (s.contains(o1)||s.contains(o2)) {
                for (Orientation o : s) pluses.add(o);
                if (s.contains(o1.getOpposite())||s.contains(o2.getOpposite()))
                    combine = true;
            } else if (s.contains(o1.getOpposite())||s.contains(o2.getOpposite())) {
                for (Orientation o : s) minuses.add(o);
            } else {
                output.add(s); // POOL
            }
        }
        if (combine) pluses.addAll(minuses);
        output.add(pluses); // POOL
        if (!combine) output.add(minuses); // POOL
        return new OrientationPartition(output);
    }

} // end of class OrientationPartition
