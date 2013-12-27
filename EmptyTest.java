import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;
import com.google.common.collect.*;
import Jama.Matrix;
import java.util.Scanner;

public class EmptyTest
{
    public static final String RESULT_FILENAME = "result.chk";
    public static final String BREAKDOWN_FILENAME = "breakdown.chk";
    public static final List<List<ImmutablePatch>> eventualPatchList = new LinkedList<List<ImmutablePatch>>();

    public static void main(String[] args)
    {

        // this is the thread executor service (a singleton)
        ThreadService executorService = ThreadService.INSTANCE;
        Logger log = executorService.getLogger();

        // create all work
        List<EmptyBoundaryWorkUnit> initialJobs = new LinkedList<>();
        EmptyBoundaryWorkUnitFactory factory = EmptyBoundaryWorkUnitFactory.createEmptyBoundaryWorkUnitFactory();
        while (factory.notDone())
            initialJobs.add( factory.nextWorkUnit() );
        for (EmptyBoundaryWorkUnit u : initialJobs)
            eventualPatchList.add(u.getEventualPatches());

        // start monitoring thread
        double monitorInterval = 1.0; //seconds
        ThreadMonitor threadMonitor = new ThreadMonitor(monitorInterval);

        // submit all jobs
        List<Future<Result>> allFutures = new LinkedList<>();
        for (WorkUnit thisUnit : initialJobs)
            {
                // submit the next work unit
                Future<Result> thisFuture = executorService.getExecutor().submit(thisUnit);
                allFutures.add(thisFuture);
                System.out.println("Job " + thisUnit.hashCode() + " submitted.\n");
                log.log(Level.INFO,"Job " + thisUnit.hashCode() + " submitted.");
            }

        // wait for all jobs to complete
        List<ImmutablePatch> allCompletedPatches = new LinkedList<>();
        for (int i=0; i < allFutures.size(); i++)
            {
                Future<Result> thisFuture = allFutures.get(i);

                // wait until the result is available
                Result thisResult = null;
                try
                    {
                        thisResult = thisFuture.get();
                        //EmptyWorkUnitResult thisEmptyResult = (EmptyWorkUnitResult)thisResult;
                        //int thisSize = thisEmptyResult.getLocalCompletedPatches().size();
                        //totalPatches += thisSize;
                        //if ( thisSize > 0 )
                        //    allCompletedPatches.addAll( thisEmptyResult.getLocalCompletedPatches() );
                    }
                catch (InterruptedException e)
                    {
                        // do not allow interruption
                        i--;
                        continue;
                    }
                catch (ExecutionException e)
                    {
                        e.printStackTrace();
                    }
                catch (CancellationException e)
                    {
                        System.out.println("Job was cancelled!");
                    }
            }

        // wait for spawned units to finish as well      
        // theoretically, if a work unit finishes before its descendents get submitted to the queue, this
        // could cause a problem; however, tests have not shown a problem; eventually this will be
        // replaced by spawnMap which will solve this race condition
        while ( executorService.getExecutor().getQueue().size() > 0        ||
                executorService.getExecutor().getNumberOfRunningJobs() > 0    )
            {
                try
                    {
                        Thread.sleep(500);
                    }
                catch (InterruptedException e)
                    {
                        continue;
                    }
            }

        /* 
            explanation of how we keep track of results:

            - because work units can spawn, there are two kinds of work units: "initial" ones that are created externally
              by a WorkUnitFactory and "spawned" ones that are created internally by EmptyBoundaryWorkUnit
            - implementation: the static factory in EmptyBoundaryWorkUnit will always return work units that are "initial"
              while the private constructor can return spawned units
            - "initial" units are defined as those whose pointer to the eventual ancestor is self-referential
            - an ancestor work unit contains a List<ImmutablePatch> 

            - each work unit returns the results it found during its call() method in Result.getLocalCompletedPatches()
            - all work units have a pointer to their eventual ancestor that can be privately accessed by EmptyBoundaryWorkUnit.initialWorkUnit
            - to get the most current list of all completed patches, call the corresponding EmptyWorkUnitResult.getEventualPatches() method
            - access to this List of eventual results is synchronized
        */

        for (int i=0; i < allFutures.size(); i++)
            {
                Future<Result> thisFuture = allFutures.get(i);

                // wait until the result is available
                Result thisResult = null;
                try
                    {
                        thisResult = thisFuture.get();
                        EmptyWorkUnitResult thisEmptyResult = (EmptyWorkUnitResult)thisResult;
                        // by the time we get here, all initial work units and their descendents will have finished
                        // so it's safe to call getEventualPatches()
                        allCompletedPatches.addAll(thisEmptyResult.getEventualPatches());
                    }
                catch (InterruptedException e)
                    {
                        // do not allow interruption
                        i--;
                        continue;
                    }
                catch (ExecutionException e)
                    {
                        e.printStackTrace();
                    }
                catch (CancellationException e)
                    {
                        System.out.println("Job was cancelled!");
                    }
            }

        // stop monitoring thread
        threadMonitor.stop();

        int totalPatches = allCompletedPatches.size();
        System.out.println("All jobs complete.  " + totalPatches + " completed patches were found.");

        if (allCompletedPatches.size() > 0)
            { // begin writing results
                System.out.print("Writing completed patches to disk...");
                TriangleResults triangleResults = new TriangleResults(allCompletedPatches);
                try
                    {
                        FileOutputStream fileOut = new FileOutputStream(RESULT_FILENAME);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(triangleResults);
                        out.close();
                        fileOut.close();
                        System.out.println("wrote results to " + RESULT_FILENAME + ".");
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                // begin writing edge breakdowns
                System.out.print("Writing edge breakdowns to disk...");
                EdgeBreakdownTree breakdown = EdgeBreakdownTree.createEdgeBreakdownTree();
                for (ImmutablePatch P : allCompletedPatches) {
                    breakdown.addBreakdown(Initializer.acute(Preinitializer.PROTOTILES.get(Preinitializer.MY_TILE).get(0))-1,P.getEdge0());
                    breakdown.addBreakdown(Initializer.acute(Preinitializer.PROTOTILES.get(Preinitializer.MY_TILE).get(1))-1,P.getEdge1());
                    breakdown.addBreakdown(Initializer.acute(Preinitializer.PROTOTILES.get(Preinitializer.MY_TILE).get(2))-1,P.getEdge2());
                }
                try
                    {
                        FileOutputStream fileOut = new FileOutputStream(BREAKDOWN_FILENAME);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(breakdown);
                        out.close();
                        fileOut.close();
                        System.out.println("wrote breakdowns to " + BREAKDOWN_FILENAME + ".");
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                System.out.println("Breakdowns:\n");
                try {
                    System.out.println(breakdown);
                } catch (Exception e) {
                    StackTraceElement[] elmnt = e.getStackTrace();
                    for (int i = 0; i < 10; i++) System.out.println(elmnt[i]);
                    System.exit(1);
                }
            } // end writing results

        // terminate normally
        System.exit(0);
    } // main method ends here

    private static class ThreadMonitor
    {
        private Timer timer;
        private double updateInterval;
        private static ThreadService executorService = ThreadService.INSTANCE;
        private Date lastUpdateTime = null;
        private Date startTime = new Date();
        private LinkedList<Double> throughputs = new LinkedList<Double>();

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
                    System.exit(1);

                // compute statistics
                long jobsRun = executorService.getExecutor().getNumberOfSolveCalls();  // number of jobs run in the last monitorInterval; simultaneously    resets counter
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

                // calculate total number of completed patches
                int numberOfCompletedPatches = 0;
                for (List<ImmutablePatch> l : EmptyTest.eventualPatchList)
                    {
                        synchronized(l)
                            {
                                numberOfCompletedPatches += l.size();
                            }
                    }

                // print statistics
                lastUpdateTime = currentTime;
                ThreadService.INSTANCE.getExecutor().printQueues(throughput, average, totalTime, numberOfCompletedPatches);

                // serialize interim results if more have been added since last time
                if ( Preinitializer.SERIALIZATION_FLAG == true && numberOfCompletedPatches > lastNumberOfCompletedPatches )
                    {
                        LinkedList<ImmutablePatch> interimPatches = new LinkedList<ImmutablePatch>();
                        for (List<ImmutablePatch> l : EmptyTest.eventualPatchList)
                            {
                                synchronized(l)
                                    {
                                        interimPatches.addAll(l);
                                    }
                            }
                        TriangleResults interimResults = new TriangleResults(interimPatches);

                        try
                            {
                                FileOutputStream fileOut = new FileOutputStream(INTERIM_RESULTS_FILENAME);
                                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                                out.writeObject(interimResults);
                                out.close();
                                fileOut.close();
                                int newResults = interimPatches.size() - lastNumberOfCompletedPatches;
                                System.out.println("\n\n" + newResults + " new results, so wrote " + interimPatches.size()
                                                   + " interim results to " + INTERIM_RESULTS_FILENAME + ".\n");
                            }
                        catch (Exception e)
                            {
                                System.out.println("\nError while writing interim results to " + INTERIM_RESULTS_FILENAME + "!");
                                e.printStackTrace();
                            }
                        
                        lastNumberOfCompletedPatches = interimPatches.size();
                    }
            }
        }
    }


} // end of SimpleTest
