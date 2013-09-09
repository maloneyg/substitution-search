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

class Initializer {

    public static final int N = Preinitializer.N;     // the order of symmetry

    public static final double COS = Math.cos(Math.PI/(double)N);// 2cos(pi/N)

    public static final double EP = Preinitializer.EP;  // threshold value

    public static final IntMatrix A;           // 2cos[pi/N], as a matrix

    public static final IntMatrix ROT;
    public static final IntMatrix REF;
    public static final IntMatrix INFL;

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
    * An IntMatrix, the (i,j)th entry of which is the number
    * of occurrences of EdgeLength i in inflated EdgeLength j.
    */
    public static final IntMatrix INFLATED_LENGTHS;

    /*
    * A list representing the edge lengths we have actually selected.  
    */
    public static final ImmutableList<EDGE_LENGTH> LENGTHS;

    /*
    * A list representing the prototile angles we have selected.
    */
    public static final ImmutableList<ImmutableList<Integer>> PROTOTILES = Preinitializer.PROTOTILES;

    static { // start of static initialization

        ImmutableList<Integer> inflList = Preinitializer.INFL;
        IntPolynomial infl = IntPolynomial.createIntPolynomial(inflList);

        /*
        * Pre-matrices.
        */

        // Pre-rotation matrix.
        int[][] preRot = new int[N-1][N-1];

        for (int i = 0; i < N - 2; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (j == i + 1) {
                    preRot[i][j] = 1;
                } else {
                    preRot[i][j] = 0;
                }
            }
        }

        for (int k = 0; k < N - 1; k++) {
            if (k % 2 == 1) {
                preRot[N-2][k] = 1;
            } else {
                preRot[N-2][k] = -1;
            }
        }


        // Pre-reflection matrix.
        int[][] preRef = new int[N-1][N-1];

        preRef[0][0] = -1;

        for (int l = 1; l < N - 1; l++) {
            preRef[0][l] = 0;
        }

        for (int k = 0; k < N - 1; k++) {
            if (k % 2 == 1) {
                preRef[1][k] = 1;
            } else {
                preRef[1][k] = -1;
            }
        }

        for (int i = 2; i < N - 1; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (j == N - i) {
                    preRef[i][j] = 1;
                } else {
                    preRef[i][j] = 0;
                }
            }
        }

        // matrix representation of 2*cos(pi/N),
        // the shortest non-edge diagonal of a regular n-gon.
        int[][] a = new int[N-1][N-1];

        for (int k = 0; k < N - 1; k++) {
            if (k % 2 == 1) {
                a[0][k] = -1;
                a[N-2][k] = 1;
            } else {
                a[0][k] = 1;
                a[N-2][k] = -1;
            }
        }

        a[0][1] = 0;
        a[N-2][N-3] = 0;

        for (int i = 1; i < N - 2; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (j == i + 1 || j == i - 1) {
                    a[i][j] = 1;
                } else {
                    a[i][j] = 0;
                }
            }
        }

        // initialize A
        A = IntMatrix.createIntMatrix(a);

        ROT = IntMatrix.createIntMatrix(preRot);
        REF = IntMatrix.createIntMatrix(preRef);
        INFL = infl.evaluate(A);

        // select a subset of the edge lengths.
        EDGE_LENGTH[] preLengths = new EDGE_LENGTH[N/2];
        for (int u = 0; u < N/2; u++) {
            preLengths[u] = EDGE_LENGTH.values()[u];
        }
        LENGTHS = ImmutableList.copyOf(preLengths);

        INFLATED_LENGTHS = LengthAndAreaCalculator.MatrixToIntMatrix(LengthAndAreaCalculator.LENGTH_MATRIX.inverse().times(infl.evaluate(LengthAndAreaCalculator.AMAT).times(LengthAndAreaCalculator.LENGTH_MATRIX)));


    } // end of static initialization

    // private constructor
    private Initializer() {
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

    }




} // end of class Initializer
