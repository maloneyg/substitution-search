/**
*    This class implements a prototile.
*    It is not located in space.  
*/

import com.google.common.collect.*;
import java.io.Serializable;
import java.util.ArrayList;

public class BasicPrototile implements AbstractPrototile<BasicAngle, BasicPoint, BasicEdgeLength, BasicEdge, BasicTriangle>, Serializable {

    // make it Serializable
    static final long serialVersionUID = 3481614476338573017L;

    private final ImmutableList<BasicAngle> angles;

    private final ImmutableList<BasicEdgeLength> lengths;

    private final ImmutableList<Orientation> orientations;

    public static final ImmutableList<BasicPrototile> ALL_PROTOTILES;

    // private constructor
    private BasicPrototile(ImmutableList<Integer> anglesList) {
        BasicAngle[] tempAngles = new BasicAngle[] { //
                             BasicAngle.createBasicAngle(anglesList.get(0)), //
                             BasicAngle.createBasicAngle(anglesList.get(1)), //
                             BasicAngle.createBasicAngle(anglesList.get(2))  //
                                                   };  
        angles = ImmutableList.copyOf(tempAngles);
        BasicEdgeLength[] tempLengths = new BasicEdgeLength[] { //
                             BasicEdgeLength.lengthOpposite(tempAngles[0]), //
                             BasicEdgeLength.lengthOpposite(tempAngles[1]), //
                             BasicEdgeLength.lengthOpposite(tempAngles[2])  //
                                                              };  
        lengths = ImmutableList.copyOf(tempLengths);
        Orientation[] tempOrientations = new Orientation[] { //
                             Orientation.createOrientation(), //
                             Orientation.createOrientation(), //
                             Orientation.createOrientation()  //
                                                              };  
        orientations = ImmutableList.copyOf(tempOrientations);
    }

    static { // initialize ALL_PROTOTILES

        BasicPrototile[] tempAllPrototiles = new BasicPrototile[Initializer.PROTOTILES.size()];
        for (int i = 0; i < Initializer.PROTOTILES.size(); i++)
            tempAllPrototiles[i] = new BasicPrototile(Initializer.PROTOTILES.get(i));
        ALL_PROTOTILES = ImmutableList.copyOf(tempAllPrototiles);

    }

    // public static factory method
    public static BasicPrototile createBasicPrototile(int[] a) {
        if (a.length != 3)
            throw new IllegalArgumentException("A prototile needs 3 angles.");
        if (a[0]+a[1]+a[2] != Initializer.N)
            throw new IllegalArgumentException("Wrong angle sum for a prototile: "+a[0]+" + "+a[1]+" + "+a[2]+".");
        BasicAngle a0 = BasicAngle.createBasicAngle(a[0]);
        BasicAngle a1 = BasicAngle.createBasicAngle(a[1]);
        BasicAngle a2 = BasicAngle.createBasicAngle(a[2]);
        for (BasicPrototile p : ALL_PROTOTILES) {
            if (p.angles.contains(a0)&&p.angles.contains(a1)&&p.angles.contains(a2))
                return p;
        }
        throw new IllegalArgumentException("We aren't using the prototile (" + a0+","+a1+","+a2+")");
    }

    // get all Orientations.  We need this to initialize a BasicPatch
    protected ImmutableList<Orientation> getOrientations() {
        return orientations;
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicPrototile p = (BasicPrototile) obj;
        return this.angles.equals(p.angles);
    }

    // hashCode method
    public int hashCode() {
        return angles.hashCode();
    }

    // toString method
    public String toString() {
        return "Prototile\n        angles: (" + angles.get(0) + "," + angles.get(1) + "," + angles.get(2) + ")\n  edge lengths: " + lengths.get(0) + "\n                " +  lengths.get(1) + "\n                " +  lengths.get(2);
    }

    /*
    * return all the lengths
    */
    public ImmutableList<BasicEdgeLength> getLengths() {
        return lengths;
    }

    /*
    * return true if this is isosceles
    */
    public boolean isosceles() {
        BasicAngle a0 = angles.get(0);
        BasicAngle a1 = angles.get(1);
        BasicAngle a2 = angles.get(2);
        return (a0.equals(a1) || a1.equals(a2) || a2.equals(a0));
    }

