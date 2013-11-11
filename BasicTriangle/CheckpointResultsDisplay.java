import java.io.*;
import java.util.*;

public class CheckpointResultsDisplay
{
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        {
            // deserialize data
            String filename = "primary_server_checkpoint.chk";//
            List<ImmutablePatch> patches = null;
            if ( ! new File(filename).isFile() )
                return;
            try
                {
                    FileInputStream fileIn = new FileInputStream(filename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    patches = ((ServerCheckpoint)in.readObject()).getAllCompletedPatches();
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

            // deserialize data
//            TriangleResults triangleResults = null;
//            filename = MutableParadigmServer.RESULT_FILENAME;
//            if ( ! new File(filename).isFile() )
//                return;
//            try
//                {
//                    FileInputStream fileIn = new FileInputStream(filename);
//                    ObjectInputStream in = new ObjectInputStream(fileIn);
//                    triangleResults = (TriangleResults)in.readObject();
//                    System.out.println(triangleResults.getPatches().size() + " completed patches have been read.");
//                }
//            catch (Exception e)
//                {
//                    e.printStackTrace();
//                    System.exit(1);
//                }
//
//            // display data
//            try
//                {
//                    PointsDisplay display = new PointsDisplay(triangleResults.getPatches(),"mutable test");
//                }
//            catch ( java.awt.HeadlessException e )
//                {
//                    System.out.println("X11 display not supported on this terminal.");
//                }
        }
}
