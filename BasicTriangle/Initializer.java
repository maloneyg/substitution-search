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
import java.util.HashSet;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableSet;

class Initializer {

    protected static final int N = 7;             // the order of symmetry

    protected static final IntMatrix A;           // 2cos[pi/N], as a matrix

    protected static final IntMatrix ROT;
    protected static final IntMatrix REF;
    protected static final IntMatrix INFL;

    /*
    * A list representing edge lengths.  
    * When we initialize we take a subset of these.
    * Notice that it's finite, so if we go to bigger 
    * orders of symmetry, we'll need to add to the list.
    */
    protected enum EDGE_LENGTH {
        E01, E02, E03, E04, E05, E06, E07, E08, //
        E09, E10, E11, E12, E13, E14, E15, E16  //
    }

    /*
    * A list representing the edge lengths we have actually selected.  
    */
    protected static final ImmutableSet<EDGE_LENGTH> LENGTHS;

    static { // start of static initialization

        int[] inflList = new int[] {1, 1}; // need to get this from the user.

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

        // Make matrices representing all diagonals of the N-gon.
        IntMatrix[] diagonals = new IntMatrix[Math.max(inflList.length, 2)];
        IntMatrix preInfl = IntMatrix.zeroMatrix(N-1,N-1);

        // initialize A
        A = IntMatrix.createIntMatrix(a);

        diagonals[0] = IntMatrix.identity(N-1);
        diagonals[1] = A;

        for (int m = 2; m < inflList.length; m++) {
            diagonals[m] = diagonals[1].times(diagonals[m-1]).minus(diagonals[m-2]);
        }

        // Make an integer combination of these matrices, 
        // using coefficients from inflList.
        for (int n = 0; n < inflList.length; n++) {
            preInfl = diagonals[n].times(inflList[n]).plus(preInfl);
        }

        ROT = IntMatrix.createIntMatrix(preRot);
        REF = IntMatrix.createIntMatrix(preRef);
        INFL = preInfl;

        // select a subset of the edge lengths.
        HashSet<EDGE_LENGTH> preLengths = new HashSet();
        for (int u = 0; u < N/2; u++) {
            preLengths.add(EDGE_LENGTH.values()[u]);
        }
        LENGTHS = Sets.immutableEnumSet(preLengths);

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

    }




} // end of class Initializer
