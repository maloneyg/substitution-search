/*************************************************************************
 *  Compilation:  javac ShortPolynomial.java
 *  Execution:    java ShortPolynomial
 *
 *  Polynomials with integer coefficients using shorts.
 *
 *************************************************************************/

import java.lang.Math.*;
import java.io.Serializable;
import com.google.common.collect.ImmutableList;
import Jama.Matrix;

public class ShortPolynomial implements Serializable {

    private final short[] coef;  // coefficients
    private final short deg;  // degree of polynomial (0 for the zero polynomial)

    // make it Serializable
    static final long serialVersionUID = 6924469409102773009L;

    // constant polynomials
    public static final ShortPolynomial ZERO = new ShortPolynomial((short)0,(short)0);
    public static final ShortPolynomial ONE = new ShortPolynomial((short)1,(short)0);

    // monic linear polynomial
    public static final ShortPolynomial X = new ShortPolynomial((short)1,(short)1);

    // a * x^b
    private ShortPolynomial(short a, short b) {
        short[] preCoef = new short[b+1];
        for (int i = 0; i < b+1; i++)
            preCoef[i] = (short)0;
        preCoef[b] += a;
        coef = preCoef;
        deg = degree();
    }

    // create a polynomial from an integer array
    private ShortPolynomial(short[] a) {
        short[] preCoef = new short[a.length];
        for (int i = 0; i < a.length; i++)
            preCoef[i] = a[i];
        coef = preCoef;
        deg = degree();
    }

    // public static factory method
    public static ShortPolynomial createShortPolynomial(short[] a) {
        return new ShortPolynomial(a);
    }

    public static ShortPolynomial createShortPolynomial(ImmutableList<Integer> a) {
        short[] b = new short[a.size()];
        for (int i=0; i < a.size(); i++)
            b[i] = (short)(int)a.get(i);
        return new ShortPolynomial(b);
    }

    // return the degree of this polynomial (0 for the zero polynomial)
    public short degree() {
        short d = 0;
        for (int i = 0; i < coef.length; i++)
            if (coef[i] != (short)0)
                d = (short)i;
        return d;
    }

    // return c = a + b
    public ShortPolynomial plus(ShortPolynomial b) {
        ShortPolynomial a = this;
        short[] c = new short[Math.max(a.deg,b.deg)+1];
        for (int i = 0; i < c.length; i++)
            c[i] = (short)0;
        for (int i = 0; i <= a.deg; i++)
            c[i] += a.coef[i];
        for (int i = 0; i <= b.deg; i++)
            c[i] += b.coef[i];
        return new ShortPolynomial(c);
    }

    // return (a - b)
    public ShortPolynomial minus(ShortPolynomial b) {
        ShortPolynomial a = this;
        short[] c = new short[Math.max(a.deg,b.deg)+1];
        for (int i = 0; i < c.length; i++)
            c[i] = (short)0;
        for (int i = 0; i <= a.deg; i++)
            c[i] += a.coef[i];
        for (int i = 0; i <= b.deg; i++)
            c[i] -= b.coef[i];
        return new ShortPolynomial(c);
    }

    // return c*a
    public ShortPolynomial scalarMultiple(short c) {
        short[] b = new short[deg+1];
        for (int i = 0; i < (int)deg+1; i++)
            b[i] = (short)(coef[i]*c);
        return new ShortPolynomial(b);
    }

    // return (a * b)
    public ShortPolynomial times(ShortPolynomial b) {
        ShortPolynomial a = this;
        short[] c = new short[a.deg+b.deg+1];
        for (int i = 0; i < c.length; i++)
            c[i] = (short)0;
        for (int i = 0; i <= a.deg; i++)
            for (int j = 0; j <= b.deg; j++)
                c[i+j] += (a.coef[i] * b.coef[j]);
        return new ShortPolynomial(c);
    }

