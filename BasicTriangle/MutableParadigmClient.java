import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

// This class will serve up work units to remote clients. 

public final class MutableParadigmClient
{
    public static final int LISTENING_PORT = 32007;
    public static String HOST_NAME = "localhost";  // name of the server

    public static final double MONITOR_INTERVAL = 0.5; // seconds
    public static final int TIMEOUT = 1; // how many seconds to wait before declaring a node unreachable
    public static final int MAX_ATTEMPTS = 300; // how many time to try connecting before giving up
    public static final String HANDSHAKE = "TriangleHandshake";
    public static final String CLOSE = "TriangleClose";

    private static Socket connection;
    private static InputStream inputStream;
    private static ObjectInputStream incomingObjectStream;
    private static OutputStream outputStream;
    private static ObjectOutputStream outgoingObjectStream;

    private static ThreadService executorService = ThreadService.INSTANCE;

    private static Object mutableParadigmClientLock = new Object();

    private static WorkUnitFactory workUnitFactory = WorkUnitFactory.createWorkUnitFactory();

    protected static ConcurrentHashMap<WorkUnit,Future<Result>> currentBatch = new ConcurrentHashMap<>();
    protected static AtomicBoolean currentBatchSent = new AtomicBoolean();
    protected static WorkUnitInstructions currentInstructions;

    // prevent instantiation
    private MutableParadigmClient()
    {
        throw new RuntimeException("you aren't supposed to instantiate this");
    }

    public static void main(String[] args)
    {
        // accept a host name from the command line if offered
        if ( args.length > 0 )
            HOST_NAME = args[0];

        ThreadMonitor threadMonitor = new ThreadMonitor();

        int attempts=0;
        while (true)
            {
                try
                    {
                        System.out.print("Attempting to connect to " + HOST_NAME + "...");
                        attempts++;
                        connect();
                        System.out.println("connected to " + connection.getInetAddress() + ".");
                        break;
                    }
                catch (InterruptedException e)
                    {
                        System.out.println("Interrupted!");
                    }
                catch (EOFException e)
                    {
                        System.out.println("Broken pipe; reconnecting.");
                    }
                catch (ConnectException | ClassNotFoundException e)
                    {
                        if ( e.getMessage().equals("Connection refused") )
                            System.out.println("connection refused.");
                        else
                            {
                                System.out.println("Error connecting:");
                                e.printStackTrace();
                            }
                    }
                catch (SocketException e)
                    {
                        System.out.println("Unable to connect, retrying.");
                    }
                catch (UnknownHostException e)
                    {
                        System.out.println("unknown host name...aborting!");
                        System.exit(1);
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                        break;
                    }

                // check if we've exceeded the maximum number of connection attempts
                if ( attempts > MAX_ATTEMPTS )
                    {
                        System.out.println("Maximum number of connection attempts exceeded.");
                        break;
                    }

                // wait before retrying
                System.out.print("Waiting to retry...");
                try
                    {
                        Thread.sleep(2000);
                    }
                catch (InterruptedException e)
                    {
                    }
                System.out.println("done.\n");

            }
        //System.out.println("Client has shut down.");
    }

    private static void connect() throws IOException, InterruptedException, ConnectException, ClassNotFoundException, SocketException, UnknownHostException
    {
        // establish connection
        connection = new Socket(HOST_NAME, LISTENING_PORT);
        //connection.setSoTimeout(TIMEOUT*1000);
        
        // create streams
        outputStream = connection.getOutputStream();
        outputStream.flush();
        outgoingObjectStream = new ObjectOutputStream(outputStream);
        inputStream = connection.getInputStream();
        incomingObjectStream = new ObjectInputStream(inputStream);

        // send handshake
        outgoingObjectStream.writeObject(HANDSHAKE);
        outgoingObjectStream.flush();

        // wait for handshake
        Object incomingObject = incomingObjectStream.readObject();
        if ( incomingObject instanceof String )
            {
                String thisHandshake = (String)incomingObject;
                if ( !thisHandshake.equals(HANDSHAKE) )
                    throw new ConnectException("Error handshaking (wrong handshake).");
            }
        else
            throw new ConnectException("Error handshaking (wrong object type).");

        // get ready to run jobs
        ThreadService executorService = ThreadService.INSTANCE;
        System.out.println("Connected and ready to run jobs.");

        // ask for initial batch of jobs
        Integer numberOfNewJobsNeeded = executorService.NUMBER_OF_THREADS - executorService.getExecutor().getNumberOfRunningJobs() + 1;
        outgoingObjectStream.writeObject(numberOfNewJobsNeeded);
        outgoingObjectStream.flush();
        outgoingObjectStream.reset();

        while (true)
            {
                try
                    {
                        incomingObject = incomingObjectStream.readObject();
                        if ( incomingObject instanceof WorkUnitInstructions )
                            {
                                WorkUnitInstructions instructions = (WorkUnitInstructions)incomingObject;
                                System.out.println("received instruction ID = " + instructions.getID());
                                List<WorkUnit> theseUnits = workUnitFactory.followInstructions(instructions);
                                
                                // wait for current batch to complete
                                while ( currentBatchIsComplete() == false || currentBatchSent.get() == false )
                                    {
                                        try
                                            {
                                                Thread.sleep(100);
                                            }
                                        catch (InterruptedException e)
                                            {
                                            }
                                    }
                                
                                MutableParadigmClient.currentBatch.clear();
                                MutableParadigmClient.currentBatchSent.set(false);
                                MutableParadigmClient.currentInstructions = instructions;
                                for (WorkUnit thisUnit : theseUnits)
                                    {
                                        Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);
                                        currentBatch.put(thisUnit, thisFuture);
                                    }
                            }
                        else if ( incomingObject instanceof String )
                            {
                                String incomingString = (String)incomingObject;
                                if ( incomingString.equals(CLOSE) )
                                    {
                                        System.out.println("Close request received.");
                                        break;
                                    }
                            }
                        else
                            break;
                    }
                catch (SocketException e)
                    {
                        throw new SocketException(e.getMessage());
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                        break;
                    }
            }

