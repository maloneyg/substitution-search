import java.io.*;

public class TestResult implements Result, Serializable
{
    private final String description;

    public TestResult(String description)
    {
        this.description = description;
    }

    public String toString()
    {
        return description;
    }
}
