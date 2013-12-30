import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.common.collect.*;
import java.util.concurrent.atomic.*;

public class Server
{
    public static final ThreadService executorService = ThreadService.INSTANCE;

    // these maps keep track of all the connections we have
    private static List<ConnectionThread> LIVE_CONNECTIONS = new ArrayList<ConnectionThread>();
    private static List<ConnectionThread> ALL_CONNECTIONS = new ArrayList<ConnectionThread>();

    // this keeps track of which jobs have been dispatched
    // maps ConnectionThreads to the unique IDs of each work unit that the thread is working on
    private static final Map<ConnectionThread,List<Long>> clientWorkUnitMap = new HashMap<>();

    // this keeps a copy of all the work units that are currently checked out over the network
    // maps unique EmptyBoundaryWorkUnit IDs to the units themselves
    private static final Map<Long,EmptyBoundaryWorkUnit> backupUnitMap = new HashMap<Long,EmptyBoundaryWorkUnit>();


    // this stores all the completed puzzles
    public static final List<ImmutablePatch> completedPatches = new LinkedList<ImmutablePatch>();

    // parameters for networking
    public static final int LISTENING_PORT = Preinitializer.LISTENING_PORT;
    public static final int TIMEOUT = 1; // seconds to wait before declaring a node unreachable
    private static Date lastRespawn = new Date();

    // prevent instantiation
    private Server()
        {
            throw new RuntimeException("you aren't supposed to instantiate this!");
        }

    public static void main(String[] args)
    {
        // create initial work
        EmptyBoundaryWorkUnitFactory factory = EmptyBoundaryWorkUnitFactory.createEmptyBoundaryWorkUnitFactory();
        System.out.print("Submitting initial units: ");
        while ( factory.notDone() )
            {
                // submit jobs to queue
                EmptyBoundaryWorkUnit thisUnit = factory.nextWorkUnit();
                executorService.getExecutor().submit(thisUnit);
                System.out.print(thisUnit.hashCode() + " (" + thisUnit.uniqueID() + ") ");
            }
        System.out.println();

        // start monitoring thread
        ThreadMonitor threadMonitor = new ThreadMonitor(2.0); // monitoring interval in seconds

        // wait briefly to let things get going
        pause(2000);

        // start accepting connections and wait for all jobs to complete
        ServerSocket listener = null;
        Socket connection = null;
        ConnectionThread connectionThread = null;
        
        System.out.println("Listening on port " + LISTENING_PORT + "...");

        while ( true )
            {
                //if (  executorService.getExecutor().getNumberOfRunningJobs() == 0 )
                //    System.out.println(backupUnitMap.size());
                if ( executorService.getExecutor().getNumberOfRunningJobs() == 0 &&
                     executorService.getExecutor().getQueue().size() == 0 &&
                     backupUnitMap.size() == 0 )
                    break;
                
                try
                    {
                        listener = new ServerSocket(LISTENING_PORT);
                        listener.setSoTimeout(TIMEOUT*1000);
                        connection = listener.accept();
                        System.out.print("[ " + new Date().toString() + " ] Opened a socket to " +
                        connection.getInetAddress().getCanonicalHostName() + " (" + connection.getInetAddress() + ").\n");
                        listener.close();
                        connectionThread = new ConnectionThread(connection);
                        connectionThread.checkConnection();
                        System.out.println("Handshake successful.\n");
                        connectionThread.start();
                    }
                catch (BindException e)
                    {
                        if (e.getMessage().equals("Address already in use"))
                            System.out.println("A triangle server is already running on this port!");
                        else
                            e.printStackTrace();
                        System.exit(1);
                    }
                catch (SocketTimeoutException e)
                    {
                        try
                            {
                                if ( listener != null )
                                    listener.close();
                            }
                        catch (Exception e2)
                            {
                                e2.printStackTrace();
                                System.exit(1);
                            }
                    }
                catch (ConnectException e)
                    {
                        System.out.println(e.getMessage());
                    }
                catch (EOFException e)
                    {
                        System.out.println("Connection to " + connection.getInetAddress() + " closed unexpectedly.");
                        synchronized(LIVE_CONNECTIONS)
                            {
                                LIVE_CONNECTIONS.remove(connectionThread);
                            }
                    }
                catch (Exception e)
                    {
                        System.out.println("Server shutdown:");
                        e.printStackTrace();
                        break;
                    }
            }

        // write out all results
        ConnectionThread.closeAllConnections();
        threadMonitor.stop();
        pause(3000);
        System.out.print("\nAll jobs complete!  Writing completed patches to disk...");
        synchronized (completedPatches)
            {
                if ( completedPatches.size() == 0 )
                    System.out.println("no results to write.");
                else
                    {
                        try
                            {
                                TriangleResults triangleResults = new TriangleResults(completedPatches);
                                FileOutputStream fileOut = new FileOutputStream(Preinitializer.RESULT_FILENAME);
                                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                                out.writeObject(triangleResults);
                                out.close();
                                fileOut.close();
                                System.out.println("wrote " + completedPatches.size() + " results to " + Preinitializer.RESULT_FILENAME + ".");
                            }
                        catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                    }
            }
        System.out.println("Have a nice day!");
        System.exit(0);
    }

