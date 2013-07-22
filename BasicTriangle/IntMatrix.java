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
    public IntMatrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new int[M][N];
    }

    // create matrix based on 2d array
    public IntMatrix(int[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new int[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                    this.data[i][j] = data[i][j];
    }

    // copy constructor
    private IntMatrix(IntMatrix A) { this(A.data); }

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
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        IntMatrix C = new IntMatrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] + B.data[i][j];
        return C;
    }


    // return C = A - B
    public IntMatrix minus(IntMatrix B) {
        IntMatrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        IntMatrix C = new IntMatrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] - B.data[i][j];
        return C;
    }

    // does A = B exactly?
    public boolean eq(IntMatrix B) {
        IntMatrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (A.data[i][j] != B.data[i][j]) return false;
        return true;
    }

    // return C = A * B
    public IntMatrix times(IntMatrix B) {
        IntMatrix A = this;
        if (A.N != B.M) throw new RuntimeException("Illegal matrix dimensions.");
        IntMatrix C = new IntMatrix(A.M, B.N);
        for (int i = 0; i < C.M; i++)
            for (int j = 0; j < C.N; j++)
                for (int k = 0; k < A.N; k++)
                    C.data[i][j] += (A.data[i][k] * B.data[k][j]);
        return C;
    }


    // print matrix to standard output
    public void show() {
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) 
                System.out.print("" + data[i][j]);
            System.out.println();
        }
    }



    // test client
    public static void main(String[] args) {
        int[][] d = { { 1, 2, 3 }, { 4, 5, 6 }, { 9, 1, 3} };

        IntMatrix A = new IntMatrix(d);
        A.show(); 
        System.out.println();

        A.swap(1, 2);
        A.show(); 
        System.out.println();

        IntMatrix B = A.transpose();
        B.show(); 
        System.out.println();

        IntMatrix C = IntMatrix.identity(5);
        C.show(); 
        System.out.println();

        A.plus(B).show();
        System.out.println();

        B.times(A).show();
        System.out.println();

        // shouldn't be equal since AB != BA in general    
        System.out.println(A.times(B).eq(B.times(A)));
        System.out.println();

    }
}
