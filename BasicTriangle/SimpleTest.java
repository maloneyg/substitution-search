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

public class SimpleTest
{

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

    private static WorkUnit nextWorkUnit() {

        if (P0.isosceles()) {
        // how we submit BasicWorkUnits
        // depends on whether P0 is isosceles.

            BasicEdge[] edgeList = P0.createSkeleton(BD0, BD2, flip);
            ImmutableList<ImmutableList<Integer>> testBD = ImmutableList.of(BD0, BD2);
            BasicPatch patch = BasicPatch.createBasicPatch(edgeList,bigVertices,BD0,BD1,BD2);
            // identify the Orientations on the two equal edges
            if (flip) {
                patch = patch.identify(o1,o2.getOpposite());
            } else {
                patch = patch.identify(o1,o2);
            }

            if (flip) iterateEdgeBreakdown();
            flip = !flip;

            // create a new unit of work
            return BasicWorkUnit.createBasicWorkUnit(patch,testBD,tiles);

        } else {

            BasicEdge[] edgeList = P0.createSkeleton(BD0, BD1, BD2);
            ImmutableList<ImmutableList<Integer>> testBD = ImmutableList.of(BD0, BD1, BD2);

            BasicPatch patch = BasicPatch.createBasicPatch(edgeList,bigVertices,BD0,BD1,BD2);

            iterateEdgeBreakdown();

            return BasicWorkUnit.createBasicWorkUnit(patch,testBD,tiles);

        } // end of BasicWorkUnit submissions
    }


    public static void main(String[] args)
    {
        // this is the thread executor service (a singleton)
        ThreadService executorService = ThreadService.INSTANCE;
        System.out.println(executorService);
        Logger log = executorService.getLogger();

        ConcurrentLinkedQueue<WorkUnit> initialWorkUnits = new ConcurrentLinkedQueue<WorkUnit>();

        Scanner kbd = new Scanner(System.in);

        // start monitoring thread
        double monitorInterval = 1.0; //seconds
        ThreadMonitor threadMonitor = new ThreadMonitor(monitorInterval);

        // submit all jobs
        nextUnit:
        while (notDoneYet)
            {
                WorkUnit thisUnit = nextWorkUnit();

                // submit the next work unit
                Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);
                System.out.println("Job " + thisUnit.hashCode() + " submitted.\n");
                log.log(Level.INFO,"Job " + thisUnit.hashCode() + " submitted.");

                // wait until the result is available
                Result thisResult = null;
                while (true)
                    {
                        try
                            {
                                thisResult = thisFuture.get();
                                break;
                            }
                        catch (InterruptedException e)
                            {
                                // do not allow interruption
                                continue;
                            }
                        catch (ExecutionException e)
                            {
                                e.printStackTrace();
                                continue nextUnit;
                            }
                        catch (CancellationException e)
                            {
                                System.out.println("Job was cancelled!");
                                continue nextUnit;
                            }
                    }

                // wait for queue to empty
                while (true)
                    {
                        if ( executorService.getExecutor().getQueue().size() > 0 )
                            {
                                try
                                    {
                                        Thread.sleep(100);
                                    }
                                catch (InterruptedException e)
                                    {
                                        continue;
                                    }
                            }
                        else
                            break;
                    }

                // job is complete
                String reportString = String.format("\nJob %010d complete ( %15s ).  %5d patches have been completed.\n", thisUnit.hashCode(), thisResult.toString(), BasicWorkUnit.output().size());
                System.out.println(reportString);

                // for monitoring purposes:
                //System.out.println("Press ENTER");
                //kbd.nextLine();
                System.out.print("Garbage collection initiated...");
                System.gc();
                System.out.println("complete.\n");
                //System.out.println("Press ENTER\n");
                //kbd.nextLine();
            }

        // stop monitoring thread
        threadMonitor.stop();

        // display results
        int numberOfResults = BasicWorkUnit.output().size();
        System.out.println(numberOfResults + " completed patches have been found.");
        if ( numberOfResults > 0 )
            {
                Enumeration<BasicPatch> output = BasicWorkUnit.output().keys();
                ArrayList<BasicPatch> preList = new ArrayList<>();
                while (output.hasMoreElements())
                    preList.add(output.nextElement());
                PointsDisplay display = new PointsDisplay(ImmutableList.copyOf(preList),"BigTest");
            }
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
                int jobsRun = executorService.getExecutor().getNumberOfJobsRun();  // number of jobs run in the last monitorInterval; simultaneously    resets counter
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


} // end of SimpleTest