        try
            {
                connection.close();
            }
        catch (IOException e)
            {
                e.printStackTrace();
            }
        System.out.println("Connection to server closed.");
        System.exit(0);
    }

    public static boolean currentBatchIsComplete()
    {
        ConcurrentHashMap<WorkUnit,Future<Result>> currentBatch = MutableParadigmClient.currentBatch;
        for (WorkUnit w : currentBatch.keySet())
            {
                Future f = currentBatch.get(w);
                if ( ! f.isDone() )
                    return false;
            }
        return true;
    }

    public static void sendResult()
    {
        // if there is no connection, do nothing
        // this makes this compatible with single-node tests
        if ( connection == null )
            return;

        // if the latest batch of job is complete send back the results
        if ( currentBatchIsComplete() && MutableParadigmClient.currentBatchSent.get() == false)
            {
                synchronized(mutableParadigmClientLock)
                    {
                        // create PatchResult to send
                        int ID = MutableParadigmClient.currentInstructions.getID();
                        LinkedList<BasicPatch> completedPatches = new LinkedList<>();
                        for (WorkUnit w : MutableParadigmClient.currentBatch.keySet())
                            {
                                Future f = MutableParadigmClient.currentBatch.get(w);
                                WorkUnitResult r = null;
                                List<BasicPatch> thesePatches = null;
                                try
                                    {
                                        // cast Result to WorkUnitResult
                                        r = (WorkUnitResult)f.get();
                                        thesePatches = r.getLocalCompletedPatches();
                                    }
                                catch (Exception e)
                                    {
                                        // ignore work units that were cancelled, died from
                                        // an exception, or were interrupted
                                    }
                                if ( thesePatches != null )
                                    completedPatches.addAll(thesePatches);
                            }
                        int numberOfUnits = MutableParadigmClient.currentBatch.size();

                        PatchResult result = new PatchResult(ID, completedPatches, numberOfUnits);

                        try
                            {
                                // send result
                                outgoingObjectStream.writeObject(result);
                                outgoingObjectStream.flush();
                                outgoingObjectStream.reset();

                                // ask for one new job
                                outgoingObjectStream.writeObject(new Integer(1));
                                outgoingObjectStream.flush();
                                outgoingObjectStream.reset();
                                System.out.println("requested 1 more job");
                            }
                        catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                        System.out.println("\nsent " + result.toString());
                        currentBatchSent.set(true);
                    }
            }
    }

    private static class ThreadMonitor
    {
        private Timer timer;
        private double updateInterval;
        private static ThreadService executorService = ThreadService.INSTANCE;
        private Date lastUpdateTime = null;
        private LinkedList<Double> throughputs = new LinkedList<Double>();

        public ThreadMonitor()
        {
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), (int)(MutableParadigmServer.MONITOR_INTERVAL*1000), (int)(MutableParadigmServer.MONITOR_INTERVAL*1000));
        }

        private class CustomTimerTask extends TimerTask
        {
            public void run()
            {
                // check for kill file
                File killFile = new File("kill.txt");
                if ( killFile.isFile() )
                    {
                        System.out.println("Kill file detected.  Shutting down...");
                        if ( connection != null )
                            {
                                try
                                    {
                                        connection.close();
                                    }
                                catch (IOException e)
                                    {
                                    }
                            }
                        System.exit(1);
                    }

                // only do stuff if the connection is alive
                if ( connection == null )
                    return;

                // number of jobs run in the last monitorInterval; simultaneously resets counter
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

                // keep track of how many jobs have been finished
                throughputs.add(throughput);

                // calculate moving average
                double average = 0.0;
                for (Double d : throughputs)
                    average += d;
                average = average / throughputs.size();

                // print statistics
                lastUpdateTime = currentTime;
                ThreadService.INSTANCE.getExecutor().printQueues(throughput, average, elapsedTime);
            }
        }
    }

}
