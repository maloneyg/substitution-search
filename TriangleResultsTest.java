import java.io.*;
import java.util.*;

public class TriangleResultsTest {

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {


        String inPrefix = args[0];
        String outPrefix = args[1];
        BasicPrototile P = BasicPrototile.createBasicPrototile(Preinitializer.PROTOTILES.get(Integer.parseInt(inPrefix)));
        int i = 0;
        int k = 0;
        int oldSize = 0;
        String filename = "interim/tile" + inPrefix + String.format("-%08d-interim.chk",i);
        String outName = outPrefix + String.format("-%08d-interim.chk",k);
        List<ImmutablePatch> patches = new LinkedList<>();
        List<ImmutablePatch> isoPatches = new LinkedList<>();

        while (new File(filename).isFile()) { // here begins deserialization

            // de-serialize
            try
                {
                    FileInputStream fileIn = new FileInputStream(filename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    patches = ((TriangleResults)in.readObject()).getPatches();
                    System.out.println(patches.size() + " completed patches have been read from " + filename + ".");
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

            // select the patches that satisfy the isosceles condition
            for (int j = 0; j < patches.size(); j++) {
                ImmutablePatch p1 = patches.get(j);
                if (p1.isosceles(P)) isoPatches.add(p1);
            }

            // write them to disk
            if (isoPatches.size()>0) {
                try
                    {
                        TriangleResults triangleResults = new TriangleResults(isoPatches);
                        FileOutputStream fileOut = new FileOutputStream(outName);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(triangleResults);
                        out.close();
                        fileOut.close();
                        System.out.println(String.format("wrote %d results to %s.",isoPatches.size(),outName));
                        k++;
                        outName = outPrefix + String.format("-%08d-interim.chk",k);
                        isoPatches.clear();
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
            } // here ends writing to disk

            i++;
            filename = "interim/tile" + inPrefix + String.format("-%08d-interim.chk",i);
            //patches.clear();
        } // here ends deserialization of interim files

        filename = "interim/tile" + inPrefix + "-final.chk";
        if (new File(filename).isFile()) { // here begins deserialization of final file
            try
                {
                    FileInputStream fileIn = new FileInputStream(filename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    patches = ((TriangleResults)in.readObject()).getPatches();
                    System.out.println(patches.size() + " completed patches have been read from " + filename + ".");
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

            // select the patches that satisfy the isosceles condition
            for (int j = 0; j < patches.size(); j++) {
                ImmutablePatch p1 = patches.get(j);
                if (p1.isosceles(P)) isoPatches.add(p1);
            }

            // write them to disk
            if (isoPatches.size()>0) {
                try
                    {
                        TriangleResults triangleResults = new TriangleResults(isoPatches);
                        FileOutputStream fileOut = new FileOutputStream(outName);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(triangleResults);
                        out.close();
                        fileOut.close();
                        System.out.println(String.format("wrote %d results to %s.",isoPatches.size(),outName));
                        k++;
                        outName = outPrefix + String.format("-%08d-interim.chk",k);
                        isoPatches.clear();
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
            } // here ends writing to disk

        } // here ends deserialization of final file

    } // end of main
} // end of class TriangleResultsTest
