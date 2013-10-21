/**
*    This class implements an edge.
*/

import java.io.Serializable;

public final class BasicEdge implements AbstractEdge<BasicAngle, BasicEdgeLength, BasicEdge>, Serializable {

    // make it Serializable
    static final long serialVersionUID = -6778708319703245773L;

    // Member variables. 
    private final BasicEdgeLength length;

    private final Orientation orientation;

    private final AbstractPoint[] ends;

    // Constructor methods.  
    private BasicEdge(BasicEdgeLength length, Orientation orientation, AbstractPoint[] ends) {
        if (ends.length != 2)
            throw new IllegalArgumentException("A BasicEdge must be initialized with two BytePoints.");
        if (ends[0].equals(ends[1]))
            throw new IllegalArgumentException("A BasicEdge must be initialized with two different BytePoints.");
        this.length = length;
        this.orientation = orientation;
        this.ends = new AbstractPoint[] { ends[0], ends[1] };
    }

    // public factory method.
    static public BasicEdge createBasicEdge(BasicEdgeLength length, Orientation orientation, AbstractPoint[] ends) {
        return new BasicEdge(length, orientation, ends);
    }

    public BasicEdgeLength getLength() {
        return length;
    }

    public AbstractPoint[] getEnds() {
        return new AbstractPoint[] {ends[0],ends[1]};
    }

    public BasicEdge transform(BasicAngle a, AbstractPoint v) {
        AbstractPoint[] newEnds = { Preinitializer.createPoint(ends[0],false,a,v), Preinitializer.createPoint(ends[1],false,a,v) };
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
        return "Edge\n  " + ends[0] + "\n  " + ends[1] + " " + orientation;
    }

