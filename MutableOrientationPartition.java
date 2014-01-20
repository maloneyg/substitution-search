/**
*    This class implements a partition of a set of edge orientations.
*    The main rule is that an orientation and its opposite cannot
*    lie in the same subset.
*    It can produce new OrientationPartitions by merging subsets.
*/

import java.util.Collections;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.*;

public class MutableOrientationPartition extends MutablePartition<Orientation> implements Serializable {

    // instructions to tell us which Orientations were initially identifed.
    // these help us reconstruct earlier versions of the partition.
    private ArrayList<Orientation> instructions1 = new ArrayList<>();
    private ArrayList<Orientation> instructions2 = new ArrayList<>();

    // constructor 
    private MutableOrientationPartition(Orientation o) {
        super(o);
    }

    public int hashCode()
    {
        return Objects.hash(instructions1, instructions2);
    }

    // public static factory method.
    public static MutableOrientationPartition createMutableOrientationPartition(Orientation[] o) {
        MutableOrientationPartition output = new MutableOrientationPartition(o[0]);
        for (int i=1; i<o.length; i++) output.add(o[i]);
        return output;
    }

    // public static factory method.
    public static MutableOrientationPartition createMutableOrientationPartition(Orientation o) {
        return new MutableOrientationPartition(o);
    }

    // deep copy
    public MutableOrientationPartition deepCopy() {
        PartitionNode<Orientation> current = getHead();
        MutableOrientationPartition output = new MutableOrientationPartition(current.getData());
        PartitionNode<Orientation> otherCurrent = output.getHead();
        current = current.getNext();
        while (current != null) {
            output.addToEnd(current.getData());
            otherCurrent = otherCurrent.getNext();
            otherCurrent.setHead(current.isHead());
            current = current.getNext();
        }
        return output;
    }

    // modify this to obtain a refinement of this and p
    // we assume that this and p represent partitions of the same set
    public MutableOrientationPartition refine(MutableOrientationPartition p) {
        PartitionNode<Orientation> current = p.getHead();
        while (current != null) {
            PartitionNode<Orientation> next = current.getNext();
            if (next != null && !next.isHead())
                this.identify(current.getData(),next.getData());
            current = next;
        }
        return this;
    }

    // return true if the joint refinement of these two partitions is consistent
    public boolean consistent(MutableOrientationPartition p) {
        return this.deepCopy().refine(p).valid();
    }

    // identify two orientations with each other.
    // do the same for their opposites.
    public void identify(Orientation one, Orientation two) {
        super.identify(one,two);
        super.identify(one.getOpposite(),two.getOpposite());
    }

    // split an Orientation equivalence class.
    // do the same for the opposite class.
    public void split(Orientation o) {
        super.split(o);
        super.split(o.getOpposite());
    }

    // add identification instructions
    public void addInstructions(Orientation one, Orientation two) {
        instructions1.add(one);
        instructions2.add(two);
        this.identify(one,two);
    }

    // follow identification instructions
    public void followInstructions() {
        for (int i = 0; i < instructions1.size(); i++)
            this.identify(instructions1.get(i),instructions2.get(i));
    }

    // produce an OrientationPartition
    public OrientationPartition dumpOrientationPartition() {
        PartitionNode<Orientation> current = getHead();
        HashSet<HashSet<Orientation>> output = new HashSet<>();
        ArrayList<Orientation> currentClass = new ArrayList<>();
        do {
            currentClass.add(current.getData());
            current = current.getNext();
            if (current == null || current.isHead()) {
                output.add(new HashSet<>(currentClass));
                currentClass.clear();
            }
        } while (current != null);
        return OrientationPartition.createOrientationPartition(output);
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
            }
            checkList.add(now);
            current = current.getNext();
        }
        return true;
    }

} // end of class MutableOrientationPartition