    public static void pause(long delay)
    {
                try
                    {
                        Thread.sleep(delay);
                    }
                catch (InterruptedException e)
                    {
                    }
    }

    public static class ConnectionThread extends Thread
    { // begin ConnectionThread
        private Socket connection = null;
        private InputStream incomingStream;
        private ObjectInputStream incomingObjectStream;
        private OutputStream outgoingStream;
        private ObjectOutputStream outgoingObjectStream;

        public static final String HANDSHAKE = "TriangleHandshake";
        public static final String CLOSE = "TriangleClose";
        public static final Object sendLock = new Object();
        public final String address;

        public ConnectionThread(Socket connection)
        {
            this.connection = connection;
            address = connection.getInetAddress().getCanonicalHostName();
        }

        // a method for dealing with incoming results
        @SuppressWarnings("deprecation")
        private void stashResult(EmptyWorkUnitResult result)
        {
            // retrieve the contents of this EmptyWorkUnitResult
            long jobID = result.uniqueID();
            //System.out.println("received ID " + jobID);
            List<ImmutablePatch> localCompletedPatches = result.getLocalCompletedPatches();

            // store results centrally
            synchronized (completedPatches)
                {
                    Server.completedPatches.addAll( localCompletedPatches );
                }

            // mark job as finished
            synchronized (clientWorkUnitMap)
                {
                    List<Long> list = clientWorkUnitMap.get(this);
                    //System.out.println("Trying to remove " + result.uniqueID() + " from: " + list);
                    boolean success = list.remove(Long.valueOf(result.uniqueID()));
                    if ( !success )
                        System.out.println("error in clientWorkUnitMap!  expected to find ID " + result.uniqueID());
                }

            // remove backup unit
            synchronized (backupUnitMap)
                {
                    EmptyBoundaryWorkUnit unit = backupUnitMap.remove(Long.valueOf(result.uniqueID()));
                    if ( unit == null )
                        System.out.println("error in backup unit map!");
                    //System.out.println(backupUnitMap.keySet());
                }

            // print a report
            Date currentDate = new Date();
            String dateString = String.format("%02d:%02d:%02d", currentDate.getHours(), currentDate.getMinutes(), currentDate.getSeconds());
            String statusString = "";
            /*if ( localCompletedPatches.size() > 0 )
                {
                    statusString = String.format("\n[ %s ] : Received result %s ", dateString, result.uniqueID());
                    statusString = statusString + "(" + localCompletedPatches.size() + " new completed puzzles) ";
                    statusString = statusString + "from " + address;
                    System.out.println(statusString);
                }*/
        } // end stashResult

        // set streams and handshake
        public void checkConnection() throws SocketException, ConnectException, SocketTimeoutException, IOException, ClassNotFoundException
        {
            // set streams
            outgoingStream = connection.getOutputStream();
            outgoingStream.flush();
            outgoingObjectStream = new ObjectOutputStream(outgoingStream);
            incomingStream = connection.getInputStream();
            incomingObjectStream = new ObjectInputStream(incomingStream);

            // receive handshake
            Object incomingObject = incomingObjectStream.readObject();
            if ( incomingObject instanceof String )
                {
                    String thisString = (String)incomingObject;
                    if ( ! thisString.equals(HANDSHAKE) )
                        throw new ConnectException("Error handshaking -- wrong text!");
                }
            else
                throw new ConnectException("Error handshaking -- wrong object type!");

            // send handshake
            outgoingObjectStream.writeObject(HANDSHAKE);
            outgoingObjectStream.flush();
            
            // keep track of the live connections
            synchronized(LIVE_CONNECTIONS)
                {
                    Server.LIVE_CONNECTIONS.add(this);
                    Server.ALL_CONNECTIONS.add(this);
                }

            // add entry to clientWorkUnitMap
            synchronized(Server.clientWorkUnitMap)
                {
                    clientWorkUnitMap.put( this, new ArrayList<Long>() );
                }
        }
    
