/**
*    This class implements a collection of triangles.
*/

import com.google.common.collect.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import org.apache.commons.math3.linear.*;

public class ImmutablePatch implements Serializable {

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

    // the edge breakdown data
    private final ImmutableList<Integer> edge0;
    private final ImmutableList<Integer> edge1;
    private final ImmutableList<Integer> edge2;

    // private constructor
    private ImmutablePatch(BasicTriangle[] t, BasicEdge[] e1, BasicEdge[] e2, OrientationPartition o, BytePoint[] v,ImmutableList<Integer> bd0,ImmutableList<Integer> bd1,ImmutableList<Integer> bd2) {
        triangles = t;
        openEdges = e1;
        closedEdges = e2;
        partition = o;
        bigVertices = v;
        edge0 = bd0;
        edge1 = bd1;
        edge2 = bd2;
    }

    // public static factory method 
    public static ImmutablePatch createImmutablePatch(BasicTriangle[] t, BasicEdge[] e1, BasicEdge[] e2, OrientationPartition o, BytePoint[] v,ImmutableList<Integer> bd0,ImmutableList<Integer> bd1,ImmutableList<Integer> bd2) {
        return new ImmutablePatch(t,e1,e2,o,v,bd0,bd1,bd2);
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        ImmutablePatch x = (ImmutablePatch) obj;
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
    * produce some stuff for drawing the patch
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

} // end of class ImmutablePatch