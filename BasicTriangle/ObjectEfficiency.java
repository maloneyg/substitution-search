import java.util.*;
import java.util.Scanner;
import com.google.common.collect.*;

public class ObjectEfficiency
{

    public static final int LIST_LENGTH = 50000;
    public static final int ARRAY_LENGTH = 10;
    public static final int MAX_INTEGER = 100;
    public static final ImmutableList<BasicPrototile> ALL_PROTOTILES = BasicPrototile.ALL_PROTOTILES;
    public static final int MAX_PROTOTILE = ALL_PROTOTILES.size();
    public static final int MAX_ANGLE = 2*BasicAngle.ANGLE_SUM;
    private static final Scanner kbd = new Scanner(System.in);

    // prompt the user to continue
    private static void promptEnter() {
        System.out.println("Press ENTER");
        kbd.nextLine();
        System.out.print("Garbage collection initiated...");
        System.gc();
        System.out.println("complete.");
    }

    public static void main(String[] args)
    {
        Date startTime = new Date();
        List<BytePoint> objectList = new ArrayList<BytePoint>(LIST_LENGTH);
        Random generator = new Random();
        Date endTime = null;
        double elapsedTime = 0.0;

    /*    // fill up a bunch of BytePoints
        for (int i=0; i < LIST_LENGTH; i++)
            {
                //System.out.print(String.format("%.2f%s   \r", (i+1)*100.0/LIST_LENGTH, "%"));
                int[] randomIntArray = new int[ARRAY_LENGTH];
                if ( i >= 0  )
                    {
                        // create a new point at random
                        for ( int j=0; j < ARRAY_LENGTH; j++ )
                            randomIntArray[j] = generator.nextInt(MAX_INTEGER);
                        BytePoint newPoint = BytePoint.createBytePoint(randomIntArray);
                        objectList.add(newPoint);
                    }
                else
                    {
                        // add an old point to the list
                        int randomOldIndex = generator.nextInt(objectList.size());
                        BytePoint oldPoint = objectList.get(randomOldIndex);
                        int[] oldPointArray = oldPoint.pointAsArray();
                        BytePoint newPoint = BytePoint.createBytePoint(oldPointArray);
                        objectList.add(newPoint);
                    }
            } // end of BytePoints

        endTime = new Date();
        elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("\nBytePoint List created.  Elapsed time: %.3f s", elapsedTime));

        // time for BasicTriangles
        promptEnter();
        System.out.println(objectList.size() + " BytePoints.");
        startTime = new Date();
        List<BasicTriangle> tl = new ArrayList<BasicTriangle>(LIST_LENGTH);
        for (int i=0; i < LIST_LENGTH; i++) {
            BasicPrototile p = ALL_PROTOTILES.get(generator.nextInt(MAX_PROTOTILE));
            BasicAngle a = BasicAngle.createBasicAngle(generator.nextInt(MAX_ANGLE));
            tl.add(p.place(objectList.get(i),a,false));
        }
        endTime = new Date();
        elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("\nBasicTriangle List created.  Elapsed time: %.3f s", elapsedTime));

        // time for BasicPatches
        promptEnter();
        System.out.println(tl.size() + " BasicTriangles.");
*/        startTime = new Date();
        List<BasicPatch> pl = new ArrayList<BasicPatch>(LIST_LENGTH);
        for (int i=0; i < LIST_LENGTH; i++) {
            BasicPrototile p = ALL_PROTOTILES.get(generator.nextInt(MAX_PROTOTILE));
            ImmutableList<Integer> BD0 = p.getLengths().get(0).getBreakdown();
            ImmutableList<Integer> BD1 = p.getLengths().get(1).getBreakdown();
            ImmutableList<Integer> BD2 = p.getLengths().get(2).getBreakdown();
            BasicEdge[] edgeList = p.createSkeleton(BD0, BD1, BD2);
            ImmutableList<ImmutableList<Integer>> testBD = ImmutableList.of(BD0, BD1, BD2);
            ImmutableList<BytePoint> vertices = p.place(BytePoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false).getVertices();
            BytePoint[] bigVertices = new BytePoint[] {vertices.get(0).inflate(),vertices.get(1).inflate(),vertices.get(2).inflate()};
            BasicPatch patch = BasicPatch.createBasicPatch(edgeList,bigVertices);
            pl.add(patch);
        }
        ImmutableList<BasicPatch> PL = ImmutableList.copyOf(pl);
        pl = null;
        endTime = new Date();
        elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("\nPatch List created.  Elapsed time: %.3f s", elapsedTime));

        // time for OrientationPartitions
//        promptEnter();
//        System.out.println(pl.size() + " BasicPatches.");
//        startTime = new Date();
//        List<OrientationPartition> op = new ArrayList<OrientationPartition>(LIST_LENGTH);
//        for (int i=0; i < LIST_LENGTH; i++) {
//            op.add(pl.get(i).getPartition());
//        }
//        endTime = new Date();
//        elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
//        System.out.println(String.format("\nOrientationPartition List created (not from scratch, but from BasicPatch List).  Elapsed time: %.3f s", elapsedTime));

        // time for OpenEdges
        promptEnter();
        System.out.println(PL.size() + " BasicPatches.");
  /*      startTime = new Date();
        List<Orientation> ol = new ArrayList<Orientation>(LIST_LENGTH);
        for (int i=0; i < LIST_LENGTH; i++) {
            ol.add(Orientation.createOrientation());
        }
        endTime = new Date();
        elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("\nOrientation List created.  Elapsed time: %.3f s", elapsedTime));

        // hypothesis: ImmutableList creates Java.lang.Objects
        // when constructed using ImmutableList.of(element1, element2, ...).
        promptEnter();
        System.out.println(ol.size() + " Orientations.");
        startTime = new Date();
        List<ImmutableList<BytePoint>> bp1 = new ArrayList<ImmutableList<BytePoint>>(LIST_LENGTH);
        for (int i=0; i < LIST_LENGTH; i++) {
            List<BytePoint> tempList = new ArrayList<BytePoint>(3);
            for ( int k=0; k < 3; k++ ) {
                int[] randomIntArray = new int[ARRAY_LENGTH];
                // create a new point at random
                for ( int j=0; j < ARRAY_LENGTH; j++ ) {
                    randomIntArray[j] = generator.nextInt(MAX_INTEGER);
                    BytePoint newPoint = BytePoint.createBytePoint(randomIntArray);
                    tempList.add(newPoint);
                }
            }
            bp1.add(ImmutableList.copyOf(tempList));
        }
        endTime = new Date();
        elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("\nImmutableList<BytePoint> List created (copyOf).  Elapsed time: %.3f s", elapsedTime));

        promptEnter();
        System.out.println(bp1.size() + " ImmutableList<BytePoint>.");

*/
        while (true)
            {
                try
                    {
                        Thread.sleep(1*1000);
                    }
                catch (InterruptedException e)
                    {
                    }
            }
    }
} // end of class ObjectEfficiency
