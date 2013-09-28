/*************************************************************************
 *  Compilation:  javac LengthAndAreaCalculator.java
 *  Execution:    java LengthAndAreaCalculator
 *
 *  A class for calculating the lengths of the diagonals of
 *  a regular n-gon and the areas of the triangles that can
 *  be inscribed in it in such a way that their vertices lie
 *  on its vertices.
 *
 *  This class doesn't work yet with even numbers N.  
 *  The calculation for MIN_POLY crashes, probably because the 
 *  minimal polynomial of 2*cos(pi/N) isn't divisible by the
 *  minimal polynomial of 2*cos(pi/(2N)) if N is even.
 *
 *  Also, it doesn't work with non-prime numbers.  
 *  This is because non-prime numbers N require solving matrix
 *  equations with singular integer matrices, for which there
 *  is no good solution in Java. 
 *
 *************************************************************************/

import java.lang.Math.*;
import java.util.ArrayList;
import Jama.Matrix;
import Jama.SingularValueDecomposition;
import Jama.LUDecomposition;
import com.google.common.collect.ImmutableList;

final public class LengthAndAreaCalculator {

    private static final int N = Initializer.N;  // order of rotational symmetry

    // 0, 1, and x as integer polynomials
    private static final IntPolynomial ZERO = IntPolynomial.ZERO;
    private static final IntPolynomial ONE = IntPolynomial.ONE;
    private static final IntPolynomial X = IntPolynomial.X;

    // the minimal polynomial of 2 * Cos(pi/N)
    private static final IntPolynomial MIN_POLY;
    // the companion matrix of MIN_POLY.
    // this is a representation of 2*cos(pi/N), much like
    // Initializer.A, but this representation is, or 
    // should be, irreducible.
    public static final Matrix AMAT;
    // the coefficient matrix of the Tschebyshev polynomials up to N
    public static final Matrix LENGTH_MATRIX;
    // the coefficient matrix of the area polynomials for the prototiles
    public static final Matrix AREA_MATRIX;

    // the minimal polynomial of Cos(pi/N)
    public static final IntPolynomial HALF_MIN_POLY;

    // a list of polynomials expressing the lengths
    // of the diagonals of a regular n-gon in terms 
    // of the first such diagonal (that is not an edge)
    public static final ImmutableList<IntPolynomial> EDGE_LIST;

    // a list of polynomials expressing cos(k pi/N)
    // in terms of cos(pi/N).
    public static final ImmutableList<IntPolynomial> COS_LIST;

    // a list of polynomials expressing sin((k+1) pi/N) / sin(pi/N)
    // in terms of cos(pi/N).
    public static final ImmutableList<IntPolynomial> SIN_LIST;

    // calculate the divisors of an integer
    public static ArrayList<Integer> divisors(int i) {
        if (i <= 0)
            throw new IllegalArgumentException("Can't compute divisors of the non-positive integer " + i + ".");
        ArrayList<Integer> output = new ArrayList<>(1);
        double root = Math.sqrt(i);
        int square = 0;
        if (Math.ceil(root) == Math.floor(root)) 
            square = 1;
        for (int d = 1; d < Math.ceil(root) + square; d++) {
            if (i%d == 0) {
                output.add(d);
                if (d != i/d) output.add(i/d);
            }
        }
        return output;
    }

    // calculate the prime divisors of an integer
    public static ArrayList<Integer> primeDivisors(int i) {
        if (i <= 0)
            throw new IllegalArgumentException("Can't compute prime divisors of the non-positive integer " + i + ".");
        int n = i;
        ArrayList<Integer> output = new ArrayList<>(1);
        for (int d = 2; d <= Math.ceil(Math.sqrt(n)); d++) {
            if (n%d == 0) {
                output.add(d);
                n /= d;
                d--;
            }
        }
        output.add(n);
        return output;
    }

    // calculate the proper prime power divisors of an integer
    public static ArrayList<Integer> primePowerDivisors(int i) {
        if (i <= 0)
            throw new IllegalArgumentException("Can't compute prime power divisors of the non-positive integer " + i + ".");
        ArrayList<Integer> primes = primeDivisors(i);
        ArrayList<Integer> output = new ArrayList<>(1);
        Integer current = 1;
        int index = -1;
        for (Integer p : primes) {
            if (p == current) {
                output.set(index,output.get(index)*p);
            } else {
                index++;
                current = p;
                output.add(p);
            }
        }
        // a kludge to use if i is a power of a prime.
        // drop the power by one.
        if (output.size() == 1)
            output.set(0,output.get(0)/primes.get(0));
        return output;
    }

    // turn an array into a String
    public static String arrayString(double[][] a) {
        String output = "Array:";
        for (int i = 0; i < a.length; i++) {
            output = output + "\n";
            for (int j = 0; j < a[i].length; j++) {
                output = output + a[i][j] + " ";
            }
        }
        return output;
    }

    // turn a Matrix into an IntMatrix.
    // Round and cast to Int.
    public static IntMatrix MatrixToIntMatrix(Matrix m) {
        int L = m.getColumnDimension();
        int M = m.getColumnDimension();
        double[][] a = m.getArrayCopy();
        int[][] preMatrix = new int[L][M];
        for (int i = 0; i < L; i++) {
            for (int j = 0; j < M; j++) {
                preMatrix[i][j] = (int) Math.round((Double)a[i][j]);
            }
        }
        return IntMatrix.createIntMatrix(preMatrix);
    }

