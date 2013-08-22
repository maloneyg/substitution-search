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

    // calculate the divisors of an integer
    public static ArrayList<Integer> divisors(int i) {
        if (i <= 0)
            throw new IllegalArgumentException("Can't compute divisors of the non-positive integer " + i + ".");
        ArrayList<Integer> output = new ArrayList(1);
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
        ArrayList<Integer> output = new ArrayList(1);
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
        ArrayList<Integer> output = new ArrayList(1);
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
                preMatrix[i][j] = ((Double)a[i][j]).intValue();
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

        IntPolynomial[] polyList = new IntPolynomial[N/2];
        for (int i = 0; i < N/2; i++) polyList[i] = IntPolynomial.tschebyshev(i).mod(MIN_POLY);
        LENGTH_MATRIX = IntPolynomial.coefficientMatrix(polyList);

    }

    // test client
    public static void main(String[] args) {

//        int n = 72;
//        ArrayList<Integer> divs = divisors(n);
//        ArrayList<Integer> primeDivs = primeDivisors(n);
//        ArrayList<Integer> primePowerDivs = primePowerDivisors(n);
//        System.out.println("Divisors of " + n + ":");
//        System.out.println(divs);
//        System.out.println("Prime factorization of " + n + ":");
//        System.out.println(primeDivs);
//        System.out.println("Prime power factorization of " + n + ":");
//        System.out.println(primePowerDivs);

        System.out.println("N: " + N);
        System.out.println("Polynomial: " + MIN_POLY);
        System.out.println("AMAT:");
        System.out.println(arrayString(AMAT.getArray()));

        IntPolynomial testPoly = IntPolynomial.createIntPolynomial(new Integer[] {1,1});
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

    }

} // end of class LengthAndAreaCalculator
