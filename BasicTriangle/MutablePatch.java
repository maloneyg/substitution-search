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

    /*
    * The state variables.
    * These variables store the current state of the puzzle--
    * what it looks like, what has been placed so far, what
    * still needs to be placed, etc. 
    * They don't contain direct information about what we're
    * trying to place now, what we placed before, or what we'll
    * place next, although some of this can be inferred from 
    * the state.
    */

    // the triangles in this patch
    private Stack<BasicTriangle> triangles;

    // the edges in this patch
    private MutableEdgeList edges;

    // the Orientations that have been identified with one another
    private MutableOrientationPartition partition;

    // prototiles available for placement
    private ArrayList<BasicPrototile> tileList;

    // vertices of the big triangle
    private final BytePoint[] bigVertices;

    /*
    * The step variables.
    * They tell us what we've tried most recently and what
    * we're going to try next.  They are reset whenever we
    * place or remove a triangle.  
    */

    // which edge are we currently trying to cover?
    private BasicEdge currentEdge;

    // which prototile are we currently trying to place?
    private BasicPrototile currentPrototile;

    // currentPrototile might have two edges of the same 
    // length as current Edge; are we on the second one?
    private boolean secondEdge;

    // are we trying to place a reflected version of currentPrototile?
    private boolean flip;

    /*
    * The intial state of the step variables.
    * They tell us when we've tried every possibility
    * for a given edge, and have to give up and remove
    * the most recent triangle.  
    */

    // the first prototile
    private BasicPrototile initialPrototile = BasicPrototile.getFirstTile();

    // initially, we are not on the second edge
    // in an isosceles triangle
    private boolean initialSecondEdge = false;

    // initially we are not trying to place a reflected tile
    private boolean initialFlip = false;

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

    // advance the step variables by one step
    private void step() {
        flip = !flip;
        if (flip == initialFlip) {
            secondEdge = !secondEdge;
            if (!currentPrototile.hasTwo(currentEdge.getLength())) secondEdge = initialSecondEdge;
            if (secondEdge == initialSecondEdge) {
                currentPrototile = currentPrototile.getNextTile();
            }
        }
    }

    // return true if the step variables are at the start, else false
    private boolean backToStart() {
        return (flip == initialFlip && secondEdge == initialSecondEdge && currentPrototile.equals(initialPrototile));
    }

    // reset the step variables to their initial states
    // use this when you've just placed a triangle
    private void resetSteps() {
        flip = initialFlip;
        secondEdge = initialSecondEdge;
        currentPrototile = initialPrototile;
        currentEdge = edges.getNextEdge();
    }

    // here is where all of the work is done.
    // place a single tile, then call this method recursively.
    public void solve() {
        do {
            if (tileList.contains(currentPrototile) && currentPrototile.compatible(currentEdge,secondEdge,flip,partition.equivalenceClass(currentEdge.getOrientation()))) {
                BasicTriangle t = currentPrototile.place(currentEdge,secondEdge,flip);
                if (compatible(t)) {
                    placeTriangle(t);
                    if (partition.valid()) solve(); // the recursive call
                    removeTriangle();
                }
            }

            step();
        } while (!backToStart()); // stop when we've tried all prototiles

        // if there are triangles in the list, pop the
        // last one and restore the previous state and steps
        //if (!triangles.empty()) {
        // do stuff
        //}

    } // solve ends here

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
    * output for drawing the result
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
            p0 = e.getEnds()[0];
            p1 = e.getEnds()[1];
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

    /*
    * update this patch by adding triangle t to it.
    * This is what we do after compatible(t) returns true.
    */
    private void placeTriangle(BasicTriangle t) {
        tileList.remove(currentPrototile);
        edges.place(t,partition);
        resetSteps();
        currentEdge = edges.getNextEdge();
        triangles.push(t);
    } // placeTriangle(t) ends here

    /*
    * remove the most recent triangle from this patch.
    * this is what we do after running through solve()
    * from beginning to end.
    */
    private void removeTriangle() {
        BasicTriangle t = triangles.pop();
        edges.remove(t,triangles,partition);
        currentEdge = edges.getNextEdge();
        currentPrototile = t.getPrototile();
        flip = t.getFlip();
        tileList.add(currentPrototile);
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
        BytePoint[] ends = getNextEdge().getEnds();
        BytePoint other = t.getOtherVertex(ends[0],ends[1]);

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
            if (Math.signum((v.subtract(t)).crossProduct(m)) != Math.signum((p.subtract(t)).crossProduct(m)))
//            if (Math.signum((v.subtract(t)).crossProduct(m).evaluate(Initializer.COS)) != Math.signum((p.subtract(t)).crossProduct(m).evaluate(Initializer.COS)))
                return false;
        }
        return true;
    }

} // end of class MutablePatch
