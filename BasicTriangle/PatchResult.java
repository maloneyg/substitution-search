import java.io.*;
import java.util.*;
import com.google.common.collect.*;

public class PatchResult implements Result, Serializable
{
    private final int ID; // ID of the original WorkUnitInstructions that created this PatchResult
    private final ImmutableList<BasicPatch> completedPatches; // finished puzzles
    private final int numberOfUnits; // how many work units went into this result

    public PatchResult(int ID, List<BasicPatch> completedPatches, int numberOfUnits)
    {
        this.ID = ID;
        this.completedPatches = ImmutableList.copyOf(completedPatches);
        this.numberOfUnits = numberOfUnits;
    }

    public int getID()
    {
        return ID;
    }

    public List<BasicPatch> getCompletedPatches()
    {
        return completedPatches;
    }

    public int getNumberOfUnits()
    {
        return numberOfUnits;
    }

    public String toString()
    {
        return "PatchResult ID " + ID + " (" + completedPatches.size() + " completed puzzles, " + numberOfUnits + " work units)";
    }

    public int hashCode()
    {
        return completedPatches.hashCode();
    }

}
