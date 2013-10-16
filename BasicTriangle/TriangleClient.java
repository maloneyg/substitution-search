import java.util.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

// This class will serve up work units to remote clients. 

public final class TriangleClient
{
    public static final int LISTENING_PORT = 32007;
    public static String HOST_NAME = "enj02.rc.fas.harvard.edu";  // name of the server

    public static final double MONITOR_INTERVAL = 0.5; // seconds
    public static final int TIMEOUT = 1; // how many seconds to wait before declaring a node unreachable
    public static final int MAX_ATTEMPTS = 300; // how many time to try connecting before giving up
    public static final String HANDSHAKE = "TriangleHandshake";

    private static Socket connection;
    private static InputStream inputStream;
    private static ObjectInputStream incomingObjectStream;
    private static OutputStream outputStream;
    private static ObjectOutputStream outgoingObjectStream;

    // prevent instantiation
    private TriangleClient()
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

    private static void connect() throws IOException, InterruptedException, ConnectException, ClassNotFoundException
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

        // run jobs
        ThreadService executorService = ThreadService.INSTANCE;
        System.out.println("Connected and ready to run jobs.");
        while (true)
            {
                try
                    {
                        // ask for new jobs
                        if ( executorService.getExecutor().getQueue().size() == 0 )
                            {
                                Integer numberOfNewJobsNeeded = executorService.NUMBER_OF_THREADS - executorService.getExecutor().getNumberOfRunningJobs() + 1;
                                outgoingObjectStream.writeObject(numberOfNewJobsNeeded);
                                outgoingObjectStream.flush();
                                outgoingObjectStream.reset();
                            }
                        incomingObject = incomingObjectStream.readObject();
                        if ( incomingObject instanceof TestWorkUnit )
                            {
                                TestWorkUnit thisUnit = (TestWorkUnit)incomingObject;
                                Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);
                            }
                        else
                            break;
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                        break;
                    }
            }
    }

    public static void sendResult(TestResult result)
    {
        System.out.println("sending: " + result.toString());
        try
            {
                outgoingObjectStream.writeObject(result);
                outgoingObjectStream.flush();
            }
        catch (Exception e)
            {
                e.printStackTrace();
            }
    }

    private static class ThreadMonitor
    {
        private Timer timer;

        public ThreadMonitor()
        {
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), (int)(TriangleServer.MONITOR_INTERVAL*1000), (int)(TriangleServer.MONITOR_INTERVAL*1000));
        }

        private class CustomTimerTask extends TimerTask
        {
            public void run()
            {
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
            }
        }
    }

}
