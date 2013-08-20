/**
*    This class implements a prototile.
*    It is not located in space.  
*/
import com.google.common.collect.*;

public class BasicPrototile implements AbstractPrototile<BasicAngle, BasicPoint, BasicEdgeLength, BasicEdge, BasicTriangle> {

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
    * return true if this has an edge with length l
    * and orientation o, false otherwise.
    */
    public boolean compatible(BasicEdgeLength l, Orientation o) {
        return true;
    }

    /*
    * Place the prototile in space.
    * p says where to place the root vertex.
    * a says how to orient it.
    * flip says whether or not to reflect it.
    */
    public BasicTriangle place(BasicPoint p, BasicAngle a, boolean flip) {
        BasicPoint p1 = BasicPoint.ZERO_VECTOR;
        System.out.println(""+p1);
        BasicPoint p2 = lengths.get(0).getAsVector();
        System.out.println(""+p2);
        BasicPoint p0 = lengths.get(2).getAsVector().rotate(angles.get(1));
        System.out.println(""+p0);
        System.out.println(Initializer.ROT);
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
            newOrientations[2] = newOrientations[0];
            newOrientations[0] = tempOrientation;
            BasicEdgeLength tempLength = newLengths[2];
            newLengths[2] = newLengths[0];
            newLengths[0] = tempLength;
        }
        for (int j = 0; j < 3; j++)
            vertices[j] = vertices[j].rotate(a).add(p);
        return BasicTriangle.createBasicTriangle(newAngles, vertices, newOrientations, newLengths);
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

    }

} // end of class BasicPrototile
