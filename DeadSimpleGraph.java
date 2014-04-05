
/*************************************************************************
 *  Compilation:  javac DeadSimpleGraph.java
 *  Execution:    java DeadSimpleGraph
 *
 *  A class representing a graph, the vertices of which are instances of E
 *
 *************************************************************************/

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.io.Serializable;

public class DeadSimpleGraph<E> implements Serializable {

    private LinkedList<E> vertices;
    private HashMultimap<E,E> edges;

    // private constructor
    protected DeadSimpleGraph() {
        vertices = new LinkedList<E>();
        edges = HashMultimap.create();
    }

    // add a vertex
    public void addVertex(E e) {
        synchronized(vertices) {
            vertices.add(e);
        }
    }

    // add many vertices
    public void addAllVertices(List<E> l) {
        synchronized(vertices) {
            vertices.addAll(l);
        }
    }

    // remove a vertex
    // we don't bother to check if it's already there
    // but afterwards we have to remove all edges containing it
    public void removeVertex(E e) {
        synchronized(vertices) {
            vertices.remove(e);
        }
        synchronized(edges) {
            edges.removeAll(e);
            if (edges.containsValue(e)) {
                List<E> toRemove = new ArrayList<>();
                for (Map.Entry m : edges.entries()) {
                    if (m.getValue().equals(e)) toRemove.add((E)m.getKey());
                }
                for (E f : toRemove) {
                    edges.remove(f,e);
                }
            }
        }
    }

    // remove many vertices
    public void removeAllVertices(List<E> l) {
        synchronized(vertices) {
            vertices.removeAll(l);
        }
        synchronized(edges) {
            for (Map.Entry<E,E> m : ImmutableSet.copyOf(edges.entries())) {
                if (l.contains(m.getKey())||l.contains(m.getValue())) edges.remove(m.getKey(),m.getValue());
            }
        }
    }

    // dump an immutable copy of the vertices
    public List<E> dumpVertices() {
        synchronized(vertices) {
            return ImmutableList.copyOf(vertices);
        }
    }

    // add an edge
    // we don't check if the corresponding vertices already exist
    public void addEdge(E e1, E e2) {
        synchronized(edges) {
            edges.put(e1,e2);
        }
    }

    // remove an edge
    // we don't check if it exists
    public void removeEdge(E e1, E e2) {
        synchronized(edges) {
            edges.remove(e1,e2);
            edges.remove(e2,e1);
        }
    }

    // return a list of all edges containing e
    public List<Map.Entry<E,E>> edgesOf(E e) {
        List<Map.Entry<E,E>> output = new LinkedList<>();
        for (Map.Entry m : edges.entries()) {
            if (m.getValue().equals(e)||m.getKey().equals(e)) output.add(m);
        }
        return ImmutableList.copyOf(output);
    }

    // return true if this contains e
    public boolean hasVertex(E e) {
        return vertices.contains(e);
    }

    // return true if this contains edge e1 e2
    public boolean hasEdge(E e1, E e2) {
        return (edges.containsEntry(e1,e2)||edges.containsEntry(e2,e1));
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
        return vertices.size();
    }


    // test client
    public static void main(String[] args) {

    }

} // end of class DeadSimpleGraph
