import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

public class ThreadServiceTester
{
    public static void main(String[] args)
    {
        /*Result testResult = new TestResult("a");
        System.out.println(testResult);

        WorkUnit testWorkUnit = new TestWorkUnit(1);
        System.out.println(testWorkUnit);
        

        System.out.println(ThreadService.INSTANCE);*/

        // this is the thread executor service (a singleton)
        ThreadService executorService = ThreadService.INSTANCE;
        System.out.println(executorService);
        Logger log = executorService.getLogger();

        // submit 30 jobs to the executor service
        // note: if this exceeds JOB_CAPACITY, then this thread will actually do the work, based on the rejected execution handler
        ArrayList<Future> futures = new ArrayList<Future>();    // keep a list of all the futures
        LinkedHashMap<WorkUnit,Future<Result>> submittedJobs = new LinkedHashMap<WorkUnit,Future<Result>>();
        for (int i=0; i < 30; i++)
            {
                // create a new unit of work
                WorkUnit thisUnit = TestWorkUnit.getTestWorkUnit(i+1);

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
                Thread.sleep(10*1000);
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
            }

    }
}
