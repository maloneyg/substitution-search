/**
*    This class implements a collection of triangles.
*/

import com.google.common.collect.*;
import java.io.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Date;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.math3.linear.*;
import java.util.Collections;
import java.util.concurrent.atomic.*;

public class EmptyBoundaryPatch implements Serializable {

    // make it Serializable
    static final long serialVersionUID = 4422733298735932933L;

    // a Date for serializing at regular intervals. delete.
    private Date lastUpdateTime = null;
    private static final long SERIALIZATION_INTERVAL = Preinitializer.SERIALIZATION_INTERVAL;
    private static final boolean SERIALIZATION_FLAG = Preinitializer.SERIALIZATION_FLAG;
    private static final String SERIALIZATION_DIRECTORY = Preinitializer.SERIALIZATION_DIRECTORY;
    private final String resultFilename;
    private boolean serialized = false;

    // the number of completed patches this has found
    private int numCompleted = 0;

    // a String for debugging purposes
    private String message = DebugMessage.NONE.toString();

    // set to true if we're debugging
    private boolean debug = false;

    // angle of pi/N.  use for comparison in valid(). 
    private static BasicAngle ONE = BasicAngle.createBasicAngle(1);
    // short edge length. use for comparison in valid().
    private static BasicEdgeLength SHORT = BasicEdgeLength.lengthOpposite(ONE);

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

    // kill signal. set to true if we want to spawn new patches
    private AtomicBoolean die;

    // a list of descendents. populate this list with spawn() calls
    private List<EmptyBoundaryPatch> spawnList;

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
    private final BasicPrototile initialPrototile;

    // initially, we are not on the second edge
    // in an isosceles triangle
    private final boolean initialSecondEdge;

    // initially we are not trying to place a reflected tile
    private final boolean initialFlip;

    // which triangles did we have in the beginning?
    // if we started from scratch we had none, but 
    // if we spawned then we probably had some.
    private List<BasicTriangle> initialTriangles;

    // initial constructor
    private EmptyBoundaryPatch(BasicEdge e, BytePoint[] v, MutablePrototileList TL) {

        // turn off the kill switch
        die = new AtomicBoolean();

        // make an empty list of descendents
        spawnList = new ArrayList<>();

        // set the initial step variables
        initialPrototile = BasicPrototile.getFirstTile();
        initialSecondEdge = false;
        initialFlip = false;
        initialTriangles = new ArrayList<>();

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

        // serialization stuff. delete.
        int hashcode = hashCode();
        if ( hashcode >= 0 )
            resultFilename = String.format("%s/P%010d.chk",SERIALIZATION_DIRECTORY,hashcode);
        else
            resultFilename = String.format("%s/N%010d.chk",SERIALIZATION_DIRECTORY,-1*hashcode);
    }

    // spawn constructor
    private EmptyBoundaryPatch(BasicPrototile initialPrototile, boolean initialSecondEdge, boolean initialFlip, List<BasicTriangle> initialTriangles, Stack<BasicTriangle> triangles, PuzzleBoundary boundary, Stack<BytePoint> vertices, EmptyBoundaryEdgeList edges, MutableOrientationPartition partition, MutablePrototileList tileList) {

        // turn off the kill switch
        die = new AtomicBoolean();

        // make an empty list of descendents
        spawnList = new ArrayList<>();

        // set the initial step variables
        this.initialPrototile = initialPrototile;
        this.initialSecondEdge = initialSecondEdge;
        this.initialFlip = initialFlip;
        this.initialTriangles = initialTriangles;

        // set the state variables
        this.triangles = triangles;
        this.boundary = boundary;
        this.vertices = vertices;
        this.edges = edges;
        this.partition = partition;
        this.tileList = tileList;

        // set the step variables
        resetSteps();

        // serialization stuff. delete.
        int hashcode = hashCode();
        if ( hashcode >= 0 )
            resultFilename = String.format("%s/P%010d.chk",SERIALIZATION_DIRECTORY,hashcode);
        else
            resultFilename = String.format("%s/N%010d.chk",SERIALIZATION_DIRECTORY,-1*hashcode);
    }

    // public static factory method, single edge
    public static EmptyBoundaryPatch createEmptyBoundaryPatch(BasicEdge e, BytePoint[] v, MutablePrototileList TL) {
        return new EmptyBoundaryPatch(e,v,TL);
    }

