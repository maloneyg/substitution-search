import java.io.*;
import java.util.*;

public class TriangleResults implements Serializable
{
    private final List<BasicPatch> allCompletedPatches;

    public TriangleResults(List<BasicPatch> allCompletedPatches)
    {
        this.allCompletedPatches = Collections.unmodifiableList(allCompletedPatches);
    }

    public List<BasicPatch> getPatches()
    {
        return allCompletedPatches;
    }
}
