/*************************************************************************
 *  Compilation:  javac Preinitializer.java
 *  Execution:    java Preinitializer
 *
 *  Strictly for storing initial data that we might change from one run 
 *  the next:
 *  N, prototiles, inflation factor, and epsilon
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

class Preinitializer {

    public static final int N = 11;             // the order of symmetry

    public static final int MY_TILE = 0;        // the tile we're searching

    public static final float EP = 0.000001f;  // threshold value

    public static final int BATCH_SIZE = 1; // number of jobs to make per set of instructions

    public static final String HOST_NAME; // name of host clients will use; set in static initializer

    public static final int NUMBER_OF_THREADS; // number of threads per client; set in static initializer

    public static final int SPAWN_MAX_SIZE = 1000; // no more work units will be spawned if the queue is bigger than this size
    public static final int SPAWN_MIN_TIME = 10000; // if a work unit takes longer than this time in ms, more units will be spawned

    public static final boolean SERIALIZATION_FLAG = false;          // should EmptyBoundaryPatch.solve() serialize periodically?
                                                                    // results will still be checkpointed periodically
    public static final long SERIALIZATION_INTERVAL = 500L;        // time in ms between serializations
    public static final String SERIALIZATION_DIRECTORY = "storage"; // directory to store checkpoints in
    public static final boolean SERIALIZATION_CLEARFIRST = true;    // clear all files in storage directory before starting

    public static final boolean DEBUG_MODE;
    // the inflation factor, represented as coefficients of
    // 1, a, a^2, etc., where a = 2*cos(pi/N).

    //public static final ImmutableList<Integer> INFL = ImmutableList.of(1, 1); // really small search. Won't work at all for tile 3. (1+a)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(1, 2, 1); // the square of the really small search (1+a)^2 won't work with tile 3 
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 0, 1); // small search (104)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 1, 1); // big search (105) 
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(1, -1, 0, 1); // quite big search (110)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, -2, 1, 1); // quite big search (111)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(2, 0, -3, 0, 1); // huge search (115)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(1, 1, -3, 0, 1); // huge search (116)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(2, 1, -3, 0, 1); // superhuge search (117)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(0, 0, -2, 0, 1); // b+d (118)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(1, -2, -3, 1, 1); // even huger search (121)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(2, -2, -3, 1, 1); // even huger search (122)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(0, -2, -2, 1, 1); // even huger search (124)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(0, 1, 1); // 1 + a + b (106)
    public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, -1, 1, 1); // a + b + c 
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(1, 0, -2, 0, 1); // 1 + b + d 

    public static final ImmutableList<ImmutableList<Integer>> PROTOTILES = ImmutableList.of( 
                             //    ImmutableList.of( 1, 2, 4 ),  // seven
                             //    ImmutableList.of( 1, 3, 3 ),  // seven
                             //    ImmutableList.of( 2, 2, 3 )   // seven
                                 ImmutableList.of( 1, 4, 6 ),  // eleven
                                 ImmutableList.of( 1, 5, 5 ),  // eleven
                                 ImmutableList.of( 2, 4, 5 ),  // eleven
                                 ImmutableList.of( 2, 3, 6 ),  // eleven
                                 ImmutableList.of( 3, 3, 5 )   // eleven
                                         );

    static
        {
            // determine main class
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            StackTraceElement main = stack[stack.length - 1];
            String mainClassName = main.getClassName();
            if ( mainClassName.toLowerCase().indexOf("display") > -1 )
                DEBUG_MODE = true;
            else
                DEBUG_MODE = false;
            System.out.println("Loaded main class " + mainClassName + "; debugging flag set to " + DEBUG_MODE + ".");


            // determine which host name to use
            if ( System.getProperty("user.name").toLowerCase().equals("ekwan") )
                HOST_NAME = "enj10.rc.fas.harvard.edu";
            else if ( System.getProperty("user.name").toLowerCase().equals("ngrm1") )
                HOST_NAME = "corbridge";
            else
                HOST_NAME = "localhost";
            if ( DEBUG_MODE == false )
                System.out.println("Host name automatically set to " + HOST_NAME + ".");

            // determine how many threads to use
            String localhost = "";
            try { localhost = java.net.InetAddress.getLocalHost().getHostName(); } catch (Exception e) {}
            if ( localhost.length() > 0 )
                System.out.println("Local host is: " + localhost);
            if ( localhost.indexOf("enj") > -1 )
                NUMBER_OF_THREADS = 24;
            else
                NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
            System.out.println("Using " + NUMBER_OF_THREADS + " threads.");

            // print out which puzzle we are searching;
            System.out.println("We are searching tile " + MY_TILE + " using inflation factor " + INFL + ".");
        
            // print out serialization settings
            if ( SERIALIZATION_FLAG == true && DEBUG_MODE == false )
                {
                    System.out.println(String.format("Serialization will be performed every %.1f seconds.", SERIALIZATION_INTERVAL/1000.0));
                    
                    // check if storage folder exists
                    File storageDirectory = new File(SERIALIZATION_DIRECTORY);
                    if ( ! storageDirectory.exists() )
                        {
                            boolean success = storageDirectory.mkdirs();
                            if ( success )
                                System.out.println("Created a new storage directory called " + SERIALIZATION_DIRECTORY + "/");
                            else
                                {
                                    System.out.println("Failure creating storage directory!");
                                    System.exit(1);
                                }
                        }
                    else if ( storageDirectory.exists() && ! storageDirectory.isDirectory() )
                        {
                            System.out.println("Fatal error: specified storage directory " + SERIALIZATION_DIRECTORY + " is a regular file!");
                            System.exit(1);
                        }
                    else
                        System.out.println("Will store checkpoints in " + SERIALIZATION_DIRECTORY + "/");

                    // clear directory if requested
                    if ( SERIALIZATION_CLEARFIRST == true )
                        {
                            int numberOfFiles = 0;
                            for ( File f : storageDirectory.listFiles() )
                                {
                                    String[] fields = f.getName().split("\\.");
                                    String extension = fields[fields.length-1];
                                    if ( extension.equals("chk") )
                                        {
                                            f.delete();
                                            numberOfFiles++;
                                        }
                                }
                            if ( numberOfFiles > 0 )
                                System.out.println("Deleted " + numberOfFiles + " pre-existing chk files from " + SERIALIZATION_DIRECTORY + "/");
                        }
                }
            else
                System.out.println("Serialization is off.");

            System.out.println();
        }

} // end of class Preinitializer