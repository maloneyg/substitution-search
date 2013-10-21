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

//    public static final double COS = Math.cos(Math.PI/(double)N);// 2cos(pi/N)
    public static final float COS = (float)Math.cos(Math.PI/(double)N);
    public static final float[] COS_LIST; // powers of COS

    public static final float EP = Preinitializer.EP;  // threshold value

    public static final ByteMatrix A;           // 2cos[pi/N], as a matrix
    public static final ByteMatrix ROT;
    public static final ByteMatrix REF;
    public static final ByteMatrix INFL;

    public static final IntMatrix iA;           // 2cos[pi/N], as a matrix
    public static final IntMatrix iROT;
    public static final IntMatrix iREF;
    public static final IntMatrix iINFL;

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

        float[] preCos = new float[N];
        for (int i = 0; i < preCos.length; i++) preCos[i] = (float)Math.pow(COS,i);
        COS_LIST = preCos;

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
        int[][] preIRot = new int[N-1][N-1];

        for (int i = 0; i < N - 2; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (j == i + 1) {
                    preRot[i][j] = O;
                    preIRot[i][j] = 1;
                } else {
                    preRot[i][j] = Z;
                    preIRot[i][j] = 0;
                }
            }
        }

        for (int k = 0; k < N - 1; k++) {
            if (k % 2 == 1) {
                preRot[N-2][k] = O;
                preIRot[N-2][k] = 1;
            } else {
                preRot[N-2][k] = (byte)(-1);
                preIRot[N-2][k] = -1;
            }
        }


        // Pre-reflection matrix.
        byte[][] preRef = new byte[N-1][N-1];
        int[][] preIRef = new int[N-1][N-1];

        preRef[0][0] = (byte)(-1);
        preIRef[0][0] = -1;

        for (int l = 1; l < N - 1; l++) {
            preRef[0][l] = Z;
            preIRef[0][l] = 0;
        }

        for (int k = 0; k < N - 1; k++) {
            if (k % 2 == 1) {
                preRef[1][k] = O;
                preIRef[1][k] = 1;
            } else {
                preRef[1][k] = (byte)(-1);
                preIRef[1][k] = -1;
            }
        }

        for (int i = 2; i < N - 1; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (j == N - i) {
                    preRef[i][j] = O;
                    preIRef[i][j] = 1;
                } else {
                    preRef[i][j] = Z;
                    preIRef[i][j] = 0;
                }
            }
        }

        // matrix representation of 2*cos(pi/N),
        // the shortest non-edge diagonal of a regular n-gon.
        byte[][] a = new byte[N-1][N-1];
        int[][] ia = new int[N-1][N-1];

        for (int k = 0; k < N - 1; k++) {
            if (k % 2 == 1) {
                a[0][k] = (byte)(-1);
                ia[0][k] = -1;
                a[N-2][k] = O;
                ia[N-2][k] = 1;
            } else {
                a[0][k] = O;
                ia[0][k] = 1;
                a[N-2][k] = (byte)(-1);
                ia[N-2][k] = -1;
            }
        }

        a[0][1] = Z;
        ia[0][1] = 0;
        a[N-2][N-3] = Z;
        ia[N-2][N-3] = 0;

        for (int i = 1; i < N - 2; i++) {
            for (int j = 0; j < N - 1; j++) {
                if (j == i + 1 || j == i - 1) {
                    a[i][j] = O;
                    ia[i][j] = 1;
                } else {
                    a[i][j] = Z;
                    ia[i][j] = 0;
                }
            }
        }

        // initialize A
        A = ByteMatrix.createByteMatrix(a);
        iA = IntMatrix.createIntMatrix(ia);

        ROT = ByteMatrix.createByteMatrix(preRot);
        iROT = IntMatrix.createIntMatrix(preIRot);
        REF = ByteMatrix.createByteMatrix(preRef);
        iREF = IntMatrix.createIntMatrix(preIRef);
        INFL = infl.evaluate(A);
        iINFL = infl.evaluate(iA);

        // select a subset of the edge lengths.
        EDGE_LENGTH[] preLengths = new EDGE_LENGTH[N/2];
        for (int u = 0; u < N/2; u++) {
            preLengths[u] = EDGE_LENGTH.values()[u];
        }

        Matrix otherInfl = infl.evaluate(LengthAndAreaCalculator.AMAT);
        LENGTHS = ImmutableList.copyOf(preLengths);

        INFLATED_LENGTHS = LengthAndAreaCalculator.MatrixToByteMatrix((LengthAndAreaCalculator.LENGTH_MATRIX.inverse()).times(otherInfl).times(LengthAndAreaCalculator.LENGTH_MATRIX));
        SUBSTITUTION_MATRIX = LengthAndAreaCalculator.MatrixToByteMatrix((LengthAndAreaCalculator.AREA_MATRIX.inverse()).times(otherInfl).times(otherInfl).times(LengthAndAreaCalculator.AREA_MATRIX));


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
        System.out.println("SUBSTITUTION_MATRIX");
        System.out.println(SUBSTITUTION_MATRIX);

    }




} // end of class Initializer
