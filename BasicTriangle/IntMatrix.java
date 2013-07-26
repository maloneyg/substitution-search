/*************************************************************************
 *  Compilation:  javac IntMatrix.java
 *  Execution:    java IntMatrix
 *
 *  A bare-bones immutable data type for M-by-N matrices.
 *
 *************************************************************************/

final public class IntMatrix {
    private final int M;             // number of rows
    private final int N;             // number of columns
    private final int[][] data;   // M-by-N array

    // create M-by-N matrix of 0's
    private IntMatrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new int[M][N];
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < M; j++) {
                data[i][j] = 0;
            }
        }
    }

    // public factory method for the zero matrix.
    public IntMatrix zeroMatrix(int M, int N) {
        return new IntMatrix(M, N);
    }

    // create matrix based on 2d array
    private IntMatrix(int[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new int[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                    this.data[i][j] = data[i][j];
    }

    // public factory method.  
    static public IntMatrix createIntMatrix(int[][] data) {
        return new IntMatrix(data);
    }

    // create and return the N-by-N identity matrix
    public static IntMatrix identity(int N) {
        IntMatrix I = new IntMatrix(N, N);
        for (int i = 0; i < N; i++)
            I.data[i][i] = 1;
        return I;
    }

    // swap rows i and j
    private void swap(int i, int j) {
        int[] temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    // create and return the transpose of the invoking matrix
    public IntMatrix transpose() {
        IntMatrix A = new IntMatrix(N, M);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[j][i] = this.data[i][j];
        return A;
    }

    // return C = A + B
    public IntMatrix plus(IntMatrix B) {
        IntMatrix A = this;
        if (B.M != A.M || B.N != A.N) throw new IllegalArgumentException("Illegal matrix dimensions.");
        IntMatrix C = new IntMatrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] + B.data[i][j];
        return C;
    }


    // return C = A - B
    public IntMatrix minus(IntMatrix B) {
        IntMatrix A = this;
        if (B.M != A.M || B.N != A.N) throw new IllegalArgumentException("Illegal matrix dimensions.");
        IntMatrix C = new IntMatrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] - B.data[i][j];
        return C;
    }

    // does A = B exactly?
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        IntMatrix B = (IntMatrix) obj;
        IntMatrix A = this;
        if (B.M != A.M || B.N != A.N) throw new IllegalArgumentException("Illegal matrix dimensions.");
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (A.data[i][j] != B.data[i][j]) return false;
        return true;
    }

    // hashCode override.
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

    // return C = A * B
    public IntMatrix times(IntMatrix B) {
        IntMatrix A = this;
        if (A.N != B.M) throw new IllegalArgumentException("Illegal matrix dimensions.");
        IntMatrix C = new IntMatrix(A.M, B.N);
        for (int i = 0; i < C.M; i++)
            for (int j = 0; j < C.N; j++)
                for (int k = 0; k < A.N; k++)
                    C.data[i][j] += (A.data[i][k] * B.data[k][j]);
        return C;
    }

    /*
    * Convert the integer array i into a row vector,
    * then multiply it on the right by A, then convert
    * the resulting row vector into an integer array.
    */
    public int[] rowTimes(int[] i) {
        IntMatrix A = this;
        if (i.length != A.M) throw new IllegalArgumentException("Illegal matrix dimensions.");
        int[][] row = new int[1][i.length];
        for (int j = 0; j < i.length; j++)
            row[0][j] = i[j];

        IntMatrix result = new IntMatrix(row);
        result = result.times(A);
        for (int j = 0; j < i.length; j++)
            row[0][j] = result.data[0][j];
        return row[0];
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



    // test client
    public static void main(String[] args) {
        int[][] d = { { 1, 2, 3 }, { 4, 5, 6 }, { 9, 1, 3} };

        IntMatrix A = new IntMatrix(d);
        System.out.println(A.toString()); 
        System.out.println();

        A.swap(1, 2);
        System.out.println(A.toString()); 
        System.out.println();

        IntMatrix B = A.transpose();
        System.out.println(B.toString()); 
        System.out.println();

        IntMatrix C = IntMatrix.identity(5);
        System.out.println(C.toString()); 
        System.out.println();

        System.out.println(A.plus(B).toString());
        System.out.println();

        int[] testInt = { 1, 2, 4 };
        for (int k = 0; k < testInt.length; k++) 
            System.out.print(" " + testInt[k]);
        System.out.println();
        A.toString();
        System.out.println();
        testInt = A.rowTimes(testInt);
        for (int k = 0; k < testInt.length; k++) 
            System.out.print(" " + testInt[k]);
        System.out.println();

        System.out.println(B.times(A).toString());
        System.out.println();

        // shouldn't be equal since AB != BA in general    
        System.out.println(A.times(B).equals(B.times(A)));
        System.out.println();

    }
} // end of class IntMatrix
