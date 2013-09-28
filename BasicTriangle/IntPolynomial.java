/*************************************************************************
 *  Compilation:  javac IntPolynomial.java
 *  Execution:    java IntPolynomial
 *
 *  Polynomials with integer coefficients.
 *
 *************************************************************************/

import java.lang.Math.*;
import java.io.Serializable;
import com.google.common.collect.ImmutableList;
import Jama.Matrix;

public class IntPolynomial implements Serializable {

    private final int[] coef;  // coefficients
    private final int deg;  // degree of polynomial (0 for the zero polynomial)

    // make it Serializable
    static final long serialVersionUID = 6924469409102773009L;

    // constant polynomials
    public static final IntPolynomial ZERO = new IntPolynomial(0,0);
    public static final IntPolynomial ONE = new IntPolynomial(1,0);

    // monic linear polynomial
    public static final IntPolynomial X = new IntPolynomial(1,1);

    // a * x^b
    private IntPolynomial(int a, int b) {
        int[] preCoef = new int[b+1];
        for (int i = 0; i < b+1; i++) preCoef[i] = 0;
        preCoef[b] += a;
        coef = preCoef;
        deg = degree();
    }

    // create a polynomial from an integer array
    private IntPolynomial(int[] a) {
        int[] preCoef = new int[a.length];
        for (int i = 0; i < a.length; i++) preCoef[i] = a[i];
        coef = preCoef;
        deg = degree();
    }

    // public static factory method
    public static IntPolynomial createIntPolynomial(int[] a) {
        return new IntPolynomial(a);
    }

    // public static factory method
    public static IntPolynomial createIntPolynomial(ImmutableList<Integer> a) {
        int[] b = new int[a.size()];
        for (int i = 0; i < a.size(); i++) b[i] = a.get(i);
        return new IntPolynomial(b);
    }

    // return the degree of this polynomial (0 for the zero polynomial)
    public int degree() {
        int d = 0;
        for (int i = 0; i < coef.length; i++)
            if (coef[i] != 0) d = i;
        return d;
    }

    // return c = a + b
    public IntPolynomial plus(IntPolynomial b) {
        IntPolynomial a = this;
        int[] c = new int[Math.max(a.deg,b.deg)+1];
        for (int i = 0; i < c.length; i++) c[i] = 0;
        for (int i = 0; i <= a.deg; i++) c[i] += a.coef[i];
        for (int i = 0; i <= b.deg; i++) c[i] += b.coef[i];
        return new IntPolynomial(c);
    }

    // return (a - b)
    public IntPolynomial minus(IntPolynomial b) {
        IntPolynomial a = this;
        int[] c = new int[Math.max(a.deg,b.deg)+1];
        for (int i = 0; i < c.length; i++) c[i] = 0;
        for (int i = 0; i <= a.deg; i++) c[i] += a.coef[i];
        for (int i = 0; i <= b.deg; i++) c[i] -= b.coef[i];
        return new IntPolynomial(c);
    }

    // return ca
    public IntPolynomial scalarMultiple(int c) {
        int[] b = new int[deg+1];
        for (int i = 0; i < deg+1; i++) b[i] = coef[i]*c;
        return new IntPolynomial(b);
    }

    // return (a * b)
    public IntPolynomial times(IntPolynomial b) {
        IntPolynomial a = this;
        int[] c = new int[a.deg+b.deg+1];
        for (int i = 0; i < c.length; i++) c[i] = 0;
        for (int i = 0; i <= a.deg; i++)
            for (int j = 0; j <= b.deg; j++)
                c[i+j] += (a.coef[i] * b.coef[j]);
        return new IntPolynomial(c);
    }

    // return (a / b) if this is an integer polynomial
    // and the quotient leaves no remainder, otherwise
    // throw an exception
    public IntPolynomial quotient(IntPolynomial b) {
        IntPolynomial a = this;
        if (a.equals(ZERO)) return ZERO;
        if (a.deg < b.deg)
            throw new IllegalArgumentException("Can't divide " + a + " by " + b + ": degree of divisor is too big.");
        int aLeading = a.coef[a.deg];
        int bLeading = b.coef[b.deg];
        if (aLeading % bLeading != 0)
            throw new IllegalArgumentException("Can't divide " + a + " by " + b + ": leading coefficients aren't divisible.");
        IntPolynomial newMonic = new IntPolynomial(aLeading / bLeading, a.deg - b.deg);
        return newMonic.plus(a.minus(newMonic.times(b)).quotient(b));
    }

