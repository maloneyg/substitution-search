/*************************************************************************
 *  Compilation:  javac SimpleSet.java
 *  Execution:    javaSimpleSet 
 *
 *  A type of set for which adding and removing elements use the same 
 *  operation.  insert(T) adds T if T isn't there already, and removes
 *  it if it is there.
 *
 *************************************************************************/

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Collections;

public class SimpleSet<T> extends HashSet<T> {

    // override the ordinary HashSet methods.
    public boolean add(T x) {
        if (contains(x)) {
            super.remove(x);
        } else {
            super.add(x);
        }
        return true;
    }

    /* 
    * Apparently there is some problem here.
    * For some reason remove is defined on Objects, not just T.
    * So I'm casting to T here, but I don't know if it's safe.
    */ 
    public boolean remove(Object o) {
        T x = (T) o;
        if (contains(x)) {
            super.remove(x);
        } else {
            super.add(x);
        }
        return true;
    }

    // Set union.
    public boolean addAll(HashSet<T> B) {
        for (T thing : B) {
            add(thing);
        }
        return true;
    }

    // Set complement.
    public boolean removeAll(HashSet<T> B) {
        for (T thing : B) {
            add(thing);
        }
        return true;
    }

    // equals method override.
    public boolean equals(HashSet<T> B) {
       for (T thing : this) {
            if (!(B.contains(thing)))
                return false;
       }
       for (T thing : B) {
            if (!(this.contains(thing)))
                return false;
       }
       return true;
    }

    // hashCode method override.
    public int hashCode() {
        int prime = 61;
        int result = 19;
        LinkedList<Integer> codes = new LinkedList();
        for ( T thing : this) {
            codes.addLast(thing.hashCode());
        }
        Collections.sort(codes);
        for ( Integer code : codes) {
            result = prime*result + code;
        }
        return result;
    }

    // test client
    public static void main(String[] args) {
        SimpleSet<Integer> L = new SimpleSet();
        SimpleSet<Integer> M = new SimpleSet();

        for (int i = 0; i < 10; i++) {
            L.add(i);
        }
        for (int j = 7; j < 15; j++) {
            M.add(j);
        }
        System.out.println("SimpleSet L:");
        System.out.println("" + L);
        System.out.println("SimpleSet M:");
        System.out.println("" + M);
        System.out.println("L simple union M:");
        L.addAll(M);
        System.out.println("" + L);

    }

} // end of class SimpleSet
