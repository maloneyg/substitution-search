import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

// This class will serve up work units to remote clients. 

public final class Client
{
    public static final int LISTENING_PORT = Preinitializer.LISTENING_PORT;
    public static String HOST_NAME = Preinitializer.HOST_NAME;

    public static final double MONITOR_INTERVAL = 1.0; // seconds
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

    private static final List<Future<Result>> allFutures = new LinkedList<Future<Result>>();

    private static Object sendLock = new Object();

    // prevent instantiation
    private Client()
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
                        if ( e.getMessage().toLowerCase().indexOf("connection refused") > -1 )
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
        TaskMonitor taskMonitor = new TaskMonitor();
        System.out.println("Connected and ready to run jobs.");

        while (true)
            {
                try
                    {
                        incomingObject = incomingObjectStream.readObject();
                        System.out.println("object received!");
                        if ( incomingObject instanceof EmptyBoundaryWorkUnit )
                            {
                                //System.out.println("job received!");
                                EmptyBoundaryWorkUnit unit = (EmptyBoundaryWorkUnit)incomingObject;
                                Future<Result> thisFuture = executorService.getExecutor().submit(unit);
                                System.out.println("submitted ID " + unit.uniqueID());
                                synchronized(allFutures)
                                    {
                                        allFutures.add(thisFuture);
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
                        else if ( incomingObject == null )
                            System.out.println("received a null!");
                        else
                            System.out.println("Unknown object type received: " + incomingObject.getClass());
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

    public static void requestJob(int i)
    {
        System.out.println("requesting " + i + " jobs");
        try
            {
                synchronized (sendLock)
                {
                    outgoingObjectStream.writeObject(Integer.valueOf(i));
                    outgoingObjectStream.flush();
                    outgoingObjectStream.reset();
                    //System.out.println("Requested new instructions.\n");
                }
            }
        catch (IOException e)
            {
                if ( e.getMessage().equals("Broken pipe") )
                    System.out.println("Broken pipe while requesting job!");
                else if ( e.getMessage().contains("reset by peer") )
                    System.out.println("Connection reset by peer while requesting job!");
                else
                    {
                        System.out.println("Error while requesting job!");
                        e.printStackTrace();
                    }
            }
        catch (Exception e)
            {
                System.out.println("Error while requesting job!");
                e.printStackTrace();
            }
    }

    public static void sendResult(EmptyWorkUnitResult result)
    {
        // send result
        try
            {
                synchronized (sendLock)
                    {
                        outgoingObjectStream.writeObject(result);
                        outgoingObjectStream.flush();
                        outgoingObjectStream.reset();
                    }
                System.out.println("sent result ID " + result.uniqueID());
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

    private static class TaskMonitor
    {
        private Timer timer;
        private static final int UPDATE_INTERVAL = 500; // ms
        private static ThreadService executorService = ThreadService.INSTANCE;
        private static Date lastUpdate = null;

        public TaskMonitor()
        {
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), UPDATE_INTERVAL, UPDATE_INTERVAL);
        }

        private class CustomTimerTask extends TimerTask
        {
            public void run()
            {
                // if any of the WorkUnitInstructions are complete, send the result
                List<Future<Result>> completedJobs = new LinkedList<Future<Result>>();
                synchronized( allFutures )
                    {
                        // find out which jobs are finished
                        for (Future<Result> f : allFutures)
                            if (f.isDone())
                                completedJobs.add(f);
                    }

                for (Future<Result> f : completedJobs)
                    {
                        // send each result
                        Result thisResult = null;
                        try
                            {
                                thisResult = f.get();
                            }
                        catch (Exception e)
                            {
                                System.out.println("error in client while trying to send back result!");
                                continue;
                            }
                        if ( thisResult == null )
                            {
                                System.out.println("null error in client while trying to send back result!");
                                continue;
                            }
                        EmptyWorkUnitResult thisEmptyResult = (EmptyWorkUnitResult)thisResult;
                        Client.sendResult(thisEmptyResult);
                    }
                synchronized( allFutures )
                    {
                        // remove the results that have been sent so they won't be sent again 
                        allFutures.removeAll(completedJobs);
                    }

                // if the number of jobs in the queue is below the threshold, request more work
                int currentSize = executorService.getExecutor().getQueue().size();
                if ( currentSize == 0 )
                    {
                        if ( TaskMonitor.lastUpdate == null )
                            {
                                TaskMonitor.lastUpdate = new Date();
                                return;
                            }
                        if ( new Date().getTime() - TaskMonitor.lastUpdate.getTime() < 1.5*Preinitializer.SPAWN_MIN_TIME )
                            return;
                        TaskMonitor.lastUpdate = new Date();
                        int numberOfJobsNeeded = Preinitializer.NUMBER_OF_THREADS + 1 - currentSize - 
                                                 executorService.getExecutor().getNumberOfRunningJobs();
                        Client.requestJob(numberOfJobsNeeded);
                    }
            }
        }
    }

    private static class ThreadMonitor
    {
        private Timer timer;
        private static ThreadService executorService = ThreadService.INSTANCE;
        private Date lastUpdateTime = null;
        private LinkedList<Double> throughputs = new LinkedList<Double>();

        public ThreadMonitor()
        {
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), (int)(Client.MONITOR_INTERVAL*1000), (int)(Client.MONITOR_INTERVAL*1000));
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
                ThreadService.INSTANCE.getExecutor().printQueues(throughput, average, elapsedTime, -1);
            }
        }
    }

}
