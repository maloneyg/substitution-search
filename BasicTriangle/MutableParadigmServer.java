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

    private static AtomicInteger LIVE_CONNECTIONS = new AtomicInteger(0);
    private static List<ConnectionThread> ALL_CONNECTIONS = Collections.synchronizedList(new ArrayList<ConnectionThread>()); 

    public static void main(String[] args)
    {
        // launch the thread that will give periodic reports
        ThreadMonitor threadMonitor = new ThreadMonitor();

        // listen for connections
        ServerSocket listener = null;
        Socket connection = null;
        ConnectionThread connectionThread = null;

        System.out.println("Listening on port " + LISTENING_PORT + "...");
        
        while (true)
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
                        connectionThread.decrement();
                    }
                catch (Exception e)
                    {
                        System.out.println("Server shutdown:");
                        e.printStackTrace();
                    }
            }
    }

    private static class ConnectionThread extends Thread
    {
        private Socket connection = null;
        private InputStream incomingStream;
        private ObjectInputStream incomingObjectStream;
        private OutputStream outgoingStream;
        private ObjectOutputStream outgoingObjectStream;
    
        public static final String HANDSHAKE = "TriangleHandshake";

        public ConnectionThread(Socket connection)
        {
            this.connection = connection;
        }

        public void decrement()
        {
            boolean contained = MutableParadigmServer.ALL_CONNECTIONS.remove(this);
            if ( contained )
                MutableParadigmServer.LIVE_CONNECTIONS.getAndDecrement();
        }

        // set streams and handshake
        public void checkConnection() throws SocketException, ConnectException, SocketTimeoutException, IOException, ClassNotFoundException
        {
            MutableParadigmServer.LIVE_CONNECTIONS.getAndIncrement();
            MutableParadigmServer.ALL_CONNECTIONS.add(this);
            
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
        }

        public void run()
        {
            while (true)
                {
                    try
                        {
                            Object incomingObject = incomingObjectStream.readObject();
                            if ( incomingObject instanceof PatchResult )
                                {
                                    // this is an incoming result
                                    PatchResult result = (PatchResult)incomingObject;
                                    List<BasicPatch> localCompletedPatches = result.getCompletedUnit().getPatch().getLocalCompletedPatches();
                                    if ( localCompletedPatches.size() > 0 )
                                        {
                                            allCompletedPatches.addAll( localCompletedPatches );
                                            System.out.println( localCompletedPatches.size() + " completed patches received from " + connection.getInetAddress() + " (" + allCompletedPatches.size() + " total patches).");
                                        }
                                    else
                                        System.out.println("Null result received from " + connection.getInetAddress() + ".");

                                }
                            else if ( incomingObject instanceof Integer )
                                {
                                    // this is a request for new jobs
                                    int numberOfNewJobs = (Integer)incomingObject;
                                    System.out.println("received request for " + numberOfNewJobs + " new jobs from " + connection.getInetAddress());
                                    for (int i=0; i < numberOfNewJobs; i++)
                                        {
                                            // get next work unit
                                            WorkUnit thisUnit = MutableWorkUnit.nextWorkUnit();

                                            // send work unit
                                            outgoingObjectStream.writeObject(thisUnit);
                                            outgoingObjectStream.flush();
                                            outgoingObjectStream.reset();
                                        }
                                    System.out.println(numberOfNewJobs + " have been sent to " + connection.getInetAddress());
                                }
                            else
                                break;
                        }
                    catch (EOFException e)
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
            try
                {
                    if ( connection != null )
                        connection.close();
                }
            catch (IOException e)
                {
                    e.printStackTrace();
                }
        
            MutableParadigmServer.LIVE_CONNECTIONS.getAndDecrement();
            MutableParadigmServer.ALL_CONNECTIONS.remove(this);
        }
    }

    private static class ThreadMonitor
    {
        private Timer timer;

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
            }
        }
    }
}
