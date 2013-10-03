/**
*    This class implements a collection of triangles.
*/

import com.google.common.collect.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import org.apache.commons.math3.linear.*;

public class BasicPatch implements AbstractPatch<BasicAngle, BytePoint, BasicEdgeLength, BasicEdge, BasicTriangle, BasicPatch>, Serializable {

    // make it Serializable
    static final long serialVersionUID = 3422733298735932933L;

    // the triangles in this patch
    private final BasicTriangle[] triangles;

    // the open edges in this patch
    private final BasicEdge[] openEdges;

    // the closed edges in this patch
    private final BasicEdge[] closedEdges;

    // the Orientations that have been identified with one another
    private final OrientationPartition partition;

    // vertices of the big triangle
    private final BytePoint[] bigVertices;

    // private constructor
    private BasicPatch(BasicTriangle[] t, BasicEdge[] e1, BasicEdge[] e2, OrientationPartition o, BytePoint[] v) {
        triangles = t;
        openEdges = e1;
        closedEdges = e2;
        partition = o;
        bigVertices = v;
    }

    // initial constructor
    private BasicPatch(BasicEdge[] e, BytePoint[] v) {
        Orientation[] o = new Orientation[e.length + 6 * BasicPrototile.ALL_PROTOTILES.size()];
        int i = 0;
        for (i = 0; i < e.length; i++) o[i] = e[i].getOrientation();
        for (BasicPrototile t : BasicPrototile.ALL_PROTOTILES) {
            for (Orientation a : t.getOrientations()) {
                o[i] = a;
                i++;
                o[i] = a.getOpposite();
                i++;
            }
        }
        triangles = new BasicTriangle[0];
        BasicEdge[] tempEdges = new BasicEdge[e.length];
        for (int j = 0; j < e.length; j++) tempEdges[j] = e[j];
        openEdges = tempEdges;
        closedEdges = new BasicEdge[0];
        partition = OrientationPartition.createOrientationPartition(o);
        BytePoint[] tempVertices = new BytePoint[v.length];
        for (int j = 0; j < v.length; j++) tempVertices[j] = v[j];
        bigVertices = tempVertices;
    }

    // public static factory method
    public static BasicPatch createBasicPatch(BasicEdge[] e, BytePoint[] v) {
        return new BasicPatch(e,v);
    }

    // return partition (for testing only)
    public OrientationPartition getPartition() {
        return partition;
    }

    // return openEdges (for testing only)
    public BasicEdge[] getOpenEdges() {
        BasicEdge[] output = new BasicEdge[openEdges.length];
        for (int i = 0; i < openEdges.length; i++) output[i] = openEdges[i];
        return output;
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicPatch x = (BasicPatch) obj;
        return (this.triangles.equals(x.triangles)&&this.openEdges.equals(x.openEdges)&&this.partition.equals(x.partition));
    }

    // hashCode method
    public int hashCode() {
        int prime = 5;
        int result = 23;
        result = prime*result + triangles.hashCode();
        result = prime*result + openEdges.hashCode();
        result = prime*result + partition.hashCode();
        return result;
    }

    /*
    * The big test.
    */
    public ArrayList<OrderedTriple> graphicsDump() {
        ArrayList<OrderedTriple> output = new ArrayList<OrderedTriple>(triangles.length);
        BytePoint p0;
        BytePoint p1;
        ArrayList<RealMatrix> edgeList = new ArrayList<RealMatrix>(3);
        int counter = 0;
        for (BasicTriangle t : triangles)
            output.add(new OrderedTriple(t.toArray()));
        for (BasicEdge e : openEdges) {
            p0 = e.getEnds().get(0);
            p1 = e.getEnds().get(1);
            counter++;
            if (counter == 1) {
                edgeList.add((RealMatrix)new Array2DRowRealMatrix(p0.arrayToDraw()));
                edgeList.add((RealMatrix)new Array2DRowRealMatrix(p1.arrayToDraw()));
                edgeList.add((RealMatrix)new Array2DRowRealMatrix(p0.arrayToDraw()));
            } else {
                edgeList.set(0,(RealMatrix)new Array2DRowRealMatrix(p0.arrayToDraw()));
                edgeList.set(1,(RealMatrix)new Array2DRowRealMatrix(p1.arrayToDraw()));
                edgeList.set(2,(RealMatrix)new Array2DRowRealMatrix(p0.arrayToDraw()));
            }
            output.add(new OrderedTriple(new ArrayList<RealMatrix>(edgeList)));
        }
        return output;
    }

