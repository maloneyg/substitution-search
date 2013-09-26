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
    public static void main(String[] args)
    {
        // this is the thread executor service (a singleton)
        ThreadService executorService = ThreadService.INSTANCE;
        System.out.println(executorService);
        Logger log = executorService.getLogger();

        List<WorkUnit> initialWorkUnits = new ArrayList<WorkUnit>();
        System.out.print("Generating initial work units...");
        createWorkUnits(initialWorkUnits);
        System.out.println(initialWorkUnits.size() + " units have been generated.");

        Scanner kbd = new Scanner(System.in);

        // start monitoring thread
        double monitorInterval = 1.0; //seconds
        ThreadMonitor threadMonitor = new ThreadMonitor(monitorInterval);

        // submit all jobs
        nextUnit:
        for (WorkUnit thisUnit : initialWorkUnits)
            {
                // submit the next work unit
                Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);
                System.out.println("Job " + thisUnit.hashCode() + " submitted.");
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

                // job is complete
                String reportString = String.format("Job %010d complete ( %15s ).  %5d patches have been completed.", thisUnit.hashCode(), thisResult.toString(), BasicWorkUnit.output().size());
                System.out.println(reportString);

                // for monitoring purposes:
                System.out.println("Press ENTER");
                kbd.nextLine();
                System.out.print("Garbage collection initiated...");
                System.gc();
                System.out.println("complete.");
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
                lastUpdateTime = currentTime;
                ThreadService.INSTANCE.getExecutor().printQueues(throughput, elapsedTime);
            }
        }
    }

    private static void createWorkUnits(List<WorkUnit> list)
    {
        //int myTile = 0; // uncomment this line for a small search
        int myTile = 4; // uncomment this line for a big search

        BasicPrototile P0 = BasicPrototile.createBasicPrototile(Preinitializer.PROTOTILES.get(myTile));

        ImmutableList<Integer> BD0 = P0.getLengths().get(0).getBreakdown();
        ImmutableList<Integer> BD1 = P0.getLengths().get(1).getBreakdown();
        ImmutableList<Integer> BD2 = P0.getLengths().get(2).getBreakdown();
        MultiSetLinkedList edge0 = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList(BD0));
        MultiSetLinkedList edge1 = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList(BD1));
        MultiSetLinkedList edge2 = MultiSetLinkedList.createMultiSetLinkedList(new ArrayList(BD2));
        ImmutableList<Integer> start0 = edge0.getImmutableList();
        ImmutableList<Integer> start1 = edge1.getImmutableList();
        ImmutableList<Integer> start2 = edge2.getImmutableList();
        BD0 = start0;
        BD1 = start1;
        BD2 = start2;
        PrototileList tiles = PrototileList.createPrototileList(BasicPrototile.getPrototileList(Initializer.SUBSTITUTION_MATRIX.getColumn(myTile)));
        ImmutableList<BasicPoint> vertices = P0.place(BasicPoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false).getVertices();
        ImmutableList<BasicPoint> bigVertices = ImmutableList.of(vertices.get(0).inflate(),vertices.get(1).inflate(),vertices.get(2).inflate());