        @SuppressWarnings("deprecation")
        public void run()
        {
            while (true)
                {
                    try
                        {
                            //System.out.println("reading");
                            Object incomingObject = incomingObjectStream.readObject();
                            //System.out.println("read object");
                            if ( incomingObject instanceof EmptyBatch )
                                {
                                    // this is a returning set of units and spawn
                                    EmptyBatch batch = (EmptyBatch)incomingObject;

                                    // mark jobs as finished
                                    List<EmptyWorkUnitResult> results = batch.getResults();
                                    for (EmptyWorkUnitResult res : results)
                                        stashResult(res);

                                    // add spawn to queue
                                    for (EmptyBoundaryWorkUnit unit : batch.getNewUnits()) {
                                        unit.setKillSwitch(new AtomicBoolean(false));
                                        ThreadService.INSTANCE.getExecutor().submit(unit);
                                    }

                                }
                            else if ( incomingObject instanceof EmptyWorkUnitResult )
                                {
                                    // this is an incoming result
                                    EmptyWorkUnitResult result = (EmptyWorkUnitResult)incomingObject;
                                    
                                    // file it away
                                    stashResult(result);

                                }
                            else if ( incomingObject instanceof Integer )
                                {
                                    // this is a request for new jobs
                                    //System.out.println("request for new jobs received");
                                    int jobCount = (Integer)incomingObject;
                                    for (int i=0; i < jobCount; i++)
                                        {
                                            // don't send out any new jobs until there's been enough time to build up more stuff in the queue
                                            if ( new Date().getTime() - Server.lastRespawn.getTime()< 3*Preinitializer.SPAWN_MIN_TIME)
                                                break;


                                            // attempt to send a job from the queue
                                            // if there are no jobs in the queue, wait
                                            Runnable r = null;
                                            try
                                                {
                                                    r = ThreadService.INSTANCE.getExecutor().getQueue().poll(1000L, TimeUnit.MILLISECONDS);
                                                }
                                            catch (InterruptedException e)
                                                {
                                                }

                                            // if there's nothing to send back, do nothing
                                            if ( r == null )
                                                break;

                                            Map<RunnableFuture,EmptyBoundaryWorkUnit> jobMap = ThreadService.INSTANCE.getExecutor().jobMap;
                                            EmptyBoundaryWorkUnit unit = null;
                                            synchronized(jobMap)
                                                {
                                                    unit = jobMap.remove(r);
                                                    unit.newEventualPatches();
                                                    //System.out.println("removed unit " + unit.uniqueID());
                                                }

                                            try
                                                {
                                                    synchronized (sendLock)
                                                        {
                                                            //System.out.println("sending unit " + unit.uniqueID());
                                                            outgoingObjectStream.writeObject(unit);
                                                            //System.out.println("done sending unit " + unit.uniqueID());
                                                            outgoingObjectStream.flush();
                                                            outgoingObjectStream.reset();
                                                        }
                                                }
                                            catch (IOException e)
                                                {
                                                    System.out.println("Error sending unit " + unit.uniqueID() + "!  Requeued.");

                                                    // resubmit the unit to the local queue
                                                    unit.serverEventualPatches();
                                                    ThreadService.INSTANCE.getExecutor().submit(unit);
                                                    continue;
                                                }
                                            
                                            // keep track of which work units have been sent out
                                            synchronized (Server.clientWorkUnitMap)
                                                {
                                                    List<Long> thisList = Server.clientWorkUnitMap.get(this);
                                                    thisList.add(unit.uniqueID());
                                                }

                                            // keep a copy of this job in case it dies
                                            synchronized (Server.backupUnitMap)
                                                {
                                                    Server.backupUnitMap.put(unit.uniqueID(), unit);
                                                }
                                        }
                                }
                        }
                    catch (EOFException | SocketException e)
                        {
                            System.out.println("Connection to " + address + " lost.");
                            break;
                        }
                    catch (Exception e)
                        {
                            System.out.println("Unexpected exception in Server.ConnectionThread.run():");
                            e.printStackTrace();
                            break;
                        }
            }

            synchronized (LIVE_CONNECTIONS)
                {
                    LIVE_CONNECTIONS.remove(this);
                }
        }

        public boolean isConnected()
        {
            if ( connection == null || connection.isClosed() )
                return false;
            return true;
        }

        public String getHostName()
        {
            return address;
        }