    // spawn a new patch to pick things up from here
    private EmptyBoundaryPatch spawn()
    {
        List<BasicTriangle> newInitialTriangles = new ArrayList<>();
        Stack<BasicTriangle> newTriangles = new Stack<>();
        for (int i = 0; i < triangles.size(); i++) {
            newTriangles.push(triangles.get(i));
            newInitialTriangles.add(triangles.get(i));
        }
        Stack<BytePoint> newVertices = new Stack<>();
        for (int i = 0; i < vertices.size(); i++) {
            newVertices.push(vertices.get(i));
        }
        return new EmptyBoundaryPatch(currentPrototile, secondEdge, flip, newInitialTriangles, newTriangles, boundary.deepCopy(), newVertices, edges.deepCopy(), partition.deepCopy(), tileList.deepCopy());
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

    // flip the kill switch
    public void kill() {
        die.lazySet(true);
    }

    // make the kill switch point to something else
    public void setKillSwitch(AtomicBoolean die) {
        this.die = die;
    }

    // get the descendents
    public List<EmptyBoundaryPatch> getSpawnList() {
        return spawnList;
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

    // return true if triangles and initialTriangles
    // contain the same triangles in the same order
    private boolean compareTriangles() {
        if (initialTriangles.size()!=triangles.size()) return false;
        for (int i = 0; i < triangles.size(); i++)
            if (!initialTriangles.get(i).equals(triangles.get(i))) return false;
        return true;
    }

    // return true if the search is done
    public boolean allDone() {
        return (backToStart()&&compareTriangles());
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
    public List<EmptyBoundaryPatch> solve() {

        // costly serialization. delete this.
        if ( SERIALIZATION_FLAG == true )
            {
                if (lastUpdateTime == null)
                    lastUpdateTime = new Date();
                else if (serialized == false && (new java.util.Date()).getTime()-lastUpdateTime.getTime() > SERIALIZATION_INTERVAL)
                    {
                        try
                            {
                                FileOutputStream fileOut = new FileOutputStream(resultFilename);
                                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                                out.writeObject(this);
                                out.close();
                                fileOut.close();
                                System.out.println("\nwrote work unit to " + resultFilename + ".");
                            }
                        catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        lastUpdateTime = new java.util.Date();
                        serialized = true;
                    }
            }
        // here ends costly serialization. 


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
                            if (die.get()) {
                                spawnList.add(spawn());
                            } else {
                                solve();
                            }
                        }
                    removeTriangle();
                }
            }

            step();
        } while (!backToStart()); // stop when we've tried all prototiles
        return spawnList;

    } // solve ends here

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
//                if (debug) setMessage(DebugMessage.FOUND.toString());
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
//                            if (debug) setMessage(t+"\n"+DebugMessage.ORIENTATION.toString()+"\n"+partition);
                        }
                    removeTriangle();
                }
            } else {
//                if (debug) setMessage(currentPrototile+"\n"+DebugMessage.NONE_OR_PROTOTILE.toString()+"\n"+currentEdge);
            }

            step();
        } while (!backToStart()); // stop when we've tried all prototiles
    } // solve ends here

    public void debugSolve(ArrayList<PatchDisplay.DebugFrame> frames)
    {
        debug = true;
        do
            {
                if ( tileList.empty() )
                    {
                        ImmutablePatch thisPatch = dumpImmutablePatch();
                        completedPatches.add(thisPatch);
                        localCompletedPatches.add(thisPatch);
                        numCompleted++;
                        //if ( debug )
                        //   setMessage(DebugMessage.FOUND.toString());
                        break;
                    }
                if (    tileList.contains(currentPrototile)
                     && currentPrototile.compatible(currentEdge,secondEdge,flip,partition.equivalenceClass( currentEdge.getOrientation() ) ) )
                     {
                        BasicTriangle t = currentPrototile.place(currentEdge,secondEdge,flip);
                        if (compatible(t))
                            {
                                placeTriangle(t);
                                if (partition.valid())
                                    {
                                        //if (debug)
                                        //setMessage(DebugMessage.PLACING.toString()+"\n"+t);
                                        debugSolve(frames);
                                        count.getAndIncrement();
                                    }
                                else
                                    {
                                        // if (debug)
                                        //setMessage(t+"\n"+DebugMessage.ORIENTATION.toString()+"\n"+partition);
                                    }
                                removeTriangle();
                            }
                    }
                else
                    {
                        //if (debug)
                        //setMessage(currentPrototile+"\n"+DebugMessage.NONE_OR_PROTOTILE.toString()+"\n"+currentEdge);
                    }
                step();
                
                PatchDisplay.DebugFrame currentFrame = new PatchDisplay.DebugFrame(dumpImmutablePatch(),message.toString());
                frames.add(currentFrame);
                System.out.print("\rComputing frames..." + frames.size() + " frames computed...");
            } 
        while ( frames.size() <= PatchDisplay.DebugPanel.MAX_FRAMES && backToStart() == false);
    }

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

        if (newVertex) { // big if statement

            // make sure the new vertex is in the inflated prototile
            if (boundary.overTheEdge(other)) {
                if (debug) setMessage(t +"\n"+ DebugMessage.NON_CONTAINMENT.toString());
                return false;
            }

            // make sure the new vertex doesn't overlap any open edges
//            for (BasicEdge e : edges.open()) {
//                if (e.incident(other)) {
//                    if (debug) setMessage(t+"\n"+DebugMessage.INCIDENT_OPEN.toString());
//                    return false;
//                }
//            }

            // make sure the new vertex doesn't overlap any closed edges
            for (BasicEdge e : edges.closed()) {
                if (e.incident(other)) {
                    if (debug) setMessage(t +"\n"+ DebugMessage.INCIDENT_CLOSED.toString());
                    return false;
                }
            }

            // make sure the new vertex isn't inside any placed triangles
            for (BasicTriangle tr : triangles) {
                if (tr.contains(other)) {
                    if (debug) setMessage(t +"\n"+ DebugMessage.OVERLAP.toString() +"\n"+ tr);
                    return false;
                }
            }

            // return false if the new vertex is too close to any open edge
            for (BasicEdge open : edges.open()) {
                if (open.tooClose(other)) {
                    if (debug) setMessage(open.cross(other)+ " hit");
                    return false;
                }
            }

        } // end if(newVertex)

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
            if (boundaryIncidence==-1) {
                if (debug) setMessage(e +"\n"+ DebugMessage.BOUNDARY_PROBLEM.toString() + "\n" + boundary);
                return false;
            }
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

        if (newVertex) { // start second-last edge check

            /*
            * This is a little complicated.
            * If other is a new vertex, then there are two new edges.
            * The one further clockwise is the next that we will try
            * to cover.  What about the one further counterclockwise?
            * We need to make sure it meets the other edge beside it
            * in such a way that does not automatically preclude 
            * completion of the puzzle. The other edge beside it is
            * edges.getPenultimateEdge().
            * So we first pick these two edges and call them c1, c2.
            * 
            * Funny thing: we need to use cw() to find the edge
            * further counterclockwise, because cw() and ccw()
            * were designed with reversed edges in mind.
            */
            BasicEdge c1 = edges.getPenultimateEdge();
            BasicEdge c2 = BasicEdge.cw(newEdges[0],newEdges[1]);

            /*
            * Now there's an additional problem.  
            * getPenultimateEdge() could return null.  This means
            * that the ccw new edge is incident with the puzzle 
            * boundary.  So we'll have to treat that case separately,
            * using the puzzle boundary data, in particular, angles.
            */
            BasicAngle wedge = c2.angle().minus(((c1==null)? boundary.incidenceAngle(c2) : c1.angle()));//.piPlus());

            // if the new edge makes an angle of ONE with the 
            // placed edges or puzzle boundary, there might
            // be trouble.
            if (wedge.equals(ONE)) {
                if (c1==null) {
                    if (!BasicPrototile.encloseAngleOne(c2)) {
                        if (debug) setMessage("*****\n HIT " + wedge + "\n*****");
                        //System.out.println("HIT: wedge " + wedge);
                        return false;
                    } 
                } else {
                    if (!BasicPrototile.encloseAngleOne(c1,c2)) {
                        if (debug) setMessage("*****\n HIT " + wedge + "\n*****");
                        //System.out.println("HIT: wedge " + wedge);
                        return false;
                    } 
                }
            } // end if (wedge==ONE)

            // now check to see if either edge has length 1
            if (c1 == null) {
                if (SHORT.equals(c2.getLength())&&!BasicPrototile.mightTouchLengthOne(wedge)) {
                    if (debug) setMessage("*****\n HIT " + wedge + "\nSHORT EDGE\n*****");
                    //System.out.println("HIT: short side");
                    return false;
                }
            } else { // c1 isn't null
                if (SHORT.equals(c2.getLength())&&!BasicPrototile.mightTouchLengthOne(c1.getLength(), wedge)) {
                    if (debug) setMessage("*****\n HIT " + wedge + "\nSHORT EDGE\n*****");
                    //System.out.println("HIT: short side");
                    return false;
                } else if (SHORT.equals(c1.getLength())&&!BasicPrototile.mightTouchLengthOne(c2.getLength(), wedge)) {
                    if (debug) setMessage("*****\n HIT " + wedge + "\nSHORT EDGE\n*****");
                    //System.out.println("HIT: short side");
                    return false;
                }

            }

        } // end second-last edge check

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
