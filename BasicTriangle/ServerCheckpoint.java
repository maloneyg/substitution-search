import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public class ServerCheckpoint implements Serializable
{
    private final int jobCount;
    private final LinkedList<WorkUnitInstructions> toBeResent;
    private final WorkUnitFactory workUnitFactory;
    private final AtomicLong numberOfResultsReceived;
    private final Date dateSerialized;
    private final int numberOfCompletedPuzzles;

    public ServerCheckpoint(int jobCount, HashMap<WorkUnitInstructions,MutableParadigmServer.ConnectionThread> dispatched,
                            LinkedList<WorkUnitInstructions> toBeResent, Object sendLock,
                            WorkUnitFactory workUnitFactory, AtomicLong numberOfResultsReceived, Date dateSerialized,
                            int numberOfCompletedPuzzles)
    {
        synchronized(sendLock)
            {
                this.jobCount = jobCount;
                this.toBeResent = new LinkedList<WorkUnitInstructions>(toBeResent);
                for (WorkUnitInstructions i : dispatched.keySet())
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

    public LinkedList<WorkUnitInstructions> getResent()
    {
        return toBeResent;
    }

    public WorkUnitFactory getWorkUnitFactory()
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
