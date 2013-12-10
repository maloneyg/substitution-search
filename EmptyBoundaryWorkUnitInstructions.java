import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.*;
import java.util.logging.*;
import com.google.common.collect.*;
import java.util.concurrent.atomic.*;

// this class is a wrapper containing instructions
// for the production of work units.
public class EmptyBoundaryWorkUnitInstructions implements Serializable {

    // the index of the starter edge
    private final int starter;

    // the number of work units to be made
    private final int num;

    // a unique identifier
    private final int ID;

    // private constructor
    private EmptyBoundaryWorkUnitInstructions(int starter, int i, int ID) {
        this.starter = starter;
        num = i;
        this.ID = ID;
    } // constructor

    public String toString()
    {
        return "edge number " + starter + "\nsize of batch " + num + "\nID " + ID;
    }

    // getter methods
    public int getStarter() {
        return starter;
    }

    public int getNum() {
        return num;
    }

    public int getID() {
        return ID;
    }
    // end of getter methods

    public int hashCode()
    {
        return starter + 17*num;
    }

    // public static factory method
    public static EmptyBoundaryWorkUnitInstructions createEmptyBoundaryWorkUnitInstructions(int starter, int i, int ID) {
        return new EmptyBoundaryWorkUnitInstructions(starter,i,ID);
    }

} // end of class EmptyBoundaryWorkUnitInstructions
