import java.io.*;
import java.util.*;
import com.google.common.collect.ImmutableList;

public class TriangleResults implements Serializable
{
    private final List<ImmutablePatch> allCompletedPatches;

    public TriangleResults(List<ImmutablePatch> allCompletedPatches)
    {
        //this.allCompletedPatches = Collections.unmodifiableList(allCompletedPatches);
        this.allCompletedPatches = ImmutableList.copyOf(allCompletedPatches);
    }

    public List<ImmutablePatch> getPatches()
    {
        return allCompletedPatches;
    }
 
    public int size()
    {
        return allCompletedPatches.size();
    }
} // end of class TriangleResults
