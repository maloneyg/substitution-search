import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.*;
import java.nio.file.*;

// This class will serve up work units to remote clients. 

public class EmptyBoundaryServer
{
    public static final int LISTENING_PORT = 32007;
    public static final double MONITOR_INTERVAL = 1.0; // seconds
    public static final double CHECKPOINT_INTERVAL = 10.0; // seconds, how often to checkpoint progress to disk
    public static final int TIMEOUT = 1; // how many seconds to wait before declaring a node unreachable

    public static List<ImmutablePatch> allCompletedPatches = new LinkedList<ImmutablePatch>();
    private static final BasicEdgeLength[] lengths = BasicPrototile.ALL_PROTOTILES.get(Preinitializer.MY_TILE).getLengths();

    public static final String RESULT_FILENAME = "results.chk";

    public static AtomicLong numberOfResultsReceived = new AtomicLong(0L);
    public static volatile boolean finished = false;

    private static List<ConnectionThread> LIVE_CONNECTIONS = new ArrayList<ConnectionThread>(); 
    private static List<ConnectionThread> ALL_CONNECTIONS = new ArrayList<ConnectionThread>(); 

    private static EmptyBoundaryWorkUnitFactory workUnitFactory = EmptyBoundaryWorkUnitFactory.createEmptyBoundaryWorkUnitFactory();

    public static final String PRIMARY_CHECKPOINT_FILENAME = "primary_server_checkpoint.chk";
    public static final String SECONDARY_CHECKPOINT_FILENAME = "secondary_server_checkpoint.chk";

    public static final String PRIMARY_RESULTS_FILENAME = "primary_results.chk";
    public static final String SECONDARY_RESULTS_FILENAME = "secondary_results.chk";

    public static String timeString = "ETA calculating...";

    private EmptyBoundaryServer()
    {
        throw new RuntimeException("this should not be instantiated!");
    }

