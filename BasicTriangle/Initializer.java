/*************************************************************************
 *  Compilation:  javac Initializer.java
 *  Execution:    java Initializer
 *
 *  Initializes the various integers and matrices associated 
 *  with a search for n-fold symmetry.
 *  I don't want to have all of these constants floating 
 *  around in different classes; I'd rather collect them all
 *  in one place and then take them from here when I need them.  
 *
 *  Really, what I want is to consolidate all the calculations
 *  that depend on n in one place.
 *
 *************************************************************************/

import java.lang.Math.*;
import com.google.common.collect.ImmutableList;
import Jama.Matrix;
import java.util.ArrayList;
import java.util.List;

class Initializer {

    public static final int N = Preinitializer.N;     // the order of symmetry

//    public static final double COS = Math.cos(Math.PI/(double)N);// 2cos(pi/N)
    public static final double COS = Math.cos(Math.PI/(double)N);
    public static final double[] COS_LIST; // powers of COS
    public static final double[] SIN_LIST; // sin((k+1)pi/N)/sin(pi/N)

    public static final float EP = Preinitializer.EP;  // threshold value

    public static final ByteMatrix A;           // 2cos[pi/N], as a matrix

    public static final ByteMatrix ROT;
    public static final ByteMatrix REF;
    public static final ByteMatrix INFL;

    public static final int TOTAL_EDGE_BREAKDOWNS; // total number of work units

    /*
    * A list representing edge lengths.  
    * When we initialize we take a subset of these.
    * Notice that it's finite, so if we go to bigger 
    * orders of symmetry, we'll need to add to the list.
    */
    public enum EDGE_LENGTH {
        E01, E02, E03, E04, E05, E06, E07, E08, //
        E09, E10, E11, E12, E13, E14, E15, E16  //
    }

    /*
    * An ByteMatrix, the (i,j)th entry of which is the number
    * of occurrences of EdgeLength i in inflated EdgeLength j.
    */
    public static final ByteMatrix INFLATED_LENGTHS;

    /*
    * An ByteMatrix, the (i,j)th entry of which is the number
    * of occurrences of prototile i in inflated prototile j.
    */
    public static final ByteMatrix SUBSTITUTION_MATRIX;

    /*
    * A list representing the edge lengths we have actually selected.  
    */
    public static final ImmutableList<EDGE_LENGTH> LENGTHS;

    /*
    * A list representing the prototile angles we have selected.
    */
    public static final ImmutableList<ImmutableList<Integer>> PROTOTILES = Preinitializer.PROTOTILES;

    static { // initialize COS_LIST

        double[] preCos = new double[N/2+1];
        double[] preSin = new double[N/2];
        for (int i = 0; i < preSin.length; i++) {
//            preCos[i] = (float)Math.pow(COS,i);
            preCos[i] = LengthAndAreaCalculator.COS_LIST.get(i).evaluate(COS);
            preSin[i] = LengthAndAreaCalculator.SIN_LIST.get(i).evaluate(COS);
        }
        preCos[N/2] = LengthAndAreaCalculator.COS_LIST.get(N/2).evaluate(COS);
        COS_LIST = preCos;
        SIN_LIST = preSin;

    } // COS_LIST has been initialized

    static { // start of static initialization

        ImmutableList<Integer> inflList = Preinitializer.INFL;
        ShortPolynomial infl = ShortPolynomial.createShortPolynomial(inflList);

        /*
        * Pre-matrices.
        */

        // Pre-rotation matrix.
        byte Z = (byte) 0;
        byte O = (byte) 1;

        byte[][] preRot = new byte[N-1][N-1];

        for (int i = 0; i < N - 2; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (j == i + 1) {
                    preRot[i][j] = O;
                } else {
                    preRot[i][j] = Z;
                }
            }
        }

        for (int k = 0; k < N - 1; k++) {
            if (k % 2 == 1) {
                preRot[N-2][k] = O;
            } else {
                preRot[N-2][k] = (byte)(-1);
            }
        }


        // Pre-reflection matrix.
        byte[][] preRef = new byte[N-1][N-1];

        preRef[0][0] = (byte)(-1);

