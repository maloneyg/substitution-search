import java.io.*;
import java.util.*;

public class TriangleResultsMove
{
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        {
            // deserialize data
            String filename = //"results/tile0-bc.chk";//
                                "results/tile0-104.chk";
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
            List<ImmutablePatch> movedPatches = new LinkedList<>();;
            boolean ref = false;
            BasicAngle rot = BasicAngle.createBasicAngle(2);
            BytePoint shift = BasicEdgeLength.createBasicEdgeLength(1).getAsVector(rot);
            for (int i = 0; i < patches.size(); i++) movedPatches.add(patches.get(i).move(ref,rot,shift));
            try
                {
                    PointsDisplay display = new PointsDisplay(movedPatches,filename);
                }
            catch ( java.awt.HeadlessException e )
                {
                    System.out.println("X11 display not supported on this terminal.");
                }

        }
}
