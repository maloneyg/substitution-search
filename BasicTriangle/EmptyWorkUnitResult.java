import java.io.*;
import java.util.*;

public class EmptyWorkUnitResult implements Result, Serializable
{
    private final List<ImmutablePatch> localCompletedPatches;
    private final List<ImmutablePatch> eventualPatches;
    private int originalHashcode;

    public EmptyWorkUnitResult(int originalHashcode, List<ImmutablePatch> localCompletedPatches, List<ImmutablePatch> eventualPatches)
    {
        this.localCompletedPatches = localCompletedPatches;
        this.eventualPatches = eventualPatches;
        this.originalHashcode = originalHashcode;

        // don't allow any null lists
        if ( localCompletedPatches == null || eventualPatches == null )
            throw new IllegalArgumentException("patch refs should not be null!");
    }

    public int getOriginalHashcode()
    {
        return originalHashcode;
    }

    public List<ImmutablePatch> getLocalCompletedPatches()
    {
        return localCompletedPatches;
    }

    public List<ImmutablePatch> getEventualPatches()
    {
        return eventualPatches;
    }

    public String toString()
    {
        return "Result for job ID " + originalHashcode + ": " + localCompletedPatches.size() + " completed patches";
    }
}
