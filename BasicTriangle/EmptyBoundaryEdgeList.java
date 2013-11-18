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

public class EmptyBoundaryEdgeList implements Serializable {

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
    private EmptyBoundaryEdgeList(BasicEdge e) {
        openEdges = new Stack<>();
        closedEdges = new Stack<>();
        openEdges.push(e);
    }

    // public static factory method
    public static EmptyBoundaryEdgeList createEmptyBoundaryEdgeList(BasicEdge e) {
        return new EmptyBoundaryEdgeList(e);
    }

    // equals method
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        EmptyBoundaryEdgeList x = (EmptyBoundaryEdgeList) obj;
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
    public void place(BasicTriangle t, MutableOrientationPartition p, PuzzleBoundary boundary) {
        BasicEdge[] matches = t.getEdges();
        List<BasicEdge> newOpens = new ArrayList<>(2); // new open edges
        /*
        * find the indices of the edges of t in openEdges.
        */
        List<Integer> indexList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            int thisIndex = openEdges.indexOf(matches[i]);
            if (thisIndex == -1) {
                newOpens.add(matches[i].reverse());
            } else {
                indexList.add(thisIndex);
            }
        }

        // check if the new edges are on the boundary
        // if they are, then file them away in the appropriate place
        for (BasicEdge e : newOpens) {
            System.out.println(boundary + "\n" + e.reverse());
            if (boundary.incident(e.reverse())==1) {
                System.out.println("oh yeah");
                boundary.add(e.reverse());
                newOpens.remove(e);
            }
        }

        // if there are two new edges, push them in the right order
        // so that the clockwise edge is on top of the stack
        if (newOpens.size()==1) {
            openEdges.push(newOpens.get(0));
        } else if (newOpens.size()==2) {
            openEdges.push(BasicEdge.ccw(newOpens.get(0),newOpens.get(1)));
            openEdges.push(BasicEdge.cw(newOpens.get(0),newOpens.get(1)));
        }

        Collections.sort(indexList);

        // the last open edge is the first to be added to closedEdges
        for (int j = indexList.size()-1; j >= 0; j--) {
            int k = indexList.get(j); // important: k is not an Integer
            BasicEdge e = openEdges.get(k);
            for (BasicEdge m : matches) {
                if (m.congruent(e)) {
                    p.identify(e.getOrientation(),m.getOrientation());
                    break;
                }
            }
            closedEdges.push(new IndexAndEdge(k,e));
            openEdges.remove(k);
        }

    }

    // remove triangle t 
    public void remove(BasicTriangle t, Iterable<BasicTriangle> triangles, MutableOrientationPartition o, PuzzleBoundary boundary) {
        BasicEdge[] matches = t.getEdges();
        // remove BasicEdges from openEdges until you get
        // one that isn't incident with t
        while (!openEdges.empty()) {
            BasicEdge e = openEdges.pop();
            if (!t.reverseIncidentEdge(e)) {
                openEdges.push(e);
                break;
            }
        }

        // remove edges from boundary
        // technically we don't need to know if they're incident
        for (BasicEdge e : matches) boundary.remove(e);

        // return BasicEdges from closedEdges to openEdges
        // until you get one that isn't incident with t
        while (!closedEdges.empty()) {
            IndexAndEdge ie = closedEdges.pop();
            if (t.simpleIncidentEdge(ie.getEdge())) {
                o.split(ie.getEdge().getOrientation());
                openEdges.add(ie.getIndex(),ie.getEdge());
            } else {
                closedEdges.push(ie);
                break;
            }
        }

        // the Orientation partition has been split; now put it back together
        o.followInstructions();
        for (BasicTriangle a : triangles) {
            for (BasicEdge c : closed()) {
                if (a.simpleIncidentEdge(c)) {
                    for (BasicEdge b : a.getEdges()) {
                        if (b.congruent(c))
                            o.identify(b.getOrientation(),c.getOrientation());
                    }
                }
            }
        }

    }

    // return the next BasicEdge to be checked
    public BasicEdge getNextEdge() {
        if (openEdges.empty()) {
            return null;
//        } else if (closedEdges.empty()) {
        } else {
            return openEdges.peek();
//        } else {
//            BasicEdge output = openEdges.peek();
//            BasicEdge current;
//            for (int i = openEdges.size()-1; i > -1; i--) {
//                current = openEdges.get(i);
//                if (current.getLength().equals(BasicEdge.UNIT_LENGTH)) return current;
//                if (current.getLength().compareTo(output.getLength())<0) output = current;
//            }
//            return output;
        }
    }

    // return the number of open edges
    public int openSize() {
        return openEdges.size();
    }

    // return the number of closed edges
    public int closedSize() {
        return closedEdges.size();
    }

    // return openEdges for iteration purposes
    public Iterable<BasicEdge> open() {
        // we could convert to an ArrayList if we're worried
        // about other methods accessing this directly
        //return new ArrayList<>(openEdges);
        return openEdges;
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


} // end of class EmptyBoundaryEdgeList
