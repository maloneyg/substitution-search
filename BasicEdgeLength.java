/**
*    This class implements an edge length.
*/

import java.lang.Math.*;
import java.io.Serializable;
import com.google.common.collect.*;
import com.google.common.base.*;
import java.util.ArrayList;
import java.util.List;

final public class BasicEdgeLength implements AbstractEdgeLength<BasicAngle, BytePoint, BasicEdgeLength>, Comparable<BasicEdgeLength>, Serializable {

    // make it Serializable
    static final long serialVersionUID = -3774381761787701530L;

    private static final int N = Initializer.N;

    /*
    * A list of enums representing the allowable edge lengths.
    * This list comes from Initializer.
    */
    static final private ImmutableList<Initializer.EDGE_LENGTH> LENGTHS = Initializer.LENGTHS;

    /*
    * A list of all possible BasicEdgeLength objects.
    * When someone asks for a BasicEdgeLength, we just give 
    * his one of these.
    */
    static final protected ImmutableList<BasicEdgeLength> ALL_EDGE_LENGTHS;

    /*
    * A list of vector representatives of the allowable edge lengths.
    * Each one starts at the origin and lies on the positive x-axis.
    */
    static final private ImmutableList<BytePoint> REPS;

    /*
    * Same thing, but for special isosceles triangles
    */
    static final private ImmutableList<BytePoint> ISOREPS;

    static { // initialize REPS. Use recursion.
        BytePoint[] preReps = new BytePoint[Math.max(2,LENGTHS.size())];
        preReps[0] = BytePoint.UNIT_VECTOR;
        preReps[1] = BytePoint.UNIT_VECTOR.timesA();
        for (int i = 2; i < preReps.length; i++)
            preReps[i] = preReps[i-1].timesA().subtract(preReps[i-2]);
        REPS = ImmutableList.copyOf(preReps);

        // now do ISOREPS
        BytePoint[] preIso = new BytePoint[Preinitializer.PROTOTILES.size()-1];
        for (int i = 1; i < preIso.length; i++) {
            preIso[i] = preReps[i].add(preReps[i-1]);
        }
        preIso[0] = preReps[0];
        preIso[preIso.length-1] = preReps[preReps.length-1];
        ISOREPS = ImmutableList.copyOf(preIso);

        // initialize ALL_EDGE_LENGTHS.
        BasicEdgeLength[] preAllEdgeLengths = new BasicEdgeLength[((Preinitializer.ISOSCELES) ? ISOREPS.size() : 0) + LENGTHS.size()];
        for (int j = 0; j < LENGTHS.size(); j++)
            preAllEdgeLengths[j] = new BasicEdgeLength(j);
        ALL_EDGE_LENGTHS = ImmutableList.copyOf(preAllEdgeLengths);
    } // here ends initialization of REPS and ISOREPS

    /*
    * The type of this edge length.
    * It is one of the elements of LENGTHS.
    */
    final private Initializer.EDGE_LENGTH length;

    /*
    * The vector representations of this edge length.
    * One vector for each possible angle.
    * The vector associated to the zero angle 
    * is one of the vectors in REPS.
    */
    final private ImmutableList<BytePoint> reps;

    /*
    * A list of Integers representing the indices of the different 
    * BasicEdgeLengths that appear in the the inflated version of
    * this BasicEdgeLength.
    */
    final private ImmutableList<Integer> breakdown;

    /*
    * A pool of Orientations to be used with edges of this length.
    */
    final private ImmutableList<Orientation> orientationPool;

    /*
    * A list of points that might be the third point of a triangle
    * having an edge of this length in the standard position
    */
    final private ImmutableList<BytePoint> quantumTriangle;