//        ImmutableList<Integer> inflList = Preinitializer.INFL;
//        IntPolynomial infl = IntPolynomial.createIntPolynomial(inflList);
//        Matrix otherInfl = infl.evaluate(LengthAndAreaCalculator.AMAT);
//        System.out.println(Initializer.SUBSTITUTION_MATRIX.getColumn(myTile));
//        System.out.println(Initializer.SUBSTITUTION_MATRIX);
//        System.out.println(LengthAndAreaCalculator.arrayString((LengthAndAreaCalculator.AREA_MATRIX.inverse()).times(otherInfl).times(otherInfl).times(LengthAndAreaCalculator.AREA_MATRIX).getArray()));

        // submit 30 jobs to the executor service
        // note: if this exceeds JOB_CAPACITY, then this thread will actually do the work, based on the rejected execution handler
        //ArrayList<Future> futures = new ArrayList<Future>();    // keep a list of all the futures
        //LinkedHashMap<WorkUnit,Future<Result>> submittedJobs = new LinkedHashMap<WorkUnit,Future<Result>>();

        if (P0.isosceles()) {
        // how we submit BasicWorkUnits
        // depends on whether P0 is isosceles.
            ImmutableList<BasicAngle> a = P0.getAngles();
            ImmutableList<Orientation> o = P0.getOrientations();
            // we want to identify the Orientations on the two equal edges.
            // to do that we need to know which Orientations those are.
            Orientation o1 = o.get(1);
            Orientation o2;
            if (a.get(1).equals(a.get(0))) {
                o2 = o.get(0);
            } else {
                o2 = o.get(2);
            }
            do {
                do {

                    ImmutableList<BasicEdge> edgeList = P0.createSkeleton(BD0, BD2, false);
                    ImmutableList<ImmutableList<Integer>> testBD = ImmutableList.of(BD0, BD2);

                    BasicPatch patch = BasicPatch.createBasicPatch(edgeList,bigVertices);
                    // identify the Orientations on the two equal edges
                    patch = patch.identify(o1,o2);

                    // create a new unit of work
                    WorkUnit thisUnit = BasicWorkUnit.createBasicWorkUnit(patch,testBD,tiles);

                    // submit this unit of work to the executor service
                    //checkIfBusy();
                    //Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);
                    list.add(thisUnit);

                    // make a map between pieces of work and their futures
                    //submittedJobs.put(thisUnit,thisFuture);
                    //System.out.println("Job " + thisUnit.hashCode() + " submitted.");
                    //log.log(Level.INFO,"Job " + thisUnit.hashCode() + " submitted.");

                    edgeList = P0.createSkeleton(BD0, BD2, true);
                    testBD = ImmutableList.of(BD0, BD2);

                    patch = BasicPatch.createBasicPatch(edgeList,bigVertices);
                    // identify the Orientations on the two equal edges
                    patch = patch.identify(o1.getOpposite(),o2);

                    // create a new unit of work
                    thisUnit = BasicWorkUnit.createBasicWorkUnit(patch,testBD,tiles);

                    // submit this unit of work to the executor service
                    //checkIfBusy();
                    //thisFuture = executorService.getExecutor().submit(thisUnit);
                    list.add(thisUnit);

                    // make a map between pieces of work and their futures
                    //submittedJobs.put(thisUnit,thisFuture);
                    //System.out.println("Job " + thisUnit.hashCode() + " submitted.");
                    //log.log(Level.INFO,"Job " + thisUnit.hashCode() + " submitted.");

                    edge2.iterate();
                    BD2 = edge2.getImmutableList();
                } while (!BD2.equals(start2));
                edge0.iterate();
                BD0 = edge0.getImmutableList();
            } while (!BD0.equals(start0));
        } else {
            do {
                do {
                    do {

                        ImmutableList<BasicEdge> edgeList = P0.createSkeleton(BD0, BD1, BD2);
                        ImmutableList<ImmutableList<Integer>> testBD = ImmutableList.of(BD0, BD1, BD2);

                        BasicPatch patch = BasicPatch.createBasicPatch(edgeList,bigVertices);

                        // create a new unit of work
                        WorkUnit thisUnit = BasicWorkUnit.createBasicWorkUnit(patch,testBD,tiles);

                        // submit this unit of work to the executor service
                        // the service returns a Future object
                        // when polled, the Future object will either block if the job isn't done yet
                        // or return the result if it is
                        // (exceptions can be thrown if the job was interrupted/cancelled, failed, etc.)
                        //checkIfBusy();
                        //Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);
                        list.add(thisUnit);

                        // make a map between pieces of work and their futures
                        //submittedJobs.put(thisUnit,thisFuture);
                        //System.out.println("Job " + thisUnit.hashCode() + " submitted.");
                        //log.log(Level.INFO,"Job " + thisUnit.hashCode() + " submitted.");

                        edge2.iterate();
                        BD2 = edge2.getImmutableList();
                    } while (!BD2.equals(start2));
                    edge1.iterate();
                    BD1 = edge1.getImmutableList();
                } while (!BD1.equals(start1));
                edge0.iterate();
                BD0 = edge0.getImmutableList();
            } while (!BD0.equals(start0));
        } // end of BasicWorkUnit submissions
    }

} // end of SimpleTest
