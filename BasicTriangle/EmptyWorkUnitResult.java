import java.io.*;
import java.util.*;

public class EmptyWorkUnitResult implements Result, Serializable
{
    private final List<ImmutablePatch> localCompletedPatches;
    private int originalHashcode;

    public EmptyWorkUnitResult(int originalHashcode, List<ImmutablePatch> localCompletedPatches)
    {
        this.localCompletedPatches = localCompletedPatches;
        this.originalHashcode = originalHashcode;
    }

    public int getOriginalHashcode()
    {
        return originalHashcode;
    }

    public List<ImmutablePatch> getLocalCompletedPatches()
    {
        return localCompletedPatches;
    }

    public String toString()
    {
        return "EmptyWorkUnitResult for job ID " + originalHashcode + ": " + localCompletedPatches.size() + " completed patches";
    }
}
