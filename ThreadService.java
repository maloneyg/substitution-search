import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.io.*;
import java.util.logging.*;
import com.google.common.collect.ImmutableList;

public class ThreadService
{
    private final static Logger log            = Logger.getLogger(ThreadService.class.getName());
    public static final ThreadService INSTANCE = new ThreadService();
    public final int NUMBER_OF_THREADS         = Preinitializer.NUMBER_OF_THREADS; //Runtime.getRuntime().availableProcessors();
    public static final int JOB_CAPACITY              = 10000000;                                 // how many jobs can wait in the queue at a time
    public static final String runningJobsCheckpointFilename = "runningJobs.chk";               // serialized checkpoints
    public static final String pendingJobsCheckpointFilename = "pendingJobs.chk";               // assumed to be in working directory
    public static final String errorsCheckpointFilename = "errors";               // assumed to be in working directory
    private static AtomicInteger errorCounter = new AtomicInteger();

    private final CustomThreadPoolExecutor executorService;

    private ThreadService()
    {
        // create logger
        FileHandler handler = null;
        try
            {
                // read logging configuration file from the local directory
                LogManager.getLogManager().readConfiguration(new FileInputStream("log.config"));

                // write logging data to disk
                handler = new FileHandler();
            }
        catch (Exception e)
            {
                e.printStackTrace();
                System.exit(1);
            }
        log.setLevel(Level.INFO);                       // all messages Level.INFO and above will be written
        log.setUseParentHandlers(false);                // don't send messages to the parent handler
        handler.setFormatter(new LoggingFormatter());   // format logging messages using this inner class
        log.addHandler(handler);
        log.log(Level.INFO, "Logging has been started.");

        // create a thread pool that has exactly NUMBER_OF_THREADS threads
        // this is an anonymous inner class that will log the start and finish of each job
        // when started, the executor service will create threads up to NUMBER_OF_THREADS
        // these threads will be kept running until the service is shut down
        // if more than JOB_CAPACITY jobs is placed in the queue, then the rejected execution policy will decide what happens
        // the "caller runs policy" returns the excess work to the calling thread
        executorService = new CustomThreadPoolExecutor(NUMBER_OF_THREADS,                                     // core pool size
                                                       NUMBER_OF_THREADS,                                     // maximum pool size
                                                       1L,                                                    // keep alive time
                                                       TimeUnit.MINUTES,                                      // keep alive time unit
                                                       new ArrayBlockingQueue<Runnable>(JOB_CAPACITY,true),   // work queue
                                                       new CustomThreadFactory("thread pool"),                // thread factory
                                                       new ThreadPoolExecutor.CallerRunsPolicy());            // rejected execution policy
    }

    public Logger getLogger()
    {
        return log;
    }

    public CustomThreadPoolExecutor getExecutor()
    {
        return executorService;
    }

    protected static class CustomThreadPoolExecutor extends ThreadPoolExecutor
    {
        //private Map<Future<?>,Callable<?>> jobMap = Collections.synchronizedMap(new HashMap<Future<?>,Callable<?>>());
        //private List<Callable<?>> currentlyRunningJobs = Collections.synchronizedList(new ArrayList<Callable<?>>());
        //private List<Callable<?>> currentlyPendingJobs = Collections.synchronizedList(new ArrayList<Callable<?>>());
        //private Map<Callable<?>,Date> startTimes = Collections.synchronizedMap(new HashMap<Callable<?>,Date>());

        public HashMap<RunnableFuture,EmptyBoundaryWorkUnit> jobMap = new HashMap<>();

        private List<AtomicInteger> listOfCounters = new ArrayList<AtomicInteger>();
        private AtomicInteger numberOfJobsRun = new AtomicInteger();
        private AtomicInteger numberOfRunningJobs = new AtomicInteger();
        public static final double GB = 1073741824.0; // bytes per GB

