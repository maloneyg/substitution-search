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

class Preinitializer {

    public static final int N = 11;             // the order of symmetry

    public static final int MY_TILE = 0;        // the tile we're searching

    public static final float EP = 0.000001f;  // threshold value

    public static final int BATCH_SIZE = 1000; // number of jobs to make per set of instructions

    public static final String HOST_NAME; // name of host clients will use; set in static initializer

    public static final int NUMBER_OF_THREADS; // number of threads per client; set in static initializer

    // the inflation factor, represented as coefficients of
    // 1, a, a^2, etc., where a = 2*cos(pi/N).

    //public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 0, 1); // small search
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 1, 1); // big search
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(1, 1, -3, 0, 1); // huge search (116)
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(2, 1, -3, 0, 1); // superhuge search (117)
    public static final ImmutableList<Integer> INFL = ImmutableList.of(1, -2, -3, 1, 1); // even huger search (121)

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
            // determine which host name to use
            if ( System.getProperty("user.name").toLowerCase().equals("ekwan") )
                HOST_NAME = "enj10.rc.fas.harvard.edu";
            else
                HOST_NAME = "corbridge";
            System.out.println("Host name automatically set to " + HOST_NAME + ".");

            // determine how many threads to use
            NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
            System.out.println("Using " + NUMBER_OF_THREADS + " threads.");
        }

} // end of class Preinitializer
