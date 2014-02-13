import java.io.*;
import java.util.*;

public class TriangleResultsMove
{
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        {
            // deserialize data
            String filename =   "results/tile1-2plusc.chk";//
                              //"results/tile0-106.chk";
            String otherfilename = "results/tile0-104.chk";
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
                    patches = ((TriangleResults)in.readObject()).getPatches();
                    System.out.println(patches.size() + " completed patches have been read.");
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

            List<ImmutablePatch> otherpatches = null;
            if ( ! new File(otherfilename).isFile() )
                {
                    System.out.println(filename + " not found!");
                    return;
                }
            try
                {
                    FileInputStream fileIn = new FileInputStream(otherfilename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    otherpatches = ((TriangleResults)in.readObject()).getPatches();
                    System.out.println(otherpatches.size() + " completed patches have been read.");
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

            // display data
            List<ImmutablePatch> movedPatches = new LinkedList<>();;
            BasicPrototile P = BasicPrototile.createBasicPrototile(Preinitializer.PROTOTILES.get(1));
            boolean ref = true;
            BasicAngle rot = BasicAngle.createBasicAngle(2);
            BasicAngle a = BasicAngle.createBasicAngle(3);
            BytePoint shift = BasicEdgeLength.createBasicEdgeLength(1).getAsVector(a);
            for (int i = 0; i < patches.size(); i++) {
                ImmutablePatch p1 = patches.get(i);
                if (p1.isosceles(P)) movedPatches.add(p1);
//                for (int j = 0; j < otherpatches.size(); j++) {
//                    if (j==0) System.out.println(i);
//                    if (p1.getEdge1().compatible(otherpatches.get(j).getEdge2().reverse())) movedPatches.add(p1.combine(otherpatches.get(j),1,2,false));
//                }
            }
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
