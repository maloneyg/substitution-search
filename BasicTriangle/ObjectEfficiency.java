import java.util.*;

public class ObjectEfficiency
{
    public static final int LIST_LENGTH = 1000000;
    public static final int ARRAY_LENGTH = 10;
    public static final int MAX_INTEGER = 100;

    public static void main(String[] args)
    {
        Date startTime = new Date();
        List<BasicPoint> objectList = new ArrayList<BasicPoint>(LIST_LENGTH);
        Random generator = new Random();

        /*int[] pointOneArray = {1,1,1,1,1,1,1,1,1,1};
        int[] pointTwoArray = {1,1,1,1,1,1,1,1,1,1};
        
        BasicPoint.ArrayWrapper a1 = new BasicPoint.ArrayWrapper(pointOneArray);
        BasicPoint.ArrayWrapper a2 = new BasicPoint.ArrayWrapper(pointTwoArray);

        BasicPoint p1 = BasicPoint.points.getUnchecked(a1);
        BasicPoint p2 = BasicPoint.points.getUnchecked(a2);
        //BasicPoint pointOne = BasicPoint.createBasicPoint(pointOneArray);
        //BasicPoint pointTwo = BasicPoint.createBasicPoint(pointTwoArray);
*/
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
            }

        Date endTime = new Date();
        double elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("\nList created.  Elapsed time: %.3f s", elapsedTime));
        System.out.println(objectList.size());
        //for (BasicPoint p : objectList)
        //    System.out.println(p);
        //System.out.println(objectList.get(0));
        //System.out.println(objectList.get(LIST_LENGTH-1));
        //System.out.println(BasicPoint.points.stats());
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
}
