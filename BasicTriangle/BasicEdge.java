/**
*    This class implements an edge.
*/

import java.io.Serializable;
import java.lang.Math.*;

public final class BasicEdge implements AbstractEdge<BasicAngle, BytePoint, BasicEdgeLength, BasicEdge>, Serializable {

    // make it Serializable
    static final long serialVersionUID = -6778708319703245773L;

    // the unit edge length
    public static final BasicEdgeLength UNIT_LENGTH = BasicEdgeLength.createBasicEdgeLength(0);

    // a threshold value indicating that a point is too close to an edge
    public static final double TOO_CLOSE;

    // Member variables. 
    private final BasicEdgeLength length;

    private final Orientation orientation;

    private final BytePoint[] ends;

    static { // initialize TOO_CLOSE
        double smallest = 1.0;
        for (BasicPrototile p : BasicPrototile.ALL_PROTOTILES) {
            BasicTriangle t = p.place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false);
            BasicEdge[] edges = t.getEdges();
            BytePoint[] vertices = t.getVertices();
            for (int i = 0; i < 3; i++) {
                double d = Math.abs(edges[i].cross(vertices[i]));
                if (d < smallest) smallest = d;
            }
        }
        TOO_CLOSE = 0.95*smallest;
    }

    // Constructor methods.  
    private BasicEdge(BasicEdgeLength length, Orientation orientation, BytePoint[] ends) {
        if (ends.length != 2)
            throw new IllegalArgumentException("A BasicEdge must be initialized with two BytePoints.");
        if (ends[0].equals(ends[1]))
            throw new IllegalArgumentException("A BasicEdge must be initialized with two different BytePoints.");
        this.length = length;
        this.orientation = orientation;
        this.ends = new BytePoint[] { ends[0], ends[1] };
    }

    // public factory method.
    static public BasicEdge createBasicEdge(BasicEdgeLength length, Orientation orientation, BytePoint[] ends) {
        return new BasicEdge(length, orientation, ends);
    }

    public BasicEdgeLength getLength() {
        return length;
    }

    public BytePoint[] getEnds() {
        return new BytePoint[] {ends[0],ends[1]};
    }

    public BasicEdge transform(BasicAngle a, BytePoint v) {
        BytePoint[] newEnds = { BytePoint.createBytePoint(ends[0],false,a,v), BytePoint.createBytePoint(ends[1],false,a,v) };
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
        BytePoint u0 = this.ends[0];
        BytePoint u1 = this.ends[1];
        BytePoint v0 = e.ends[1];
        BytePoint v1 = e.ends[1];

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
        BytePoint p0 = this.ends[0];
        BytePoint p1 = this.ends[1];
        BytePoint q0 = e.ends[0];
        BytePoint q1 = e.ends[1];
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
        BytePoint u0 = this.ends[0];
        BytePoint u1 = this.ends[1];
        BytePoint v0 = e.ends[0];
        BytePoint v1 = e.ends[1];

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
        BytePoint u0 = this.ends[0];
        BytePoint u1 = this.ends[1];
        BytePoint v0 = e.ends[0];
        BytePoint v1 = e.ends[1];

        if (u0.equals(v0)) {
            return u1.equals(v1);
        } else if (u0.equals(v1)) {
            return u1.equals(v0);
        }
        return false;
    }

    // Check if two edges have any end points in common
    public boolean commonEnd(BasicEdge e) {
//        BytePoint u0 = this.ends[0];
//        BytePoint u1 = this.ends[1];
//        BytePoint v0 = e.ends[0];
//        BytePoint v1 = e.ends[1];
        return (ends[0].equals(e.ends[0])||ends[0].equals(e.ends[1])||ends[1].equals(e.ends[0])||ends[1].equals(e.ends[1]));
    }

    // return the angle that this edge makes with the positive x-axis
    public BasicAngle angle() {
        BytePoint direction = this.ends[1].subtract(this.ends[0]);
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
        BytePoint u0 = this.ends[0];
        BytePoint u1 = this.ends[1];
        BytePoint v0 = e.ends[0];
        BytePoint v1 = e.ends[1];
        BytePoint m0 = u1.subtract(u0); // the direction vector for this edge
        BytePoint m1 = v1.subtract(v0); // the direction vector for e
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
        
//        BytePoint u0 = this.ends[0];
//        BytePoint u1 = this.ends[1];
//        BytePoint v0 = e.ends[0];
//        BytePoint v1 = e.ends[1];

        BytePoint m0 = ends[1].subtract(ends[0]); // the direction vector for this edge
        BytePoint m1 = e.ends[1].subtract(e.ends[0]); // the direction vector for e
        
        return (Math.signum((ends[0].subtract(e.ends[0])).crossProduct(m1)) != Math.signum((ends[1].subtract(e.ends[0])).crossProduct(m1)) && Math.signum((e.ends[1].subtract(ends[0])).crossProduct(m0)) != Math.signum((e.ends[0].subtract(ends[0]).crossProduct(m0))));
    }


    // return the same edge, with end points listed
    // in reverse order and the opposite Orientation
    public BasicEdge reverse() {
        return new BasicEdge(length, orientation.getOpposite(), new BytePoint[] {ends[1], ends[0]});
    }

    // if two edges are congruent, extract the Orientations
    // that get identified by placing them incident to one 
    // another.
    // No sanity check! We assume that they're congruent.
    protected Orientation[] getMatches(BasicEdge e) {
        BytePoint u0 = this.ends[0];
        BytePoint v0 = e.ends[0];
        if (u0.equals(v0)) {
            return new Orientation[] {this.orientation,e.orientation};
        } else {
            return new Orientation[] {this.orientation,e.orientation.getOpposite()};
        }
    }

    // check to see if the BytePoint p touches this edge
    // (not necessarily at the ends)
    public boolean incident(BytePoint p) {
        BytePoint u0 = this.ends[0].subtract(p);
        BytePoint u1 = p.subtract(this.ends[1]);
        BytePoint u2 = this.ends[0].subtract(this.ends[1]);
//        double d0 = Math.sqrt(u0.dotProduct(u0).evaluate(Initializer.COS));
//        double d1 = Math.sqrt(u1.dotProduct(u1).evaluate(Initializer.COS));
//        double d2 = Math.sqrt(u2.dotProduct(u2).evaluate(Initializer.COS));
        float d0 = (float) Math.sqrt(u0.dotProduct(u0));
        float d1 = (float) Math.sqrt(u1.dotProduct(u1));
        float d2 = (float) Math.sqrt(u2.dotProduct(u2));
        return (-Initializer.EP < d0 + d1 - d2 && d0 + d1 - d2 < Initializer.EP);
    }

    // check to see if the BytePoint p is too close to this edge
    public boolean tooClose(BytePoint p) {
        BytePoint u = UNIT_LENGTH.getAsVector(angle());
        BytePoint v = p.subtract(ends[0]);
        double d = v.testCross(u);
        if (-TOO_CLOSE < d && d < TOO_CLOSE) {
            if (Math.signum(v.dotProduct(u)) != Math.signum(p.subtract(ends[1]).dotProduct(u))) return true;
        }
        return false;
    }

    // check to see if the BytePoint p is too close to this edge
    public double cross(BytePoint p) {
        BytePoint u = UNIT_LENGTH.getAsVector(angle());
        BytePoint v = p.subtract(ends[0]);
        double d = v.crossProduct(u);
        return d;
    }

    // check to see if the BytePoint p is one of the ends
    // of this BasicEdge 
    public boolean hasVertex(BytePoint p) {
        return (p.equals(ends[0]) || p.equals(ends[1]));
    }

    public static void main(String[] args) {
        BasicEdgeLength l0 = BasicEdgeLength.createBasicEdgeLength(0);
        BasicEdgeLength l1 = BasicEdgeLength.createBasicEdgeLength(1);
        BasicEdgeLength l2 = BasicEdgeLength.createBasicEdgeLength(2);
        BasicEdgeLength l3 = BasicEdgeLength.createBasicEdgeLength(3);
        BasicEdgeLength l4 = BasicEdgeLength.createBasicEdgeLength(4);

        BytePoint testPoint = l2.getAsVector(BasicAngle.createBasicAngle(1)).subtract(l1.getAsVector(BasicAngle.createBasicAngle(1)));


        BytePoint p1 = BytePoint.createBytePoint(new byte[] { //
                                (byte)  5, //
                                (byte)  0, //
                                (byte)  5, //
                                (byte) -1, //
                                (byte)  4, //
                                (byte) -3, //
                                (byte)  3, //
                                (byte) -4, //
                                (byte)  1, //
                                (byte) -5});//

        BytePoint p2 = BytePoint.createBytePoint(new byte[] { //
                                (byte)  5, //
                                (byte)  0, //
                                (byte)  5, //
                                (byte)  0, //
                                (byte)  4, //
                                (byte) -2, //
                                (byte)  3, //
                                (byte) -3, //
                                (byte)  1, //
                                (byte) -4});//

        BytePoint q  = BytePoint.createBytePoint(new byte[] { //
                                (byte)  4, //
                                (byte)  0, //
                                (byte)  5, //
                                (byte)  0, //
                                (byte)  4, //
                                (byte) -2, //
                                (byte)  2, //
                                (byte) -3, //
                                (byte)  0, //
                                (byte) -4});//

        System.out.println(p1.subtract(p2).crossProduct(q.subtract(p2)));


        BytePoint aPoint = UNIT_LENGTH.getAsVector(BasicAngle.createBasicAngle(6));
        System.out.println(aPoint.crossProduct(q.subtract(p2)));
        System.out.println(aPoint.crossProduct(q.subtract(p1)));

//        for (int i = 0; i < 22; i++) {
            for (int j = 0; j < 22; j++) {
                System.out.println("testPoint cross l0 "   + j + " " + testPoint.crossProduct(l0.getAsVector(BasicAngle.createBasicAngle(j))));
            }
//        }

//        for (int i = 0; i < 22; i++) {
            for (int j = 0; j < 22; j++) {
                System.out.println("testPoint testCross l0 "   + j + " " + testPoint.testCross(l0.getAsVector(BasicAngle.createBasicAngle(j))));
            }
//        }

    }

} // end of class BasicEdge