    // return the new OrientationPartition obtained by
    // identifying the Orientations of edges of t with 
    // the Orientations of the openEdges with which they
    // are incident.
    private OrientationPartition newOrientationPartition(BasicTriangle t) {
        OrientationPartition output = partition;
        for (BasicEdge e1 : openEdges) {
            for (BasicEdge e2 : t.getEdges()) {
                if (e1.congruent(e2)) {
                    ImmutableList<Orientation> matches = e1.getMatches(e2);
                    output = output.identify(matches.get(0),matches.get(1));
                }
            }
        }
        return output;
    }

    /*
    * construct a new patch that is the same as this one, with 
    * Orientations o1 and o1 identified.
    *
    */
    public BasicPatch identify(Orientation o1, Orientation o2) {
        return new BasicPatch(triangles,openEdges,closedEdges,partition.identify(o1,o2),bigVertices);
    }

    /*
    * construct a new patch that is the same as this one, with 
    * triangle t added to it.
    * This is what we do after compatible(t) returns true.
    *
    */
    public BasicPatch placeTriangle(BasicTriangle t) {
        // fill up the new triangle list
        BasicTriangle[] newTriangles = new BasicTriangle[triangles.length+1];
        int i = 0;
        for (i = 0; i < triangles.length; i++)
            newTriangles[i] = triangles[i];
        newTriangles[triangles.length] = t;

        BasicEdge[] matches = t.getEdges();
        /*
        * return an error message if any of the edges in t is
        * already in closedEdges.
        */
        for (i = 0; i < 3; i++) {
            if (Arrays.asList(closedEdges).contains(matches[i]))
                throw new IllegalArgumentException("Trying to add " + matches[i] + ", which is already closed in this patch.");
        }

        /*
        * find the indices of the edges of t in openEdges.
        */
        List<BasicEdge> tempEdges = Arrays.asList(openEdges);
        int[] edgeIndexList = { //
                                  tempEdges.indexOf(matches[0]), // 
                                  tempEdges.indexOf(matches[1]), // 
                                  tempEdges.indexOf(matches[2])  // 
                              };
        tempEdges = null;

        // find the indices of the edges of t in openEdges.
        int totalMatches = 0;
        // count how many edges of t are in openEdges.
        for (i = 0; i < 3; i++) {
            if (edgeIndexList[i] != -1)
                totalMatches++;
        }

        // fill up the new openEdge list
        BasicEdge[] newOpenEdges = new BasicEdge[openEdges.length+3-2*totalMatches];
        int j = 0;
        // first put in all the old open edges that haven't been covered
        for (i = 0; i < openEdges.length; i++) {
            if (!(i==edgeIndexList[0]||i==edgeIndexList[1]||i==edgeIndexList[2])) {
                newOpenEdges[j] = openEdges[i];
                j++;
            }
        }
        // now put in all the new edges that don't match any old edges
        for (i = 0; i < 3; i++) {
            if (edgeIndexList[i]==-1) {
                newOpenEdges[j] = matches[i].reverse();
                j++;
            }
        }

        // fill up the new closedEdgeList
        BasicEdge[] newClosedEdges = new BasicEdge[closedEdges.length+totalMatches];
        // first put in all the old closed edges
        for (i = 0; i < closedEdges.length; i++) {
            newClosedEdges[i] = closedEdges[i];
        }
        // now put in all the new edges from t
        for (j = 0; j < 3; j++) {
            if (edgeIndexList[j]!=-1) {
                newClosedEdges[i] = matches[j];
                i++;
            }
        }
        return new BasicPatch( //
                                 newTriangles, //
                                 newOpenEdges, //
                                 newClosedEdges, //
                                 newOrientationPartition(t), //
                                 bigVertices //
                             );
    }

