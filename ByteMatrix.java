/*************************************************************************
 *  Compilation:  javac ByteMatrix.java
 *  Execution:    java ByteMatrix
 *
 *  A bare-bones immutable data type for M-by-N matrices.
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.io.Serializable;

final public class ByteMatrix implements Serializable {
    private final int M;             // number of rows
    private final int N;             // number of columns
    private final byte[][] data;   // M-by-N array

    // make it Serializable
//    static final long serialVersionUID = 692395754433643754L;

    // create M-by-N matrix of 0's
    private ByteMatrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new byte[M][N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                data[i][j] = (byte) 0;
            }
        }
    }

    // public factory method for the zero matrix.
    static public ByteMatrix zeroMatrix(int M, int N) {
        return new ByteMatrix(M, N);
    }

    // create matrix based on 2d array
    private ByteMatrix(byte[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new byte[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                    this.data[i][j] = data[i][j];
    }

    // public factory method.  
    static public ByteMatrix createByteMatrix(byte[][] data) {
        return new ByteMatrix(data);
    }

    // create and return the N-by-N identity matrix
    public static ByteMatrix identity(int N) {
        ByteMatrix I = new ByteMatrix(N, N);
        for (int i = 0; i < N; i++)
            I.data[i][i] = (byte) 1;
        return I;
    }

    // return M
    public int getColumnDimension() {
        return M;
    }

    // return N
    public int getRowDimension() {
        return N;
    }

    // create and return the transpose of the invoking matrix
    public ByteMatrix transpose() {
        ByteMatrix A = new ByteMatrix(N, M);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[j][i] = this.data[i][j];
        return A;
    }

    // return C = A + B
    public ByteMatrix plus(ByteMatrix B) {
        ByteMatrix A = this;
        if (B.M != A.M || B.N != A.N) throw new IllegalArgumentException("Illegal matrix dimensions.");
        ByteMatrix C = new ByteMatrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = (byte) (A.data[i][j] + B.data[i][j]);
        return C;
    }


    // return C = A - B
    public ByteMatrix minus(ByteMatrix B) {
        ByteMatrix A = this;
        if (B.M != A.M || B.N != A.N) throw new IllegalArgumentException("Illegal matrix dimensions.");
        ByteMatrix C = new ByteMatrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = (byte) (A.data[i][j] - B.data[i][j]);
        return C;
    }

    // does A = B exactly?
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        ByteMatrix B = (ByteMatrix) obj;
        ByteMatrix A = this;
        if (B.M != A.M || B.N != A.N) throw new IllegalArgumentException("Illegal matrix dimensions.");
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (A.data[i][j] != B.data[i][j]) return false;
        return true;
    }

    // hashCode method
    public int hashCode() {
        int prime = 43;
        int result = 7;
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                result = prime*result + data[i][j];
            }
        }
        return result;
    }

    // return C = c * A
    public ByteMatrix times(int c) {
        ByteMatrix A = this;
        ByteMatrix C = new ByteMatrix(A.M, A.N);
        for (int i = 0; i < A.M; i++)
            for (int j = 0; j < A.N; j++)
                C.data[i][j] = (byte) (c * A.data[i][j]);
        return C;
    }

    // return C = A * B
    public ByteMatrix times(ByteMatrix B) {
        ByteMatrix A = this;
        if (A.N != B.M) throw new IllegalArgumentException("Illegal matrix dimensions.");
        ByteMatrix C = new ByteMatrix(A.M, B.N);
        for (int i = 0; i < C.M; i++)
            for (int j = 0; j < C.N; j++)
                for (int k = 0; k < A.N; k++)
                    C.data[i][j] = (byte) (C.data[i][j] + (A.data[i][k] * B.data[k][j]));
        return C;
    }

    /*
    * Convert the integer array i into a row vector,
    * then multiply it on the right by A, then convert
    * the resulting row vector into an integer array.
    */
    public byte[] rowTimes(byte[] i) {
        ByteMatrix A = this;
        if (i.length != A.M) throw new IllegalArgumentException("Illegal matrix dimensions.");
        byte[] row = new byte[A.N];
        for (int k = 0; k < A.N; k++) {
            row[k] = (byte) 0;
            for (int l = 0; l < A.M; l++) {
                row[k] = (byte) (row[k] + i[l]*A.data[l][k]);
            }
        }
        return row;
    }


    // print matrix to standard output
    public String toString() {
        String output = "";
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) 
                output = output + " " + data[i][j];
            output = output + "\n";
        }
        return output;
    }

    // print matrix in gap-readable form
    public String gapString() {
        String output = "[ ";
        for (int i = 0; i < M; i++) {
            output += "[";
            for (int j = 0; j < N; j++) {
                output += data[i][j];
                output += (j==N-1) ? "]" : ",";
            }
            output += (i==M-1) ? " ]" : ", ";
        }
        return output;
    }

    // extract a column
    public ImmutableList<Integer> getColumn(int i) {
        if (i < 0 || i >= N) 
            throw new IllegalArgumentException("Index out of range: can't extract column " + i + " from matrix\n" + this);
        Integer[] output = new Integer[M];
        for (int j = 0; j < M; j++)
            output[j] = Integer.valueOf((int)data[j][i]);
        return ImmutableList.copyOf(output);
    }

    // extract a row
    public ImmutableList<Integer> getRow(int i) {
        if (i < 0 || i >= M) 
            throw new IllegalArgumentException("Index out of range: can't extract row " + i + " from matrix\n" + this);
        Integer[] output = new Integer[N];
        for (int j = 0; j < N; j++)
            output[j] = Integer.valueOf((int)data[i][j]);
        return ImmutableList.copyOf(output);
    }

    /*
    * Convert the integer array i into a row vector,
    * then multiply it on the right by A, then convert
    * the resulting row vector into an integer array.
    */
    public Integer[] rowTimes(Integer[] i) {
        byte[] newArray = new byte[i.length];
        for (int j = 0; j < i.length; j++)
            newArray[j] = (byte) i[j].intValue();
        newArray = this.rowTimes(newArray);
        Integer[] output = new Integer[i.length];
        for (int k = 0; k < i.length; k++)
            output[k] = Integer.valueOf((int)newArray[k]);
        return output;
    }

    // compute the GCD of two integers.
    public static int GCD(int a, int b) {
        if (b == 0)
            return a;
        else
            return GCD(b, a%b);
    }

    // compute the GCD of two bytes.
    public static byte GCD(byte a, byte b) {
        if (b == (byte) 0)
            return a;
        else
            return GCD(b, (byte) (a%b));
    }


    // test client
    public static void main(String[] args) {
        byte[][] d = { { 1, 2, 3 }, { 4, 5, 6 }, { 9, 1, 3} };

        ByteMatrix A = new ByteMatrix(d);
        System.out.println(A.toString()); 
        System.out.println();

        byte[] testInt = { 1, 2, 4 };
        for (int k = 0; k < testInt.length; k++) 
            System.out.print(" " + testInt[k]);
        System.out.println();
        System.out.println();
        System.out.println(A);
        System.out.println();
        testInt = A.rowTimes(testInt);
        for (int j = 0; j < testInt.length; j++) 
            System.out.print(" " + testInt[j]);
        System.out.println();

        System.out.println("A:");
        System.out.println(A);

        System.out.println("Column 0 of A:");
        ImmutableList<Integer> column = A.getColumn(0);
        System.out.print("( ");
        for (Integer i : column) System.out.print(i + " ");
        System.out.print(")\n");

        System.out.println("a = 72.");
        System.out.println("b = 8.");
        System.out.println("GCD(a,b) = " + GCD(72,8) + ".");
        System.out.println("GCD(-b,a) = " + GCD(-8,72) + ".");
        System.out.println("GCD(b,b) = " + GCD(8,8) + ".");

        System.out.print("GCD(10,12) = ");
        System.out.print(GCD(10,12) + ".\n");
        System.out.print("GCD(12,10) = ");
        System.out.print(GCD(12,10) + ".\n");

    }
} // end of class ByteMatrix
