import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class EmptyBoundaryServerCheckpoint implements Serializable
{
    private final int jobCount;
    private final LinkedList<EmptyBoundaryWorkUnitInstructions> toBeResent;
    private final EmptyBoundaryWorkUnitFactory workUnitFactory;
    private final AtomicLong numberOfResultsReceived;
    private final Date dateSerialized;
    private final int numberOfCompletedPuzzles;

    public EmptyBoundaryServerCheckpoint(int jobCount, HashMap<EmptyBoundaryWorkUnitInstructions,EmptyBoundaryServer.ConnectionThread> dispatched,
                            LinkedList<EmptyBoundaryWorkUnitInstructions> toBeResent, Object sendLock,
                            EmptyBoundaryWorkUnitFactory workUnitFactory, AtomicLong numberOfResultsReceived, Date dateSerialized,
                            int numberOfCompletedPuzzles)
    {
        synchronized(sendLock)
            {
                this.jobCount = jobCount;
                this.toBeResent = new LinkedList<EmptyBoundaryWorkUnitInstructions>(toBeResent);
                for (EmptyBoundaryWorkUnitInstructions i : dispatched.keySet())
                    this.toBeResent.add(i);
                this.workUnitFactory = workUnitFactory;
                this.numberOfResultsReceived = new AtomicLong(numberOfResultsReceived.get());
                this.dateSerialized = dateSerialized;
                this.numberOfCompletedPuzzles = numberOfCompletedPuzzles;
            }
    }

    public AtomicLong getNumberOfResultsReceived()
    {
        return numberOfResultsReceived;
    }

    public int getJobCount()
    {
        return jobCount;
    }

    public LinkedList<EmptyBoundaryWorkUnitInstructions> getResent()
    {
        return toBeResent;
    }

    public EmptyBoundaryWorkUnitFactory getEmptyBoundaryWorkUnitFactory()
    {
        return workUnitFactory;
    }

    public Date getDate()
    {
        return dateSerialized;
    }

    public int getNumberOfCompletedPuzzles()
    {
        return numberOfCompletedPuzzles;
    }
}
