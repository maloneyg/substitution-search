/**
*    This class implements a collection of triangles.
*/

import com.google.common.collect.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.linear.*;
import java.util.Collections;
import java.util.concurrent.atomic.*;

public class EmptyBoundaryPatch implements Serializable {

    // make it Serializable
//    static final long serialVersionUID = 3422733298735932933L;

    // the number of completed patches this has found
    private int numCompleted = 0;

    // a String for debugging purposes
    private String message = DebugMessage.NONE.toString();

    // set to true if we're debugging
    private boolean debug = false;

    // the completed patches that have been found
    private static List<ImmutablePatch> completedPatches;

    // a list of completed patches for this particular puzzle
    private List<ImmutablePatch> localCompletedPatches = new ArrayList<ImmutablePatch>();

    private AtomicInteger count = new AtomicInteger(0);

    static { // initialize completedPatches
        ArrayList<ImmutablePatch> tempList = new ArrayList<>();
        completedPatches = Collections.synchronizedList(tempList);
    }

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

    // the boundary of this patch
    private PuzzleBoundary boundary;

    // the vertices in this patch, purely for testing triangles
    private Stack<BytePoint> vertices;

    // the edges in this patch
    private EmptyBoundaryEdgeList edges;

    // the Orientations that have been identified with one another
    private MutableOrientationPartition partition;

    // prototiles available for placement
    private MutablePrototileList tileList;

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
    private final BasicPrototile initialPrototile = BasicPrototile.getFirstTile();

    // initially, we are not on the second edge
    // in an isosceles triangle
    private final boolean initialSecondEdge = false;

    // initially we are not trying to place a reflected tile
    private final boolean initialFlip = false;

    // initial constructor
    private EmptyBoundaryPatch(BasicEdge e, BytePoint[] v, MutablePrototileList TL) {
        tileList = TL;
        triangles = new Stack<>();
        edges = EmptyBoundaryEdgeList.createEmptyBoundaryEdgeList(e);
        vertices = new Stack<>();
        vertices.push(e.getEnds()[0]);
        vertices.push(e.getEnds()[1]);
        boundary = PuzzleBoundary.createPuzzleBoundary(e);

        // fill up the partition
        partition = MutableOrientationPartition.createMutableOrientationPartition(e.getOrientation());
        partition.add(e.getOrientation().getOpposite());
        for (BasicPrototile t : BasicPrototile.ALL_PROTOTILES) {
            for (Orientation a : t.getOrientations()) {
                if (!partition.contains(a)) partition.add(a);
                if (!partition.contains(a.getOpposite())) partition.add(a.getOpposite());
            }
        }

        resetSteps();
    }

    // public static factory method, single edge
    public static EmptyBoundaryPatch createEmptyBoundaryPatch(BasicEdge e, BytePoint[] v, MutablePrototileList TL) {
        return new EmptyBoundaryPatch(e,v,TL);
    }

    // get all the completed patches
    public static List<ImmutablePatch> getCompletedPatches() {
        return completedPatches;
    }

    // set the message
    public void setMessage(String s) {
        message = s;
    }

    // get the message
    public String getMessage() {
        return message;
    }

    // toggle the debug status
    public void setDebug(boolean tf) {
        debug = tf;
    }

    // get all the patches for this puzzle
    public List<ImmutablePatch> getLocalCompletedPatches()
    {
        return localCompletedPatches;
    }

