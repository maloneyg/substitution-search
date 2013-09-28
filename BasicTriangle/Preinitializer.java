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

    public static final int N = 7;             // the order of symmetry

    public static final double EP = 0.000001;  // threshold value

    // the inflation factor, represented as coefficients of
    // 1, a, a^2, etc., where a = 2*cos(pi/N).

    // uncomment the following line for a small search
    //public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 0, 1);
    // uncomment the following line for a big search
    public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 1, 1);

    public static final ImmutableList<ImmutableList<Integer>> PROTOTILES = ImmutableList.of( 
                                 ImmutableList.of( 1, 2, 4 ),  //
                                 ImmutableList.of( 1, 3, 3 ),  //
                                 ImmutableList.of( 2, 2, 3 )   //
                             //    ImmutableList.of( 1, 4, 6 ),  //
                             //    ImmutableList.of( 1, 5, 5 ),  //
                             //    ImmutableList.of( 2, 4, 5 ),  //
                             //    ImmutableList.of( 2, 3, 6 ),  //
                             //    ImmutableList.of( 3, 3, 5 )   //
                                         );

} // end of class Preinitializer
