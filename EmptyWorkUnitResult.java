import java.io.*;
import java.util.*;

public class EmptyWorkUnitResult implements Result, Serializable
{
    private final List<ImmutablePatch> localCompletedPatches;
    private final long uniqueID;

    public EmptyWorkUnitResult(long uniqueID, List<ImmutablePatch> localCompletedPatches)
    {
        this.localCompletedPatches = localCompletedPatches;
        this.uniqueID = uniqueID;
    }

    public long uniqueID()
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