    // return (a / b) if this is an integer polynomial
    // and the quotient leaves no remainder, otherwise
    // throw an exception
    public ShortPolynomial quotient(ShortPolynomial b) {
        ShortPolynomial a = this;
        if (a.equals(ZERO))
            return ZERO;
        if (a.deg < b.deg)
            throw new IllegalArgumentException("Can't divide " + a + " by " + b + ": degree of divisor is too big.");
        short aLeading = a.coef[a.deg];
        short bLeading = b.coef[b.deg];
        if (aLeading % bLeading != (short)0)
            throw new IllegalArgumentException("Can't divide " + a + " by " + b + ": leading coefficients aren't divisible.");
        ShortPolynomial newMonic = new ShortPolynomial((short)(aLeading / bLeading), (short)(a.deg - b.deg));
        return newMonic.plus(a.minus(newMonic.times(b)).quotient(b));
    }

    // return (a mod b) if this is an integer polynomial
    public ShortPolynomial mod(ShortPolynomial b) {
        ShortPolynomial a = this;
        if (a.deg < b.deg) return a;
        short aLeading = a.coef[a.deg];
        short bLeading = b.coef[b.deg];
        if (aLeading % bLeading != (short)0)
            throw new IllegalArgumentException("Can't divide " + a + " by " + b + ": leading coefficients aren't divisible.");
        ShortPolynomial newMonic = new ShortPolynomial((short)(aLeading / bLeading), (short)(a.deg - b.deg));
        return a.minus(newMonic.times(b)).mod(b);
    }

    // return a (reparametrized) Tschebyshev polynomial
    public static ShortPolynomial tschebyshev(short n) {
        if (n == (short)(-1))
            return ZERO;
        ShortPolynomial f0 = ZERO;
        ShortPolynomial f1 = ONE;
        ShortPolynomial temp;
        for (short i = (short)0; i < n; i++) {
            temp = f1.times(X).minus(f0);
            f0 = f1;
            f1 = temp;
        }
        return f1;
    }

    // return a Tschebyshev polynomial of the first type
    public static ShortPolynomial T(short n) {
        if (n == (short)0)
            return ONE;
        if (n == (short)1)
            return X;
        ShortPolynomial f0 = ONE;
        ShortPolynomial f1 = X;
        ShortPolynomial TWO_X = X.scalarMultiple((short)2);
        ShortPolynomial temp;
        for (short i = 0; i < n-(short)1; i++) {
            temp = f1.times(TWO_X).minus(f0);
            f0 = f1;
            f1 = temp;
        }
        return f1;
    }

    // return a Tschebyshev polynomial of the second type
    public static ShortPolynomial U(short n) {
        return tschebyshev(n).reParametrize((short)2);
    }

    // return a difference of two Tschebyshev polynomials.
    // If n is odd, they should be degrees (n-1)/2 and (n-1)/2 - 1.
    // If n is even, they should be degrees n/2 and n/2 - 2.
    // Here is a formula that works for both cases:
    // ceiling((n-1)/2) and floor((n-1)/2) - 1
    public static ShortPolynomial diag(short n) {
        if (n == (short)1)
            return ONE;
        int arg1 = (n-1)/2+(1-(n%2));
        int arg2 = (n-1)/2-1;
        return tschebyshev((short)arg1).minus(tschebyshev((short)arg2));
    }

    // use Horner's method to compute and return the polynomial evaluated at x
    public short evaluate(short x) {
        short p = (short)0;
        for (int i = (int)deg; i >= 0; i--)
            p = (short)(coef[i] + (short)(x * p));
        return p;
    }

    // use Horner's method to compute and return the polynomial evaluated at x
    public double evaluate(double x) {
        double p = 0.0;
        for (int i = (int)deg; i >= 0; i--)
            p = coef[i] + (x * p);
        return p;
    }