        public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                        ArrayBlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)
        {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        public void registerCounter(AtomicInteger counter)
        {
            synchronized(listOfCounters)
                {
                    listOfCounters.add(counter);
                }
        }

        public void deregisterCounter(AtomicInteger counter)
        {
            synchronized(listOfCounters)
                {
                    listOfCounters.remove(counter);
                }
        }

        // resets all counters and returns the total number of solve calls since the last reset
        public long getNumberOfSolveCalls()
        {
            long numberOfCalls = 0L;
            synchronized (listOfCounters)
                {
                    for (AtomicInteger i : listOfCounters)
                        numberOfCalls += (long)i.getAndSet(0);
                }
            return numberOfCalls;
        }

        //public void printQueues(double throughput, double average, double timeSinceLastUpdate)
        public void printQueues(double throughput, double average, double totalTime, int numberOfCompletedPatches)
        {
            //Runtime runtime = Runtime.getRuntime();
            //String reportString = String.format("Queue: %10d   Running: %2d   Complete: %5d   Average: %6.0f /s   Now: %6.0f / s   Last Update: %.2f s  Memory: %6.3f GB / %6.3f GB    \r",
            //getQueue().size(), numberOfRunningJobs.get(), numberOfJobsRun.get(), average, throughput, timeSinceLastUpdate,
            //(runtime.totalMemory() - runtime.freeMemory()) / GB , (runtime.maxMemory() / GB)     );
            //System.out.print(reportString);
            
            String totalTimeString = "";

            if ( totalTime < 60.0 )
                // time is less than one minute
                totalTimeString = String.format("%.1f s", totalTime);

            else if ( totalTime >= 60.0 && totalTime < 3600.0 )
                // time is between a minute and an hour
                totalTimeString = String.format("%.1f min", totalTime/60.0);

            else if ( totalTime >= 3600.0 && totalTime < 86400.0 )
                // time is between an hour and a day
                {
                    double hours = Math.floor(totalTime / 3600.0);
                    totalTime = totalTime - hours * 3600.0;
                    double minutes = totalTime / 60.0;
                    totalTimeString = String.format("%.0f h %.1f min", hours, minutes);
                }
            else if ( totalTime >= 86400.0 )
                // time is between a day and a week
                {
                    double days = Math.floor(totalTime / 86400.0);
                    totalTime = totalTime - days * 86400.0;
                    double hours = Math.floor(totalTime / 3600.0);
                    totalTime = totalTime - hours * 3600.0;
                    double minutes = totalTime / 60.0;
                    totalTimeString = String.format("%.0f d %.0f h %.1f min", days, hours, minutes); 
                }
            else
                // time is really big
                {
                    totalTimeString = String.format("%6.3 s", totalTime);
                }

            String reportString = String.format("Queue: %6d   Running: %2d   Done: %5d   Found: %5d   Solves: %6.0f /s   Now: %6.0f /s   Elapsed: %s\r",
            getQueue().size(), numberOfRunningJobs.get(), numberOfJobsRun.get(), numberOfCompletedPatches, average, throughput, totalTimeString);
            System.out.print(reportString);
        }

        //private static Map<Runnable,Date> startTimes = Collections.synchronizedMap(new HashMap<Runnable,Date>());

        protected void beforeExecute(Thread t, Runnable r)
        {
            //log.log(Level.INFO, String.format("%s is starting work on %s", Thread.currentThread().getName(), jobMap.get(r).toString()));
            //startTimes.put(r,new Date());

            super.beforeExecute(t,r);
            incrementNumberOfRunningJobs();
         }

        protected void afterExecute(Runnable r, Throwable t)
        {
            /*Date endDate = new Date();
            Date startDate = startTimes.get(r);
            if ( startDate != null )
                {
                    double elapsedTime = ( endDate.getTime() - startTimes.get(r).getTime() ) / (60.0 * 1000.0);
                    log.log(Level.INFO, String.format("Finished a work unit in %.4f minutes.", elapsedTime));
                    startTimes.remove(r);
                }
            else
                log.log(Level.INFO,"Warning: runnable not in the map!");*/

            super.afterExecute(r,t);
            decrementNumberOfRunningJobs();
            numberOfJobsRun.getAndIncrement();
            //log.log(Level.INFO, String.format("%s finished work on %s", Thread.currentThread().getName(), jobName));
            try
                {
                    Future<?> future = (Future<?>) r;
                    if (future.isDone())
                        future.get();
                }
            catch (ExecutionException e)
                {
                    // dump a picture of the error
                    synchronized ( jobMap ) {
                        EmptyBoundaryWorkUnit u = jobMap.get(r); 
                        try
                            {
                                TriangleResults errorResults = new TriangleResults(ImmutableList.of(u.getPatch().dumpImmutablePatch()));
                                while (true)
                                    {
                                        File test = new File(errorsCheckpointFilename + errorCounter.get() + ".chk");
                                        if ( test.isFile() )
                                            errorCounter.incrementAndGet();
                                        else
                                            break;
                                    }
                                FileOutputStream fileOut = new FileOutputStream(errorsCheckpointFilename + errorCounter.get() + ".chk");
                                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                                out.writeObject(errorResults);
                                out.close();
                                fileOut.close();
                                System.out.println("wrote an error to " + errorsCheckpointFilename + errorCounter.get() + ".chk.");
                            }
                        catch (Exception f)
                            {
                                f.printStackTrace();
                            }
                    } // here ends the error picture dump

                    // dump a String describing the error
                    try (PrintWriter out = new PrintWriter(errorsCheckpointFilename + errorCounter.get() + ".txt")) {
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        String exceptionAsString = sw.toString();
                        out.write(exceptionAsString);
                    } catch (Exception f) {
                        f.printStackTrace();
                    }
                    errorCounter.getAndIncrement();

                    // convert stack trace to String
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.getCause().printStackTrace(pw);
                    String message = "EXECUTION EXCEPTION (details follow)" + "\n" + sw.toString(); // stack trace as a string
                    log.log(Level.SEVERE, message);
                }
            catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
            catch (CancellationException e)
                {
                }
            synchronized(jobMap)
                {
                    EmptyBoundaryWorkUnit u = jobMap.remove(r);
                    /*if ( u!=null )
                        System.out.println("success");
                    else
                        System.out.println("failure");*/
                }

        }

        public int getNumberOfJobsRun()
        {
            return numberOfJobsRun.get();
        }

        public int getNumberOfRunningJobs()
        {
            return numberOfRunningJobs.get();
        }

        public int incrementNumberOfRunningJobs()
        {
            return numberOfRunningJobs.getAndIncrement();
        }

        public int decrementNumberOfRunningJobs()
        {
            return numberOfRunningJobs.getAndDecrement();
        }

        public <T> Future<T> submit(Callable<T> task)
        {
            if ( task == null )
                throw new NullPointerException();
            RunnableFuture<T> ftask = newTaskFor(task);
            if ( task instanceof EmptyBoundaryWorkUnit)
                {
                    synchronized(jobMap)
                        {
                            jobMap.put(ftask,(EmptyBoundaryWorkUnit)task);
                        }
                }
            execute(ftask);
            return ftask;
        }

        public void terminated()
        {
            log.log(Level.WARNING, "Executor service has been ordered to shut down.");
        }
    }

