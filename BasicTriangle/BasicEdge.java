/**
*    This class implements an edge.
*/

import com.google.common.collect.*;

public final class BasicEdge implements AbstractEdge<BasicAngle, BasicPoint, BasicEdgeLength, BasicEdge> {

    // Member variables. 
    private final BasicEdgeLength length;

    private final Orientation orientation;

    private final ImmutableList<BasicPoint> ends;

    // Constructor methods.  
    private BasicEdge(BasicEdgeLength length, Orientation orientation, BasicPoint[] ends) {
        if (ends.length != 2)
            throw new IllegalArgumentException("A BasicEdge must be initialized with two BasicPoints.");
        if (ends[0].equals(ends[1]))
            throw new IllegalArgumentException("A BasicEdge must be initialized with two different BasicPoints.");
        this.length = length;
        this.orientation = orientation;
        this.ends = ImmutableList.of(ends[0],ends[1]);
    }

    // public factory method.
    static public BasicEdge createBasicEdge(BasicEdgeLength length, Orientation orientation, BasicPoint[] ends) {
        return new BasicEdge(length, orientation, ends);
    }

    public BasicEdgeLength getLength() {
        return length;
    }

    public ImmutableList<BasicPoint> getEnds() {
        return ends;
    }

    public BasicEdge transform(BasicAngle a, BasicPoint v) {
        BasicPoint[] newEnds = { ends.get(0).rotate(a).add(v), ends.get(1).rotate(a).add(v) };
        return new BasicEdge(length, orientation, newEnds);
    }

    /* 
    * Given an edge with a (possibly different) orientation,
    * get the orientation of this edge, using the direction
    * convention established by the other edge.  
    */ 
    public Orientation getOrientation(BasicEdge e) {
        if (!(this.length.equals(e.length)))
            throw new IllegalArgumentException("You need to match edges of the same length.");
        BasicPoint u0 = this.ends.get(0);
        BasicPoint u1 = this.ends.get(1);
        BasicPoint v0 = e.ends.get(1);
        BasicPoint v1 = e.ends.get(1);

        if (u0.equals(v0)) {
            if (u1.equals(v1)) {
                return this.orientation;
            } else {
                throw new IllegalArgumentException("You need to match edges in the same position.");
            }
        } else if (u0.equals(v1)) {
            if (u1.equals(v0)) {
                return this.orientation.getOpposite();
            } else {
                throw new IllegalArgumentException("You need to match edges in the same position.");
            }
        }
        throw new IllegalArgumentException("You need to match edges in the same position.");
    }

    // Check if two edges are the same, with identical orientations. 
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicEdge e = (BasicEdge) obj;
        if (!(this.length.equals(e.length)))
            return false;
        BasicPoint u0 = this.ends.get(0);
        BasicPoint u1 = this.ends.get(1);
        BasicPoint v0 = e.ends.get(1);
        BasicPoint v1 = e.ends.get(1);

        if (u0.equals(v0)) {
            if (u1.equals(v1)) {
                return this.orientation.equals(e.orientation);
            } else {
                return false;
            }
        } else if (u0.equals(v1)) {
            if (u1.equals(v0)) {
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

        /*
        * We can't just throw together the hashCode of the ends
        * in order, because two BasicEdges equal one another
        * if their ends are listed in the opposite order and 
        * their orientations are opposites. So we have to do 
        * something more sophisticated. 
        */
        int h0 = ends.get(0).hashCode();
        int h1 = ends.get(1).hashCode();
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
        BasicPoint u0 = this.ends.get(0);
        BasicPoint u1 = this.ends.get(1);
        BasicPoint v0 = e.ends.get(1);
        BasicPoint v1 = e.ends.get(1);

        if (u0.equals(v0)) {
            if (u1.equals(v1)) {
                return !this.orientation.equals(e.orientation.getOpposite());
            } else {
                return false;
            }
        } else if (u0.equals(v1)) {
            if (u1.equals(v0)) {
                return !this.orientation.equals(e.orientation);
            } else {
                return false;
            }
        }
        return false;
    }

} // end of class BasicEdge