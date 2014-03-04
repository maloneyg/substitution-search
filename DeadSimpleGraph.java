
/*************************************************************************
 *  Compilation:  javac DeadSimpleGraph.java
 *  Execution:    java DeadSimpleGraph
 *
 *  A class representing a graph, the vertices of which are instances of E
 *
 *************************************************************************/

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Stack;
import java.io.Serializable;

public class DeadSimpleGraph<E> implements Serializable {

    private LinkedList<E> vertices;
    private HashMultiMap<E,E> edges;

    // private constructor
    protected DeadSimpleGraph() {
        vertices = new LinkedList<E>();
        edges = new HashMultiMap<E,E>();
    }

    // add data and make it the head of a one-unit subset
    public void add(E data) {
        PartitionNode<E> newNode = new PartitionNode<>(data,head,null,true);
        head.setPrevious(newNode);
        head = newNode;
    }

    // return true if this contains e
    public boolean hasVertex(E e) {
        synchronized(vertices) {
            return vertices.contains(e);
        }
    }

    // equals method.
    // currently broken
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        DeadSimpleGraph l = (DeadSimpleGraph) obj;
        return true;
    }

    // hashCode override.
    // currently somewhat broken
    public int hashCode() {
        int prime = 59;
        int result = 19;
            for (E e : vertices) result = prime*result + e.hashCode();
        return result;
    }

    // the number of vertices
    public int size() {
        int output = 0;
        PartitionNode<E> currentNode = head;
        while (currentNode != null) {
            output++;
            currentNode = currentNode.getNext();
        }
        return output;
    }


    // test client
    public static void main(String[] args) {

    }

} // end of class DeadSimpleGraph