        for (int l = 1; l < N - 1; l++) {
            preRef[0][l] = Z;
        }

        for (int k = 0; k < N - 1; k++) {
            if (k % 2 == 1) {
                preRef[1][k] = O;
            } else {
                preRef[1][k] = (byte)(-1);
            }
        }

        for (int i = 2; i < N - 1; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (j == N - i) {
                    preRef[i][j] = O;
                } else {
                    preRef[i][j] = Z;
                }
            }
        }

        // matrix representation of 2*cos(pi/N),
        // the shortest non-edge diagonal of a regular n-gon.
        byte[][] a = new byte[N-1][N-1];

        for (int k = 0; k < N - 1; k++) {
            if (k % 2 == 1) {
                a[0][k] = (byte)(-1);
                a[N-2][k] = O;
            } else {
                a[0][k] = O;
                a[N-2][k] = (byte)(-1);
            }
        }

        a[0][1] = Z;
        a[N-2][N-3] = Z;

        for (int i = 1; i < N - 2; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (j == i + 1 || j == i - 1) {
                    a[i][j] = O;
                } else {
                    a[i][j] = Z;
                }
            }
        }

        // initialize A
        A = ByteMatrix.createByteMatrix(a);

        ROT = ByteMatrix.createByteMatrix(preRot);
        REF = ByteMatrix.createByteMatrix(preRef);
        INFL = infl.evaluate(A);

        // select a subset of the edge lengths.
        EDGE_LENGTH[] preLengths = new EDGE_LENGTH[N/2];
        for (int u = 0; u < N/2; u++) {
            preLengths[u] = EDGE_LENGTH.values()[u];
        }

        Matrix otherInfl = infl.evaluate(LengthAndAreaCalculator.AMAT);
        LENGTHS = ImmutableList.copyOf(preLengths);

        INFLATED_LENGTHS = LengthAndAreaCalculator.MatrixToByteMatrix((LengthAndAreaCalculator.LENGTH_MATRIX.inverse()).times(otherInfl).times(LengthAndAreaCalculator.LENGTH_MATRIX));
        SUBSTITUTION_MATRIX = LengthAndAreaCalculator.MatrixToByteMatrix((LengthAndAreaCalculator.AREA_MATRIX.inverse()).times(otherInfl).times(otherInfl).times(LengthAndAreaCalculator.AREA_MATRIX));

        int total = 1;
        List<Integer> hitsYet = new ArrayList<>(3);
        for (Integer jj : Preinitializer.PROTOTILES.get(Preinitializer.MY_TILE)) {
            if (!hitsYet.contains(jj)) {
                ImmutableList<Integer> totals = INFLATED_LENGTHS.getColumn((jj-1<(N/2))? jj-1 : N-jj-1);
                int subtotal = 0;
                for (Integer ii : totals) subtotal += ii;
                subtotal = factorial(subtotal);
                for (Integer ii : totals) subtotal /= (int)factorial(ii);
                total *= subtotal;
            }
            hitsYet.add(jj);
        }
        TOTAL_EDGE_BREAKDOWNS = (BasicPrototile.ALL_PROTOTILES.get(Preinitializer.MY_TILE).isosceles())? 2*total : total;

    } // end of static initialization

    // private constructor
    private Initializer() {
    }

    // factorial function. Couldn't find an (easy) implementation online.
    // returns 1 for negative numbers (lazy).
    private static int factorial(int n) {
        if (n < 1) {
            return 1;
        } else {
            return n*factorial(n-1);
        }
    }


    public static void main(String[] args) {

        System.out.println("ROT");
        System.out.println(ROT);
        System.out.println("REF");
        System.out.println(REF);
        System.out.println("INFL");
        System.out.println(INFL);
        System.out.println("A");
        System.out.println(A);
        System.out.println("INFLATED_LENGTHS");
        System.out.println(INFLATED_LENGTHS);
        System.out.println("SUBSTITUTION_MATRIX");
        System.out.println(SUBSTITUTION_MATRIX);
        System.out.println(TOTAL_EDGE_BREAKDOWNS);

    }




} // end of class Initializer
