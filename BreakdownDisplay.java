import java.io.*;
import java.util.*;

public class BreakdownDisplay
{
    @SuppressWarnings("unchecked")
    public static void main(String[] args)
        {
            // deserialize data
            String filename = //
                                "breakdowns1.chk";
            if ( ! new File(filename).isFile() )
                {
                    System.out.println(filename + " not found!");
                    return;
                }
            try
                {
                    FileInputStream fileIn = new FileInputStream(filename);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    EdgeBreakdownTree breakdowns = ((EdgeBreakdownTree)in.readObject());
                    System.out.println(" Breakdowns have been read.");
                    System.out.println(breakdowns.chainString());
                }
            catch (Exception e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

        }
}
