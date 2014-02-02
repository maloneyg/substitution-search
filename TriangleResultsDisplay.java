import java.io.*;
import java.util.*;

public class TriangleResultsDisplay
{
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        {
            // deserialize data
            String filename = //"results/tile0-bc.chk";//
                              //"result.chk";
                                "interim/tile344-105.chk";
                              //"results/tile2-105-option.chk";
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
                    PointsDisplay display = new PointsDisplay(patches,filename);
                }
            catch ( java.awt.HeadlessException e )
                {
                    System.out.println("X11 display not supported on this terminal.");
                }

        }
}