    // private constructor
    private BasicEdgeLength(int i) {
        // make a list of all edges with this length
        // with one end at the origin
        BytePoint[] preReps = new BytePoint[2 * N];
        BasicAngle a = BasicAngle.createBasicAngle(1);
        preReps[0] = (i<REPS.size()) ? REPS.get(i) : ISOREPS.get(i-REPS.size());
        for (int j = 1; j < 2*N; j++) preReps[j] = preReps[j-1].rotate(a);
        reps = ImmutableList.copyOf(preReps);
        // pick the correct length out of the main list
        length = LENGTHS.get(i);

        // get the number of each BasicEdgeLength occuring in 
        // inflated version of this.
        // how we do it depends on if this is a special isosceles edge or not
        ImmutableList<Integer> preList = (i<REPS.size()) ? Initializer.INFLATED_LENGTHS.getColumn(i) : Initializer.INFLATED_ISOLENGTHS.getColumn(i-REPS.size());
        // now turn it into a list of indices of BasicEdgeLengths.
        int numEdges = 0;
        for (Integer k : preList) numEdges += k;
        Integer[] preBreakdown = new Integer[numEdges];
        int k = 0;
        for (int l = 0; l < preList .size(); l++) {
            for (int m = 0; m < preList.get(l); m++) {
                preBreakdown[k] = l;
                k++;
            }
        }
        breakdown = ImmutableList.copyOf(preBreakdown);

        // initialize orientationPool
        // I think we only use one of these now
        orientationPool = ImmutableList.of(Orientation.createOrientation());
//        ImmutableList<Integer> numOccurrences = Initializer.INFLATED_LENGTHS.getRow(i);
//        int kk = 0;
//        for (int j = 0; j < numOccurrences.size(); j++) kk += numOccurrences.get(j);
//        Orientation[] preO = new Orientation[kk];
//        for (int j = 0; j < preO.length; j++) preO[j] = Orientation.createOrientation();
//        orientationPool = ImmutableList.copyOf(preO);

        // initialize quantumTriangle
        List<BytePoint> preQT = new ArrayList<>();
        if (i<REPS.size()) {
            for (ImmutableList<Integer> L : (Preinitializer.ISOSCELES) ? Preinitializer.PREPROTOTILES : Preinitializer.PROTOTILES) {
                for (int j = 0; j < 3; j++) {
                    if (L.get(j)-1==i||Initializer.N-L.get(j)-1==i) {
                        // get the next and previous angles in the list
                        Integer ang1 = L.get((j+1)%3);
                        Integer ang2 = L.get((j-1<0)? 2 : j-1);
                        // get the length represented by the next angle
                        BytePoint otherLength = REPS.get(Initializer.acute(ang1)-1);
                        BytePoint newOne = otherLength.rotate(BasicAngle.createBasicAngle(ang2));
                        if (!preQT.contains(newOne)) preQT.add(newOne);
                        BytePoint newTwo = otherLength.rotate(BasicAngle.createBasicAngle(ang2).supplement()).add(REPS.get(i));
                        if (!preQT.contains(newTwo)) preQT.add(newTwo);
                    }
                }
            } // here ends initialization of quantumTriangle
        } else if (i-REPS.size()<ISOREPS.size()-1) {
        } else {
        }
        quantumTriangle = ImmutableList.copyOf(preQT);
    } // here ends constructor

    // public static factory method
    static public BasicEdgeLength createBasicEdgeLength(int i) {
        if (i < 0 || i > ALL_EDGE_LENGTHS.size()-1)
            throw new IllegalArgumentException("Incorrect edge length index.");
        return ALL_EDGE_LENGTHS.get(i);
    }

