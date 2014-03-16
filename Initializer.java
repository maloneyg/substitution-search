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
        E09, E10, E11, E12, E13, E14, E15, E16, //
        E17, E18, E19, E20, E21, E22, E23, E24  //
    }

    public static final Matrix AREA_MATRIX = LengthAndAreaCalculator.AREA_MATRIX;

    public static final int DEG = (int)LengthAndAreaCalculator.MIN_POLY.degree();

    /*
    * A ByteMatrix, the (i,j)th entry of which is the number
    * of occurrences of EdgeLength i in inflated EdgeLength j.
    */
    public static final ByteMatrix INFLATED_LENGTHS;

    /*
    * A ByteMatrix, the (i,j)th entry of which is the number
    * of occurrences of prototile i in inflated prototile j.
    */
    public static final ByteMatrix SUBSTITUTION_MATRIX;

    /*
    * A ByteMatrix giving linear relations between the core prototile
    * areas and the extra prototile areas.
    */
    public static final ByteMatrix NULL_MATRIX;
    /*
    * likewise for lengths
    */
    public static final ByteMatrix LENGTH_NULL_MATRIX;

    /*
    * A column ByteMatrix, the j-th entry of which is the number
    * of occurrences of prototile j in the inflated search tile.
    */
    public static final ByteMatrix TILE_LIST;

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
        EDGE_LENGTH[] preLengths = new EDGE_LENGTH[(Preinitializer.ISOSCELES) ? N/2+N/2-1 : N/2];
        for (int u = 0; u < preLengths.length; u++) {
            preLengths[u] = EDGE_LENGTH.values()[u];
        }

        Matrix otherInfl = infl.evaluate(LengthAndAreaCalculator.AMAT);
        LENGTHS = ImmutableList.copyOf(preLengths);

        INFLATED_LENGTHS = LengthAndAreaCalculator.MatrixToByteMatrix(((Preinitializer.ISOSCELES) ? LengthAndAreaCalculator.ISOLENGTH_MATRIX : LengthAndAreaCalculator.LENGTH_MATRIX).getMatrix(0,DEG-1,0,DEG-1).inverse().times(otherInfl).times(LengthAndAreaCalculator.LENGTH_MATRIX));
        LENGTH_NULL_MATRIX = LengthAndAreaCalculator.MatrixToByteMatrix((LengthAndAreaCalculator.ISOLENGTH_MATRIX.getMatrix(0,DEG-1,0,DEG-1).inverse()).times(LengthAndAreaCalculator.ISOLENGTH_MATRIX.getMatrix(0,DEG-1,DEG,LengthAndAreaCalculator.ISOLENGTH_MATRIX.getColumnDimension()-1)));
        //INFLATED_LENGTHS = null;
        SUBSTITUTION_MATRIX = LengthAndAreaCalculator.MatrixToByteMatrix((((Preinitializer.ISOSCELES) ? LengthAndAreaCalculator.ISOAREA_MATRIX : AREA_MATRIX).inverse()).times(otherInfl).times(otherInfl).times(AREA_MATRIX));
//        System.out.println("RS = " + LengthAndAreaCalculator.MatrixToByteMatrix(otherInfl.times(otherInfl).times(AREA_MATRIX)));
//        System.out.println("LS = " + LengthAndAreaCalculator.MatrixToByteMatrix(AREA_MATRIX));
        NULL_MATRIX = (AREA_MATRIX.getColumnDimension() > DEG) ? LengthAndAreaCalculator.MatrixToByteMatrix((AREA_MATRIX.getMatrix(0,DEG-1,0,DEG-1).inverse()).times(AREA_MATRIX.getMatrix(0,DEG-1,DEG,AREA_MATRIX.getColumnDimension()-1))) : null;
        TILE_LIST = LengthAndAreaCalculator.MatrixToByteMatrix((LengthAndAreaCalculator.AREA_MATRIX.getMatrix(0,DEG-1,0,DEG-1).inverse()).times(otherInfl).times(otherInfl).times(LengthAndAreaCalculator.SEARCH_AREA_COLUMN));

        int total = 1;
        List<Integer> hitsYet = new ArrayList<>(3);
        int factor = 1;
        for (Integer jj : Preinitializer.PROTOTILES.get(Preinitializer.MY_TILE)) {
            if (!hitsYet.contains(jj)) {
                ImmutableList<Integer> totals = INFLATED_LENGTHS.getColumn((jj-1<(N/2))? jj-1 : N-jj-1);
                int subtotal = 0;
                for (Integer ii : totals) subtotal += ii;
                subtotal = factorial(subtotal);
                for (Integer ii : totals) subtotal /= (int)factorial(ii);
                total *= subtotal;
            } else {
                factor = 2;
            }
            hitsYet.add(jj);
        }
        TOTAL_EDGE_BREAKDOWNS = factor * total;

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

    // return String of preamble for gap file
    public static String gapPreambleString(String name) {
        String output = name + " := rec(\n\n  inf := ";
        output += INFL.gapString() + ",\n\n";
        output += "  rot := List( [1.." + (2*N) + "], i->Rot" + N + "^(i-1) ),\n\n";
        output += "  seed := [ rec( pos := [";
        for (int i = 0; i < N-1; i++) {
            output += "0";
            output += ((i==N-2) ? "]" : ",");
        }
        output += ", typ := 1, orient := 0) ],\n\n";
        output += "  basis := List( [0.." + (N-2) + "], i->[Cos(i*Phi" + N + "), Sin(i*Phi" + N + ")]),\n\n";
        return output;
    }

    // return i or N - i, depending on whether i > N/2
    public static int acute(int n) {
        if (n <= N/2) {
            return n;
        } else {
            return N - n;
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
        System.out.println("LENGTH_MATRIX");
        System.out.println(LengthAndAreaCalculator.MatrixToByteMatrix(LengthAndAreaCalculator.LENGTH_MATRIX));
        System.out.println("ISOLENGTH_MATRIX");
        System.out.println(LengthAndAreaCalculator.MatrixToByteMatrix(LengthAndAreaCalculator.ISOLENGTH_MATRIX));
        System.out.println("INFLATED_LENGTHS");
        System.out.println(INFLATED_LENGTHS);
        System.out.println("LENGTH_NULL_MATRIX");
        System.out.println(LENGTH_NULL_MATRIX);
        System.out.println("SUBSTITUTION_MATRIX");
        System.out.println(SUBSTITUTION_MATRIX);
        System.out.println("NULL_MATRIX");
        System.out.println((NULL_MATRIX==null) ? "null" : NULL_MATRIX);
        //System.out.println(NULL_MATRIX.getColumnDimension() + " " + NULL_MATRIX.getRowDimension());
        System.out.println("TILE_LIST");
        System.out.println(TILE_LIST);
        System.out.println("PROTOTILES");
        for (int i = 0; i < Preinitializer.PROTOTILES.size(); i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(Preinitializer.PROTOTILES.get(i).get(j) + " ");
            }
            System.out.print("\n");
        }
        System.out.println("PREPROTOTILES");
        for (int i = 0; i < Preinitializer.PREPROTOTILES.size(); i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(Preinitializer.PREPROTOTILES.get(i).get(j) + " ");
            }
            System.out.print("\n");
        }

    }




} // end of class Initializer