    // return (a mod b) if this is an integer polynomial
    public IntPolynomial mod(IntPolynomial b) {
        IntPolynomial a = this;
        if (a.deg < b.deg) return a;
        int aLeading = a.coef[a.deg];
        int bLeading = b.coef[b.deg];
        if (aLeading % bLeading != 0)
            throw new IllegalArgumentException("Can't divide " + a + " by " + b + ": leading coefficients aren't divisible.");
        IntPolynomial newMonic = new IntPolynomial(aLeading / bLeading, a.deg - b.deg);
        return a.minus(newMonic.times(b)).mod(b);
    }

    // return a (reparametrized) Tschebyshev polynomial
    public static IntPolynomial tschebyshev(int n) {
        if (n == -1) return ZERO;
        IntPolynomial f0 = ZERO;
        IntPolynomial f1 = ONE;
        IntPolynomial temp;
        for (int i = 0; i < n; i++) {
            temp = f1.times(X).minus(f0);
            f0 = f1;
            f1 = temp;
        }
        return f1;
    }

    // return a Tschebyshev polynomial of the first type
    public static IntPolynomial T(int n) {
        if (n == 0) return ONE;
        if (n == 1) return X;
        IntPolynomial f0 = ONE;
        IntPolynomial f1 = X;
        IntPolynomial TWO_X = X.scalarMultiple(2);
        IntPolynomial temp;
        for (int i = 0; i < n-1; i++) {
            temp = f1.times(TWO_X).minus(f0);
            f0 = f1;
            f1 = temp;
        }
        return f1;
    }

    // return a Tschebyshev polynomial of the second type
    public static IntPolynomial U(int n) {
        return tschebyshev(n).reParametrize(2);
    }

    // return a difference of two Tschebyshev polynomials.
    // If n is odd, they should be degrees (n-1)/2 and (n-1)/2 - 1.
    // If n is even, they should be degrees n/2 and n/2 - 2.
    // Here is a formula that works for both cases:
    // ceiling((n-1)/2) and floor((n-1)/2) - 1
    public static IntPolynomial diag(int n) {
        if (n == 1) return ONE;
        return tschebyshev((n-1)/2+(1-(n%2))).minus(tschebyshev((n-1)/2-1));
    }

    // use Horner's method to compute and return the polynomial evaluated at x
    public int evaluate(int x) {
        int p = 0;
        for (int i = deg; i >= 0; i--)
            p = coef[i] + (x * p);
        return p;
    }

    // use Horner's method to compute and return the polynomial evaluated at x
    public double evaluate(double x) {
        double p = 0.0;
        for (int i = deg; i >= 0; i--)
            p = coef[i] + (x * p);
        return p;
    }

