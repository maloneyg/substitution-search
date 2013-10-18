import java.io.*;
import java.util.*;

public class TriangleResultsDisplay
{
    public static void main(String[] args)
        {
            // deserialize data
            TriangleResults triangleResults = null;
            String filename = MutableParadigmServer.RESULT_FILENAME;
            try
                {
                    FileInputStream fileIn = new FileInputStream(filename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    triangleResults = (TriangleResults)in.readObject();
                    System.out.println(triangleResults.getPatches().size() + " completed patches have been read.");
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

            // display data
            try
                {
                    PointsDisplay display = new PointsDisplay(triangleResults.getPatches(),"mutable test");
                }
            catch ( java.awt.HeadlessException e )
                {
                    System.out.println("X11 display not supported on this terminal.");
                }
        }
}
