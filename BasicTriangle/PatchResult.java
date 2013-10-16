import java.io.*;
import java.util.*;

public class PatchResult implements Result, Serializable
{
    private final MutableWorkUnit completedUnit;

    public PatchResult(MutableWorkUnit completedUnit)
    {
        this.completedUnit = completedUnit;
    }

    public MutableWorkUnit getCompletedUnit()
    {
        return completedUnit;
    }

    public String toString()
    {
        return completedUnit.getPatch().getLocalCompletedPatches().size() + " completed patches";
    }
}
