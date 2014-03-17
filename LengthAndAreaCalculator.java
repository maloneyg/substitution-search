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
    private static final ShortPolynomial ZERO = ShortPolynomial.ZERO;
    private static final ShortPolynomial ONE = ShortPolynomial.ONE;
    private static final ShortPolynomial X = ShortPolynomial.X;

    // the minimal polynomial of 2 * Cos(pi/N)
    public static final ShortPolynomial MIN_POLY;
    // the companion matrix of MIN_POLY.
    // this is a representation of 2*cos(pi/N), much like
    // Initializer.A, but this representation is, or 
    // should be, irreducible.
    public static final Matrix AMAT;
    // the coefficient matrix of the Tschebyshev polynomials up to N
    public static final Matrix LENGTH_MATRIX;
    // adding up consecutive columns of previous matrix
    // these are side lengths of special isosceles triangles
    public static final Matrix ISOLENGTH_MATRIX;
    // the coefficient matrix of the area polynomials for the prototiles
    public static final Matrix AREA_MATRIX;
    // likewise for the special isosceles prototiles
    public static final Matrix ISOAREA_MATRIX;
    // the coefficient matrix of the area polynomial for the tile we're 
    // searching (which might not be one of the prototiles)
    // it's just a column matrix
    public static final Matrix SEARCH_AREA_COLUMN;

    // the minimal polynomial of Cos(pi/N)
    public static final ShortPolynomial HALF_MIN_POLY;

    // a list of polynomials expressing the lengths
    // of the diagonals of a regular n-gon in terms 
    // of the first such diagonal (that is not an edge)
    public static final ImmutableList<ShortPolynomial> EDGE_LIST;

    // a list of polynomials expressing cos(k pi/N)
    // in terms of cos(pi/N).
    public static final ImmutableList<ShortPolynomial> COS_LIST;

    // a list of polynomials expressing sin((k+1) pi/N) / sin(pi/N)
    // in terms of cos(pi/N).
    public static final ImmutableList<ShortPolynomial> SIN_LIST;

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

    // turn a Matrix into an ByteMatrix.
    // Round and cast to Int.
    public static ByteMatrix MatrixToByteMatrix(Matrix m) {
        int M = m.getColumnDimension();
        int L = m.getRowDimension();
        double[][] a = m.getArrayCopy();
        byte[][] preMatrix = new byte[L][M];
        for (int i = 0; i < L; i++) {
            for (int j = 0; j < M; j++) {
                preMatrix[i][j] = (byte) Math.round((Double)a[i][j]);
            }
        }
        return ByteMatrix.createByteMatrix(preMatrix);
    }

    static { // initialize MIN_POLY

        ShortPolynomial tempPoly = ShortPolynomial.diag((short)N);
        for (Integer p : primePowerDivisors(N))
            tempPoly = tempPoly.quotient(ShortPolynomial.diag((short)(int)p));
        MIN_POLY = tempPoly;
        AMAT = MIN_POLY.companionMatrix();

        HALF_MIN_POLY = tempPoly.reParametrize((short)2);

        ShortPolynomial[] polyList = new ShortPolynomial[N/2];
        // polynomials representing edge lengths of special isosceles triangles
        ShortPolynomial[] otherPolyList = new ShortPolynomial[N/2+1];
        ShortPolynomial[] cosList = new ShortPolynomial[N/2+1];
        ShortPolynomial[] sinList = new ShortPolynomial[N/2];
        for (int i = 0; i < N/2; i++) {
            polyList[i] = ShortPolynomial.tschebyshev((short)i).mod(MIN_POLY);
            otherPolyList[i] = (i==0) ? polyList[i] : polyList[i].plus(polyList[i-1]);
            cosList[i] = ShortPolynomial.T((short)i);
            sinList[i] = ShortPolynomial.U((short)i);
        }
        otherPolyList[N/2] = polyList[polyList.length-1];
        cosList[N/2] = ShortPolynomial.T((short)(N/2));
        // lengths of sides of standard, not special isosceles, prototiles
        LENGTH_MATRIX = ShortPolynomial.coefficientMatrix(polyList);
        ISOLENGTH_MATRIX = ShortPolynomial.coefficientMatrix(otherPolyList);
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
        ShortPolynomial[] narrowAreas = new ShortPolynomial[N/2];
        narrowAreas[0] = ShortPolynomial.ONE;
        for (int i = 1; i < N/2; i++)
            narrowAreas[i] = EDGE_LIST.get(i).times(EDGE_LIST.get(i)).minus(narrowAreas[i-1]);
        // this has to do double duty
        // if ISOSCELES, then it's areas of PREPROTOTILES
        // otherwise it's areas of PROTOTILES
        ShortPolynomial[] prototileAreas = new ShortPolynomial[(Preinitializer.ISOSCELES) ? Preinitializer.PREPROTOTILES.size() : Preinitializer.PROTOTILES.size()];

        // three int variables used in identifying which 
        // narrow triangle l represents
        int alreadyOne = 0;
        int count = 0;
        int secondMin = N;
        // the two angles in l that are less than 90 degrees
        int a0 = 0;
        int a1 = 0;
        for (ImmutableList<Integer> l : ((Preinitializer.ISOSCELES) ? Preinitializer.PREPROTOTILES : Preinitializer.PROTOTILES)) {
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

        ShortPolynomial[] otherAreas = new ShortPolynomial[Preinitializer.PROTOTILES.size()];
        for (int i = 0; i < otherAreas.length-1; i++) {
            otherAreas[i] = (2*i < N/2-1) ? prototileAreas[2*i].plus(prototileAreas[2*i+1]) : prototileAreas[N-3-2*i].plus(prototileAreas[N-2*i-4]);
        }
        otherAreas[otherAreas.length-1] = prototileAreas[0];

        AREA_MATRIX = ShortPolynomial.coefficientMatrix(prototileAreas);
        ISOAREA_MATRIX = ShortPolynomial.coefficientMatrix(otherAreas);

    } // initialization of area polynomials ends here

    static { // now do the same thing with SEARCH_TILE

        /*
        * a list of polynomials representing areas of
        * triangles that contain an angle of pi/N.
        * expressed as polynomials in the ring of integers
        * extended by 2*cos(pi/N), with the area of the
        * triangle with angles (1,1,N-2) normalized to be 1.
        */
        ShortPolynomial[] narrowAreas = new ShortPolynomial[N/2];
        narrowAreas[0] = ShortPolynomial.ONE;
        for (int i = 1; i < N/2; i++)
            narrowAreas[i] = EDGE_LIST.get(i).times(EDGE_LIST.get(i)).minus(narrowAreas[i-1]);
        ShortPolynomial[] searchArea = new ShortPolynomial[1];;

        // three int variables used in identifying which 
        // narrow triangle l represents
        int alreadyOne = 0;
        int secondMin = N;
        // the two angles in l that are less than 90 degrees
        int a0 = 0;
        int a1 = 0;
        ImmutableList<Integer> l = Preinitializer.SEARCH_TILE;

        if (l==null) {
            searchArea[0] = ShortPolynomial.ZERO;
        } else {
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
                searchArea[0] = narrowAreas[secondMin-1].mod(MIN_POLY);
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
                searchArea[0] = (narrowAreas[a0-1].times(EDGE_LIST.get(a1-1)).times(EDGE_LIST.get(a1-1)).minus(narrowAreas[a1-2].times(EDGE_LIST.get(a0-1)).times(EDGE_LIST.get(a0-1)))).mod(MIN_POLY);
            }
        } // here ends if(l==null)

        SEARCH_AREA_COLUMN = ShortPolynomial.coefficientMatrix(searchArea,MIN_POLY.degree()-1);

    } // initialization of SEARCH_TILE stuff ends here

    // return a ShortPolynomial representing the length of edge number i
    public static ShortPolynomial getLengthPolynomial(int i) {
        if (i<0||i>LENGTH_MATRIX.getRowDimension()) throw new IllegalArgumentException("Can't get length number " + i + ".");
        return ShortPolynomial.createShortPolynomial(MatrixToByteMatrix(LENGTH_MATRIX).getColumn(i));
    }

    public static void main(String[] args) {
//        System.out.println("SIN_LIST:");
//        for (int i = 0; i < SIN_LIST.size(); i++) System.out.println(SIN_LIST.get(i));
//        System.out.println("COS_LIST:");
//        for (int i = 0; i < COS_LIST.size(); i++) System.out.println(COS_LIST.get(i));
//        System.out.println("INFL:");
//        System.out.println((ShortPolynomial.createShortPolynomial(Preinitializer.INFL)));
        System.out.println("MIN_POLY:");
        System.out.println(MIN_POLY);
//        System.out.println("c * d / a:");
//        System.out.println(getLengthPolynomial(3).times(getLengthPolynomial(4)).quotient(getLengthPolynomial(1)).mod(MIN_POLY));
//        System.out.println("a:");
//        System.out.println(getLengthPolynomial(1));
//        System.out.println("d:");
//        System.out.println(getLengthPolynomial(4));
//        System.out.println("INFL * a:");
//        System.out.println((ShortPolynomial.createShortPolynomial(Preinitializer.INFL).times(getLengthPolynomial(1))));
//        System.out.println(getLengthPolynomial(4).times(dinv).mod(MIN_POLY));
        System.out.println("\nGood stuff:\n");
        ShortPolynomial dinv = ShortPolynomial.createShortPolynomial(new short[] {(short)1,(short)2,(short)-3,(short)-1,(short)1});
        ShortPolynomial ainv = ShortPolynomial.createShortPolynomial(new short[] {(short)3,(short)3,(short)-4,(short)-1,(short)1});
        //System.out.println((ShortPolynomial.createShortPolynomial(Preinitializer.INFL).times(getLengthPolynomial(3))).times(dinv).mod(MIN_POLY));
        System.out.println("times b: " + (ShortPolynomial.createShortPolynomial(Preinitializer.INFL).times(getLengthPolynomial(2))).mod(MIN_POLY));
        System.out.println("times c: " + (ShortPolynomial.createShortPolynomial(Preinitializer.INFL).times(getLengthPolynomial(3))).mod(MIN_POLY));
        System.out.println("times d: " + (ShortPolynomial.createShortPolynomial(Preinitializer.INFL).times(getLengthPolynomial(4))).mod(MIN_POLY));
    }

} // end of class LengthAndAreaCalculator
