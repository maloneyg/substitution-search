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

class Initializer {

    static protected final int N;             // the order of symmetry.

    /*
    * This is a little tricky.
    * inflList is a list of integer coefficients of certain numbers.
    * The numbers are the lengths of the diagonals of a 
    * regular n-gon.  We're going to use inflList to create the 
    * inflation matrix. 
    */
    static protected final int[] INFL_LIST = { 1, 1 };

    protected static final IntMatrix ROT;
    protected static final IntMatrix REF;
    protected static final IntMatrix INFL;

    private Initializer(int N) {

        this.N = N;

        /*
        * Pre-matrices.
        */

        // Pre-rotation matrix.
        int[][] preRot = new int[N-1][N-1];

        for (int i = 0; i < N - 2; i++) {
            for (int j = 0; j < N - 2; j++) {
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
                if (j == N - 1 -i) {
                    preRef[i][j] = 1;
                } else {
                    preRef[i][j] = 0;
                }
            }
        }

        // matrix representation of 2*cos(pi/N).
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
        
        ROT = IntMatrix.createIntMatrix(preRot);
        REF = IntMatrix.createIntMatrix(preRef);
        INFL = IntMatrix.createIntMatrix(preInfl);

    }

} // end of class Initializer
