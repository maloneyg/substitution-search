/**
*    This class implements a list of prototiles.
*/

import com.google.common.collect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

public class MutablePrototileList implements Serializable {

    private int[] tileCount;
    private static final ImmutableList<BasicPrototile> ALL_PROTOTILES = BasicPrototile.ALL_PROTOTILES;

    // make it Serializable
//    static final long serialVersionUID = 1267821834624463132L;

    // static method to see if a prototile is allowed
    private static boolean valid(BasicPrototile p) {
        return ALL_PROTOTILES.indexOf(p) > -1;
    }

    // constructor methods.
    private MutablePrototileList(ImmutableList<BasicPrototile> tiles) {
        int[] tempCount = new int[ALL_PROTOTILES.size()];
        for (BasicPrototile p : tiles) {
            if (valid(p)) {
                tempCount[ALL_PROTOTILES.indexOf(p)]++;
            } else {
                throw new IllegalArgumentException("We aren't using prototile " + p);
            }
        }
        tileCount = tempCount;
    }

    private MutablePrototileList(int[] tileCount) {
        this.tileCount = tileCount;
    }

    // public static factory method.
    static public MutablePrototileList createMutablePrototileList(int[] i) {
        return new MutablePrototileList(i);
    }

    // implementation of equals method.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        MutablePrototileList o = (MutablePrototileList) obj;
        return this.tileCount.equals(o.tileCount);
    }

    // hashCode implementation.
    public int hashCode() {
        int prime = 13;
        int result = 29;
        for (int i : tileCount)
            result = prime*result + i;
        return result;
    }

    // string
    public String toString() {
        String output = "PrototileList:\n";
        for (int i = 0; i < tileCount.length; i++) output += tileCount[i] + " of " + ALL_PROTOTILES.get(i) + "\n";
        return output;
    }

    // remove the prototile p
    public void remove(BasicPrototile p) {
        int position = ALL_PROTOTILES.indexOf(p);
        if (!valid(p))
            throw new IllegalArgumentException("We aren't using prototile " + p);
        if (tileCount[position]<1)
            throw new IllegalArgumentException("Can't remove prototile " + p + "\nbecause we haven't got any left.");
        tileCount[position]--;
    }

    // add the prototile p
    public void add(BasicPrototile p) {
        int position = ALL_PROTOTILES.indexOf(p);
        if (!valid(p))
            throw new IllegalArgumentException("We aren't using prototile " + p);
        tileCount[position]++;
    }

    public boolean contains(BasicPrototile p) {
        if (!valid(p)) return false;
        return tileCount[ALL_PROTOTILES.indexOf(p)] > 0;
    }

    public int size() {
        int output = 0;
        for (int i = 0; i < tileCount.length; i++)
            output+=tileCount[i];
        return output;
    }

    public boolean empty() {
        for (int i = 0; i < tileCount.length; i++) {
            if (tileCount[i] != 0) return false;
        }
        return true;
    }

} // end of class MutablePrototileList
