import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

// This class will serve up work units to remote clients. 

public final class MutableParadigmClient
{
    public static final int LISTENING_PORT = 32007;
    public static String HOST_NAME = Preinitializer.HOST_NAME;

    public static final double MONITOR_INTERVAL = 0.5; // seconds
    public static final int TIMEOUT = 1; // how many seconds to wait before declaring a node unreachable
    public static final int MAX_ATTEMPTS = 5; // how many time to try connecting before giving up
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
                        System.exit(1);
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

    private static void connect() throws IOException, InterruptedException, ConnectException, ClassNotFoundException, SocketException, UnknownHostException, EOFException
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
        //Integer numberOfNewJobsNeeded = executorService.NUMBER_OF_THREADS - executorService.getExecutor().getNumberOfRunningJobs() + 1;
        //outgoingObjectStream.writeObject(numberOfNewJobsNeeded);
        outgoingObjectStream.writeObject(Integer.valueOf(1));
        outgoingObjectStream.flush();
        outgoingObjectStream.reset();
        System.out.println("Initial job request made.");

        while (true)
            {
                try
                    {
                        incomingObject = incomingObjectStream.readObject();
                        if ( incomingObject instanceof WorkUnitInstructions )
                            {
                                WorkUnitInstructions instructions = (WorkUnitInstructions)incomingObject;
                                System.out.println("received instruction ID = " + instructions.getID());
                                List<MutableWorkUnit> theseUnits = workUnitFactory.followInstructions(instructions);

                                AtomicInteger counter = new AtomicInteger(0);
                                LinkedList<BasicPatch> completedPuzzles = new LinkedList<BasicPatch>();
                                
                                for (MutableWorkUnit thisUnit : theseUnits)
                                    {
                                        thisUnit.setCounter(counter);
                                        thisUnit.setResultTarget(completedPuzzles);
                                        Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);
                                        //System.out.println("submitted unit " + thisUnit.hashCode());
                                    }
                                
                                // wait for current batch to complete
                                while ( counter.get() < theseUnits.size() )
                                    {
                                        try
                                            {
                                                Thread.sleep(50);
                                            }
                                        catch (InterruptedException e)
                                            {
                                            }
                                    }
                                
                                sendResult(instructions.getID(), completedPuzzles, theseUnits.size());
                                
                                // ask for another piece of work
                                outgoingObjectStream.writeObject(Integer.valueOf(1));
                                outgoingObjectStream.flush();
                                outgoingObjectStream.reset();
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
                catch (EOFException e)
                    {
                        throw new EOFException(e.getMessage());
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

    public static void sendResult(int ID, List<BasicPatch> completedPatches, int numberOfUnits )
    {
        // create PatchResult to send
        PatchResult result = new PatchResult(ID, completedPatches, numberOfUnits);

        // send result
        try
            {
                outgoingObjectStream.writeObject(result);
                outgoingObjectStream.flush();
                outgoingObjectStream.reset();
                System.out.println("\nsent " + result.toString());
            }
        catch (SocketException e)
            {
                System.out.println("Broken pipe!  Unable to send result!");
            }
        catch (Exception e)
            {
                e.printStackTrace();
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
