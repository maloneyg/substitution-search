/**
*    This class implements a collection of triangles.
*/

import com.google.common.collect.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Arrays;
import org.apache.commons.math3.linear.*;

public class MutablePatch {

    // make it Serializable
//    static final long serialVersionUID = 3422733298735932933L;

    // the triangles in this patch
    private Stack<BasicTriangle> triangles;

    // the edges in this patch
    private MutableEdgeList edges;

    // the Orientations that have been identified with one another
    private MutableOrientationPartition partition;

    // vertices of the big triangle
    private final BytePoint[] bigVertices;

    // initial constructor
    private MutablePatch(BasicEdge[] e, BytePoint[] v) {
        triangles = new Stack<>();
        edges = MutableEdgeList.createMutableEdgeList(e);

        // fill up the partition
        partition = MutableOrientationPartition.createMutableOrientationPartition(e[0].getOrientation());
        for (int i = 1; i < e.length; i++) {
            Orientation o = e[i].getOrientation();
            if (!partition.contains(o)) partition.add(o);
            if (!partition.contains(o.getOpposite())) partition.add(o.getOpposite());
        }
        for (BasicPrototile t : BasicPrototile.ALL_PROTOTILES) {
            for (Orientation a : t.getOrientations()) {
                if (!partition.contains(a)) partition.add(a);
                if (!partition.contains(a.getOpposite())) partition.add(a.getOpposite());
            }
        }

        BytePoint[] tempVertices = new BytePoint[v.length];
        for (int j = 0; j < v.length; j++) tempVertices[j] = v[j];
        bigVertices = tempVertices;
    }

    // public static factory method
    public static MutablePatch createMutablePatch(BasicEdge[] e, BytePoint[] v) {
        return new MutablePatch(e,v);
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        MutablePatch x = (MutablePatch) obj;
        return (this.triangles.equals(x.triangles)&&this.edges.equals(x.edges)&&this.partition.equals(x.partition));
    }

    // hashCode method
    public int hashCode() {
        int prime = 5;
        int result = 23;
        result = prime*result + triangles.hashCode();
        result = prime*result + edges.hashCode();
        result = prime*result + partition.hashCode();
        return result;
    }

    /*
    * The big test.
    */
    public ArrayList<OrderedTriple> graphicsDump() {
        ArrayList<OrderedTriple> output = new ArrayList<OrderedTriple>(triangles.size());
        BytePoint p0;
        BytePoint p1;
        ArrayList<RealMatrix> edgeList = new ArrayList<RealMatrix>(3);
        int counter = 0;
        for (BasicTriangle t : triangles)
            output.add(new OrderedTriple(t.toArray()));
        for (BasicEdge e : edges.open()) {
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

    // Identify the Orientations of edges of t with 
    // the Orientations of the openEdges with which they
    // are incident.
//    private void updateOrientations(BasicTriangle t) {
//        for (BasicEdge e1 : openEdges) {
//            for (BasicEdge e2 : t.getEdges()) {
//                if (e1.congruent(e2)) {
//                    ImmutableList<Orientation> matches = e1.getMatches(e2);
//                    output.identify(matches.get(0),matches.get(1));
//                }
//            }
//        }
//    }

    /*
    * update this patch by addign triangle t to it.
    * This is what we do after compatible(t) returns true.
    *
    */
//    public void placeTriangle(BasicTriangle t) {
//        triangles.push(t);
//        updateOrientations(t);
//
//        BasicEdge[] matches = t.getEdges();
//        /*
//        * return an error message if any of the edges in t is
//        * already in closedEdges.
//        */
//        for (i = 0; i < 3; i++) {
//            if (closedEdges.contains(matches[i]))
//                throw new IllegalArgumentException("Trying to add " + matches[i] + ", which is already closed in this patch.");
//        }
//
//        /*
//        * find the indices of the edges of t in openEdges.
//        */
//        List<BasicEdge> tempEdges = Arrays.asList(openEdges);
//        int[] edgeIndexList = { //
//                                  tempEdges.indexOf(matches[0]), // 
//                                  tempEdges.indexOf(matches[1]), // 
//                                  tempEdges.indexOf(matches[2])  // 
//                              };
//        tempEdges = null;
//
//        // find the indices of the edges of t in openEdges.
//        int totalMatches = 0;
//        // count how many edges of t are in openEdges.
//        for (i = 0; i < 3; i++) {
//            if (edgeIndexList[i] != -1)
//                totalMatches++;
//        }
//
//        // fill up the new openEdge list
//        BasicEdge[] newOpenEdges = new BasicEdge[openEdges.length+3-2*totalMatches];
//        int j = 0;
//        // first put in all the old open edges that haven't been covered
//        for (i = 0; i < openEdges.length; i++) {
//            if (!(i==edgeIndexList[0]||i==edgeIndexList[1]||i==edgeIndexList[2])) {
//                newOpenEdges[j] = openEdges[i];
//                j++;
//            }
//        }
//        // now put in all the new edges that don't match any old edges
//        for (i = 0; i < 3; i++) {
//            if (edgeIndexList[i]==-1) {
//                newOpenEdges[j] = matches[i].reverse();
//                j++;
//            }
//        }
//
//        // fill up the new closedEdgeList
 //       BasicEdge[] newClosedEdges = new BasicEdge[closedEdges.length+totalMatches];
//        // first put in all the old closed edges
//        for (i = 0; i < closedEdges.length; i++) {
//            newClosedEdges[i] = closedEdges[i];
//        }
//        // now put in all the new edges from t
//        for (j = 0; j < 3; j++) {
//            if (edgeIndexList[j]!=-1) {
//                newClosedEdges[i] = matches[j];
//                i++;
//            }
//        }
//        return new MutablePatch( //
//                                 newTriangles, //
 //                                newOpenEdges, //
//                                 newClosedEdges, //
//                                 newOrientationPartition(t), //
//                                 bigVertices //
//                             );
//    } // placeTriangle(t) ends here

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
        return edges.getNextEdge();
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
        for (BasicEdge e : edges.open()) {
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
            for (BasicEdge e : edges.closed()) {
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
        if (!partition.valid()) return false;

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
            for (BasicEdge open : edges.open()) {
                if (e.cross(open)) return false;
            }
            for (BasicEdge closed : edges.closed()) {
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

} // end of class MutablePatch
