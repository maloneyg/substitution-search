/*************************************************************************
 *
 *  A class for testing ThreadService, BasicWorkUnit, and all their components
 *
 *************************************************************************/

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import com.google.common.collect.*;
import Jama.Matrix;
import java.util.Scanner;

public class FactoryTest
{
    // how many jobs to make per set of instructions
    public static final int BATCH_SIZE = Preinitializer.BATCH_SIZE;

    // the guy who pauses the program for us
    private static Scanner kbd = new Scanner(System.in);

    // the number of the triangle we're searching
    private static final int myTile = Preinitializer.MY_TILE;
    
    // the triangle we're searching
    private static final BasicPrototile P0 = BasicPrototile.createBasicPrototile(Preinitializer.PROTOTILES.get(myTile));
    
    // the numbers of the different prototiles that fit in INFL.P0
    private static final PrototileList tiles = PrototileList.createPrototileList(BasicPrototile.getPrototileList(Initializer.SUBSTITUTION_MATRIX.getColumn(myTile)));
    
    // vertices of INFL.P0
    private static final BytePoint[] vertices = P0.place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false).getVertices();
    private static final BytePoint[] bigVertices = new BytePoint[] {vertices[0].inflate(),vertices[1].inflate(),vertices[2].inflate()};
    
    // iterators for producing new edge breakdowns
    private static MultiSetLinkedList edge0;
    private static MultiSetLinkedList edge1;
    private static MultiSetLinkedList edge2;
    
    // the starting edge breakdowns
    private static final ImmutableList<Integer> start0;
    private static final ImmutableList<Integer> start1;
    private static final ImmutableList<Integer> start2;
    
    // the starting edge breakdowns of P0
    private static ImmutableList<Integer> BD0;
    private static ImmutableList<Integer> BD1;
    private static ImmutableList<Integer> BD2;
    
    // two Orientations to be identified.
    // only relevant if P0 is isosceles.
    private static final Orientation o1;
    private static final Orientation o2;
    
    // a boolean that tells us if o1 = o2 or o1 = -o2.
    // only relevant if P0 is isosceles.
    private static boolean flip = false;

    // true if we haven't created all edge breakdowns yet
    private static boolean notDoneYet = true;

    static { // initialize the edge breakdown iterators
        ImmutableList<Integer> first0 = P0.getLengths()[0].getBreakdown();
        ImmutableList<Integer> first1 = P0.getLengths()[1].getBreakdown();
        ImmutableList<Integer> first2 = P0.getLengths()[2].getBreakdown();
        edge0 = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList<Integer>(first0));
        edge1 = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList<Integer>(first1));
        edge2 = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList<Integer>(first2));
        start0 = edge0.getImmutableList();
        start1 = edge1.getImmutableList();
        start2 = edge2.getImmutableList();
        BD0 = start0;
        BD1 = start1;
        BD2 = start2;
    } // initialization of edge breakdown iterators ends here

    static { // initialize o1 and o2
        BasicAngle[] a = P0.getAngles();
        Orientation[] o = P0.getOrientations();
        // we want to identify the Orientations on the two equal edges.
        // to do that we need to know which Orientations those are.
        o1 = o[1];
        if (a[1].equals(a[0])) {
            o2 = o[0];
        } else {
            o2 = o[2];
        }
    } // initialization of o1 and o2 ends here

    private static void iterateEdgeBreakdown() {
        edge0.iterate();
        BD0 = edge0.getImmutableList();
        if (BD0.equals(start0)) {
            if (P0.isosceles()) { // then we only need two breakdowns
                edge2.iterate();
                BD2 = edge2.getImmutableList();
                if (BD2.equals(start2)) notDoneYet = false;
            } else { // if it's not isosceles, use all three breakdowns
                edge1.iterate();
                BD1 = edge1.getImmutableList();
                if (BD1.equals(start1)) {
                    edge2.iterate();
                    BD2 = edge2.getImmutableList();
                    if (BD2.equals(start2)) notDoneYet = false;
                }
            }
        }
    }

    private static MutableWorkUnit nextWorkUnit() {

        if (P0.isosceles()) {
        // how we submit BasicWorkUnits
        // depends on whether P0 is isosceles.

            BasicEdge[] edgeList = P0.createSkeleton(BD0, BD2, flip);
            MutablePatch patch = MutablePatch.createMutablePatch(edgeList,bigVertices,tiles.dumpMutablePrototileList());
            // identify the Orientations on the two equal edges
            if (flip) {
                patch.addInstructions(o1,o2.getOpposite());
            } else {
                patch.addInstructions(o1,o2);
            }

            if (flip) iterateEdgeBreakdown();
            flip = !flip;

            // create a new unit of work
            return MutableWorkUnit.createMutableWorkUnit(patch);

        } else {

            BasicEdge[] edgeList = P0.createSkeleton(BD0, BD1, BD2);

            MutablePatch patch = MutablePatch.createMutablePatch(edgeList,bigVertices,tiles.dumpMutablePrototileList());

            iterateEdgeBreakdown();

            return MutableWorkUnit.createMutableWorkUnit(patch);

        } // end of BasicWorkUnit submissions
    }


    // pause the action
    private static void pause() {
        System.out.println("Press ENTER");
        kbd.nextLine();
    }

    public static void main(String[] args)
    {
        // this is the thread executor service (a singleton)
        ThreadService executorService = ThreadService.INSTANCE;
        System.out.println(executorService);
        Logger log = executorService.getLogger();


        // start monitoring thread
        double monitorInterval = 1.0; //seconds
        ThreadMonitor threadMonitor = new ThreadMonitor(monitorInterval);

        WorkUnitFactory workUnitFactory = WorkUnitFactory.createWorkUnitFactory();

        // submit all jobs
        while (workUnitFactory.notDone())
            {
                WorkUnitInstructions instructions = workUnitFactory.getInstructions(BATCH_SIZE, 1);

                // serialization test: serialize instructions to disk
                String filename = "instructions.tmp";
                try
                    {
                        FileOutputStream fileOut = new FileOutputStream(filename);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(instructions);
                        out.close();
                        fileOut.close();
                    }
                catch (IOException e)
                    {
                        e.printStackTrace();
                        System.exit(1);
                    }

                // serialization test: deserialize instructions from disk
                WorkUnitInstructions reconstitutedInstructions = null;
                try
                    {
                        FileInputStream fileIn = new FileInputStream(filename);
                        ObjectInputStream in = new ObjectInputStream(fileIn);
                        reconstitutedInstructions = (WorkUnitInstructions)in.readObject();
                        in.close();
                        fileIn.close();
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                        System.exit(1);
                    }

                // submit the next work unit
                List<WorkUnit> theseUnits = workUnitFactory.followInstructions(reconstitutedInstructions);
                
                for (WorkUnit thisUnit : theseUnits)
                    {
                        Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);
                        System.out.println("Job " + thisUnit.hashCode() + " submitted.\n");
                        log.log(Level.INFO,"Job " + thisUnit.hashCode() + " submitted.");
                    
                        // if the queue is full, wait
                        while (true)
                            {
                                int queueSize = executorService.getExecutor().getQueue().size();
                                if ( queueSize > 0 )
                                    {
                                        try
                                            {
                                                Thread.sleep(1000);
                                            }
                                        catch ( InterruptedException e )
                                            {
                                                continue;
                                            }
                                    }
                                else
                                    break;
                            }
                    }
            }

        // wait for all jobs to complete
        while (true)
            {
                int queueSize = executorService.getExecutor().getQueue().size();
                int runningJobs = executorService.getExecutor().getNumberOfRunningJobs();
                if ( queueSize > 0 || runningJobs > 0 )
                    {
                        try
                            {
                                Thread.sleep(1000);
                            }
                        catch ( InterruptedException e )
                            {
                                continue;
                            }
                    }
                else
                    break;
            }
        System.out.println("All jobs have finished.");

        // stop monitoring thread
        threadMonitor.stop();

        // display results
        int numberOfResults = MutablePatch.getCompletedPatches().size();
        System.out.println(numberOfResults + " completed patches have been found.");
        if ( numberOfResults > 0 )
            {
                List<BasicPatch> output = MutablePatch.getCompletedPatches();

                // serialization test: serialize and deserialize each basicpatch

                try
                    {
                        PointsDisplay display = new PointsDisplay(output,"mutable test");
                    }
                catch ( java.awt.HeadlessException e )
                    {
                        System.out.println("X11 display not supported on this terminal.");
                    }
            }
        System.exit(0);
    }

    private static class ThreadMonitor
    {
        private Timer timer;
        private double updateInterval;
        private static ThreadService executorService = ThreadService.INSTANCE;
        private Date lastUpdateTime = null;
        private LinkedList<Double> throughputs = new LinkedList<Double>();

        public ThreadMonitor(double updateInterval) // seconds
        {
            this.updateInterval = updateInterval;
            timer = new Timer();
            timer.schedule(new CustomTimerTask(), (int)updateInterval*1000, (int)updateInterval*1000);
            System.out.println("Thread monitor started.");
        }

        public void stop()
        {
            timer.cancel();
            System.out.println("Thread monitor stopped.");
        }

        private class CustomTimerTask extends TimerTask
        {
            public void run()
            {
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


} // end of MutableSerializationTest
