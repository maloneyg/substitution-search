import java.io.*;
import java.util.*;

public class LengthSearch
{

    private static final long r = 101027L;
    private long a = 2L;
    private static File slowFile = new File("slowsearch.txt");
    private static File killFile = new File("stopsearch.txt");

    public LengthSearch() { }

    private long step(long l) {
        return (long)((long)l*a)/r;
    }


    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        {

            LengthSearch L = new LengthSearch();

            long l = 1L;

            while (!killFile.isFile()) { 
                if (slowFile.isFile()) {
                    try {
                        Thread.sleep(4000);
                    } catch (Exception e) {
                        // do nothing
                    }
                } else {
                    l = L.step(l);
                }
            }

            System.out.println("Server has found kill file!  Shutting down!");

        }
} // end of class LengthSearch