    /*
    * return a list of BasicPrototiles.
    * BasicPrototile p at position i in ALL_PROTOTILES 
    * should appear counts.get(i) times in this list.
    */
    public static ImmutableList<BasicPrototile> getPrototileList(ImmutableList<Integer> counts) {
        if (counts.size() != ALL_PROTOTILES.size()) throw new IllegalArgumentException("There are " + ALL_PROTOTILES.size() + " prototiles, but we're trying to initialize a list with " + counts.size() + " of them.");
        ArrayList<BasicPrototile> output = new ArrayList<>(counts.get(0));
        for (int i = 0; i < counts.size(); i++) {
            for (int j = 0; j < counts.get(i); j++) output.add(ALL_PROTOTILES.get(i));
        }
        return ImmutableList.copyOf(output);
    }

    /*
    * return true if this has an edge with length l
    */
    public boolean compatible(BasicEdgeLength l) {
        return lengths.contains(l);
    }

    /*
    * return all possible ways of placing this prototile
    * against the BasicEdge e, subject to the constraint 
    * that the Orientation of the edge that is placed
    * incident to e not be opposite to any of the 
    * Orientations in equivalenceClass.
    *
    * We've already tested to see if e.length is in 
    * this.lengths, so this method should return a
    * non-empty list (i.e., it is possible to place
    * an instance of p against e somehow).
    */
    public ImmutableList<BasicTriangle> placements(BasicEdge e, ImmutableSet<Orientation> equivalenceClass) {
        ArrayList<BasicTriangle> output = new ArrayList<>(0);
        BasicEdgeLength l = e.getLength();
        ImmutableList<BasicPoint> ends = e.getEnds();
        BasicPoint e0 = ends.get(0);
        BasicPoint e1 = ends.get(1).subtract(e0);
        BasicPoint shift;
        BasicAngle turn;
        int intTurn = 0;
        // set intTurn equal to the angle between e1 and the positive x-axis
        for (int i = 0; i < 2*BasicAngle.ANGLE_SUM; i++) {
            if (l.getAsVector(BasicAngle.createBasicAngle(i)).equals(e1)) {
                intTurn = i;
                break;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (l.equals(lengths.get(i))&&!equivalenceClass.contains(orientations.get(i).getOpposite())) {
                if (i == 0) {
                    shift = e0;
                    turn = BasicAngle.createBasicAngle(intTurn);
                } else if (i == 1) {
                    turn = BasicAngle.createBasicAngle(intTurn-angles.get(2).supplement().getAsInt());
                    shift = e0.subtract(lengths.get(0).getAsVector(turn));
                } else {
                    turn = BasicAngle.createBasicAngle(intTurn-angles.get(1).piPlus().getAsInt());
                    shift = e0.subtract(lengths.get(2).getAsVector(BasicAngle.createBasicAngle(turn.getAsInt()+angles.get(1).getAsInt())));
                }
                output.add(place(shift,turn,false));
            }
            if (l.equals(lengths.get(i))&&!equivalenceClass.contains(orientations.get(i))) {
                if (i == 0) {
                    turn = BasicAngle.createBasicAngle(intTurn);
                    shift = e0.add(lengths.get(0).getAsVector(turn));
                } else if (i == 1) {
                    turn = BasicAngle.createBasicAngle(intTurn-angles.get(2).piPlus().getAsInt());
                    shift = e0.subtract(lengths.get(2).getAsVector(BasicAngle.createBasicAngle(turn.getAsInt()+angles.get(1).supplement().getAsInt())));
                } else {
                    turn = BasicAngle.createBasicAngle(intTurn-angles.get(1).supplement().getAsInt());
                    shift = e0;
                }
                output.add(place(shift,turn,true));
            }
        }
        return ImmutableList.copyOf(output);
    }

    /*
    * Place the prototile in space.
    * p says where to place the root vertex.
    * a says how to orient it.
    * flip says whether or not to reflect it.
    */
    public BasicTriangle place(BasicPoint p, BasicAngle a, boolean flip) {
        BasicPoint p1 = BasicPoint.ZERO_VECTOR;
        BasicPoint p2 = lengths.get(0).getAsVector(BasicAngle.createBasicAngle(0));
        BasicPoint p0 = lengths.get(2).getAsVector(angles.get(1));
        BasicPoint[] vertices = new BasicPoint[] { p0, p1, p2 };
        BasicAngle[] newAngles = new BasicAngle[] { angles.get(0), angles.get(1), angles.get(2) };
        Orientation[] newOrientations = new Orientation[] { orientations.get(0), orientations.get(1), orientations.get(2) };
        BasicEdgeLength[] newLengths = new BasicEdgeLength[] { lengths.get(0), lengths.get(1), lengths.get(2) };
        if (flip) {
            for (int i = 0; i < 3; i++)
                vertices[i] = vertices[i].reflect();
            /* 
            * Now flip the first and last of everything
            * to put things in ccw order.
            */
            BasicPoint tempVertex = vertices[2];
            vertices[2] = vertices[0];
            vertices[0] = tempVertex;
            BasicAngle tempAngle = newAngles[2];
            newAngles[2] = newAngles[0];
            newAngles[0] = tempAngle;
            Orientation tempOrientation = newOrientations[2];
            newOrientations[2] = newOrientations[0].getOpposite();
            newOrientations[0] = tempOrientation.getOpposite();
            newOrientations[1] = newOrientations[1].getOpposite();
            BasicEdgeLength tempLength = newLengths[2];
            newLengths[2] = newLengths[0];
            newLengths[0] = tempLength;
        }
        for (int j = 0; j < 3; j++)
            vertices[j] = vertices[j].rotate(a).add(p);
        return BasicTriangle.createBasicTriangle(newAngles, vertices, newOrientations, newLengths);
    }

    // create an outline of the inflated prototile.  
    // the Integer lists describe the edge breakdowns.  
    // no sanity check!
    // we assume that these are breakdowns of the actual
    // edge of this prototile.
    public ImmutableList<BasicEdge> createSkeleton(ImmutableList<Integer> b1, ImmutableList<Integer> b2, ImmutableList<Integer> b3) {
        BasicEdge[] output = new BasicEdge[b1.size()+b2.size()+b3.size()];
        int k = 0;
        BasicAngle a1 = angles.get(1).piPlus();
        BasicAngle a2 = angles.get(2).supplement();
        BasicEdgeLength currentLength;
        BasicPoint currentPoint = lengths.get(2).getAsVector(angles.get(1)).inflate();
        BasicPoint nextPoint;

        // run through the edge breakdowns, adding edges to the skeleton.
        // this is going to get repetitive. 
        // the only thing that changes from one breakdown to the next
        // is the angle of rotation.
        for (Integer i : b3) {
            currentLength = BasicEdgeLength.createBasicEdgeLength(i);
            nextPoint = currentLength.getAsVector(a1).add(currentPoint);
            output[k] = BasicEdge.createBasicEdge(currentLength, Orientation.createOrientation(), new BasicPoint[] { currentPoint, nextPoint });
            k++;
            currentPoint = nextPoint;
        }

        for (Integer i : b1) {
            currentLength = BasicEdgeLength.createBasicEdgeLength(i);
            nextPoint = currentLength.getAsVector(BasicAngle.createBasicAngle(0)).add(currentPoint);
            output[k] = BasicEdge.createBasicEdge(currentLength, Orientation.createOrientation(), new BasicPoint[] { currentPoint, nextPoint });
            k++;
            currentPoint = nextPoint;
        }

        for (Integer i : b2) {
            currentLength = BasicEdgeLength.createBasicEdgeLength(i);
            nextPoint = currentLength.getAsVector(a2).add(currentPoint);
            output[k] = BasicEdge.createBasicEdge(currentLength, Orientation.createOrientation(), new BasicPoint[] { currentPoint, nextPoint });
            k++;
            currentPoint = nextPoint;
        }

        return ImmutableList.copyOf(output);
    }

    public static void main(String[] args) {

        BasicPrototile P0 = createBasicPrototile(new int[] { 1, 3, 3 });
        System.out.println(P0);

        BasicPrototile P1 = createBasicPrototile(new int[] { 1, 2, 4 });
        System.out.println(P1);

        BasicPrototile P2 = createBasicPrototile(new int[] { 2, 2, 3 });
        System.out.println(P2);

        BasicTriangle T0 = P2.place(BasicPoint.createBasicPoint(new int[] {1,0,1,0,1,0}),BasicAngle.createBasicAngle(3),false);
        System.out.println(T0);

        BasicTriangle T1 = P2.place(BasicPoint.createBasicPoint(new int[] {0,0,0,0,0,0}),BasicAngle.createBasicAngle(0),false);
        System.out.println(T1);

        System.out.println("Testing skeleton output.");
        ImmutableList<BasicEdge> edgeList = P1.createSkeleton(//
                                P1.lengths.get(0).getBreakdown(), //
                                P1.lengths.get(1).getBreakdown(), //
                                P1.lengths.get(2).getBreakdown()  //
                                                );
        for (BasicEdge e : edgeList) System.out.println(e);

        BasicPoint p = BasicEdgeLength.createBasicEdgeLength(0).getAsVector(BasicAngle.createBasicAngle(0));
        BasicEdge[] ee = T1.getEdges();
        for (BasicEdge e : ee) {
            System.out.print(p + " on " + e + ": " + e.incident(p) + "\n");
        }

    }

} // end of class BasicPrototile
