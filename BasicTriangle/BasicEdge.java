/**
*    This class implements an edge.
*/

public class BasicEdge implements AbstractEdge<BasicPoint, BasicEdgeLength, BasicOrientation, BasicEdge> {

    // Member variables. 
    private final BasicEdgeLength length;

    private BasicOrientation orientation;

    private final BasicPoint[] ends;

    // Constructor methods.  
    private BasicEdge(BasicEdgeLength length, BasicOrientation orientation, BasicPoint[] ends) {
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
    public boolean equals(BasicEdge e) {
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
