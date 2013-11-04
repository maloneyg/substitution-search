import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.*;

// This class will serve up work units to remote clients. 

public class MutableParadigmServer
{
    public static final int LISTENING_PORT = 32007;
    public static final double MONITOR_INTERVAL = 1.0; // seconds
    public static final int TIMEOUT = 1; // how many seconds to wait before declaring a node unreachable

    public static final List<BasicPatch> allCompletedPatches = new ArrayList<BasicPatch>();
    public static final String RESULT_FILENAME = "results.chk";

    public static AtomicLong numberOfResultsReceived = new AtomicLong(0L);
    public static volatile boolean finished = false;

    private static List<ConnectionThread> LIVE_CONNECTIONS = new ArrayList<ConnectionThread>(); 
    private static List<ConnectionThread> ALL_CONNECTIONS = new ArrayList<ConnectionThread>(); 

    private static WorkUnitFactory workUnitFactory = WorkUnitFactory.createWorkUnitFactory();

    private MutableParadigmServer()
    {
        throw new RuntimeException("this should not be instantiated!");
    }

    public static void main(String[] args)
    {
        // launch the thread that will give periodic reports
        ThreadMonitor threadMonitor = new ThreadMonitor();

        // listen for connections
        ServerSocket listener = null;
        Socket connection = null;
        ConnectionThread connectionThread = null;

        System.out.println("Listening on port " + LISTENING_PORT + "...");
        
        while (finished == false)
            {
                try
                    {
                        listener = new ServerSocket(LISTENING_PORT);
                        listener.setSoTimeout(TIMEOUT*1000);
                        connection = listener.accept();
                        System.out.print("[ " + new Date().toString() + " ] Opened a socket to " + connection.getInetAddress() + ".\n");
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

        System.out.println("Program complete.  " + numberOfResultsReceived + " work units were processed, comprising " + allCompletedPatches.size() + " completed puzzles.");
        
        // write results to file 
        if (allCompletedPatches.size() > 0)
            {
                TriangleResults triangleResults = new TriangleResults(allCompletedPatches);
                try
                    {
                        FileOutputStream fileOut = new FileOutputStream(RESULT_FILENAME);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(triangleResults);
                        out.close();
                        fileOut.close();
                        System.out.println("Wrote results to " + RESULT_FILENAME + ".");
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
            }
        System.exit(0);
    }

    private static class ConnectionThread extends Thread
    {
        private Socket connection = null;
        private InputStream incomingStream;
        private ObjectInputStream incomingObjectStream;
        private OutputStream outgoingStream;
        private ObjectOutputStream outgoingObjectStream;
   
        public static final String HANDSHAKE = "TriangleHandshake";
        public static final String CLOSE = "TriangleClose";
        public static Object sendLock = new Object();
        public final String address;

        private static int jobCount = 0;
        protected static HashMap<WorkUnitInstructions,ConnectionThread> dispatched = new HashMap<>();
        protected static LinkedList<WorkUnitInstructions> toBeResent = new LinkedList<>();

        public static final int BATCH_SIZE = Preinitializer.BATCH_SIZE;

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
                    MutableParadigmServer.LIVE_CONNECTIONS.add(this);
                    MutableParadigmServer.ALL_CONNECTIONS.add(this);
                }
        }

        @SuppressWarnings("deprecation")
        public void run()
        {
            int jobsSent = 0;
            main:
            while ( Thread.interrupted() == false )
                {
                    // check if all results have been received
                    System.out.print("jobs finished: " + numberOfResultsReceived + "\r");
                    //+ " outstanding: " + outstandingResults.size() + " jobsSent: " + jobsSent);
                    if ( numberOfResultsReceived.get() > 0 && dispatched.size() == 0 &&
                         toBeResent.size() == 0 && jobsSent == 0 )
                        {
                            closeAllConnections();
                            finished = true;
                            break;
                        }
                    try
                        {
                            Object incomingObject = incomingObjectStream.readObject();
                            if ( incomingObject instanceof PatchResult )
                                {
                                    // this is an incoming result
                                    PatchResult result = (PatchResult)incomingObject;
                                    numberOfResultsReceived.addAndGet(result.getNumberOfUnits());
                                    int jobID = result.getID();
                                    
                                    List<BasicPatch> localCompletedPatches = result.getCompletedPatches();
                                    allCompletedPatches.addAll( localCompletedPatches );
                                    Date currentDate = new Date();
                                    String dateString = String.format("%02d:%02d:%02d", currentDate.getHours(), currentDate.getMinutes(), currentDate.getSeconds());
                                    System.out.println("[ " + dateString + " ] Received " + result + " (" + allCompletedPatches.size() + " finished puzzles total) from " + address);

                                    // mark job as finished
                                    WorkUnitInstructions toBeRemoved = null;
                                    synchronized (sendLock)
                                        {
                                            for (WorkUnitInstructions i : dispatched.keySet())
                                                {
                                                    if (i.getID() == jobID)
                                                        {
                                                            toBeRemoved = i;
                                                            break;
                                                        }
                                                }
                                            if ( toBeRemoved == null )
                                                System.out.println("Warning, problem in the job database!");
                                            else
                                                dispatched.remove(toBeRemoved);
                                        }
                                }
                            else if ( incomingObject instanceof Integer )
                                {
                                    // this is a request for new jobs
                                    int numberOfNewJobs = (Integer)incomingObject;
                                    //System.out.println("received request for " + numberOfNewJobs + " new jobs from " + address);
                                    
                                    jobsSent = provideJobs(numberOfNewJobs);
                                    //if (jobsSent > 0 )
                                    //    System.out.println(jobsSent + " new jobs have been sent to " + address);
                                    if (jobsSent == 0)
                                        {
                                            System.out.println("No more instructions to create.");
                                        }
                                }
                        }
                    catch (EOFException | SocketException e)
                        {
                            System.out.println("Connection lost.");
                            break;
                        }
                    catch (Exception e)
                        {
                            e.printStackTrace();
                            break;
                        }
                }

            // close connection
            try
                {
                    // close socket
                    if ( connection != null && connection.isConnected() )
                        connection.close();
                }
            catch (IOException e)
                {
                    e.printStackTrace();
                }
        
            synchronized(LIVE_CONNECTIONS)
                {
                    MutableParadigmServer.LIVE_CONNECTIONS.remove(this);
                }
            System.out.println("Connection to " + address + " closed.");
        }

        public boolean isConnected()
        {
            if ( connection == null | connection.isClosed() )
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
                                            t.outgoingObjectStream.writeObject(CLOSE);
                                            t.outgoingObjectStream.flush();
                                            t.outgoingObjectStream.close();
                                        }
                                    catch (Exception e)
                                        {
                                            e.printStackTrace();
                                        }
                                }
                        }
                }
        }

        public int provideJobs(int numberOfNewJobs) throws IOException
        {
            int jobsSent = 0;
            synchronized(sendLock)
                {
                    while (true)
                        {
                            if (toBeResent.size() == 0 || jobsSent == numberOfNewJobs)
                                break;
                            
                            WorkUnitInstructions theseInstructions = toBeResent.removeFirst();

                            // send instructions
                            outgoingObjectStream.writeObject(theseInstructions);
                            outgoingObjectStream.flush();
                            outgoingObjectStream.reset();

                            // make a note of which instructions have gone out
                            dispatched.put(theseInstructions, this);
                            System.out.println("Re-dispatched job " + theseInstructions.getID() + " to " + address + ".");

                            // keep track of how many jobs got sent in this function call
                            jobsSent++;
                        }
                }

            while (jobsSent < numberOfNewJobs)
                {
                    synchronized (sendLock)
                        {
                            // check to see if we've run out of jobs to send
                            if (!workUnitFactory.notDone())
                                break;

                            // create the next set of instructions
                            jobCount++;
                            WorkUnitInstructions theseInstructions = workUnitFactory.getInstructions(BATCH_SIZE,jobCount);
                            
                            // send instructions
                            outgoingObjectStream.writeObject(theseInstructions);
                            outgoingObjectStream.flush();
                            outgoingObjectStream.reset();

                            // make a note of which instructions have gone out
                            dispatched.put(theseInstructions, this);

                            // keep track of how many jobs got sent in this function call
                            jobsSent++;
                        }
                }
            return jobsSent;
        }
    }

    private static class ThreadMonitor
    {
        private Timer timer;
        private static HashMap<WorkUnitInstructions,ConnectionThread> dispatched = MutableParadigmServer.ConnectionThread.dispatched; 
        private static LinkedList<WorkUnitInstructions> toBeResent = MutableParadigmServer.ConnectionThread.toBeResent;
        private static Object sendLock = MutableParadigmServer.ConnectionThread.sendLock;

        public ThreadMonitor()
        {
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), (int)(MutableParadigmServer.MONITOR_INTERVAL*1000), (int)(MutableParadigmServer.MONITOR_INTERVAL*1000));
        }

        private class CustomTimerTask extends TimerTask
        {
            public void run()
            {
                //System.out.print("Live connections: " + MutableParadigmServer.LIVE_CONNECTIONS.get() + "\r");
                File killFile = new File("kill.txt");
                if ( killFile.isFile() )
                    {
                        System.out.println("\nKill file detected.  Shutting down...");
                        System.exit(1);
                    }

                // re-queue any missed instructions
                synchronized(sendLock)
                    {
                        for (ConnectionThread t : MutableParadigmServer.ALL_CONNECTIONS)
                            {
                                if (!t.isConnected())
                                    {
                                        LinkedList<WorkUnitInstructions> resend = new LinkedList<>();
                                        for (WorkUnitInstructions i : dispatched.keySet())
                                            {
                                                ConnectionThread t2 = dispatched.get(i);
                                                if ( t == t2 )
                                                    {
                                                        resend.add(i);
                                                        System.out.println("Marked instruction number " + i.getID() + " for re-dispatch (originally sent to " + t.getHostName() + ")");
                                                    }
                                            }
                                        for (WorkUnitInstructions i : resend)
                                            {
                                                dispatched.remove(i);
                                                toBeResent.add(i);
                                            }
                                    }
                            }
                    }
            }
        }
    }
}