    // return the set of Orientations that have been declared
    // equivalent to o.
    public ImmutableSet<Orientation> getEquivalenceClass(Orientation o) {
        return partition.getEquivalenceClass(o);
    }

    /*
    * Get the next edge.
    * Presumably, the word `next' implies some kind of order,
    * probably from shortest to longest, although other ideas 
    * are conceivable.  We really want to return the edge 
    * that is most difficult to cover, because that will lead
    * to a quicker rejection of the configuration, if it is
    * already uncompletable.
    */
    public BasicEdge getNextEdge() {
        return openEdges[openEdges.length-1];
    }

    /*
    * The big test.
    * Presumably this is where all of the work will happen.
    * We check and see if the triangle t fits in this patch.
    * This will involve applying many different tests to it.
    * Presumably there will be many other private functions
    * called in the execution of this one.  
    */
    public boolean compatible(BasicTriangle t) {
        ImmutableList<BytePoint> ends = getNextEdge().getEnds();
        BytePoint other = t.getOtherVertex(ends.get(0),ends.get(1));

        // test to see if other is new or already there.
        // if it's on an openEdge but not equal to one 
        // of its endpoints, return false immediately.
        boolean newVertex = true;
        for (BasicEdge e : openEdges) {
            if (e.hasVertex(other)) {
                newVertex = false;
                break;
            } else if (e.incident(other)) {
                return false;
            }
        }

        // make sure the new vertex is in the inflated prototile
        if (newVertex && !contains(other)) return false;

        // make sure the new vertex doesn't overlap any closed edges
        if (newVertex) {
            for (BasicEdge e : closedEdges) {
                if (e.incident(other)) return false;
            }
        }

        // make sure the new vertex isn't inside any placed triangles
        if (newVertex) {
            for (BasicTriangle tr : triangles) {
                if (tr.contains(other)) return false;
            }
        }

        // make sure the orientations match
        if (!newOrientationPartition(t).valid()) return false;

        // newEdges are the edges containing other in t
        BasicEdge[] newEdges = new BasicEdge[2];
        int count = 0;
        for (BasicEdge e : t.getEdges()) {
            if (e.hasVertex(other)) {
                newEdges[count] = e;
                count++;
            }
        }

        // return false if a new edge crosses any old one
        for (BasicEdge e : newEdges) {
            for (BasicEdge open : openEdges) {
                if (e.cross(open)) return false;
            }
            for (BasicEdge closed : closedEdges) {
                if (e.cross(closed)) return false;
            }
        }

        return true;
    }

    /*
    * return true if p is inside the big triangle, false otherwise.
    * this involves taking three inner products with vectors orthogonal
    * to the three sides.  Taking an inner product with an orthogonal
    * vector is the same as taking the 2d cross product with the 
    * original vector.
    */
    public boolean contains(BytePoint p) {
        BytePoint m; // the direction vector for a side
        BytePoint v; // the other vertex
        BytePoint t; // vertex on the given side, used to test cross product
        for (int i = 0; i < 3; i++) {
            m = bigVertices[(i+2)%3].subtract(bigVertices[(i+1)%3]);
            v = bigVertices[i];
            t = bigVertices[(i+1)%3];
            if (Math.signum((v.subtract(t)).crossProduct(m).evaluate(Initializer.COS)) != Math.signum((p.subtract(t)).crossProduct(m).evaluate(Initializer.COS)))
                return false;
        }
        return true;
    }

} // end of class BasicPatch
