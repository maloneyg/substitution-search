import java.io.*;
import java.util.*;

public class TriangleResultsCombine {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {

        String inPrefix = args[0];
        String outName = args[1];
        int i = 0;
        int oldSize = 0;
        String filename = "interim/" + inPrefix + String.format("-%08d-interim.chk",i);
        List<ImmutablePatch> patches = new LinkedList<>();

        while (new File(filename).isFile()) { // here begins deserialization
            try
                {
                    FileInputStream fileIn = new FileInputStream(filename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    patches.addAll(((TriangleResults)in.readObject()).getPatches());
                    System.out.println((patches.size()-oldSize) + " completed patches have been read from " + filename + ".");
                    oldSize = patches.size();
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

            i++;
            filename = "interim/" + inPrefix + String.format("-%08d-interim.chk",i);
        } // here ends deserialization of interim files

        filename = "interim/" + inPrefix + "-final.chk";
        if (new File(filename).isFile()) { // here begins deserialization
            try
                {
                    FileInputStream fileIn = new FileInputStream(filename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    patches.addAll(((TriangleResults)in.readObject()).getPatches());
                    System.out.println((patches.size()-oldSize) + " completed patches have been read from " + filename + ".");
                    oldSize = patches.size();
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }
        } // here ends deserialization of final file


        try
            {
                TriangleResults triangleResults = new TriangleResults(patches);
                FileOutputStream fileOut = new FileOutputStream(outName);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(triangleResults);
                out.close();
                fileOut.close();
                System.out.println(String.format("wrote %d results to %s.",patches.size(),outName));
            }
        catch (Exception e)
            {
                e.printStackTrace();
            }

    } // end of main
} // end of class TriangleResultsCombine
