/**
*    This class implements an edge.
*/

import com.google.common.collect.*;
import java.io.Serializable;

public final class BasicEdge implements AbstractEdge<BasicAngle, BasicPoint, BasicEdgeLength, BasicEdge>, Serializable {

    // make it Serializable
    static final long serialVersionUID = -6778708319703245773L;

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
    * return the orientation of this edge.
    * we need this to initialize BasicPatch.
    */ 
    protected Orientation getOrientation() {
        return orientation;
    }

    /* 
    * toString method.  Just spits out the ends.
    */ 
    public String toString() {
        return "Edge\n  " + ends.get(0) + "\n  " + ends.get(1);
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

    // equals and hashCode had some problems.
    // I had designed it so that only edges with identical
    // Orientations are equal, but this is no longer desirable.
    // Now I want two edges to be equal if their endpoints
    // are the same, which turns out to be much easier.
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicEdge e = (BasicEdge) obj;
        BasicPoint p0 = this.ends.get(0);
        BasicPoint p1 = this.ends.get(1);
        BasicPoint q0 = e.ends.get(0);
        BasicPoint q1 = e.ends.get(1);
        if ((p0.equals(q0)&&p1.equals(q1))||(p0.equals(q1)&&p1.equals(q0))) {
            return true;
        } else {
            return false;
        }
    }

    // new hashCode
    public int hashCode() {
        int prime = 17;
        int result = 19;
        int c0 = ends.get(0).hashCode();
        int c1 = ends.get(1).hashCode();
        if (c0<c1)
            result = prime*result + c1;
        result = prime*result + c0;
        if (c0>=c1)
            result = prime*result + c1;
        return result;
    }

    // Check if two edges are the same, with non-opposite orientations. 
    public boolean compatible(BasicEdge e) {
        if (!(this.length.equals(e.length)))
            return false;
        BasicPoint u0 = this.ends.get(0);
        BasicPoint u1 = this.ends.get(1);
        BasicPoint v0 = e.ends.get(0);
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

    // Check if two edges have the same end points
    public boolean congruent(BasicEdge e) {
        if (!(this.length.equals(e.length)))
            return false;
        BasicPoint u0 = this.ends.get(0);
        BasicPoint u1 = this.ends.get(1);
        BasicPoint v0 = e.ends.get(0);
        BasicPoint v1 = e.ends.get(1);

        if (u0.equals(v0)) {
            return u1.equals(v1);
        } else if (u0.equals(v1)) {
            return u1.equals(v0);
        }
        return false;
    }

    // Check if two edges have any end points in common
    public boolean commonEnd(BasicEdge e) {
        BasicPoint u0 = this.ends.get(0);
        BasicPoint u1 = this.ends.get(1);
        BasicPoint v0 = e.ends.get(0);
        BasicPoint v1 = e.ends.get(1);
        return (u0.equals(v0)||u0.equals(v1)||u1.equals(v0)||u1.equals(v1));
    }

    // return the angle that this edge makes with the positive x-axis
    public BasicAngle angle() {
        BasicPoint direction = this.ends.get(1).subtract(this.ends.get(0));
        BasicAngle output = BasicAngle.createBasicAngle(0);
        for (int i = 0; i < 2*BasicAngle.ANGLE_SUM; i++) {
            output = BasicAngle.createBasicAngle(i);
            if (length.getAsVector(output).equals(direction)) break;
        }
        return output;
    }

    // return true if these edges cross, and false otherwise.
    // in particular, return false if they share a common
    // end point or if they have the same slope.
    public boolean cross(BasicEdge e) {
        if (commonEnd(e)) return false;
        BasicAngle a0 = this.angle();
        BasicAngle a1 = e.angle();
        if (a0.equals(a1)||a0.equals(a1.piPlus())) return false;
        BasicPoint u0 = this.ends.get(0);
        BasicPoint u1 = this.ends.get(1);
        BasicPoint v0 = e.ends.get(0);
        BasicPoint v1 = e.ends.get(1);
        BasicPoint m0 = u1.subtract(u0); // the direction vector for this edge
        BasicPoint m1 = v1.subtract(v0); // the direction vector for e
        return (Math.signum((u0.subtract(v0)).crossProduct(m1).evaluate(Initializer.COS)) == Math.signum((u1.subtract(v0)).crossProduct(m).evaluate(Initializer.COS)))
        return output;
    }

                return false;
        }
        return true;
    }

    // return the same edge, with end points listed
    // in reverse order and the opposite Orientation
    public BasicEdge reverse() {
        return new BasicEdge(length, orientation.getOpposite(), new BasicPoint[] {ends.get(1), ends.get(0)});
    }

    // if two edges are congruent, extract the Orientations
    // that get identified by placing them incident to one 
    // another.
    // No sanity check! We assume that they're congruent.
    protected ImmutableList<Orientation> getMatches(BasicEdge e) {
        BasicPoint u0 = this.ends.get(0);
        BasicPoint v0 = e.ends.get(0);
        if (u0.equals(v0)) {
            return ImmutableList.of(this.orientation,e.orientation);
        } else {
            return ImmutableList.of(this.orientation,e.orientation.getOpposite());
        }
    }

    // check to see if the BasicPoint p touches this edge
    // (not necessarily at the ends)
    public boolean incident(BasicPoint p) {
        BasicPoint u0 = this.ends.get(0).subtract(p);
        BasicPoint u1 = p.subtract(this.ends.get(1));
        BasicPoint u2 = this.ends.get(0).subtract(this.ends.get(1));
        double d0 = Math.sqrt(u0.dotProduct(u0).evaluate(Initializer.COS));
        double d1 = Math.sqrt(u1.dotProduct(u1).evaluate(Initializer.COS));
        double d2 = Math.sqrt(u2.dotProduct(u2).evaluate(Initializer.COS));
        return (-Initializer.EP < d0 + d1 - d2 && d0 + d1 - d2 < Initializer.EP);
    }

    // check to see if the BasicPoint p is one of the ends
    // of this BasicEdge 
    public boolean hasVertex(BasicPoint p) {
        return (p.equals(ends.get(0)) || p.equals(ends.get(1)));
    }

} // end of class BasicEdge
