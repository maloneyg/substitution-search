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

public class SimpleTest
{
    public static void main(String[] args)
    {

//        int myTile = 0; // uncomment this line for a small search
        int myTile = 4; // uncomment this line for a big search

        // this is the thread executor service (a singleton)
        ThreadService executorService = ThreadService.INSTANCE;
        System.out.println(executorService);
        Logger log = executorService.getLogger();

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
                    checkIfBusy();
                    Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);

                    // make a map between pieces of work and their futures
                    //submittedJobs.put(thisUnit,thisFuture);
                    System.out.println("Job " + thisUnit.hashCode() + " submitted.");
                    log.log(Level.INFO,"Job " + thisUnit.hashCode() + " submitted.");

                    edgeList = P0.createSkeleton(BD0, BD2, true);
                    testBD = ImmutableList.of(BD0, BD2);

                    patch = BasicPatch.createBasicPatch(edgeList,bigVertices);
                    // identify the Orientations on the two equal edges
                    patch = patch.identify(o1.getOpposite(),o2);

                    // create a new unit of work
                    thisUnit = BasicWorkUnit.createBasicWorkUnit(patch,testBD,tiles);

                    // submit this unit of work to the executor service
                    checkIfBusy();
                    thisFuture = executorService.getExecutor().submit(thisUnit);

                    // make a map between pieces of work and their futures
                    //submittedJobs.put(thisUnit,thisFuture);
                    System.out.println("Job " + thisUnit.hashCode() + " submitted.");
                    log.log(Level.INFO,"Job " + thisUnit.hashCode() + " submitted.");

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
                        checkIfBusy();
                        Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);

                        // make a map between pieces of work and their futures
                        //submittedJobs.put(thisUnit,thisFuture);
                        System.out.println("Job " + thisUnit.hashCode() + " submitted.");
                        log.log(Level.INFO,"Job " + thisUnit.hashCode() + " submitted.");

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

        System.out.println("Job submission complete.");

        // wait for jobs to finish

        while (true)
            {
                try
                    {
                        // wait for a few seconds, then check every second to see if the queue is empty
                        Thread.sleep(1*1000);
                        System.out.print("Waiting for jobs to complete...\r");
                        if ( executorService.getExecutor().isIdle() )
                            {
                                executorService.getExecutor().shutdown();
                                System.out.println("\nAwaiting executor shutdown...");
                                boolean finished = executorService.getExecutor().awaitTermination(5L, TimeUnit.MINUTES);

                                if ( finished )
                                    break;
                                else
                                    System.out.println("Job timeout ran out.  Continuing to wait for all jobs to fnish...");
                            }
                    }
                catch (InterruptedException e)
                    {
                        System.out.println("Interrupted!");
                    }
            }

        System.out.println("All jobs are complete.");

/*        
        // wait a few seconds and then shut down the executor service
        try
            {
                Thread.sleep(5*60*1000);
//                System.out.print("Writing a checkpoint...");
//                executorService.getExecutor().writeCheckpoint();
//                System.out.println("done.");
                log.log(Level.WARNING, "Requesting immediate executor service shutdown.");
                System.out.println("Requesting immediate executor service shutdown.");
                executorService.getExecutor().shutdownNow();                                     // cancel all pending and current jobs
                executorService.getExecutor().awaitTermination(10L, TimeUnit.MINUTES);           // wait until the shutdown is complete
            }
        catch (InterruptedException e)
            {
            }

        //executorService.getExecutor().shutdown();   // use this to tell the executor service to not accept any more jobs, but to finish the ones that have already started
        
        // list the jobs and their results
        for (WorkUnit w : submittedJobs.keySet())
            {
                Future f = submittedJobs.get(w);
                Result r;
                try
                    {
                        r = (Result)f.get(10, TimeUnit.MILLISECONDS);
                    }
                catch (CancellationException e)
                    {
                        r = Result.JOB_INTERRUPTED;
                    }
                catch (ExecutionException e)
                    {
                        r = Result.JOB_FAILED;
                    }
                catch (InterruptedException e)
                    {
                        System.out.println("Attempt to retrieve result was interrupted.");
                        r = Result.JOB_UNAVAILABLE;
                    }
                catch (TimeoutException e)
                    {
                        r = Result.JOB_UNAVAILABLE;
                    }
                System.out.println(w.toString() + " : " + r.toString());

            }
*/

            Enumeration<BasicPatch> output = BasicWorkUnit.output().keys();
            ArrayList<BasicPatch> preList = new ArrayList<>();
            while (output.hasMoreElements()) preList.add(output.nextElement());
            PointsDisplay display = new PointsDisplay(ImmutableList.copyOf(preList),"BigTest");

    }

    // pause if the queue is too full or too many jobs are being run
    private static void checkIfBusy()
    {
        ThreadService executorService = ThreadService.INSTANCE;
        int THRESHOLD = 0;
        boolean throttled = false;
        while (true)
            {
                int queueSize = executorService.getExecutor().getQueue().size();
                if ( queueSize > THRESHOLD || executorService.getExecutor().runningJobs() )
                    {
                        try
                            {
                                Thread.sleep(1*1000);
                                if ( throttled )
                                    System.out.print("\r");
                                System.out.print("Throttling initial job submission...");
                                executorService.getExecutor().printQueues();
                                throttled = true;
                            }
                        catch (InterruptedException e)
                            {
                                continue;
                            }
                    }
                else
                    {
                        if ( throttled )
                            System.out.println();
                        break;
                    }
            }
    }
} // end of SimpleTest
