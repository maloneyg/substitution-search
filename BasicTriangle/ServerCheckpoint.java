import java.io.*;
import java.util.*;

public class ServerCheckpoint implements Serializable
{
    private final int jobCount;
    private final LinkedList<WorkUnitInstructions> toBeResent;
    private final List<BasicPatch> allCompletedPatches;
    private final WorkUnitFactory workUnitFactory;

    public ServerCheckpoint(int jobCount, HashMap<WorkUnitInstructions,MutableParadigmServer.ConnectionThread> dispatched,
                            LinkedList<WorkUnitInstructions> toBeResent, Object sendLock,
                            List<BasicPatch> allCompletedPatches, WorkUnitFactory workUnitFactory)
    {
        synchronized(sendLock)
            {
                this.jobCount = jobCount;
                this.toBeResent = new LinkedList<WorkUnitInstructions>(toBeResent);
                for (WorkUnitInstructions i : dispatched.keySet())
                    toBeResent.add(i);
                this.allCompletedPatches = new LinkedList<BasicPatch>(allCompletedPatches);
                this.workUnitFactory = workUnitFactory;
            }
    }

    public int getJobCount()
    {
        return jobCount;
    }

    public LinkedList<WorkUnitInstructions> getResent()
    {
        return toBeResent;
    }

    public List<BasicPatch> getAllCompletedPatches()
    {
        return allCompletedPatches;
    }

    public WorkUnitFactory getWorkUnitFactory()
    {
        return workUnitFactory;
    }
}
