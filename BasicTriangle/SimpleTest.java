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

public class SimpleTest
{
    public static void main(String[] args)
    {

        // this is the thread executor service (a singleton)
        ThreadService executorService = ThreadService.INSTANCE;
        System.out.println(executorService);
        Logger log = executorService.getLogger();

        

        BasicPrototile P0 = BasicPrototile.createBasicPrototile(new int[] { 1, 3, 3 });
        BasicPrototile P1 = BasicPrototile.createBasicPrototile(new int[] { 1, 2, 4 });
        BasicPrototile P2 = BasicPrototile.createBasicPrototile(new int[] { 2, 2, 3 });


        PrototileList testTiles = PrototileList.createPrototileList(ImmutableList.of(P1));

        BasicEdge[] edgeList = P0.createSkeleton(//
                                P0.getLengths().get(0).getBreakdown(), //
                                P0.getLengths().get(1).getBreakdown(), //
                                P0.getLengths().get(2).getBreakdown()  //
                                                );

        ImmutableList<ImmutableList<Integer>> testBD = ImmutableList.of(//
                                P0.getLengths().get(0).getBreakdown(), //
                                P0.getLengths().get(1).getBreakdown(), //
                                P0.getLengths().get(2).getBreakdown()  //
                                                );

        BasicPatch patch = BasicPatch.createBasicPatch(edgeList);
        BasicEdge s = patch.getNextEdge();
        ImmutableList<BasicTriangle> newTriangles = P1.placements(s,patch.getEquivalenceClass(s.getOrientation()));
        BasicPatch newPatch = patch.placeTriangle(newTriangles.get(0));
        BasicEdge newEdge = newPatch.getNextEdge();
        ImmutableList<BasicTriangle> newPlacements = P1.placements(newEdge,newPatch.getEquivalenceClass(newEdge.getOrientation()));
        System.out.println(newPlacements);
        

        // submit 30 jobs to the executor service
        // note: if this exceeds JOB_CAPACITY, then this thread will actually do the work, based on the rejected execution handler
        ArrayList<Future> futures = new ArrayList<Future>();    // keep a list of all the futures
        LinkedHashMap<WorkUnit,Future<Result>> submittedJobs = new LinkedHashMap<WorkUnit,Future<Result>>();
        for (int i=0; i < 1; i++)
            {
                // create a new unit of work
                WorkUnit thisUnit = BasicWorkUnit.createBasicWorkUnit(patch,testBD,testTiles);

                // submit this unit of work to the executor service
                // the service returns a Future object
                // when polled, the Future object will either block if the job isn't done yet
                // or return the result if it is
                // (exceptions can be thrown if the job was interrupted/cancelled, failed, etc.)
                Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);

                // make a map between pieces of work and their futures
                submittedJobs.put(thisUnit,thisFuture);
                System.out.println("Job " + (i+1) + " submitted.");
                log.log(Level.INFO,"Job " + (i+1) + " submitted.");
            }

        // wait a few seconds and then shut down the executor service
        try
            {
                Thread.sleep(10*100);
                System.out.print("Writing a checkpoint...");
                executorService.getExecutor().writeCheckpoint();
                System.out.println("done.");
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

            Enumeration<BasicPatch> output = BasicWorkUnit.output().keys();
            PointsDisplay display = new PointsDisplay(output.nextElement().graphicsDump(),"BigTest");

            }

    }
} // end of SimpleTest