    // make the companion matrix of a polynomial
    public Matrix companionMatrix() {
        IntPolynomial f = this;
        int n = f.deg;
        double[][] preMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (j < n - 1) {
                    if (i == j + 1) {
                        preMatrix[i][j] = 1.0;
                    } else {
                        preMatrix[i][j] = 0.0;
                    }
                } else {
                    preMatrix[i][j] = (double) -f.coef[i];
                }
            }
        }
        return new Matrix(preMatrix);
    }

    // use Horner's method to compute and return the polynomial evaluated at x
    public Matrix evaluate(Matrix x) {
        int m = x.getColumnDimension();
        int n = x.getRowDimension();
        if (m != n) throw new IllegalArgumentException("Can't plug non-square Matrix x into a polynomial. x = " + x);
        Matrix p = new Matrix(m,n);
        Matrix id = Matrix.identity(m,n);
        for (int i = deg; i >= 0; i--) {
            p = p.times(x);
            p.plusEquals(id.times(coef[i])); 
        }
        return p;
    }

    // use Horner's method to compute and return the polynomial evaluated at x
    public IntMatrix evaluate(IntMatrix x) {
        int m = x.getColumnDimension();
        int n = x.getRowDimension();
        if (m != n) throw new IllegalArgumentException("Can't plug non-square IntMatrix x into a polynomial. x = " + x);
        IntMatrix p = IntMatrix.zeroMatrix(m,n);
        IntMatrix id = IntMatrix.identity(m);
        for (int i = deg; i >= 0; i--) {
            p = p.times(x);
            p = p.plus(id.times(coef[i])); 
        }
        return p;
    }

    // make a coefficient matrix for a set of polynomials.
    // the i-th column is a vector, the entries of which
    // are the coefficients of the i-th polynomial.
    public static Matrix coefficientMatrix(IntPolynomial[] p) {
        int d = 0;
        for (int i = 0; i < p.length; i++) if (p[i].deg > d) d = p[i].deg;
        double[][] preMatrix = new double[d+1][p.length];
        for (int i = 0; i <= d; i++) {
            for (int j = 0; j < p.length; j++) {
                if (i <= p[j].deg) {
                    preMatrix[i][j] = p[j].coef[i];
                } else {
                    preMatrix[i][j] = 0;
                }
            }
        }
        return new Matrix(preMatrix);
    }

    // substitute 2x for x.
    public IntPolynomial reParametrize(int n) {
        int [] newCoeffs = new int [deg+1];
        for (int i = 0; i < deg+1; i++)
            newCoeffs[i] = coef[i]*((int)Math.pow(n,i));
        return new IntPolynomial(newCoeffs);
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        IntPolynomial b = (IntPolynomial) obj;
        IntPolynomial a = this;
        if (a.deg != b.deg) return false;
        for (int i = a.deg; i >= 0; i--)
            if (a.coef[i] != b.coef[i]) return false;
        return true;
    }

    // hashCode method
    public int hashCode() {
        int prime = 71;
        int result = 5;
        for (int i = 0; i < deg; i++) {
            result = prime*result + coef[i];
        }
        return result;
    }

    // convert to string representation
    public String toString() {
        if (deg ==  0) return "" + coef[0];
        if (deg ==  1) return coef[1] + "x + " + coef[0];
        String s = coef[deg] + "x^" + deg;
        for (int i = deg-1; i >= 0; i--) {
            if      (coef[i] == 0) continue;
            else if (coef[i]  > 0) s = s + " + " + ( coef[i]);
            else if (coef[i]  < 0) s = s + " - " + (-coef[i]);
            if      (i == 1) s = s + "x";
            else if (i >  1) s = s + "x^" + i;
        }
        return s;
    }

    // test client
    public static void main(String[] args) {

        int[] d = { 1, 2, 3 };
        int[] c = { -2, 5, 3, 0, 0, -4 };
        IntPolynomial D = new IntPolynomial(d);
        IntPolynomial C = new IntPolynomial(c);

        System.out.println("D");
        System.out.println(D);
        System.out.println(D.deg);

        System.out.println("C");
        System.out.println(C);
        System.out.println(C.deg);

        System.out.println("C+D");
        System.out.println(C.plus(D));
        System.out.println(C.plus(D).deg);

        System.out.println("C*D");
        IntPolynomial E = C.times(D);
        System.out.println(E);
        System.out.println(E.deg);

        System.out.println("C*D/D");
        IntPolynomial F = E.quotient(D);
        System.out.println(F);
        System.out.println(F.deg);

        System.out.println("Tschebyshev polynomials:");
        System.out.println(tschebyshev(0));
        System.out.println(tschebyshev(1));
        System.out.println(tschebyshev(2));
        System.out.println(tschebyshev(3));
        System.out.println(tschebyshev(4));
        System.out.println(tschebyshev(5));

        System.out.println("First type:");
        System.out.println(T(0));
        System.out.println(T(1));
        System.out.println(T(2));
        System.out.println(T(3));
        System.out.println(T(4));
        System.out.println(T(5));

        System.out.println("Tschebyshev differences:");
        System.out.println(diag(2));
//        System.out.println(diag(3));
//        System.out.println(diag(4));
        System.out.println(diag(5));
//        System.out.println(diag(6));
//        System.out.println(diag(7));
//        System.out.println(diag(8));
//        System.out.println(diag(9));
        System.out.println(diag(10));

    }

} // end of IntPolynomial