    // compare based on the length field
    public int compareTo(BasicEdgeLength l) {
        return this.length.compareTo(l.length);
    }

    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        BasicEdgeLength l = (BasicEdgeLength) obj;
        return this.length.equals(l.length);
    }

    public int hashCode() {
        return length.hashCode();
    }

    public String toString() {
        return "Edge length " + ALL_EDGE_LENGTHS.indexOf(this);
    }

    /*
    * return a vector with length equal to this edge length,
    * making an angle of a with the positive x-axis.
    */
    public BytePoint getAsVector(BasicAngle a) {
        return reps.get(a.getAsInt());
    }

    /*
    * return an edge breakdown of the inflated version of
    * this BasicEdgeLength.
    * returns the same thing every time, whereas presumably
    * we want many such edge breakdowns, so we'll have to
    * produce more from this one.
    */
    public ImmutableList<Integer> getBreakdown() {
        return breakdown;
    }

    /*
    * return a new Orientation for an edge of this length
    */
    public Orientation getOrientation(int i) {
        return orientationPool.get(i);
    }

    /*
    * return the quantum triangle associated with this edge length
    */
    public List<BytePoint> getQuantumTriangle() {
        return quantumTriangle;
    }

    /*
    * return the edge length opposite the given angle in a triangle.
    */
    public static BasicEdgeLength lengthOpposite(BasicAngle a) {
        int angleModN = a.getAsInt() % N;
        if (angleModN == 0 || angleModN == N)
            throw new IllegalArgumentException("There is no edge length opposite an angle of 0.");
        return createBasicEdgeLength(Math.min(angleModN, N - angleModN)- 1);
    }

    public static void main(String[] args) {


        for (int i = 0; i < ALL_EDGE_LENGTHS.size(); i++) {
            System.out.println(ALL_EDGE_LENGTHS.get(i).getAsVector(BasicAngle.createBasicAngle(0)));
        }


        int k = 0; 
        ImmutableList<Integer> preBreakdown = Initializer.INFLATED_LENGTHS.getColumn(k);
        System.out.println("Testing edge breakdowns.");
        System.out.println("PreBreakdown for edge " + k + ":");
        System.out.print("( ");
        for (int i = 0; i < preBreakdown.size(); i++) System.out.print(preBreakdown.get(i) + " ");
        System.out.print(")\n");


        BasicEdgeLength l = ALL_EDGE_LENGTHS.get(k);
        System.out.println("Breakdown for edge " + k + ":");
        System.out.print("( ");
        for (int i = 0; i < l.breakdown.size(); i++) System.out.print(l.breakdown.get(i) + " ");
        System.out.print(")\n");

        BasicAngle a0 = BasicAngle.createBasicAngle(0);
        BasicAngle a1 = BasicAngle.createBasicAngle(1);
        BytePoint p0 = ALL_EDGE_LENGTHS.get(0).getAsVector(a0);
        BytePoint p1 = ALL_EDGE_LENGTHS.get(1).getAsVector(a0);
        BytePoint p2 = ALL_EDGE_LENGTHS.get(2).getAsVector(a0);
        BytePoint p3 = ALL_EDGE_LENGTHS.get(2).getAsVector(a1);

        System.out.print(p0 + " and " + p1 + ": " + p0.colinear(p1) + "\n");
        System.out.print(p1 + " and " + p0 + ": " + p1.colinear(p0) + "\n");
        System.out.print(p1 + " and " + p2 + ": " + p1.colinear(p2) + "\n");
        System.out.print(p2 + " and " + p1 + ": " + p2.colinear(p1) + "\n");
        System.out.print(p1 + " and " + p3 + ": " + p1.colinear(p3) + "\n");
        System.out.print(p3 + " and " + p1 + ": " + p3.colinear(p1) + "\n");
        System.out.print(p2 + " and " + p0 + ": " + p2.colinear(p0) + "\n");
        System.out.print(p0 + " and " + p2 + ": " + p0.colinear(p2) + "\n");
        System.out.print(p0 + " and " + p3 + ": " + p0.colinear(p3) + "\n");
        System.out.print(p3 + " and " + p0 + ": " + p3.colinear(p0) + "\n");

        System.out.println(p0 + "." + p1 + ": " + p0.dotProduct(p1));
        System.out.println(p1 + "." + p0 + ": " + p1.dotProduct(p0));
        System.out.println(p1 + "." + p2 + ": " + p1.dotProduct(p2));
        System.out.println(p0 + "." + p2 + ": " + p0.dotProduct(p2));

//        ShortPolynomial p02 = p0.dotProduct(p0);
//        ShortPolynomial p12 = p1.dotProduct(p1);
//        ShortPolynomial q02 = p1.subtract(p0).dotProduct(p1.subtract(p0));

        double p02 = p0.dotProduct(p0);
        double p12 = p1.dotProduct(p1);
        double q02 = p1.subtract(p0).dotProduct(p1.subtract(p0));

        System.out.println(p0 + "." + p0 + ": " + p02);
        System.out.println(p1.subtract(p0) + "." + p1.subtract(p0) + ": " + q02);
        System.out.println(p1 + "." + p1 + ": " + p12);
        System.out.println("MINPOLY: " + LengthAndAreaCalculator.HALF_MIN_POLY);
        System.out.println("Plug in cos(pi/N): " + LengthAndAreaCalculator.HALF_MIN_POLY.evaluate(Initializer.COS));

        System.out.print(p0 + " x " + p1 + ": " + p0.crossProduct(p1) + "\n");
        System.out.print(p1 + " x " + p0 + ": " + p1.crossProduct(p0) + "\n");
        System.out.print(p1 + " x " + p2 + ": " + p1.crossProduct(p2) + "\n");
        System.out.print(p2 + " x " + p1 + ": " + p2.crossProduct(p1) + "\n");
        System.out.print(p1 + " x " + p3 + ": " + p1.crossProduct(p3) + "\n");
        System.out.print(p3 + " x " + p1 + ": " + p3.crossProduct(p1) + "\n");
        System.out.print(p2 + " x " + p0 + ": " + p2.crossProduct(p0) + "\n");
        System.out.print(p0 + " x " + p2 + ": " + p0.crossProduct(p2) + "\n");
        System.out.print(p0 + " x " + p3 + ": " + p0.crossProduct(p3) + "\n");
        System.out.print(p3 + " x " + p0 + ": " + p3.crossProduct(p0) + "\n");


    }

} // end of class BasicEdgeLength
