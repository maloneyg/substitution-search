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

    // getters
    public ImmutableList<Integer> getEdge0() {
        return edge0;
    }
    public ImmutableList<Integer> getEdge1() {
        return edge1;
    }
    public ImmutableList<Integer> getEdge2() {
        return edge2;
    }

    // toArray method. For drawing the big triangle
    public ArrayList<RealMatrix> toArray() {
        ArrayList<RealMatrix> output = new ArrayList<RealMatrix>(3);
        for (BytePoint p : bigVertices) {
            output.add((RealMatrix)new Array2DRowRealMatrix(p.arrayToDraw()));
        }
        return output;
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        ImmutablePatch x = (ImmutablePatch) obj;
        return (//
            Arrays.equals(this.triangles,x.triangles)//
          &&Arrays.equals(this.openEdges,x.openEdges)//
          &&Arrays.equals(this.closedEdges,x.closedEdges)//
          &&this.edge0.equals(x.edge0)//
          &&this.edge1.equals(x.edge1)//
          &&this.edge2.equals(x.edge2)//
        );
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

    // toString method
    public String toString() {
        String output = "ImmutablePatch:\nTriangles:\n";
        for (BasicTriangle t : triangles) output += "\n" + t;
        output += "Open Edges:\n";
        for (BasicEdge e : openEdges) output += "\n" + e;
        output += "\nClosed Edges:\n";
        for (BasicEdge e : closedEdges) output += "\n" + e;
        return output;
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
        output.add(new OrderedTriple(toArray()));
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
        if (openEdges.length > 1 && Preinitializer.SHOW_QUANTUM_TRIANGLES) {
            for (BytePoint p : openEdges[openEdges.length-2].reverse().getQuantumTriangle()) {
                edgeList.set(0,(RealMatrix)new Array2DRowRealMatrix(p.arrayToDraw()));
                edgeList.set(1,(RealMatrix)new Array2DRowRealMatrix(p.arrayToDraw()));
                edgeList.set(2,(RealMatrix)new Array2DRowRealMatrix(p.arrayToDraw()));
                output.add(new OrderedTriple(new ArrayList<RealMatrix>(edgeList)));
            }
        }
        return output;
    }

    /*
    * produce closed edges for drawing the patch
    */
    public ArrayList<OrderedTriple> closedGraphicsDump() {
        ArrayList<OrderedTriple> output = new ArrayList<OrderedTriple>(closedEdges.length);
        BytePoint p0;
        BytePoint p1;
        ArrayList<RealMatrix> edgeList = new ArrayList<RealMatrix>(3);
        int counter = 0;
        output.add(new OrderedTriple(toArray()));
        for (BasicEdge e : closedEdges) {
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

    public int openSize() {
        return openEdges.length;
    }

    public int closedSize() {
        return closedEdges.length;
    }

    public int triangleSize() {
        return triangles.length;
    }

} // end of class ImmutablePatch
