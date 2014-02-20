/**
*    This class implements a list of prototiles.
*/

import com.google.common.collect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.io.Serializable;
import java.util.List;

public class MutablePrototileList implements Serializable {

    private int[] tileCount;
    private static final ImmutableList<BasicPrototile> ALL_PROTOTILES = BasicPrototile.ALL_PROTOTILES;

    // constants to help us if we have extra prototiles
    private static final int DEG = Initializer.DEG;
    private static final boolean MIN = (Initializer.NULL_MATRIX==null);
    private static final ImmutableList<ImmutableList<Integer>> NULL_VECTORS;

    static { // initialize the list of null vectors
        if (MIN) {
            NULL_VECTORS = null;
        } else {
            List<ImmutableList<Integer>> preNV = new ArrayList<>();
            for (int i = 0; i < Initializer.NULL_MATRIX.getRowDimension(); i++) preNV.add(Initializer.NULL_MATRIX.getColumn(i));
            NULL_VECTORS = ImmutableList.copyOf(preNV);
        }
    } // here ends initialization of null vectors

    // make it Serializable
    static final long serialVersionUID = -6946831502508212124L;

    // static method to see if a prototile is allowed
    private static boolean valid(BasicPrototile p) {
        return ALL_PROTOTILES.indexOf(p) > -1;
    }

    // constructor methods.
    private MutablePrototileList(ImmutableList<BasicPrototile> tiles) {
        int[] tempCount = new int[DEG];
        for (BasicPrototile p : tiles) {
            if (valid(p)) {
                if (ALL_PROTOTILES.indexOf(p)<DEG) tempCount[ALL_PROTOTILES.indexOf(p)]++;
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

    // deep copy
    public MutablePrototileList deepCopy() {
        int[] i = new int[tileCount.length];
        for (int j = 0; j < i.length; j++) i[j] = tileCount[j];
        return new MutablePrototileList(i);
    }

    // implementation of equals method.  
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        MutablePrototileList o = (MutablePrototileList) obj;
        for (int i = 0; i < tileCount.length; i++) if (tileCount[i]!=o.tileCount[i]) return false;
        return true;
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
        int where = ALL_PROTOTILES.indexOf(p);
//        if (tileCount[where]<1)
//            throw new IllegalArgumentException("Can't remove prototile " + p + "\nbecause we haven't got any left.");
        if (MIN||where<tileCount.length) {
            tileCount[where]--;
        } else {
            for (int j = 0; j < tileCount.length; j++) tileCount[j]-=NULL_VECTORS.get(where-DEG).get(j);
        }
    }

    // add the prototile p
    public void add(BasicPrototile p) {
        int where = ALL_PROTOTILES.indexOf(p);
        if (MIN||where<tileCount.length) {
            tileCount[where]++;
        } else {
            for (int j = 0; j < tileCount.length; j++) tileCount[j]+=NULL_VECTORS.get(where-DEG).get(j);
        }
    }

    public boolean contains(BasicPrototile p) {
        if (!valid(p)) return false;
        int where = ALL_PROTOTILES.indexOf(p);
        if (MIN||where<tileCount.length) {
            return tileCount[where] > 0;
        } else {
            for (int j = 0; j < tileCount.length; j++) if (tileCount[j]<NULL_VECTORS.get(where-DEG).get(j)) return false;
            return true;
        }
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
