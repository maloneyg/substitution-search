import java.util.*;
import java.io.*;
import com.google.common.collect.*;

public class EmptyBatch implements Serializable
{
    private final List<EmptyWorkUnitResult> results;    // holds the results of the work units that were sent to the client
    private final List<EmptyBoundaryPatch> newPatches;  // holds the spawn of the results

    public EmptyBatch(List<EmptyBoundaryWorkUnitResult> results, List<EmptyBoundaryPatch> newPatches)
    {
        this.results = ImmutableList.copyOf(results);
        this.newPatches = ImmutableList.copyOf(newPatches);
    }

    public List<EmptyBoundaryWorkUnitResult> getResults()
    {
        return results;
    }

    public List<EmptyBoundaryPatch> getPatches()
    {
        return newPatches;
    }

    // returns work units that can be submitted to the queue
    public List<EmptyBoundaryWorkUnit> getNewUnits()
    {
        if ( !Preinitializer.MAIN_CLASS_NAME.equals("Server") )
            System.out.println("Warning: are you sure you want to invoke getNewUnits from somewhere that isn't the server?");

        List<EmptyBoundaryWorkUnit> newUnits = new LinkedList<EmptyBoundaryWorkUnit>();
        for (EmptyBoundaryPatch p : newPatches)
            newUnits.add( EmptyBoundaryWorkUnit.createEmptyBoundaryWorkUnit(p, new AtomicBoolean()) );

        return newUnits;
    }
}
