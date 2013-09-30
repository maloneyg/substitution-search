/**
*    This class implements a pool for ImmutableSets of Orientations.
*    If we need one and it already exists, the pool gives it to us.
*/

import com.google.common.collect.*;
import com.google.common.base.*;
import com.google.common.cache.*;
import java.io.Serializable;
import java.util.concurrent.*;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class OrientationClassPool {

    // all existing Orientation classes are held here.
    private static ConcurrentHashMap<HashSet<Orientation>,HashSet<Orientation>> pool = new ConcurrentHashMap<>(100000);

    private static AtomicInteger hits = new AtomicInteger();
    private static AtomicInteger tries = new AtomicInteger();

    // the only instance of this class
    private static OrientationClassPool instance = new OrientationClassPool();

    // constructor
    private OrientationClassPool() {
    }

    // public static factory method
    public static OrientationClassPool getInstance() {
        return instance;
    }

    public int size() {
        return pool.size();
    }

    // return the BasicPoint created from key.
    public HashSet<Orientation> getCanonicalVersion(HashSet<Orientation> key) {
        HashSet<Orientation> output = pool.get(key);
        tries.getAndIncrement();
        if (output == null) {
            pool.put(key,key);
            output = key;
        } else {
            hits.getAndIncrement();
        } 
        return output;
    }

    // clean it out
    public void clear() {
        pool.clear();
    }

    // how many hits have we got?
    public int hits() {
        return hits.get();
    }

    // batting average
    public double hitPercentage() {
        return ((double) hits.get())/tries.get();
    }

} // end of class OrientationClassPool
