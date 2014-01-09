import java.io.*;
import java.util.*;

public class ClosedResultsDisplay
{
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        {
            // deserialize data
            String filename = //"results/tile0-bc.chk";//
                              //"results/tile0-abc.chk";//"results/tile0superhuge.chk";
                              "errors3.chk";
            List<ImmutablePatch> patches = null;
            if ( ! new File(filename).isFile() )
                {
                    System.out.println(filename + " not found!");
                    return;
                }
            try
                {
                    FileInputStream fileIn = new FileInputStream(filename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
//                    patches = (ArrayList<ImmutablePatch>)in.readObject();
                    patches = ((TriangleResults)in.readObject()).getPatches();
                    System.out.println(patches.size() + " completed patches have been read.");
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

            // display data
            try
                {
                    ClosedPointsDisplay display = new ClosedPointsDisplay(patches,filename);
                    System.out.println(patches.get(0).openSize() + " open edges.");
                    System.out.println(patches.get(0).closedSize() + " closed edges.");
                    System.out.println(patches.get(0).triangleSize() + " triangles.");
                }
            catch ( java.awt.HeadlessException e )
                {
                    System.out.println("X11 display not supported on this terminal.");
                }

        }
}
