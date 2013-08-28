import org.apache.commons.math3.linear.*;
import java.io.*;
import java.util.*;

/**
*    This class implements a simple program that will read in a file,
*    the lines of which are substitution rules on some tile, and print
*    out a picture of the substitution.  
*/

public class TriangleDraw {

  public static final String inputFilename = "105-4-tilings";



  public static String GrabSublist(String biglist, Integer index) {
    int depth = 0; // How deep have we descended into sublists?
    int whichsublist = 0; // Which number of sublist are we in?
    int substringstart = 0; // Where does our chosen substring start?
    char ch; // The character at which we are currently looking.

    for (int i = 0; i < biglist.length(); i++) {
      ch = biglist.charAt(i);
      if (ch == '{') {
        depth++;
        if (depth == 1) {
          whichsublist++;
          if (whichsublist == index) {
            substringstart = i;
          }
        }
      }

      if (ch == '}') {
        depth--;
        if (depth == 0 && whichsublist == index) {
          if (substringstart + 2 > i) {
            return "";
          }
          else {
            return biglist.substring(substringstart+1,i);
          }
        }
      }


    };

    return "";

  }


  public static String ReadInputString(String inputfilename) {
    String nextline;
    char ch;
    try (BufferedReader in = new BufferedReader(new FileReader(inputfilename))) {
      String linesofar = in.readLine();
      while ((nextline = in.readLine()) != null) {
        if (Character.isWhitespace(nextline.charAt(0))) {
          linesofar = linesofar + nextline;
        } else {
          break;
        }

      }
      return linesofar;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return "";
  }

  public static ArrayList<RealMatrix> GetVertices(String trianglelist) {
    ArrayList<RealMatrix> outputlist = new ArrayList<RealMatrix>();
    String currentlist;
    String currentvertices;
    String[] currentnumbers = new String[10];
    int i = 1;
    double[][] pointsprematrix = new double[3][10];
    double[] currentintegers = new double[10];

    while ((currentlist = GrabSublist(trianglelist,i)) != "") {
      currentvertices = GrabSublist(currentlist,2);
      for (int j = 0; j < 3; j++) { 
        currentnumbers = GrabSublist(currentvertices,j+1).split(",");
        for (int k = 0; k < 10; k++) {
          currentintegers[k] = Integer.parseInt(currentnumbers[k]);
        }
        pointsprematrix[j] = currentintegers;
      }
      outputlist.add((RealMatrix)new Array2DRowRealMatrix(pointsprematrix));
      i++;
    }

    return outputlist;

  }

  public static void main(String[] args) {

    //ArrayList<RealMatrix> vertexlist = GetVertices(GrabSublist(GrabSublist(ReadInputString(inputFilename),1),1).replaceAll("\\s",""));

    ArrayList<OrderedTriple> pointslist = new ArrayList<OrderedTriple>();
    String trianglelist = GrabSublist(GrabSublist(ReadInputString(inputFilename),1),1).replaceAll("\\s","");
    ArrayList<RealMatrix> outputlist = new ArrayList<RealMatrix>();
    String currentlist;
    String currentvertices;
    String[] currentnumbers = new String[10];
    int i = 1;
    double[] currentintegers = new double[10];

    while ((currentlist = GrabSublist(trianglelist,i)) != "") {
      currentvertices = GrabSublist(currentlist,2);
      outputlist.clear(); 
      for (int j = 0; j < 3; j++) { 
        currentnumbers = GrabSublist(currentvertices,j+1).split(",");
        for (int k = 0; k < 10; k++) {
          currentintegers[k] = Integer.parseInt(currentnumbers[k]);
        }
        outputlist.add((RealMatrix)new Array2DRowRealMatrix(currentintegers));
      }
      pointslist.add(new OrderedTriple(outputlist));
      i++;
    }

    PointsDisplay theseData = new PointsDisplay(pointslist, "TriangleDraw");

  } // end of main()
} // end of class TriangleDraw
