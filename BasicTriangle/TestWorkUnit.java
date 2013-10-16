import java.io.*;
import java.util.concurrent.*;

public class TestWorkUnit implements WorkUnit, Callable<Result>, Serializable
{
    private final int ID;

    private TestWorkUnit(int ID)
    {
        this.ID = ID;
    }

    public static TestWorkUnit getTestWorkUnit(int ID)
    {
        return new TestWorkUnit(ID);
    }

    public Result call()
    {
        try
            {
                if ( ID == 10 )
                    throw new IllegalArgumentException("oops");

                Thread.sleep(10*1000);

                TestResult result = new TestResult("result for ID " + ID);
                TriangleClient.sendResult(result);
                return result;
            }
        catch (InterruptedException e)
            {
            }
        return Result.JOB_INTERRUPTED;
    }

    public String toString()
    {
        return "job " + ID;
    }
}
