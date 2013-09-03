/**
*    This class implements a collection of triangles.
*/

import com.google.common.collect.*;
import java.io.Serializable;
import java.util.ArrayList;
import org.apache.commons.math3.linear.*;

public class BasicPatch implements AbstractPatch<BasicAngle, BasicPoint, BasicEdgeLength, BasicEdge, BasicTriangle, BasicPatch>, Serializable {

    // make it Serializable
    static final long serialVersionUID = 3422733298735932933L;

    // the triangles in this patch
    private final ImmutableList<BasicTriangle> triangles;

    // the open edges in this patch
    private final ImmutableList<BasicEdge> openEdges;

    // the closed edges in this patch
    private final ImmutableList<BasicEdge> closedEdges;

    // the Orientations that have been identified with one another
    private final OrientationPartition partition;

    // private constructor
    private BasicPatch(BasicTriangle[] t, BasicEdge[] e1, BasicEdge[] e2, OrientationPartition o) {
        triangles = ImmutableList.copyOf(t);
        openEdges = ImmutableList.copyOf(e1);
        closedEdges = ImmutableList.copyOf(e2);
        partition = o;
    }

    // initial constructor
    private BasicPatch(BasicEdge[] e) {
        Orientation[] o = new Orientation[e.length + 3 * BasicPrototile.ALL_PROTOTILES.size()];
        int i = 0;
        for (i = 0; i < e.length; i++) o[i] = e[i].getOrientation();
        for (BasicPrototile t : BasicPrototile.ALL_PROTOTILES) {
            for (Orientation a : t.getOrientations()) {
                o[i] = a;
                i++;
            }
        }
        triangles = ImmutableList.copyOf(new BasicTriangle[0]);
        openEdges = ImmutableList.copyOf(e);
        closedEdges = ImmutableList.copyOf(new BasicEdge[0]);
        partition = OrientationPartition.createOrientationPartition(o);
    }

    // public static factory method
    public static BasicPatch createBasicPatch(BasicEdge[] e) {
        return new BasicPatch(e);
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
        ArrayList<OrderedTriple> output = new ArrayList<OrderedTriple>(triangles.size());
        BasicPoint p0;
        BasicPoint p1;
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
    * triangle t added to it.
    * This is what we do after compatible(t) returns true.
    *
    */
    public BasicPatch placeTriangle(BasicTriangle t) {
        // fill up the new triangle list
        BasicTriangle[] newTriangles = new BasicTriangle[triangles.size()+1];
        int i = 0;
        for (i = 0; i < triangles.size(); i++)
            newTriangles[i] = triangles.get(i);
        newTriangles[triangles.size()] = t;

        BasicEdge[] matches = t.getEdges();
        /*
        * return an error message if any of the edges in t is
        * already in closedEdges.
        */
        for (i = 0; i < 3; i++) {
            if (closedEdges.contains(matches[i]))
                throw new IllegalArgumentException("Trying to add " + matches[i] + ", which is already closed in this patch.");
        }

        /*
        * find the indices of the edges of t in openEdges.
        */
        int[] edgeIndexList = { //
                                  openEdges.indexOf(matches[0]), // 
                                  openEdges.indexOf(matches[1]), // 
                                  openEdges.indexOf(matches[2])  // 
                              };

        // find the indices of the edges of t in openEdges.
        int totalMatches = 0;
        // count how many edges of t are in openEdges.
        for (i = 0; i < 3; i++) {
            if (edgeIndexList[i] != -1)
                totalMatches++;
        }

        // fill up the new openEdge list
        BasicEdge[] newOpenEdges = new BasicEdge[openEdges.size()+3-2*totalMatches];
        int j = 0;
        // first put in all the old open edges that haven't been covered
        for (i = 0; i < openEdges.size(); i++) {
            if (!(i==edgeIndexList[0]||i==edgeIndexList[1]||i==edgeIndexList[2])) {
                newOpenEdges[j] = openEdges.get(i);
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
        BasicEdge[] newClosedEdges = new BasicEdge[closedEdges.size()+totalMatches];
        // first put in all the old closed edges
        for (i = 0; i < closedEdges.size(); i++) {
            newClosedEdges[i] = closedEdges.get(i);
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
                                 newOrientationPartition(t) //
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
        return openEdges.get(3);
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
        return true;
    }

} // end of class BasicPatch
