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

    public static void main(String[] args)
    {

        // this is the thread executor service (a singleton)
        ThreadService executorService = ThreadService.INSTANCE;
        System.out.println(executorService);
        Logger log = executorService.getLogger();

        // create all work
        List<EmptyBoundaryWorkUnit> initialJobs = new LinkedList<>();
        EmptyBoundaryWorkUnitFactory factory = EmptyBoundaryWorkUnitFactory.createEmptyBoundaryWorkUnitFactory();
        while (factory.notDone())
            initialJobs.add( factory.nextWorkUnit() );

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
        int totalPatches = 0;
        List<ImmutablePatch> allCompletedPatches = new LinkedList<>();
        for (int i=0; i < allFutures.size(); i++)
            {
                Future<Result> thisFuture = allFutures.get(i);

                // wait until the result is available
                Result thisResult = null;
                try
                    {
                        thisResult = thisFuture.get();
                        EmptyWorkUnitResult thisEmptyResult = (EmptyWorkUnitResult)thisResult;
                        int thisSize = thisEmptyResult.getLocalCompletedPatches().size();
                        totalPatches += thisSize;
                        if ( thisSize > 0 )
                            allCompletedPatches.addAll( thisEmptyResult.getLocalCompletedPatches() );
                    }
                catch (InterruptedException e)
                    {
                        // do not allow interruption
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

        try {
            while (!executorService.getExecutor().getQueue().isEmpty()||executorService.getExecutor().getNumberOfRunningJobs()>0) Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Interrupted!");
        }
        
        System.out.println("All jobs complete.  " + totalPatches + " completed patches were found.");


        if (allCompletedPatches.size() > 0)
            {
                System.out.print("Writing completed patches to disk...");
                TriangleResults triangleResults = new TriangleResults(allCompletedPatches);
                //for (ImmutablePatch p : allCompletedPatches) G.add(p.getEdge0(),p.getEdge1(),p.getEdge2(),lengths[0],lengths[1],lengths[2]); // write   to the edge breakdown graph
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
            }

        // stop monitoring thread
        threadMonitor.stop();
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
