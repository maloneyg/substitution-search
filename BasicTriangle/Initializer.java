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

final class Initializer {

    static protected final int n = 7;             // the order of symmetry.

    /*
    * This is a little tricky.
    * inflList is a list of integer coefficients of certain numbers.
    * The numbers are the lengths of the diagonals of a 
    * regular n-gon.  We're going to use inflList to create the 
    * inflation matrix. 
    */
    static protected final int[] inflList = { 1, 1 };

    /*
    * Pre-matrices.
    * 
    * 
    */
    static private final int[][] makePreRot() {
        int[][] preRot = new int[n-1][n-1];

        for (int i = 0; i < n - 2; i++) {
            for (int j = 0; j < n - 2; j++) {
                if (j == i + 1) {
                    preRot[i][j] = 1;
                } else {
                    preRot[i][j] = 0;
                }
            }
        }

        for (int k = 0; k < n - 1; k++) {
            if (k % 2 == 1) {
                preRot[n-2][k] = 1;
            } else {
                preRot[n-2][k] = -1;
            }
        }
        return preRot;
    }

    static private final int[][] makePreRef() {
        int[][] preRef = new int[n-1,n-1];

        preRef[0][0] = -1;

        for (int l = 1; l < n - 1; l++) {
            preRef[0][l] = 0;
        }

        for (int k = 0; k < n - 1; k++) {
            if (k % 2 == 1) {
                preRef[1][k] = 1;
            } else {
                preRef[1][k] = -1;
            }
        }

        for (int i = 2; i < n - 1; i++) {
            for (int j = 0; j < n - 1; j++) {
                if (j == n - 1 -i) {
                    preRef[i][j] = 1;
                } else {
                    preRef[i][j] = 0;
                }
            }
        }

    }

    static private final IntMatrix makePreInfl() {
        int[][] a = new int[n-1,n-1];

        for (int k = 0; k < n - 1; k++) {
            if (k % 2 == 1) {
                a[0][k] = -1;
                a[n-2][k] = 1;
            } else {
                a[0][k] = 1;
                a[n-2][k] = -1;
            }
        }

        a[0][1] = 0;
        a[n-2][n-3] = 0;

        for (int i = 1; i < n - 2; i++) {
            for (int j = 0; j < n - 1; j++) {
                if (j == i + 1 || j == i - 1) {
                    a[i][j] = 1;
                } else {
                    a[i][j] = 0;
                }
            }
        }
        

    }

    private static final int[][] preInfl = {
                       {2, 0, 1, -1, 1, -1},
                       {1, 1, 1, 0, 0, 0},
                       {0, 1, 1, 1, 0, 0}, 
                       {0, 0, 1, 1, 1, 0}, 
                       {0, 0, 0, 1, 1, 1}, 
                       {-1, 1, -1, 1, 0, 2}
                     };

    protected static final IntMatrix rot = IntMatrix.createIntMatrix(makePreRot());
    protected static final IntMatrix ref = IntMatrix.createIntMatrix(makePreRef());
    protected static final IntMatrix infl = IntMatrix.createIntMatrix(makePreInfl());

} // end of class Initializer