    // dump the contents of this as a ImmutablePatch
    public ImmutablePatch dumpImmutablePatch() {
        BasicTriangle[] t = new BasicTriangle[triangles.size()];
        for (int i = 0; i < t.length; i++) t[i] = triangles.get(i);
        BasicEdge[] e1 = new BasicEdge[edges.openSize()];
        int j = 0;
        for (BasicEdge e : edges.open()) {
            e1[j] = e;
            j++;
        }
        BasicEdge[] e2 = new BasicEdge[edges.closedSize()];
        j = 0;
        for (BasicEdge e : edges.closed()) {
            e2[j] = e;
            j++;
        }
        OrientationPartition o = partition.dumpOrientationPartition();
        return ImmutablePatch.createImmutablePatch(t,e1,e2,o,boundary.getVertices(),boundary.getBreakdown(0),boundary.getBreakdown(1),boundary.getBreakdown(2));
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
    public boolean backToStart() {
        return (flip == initialFlip && secondEdge == initialSecondEdge && currentPrototile.equals(initialPrototile));
    }

    // return true if the search is done
    public boolean allDone() {
        return (backToStart()&&triangles.empty());
    }

    // reset the step variables to their initial states
    // use this when you've just placed a triangle
    private void resetSteps() {
        flip = initialFlip;
        secondEdge = initialSecondEdge;
        currentPrototile = initialPrototile;
        currentEdge = edges.getNextEdge();
    }

    // identify two Orientations
    public void identify(Orientation one, Orientation two) {
        partition.identify(one,two);
    }

    // add instructions to identify two Orientations at all times in the future
    public void addInstructions(Orientation one, Orientation two) {
        partition.addInstructions(one,two);
    }

    public void setCount(AtomicInteger count)
    {
        this.count = count;
    }

    // here is where all of the work is done.
    // place a single tile, then call this method recursively.
    public void solve() {
        count.getAndIncrement();
        do {
            if (tileList.empty()) {
                ImmutablePatch thisPatch = dumpImmutablePatch();
                completedPatches.add(thisPatch);
                localCompletedPatches.add(thisPatch);
                numCompleted++;
                break;
            }
            if (tileList.contains(currentPrototile) && currentPrototile.compatible(currentEdge,secondEdge,flip,partition.equivalenceClass(currentEdge.getOrientation()))) {
                BasicTriangle t = currentPrototile.place(currentEdge,secondEdge,flip);
                if (compatible(t)) {
                    placeTriangle(t);
                    if (partition.valid())
                        {
                            solve();
                        }
                    removeTriangle();
                }
            }

            step();
        } while (!backToStart()); // stop when we've tried all prototiles

    } // solve ends here

    // a solve method that stops for debugging purposes.
//    public ImmutablePatch debugSolve() {
//
//        ImmutablePatch output = dumpImmutablePatch();
//        if (tileList.empty()) {
//            removeTriangle();
//            step();
//        }
//
//        if (tileList.contains(currentPrototile) && currentPrototile.compatible(currentEdge,secondEdge,flip,partition.equivalenceClass(currentEdge.getOrientation()))) {
//            BasicTriangle t = currentPrototile.place(currentEdge,secondEdge,flip);
//            if (compatible(t)) {
//                placeTriangle(t);
//                if (!partition.valid()) {
//                    removeTriangle();
//                }
//            }
//        }
//        step();
//        if (backToStart()&&!triangles.empty()) {
//            removeTriangle();
//            step();
//        }
//        return output;
//
//
//    } // debugSolve ends here


    // place a single tile, then call this method recursively.
    public void debugSolve(EmptyBoundaryDebugDisplay d) {
        do {
            d.updateMessage(message);
            d.update(dumpImmutablePatch());

            if (tileList.empty()) {
                ImmutablePatch thisPatch = dumpImmutablePatch();
                completedPatches.add(thisPatch);
                localCompletedPatches.add(thisPatch);
                numCompleted++;
                if (debug) setMessage(DebugMessage.FOUND.toString());
                break;
            }
            if (tileList.contains(currentPrototile) && currentPrototile.compatible(currentEdge,secondEdge,flip,partition.equivalenceClass(currentEdge.getOrientation()))) {
                BasicTriangle t = currentPrototile.place(currentEdge,secondEdge,flip);
                if (compatible(t)) {
                    placeTriangle(t);
                    if (partition.valid())
                        {
//                            if (debug) setMessage(DebugMessage.PLACING.toString()+"\n"+t);
                            debugSolve(d);
                            count.getAndIncrement();
                        } else
                        {
                            if (debug) setMessage(t+"\n"+DebugMessage.ORIENTATION.toString()+"\n"+partition);
                        }
                    removeTriangle();
                }
            } else {
                if (debug) setMessage(currentPrototile+"\n"+DebugMessage.NONE_OR_PROTOTILE.toString()+"\n"+currentEdge);
            }

            step();
        } while (!backToStart()); // stop when we've tried all prototiles
    } // solve ends here

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        EmptyBoundaryPatch x = (EmptyBoundaryPatch) obj;

        return (
          this.triangles.equals(x.triangles)
          &&this.edges.equals(x.edges)
          &&this.partition.equals(x.partition)
          &&this.numCompleted==x.numCompleted
          &&this.tileList.equals(x.tileList)
          &&this.currentEdge.equals(x.currentEdge)
          &&this.currentPrototile.equals(x.currentPrototile)
          &&this.secondEdge==x.secondEdge
          &&this.flip==x.flip
          &&this.initialPrototile.equals(x.initialPrototile)
          &&this.initialSecondEdge==x.initialSecondEdge
          &&this.initialFlip==x.initialFlip
            );
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

    // return the number of completed puzzles
    public int getNumCompleted() {
        return numCompleted;
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
        BytePoint[] ends = currentEdge.getEnds();
        BytePoint other = t.getOtherVertex(ends[0],ends[1]);
        if (!vertices.contains(other)) vertices.push(other);
        tileList.remove(currentPrototile);
        edges.place(t,partition,boundary);
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
        edges.remove(t,triangles,partition,boundary);
        currentEdge = edges.getNextEdge();
        currentPrototile = t.getPrototile();
        flip = t.getFlip();
        tileList.add(currentPrototile);
        secondEdge = t.isSecondEdge(currentEdge);
        BytePoint p = vertices.pop();
        for (BasicEdge e : edges.open()) {
            if (e.hasVertex(p)) {
                vertices.push(p);
                return;
            }
        }
        for (BasicEdge e : edges.closed()) {
            if (e.hasVertex(p)) {
                vertices.push(p);
                return;
            }
        }
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
    * print the current step variables
    */
    public String printStep() {
        return currentPrototile + "\n" + secondEdge + "\n" + flip + "\n";
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
        BytePoint[] ends = currentEdge.getEnds();
        BytePoint other = t.getOtherVertex(ends[0],ends[1]);

        boolean newVertex = !(vertices.contains(other)||boundary.incident(other)==1);

        // make sure the new vertex is in the inflated prototile
        if (newVertex && boundary.overTheEdge(other)) {
            if (debug) setMessage(t +"\n"+ DebugMessage.NON_CONTAINMENT.toString());
            return false;
        }

        // make sure the new vertex doesn't overlap any open edges
        if (newVertex) {
            for (BasicEdge e : edges.open()) {
                if (e.incident(other)) {
                    if (debug) setMessage(t+"\n"+DebugMessage.INCIDENT_OPEN.toString());
                    return false;
                }
            }
        }

        // make sure the new vertex doesn't overlap any closed edges
        if (newVertex) {
            for (BasicEdge e : edges.closed()) {
                if (e.incident(other)) {
                    if (debug) setMessage(t +"\n"+ DebugMessage.INCIDENT_CLOSED.toString());
                    return false;
                }
            }
        }

        // make sure the new vertex isn't inside any placed triangles
        if (newVertex) {
            for (BasicTriangle tr : triangles) {
                if (tr.contains(other)) {
                    if (debug) setMessage(t +"\n"+ DebugMessage.OVERLAP.toString() +"\n"+ tr);
                    return false;
                }
            }
        }

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
            // check how the new edge is incident with the boundary
            //  0 means not at all
            //  1 means incident, without overlapping anything
            // -1 means incident and overlapping something
            int boundaryIncidence = boundary.incident(e);
            if (boundaryIncidence==-1) return false;
            if (boundaryIncidence==1) break;
            for (BasicEdge open : edges.open()) {
                if (e.cross(open)) {
                    if (debug) setMessage(e +"\n"+ DebugMessage.CROSS_OPEN.toString() +"\n"+ open);
                    return false;
                }
            }
            for (BasicEdge closed : edges.closed()) {
                if (e.cross(closed)) {
                    if (debug) setMessage(e +"\n"+ DebugMessage.CROSS_CLOSED.toString() +"\n"+ closed);
                    return false;
                }
            }
        }

//        // return false if the new vertex is too close to any existing vertex
//        if (newVertex) {
//            for (BytePoint p : vertices) {
//                if (other.tooClose(p)) {
//                    if (debug) setMessage(other +"\n"+ DebugMessage.TOO_CLOSE.toString() +"\n"+ p);
//                    return false;
//                }
//            }
//        }

        // return false if the new vertex is too close to any open edge
        if (newVertex) {
            for (BasicEdge open : edges.open()) {
                if (open.tooClose(other)) {
                    if (debug) setMessage(open.cross(other)+ " hit");
                    return false;
                }
            }
        }

        return true;
    }

    // temporary--destroy this method when you don't need it anymore!
    public void edgeDump() {
        for (BasicEdge e: edges.open()) System.out.println(""+e);
    }

    // big String
    public String toString() {
        String output = "==EmptyBoundaryPatch==\n";
        
        output = output + "Triangles:\n";
        for (BasicTriangle t : triangles)
            output += t.toString() + "\n";
        
        output += "Open edges:\n";
        for (BasicEdge e : edges.open())
            output += e.toString() + "\n";
        
        output += "Closed edges:\n";
        for (BasicEdge e : edges.closed())
            output += e.toString() + "\n";
        
        output += "Partition:\n";
        output += partition.toString() + "\n";

        output += "Number Completed:" + numCompleted + "\n";
        output += "Count:" + count.get() + "\n";
        output += "Tile List:\n";
        output += tileList.toString() + "\n";
        output += "currentEdge: " + currentEdge.toString() + "\n";
        output += "currentPrototile: " + currentPrototile.toString() + "\n";
        output += "initialPrototile: " + initialPrototile.toString() + "\n";
        output += "secondEdge: " + secondEdge + "\n";
        output += "initialSecondEdge: " + initialSecondEdge + "\n";
        output += "flip: " + flip + "\n";
        output += "initialFlip: " + initialFlip + "\n";
        output += "end of EmptyBoundaryPatch\n";
        return output;
    }

} // end of class EmptyBoundaryPatch
