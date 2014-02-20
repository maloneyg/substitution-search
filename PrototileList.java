/**
*    This class implements a list of prototiles.
*/

import com.google.common.collect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

public final class PrototileList implements Serializable {

    private final int[] tileCount;
    private static final ImmutableList<BasicPrototile> ALL_PROTOTILES = BasicPrototile.ALL_PROTOTILES;

    // make it Serializable
//    static final long serialVersionUID = 1267821834624463132L;

    // static method to see if a prototile is allowed
    private static boolean valid(BasicPrototile p) {
        return ALL_PROTOTILES.indexOf(p) > -1;
    }

    // constructor methods.
    private PrototileList(ImmutableList<BasicPrototile> tiles) {
        int[] tempCount = new int[Initializer.DEG];
        for (BasicPrototile p : tiles) {
            if (valid(p)) {
                tempCount[ALL_PROTOTILES.indexOf(p)]++;
            } else {
                throw new IllegalArgumentException("We aren't using prototile " + p);
            }
        }
        tileCount = tempCount;
    }

    private PrototileList(int[] tileCount) {
        this.tileCount = tileCount;
    }

    // public static factory method.
    static public PrototileList createPrototileList(ImmutableList<BasicPrototile> tiles) {
        return new PrototileList(tiles);
    }

    // dump a MutablePrototileList
    public MutablePrototileList dumpMutablePrototileList() {
        int[] tempCount = new int[tileCount.length];
        for (int i = 0; i < tempCount.length; i++) tempCount[i] = tileCount[i];
        return MutablePrototileList.createMutablePrototileList(tempCount);
    }

    // implementation of equals method.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        PrototileList o = (PrototileList) obj;
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

    // create a new PrototileList by removing the prototile p
    //  from this one.
    public PrototileList remove(BasicPrototile p) {
        int position = ALL_PROTOTILES.indexOf(p);
        if (!valid(p))
            throw new IllegalArgumentException("We aren't using prototile " + p);
        if (tileCount[position]<1)
            throw new IllegalArgumentException("Can't remove prototile " + p + "\nbecause we haven't got any left.");
        int[] output = new int[ALL_PROTOTILES.size()];
        for (int i = 0; i < output.length; i++)
            output[i] = tileCount[i];
        output[position]--;
        return new PrototileList(output);
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

} // end of class PrototileList
