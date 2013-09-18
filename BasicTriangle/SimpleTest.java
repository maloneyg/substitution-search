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

        int myTile = 0;

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
        ArrayList<Future> futures = new ArrayList<Future>();    // keep a list of all the futures
        LinkedHashMap<WorkUnit,Future<Result>> submittedJobs = new LinkedHashMap<WorkUnit,Future<Result>>();
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
                    Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);

                    // make a map between pieces of work and their futures
                    submittedJobs.put(thisUnit,thisFuture);
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

        // wait a few seconds and then shut down the executor service
        try
            {
                Thread.sleep(10*100);
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

            Enumeration<BasicPatch> output = BasicWorkUnit.output().keys();
            ArrayList<BasicPatch> preList = new ArrayList<>();
            while (output.hasMoreElements()) preList.add(output.nextElement());
            PointsDisplay display = new PointsDisplay(ImmutableList.copyOf(preList),"BigTest");

    }
} // end of SimpleTest
