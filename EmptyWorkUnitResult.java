import java.io.*;
import java.util.*;

public class EmptyWorkUnitResult implements Result, Serializable
{
    private final List<ImmutablePatch> localCompletedPatches;
    private final Long uniqueID;

    public EmptyWorkUnitResult(Long uniqueID, List<ImmutablePatch> localCompletedPatches)
    {
        this.localCompletedPatches = localCompletedPatches;
        this.uniqueID = uniqueID;
    }

    public Long uniqueID()
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