    /* 
    * Given an edge with a (possibly different) orientation,
    * get the orientation of this edge, using the direction
    * convention established by the other edge.  
    */ 
    public Orientation getOrientation(BasicEdge e) {
        if (!(this.length.equals(e.length)))
            throw new IllegalArgumentException("You need to match edges of the same length.");
        AbstractPoint u0 = this.ends[0];
        AbstractPoint u1 = this.ends[1];
        AbstractPoint v0 = e.ends[1];
        AbstractPoint v1 = e.ends[1];

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
        AbstractPoint p0 = this.ends[0];
        AbstractPoint p1 = this.ends[1];
        AbstractPoint q0 = e.ends[0];
        AbstractPoint q1 = e.ends[1];
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
        int c0 = ends[0].hashCode();
        int c1 = ends[1].hashCode();
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
        AbstractPoint u0 = this.ends[0];
        AbstractPoint u1 = this.ends[1];
        AbstractPoint v0 = e.ends[0];
        AbstractPoint v1 = e.ends[1];

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
    // This method is now the same as equals, except without a cast
    public boolean congruent(BasicEdge e) {
        if (!(this.length.equals(e.length)))
            return false;
        AbstractPoint u0 = this.ends[0];
        AbstractPoint u1 = this.ends[1];
        AbstractPoint v0 = e.ends[0];
        AbstractPoint v1 = e.ends[1];

        if (u0.equals(v0)) {
            return u1.equals(v1);
        } else if (u0.equals(v1)) {
            return u1.equals(v0);
        }
        return false;
    }

    // Check if two edges have any end points in common
    public boolean commonEnd(BasicEdge e) {
//        AbstractPoint u0 = this.ends[0];
//        AbstractPoint u1 = this.ends[1];
//        AbstractPoint v0 = e.ends[0];
//        AbstractPoint v1 = e.ends[1];
        return (ends[0].equals(e.ends[0])||ends[0].equals(e.ends[1])||ends[1].equals(e.ends[0])||ends[1].equals(e.ends[1]));
    }

    // return the angle that this edge makes with the positive x-axis
    public BasicAngle angle() {
        AbstractPoint direction = this.ends[1].subtract(this.ends[0]);
        BasicAngle output = BasicAngle.createBasicAngle(0);
        for (int i = 0; i < 2*BasicAngle.ANGLE_SUM; i++) {
            output = BasicAngle.createBasicAngle(i);
            if (length.getAsVector(output).equals(direction)) break;
        }
        return output;
    }

/*    // return true if these edges cross, and false otherwise.
    // in particular, return false if they share a common
    // end point or if they have the same slope.
    public boolean cross(BasicEdge e) {
        if (commonEnd(e)) return false;
        BasicAngle a0 = this.angle();
        BasicAngle a1 = e.angle();
        if (a0.equals(a1)||a0.equals(a1.piPlus())) return false;
        AbstractPoint u0 = this.ends[0];
        AbstractPoint u1 = this.ends[1];
        AbstractPoint v0 = e.ends[0];
        AbstractPoint v1 = e.ends[1];
        AbstractPoint m0 = u1.subtract(u0); // the direction vector for this edge
        AbstractPoint m1 = v1.subtract(v0); // the direction vector for e
//        return (Math.signum((u0.subtract(v0)).crossProduct(m1).evaluate(Initializer.COS)) != Math.signum((u1.subtract(v0)).crossProduct(m1).evaluate(Initializer.COS)) && Math.signum((v1.subtract(u0)).crossProduct(m0).evaluate(Initializer.COS)) != Math.signum((v0.subtract(u0).crossProduct(m0).evaluate(Initializer.COS))));
        return (Math.signum((u0.subtract(v0)).crossProduct(m1)) != Math.signum((u1.subtract(v0)).crossProduct(m1)) && Math.signum((v1.subtract(u0)).crossProduct(m0)) != Math.signum((v0.subtract(u0).crossProduct(m0))));
    }
*/
    // end point or if they have the same slope.
    public boolean cross(BasicEdge e) {
        if (commonEnd(e)) return false;
        BasicAngle a0 = this.angle();
        BasicAngle a1 = e.angle();
        if ( a0.equals(a1) || a0.equals(a1.piPlus()))
            return false;
        
//        AbstractPoint u0 = this.ends[0];
//        AbstractPoint u1 = this.ends[1];
//        AbstractPoint v0 = e.ends[0];
//        AbstractPoint v1 = e.ends[1];

        AbstractPoint m0 = ends[1].subtract(ends[0]); // the direction vector for this edge
        AbstractPoint m1 = e.ends[1].subtract(e.ends[0]); // the direction vector for e
        
        return (Math.signum((ends[0].subtract(e.ends[0])).crossProduct(m1)) != Math.signum((ends[1].subtract(e.ends[0])).crossProduct(m1)) && Math.signum((e.ends[1].subtract(ends[0])).crossProduct(m0)) != Math.signum((e.ends[0].subtract(ends[0]).crossProduct(m0))));
    }


    // return the same edge, with end points listed
    // in reverse order and the opposite Orientation
    public BasicEdge reverse() {
        return new BasicEdge(length, orientation.getOpposite(), new AbstractPoint[] {ends[1], ends[0]});
    }

    // if two edges are congruent, extract the Orientations
    // that get identified by placing them incident to one 
    // another.
    // No sanity check! We assume that they're congruent.
    protected Orientation[] getMatches(BasicEdge e) {
        AbstractPoint u0 = this.ends[0];
        AbstractPoint v0 = e.ends[0];
        if (u0.equals(v0)) {
            return new Orientation[] {this.orientation,e.orientation};
        } else {
            return new Orientation[] {this.orientation,e.orientation.getOpposite()};
        }
    }

    // check to see if the AbstractPoint p touches this edge
    // (not necessarily at the ends)
    public boolean incident(AbstractPoint p) {
        AbstractPoint u0 = this.ends[0].subtract(p);
        AbstractPoint u1 = p.subtract(this.ends[1]);
        AbstractPoint u2 = this.ends[0].subtract(this.ends[1]);
//        double d0 = Math.sqrt(u0.dotProduct(u0).evaluate(Initializer.COS));
//        double d1 = Math.sqrt(u1.dotProduct(u1).evaluate(Initializer.COS));
//        double d2 = Math.sqrt(u2.dotProduct(u2).evaluate(Initializer.COS));
        float d0 = (float)Math.sqrt(u0.dotProduct(u0));
        float d1 = (float)Math.sqrt(u1.dotProduct(u1));
        float d2 = (float)Math.sqrt(u2.dotProduct(u2));
        return (-Initializer.EP < d0 + d1 - d2 && d0 + d1 - d2 < Initializer.EP);
    }

    // check to see if the AbstractPoint p is one of the ends
    // of this BasicEdge 
    public boolean hasVertex(AbstractPoint p) {
        return (p.equals(ends[0]) || p.equals(ends[1]));
    }

} // end of class BasicEdge