    public String toString()
    {
        return "This is a ThreadService with a " + NUMBER_OF_THREADS + " thread pool with a maximum job capacity of " + JOB_CAPACITY;
    }

    // custom formatting for logging messages
    private static class LoggingFormatter extends java.util.logging.Formatter
    {
        public String format(LogRecord record)
            {
                String sourceString = record.getSourceClassName() + "/" + record.getSourceMethodName() + "()";
                String returnString = String.format("[ %s ] (%-60s) Thread %-2s - %7s : %s\n", new Date(record.getMillis()),
                                                    sourceString, record.getThreadID(), record.getLevel(), record.getMessage());
                return returnString;
            }
    }

    // this creates customized threads
    private static class CustomThreadFactory implements ThreadFactory
    {
        private final String poolName;

        public CustomThreadFactory(String poolName)
        {
            this.poolName = poolName;
        }

        // instead of creating Threads, create WorkerThreads (a descendent of Thread)
        public Thread newThread(Runnable runnable)
        {
            return new WorkerThread(runnable, poolName);
        }
    }

    // the subclasses of Thread that will do the calculations
    public static class WorkerThread extends Thread
    {
        private static final AtomicInteger created = new AtomicInteger();     // thread safe integer
        private static final List<WorkerThread> threadList = Collections.synchronizedList(new ArrayList<WorkerThread>()); 

        public WorkerThread(Runnable runnable, String name)
        {
            //super(runnable, name + "-thread " + created.incrementAndGet());   // set work and name for thread
            super(runnable, "thread " + created.incrementAndGet());
            log.log(Level.INFO, "thread created: " + getName());
        }

        public void run()
        {
            log.log(Level.INFO, getName() + " has been started");
            super.run();
            log.log(Level.INFO, getName() + " has been terminated");
        }
    }
} // end of ThreadService
