/**
*    This class implements a partition of a set of edge orientations.
*    The main rule is that an orientation and its opposite cannot
*    lie in the same subset.
*    It can produce new OrientationPartitions by merging subsets.
*/

import java.util.Collections;
import java.io.Serializable;
import java.util.ArrayList;

public class MutableOrientationPartition extends MutablePartition<Orientation> {

    // constructor 
    private MutableOrientationPartition(Orientation o) {
        super(o);
    }

    // public static factory method.
    public static MutableOrientationPartition createMutableOrientationPartition(Orientation[] o) {
        MutableOrientationPartition output = new MutableOrientationPartition(o[0]);
        for (int i=1; i<o.length; i++) output.add(o[i]);
        return output;
    }

    // identify two orientations with each other.
    // do the same for their opposites.
    public void identify(Orientation one, Orientation two) {
        super.identify(one,two);
        super.identify(one.getOpposite(),two.getOpposite());
    }

    // check if a partition is valid.
    // return false if any Orientation lies in the same
    // subset as its opposite, otherwise return true.
    public boolean valid() {
        ArrayList<Orientation> checkList = new ArrayList<>();
        PartitionNode<Orientation> current = getHead();
        while (current != null) {
            if (current.isHead()) checkList.clear();
            Orientation now = current.getData();
            for (Orientation o : checkList) {
                if (o.equals(now.getOpposite())) return false;
                checkList.add(now);
            }
            current = current.getNext();
        }
        return true;
    }

} // end of class MutableOrientationPartition
