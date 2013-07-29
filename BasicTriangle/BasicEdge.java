/**
*    This class implements an edge.
*/

public class BasicEdge implements AbstractEdge<BasicAngle, BasicPoint, BasicEdgeLength, BasicOrientation, BasicEdge> {

    // Member variables. 
    private final BasicEdgeLength length;

    private BasicOrientation orientation;

    private final BasicPoint[] ends;

    // Constructor methods.  
    private BasicEdge(BasicEdgeLength length, BasicOrientation orientation, BasicPoint[] ends) {
        if (ends.length != 2)
            throw new IllegalArgumentException("A BasicEdge must be initialized with two BasicPoints.");
        if (ends[0].equals(ends[1]))
            throw new IllegalArgumentException("A BasicEdge must be initialized with two different BasicPoints.");
        this.length = length;
        this.orientation = orientation;
        this.ends = ends;
    }

    // public factory method.
    static public BasicEdge createBasicEdge(BasicEdgeLength length, BasicOrientation orientation, BasicPoint[] ends) {
        return new BasicEdge(length, orientation, ends);
    }

    public BasicEdgeLength getLength() {
        return length;
    }

    public BasicPoint[] getEnds() {
        return ends;
    }

    public BasicEdge transform(BasicAngle a, BasicPoint v) {
        BasicPoint[] newEnds = { ends[0].rotate(a).add(v), ends[1].rotate(a).add(v) };
        return new BasicEdge(length, orientation, newEnds);
    }

    /* 
    * Given an edge with a (possibly different) orientation,
    * get the orientation of this edge, using the direction
    * convention established by the other edge.  
    */ 
    public BasicOrientation getOrientation(BasicEdge e) {
        if (!(this.length.equals(e.length)))
            throw new RuntimeException("You need to match edges of the same length.");
        BasicPoint[] u = this.ends;
        BasicPoint[] v = e.ends;

        if (u[0].equals(v[0])) {
            if (u[1].equals(v[1])) {
                return this.orientation;
            } else {
                throw new RuntimeException("You need to match edges in the same position.");
            }
        } else if (u[0].equals(v[1])) {
            if (u[1].equals(v[0])) {
                return this.orientation.getOpposite();
            } else {
                throw new RuntimeException("You need to match edges in the same position.");
            }
        }
        throw new RuntimeException("You need to match edges in the same position.");
    }

    // Check if two edges are the same, with identical orientations. 
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicEdge e = (BasicEdge) obj;
        if (!(this.length.equals(e.length)))
            return false;
        BasicPoint[] u = this.ends;
        BasicPoint[] v = e.ends;

        if (u[0].equals(v[0])) {
            if (u[1].equals(v[1])) {
                return this.orientation.equals(e.orientation);
            } else {
                return false;
            }
        } else if (u[0].equals(v[1])) {
            if (u[1].equals(v[0])) {
                return this.orientation.equals(e.orientation.getOpposite());
            } else {
                return false;
            }
        }
        return false;
    }

    // hashCode override.
    public int hashCode() {
        int prime = 17;
        int result = 19;
        result = prime*result + length.hashCode();
        int h0 = ends[0].hashCode();
        int h1 = ends[1].hashCode();
        if (h0 < h1) {
            result = prime*result + h0;
            result = prime*result + h1;
            result = prime*result + orientation.hashCode();
        } else if (h1 < h0) {
            result = prime*result + h1;
            result = prime*result + h0;
            result = prime*result + orientation.getOpposite().hashCode();
        } else {
            result = prime*result + h0;
            result = prime*result + h1;
        }
        return result;
    }

    // Check if two edges are the same, with non-opposite orientations. 
    public boolean compatible(BasicEdge e) {
        if (!(this.length.equals(e.length)))
            return false;
        BasicPoint[] u = this.ends;
        BasicPoint[] v = e.ends;

        if (u[0].equals(v[0])) {
            if (u[1].equals(v[1])) {
                return !this.orientation.equals(e.orientation.getOpposite());
            } else {
                return false;
            }
        } else if (u[0].equals(v[1])) {
            if (u[1].equals(v[0])) {
                return !this.orientation.equals(e.orientation);
            } else {
                return false;
            }
        }
        return false;
    }

} // end of class BasicEdge