    public static void main(String[] args)
    {
        // check to see if we should resume from a checkpoint
        if (args.length > 0)
            {
                if ( args[0].toLowerCase().equals("-resume") )
                    {
                        System.out.print("Attempting to read checkpoint...");

                        // try to read primary checkpoint from disk
                        EmptyBoundaryServerCheckpoint checkpoint = readCheckpoint(PRIMARY_CHECKPOINT_FILENAME);
                        TriangleResults resultsCheckpoint = readResults(PRIMARY_RESULTS_FILENAME);
                        if (    checkpoint != null && resultsCheckpoint != null 
                             && checkpoint.getNumberOfCompletedPuzzles() == resultsCheckpoint.getPatches().size() )   
                            System.out.println("read from primary file succesfully.");

                        // if the primary checkpoint is a dud, switch to secondary checkpoint
                        if ( checkpoint == null )
                            {
                                System.out.print("primary failed...trying secondary...");
                                checkpoint = null;
                                resultsCheckpoint = null;
                                checkpoint = readCheckpoint(SECONDARY_CHECKPOINT_FILENAME);
                                resultsCheckpoint = readResults(SECONDARY_RESULTS_FILENAME);

                                // check that both the checkpoint and the results file correspond to consistent states
                                // both have to exist and point to the same number of puzzles
                                // this precludes the possibility that the checkpointing process was interrupted at a point
                                // when the checkpoint was written but the results hadn't; this avoids losing any results

                                if (    checkpoint != null && resultsCheckpoint != null
                                     && checkpoint.getNumberOfCompletedPuzzles() == resultsCheckpoint.getPatches().size() )
                                    System.out.println("successful!");
                                else
                                    System.out.println("failed!");
                            }

                        // load the previous state
                        if (    checkpoint != null && resultsCheckpoint != null
                             && checkpoint.getNumberOfCompletedPuzzles() == resultsCheckpoint.getPatches().size() )
                            {
                                Date date = checkpoint.getDate();
                                System.out.print("Parsing data from checkpoint (" + date.toString() + ")...");
                                allCompletedPatches = new LinkedList<ImmutablePatch>(resultsCheckpoint.getPatches());
                                workUnitFactory = checkpoint.getEmptyBoundaryWorkUnitFactory();
                                ConnectionThread.setToCheckpoint(checkpoint);
                                numberOfResultsReceived = checkpoint.getNumberOfResultsReceived();
                                System.out.println("complete.");
                                System.out.println(allCompletedPatches.size() + " previously completed puzzles have been read.");
                                System.out.println(ConnectionThread.toBeResent.size() + " pending jobs have been re-entered into the queue for re-dispatching.");
                                System.out.println("Successfully restored old state (" + numberOfResultsReceived + " work unit results received).\n");
                            }
                        else
                            System.out.println("Error: unable to resume, so starting over.");
                    }
                else
                    {
                        System.out.println("Invalid command-line arguments.  Use -resume to restart calculations.");
                        System.exit(1);
                    }
            }
        
        // launch the thread that will give periodic reports
        ThreadMonitor threadMonitor = new ThreadMonitor();

        // launch the thread that will periodically checkpoint progress to disk
        CheckpointMonitor checkpointMonitor = new CheckpointMonitor();

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

        System.out.println("Program complete.  " + numberOfResultsReceived + " work units were processed, comprising " + allCompletedPatches.size() + " completed puzzles.");
        
        // write results to file 
        if (allCompletedPatches.size() > 0)
            {
                TriangleResults triangleResults = new TriangleResults(allCompletedPatches);
                //for (ImmutablePatch p : allCompletedPatches) G.add(p.getEdge0(),p.getEdge1(),p.getEdge2(),lengths[0],lengths[1],lengths[2]); // write to the edge breakdown graph
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

    private static EmptyBoundaryServerCheckpoint readCheckpoint(String filename)
    {
        // check to see if checkpoint file exists
        File file = new File(filename);
        if ( ! file.isFile() )
            return null;

        // read checkpoint from disk
        EmptyBoundaryServerCheckpoint checkpoint = null;
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try
            {
                fileIn = new FileInputStream(filename);
                in = new ObjectInputStream(fileIn);
                checkpoint = (EmptyBoundaryServerCheckpoint)in.readObject();
            }
        catch (Exception e)
            {
                System.out.println("Error while reading in checkpoint!");
                //e.printStackTrace();
            }
        finally
            {
                try
                    {
                        if ( in != null )
                            in.close();
                        if ( fileIn != null )
                            fileIn.close();
                    }
                catch (IOException e2)
                    {
                        e2.printStackTrace();
                    }
            }
        return checkpoint;
    }

    private static TriangleResults readResults(String filename)
    {
        // check to see if checkpoint file exists
        File file = new File(filename);
        if ( ! file.isFile() )
            return null;

        // read checkpoint from disk
        TriangleResults results = null;
        FileInputStream fileIn = null;
        ObjectInputStream in = null;
        try
            {
                fileIn = new FileInputStream(filename);
                in = new ObjectInputStream(fileIn);
                results = (TriangleResults)in.readObject();
            }
        catch (Exception e)
            {
                System.out.println("Error while reading in results!");
                //e.printStackTrace();
            }
        finally
            {
                try
                    {
                        if ( in != null )
                            in.close();
                        if ( fileIn != null )
                            fileIn.close();
                    }
                catch (IOException e2)
                    {
                        e2.printStackTrace();
                    }
            }
        return results;
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
        public static Object sendLock = new Object();
        public final String address;

        private static int jobCount = 0;
        protected static HashMap<EmptyBoundaryWorkUnitInstructions,ConnectionThread> dispatched = new HashMap<>();
        protected static LinkedList<EmptyBoundaryWorkUnitInstructions> toBeResent = new LinkedList<>();

        public static final int BATCH_SIZE = Preinitializer.BATCH_SIZE;

        public ConnectionThread(Socket connection)
        {
            this.connection = connection;
            address = connection.getInetAddress().getCanonicalHostName();
        }

        public static void setToCheckpoint(EmptyBoundaryServerCheckpoint checkpoint)
        {
            jobCount = checkpoint.getJobCount();
            toBeResent = checkpoint.getResent();
        }

        public static int getJobCount()
        {
            return jobCount;
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
                    EmptyBoundaryServer.LIVE_CONNECTIONS.add(this);
                    EmptyBoundaryServer.ALL_CONNECTIONS.add(this);
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
                    System.out.print(String.format("%d of %d jobs complete (%.2f%%, %s)\r", numberOfResultsReceived.get(),
                                                   Initializer.TOTAL_EDGE_BREAKDOWNS,
                                                   (double)(100.0*numberOfResultsReceived.get()/(double)Initializer.TOTAL_EDGE_BREAKDOWNS),
                                                   EmptyBoundaryServer.timeString));
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
                                    
                                    List<ImmutablePatch> localCompletedPatches = result.getCompletedPatches();
                                    allCompletedPatches.addAll( localCompletedPatches );
                                    Date currentDate = new Date();
                                    String dateString = String.format("%02d:%02d:%02d", currentDate.getHours(), currentDate.getMinutes(), currentDate.getSeconds());
                                    String statusString = String.format("[ %s ] : Received %s ", dateString, result.toBriefString());
                                    if ( localCompletedPatches.size() > 0 )
                                        statusString = statusString + "(" + localCompletedPatches.size() + " new completed puzzles) ";
                                    statusString = statusString + "from " + address;
                                    System.out.println(statusString);

                                    // mark job as finished
                                    EmptyBoundaryWorkUnitInstructions toBeRemoved = null;
                                    synchronized (sendLock)
                                        {
                                            for (EmptyBoundaryWorkUnitInstructions i : dispatched.keySet())
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
                            System.out.println("Connection to " + address + " lost.");
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
                    EmptyBoundaryServer.LIVE_CONNECTIONS.remove(this);
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
                            
                            EmptyBoundaryWorkUnitInstructions theseInstructions = toBeResent.removeFirst();
                            //System.out.println("resend instructions " + theseInstructions.getID() + " : " + theseInstructions.toString());

                            // send instructions
                            outgoingObjectStream.writeObject(theseInstructions);
                            outgoingObjectStream.flush();
                            outgoingObjectStream.reset();

                            // make a note of which instructions have gone out
                            dispatched.put(theseInstructions, this);
                            System.out.println("Re-dispatched job " + theseInstructions.getID() + " to " + address + ".             ");

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
                            EmptyBoundaryWorkUnitInstructions theseInstructions = workUnitFactory.getInstructions(BATCH_SIZE,jobCount);
                            //System.out.println("original instructions " + theseInstructions.getID() + " : " + theseInstructions.toString());
                            
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

    private static class CheckpointMonitor
    {
        private Timer timer;
        private static EmptyBoundaryServerCheckpoint lastCheckpoint = null;
        private static TriangleResults lastResults = null;
        private static Object sendLock = EmptyBoundaryServer.ConnectionThread.sendLock;

        public CheckpointMonitor()
        {
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), (int)(EmptyBoundaryServer.CHECKPOINT_INTERVAL*1000), (int)(EmptyBoundaryServer.CHECKPOINT_INTERVAL*1000));
        }

        private class CustomTimerTask extends TimerTask
        {
            public void run()
            {
                // don't do anything if there aren't any live connections
                if ( EmptyBoundaryServer.LIVE_CONNECTIONS.size() == 0 || EmptyBoundaryServer.finished == true )
                    return;

                // don't do anything if we haven't received any new results since the last checkpoint
                if ( lastCheckpoint != null )
                    {
                        if ( EmptyBoundaryServer.numberOfResultsReceived.get() <= lastCheckpoint.getNumberOfResultsReceived().get() )
                            {
                                //System.out.println( EmptyBoundaryServer.numberOfResultsReceived.get() + " <= " + lastCheckpoint.getNumberOfResultsReceived().get() );
                                //System.out.println("skipped");
                                return;
                            }
                    }

                // back up old checkpoint
                File from = new File(EmptyBoundaryServer.PRIMARY_CHECKPOINT_FILENAME);
                File to   = new File(EmptyBoundaryServer.SECONDARY_CHECKPOINT_FILENAME);
                if ( from.isFile() ) // only copy if there is a source file
                    {
                        try
                            {
                                Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            }
                        catch (IOException e)
                            {
                                System.out.println("Error while backing up checkpoint!  Aborting operation.");
                                e.printStackTrace();
                                return;
                            }
                    }

                // create checkpoint
                synchronized(sendLock)
                    {
                        EmptyBoundaryServerCheckpoint serverCheckpoint = new EmptyBoundaryServerCheckpoint(ConnectionThread.getJobCount(), ConnectionThread.dispatched,
                                                                                 ConnectionThread.toBeResent, ConnectionThread.sendLock,
                                                                                 EmptyBoundaryServer.workUnitFactory,
                                                                                 EmptyBoundaryServer.numberOfResultsReceived,
                                                                                 new Date(), EmptyBoundaryServer.allCompletedPatches.size() );

                        // if more completed puzzles have been found since the last checkpoint, write those out
                        TriangleResults results = null;
                        if ( lastResults == null || lastResults.getPatches().size() < EmptyBoundaryServer.allCompletedPatches.size() )
                            {
                                // create results object
                                results = new TriangleResults(EmptyBoundaryServer.allCompletedPatches);

                                // back up old results
                                File from2 = new File(EmptyBoundaryServer.PRIMARY_RESULTS_FILENAME);
                                File to2   = new File(EmptyBoundaryServer.SECONDARY_RESULTS_FILENAME);
                                if ( from.isFile() ) // only copy if there is a source file
                                    {
                                        try
                                            {
                                                Files.copy(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                            }
                                        catch (IOException e)
                                            {
                                                System.out.println("Error while backing up results!  Aborting operation.");
                                                e.printStackTrace();
                                                return;
                                            }
                                    }
                            }

                        // serialize checkpoint
                        FileOutputStream fileOut = null;
                        ObjectOutputStream out = null;
                        try
                            {
                                fileOut = new FileOutputStream(EmptyBoundaryServer.PRIMARY_CHECKPOINT_FILENAME);
                                out = new ObjectOutputStream(fileOut);
                                out.writeObject(serverCheckpoint);
                                out.flush();
                                lastCheckpoint = serverCheckpoint;
                                File checkFile = new File(EmptyBoundaryServer.PRIMARY_CHECKPOINT_FILENAME);
                                double size = (double)(checkFile.length()/1048576L);
                                if ( size > 0.01 )
                                    System.out.println(String.format("\nWrote checkpoint (%.2f MB, %d results received, %d completed puzzles).                 \n", (double)(checkFile.length()/1048576L), serverCheckpoint.getNumberOfResultsReceived().get(), EmptyBoundaryServer.allCompletedPatches.size()));
                                else
                                    System.out.println(String.format("\nWrote checkpoint (%d bytes, %d results received, %d completed puzzles).                 \n",checkFile.length(), serverCheckpoint.getNumberOfResultsReceived().get(), EmptyBoundaryServer.allCompletedPatches.size()));
                            }
                        catch (IOException e)
                            {
                                System.out.println("Error while writing checkpoint!");
                                e.printStackTrace();
                            }
                        finally
                            {
                                try
                                    {
                                        if ( out != null )
                                            out.close();
                                        if ( fileOut != null )
                                            fileOut.close();
                                    }
                                catch (IOException e2)
                                    {
                                        e2.printStackTrace();
                                    }
                            }

                        // serialize results if necessary
                        if ( results == null ) 
                            return;
                        fileOut = null;
                        out = null;
                        try
                            {
                                fileOut = new FileOutputStream(EmptyBoundaryServer.PRIMARY_RESULTS_FILENAME);
                                out = new ObjectOutputStream(fileOut);
                                out.writeObject(results);
                                out.flush();
                                lastResults = results;
                                File resultsFile = new File(EmptyBoundaryServer.PRIMARY_RESULTS_FILENAME);
                                double size = (double)(resultsFile.length()/1048576L);
                                if ( size > 0.01 )
                                    System.out.println(String.format("Wrote results (%.2f MB, %d completed puzzles).\n", (double)(resultsFile.length()/1048576L),
                                                       results.getPatches().size() ));
                                else
                                    System.out.println("Wrote results (" + resultsFile.length() + " bytes, " +
                                                       results.getPatches().size() + " completed puzzles).\n"          );
                            }
                        catch (IOException e)
                            {
                                System.out.println("Error while writing results!");
                                e.printStackTrace();
                            }
                        finally
                            {
                                try
                                    {
                                        if ( out != null )
                                            out.close();
                                        if ( fileOut != null )
                                            fileOut.close();
                                    }
                                catch (IOException e2)
                                    {
                                        e2.printStackTrace();
                                    }
                            }

                    }
            }
        }
    }

    private static class ThreadMonitor
    {
        private Timer timer;
        private static HashMap<EmptyBoundaryWorkUnitInstructions,ConnectionThread> dispatched = EmptyBoundaryServer.ConnectionThread.dispatched; 
        private static LinkedList<EmptyBoundaryWorkUnitInstructions> toBeResent = EmptyBoundaryServer.ConnectionThread.toBeResent;
        private static Object sendLock = EmptyBoundaryServer.ConnectionThread.sendLock;
        
        // to calculate estimated time to completion
        private Date lastTime = null;
        private long lastNumberOfResultsReceived = 0L;
        public static double SMOOTHING_FACTOR = 0.005; // for exponential moving average
        private double averageSpeed = 0.0;

        public ThreadMonitor()
        {
            timer = new Timer();
            // 30 second delay before this thread will start
            timer.schedule(new CustomTimerTask(), 30*1000, (int)(EmptyBoundaryServer.MONITOR_INTERVAL*1000));
        }

        private class CustomTimerTask extends TimerTask
        {
            public void run()
            {
                //System.out.print("Live connections: " + EmptyBoundaryServer.LIVE_CONNECTIONS.get() + "\r");
                File killFile = new File("kill.txt");
                if ( killFile.isFile() )
                    {
                        System.out.println("\nKill file detected.  Shutting down...");
                        System.exit(1);
                    }

                // re-queue any missed instructions
                synchronized(sendLock)
                    {
                        for (ConnectionThread t : EmptyBoundaryServer.ALL_CONNECTIONS)
                            {
                                if (!t.isConnected())
                                    {
                                        LinkedList<EmptyBoundaryWorkUnitInstructions> resend = new LinkedList<>();
                                        for (EmptyBoundaryWorkUnitInstructions i : dispatched.keySet())
                                            {
                                                ConnectionThread t2 = dispatched.get(i);
                                                if ( t == t2 )
                                                    {
                                                        resend.add(i);
                                                        System.out.println("Marked instruction number " + i.getID() + " for re-dispatch (originally sent to " + t.getHostName() + ")");
                                                    }
                                            }
                                        for (EmptyBoundaryWorkUnitInstructions i : resend)
                                            {
                                                dispatched.remove(i);
                                                toBeResent.add(i);
                                            }
                                    }
                            }
                    }


                // estimate time to completion using an exponential moving average
                if ( lastTime == null )
                    {
                        // start of run, so initialize values
                        // don't do anything if we haven't started yet
                        lastNumberOfResultsReceived = EmptyBoundaryServer.numberOfResultsReceived.get();
                        if ( lastNumberOfResultsReceived < 1000 )
                            return;
                        lastTime = new Date();
                    }
                else
                    {
                        long jobsNow = EmptyBoundaryServer.numberOfResultsReceived.get();
                        long newJobs = jobsNow - lastNumberOfResultsReceived;
                        
                        Date currentTime = new Date();
                        double deltaT = (double)( (currentTime.getTime() - lastTime.getTime()) / 1000L );
                        
                        double lastSpeed = (double)( newJobs/deltaT );
                        
                        if ( averageSpeed < 1 )
                            averageSpeed = lastSpeed;
                        else
                            averageSpeed = SMOOTHING_FACTOR * lastSpeed + (1-SMOOTHING_FACTOR)*averageSpeed;
                        int jobsRemaining = (int)(Initializer.TOTAL_EDGE_BREAKDOWNS - jobsNow);
                        double ETA = jobsRemaining / (averageSpeed * 3600.0);
                        
                        if ( Double.isNaN(ETA) || Double.isInfinite(ETA) )
                            ETA = jobsRemaining / ( lastSpeed * 3600.0 );
                        
                        //System.out.println(jobsNow + " " + jobsRemaining + " " + ETA + " " + averageSpeed + " " + lastSpeed);
                       
                        String timeString = "";
                        if ( ! Double.isNaN(ETA) && ! Double.isInfinite(ETA) )
                            timeString = String.format("ETA %.2f h", ETA);
                        else
                            timeString = "ETA unknown";

                        if ( ! Double.isNaN(averageSpeed) && ! Double.isInfinite(averageSpeed) &&
                             ! Double.isNaN(lastSpeed)    && ! Double.isInfinite(lastSpeed)       )
                            timeString = timeString + String.format(", avg speed %.1f, now %.1f", averageSpeed, lastSpeed);
                        else if ( ! Double.isNaN(averageSpeed) && ! Double.isInfinite(averageSpeed) )
                            timeString = timeString + String.format(", avg speed %.1f", averageSpeed);
                        else if ( ! Double.isNaN(lastSpeed)    && ! Double.isInfinite(lastSpeed)    )
                            timeString = timeString + String.format(", speed now %.1f", lastSpeed);

                        EmptyBoundaryServer.timeString = timeString;
                    }
            }
        }
    }
}