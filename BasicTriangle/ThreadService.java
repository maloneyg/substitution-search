import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.io.*;
import java.util.logging.*;

public class ThreadService
{
    private final static Logger log            = Logger.getLogger(ThreadService.class.getName());
    public static final ThreadService INSTANCE = new ThreadService();
    public final int NUMBER_OF_THREADS         = Runtime.getRuntime().availableProcessors();
    public static final int JOB_CAPACITY              = 10000000;                                 // how many jobs can wait in the queue at a time
    public static final String runningJobsCheckpointFilename = "runningJobs.chk";               // serialized checkpoints
    public static final String pendingJobsCheckpointFilename = "pendingJobs.chk";               // assumed to be in working directory

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
        private AtomicInteger numberOfJobsRun = new AtomicInteger();
        private AtomicInteger numberOfRunningJobs = new AtomicInteger();

        public CustomThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                                        ArrayBlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler)
        {
            super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        }

        public void printQueues(double throughput, double average, double timeSinceLastUpdate)
        {
            String reportString = String.format("Queue size: %10d   Running Jobs: %2d   Average: %8.0f jobs /s   Now: %8.0f jobs / s   Time since last update: %.4f s\r",
            getQueue().size(), numberOfRunningJobs.get(), average, throughput, timeSinceLastUpdate);
            System.out.print(reportString);
        }

        protected void beforeExecute(Thread t, Runnable r)
        {
            //log.log(Level.INFO, String.format("%s is starting work on %s", Thread.currentThread().getName(), jobMap.get(r).toString()));
            super.beforeExecute(t,r);
         }

        protected void afterExecute(Runnable r, Throwable t)
        {
            super.afterExecute(r,t);
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
        }

        public int getNumberOfJobsRun()
        {
            return numberOfJobsRun.getAndSet(0);
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
            Future<T> ftask = super.submit(task);
            return ftask;
        }

        public void terminated()
        {
            log.log(Level.WARNING, "Executor service has been ordered to shut down.");
        }

/*
        public void writeCheckpoint()
        {
            synchronized(this)       // lock the thread service while checkpointing is in progress
                {
                    // copy list of all running jobs
                    List<Callable<?>> runningJobs = new ArrayList<Callable<?>>(currentlyRunningJobs);

                    // copy list of all pending jobs
                    List<Callable<?>> pendingJobs = new ArrayList<Callable<?>>(currentlyPendingJobs);

                    // serialize running jobs
                    FileOutputStream fileOut = null;
                    ObjectOutputStream out = null;
                    try
                        {
                            fileOut = new FileOutputStream(runningJobsCheckpointFilename);
                            out = new ObjectOutputStream(fileOut);
                            out.writeObject(runningJobs);
                            fileOut.close();
                            out.close();
                        }
                    catch (IOException e)
                        {
                            e.printStackTrace();
                            System.exit(1);
                        }

                    // serialize pending jobs
                    try
                        {
                            fileOut = new FileOutputStream(pendingJobsCheckpointFilename);
                            out = new ObjectOutputStream(fileOut);
                            out.writeObject(pendingJobs);
                            fileOut.close();
                            out.close();
                        }
                    catch (IOException e)
                        {
                            e.printStackTrace();
                            System.exit(1);
                        }
                }
        }
*/

    }

    public void loadCheckpoint()
    {
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

        // instead of creating Threads, create WorkerThreads (a descendant of Thread)
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
}
