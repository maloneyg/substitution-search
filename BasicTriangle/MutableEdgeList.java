/**
*    This class implements two collections of edges:
*    one list of open edges and one list of closed edges
*/

import com.google.common.collect.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Stack;
import java.util.List;
import java.util.Arrays;
import org.apache.commons.math3.linear.*;
import java.util.Iterator;
import java.util.Collections;

public class MutableEdgeList {

    // a helper class. closedEdges holds a Stack of these.
    private class IndexAndEdge { // nested class begins here

        private final int index;
        private final BasicEdge edge;

        // constructor
        private IndexAndEdge(int index, BasicEdge edge) {
            this.index = index;
            this.edge = edge;
        }

        public int getIndex() {
            return index;
        }

        public BasicEdge getEdge() {
            return edge;
        }

    } // nested class ends here

    // make it Serializable
//    static final long serialVersionUID = 3422733298735932933L;

    // the open edges in this patch
    private Stack<BasicEdge> openEdges;

    // the closed edges in this patch
    private Stack<IndexAndEdge> closedEdges;

    // initial constructor
    private MutableEdgeList(BasicEdge[] e) {
        openEdges = new Stack<>();
        closedEdges = new Stack<>();

        for (int i = 1; i < e.length; i++) {
            openEdges.push(e[i]);
        }
    }

    // public static factory method
    public static MutableEdgeList createMutableEdgeList(BasicEdge[] e) {
        return new MutableEdgeList(e);
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        MutableEdgeList x = (MutableEdgeList) obj;
        if (this.openEdges.size()!=x.openEdges.size()) return false;
        if (this.closedEdges.size()!=x.closedEdges.size()) return false;
        for (int i = 0; i < openEdges.size(); i++) {
            if (!this.openEdges.get(i).equals(x.openEdges.get(i))) return false;
        }
        for (int i = 0; i < closedEdges.size(); i++) {
            IndexAndEdge ie1 = this.closedEdges.get(i);
            IndexAndEdge ie2 = x.closedEdges.get(i);
            if (ie1.getIndex()!=ie2.getIndex()) return false;
            if (!ie1.getEdge().equals(ie2.getEdge())) return false;
        }
        return true;
    }

    // hashCode method
    public int hashCode() {
        int prime = 31;
        int result = 9;
        result = prime*result + openEdges.hashCode();
        result = prime*result + closedEdges.hashCode();
        return result;
    }

    // place triangle t 
    public void place(BasicTriangle t, MutableOrientationPartition p) {
        BasicEdge[] matches = t.getEdges();
        /*
        * find the indices of the edges of t in openEdges.
        */
        List<Integer> indexList = new ArrayList<>();
        List<Integer> tList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int thisIndex = openEdges.indexOf(matches[i]);
            if (thisIndex == -1) {
                openEdges.push(matches[i].reverse());
            } else {
                indexList.add(thisIndex);
                tList.add(i);
            }
        }
        Collections.sort(indexList);

        // the last open edge is the first to be added to closedEdges
        for (int j = 0; j < indexList.size(); j++) {
            int k = indexList.get(j);
            BasicEdge e = openEdges.get(k);
            p.identify(e.getOrientation(),matches[tList.get(j)].getOrientation());
            closedEdges.push(new IndexAndEdge(k,e));
        }
        // but the last to be removed from openEdges
        for (int j = indexList.size()-1; j >= 0; j--) {
            openEdges.remove(indexList.get(j));
        }

    }

    // remove triangle t 
    public void remove(BasicTriangle t, Iterable<BasicTriangle> triangles, MutableOrientationPartition o) {
        BasicEdge[] matches = t.getEdges();
        // remove BasicEdges from openEdges until you get
        // one that isn't incident with t
        while (true) {
            BasicEdge e = openEdges.pop();
            if (!t.reverseIncidentEdge(e)) {
                openEdges.push(e);
                break;
            }
        }

        // return BasicEdges from closedEdges to openEdges
        // until you get one that isn't incident with t
        while (true) {
            IndexAndEdge ie = closedEdges.pop();
            if (t.simpleIncidentEdge(ie.getEdge())) {
                openEdges.add(ie.getIndex(),ie.getEdge());
            } else {
                closedEdges.push(ie);
                break;
            }
        }

    }

    // return the next BasicEdge to be checked
    public BasicEdge getNextEdge() {
        return openEdges.peek();
    }

    // return openEdges for iteration purposes
    public Iterable<BasicEdge> open() {
        // we could convert to an ArrayList if we're worried
        // about other methods accessing this directly
        return new ArrayList<>(openEdges);
    }

    // return closedEdges for iteration purposes
    public Iterable<BasicEdge> closed() {
        return new Iterable<BasicEdge>() {
            // an iterator that iterates through things in this class
            public Iterator<BasicEdge> iterator() {
                return new Iterator<BasicEdge>() {

                    private int index = 0;

                    public boolean hasNext() {
                        return (index < closedEdges.size());
                    }

                    public BasicEdge next() {
                        index++;
                        return closedEdges.get(index-1).getEdge();
                    }

                    public void remove() { // do nothing
                    }

                };
            } // first iterator ends here
        };
    }


} // end of class MutableEdgeList
