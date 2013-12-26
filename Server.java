import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import com.google.common.collect.*;

public class Server
{
    public static final ThreadService executorService = ThreadService.INSTANCE;
    private static List<ConnectionThread> LIVE_CONNECTIONS = new ArrayList<ConnectionThread>();
    private static List<ConnectionThread> ALL_CONNECTIONS = new ArrayList<ConnectionThread>();

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
                System.out.print(thisUnit.hashCode() + " ");
            }
        System.out.println();

        // start monitoring thread
        ThreadMonitor threadMonitor = new ThreadMonitor(1.0); // monitoring interval in seconds

        // create workload balancer

        // start accepting connections

        // wait for all jobs to complete
        pause(1000);
        while ( true )
            {
                if ( executorService.getExecutor().getNumberOfRunningJobs() == 0 &&
                     executorService.getExecutor().getQueue().size() == 0 )
                    break;
                pause(500);
            }

        // write out all results
        threadMonitor.stop();
        pause(500);
        System.out.println("All jobs complete!  Have a nice day!");
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
        }

        public void run()
        {
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

                // right now we're not keeping track of the number of completed patches
                int numberOfCompletedPatches = -1;

                // print statistics
                lastUpdateTime = currentTime;
                ThreadService.INSTANCE.getExecutor().printQueues(throughput, average, totalTime, numberOfCompletedPatches);
            }
        }
    }
}
