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

    public static final int BATCH_SIZE = 500; // number of jobs to make per set of instructions

    // the inflation factor, represented as coefficients of
    // 1, a, a^2, etc., where a = 2*cos(pi/N).

//    public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 0, 1); // small search
    public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 1, 1); // big search
//    public static final ImmutableList<Integer> INFL = ImmutableList.of(1, 1, -3, 0, 1); // huge search

    public static final ImmutableList<ImmutableList<Integer>> PROTOTILES = ImmutableList.of( 
                             //    ImmutableList.of( 1, 2, 4 ),  //
                             //    ImmutableList.of( 1, 3, 3 ),  //
                             //    ImmutableList.of( 2, 2, 3 )   //
                                 ImmutableList.of( 1, 4, 6 ),  //
                                 ImmutableList.of( 1, 5, 5 ),  //
                                 ImmutableList.of( 2, 4, 5 ),  //
                                 ImmutableList.of( 2, 3, 6 ),  //
                                 ImmutableList.of( 3, 3, 5 )   //
                                         );

} // end of class Preinitializer
