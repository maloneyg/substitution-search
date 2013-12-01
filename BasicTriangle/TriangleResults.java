import java.io.*;
import java.util.*;

public class TriangleResults implements Serializable
{
    private final List<ImmutablePatch> allCompletedPatches;

    public TriangleResults(List<ImmutablePatch> allCompletedPatches)
    {
        this.allCompletedPatches = Collections.unmodifiableList(allCompletedPatches);
    }

    public List<ImmutablePatch> getPatches()
    {
        return allCompletedPatches;
    }
}
