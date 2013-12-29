import java.io.*;
import java.util.*;

public class EmptyWorkUnitResult implements Result, Serializable
{
    private final List<ImmutablePatch> localCompletedPatches;
    private final int uniqueID;

    public EmptyWorkUnitResult(int uniqueID, List<ImmutablePatch> localCompletedPatches)
    //public EmptyWorkUnitResult(int originalHashcode, List<ImmutablePatch> localCompletedPatches, List<ImmutablePatch> eventualPatches)
    {
        //this.localCompletedPatches = localCompletedPatches;
        this.localCompletedPatches = new LinkedList<ImmutablePatch>();
        this.uniqueID = uniqueID;
    }

    public int uniqueID()
    {
        return uniqueID;
    }

    public List<ImmutablePatch> getLocalCompletedPatches()
    {
        return localCompletedPatches;
    }

    public String toString()
    {
        return "Result for job ID " + uniqueID + ": " + localCompletedPatches.size() + " completed patches";
    }
}
