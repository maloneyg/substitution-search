import java.util.*;
import java.net.*;
import java.util.concurrent.*;

// this is a singleton
public class ClientDatabase
{
    public static final ClientDatabase INSTANCE = new ClientDatabase();

    private final ArrayList<Long> IDlist = new ArrayList<>();
    private final ArrayList<EmptyBoundaryWorkUnit> unitList = new ArrayList<>();
    private final ArrayList<Server.ConnectionThread> threadList = new ArrayList<>();
    private final Object internalLock = new Object();
    private final ThreadService executorService = ThreadService.INSTANCE;

    private ClientDatabase()
    {
        if ( INSTANCE != null )
            throw new IllegalStateException("double instantiation!");
    }

    public int jobsCheckedOut()
    {
        synchronized (internalLock)
            {
                return IDlist.size();
            }
    }

    public void markAsStarted(Server.ConnectionThread t, EmptyBoundaryWorkUnit u)
    {
        if ( t==null || u == null )
            throw new IllegalArgumentException("markAsStarted does not allow nulls!");
        synchronized (internalLock)
            {
                if ( numberOfEntriesFor(u.uniqueID()) > 0 )
                    throw new IllegalArgumentException("Cannot add the same ID twice (" + u.uniqueID() + ")!");
                IDlist.add(u.uniqueID());
                threadList.add(t);
                unitList.add(u);
            }
    }

    public void markAsFinished(Server.ConnectionThread t, Long l)
    {
        if ( t == null || l == null )
            throw new IllegalArgumentException("markAsStarted does not allow nulls!");
        synchronized (internalLock)
            {
                int hits = numberOfEntriesFor(l);
                if ( hits != 1 )
                    throw new IllegalArgumentException("Unexpected number of instances of ID " + l + " (" + hits + " hits)!");
                int i = locate(l);
                threadList.remove(i);
                IDlist.remove(i);
                unitList.remove(i);
            }
    }

    public void requeueDead()
    {
        synchronized (internalLock)
            {
                for (int i=0; i < threadList.size(); i++)
                    {
                        checkConsistency();

                        Server.ConnectionThread t = threadList.get(i);
                        if ( t.isAlive() )
                            continue;
                        Long l = IDlist.get(i);
                        EmptyBoundaryWorkUnit u = unitList.get(i);

                        // re-queue unit on server
                        System.out.print("Requeueing unit " + l + " (original host: " + t.address + ")...");
                        u.serverEventualPatches(); // so that completed puzzles get redirected to the common pool of results
                        executorService.getExecutor().submit(u);

                        // remove this entry from the database
                        threadList.remove(i);
                        IDlist.remove(i);
                        unitList.remove(i);

                        i=0; // reset loop counter
                        System.out.println("done.");
                    }
            }
    }

    private void checkConsistency()
    {
        int size1 = threadList.size();
        int size2 = unitList.size();
        int size3 = IDlist.size();
        if ( ! (size1 == size2 && size2 == size3) )
            throw new IllegalStateException("problem in client database!");
    }

    private int numberOfEntriesFor(Long l0)
    {
        int hits = 0;
        for (Long l1 : IDlist)
            if ( l0.equals(l1) )
                hits++;
        return hits;
    }

    private int locate(Long id)
    {
        int location = -1;
        for (int i=0; i < IDlist.size(); i++)
            if ( IDlist.get(i).equals(id) )
                location = i;
        return location;
    }
}
