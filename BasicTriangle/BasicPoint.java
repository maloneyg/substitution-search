/**
*    This class implements a point.
*    It uses a representation as a vector of integers.  
*/


final public class BasicPoint implements AbstractPoint<BasicPoint> {

    // static variables for all points.
    private static final int length = 6;

    /*
    * We're going to replace these later with 
    * things calculated in other classes.
    * For now, it's all hard-coded.
    */
    private static final int[][] preRot = {
                       {0, 1, 0, 0, 0, 0}, 
                       {0, 0, 1, 0, 0, 0}, 
                       {0, 0, 0, 1, 0, 0}, 
                       {0, 0, 0, 0, 1, 0}, 
                       {0, 0, 0, 0, 0, 1}, 
                       {-1, 1, -1, 1, -1, 1}
                     };

    private static final IntMatrix rot = IntMatrix.createIntMatrix(preRot);

    private static final int[][] preRef = {
                       {-1, 0, 0, 0, 0, 0}, 
                       {-1, 1, -1, 1, -1, 1},
                       {0, 0, 0, 0, 1, 0}, 
                       {0, 0, 0, 1, 0, 0}, 
                       {0, 0, 1, 0, 0, 0}, 
                       {0, 1, 0, 0, 0, 0} 
                     };

    private static final IntMatrix ref = IntMatrix.createIntMatrix(preRef);

    private static final int[][] preInfl = {
                       {2, 0, 1, -1, 1, -1},
                       {1, 1, 1, 0, 0, 0},
                       {0, 1, 1, 1, 0, 0}, 
                       {0, 0, 1, 1, 1, 0}, 
                       {0, 0, 0, 1, 1, 1}, 
                       {-1, 1, -1, 1, 0, 2}
                     };

    private static final IntMatrix infl = IntMatrix.createIntMatrix(preInfl);

    // A vector identifying the point.  
    private final int[] point;

    // Constructor methods.

    private BasicPoint(int[] vector) {
        if (vector.length != length) {
            throw new IllegalArgumentException("Point length is incorrect.");
        }
        this.point = vector;
    }

    private BasicPoint() {
        int[] vector = new int[length];
        point = vector;
    }

    // toString method.
    public String toString() {
        String outString = "(";
        for (int i = 0; i < length - 1; i++) {
            outString = outString + point[i] + ",";
        }
        outString = outString + point[length] + ")";
        return outString;
    }

    // equals method.
    public boolean equals(BasicPoint p) {
        for (int i = 0; i < length; i++) {
            if (p.point[i] != this.point[i])
                return false;
        }
        return true;
    }

    // Manipulation methods.  
    public BasicPoint add(BasicPoint p) {
        int[] q = new int[length];
        for (int i = 0; i < length; i++) {
            q[i] = this.point[i] + p.point[i];
        }
        return new BasicPoint(q);
    }

    public BasicPoint scalarMultiple(int c) {
        int[] q = new int[length];
        for (int i = 0; i < length; i++) {
            q[i] = c * this.point[i];
        }
        return new BasicPoint(q);
    }

    public BasicPoint subtract(BasicPoint p) {
        return this.add(p.scalarMultiple(-1));
    }

    public BasicPoint rotate(int i) {
        if (i < 0)
            throw new RuntimeException("You must perform a positive number of rotations.");

        int[] result  = new int[length];
        for (int j = 0; j < i; j++)
            result = rot.rowTimes(result);
        return new BasicPoint(result);
    }

    public BasicPoint reflect() {
        return new BasicPoint(ref.rowTimes(this.point));
    }

    public BasicPoint inflate() {
        return new BasicPoint(infl.rowTimes(this.point));
    }

} // end of class BasicPoint
