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
    private static final Map<ConnectionThread,List<Integer>> clientWorkUnitMap = new HashMap<>();

    // this keeps a copy of all the work units that are currently checked out over the network
    // maps unique EmptyBoundaryWorkUnit IDs to the units themselves
    private static final Map<Integer,EmptyBoundaryWorkUnit> backupUnitMap = new HashMap<>();

    // this stores all the completed puzzles
    public static final List<ImmutablePatch> completedPatches = Collections.synchronizedList(new LinkedList<ImmutablePatch>());

    // parameters for networking
    public static final int LISTENING_PORT = Preinitializer.LISTENING_PORT;
    public static final int TIMEOUT = 1; // seconds to wait before declaring a node unreachable

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
        ThreadMonitor threadMonitor = new ThreadMonitor(1.0); // monitoring interval in seconds

        // wait briefly to let things get going
        pause(2000);

        // start accepting connections and wait for all jobs to complete
        ServerSocket listener = null;
        Socket connection = null;
        ConnectionThread connectionThread = null;
        
        System.out.println("Listening on port " + LISTENING_PORT + "...");

        while ( true )
            {
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
        threadMonitor.stop();
        pause(1000);
        System.out.print("\nAll jobs complete!  Writing completed patches to disk...");
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
    {
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
                    clientWorkUnitMap.put( this, new ArrayList<Integer>() );
                }
        }
    
        @SuppressWarnings("deprecation")
        public void run()
        {
            while (true)
                {
                    try
                        {
                            Object incomingObject = incomingObjectStream.readObject();
                            if ( incomingObject instanceof EmptyWorkUnitResult )
                                {
                                    // this is an incoming result
                                    EmptyWorkUnitResult result = (EmptyWorkUnitResult)incomingObject;
                                    
                                    // retrieve the contents of this EmptyWorkUnitResult
                                    int jobID = result.uniqueID();
                                    System.out.println("received ID " + jobID);
                                    List<ImmutablePatch> localCompletedPatches = result.getLocalCompletedPatches();

                                    // store results centrally
                                    Server.completedPatches.addAll( localCompletedPatches );

                                    // mark job as finished
                                    synchronized (clientWorkUnitMap)
                                        {
                                            List<Integer> list = clientWorkUnitMap.get(this);
                                            boolean success = list.remove(Integer.valueOf(result.uniqueID()));
                                            if ( !success )
                                                System.out.println("error in clientWorkUnitMap!");
                                        }

                                    // remove backup unit
                                    synchronized (backupUnitMap)
                                        {
                                            EmptyBoundaryWorkUnit unit = backupUnitMap.remove(Integer.valueOf(result.uniqueID()));
                                            if ( unit == null )
                                                System.out.println("error in backup unit map!");
                                        }

                                    // print a report
                                    Date currentDate = new Date();
                                    String dateString = String.format("%02d:%02d:%02d", currentDate.getHours(), currentDate.getMinutes(), currentDate.getSeconds());
                                    String statusString = String.format("[ %s ] : Received result %s ", dateString, result.uniqueID());
                                    if ( localCompletedPatches.size() > 0 )
                                        statusString = statusString + "(" + localCompletedPatches.size() + " new completed puzzles) ";
                                    statusString = statusString + "from " + address;
                                    System.out.println(statusString);

                                }
                            else if ( incomingObject instanceof Integer )
                                {
                                    // this is a request for new jobs
                                    System.out.println("request for new jobs received");
                                    int jobCount = (Integer)incomingObject;
                                    for (int i=0; i < jobCount; i++)
                                        {

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

                                            if ( r == null )
                                                {
                                                    i--;
                                                    System.out.println("waiting for more jobs to enter queue");
                                                    continue;
                                                }

                                            Map<RunnableFuture,EmptyBoundaryWorkUnit> jobMap = ThreadService.INSTANCE.getExecutor().jobMap;
                                            EmptyBoundaryWorkUnit unit = null;
                                            synchronized(jobMap)
                                                {
                                                    unit = jobMap.remove(r);
                                                }

                                            if ( unit == null )
                                                {
                                                    System.out.println("unexpected failure in job stealer");
                                                    break;
                                                }

                                            try
                                                {
                                                    synchronized (sendLock)
                                                        {
                                                            System.out.println("sending job " + unit.uniqueID());
                                                            outgoingObjectStream.writeObject(unit);
                                                            outgoingObjectStream.flush();
                                                            outgoingObjectStream.reset();
                                                        }
                                                }
                                            catch (IOException e)
                                                {
                                                    System.out.println("Error sending unit!  Requeued.");

                                                    // resubmit the unit to the local queue
                                                    ThreadService.INSTANCE.getExecutor().submit(unit);
                                                }
                                            
                                            // keep track of which work units have been sent out
                                            synchronized (Server.clientWorkUnitMap)
                                                {
                                                    List<Integer> thisList = Server.clientWorkUnitMap.get(this);
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

            // this thread is not running anymore, so remove it from the clientWorkUnitMap
            synchronized (clientWorkUnitMap)
                {
                    clientWorkUnitMap.remove(this);
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
                                    System.out.println("Signalling " + t.address + " to close.");
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
    }

    private static class ThreadMonitor
    {
        private Timer timer;
        private double updateInterval;
        private static ThreadService executorService = ThreadService.INSTANCE;
        private Date lastUpdateTime = null;
        private Date startTime = new Date();
        private EvictingQueue<Double> throughputs = EvictingQueue.create(500);

        private int lastNumberOfCompletedPatches = 0;
        private final String INTERIM_RESULTS_FILENAME = Preinitializer.SERIALIZATION_DIRECTORY + "/interimResults.chk";

        public ThreadMonitor(double updateInterval) // seconds
        {
            this.updateInterval = updateInterval;
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), (int)updateInterval*1000, (int)updateInterval*1000);
            System.out.println("Thread monitor started.\n");
        }

        public void stop()
        {
            timer.cancel();
            System.out.println("\nThread monitor stopped.");
        }

        private class CustomTimerTask extends TimerTask
        {
            public void run()
            {
                // check for kill file
                File killFile = new File("kill.txt");
                if ( killFile.isFile() )
                    {
                        System.out.println("Server has found kill file!  Shutting down!");
                        System.exit(1);
                    }
                
                // compute statistics
                // jobsRun is the number of jobs run in the last monitorInterval; simultaneously resets counter
                long jobsRun = executorService.getExecutor().getNumberOfSolveCalls(); 

                // this accounts for the fact that the timer might be occasionally delayed
                Date currentTime = new Date();
                if ( lastUpdateTime == null )
                    {
                        lastUpdateTime = currentTime;
                        return;
                    }
                double elapsedTime = ( currentTime.getTime() - lastUpdateTime.getTime() ) / 1000.0;
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

                int numberOfCompletedPatches = Server.completedPatches.size();

                // print statistics
                lastUpdateTime = currentTime;
                ThreadService.INSTANCE.getExecutor().printQueues(throughput, average, totalTime, numberOfCompletedPatches);
            }
        }
    }
}