        public static void closeAllConnections()
        {
            synchronized(LIVE_CONNECTIONS)
                {
                    for (ConnectionThread t : LIVE_CONNECTIONS)
                        {
                            if ( t.connection.isConnected() )
                                {
                                    System.out.println("\nSignalling " + t.address + " to close.");
                                    try
                                        {
                                            synchronized(t.sendLock)
                                                {
                                                    t.outgoingObjectStream.writeObject(CLOSE);
                                                    t.outgoingObjectStream.flush();
                                                    t.outgoingObjectStream.close();
                                                }
                                        }
                                    catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                }
                        }
                }
        }
    } // end ConnectionThread

    private static class ThreadMonitor
    {
        private Timer timer;
        private double updateInterval;
        private static ThreadService executorService = ThreadService.INSTANCE;
        private Date lastUpdateTime = null;
        private Date startTime = new Date();
        private EvictingQueue<Double> throughputs = EvictingQueue.create(500);
        private static AtomicBoolean cancelled = new AtomicBoolean();
        private int lastNumberOfCompletedPatches = 0;
        private final String INTERIM_RESULT_FILENAME = Preinitializer.INTERIM_RESULT_FILENAME;
        private Date lastInterimWrite = new Date();

        public ThreadMonitor(double updateInterval) // seconds
        {
            this.updateInterval = updateInterval;
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), (int)updateInterval*1000, (int)updateInterval*1000);
            System.out.println("Thread monitor started.\n");
        }

        public void stop()
        {
            cancelled.set(true);
            timer.cancel();
            System.out.println("\nThread monitor stopped.");
        }

        private class CustomTimerTask extends TimerTask
        {
            public void run()
            {
                // abort if cancelled
                if ( cancelled.get() )
                    return;

                // check for kill file
                File killFile = new File("kill.txt");
                if ( killFile.isFile() )
                    {
                        System.out.println("Server has found kill file!  Shutting down!");
                        System.exit(1);
                    }
                
                // if the queue is empty and no jobs are running, send a signal to all clients to spawn
                Date currentTime = new Date();
                if ( ThreadService.INSTANCE.getExecutor().getNumberOfRunningJobs() == 0 &&
                     ThreadService.INSTANCE.getExecutor().getQueue().size()        == 0 &&
                     currentTime.getTime() - Server.lastRespawn.getTime() > 1.5*Preinitializer.SPAWN_MIN_TIME )
                    {
                        lastRespawn = currentTime;
                        synchronized(LIVE_CONNECTIONS)
                            {
                                for (ConnectionThread t : LIVE_CONNECTIONS)
                                    {
                                        if ( t.connection.isConnected() )
                                            {
                                                System.out.println("\nSignaling " + t.address + " to return spawn."); 
                                                try
                                                    {
                                                        synchronized (t.sendLock)
                                                            {
                                                                t.outgoingObjectStream.writeObject(Client.RETURN);
                                                                t.outgoingObjectStream.flush();
                                                            }
                                                    }
                                                catch (Exception e)
                                                    {
                                                        e.printStackTrace();
                                                    }
                                            }
                                    }
                            }
                    }

                // if a connection has died, re-queue the jobs that were dispatched
                synchronized ( clientWorkUnitMap )
                    {
                        List<ConnectionThread> threadsToRemove = new LinkedList<>();

                        for ( ConnectionThread t : clientWorkUnitMap.keySet() )
                            {
                                if ( t.isAlive() )
                                    continue;

                                // if a thread is dead, then find out which unique IDs it had checked out
                                List<Long> IDlist = clientWorkUnitMap.get(t);
                                if ( IDlist.size() == 0 )
                                    {
                                        threadsToRemove.add(t);
                                        continue;
                                    }

                                System.out.println("\nRe-queueing " + IDlist.size() + " jobs from dead thread: " + t.address);
                                
                                // get the EmptyBoundaryWorkUnits that correspond to these IDs
                                HashMap<EmptyBoundaryWorkUnit,Long> unitMap = new HashMap<>();
                                synchronized ( backupUnitMap )
                                    {
                                        // put all the work units that have to be requeued in unitMap
                                        for (Long l : IDlist)
                                            {
                                                EmptyBoundaryWorkUnit unit = backupUnitMap.get(l);
                                                if ( unit == null )
                                                    {
                                                        System.out.println("Error in unit map!  Expected to find a unit for ID " + l);
                                                        System.out.println(backupUnitMap.keySet());
                                                        break;
                                                    }
                                                Long l2 = unitMap.put(unit,l);
                                                if ( l2 != null )
                                                    System.out.println("replaced " + l2 + " with " + l);
                                                if ( unit == null )
                                                    System.out.println("null unit");
                                                if ( l == null )
                                                    System.out.println("null long");
                                            }
                                       // *** just use the unique ID straight from the work unit? 
                                        System.out.println("Re-queueing unit IDs: ");
                                        System.out.println(unitMap.values());
                                        for ( EmptyBoundaryWorkUnit u : unitMap.keySet() )
                                            {
                                                Long l = unitMap.get(u);

                                                if ( u == null )
                                                    System.out.println("2null unit");
                                                if ( l == null )
                                                    System.out.println("2null long");

                                                 // requeue each unit
                                                u.serverEventualPatches();
                                                ThreadService.INSTANCE.getExecutor().submit(u);
                                                System.out.print(u.uniqueID() + " ");

                                                // remove unit from clientWorkMap
                                                boolean success = IDlist.remove(l);
                                                if ( success == false )
                                                    System.out.println("Failed to remove " + l + " from " + IDlist.toString());

                                                // remove unit from backupUnitMap
                                                EmptyBoundaryWorkUnit u2 = backupUnitMap.remove(l);
                                                if ( u2 == null )
                                                    System.out.println("Failed to remove " + l + " from " + backupUnitMap.keySet().toString());
                                            }
                                        System.out.println();
                                    }
                                // if we have successfully re-queued all the dead units, mark this thread for removal
                                if ( clientWorkUnitMap.get(t).size() == 0 )
                                    threadsToRemove.add(t);
                                else
                                    {
                                        System.out.println("Error: failed to re-queue " + IDlist.size() + " jobs: ");
                                        for ( Long l : IDlist )
                                            System.out.print(l + " ");
                                    }
                            }
                        for ( ConnectionThread t : threadsToRemove )
                            {
                                List<Long> list = clientWorkUnitMap.remove(t);
                                if ( list == null )
                                    System.out.println("Error while trying to remove thread!");
                                else
                                    System.out.println("Successfully requeued all jobs that were sent to " + t.address + ".");
                            }
                    }

                // periodically write interim results to disk
                int numberOfCompletedPatches = 0;
                synchronized ( Server.completedPatches )
                    {
                        numberOfCompletedPatches = Server.completedPatches.size();
                    }

                if (    Preinitializer.WRITE_INTERIM_RESULTS == true
                     && numberOfCompletedPatches > lastNumberOfCompletedPatches
                     && currentTime.getTime() - lastInterimWrite.getTime() > 30 * 1000 )
                    {
                        TriangleResults interimResults = null;
                        synchronized (Server.completedPatches)
                            {
                                interimResults = new TriangleResults(Server.completedPatches);
                            }

                        try
                            {
                                FileOutputStream fileOut = new FileOutputStream(INTERIM_RESULT_FILENAME);
                                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                                out.writeObject(interimResults);
                                out.close();
                                fileOut.close();
                                int newResults = numberOfCompletedPatches - lastNumberOfCompletedPatches;
                                System.out.println("\n" + newResults + " new results, so wrote " + numberOfCompletedPatches
                                                   + " interim results to " + INTERIM_RESULT_FILENAME + ".\n");
                            }
                        catch (Exception e)
                            {
                                System.out.println("\nError while writing interim results to " + INTERIM_RESULT_FILENAME + "!");
                                e.printStackTrace();
                            }

                        lastNumberOfCompletedPatches = numberOfCompletedPatches;
                        lastInterimWrite = currentTime;
                    }

                // compute statistics
                // jobsRun is the number of jobs run in the last monitorInterval; simultaneously resets counter
                long jobsRun = executorService.getExecutor().getNumberOfSolveCalls(); 

                // this accounts for the fact that the timer might be occasionally delayed
                currentTime = new Date();
                if ( lastUpdateTime == null )
                    {
                        lastUpdateTime = currentTime;
                        return;
                    }
                double elapsedTime = ( currentTime.getTime() - lastUpdateTime.getTime() ) / 1000.0;
                if ( elapsedTime < 0.1 )
                    return;
                //System.out.println(elapsedTime);
                double throughput = jobsRun / elapsedTime;

                // calculte how long the timer has been running
                double totalTime = ( currentTime.getTime() - startTime.getTime() ) / 1000.0;  // in seconds

                // keep track of how many jobs have been finished
                throughputs.add(throughput);

                // calculate moving average
                double average = 0.0;
                for (Double d : throughputs)
                    average += d;
                average = average / throughputs.size();

                // print statistics
                lastUpdateTime = currentTime;
                ThreadService.INSTANCE.getExecutor().printQueues(throughput, average, totalTime, numberOfCompletedPatches);
            }
        }
    }
}
