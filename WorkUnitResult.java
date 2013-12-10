import java.io.*;
import java.util.*;

public class WorkUnitResult implements Result, Serializable
{
    private final List<ImmutablePatch> localCompletedPatches;

    public WorkUnitResult(List<ImmutablePatch> localCompletedPatches)
    {
        this.localCompletedPatches = localCompletedPatches;
    }

    public List<ImmutablePatch> getLocalCompletedPatches()
    {
        return localCompletedPatches;
    }

    public String toString()
    {
        return "WorkUnitResult: " + localCompletedPatches.size() + " completed patches";
    }
}
