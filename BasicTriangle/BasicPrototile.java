/**
*    This class implements a prototile.
*    It is not located in space.  
*/

import com.google.common.collect.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicPrototile implements AbstractPrototile<BasicAngle, BytePoint, BasicEdgeLength, BasicEdge, BasicTriangle>, Serializable {

    // make it Serializable
    static final long serialVersionUID = 3481614476338573017L;

    private final BasicAngle[] angles;
    private final BasicAngle[] flipAngles;

    private final BasicEdgeLength[] lengths;
    private final BasicEdgeLength[] flipLengths;

    private final Orientation[] orientations;
    private final Orientation[] flipOrientations;

    public static final ImmutableList<BasicPrototile> ALL_PROTOTILES;
    public static final ImmutableList<BasicEdgeLength> EDGE_LENGTHS = BasicEdgeLength.ALL_EDGE_LENGTHS;

    // private constructor
    private BasicPrototile(ImmutableList<Integer> anglesList) {
        angles = new BasicAngle[] { //
                             BasicAngle.createBasicAngle(anglesList.get(0)), //
                             BasicAngle.createBasicAngle(anglesList.get(1)), //
                             BasicAngle.createBasicAngle(anglesList.get(2))  //
                                                   };  
        flipAngles = new BasicAngle[] { //
                             angles[2], //
                             angles[1], //
                             angles[0]  //
                                                   };  
        lengths = new BasicEdgeLength[] { //
                             BasicEdgeLength.lengthOpposite(angles[0]), //
                             BasicEdgeLength.lengthOpposite(angles[1]), //
                             BasicEdgeLength.lengthOpposite(angles[2])  //
                                                              };  
        flipLengths = new BasicEdgeLength[] { //
                             lengths[2], //
                             lengths[1], //
                             lengths[0]  //
                                                              };  
        orientations = new Orientation[] { //
                             Orientation.createOrientation(), //
                             Orientation.createOrientation(), //
                             Orientation.createOrientation()  //
                                                              };  
        flipOrientations = new Orientation[] { //
                             orientations[2].getOpposite(), //
                             orientations[1].getOpposite(), //
                             orientations[0].getOpposite()  //
                                                              };  
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
            List<BasicAngle> tempList = Arrays.asList(p.angles);
            if (tempList.contains(a0)&&tempList.contains(a1)&&tempList.contains(a2))
                return p;
        }
        throw new IllegalArgumentException("We aren't using the prototile (" + a0+","+a1+","+a2+")");
    }

    // public static factory method
    public static BasicPrototile createBasicPrototile(ImmutableList<Integer> a) {
        if (a.size() != 3)
            throw new IllegalArgumentException("A prototile needs 3 angles.");
        return createBasicPrototile(new int[] {a.get(0),a.get(1),a.get(2)});
    }

    // public static factory method to return the first tile
    public static BasicPrototile getFirstTile() {
        return ALL_PROTOTILES.get(0);
    }

    // return the prototile after this one in the list
    public BasicPrototile getNextTile() {
        return ALL_PROTOTILES.get((ALL_PROTOTILES.indexOf(this)+1)%ALL_PROTOTILES.size());
    }

    // get all Orientations.  We need this to initialize a BasicPatch
    // unsafe operation: passing a final field to the outside world
    protected Orientation[] getOrientations() {
        return orientations;
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicPrototile p = (BasicPrototile) obj;
        for (int i = 0; i < 3; i++) if (angles[i].equals(p.angles[i])) return false;
        return true;
    }

    // hashCode method
    public int hashCode() {
        return angles.hashCode();
    }

    // toString method
    public String toString() {
        return "Prototile\n        angles: (" + angles[0] + "," + angles[1] + "," + angles[2] + ")\n  edge lengths: " + lengths[0] + "\n                " +  lengths[1] + "\n                " +  lengths[2];
    }

    /*
    * return all the lengths
    */
    public BasicEdgeLength[] getLengths() {
        return lengths;
    }

    /*
    * return all the angles
    */
    public BasicAngle[] getAngles() {
        return angles;
    }

    /*
    * return true if this is isosceles
    */
    public boolean isosceles() {
        BasicAngle a0 = angles[0];
        BasicAngle a1 = angles[1];
        BasicAngle a2 = angles[2];
        return (a0.equals(a1) || a1.equals(a2) || a2.equals(a0));
    }

    /*
    * return true if this has two edges of length l
    */
    public boolean hasTwo(BasicEdgeLength l) {
        int count = 0;
        for (int i = 0; i < 3; i++) {
            if (lengths[i].equals(l)) count++;
        }
        return (count==2);
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
        return Arrays.asList(lengths).contains(l);
    }

    /*
    * check if this has an edge with length equal
    * to that of e.
    * if not, return false.
    * if yes, then check either the first or 
    * second such edge, depending on if secondEdge
    * is false or true. make sure its orientation
    * does not lie in klass (or its opposite 
    * does not lie in klass, if flip is true). 
    */
    public boolean compatible(BasicEdge e, boolean secondEdge, boolean flip, Iterable<Orientation> klass) {
        int i = -1;
        boolean yet = !secondEdge;
        for (int j = 0; j < 3; j++) {
            if (lengths[j].equals(e.getLength())) {
                if (yet) {
                    i = j;
                    break;
                } else {
                    yet = true;
                }
            }
        }
        if (i == -1) return false; // we can't find this length
        // if we're flipping, check the opposite Orientation
        Orientation o = (flip)? orientations[i] : orientations[i].getOpposite();
        for (Orientation a : klass) {
//            if (a.equals(o)) {System.out.println("Rejecting for Orientation.");return false;}
        }
        return true;
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
    public List<BasicTriangle> placements(BasicEdge e, ImmutableSet<Orientation> equivalenceClass) {
        ArrayList<BasicTriangle> output = new ArrayList<>(0);
        BasicEdgeLength l = e.getLength();
        BytePoint[] ends = e.getEnds();
        BytePoint e0 = ends[0];
        BytePoint e1 = ends[1].subtract(e0);
        BytePoint shift;
        BasicAngle turn;
        // set preTurn equal to the angle between e1 and the positive x-axis
        BasicAngle preTurn = e.angle();
        for (int i = 0; i < 3; i++) {
            if (l.equals(lengths[i])&&!equivalenceClass.contains(orientations[i].getOpposite())) {
                if (i == 0) {
                    shift = e0;
                    turn = preTurn;
                } else if (i == 1) {
                    turn = preTurn.minus(angles[2].supplement());
                    shift = e0.subtract(lengths[0].getAsVector(turn));
                } else {
                    turn = preTurn.minus(angles[1].piPlus());
                    shift = e0.subtract(lengths[2].getAsVector(turn.plus(angles[1])));
                }
                output.add(place(shift,turn,false));
            }
            if (l.equals(lengths[i])&&!equivalenceClass.contains(orientations[i])) {
                if (i == 0) {
                    turn = preTurn;
                    shift = e0.add(lengths[0].getAsVector(turn));
                } else if (i == 1) {
                    turn = preTurn.minus(angles[2].piPlus());
                    shift = e0.subtract(lengths[2].getAsVector(turn.plus(angles[1].supplement())));
                } else {
                    turn = preTurn.minus(angles[1].supplement());
                    shift = e0;
                }
                output.add(place(shift,turn,true));
            }
        }
        return output;
    }

    /*
    * Place the prototile in space.
    * p says where to place the root vertex.
    * a says how to orient it.
    * flip says whether or not to reflect it.
    */
    public BasicTriangle place(BytePoint p, BasicAngle a, boolean flip) {
        BytePoint p1 = BytePoint.ZERO_VECTOR;
        BytePoint p2 = lengths[0].getAsVector(BasicAngle.createBasicAngle(0));
        BytePoint p0 = lengths[2].getAsVector(angles[1]);
        BytePoint[] vertices = new BytePoint[] { p0, p1, p2 };
        BasicAngle[] newAngles = angles;
        Orientation[] newOrientations = orientations;
        BasicEdgeLength[] newLengths = lengths;
        if (flip) {
            /* 
            * Now flip the first and last of everything
            * to put things in ccw order.
            */
            BytePoint tempVertex = vertices[2];
            vertices[2] = vertices[0];
            vertices[0] = tempVertex;
            newAngles = flipAngles;
            newLengths = flipLengths;
            newOrientations = flipOrientations;
        }
        for (int j = 0; j < 3; j++)
            vertices[j] = BytePoint.createBytePoint(vertices[j],flip,a,p);
        return BasicTriangle.createBasicTriangle(newAngles, vertices, newOrientations, newLengths, this, flip);
    }

    /*
    * place this prototile against e.
    * this triangle might contain two edges with length
    * equal to that of e. If so, secondEdge tells us 
    * whether or not to place the second of them against
    * e. flip tells us whether or not to reflect this.
    */
    public BasicTriangle place(BasicEdge e, boolean secondEdge, boolean flip) {
        BasicEdgeLength l = e.getLength();
        BytePoint[] ends = e.getEnds();
        BytePoint e0 = ends[0];
        BytePoint e1 = ends[1].subtract(e0);
        BytePoint shift;
        BasicAngle turn;
        // set preTurn equal to the angle between e1 and the positive x-axis
        BasicAngle preTurn = e.angle();
        int i;
        boolean yet = !secondEdge; // we might have to go past the first matching edge
        // find the index of the edge matching length l
        for (i = 0; i < 3; i++) {
            if (l.equals(lengths[i])) {
                if (yet) {
                    break;
                } else {
                    yet = true;
                }
            }
        }

        if (!flip) {
            if (i == 0) {
                shift = e0;
                turn = preTurn;
            } else if (i == 1) {
                turn = preTurn.minus(angles[2].supplement());
                shift = e0.subtract(lengths[0].getAsVector(turn));
            } else {
                turn = preTurn.minus(angles[1].piPlus());
                shift = e0.subtract(lengths[2].getAsVector(turn.plus(angles[1])));
            }
        } else {
            if (i == 0) {
                turn = preTurn;
                shift = e0.add(lengths[0].getAsVector(turn));
            } else if (i == 1) {
                turn = preTurn.minus(angles[2].piPlus());
                shift = e0.subtract(lengths[2].getAsVector(turn.plus(angles[1].supplement())));
            } else {
                turn = preTurn.minus(angles[1].supplement());
                shift = e0;
            }
        }
    return place(shift,turn,flip);
    }

    // create an outline of the inflated prototile.  
    // the Integer lists describe the edge breakdowns.  
    // no sanity check!
    // we assume that these are breakdowns of the actual
    // edge of this prototile.
    public BasicEdge[] createSkeleton(ImmutableList<Integer> b1, ImmutableList<Integer> b2, ImmutableList<Integer> b3) {
        BasicEdge[] output = new BasicEdge[b1.size()+b2.size()+b3.size()];
        int k = 0;
        BasicAngle a1 = angles[1].piPlus();
        BasicAngle a2 = angles[2].supplement();
        BasicEdgeLength currentLength;
        BytePoint currentPoint = lengths[2].getAsVector(angles[1]).inflate();
        BytePoint nextPoint;

        // create lists of Orientations 
        int[] oCount = new int[EDGE_LENGTHS.size()]; // how many Orientations used from each pool?
        Orientation[] ol1 = new Orientation[b1.size()];
        Orientation[] ol2 = new Orientation[b2.size()];
        Orientation[] ol3 = new Orientation[b3.size()];
        for (int i = 0; i < ol1.length; i++) {
            int currentIndex = b1.get(i);
            BasicEdgeLength nowLength = EDGE_LENGTHS.get(currentIndex);
            ol1[i] = nowLength.getOrientation(oCount[currentIndex]);
            oCount[currentIndex]++;
        }
        for (int i = 0; i < ol2.length; i++) {
            int currentIndex = b2.get(i);
            BasicEdgeLength nowLength = EDGE_LENGTHS.get(currentIndex);
            ol2[i] = nowLength.getOrientation(oCount[currentIndex]);
            oCount[currentIndex]++;
        }
        for (int i = 0; i < ol3.length; i++) {
            int currentIndex = b3.get(i);
            BasicEdgeLength nowLength = EDGE_LENGTHS.get(currentIndex);
            ol3[i] = nowLength.getOrientation(oCount[currentIndex]);
            oCount[currentIndex]++;
        }

        // run through the edge breakdowns, adding edges to the skeleton.
        // this is going to get repetitive. 
        // the only thing that changes from one breakdown to the next
        // is the angle of rotation.
        for (Integer i : b3) {
            currentLength = BasicEdgeLength.createBasicEdgeLength(i);
            nextPoint = currentLength.getAsVector(a1).add(currentPoint);
            output[k] = BasicEdge.createBasicEdge(currentLength, ol3[k], new BytePoint[] { currentPoint, nextPoint });
            k++;
            currentPoint = nextPoint;
        }

        for (Integer i : b1) {
            currentLength = BasicEdgeLength.createBasicEdgeLength(i);
            nextPoint = currentLength.getAsVector(BasicAngle.createBasicAngle(0)).add(currentPoint);
            output[k] = BasicEdge.createBasicEdge(currentLength, ol1[k-ol3.length], new BytePoint[] { currentPoint, nextPoint });
            k++;
            currentPoint = nextPoint;
        }

        for (Integer i : b2) {
            currentLength = BasicEdgeLength.createBasicEdgeLength(i);
            nextPoint = currentLength.getAsVector(a2).add(currentPoint);
            output[k] = BasicEdge.createBasicEdge(currentLength, ol2[k-ol3.length-ol1.length], new BytePoint[] { currentPoint, nextPoint });
            k++;
            currentPoint = nextPoint;
        }

        return output;
    }

    // create an outline of the inflated prototile,  
    // assuming that it's isosceles.
    // the Integer lists describe the edge breakdowns.  
    // no sanity check!
    // we assume that these are breakdowns of the actual
    // edge of this prototile.
    public BasicEdge[] createSkeleton(ImmutableList<Integer> c1, ImmutableList<Integer> c2, boolean reverse) {
        ImmutableList<Integer> b1 = c1;
        ImmutableList<Integer> b3 = c2;
        // set up the middle edge.
        // it's either the same as the first or the same as the last edge.
        Integer[] preB2 = new Integer[(angles[0]==angles[1])?b1.size():b3.size()];

        // create lists of Orientations 
        int[] oCount = new int[EDGE_LENGTHS.size()]; // how many Orientations used from each pool?
        Orientation[] o1 = new Orientation[b1.size()];
        Orientation[] o3 = new Orientation[b3.size()];
        Orientation[] o2 = new Orientation[preB2.length];
        for (int i = 0; i < o1.length; i++) {
            int currentIndex = b1.get(i);
            BasicEdgeLength nowLength = EDGE_LENGTHS.get(currentIndex);
            o1[i] = nowLength.getOrientation(oCount[currentIndex]);
            oCount[currentIndex]++;
        }
        for (int i = 0; i < o3.length; i++) {
            int currentIndex = b3.get(i);
            BasicEdgeLength nowLength = EDGE_LENGTHS.get(currentIndex);
            o3[i] = nowLength.getOrientation(oCount[currentIndex]);
            oCount[currentIndex]++;
        }

        // initialize preB2 and o2.
        if (angles[0]==angles[1]) {
            if (reverse) {
                for (int i = 0; i < o2.length; i++) {
                    o2[i] = o1[o1.length-1-i].getOpposite();
                    preB2[i] = b1.get(o1.length-1-i);
                }
            } else {
                for (int i = 0; i < o2.length; i++) {
                    o2[i] = o1[i];
                    preB2[i] = b1.get(i);
                }
            }
        } else {
            if (reverse) {
                for (int i = 0; i < o2.length; i++) {
                    o2[i] = o3[o3.length-1-i].getOpposite();
                    preB2[i] = b3.get(o3.length-1-i);
                }

            } else {
                for (int i = 0; i < o2.length; i++) {
                    o2[i] = o3[i];
                    preB2[i] = b3.get(i);
                }
            }
        }
        ImmutableList<Integer> b2 = ImmutableList.copyOf(preB2);

        BasicEdge[] output = new BasicEdge[b1.size()+b2.size()+b3.size()];
        int k = 0;
        BasicAngle a1 = angles[1].piPlus();
        BasicAngle a2 = angles[2].supplement();
        BasicEdgeLength currentLength;
        BytePoint currentPoint = lengths[2].getAsVector(angles[1]).inflate();
        BytePoint nextPoint;

        // run through the edge breakdowns, adding edges to the skeleton.
        // this is going to get repetitive. 
        // the only thing that changes from one breakdown to the next
        // is the angle of rotation.
        int j = 0;
        for (Integer i : b3) {
            currentLength = BasicEdgeLength.createBasicEdgeLength(i);
            nextPoint = currentLength.getAsVector(a1).add(currentPoint);
            output[k] = BasicEdge.createBasicEdge(currentLength, o3[j], new BytePoint[] { currentPoint, nextPoint });
            j++;
            k++;
            currentPoint = nextPoint;
        }

        j = 0;
        for (Integer i : b1) {
            currentLength = BasicEdgeLength.createBasicEdgeLength(i);
            nextPoint = currentLength.getAsVector(BasicAngle.createBasicAngle(0)).add(currentPoint);
            output[k] = BasicEdge.createBasicEdge(currentLength, o1[j], new BytePoint[] { currentPoint, nextPoint });
            j++;
            k++;
            currentPoint = nextPoint;
        }

        j = 0;
        for (Integer i : b2) {
            currentLength = BasicEdgeLength.createBasicEdgeLength(i);
            nextPoint = currentLength.getAsVector(a2).add(currentPoint);
            output[k] = BasicEdge.createBasicEdge(currentLength, o2[j], new BytePoint[] { currentPoint, nextPoint });
            j++;
            k++;
            currentPoint = nextPoint;
        }

        return output;
    }

    public static void main(String[] args) {

        BasicPrototile testMe = getFirstTile();
        for (int k = 0; k < 10; k++) {
            System.out.println(testMe);
            testMe = testMe.getNextTile();
        }

    }

} // end of class BasicPrototile
