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

    // the inflation factor, represented as coefficients of
    // 1, a, a^2, etc., where a = 2*cos(pi/N).

//    public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 0, 1); // small search
//    public static final ImmutableList<Integer> INFL = ImmutableList.of(-1, 1, 1); // big search
    public static final ImmutableList<Integer> INFL = ImmutableList.of(1, 1, -3, 0, 1); // huge search

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

    // the zero and unit vectors.  
    // we want to be able to change them from BytePoints to IntPoints.
    public static final AbstractPoint ZERO_VECTOR;
    public static final AbstractPoint UNIT_VECTOR;

    static {
        byte[] preZero = new byte[N-1];
        byte[] preUnit = new byte[N-1];
        preUnit[0] = (byte)1;
        ZERO_VECTOR = BytePoint.createBytePoint(preZero);
        UNIT_VECTOR = BytePoint.createBytePoint(preUnit);

//        int[] preIZero = new int[N-1];
//        int[] preIUnit = new int[N-1];
//        preIUnit[0] = 1;
//        ZERO_VECTOR = IntPoint.createIntPoint(preIZero);
//        UNIT_VECTOR = IntPoint.createIntPoint(preIUnit);
    }

    // method for creating AbstractPoints.
    // we want to be able to toggle between BytePoint and IntPoint
    // just by modifying this file.
    public static AbstractPoint createPoint(AbstractPoint p, boolean flip, BasicAngle a, AbstractPoint shift) {
        return BytePoint.createBytePoint((BytePoint)p,flip,a,(BytePoint)shift);
//        return IntPoint.createIntPoint((IntPoint)p,flip,a,(IntPoint)shift);
    }

} // end of class Preinitializer
