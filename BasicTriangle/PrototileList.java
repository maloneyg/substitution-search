/**
*    This class implements a list of prototiles.
*/

import com.google.common.collect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;

public final class PrototileList implements Serializable {

    private final ImmutableList<BasicPrototile> tiles;

    // make it Serializable
//    static final long serialVersionUID = 1267821834624463132L;

    // constructor methods.
    private PrototileList(ImmutableList<BasicPrototile> tiles) {
        this.tiles = tiles;
    }

    // public static factory method.
    static public PrototileList createPrototileList(ImmutableList<BasicPrototile> tiles) {
        return new PrototileList(tiles);
    }

    // implementation of equals method.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        PrototileList o = (PrototileList) obj;
        // lazy test: just check to see if the two lists
        // are the same, in the same order.
        // A better test would check to see if the corresponding
        // sets were the same.
        return this.tiles.equals(o.tiles);
    }

    // hashCode implementation.
    public int hashCode() {
        ArrayList<Integer> codes = new ArrayList<>(tiles.size());
        for (BasicPrototile p : tiles)
            codes.add(p.hashCode());
        Collections.sort(codes);
        int prime = 13;
        int result = 29;
        for (Integer i : codes)
            result = prime*result + i;
        return result;
    }

    // create a new PrototileList by removing the prototile p
    //  from this one.
    public PrototileList remove(BasicPrototile p) {
        int count = 0;
        ArrayList<BasicPrototile> temp = new ArrayList<>(tiles.size()-1);
        for (BasicPrototile t : tiles) {
            if (t.equals(p) && count == 0) {
                count++;
            } else {
                temp.add(t);
            }
        }
        return new PrototileList(ImmutableList.copyOf(temp));
    }

    public boolean contains(BasicPrototile p) {
        return tiles.contains(p);
    }

    public int size() {
        return tiles.size();
    }

} // end of class PrototileList
