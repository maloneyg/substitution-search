import java.util.*;
import java.util.Scanner;
import com.google.common.collect.*;

public class ObjectEfficiency
{

    public static final int LIST_LENGTH = 100000;
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
//        System.out.print("Garbage collection initiated...");
//        System.gc();
//        System.out.println("complete.");
    }

    public static void main(String[] args)
    {
        Date startTime = new Date();
        List<BasicPoint> objectList = new ArrayList<BasicPoint>(LIST_LENGTH);
        Random generator = new Random();

        // fill up a bunch of BasicPoints
        for (int i=0; i < LIST_LENGTH; i++)
            {
                //System.out.print(String.format("%.2f%s   \r", (i+1)*100.0/LIST_LENGTH, "%"));
                int[] randomIntArray = new int[ARRAY_LENGTH];
                if ( i >= 0  )
                    {
                        // create a new point at random
                        for ( int j=0; j < ARRAY_LENGTH; j++ )
                            randomIntArray[j] = generator.nextInt(MAX_INTEGER);
                        BasicPoint newPoint = BasicPoint.createBasicPoint(randomIntArray);
                        objectList.add(newPoint);
                    }
                else
                    {
                        // add an old point to the list
                        int randomOldIndex = generator.nextInt(objectList.size());
                        BasicPoint oldPoint = objectList.get(randomOldIndex);
                        int[] oldPointArray = oldPoint.pointAsArray();
                        BasicPoint newPoint = BasicPoint.createBasicPoint(oldPointArray);
                        objectList.add(newPoint);
                    }
            } // end of BasicPoints

        Date endTime = new Date();
        double elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("\nBasicPoint List created.  Elapsed time: %.3f s", elapsedTime));

        // time for BasicTriangles
        promptEnter();
        System.out.println(objectList.size() + " BasicPoints.");
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
        startTime = new Date();
        List<BasicPatch> pl = new ArrayList<BasicPatch>(LIST_LENGTH);
        for (int i=0; i < LIST_LENGTH; i++) {
            BasicPrototile p = ALL_PROTOTILES.get(generator.nextInt(MAX_PROTOTILE));
            ImmutableList<Integer> BD0 = p.getLengths().get(0).getBreakdown();
            ImmutableList<Integer> BD1 = p.getLengths().get(1).getBreakdown();
            ImmutableList<Integer> BD2 = p.getLengths().get(2).getBreakdown();
            ImmutableList<BasicEdge> edgeList = p.createSkeleton(BD0, BD1, BD2);
            ImmutableList<ImmutableList<Integer>> testBD = ImmutableList.of(BD0, BD1, BD2);
            ImmutableList<BasicPoint> vertices = p.place(BasicPoint.ZERO_VECTOR,BasicAngle.createBasicAngle(0),false).getVertices();
            ImmutableList<BasicPoint> bigVertices = ImmutableList.of(vertices.get(0).inflate(),vertices.get(1).inflate(),vertices.get(2).inflate());
            BasicPatch patch = BasicPatch.createBasicPatch(edgeList,bigVertices);
            pl.add(patch);
        }
        endTime = new Date();
        elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("\nPatch List created.  Elapsed time: %.3f s", elapsedTime));

        // time for Orientations
        promptEnter();
        System.out.println(pl.size() + " BasicPatches.");
        startTime = new Date();
        List<Orientation> ol = new ArrayList<Orientation>(LIST_LENGTH);
        for (int i=0; i < LIST_LENGTH; i++) {
            ol.add(Orientation.createOrientation());
        }
        endTime = new Date();
        elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("\nOrientation List created.  Elapsed time: %.3f s", elapsedTime));

        promptEnter();
        System.out.println(ol.size() + " Orientations.");


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
