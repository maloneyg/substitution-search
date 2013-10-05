/**
*    This class implements a pool for BasicPoints.
*    If we need one and it already exists, the pool gives it to us.
*    The theoretical justification for this is that there are a few 
*    thousand BasicPoints that need to be used, but each one has 
*    thousands of references to it for calculation purposes.
*/

import com.google.common.collect.*;
import com.google.common.base.*;
import com.google.common.cache.*;
import java.io.Serializable;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BasicPointPool {

    // all existing points are held here.
    private static ConcurrentHashMap<IntWrapper,BasicPoint> pool = new ConcurrentHashMap<IntWrapper,BasicPoint>(10000,0.75F,24);

    private static AtomicInteger hits = new AtomicInteger();
    private static AtomicInteger tries = new AtomicInteger();

    // the only instance of this class
    private static BasicPointPool instance = new BasicPointPool();

    // constructor
    private BasicPointPool() {
    }

    // public static factory method
    public static BasicPointPool getInstance() {
        return instance;
    }

    public int size() {
        return pool.size();
    }

    // return the BasicPoint created from key.
    public BasicPoint getCanonicalVersion(int[] key) {
        BasicPoint output = pool.get(IntWrapper.createIntWrapper(key));
//        tries.getAndIncrement();
        if (output == null) {
            output = BasicPoint.createExNihilo(key);
            pool.put(IntWrapper.createIntWrapper(key),output);
        } else {
//            hits.getAndIncrement();
        }
        return output;
    }

    // clean it out
    public void clear() {
        pool.clear();
        hits.set(0);
        tries.set(0);
    }

    // how many hits?
    public int hits() {
        return hits.get();
    }

    // batting average
//    public double hitPercentage() {
//        return (100.0*hits.get())/tries.get();
//    }

} // end of class BasicPointPool
