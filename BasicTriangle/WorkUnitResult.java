import java.io.*;
import java.util.*;

public class WorkUnitResult implements Result, Serializable
{
    private final List<BasicPatch> localCompletedPatches;

    public WorkUnitResult(List<BasicPatch> localCompletedPatches)
    {
        this.localCompletedPatches = localCompletedPatches;
    }

    public List<BasicPatch> getLocalCompletedPatches()
    {
        return localCompletedPatches;
    }

    public String toString()
    {
        return "WorkUnitResult: " + localCompletedPatches.size() + " completed patches";
    }
}