    // make the companion matrix of a polynomial
    public Matrix companionMatrix() {
        int n = (int)this.deg;
        double[][] preMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (j < n - 1)
                    {
                        if (i == j + 1)
                            preMatrix[i][j] = 1.0;
                        else 
                            preMatrix[i][j] = 0.0;
                    }
                else
                    preMatrix[i][j] = (double) -this.coef[i];
                
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
        for (int i = (int)deg; i >= 0; i--) {
            p = p.times(x);
            p.plusEquals(id.times(coef[i])); 
        }
        return p;
    }

    // use Horner's method to compute and return the polynomial evaluated at x
    public ByteMatrix evaluate(ByteMatrix x) {
        int m = x.getColumnDimension();
        int n = x.getRowDimension();
        if (m != n) throw new IllegalArgumentException("Can't plug non-square ByteMatrix x into a polynomial. x = " + x);
        ByteMatrix p = ByteMatrix.zeroMatrix(m,n);
        ByteMatrix id = ByteMatrix.identity(m);
        for (int i = (int)deg; i >= 0; i--) {
            p = p.times(x);
            p = p.plus(id.times(coef[i])); 
        }
        return p;
    }

    // use Horner's method to compute and return the polynomial evaluated at x
    public IntMatrix evaluate(IntMatrix x) {
        int m = x.getColumnDimension();
        int n = x.getRowDimension();
        if (m != n) throw new IllegalArgumentException("Can't plug non-square ByteMatrix x into a polynomial. x = " + x);
        IntMatrix p = IntMatrix.zeroMatrix(m,n);
        IntMatrix id = IntMatrix.identity(m);
        for (int i = (int)deg; i >= 0; i--) {
            p = p.times(x);
            p = p.plus(id.times(coef[i])); 
        }
        return p;
    }

    // make a coefficient matrix for a set of polynomials.
    // the i-th column is a vector, the entries of which
    // are the coefficients of the i-th polynomial.
    public static Matrix coefficientMatrix(ShortPolynomial[] p) {
        short d = (short)0;
        for (int i = 0; i < p.length; i++)
            if (p[i].deg > d)
                d = p[i].deg;
        double[][] preMatrix = new double[d+1][p.length];
        for (int i = 0; i <= (int)d; i++) {
            for (int j = 0; j < p.length; j++) {
                if (i <= p[j].deg) {
                    preMatrix[i][j] = (double)p[j].coef[i];
                } else {
                    preMatrix[i][j] = 0.0;
                }
            }
        }
        return new Matrix(preMatrix);
    }

    // substitute 2x for x.
    public ShortPolynomial reParametrize(short n) {
        short[] newCoeffs = new short[(int)deg+1];
        for (int i = 0; i < (int)deg+1; i++)
            newCoeffs[i] = (short)(coef[i]*(short)Math.pow(n,i));
        return new ShortPolynomial(newCoeffs);
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        ShortPolynomial b = (ShortPolynomial) obj;
        ShortPolynomial a = this;
        if (a.deg != b.deg) return false;
        for (int i = (int)a.deg; i >= 0; i--)
            if (a.coef[i] != b.coef[i])
                return false;
        return true;
    }

    // hashCode method
    public int hashCode() {
        int prime = 71;
        int result = 5;
        for (int i = 0; i < (int)deg; i++) {
            result = prime*result + (int)coef[i];
        }
        return result;
    }

    // convert to string representation
    public String toString() {
        if (deg == (short)0)
            return "" + coef[0];
        if (deg == (short)1)
            return coef[1] + "x + " + coef[0];
        String s = coef[deg] + "x^" + deg;
        for (int i = (int)deg-1; i >= 0; i--) {
            if (coef[i] == (short)0)
                continue;
            else if (coef[i] > (short)0)
                s = s + " + " + ( coef[i]);
            else if (coef[i] < (short)0)
                s = s + " - " + (-coef[i]);
            if (i == 1)
                s = s + "x";
            else if (i > 1) 
                s = s + "x^" + i;
        }
        return s;
    }

} // end of ShortPolynomial
