import java.util.*;

public class ObjectEfficiency
{
    public static final int LIST_LENGTH = 1000000;
    public static final int ARRAY_LENGTH = 10;
    public static final int MAX_INTEGER = 100;

    public static void main(String[] args)
    {
        Date startTime = new Date();
        List<BasicPoint> objectList = new LinkedList<BasicPoint>();
        Random generator = new Random();
        for (int i=0; i < LIST_LENGTH; i++)
            {
                int[] randomIntArray = new int[ARRAY_LENGTH];
                for ( int j=0; j < ARRAY_LENGTH; j++ )
                    randomIntArray[j] = generator.nextInt(MAX_INTEGER);
                BasicPoint newPoint = BasicPoint.createBasicPoint(randomIntArray);
                objectList.add(newPoint);
            }
        Date endTime = new Date();
        double elapsedTime = (double)(endTime.getTime() - startTime.getTime())/1000; // seconds
        System.out.println(String.format("List created.  Elapsed time: %.3f s", elapsedTime));
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