    static { // initialize MIN_POLY

        IntPolynomial tempPoly = IntPolynomial.diag(N);
        for (Integer p : primePowerDivisors(N))
            tempPoly = tempPoly.quotient(IntPolynomial.diag(p));
        MIN_POLY = tempPoly;
        AMAT = MIN_POLY.companionMatrix();

        HALF_MIN_POLY = tempPoly.reParametrize(2);

        IntPolynomial[] polyList = new IntPolynomial[N/2];
        IntPolynomial[] cosList = new IntPolynomial[N/2+1];
        IntPolynomial[] sinList = new IntPolynomial[N/2];
        for (int i = 0; i < N/2; i++) {
            polyList[i] = IntPolynomial.tschebyshev(i).mod(MIN_POLY);
            cosList[i] = IntPolynomial.T(i);
            sinList[i] = IntPolynomial.U(i);
        }
        cosList[N/2] = IntPolynomial.T(N/2);
        LENGTH_MATRIX = IntPolynomial.coefficientMatrix(polyList);
        EDGE_LIST = ImmutableList.copyOf(polyList);
        COS_LIST = ImmutableList.copyOf(cosList);
        SIN_LIST = ImmutableList.copyOf(sinList);

    } // static initialization ends here

    static { // initialize the area polynomials

        /*
        * a list of polynomials representing areas of
        * triangles that contain an angle of pi/N.
        * expressed as polynomials in the ring of integers
        * extended by 2*cos(pi/N), with the area of the
        * triangle with angles (1,1,N-2) normalized to be 1.
        */
        IntPolynomial[] narrowAreas = new IntPolynomial[N/2];
        narrowAreas[0] = IntPolynomial.ONE;
        for (int i = 1; i < N/2; i++)
            narrowAreas[i] = EDGE_LIST.get(i).times(EDGE_LIST.get(i)).minus(narrowAreas[i-1]);
        IntPolynomial[] prototileAreas = new IntPolynomial[Preinitializer.PROTOTILES.size()];
        // three int variables used in identifying which 
        // narrow triangle l represents
        int alreadyOne = 0;
        int count = 0;
        int secondMin = N;
        // the two angles in l that are less than 90 degrees
        int a0 = 0;
        int a1 = 0;
        for (ImmutableList<Integer> l : Preinitializer.PROTOTILES) {
            if (l.contains(1)) { // in this case l is a narrow triangle
                secondMin = N; // the second-smallest angle
                alreadyOne = 0;
                for (Integer j : l) {
                    if (j == 1) {
                        // find the second-smallest angle
                        if (alreadyOne > 0) {
                            secondMin = 1;
                            break;
                        } else {
                            alreadyOne++;
                        }
                    } else { // this angle is not 1
                        if (j < secondMin) secondMin = j;
                    }
                }
                prototileAreas[count] = narrowAreas[secondMin-1].mod(MIN_POLY);
                count++;
            } else { // in this case l is not a narrow triangle
                a0 = 0;
                a1 = 0;
                alreadyOne = 0;
                for (Integer j : l) {
                    if (j <= N/2) {
                        if (alreadyOne > 0) {
                            a1 = j;
                            break;
                        } else {
                            a0 = j;
                            alreadyOne++;
                        }
                    }
                }
                prototileAreas[count] = (narrowAreas[a0-1].times(EDGE_LIST.get(a1-1)).times(EDGE_LIST.get(a1-1)).minus(narrowAreas[a1-2].times(EDGE_LIST.get(a0-1)).times(EDGE_LIST.get(a0-1)))).mod(MIN_POLY);
                count++;
            }
        }

        AREA_MATRIX = IntPolynomial.coefficientMatrix(prototileAreas);

    } // initialization of area polynomials ends here

    // test client
    public static void main(String[] args) {

        System.out.println("N: " + N);
        System.out.println("Polynomial: " + MIN_POLY);
        System.out.println("AMAT:");
        System.out.println(arrayString(AMAT.getArray()));

        IntPolynomial testPoly = IntPolynomial.createIntPolynomial(new int[] {1,1});
        System.out.println("testPoly:");
        System.out.println(testPoly);
        System.out.println("AMAT plugged into testPoly:");
        System.out.println(arrayString(testPoly.evaluate(AMAT).getArray()));

        System.out.println("LENGTH_MATRIX:");
        System.out.println(arrayString(LENGTH_MATRIX.getArray()));

        System.out.println("Testing solve():");
        System.out.println("Solve LENGTH_MATRIX * X = (INFLATED) AMAT * LENGTH_MATRIX");
        Matrix LL = LENGTH_MATRIX.inverse().times(testPoly.evaluate(AMAT).times(LENGTH_MATRIX));
        System.out.println(arrayString(LL.getArray()));

        System.out.println("Converting to an IntMatrix:");
        System.out.println(MatrixToIntMatrix(LL));

        System.out.println("Ordinary Tschebyshev polynomials:");
        for (int i = 0; i < COS_LIST.size(); i++)
            System.out.println(COS_LIST.get(i));

        System.out.println("AREA_MATRIX:");
        System.out.println(AREA_MATRIX);
        System.out.println(arrayString(AREA_MATRIX.getArray()));



    }

} // end of class LengthAndAreaCalculator
