import org.apache.commons.math3.linear.*;
import java.util.*;

public class OrderedTriple
{
    private int N = Initializer.N;
    private ArrayList<RealMatrix> data;
    private ArrayList<OrderedPair> points;

    // access to OrderedTriples through static factory method only
    public OrderedTriple(ArrayList<RealMatrix> data)
    {
        this.data = data;
        points = new ArrayList<OrderedPair>();
        for (RealMatrix m : data)
            points.add( projectTo2D(m) );
    }

    public static OrderedTriple getOrderedTriple(String inputString)
    {
        // ignore strings that do not contain any numbers
        if ( ! inputString.matches(".*\\d.*") )
            {
                //System.out.println(inputString + " contains no numbers!");
                return null;
            }
    
        // locate breaks
        //System.out.println(inputString);
        ArrayList<Integer> breaks = new ArrayList<Integer>();
        for (int i=0; i < inputString.length() - 1; i++)
            {
                String currentWindow = inputString.substring(i,i+2);
                if ( currentWindow.equals("},") )
                    {
                        breaks.add(i);
                        i++;
                    }
            }
        if ( breaks.size() == 0 )
            {
                System.out.println("Invalid number of entries.");
                return null;
            }

        // create new triple object
        int currentField = 0;
        int currentIndex = breaks.get(0);
        ArrayList<Integer> currentNumbers = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> numbers = new ArrayList<ArrayList<Integer>>();
        String currentNumber = "";
        for (int i=0; i < inputString.length(); i++)
            {
                String currentCharacter = inputString.substring(i,i+1);
                if ( i == currentIndex || i == inputString.length() - 1 )
                    {
                        currentNumbers.add( Integer.valueOf(currentNumber) );
                        numbers.add(currentNumbers);
                        //System.out.println(currentNumbers);
                        currentNumbers = new ArrayList<Integer>();
                        currentNumber = "";
                        currentField++;
                        if (currentField < breaks.size())
                            currentIndex = breaks.get(currentField);
                        i++;
                        continue;
                    }
                else if ( Character.isDigit(currentCharacter.charAt(0)) || currentCharacter.equals("-") )
                    currentNumber = currentNumber + currentCharacter;
                else if ( currentCharacter.equals(",") )
                    {
                        currentNumbers.add( Integer.valueOf(currentNumber) );
                        currentNumber = "";
                    }
            }

        // parse numbers into matrix format
        int count=0;
        ArrayList<RealMatrix> thisData = new ArrayList<RealMatrix>();
        for (ArrayList<Integer> currentArray : numbers)
            {
                if ( currentArray.size() != 10 )
                    continue;
                count++;
                double[] temp = new double[currentArray.size()];
                for (int i=0; i < 10; i++)
                    temp[i] = (double)currentArray.get(i);
                //System.out.println(temp);
                thisData.add( new Array2DRowRealMatrix(temp) );
                if ( count==3 )
                    break;
            }
        if ( count != 3 )
            {
                System.out.println("unexpected number of numbers");
                return null;
            }
        //System.out.println(thisData);
        OrderedTriple newTriple = new OrderedTriple(thisData);
        return newTriple;
    }

    // projects the vector to R2
    private OrderedPair projectTo2D(RealMatrix vector)
    {
        // create conversion matrix
        double[][] conversionPreMatrix = new double[2][N-1];
        for (int col=0; col < N-1; col++)
            {
                conversionPreMatrix[0][col]=Math.sin( Math.PI*col / ((double)N) );
                conversionPreMatrix[1][col]=Math.cos( Math.PI*col / ((double)N) );
            }
        RealMatrix conversionMatrix = new Array2DRowRealMatrix(conversionPreMatrix);
        RealMatrix result = conversionMatrix.multiply(vector);
        return new OrderedPair(result.getEntry(0,0), result.getEntry(1,0));
    }

    public String toString()
    {
        String returnString = data.toString() + "\n";
        returnString = returnString + points.toString() + "\n";
        return returnString;
    }

    public String toTripleString()
    {
        String returnString = "";
        for (OrderedPair p : points)
            returnString = returnString + p.getX() + "," + p.getY() + "\n";
        return returnString;
    }

    public ArrayList<OrderedPair> getPoints()
    {
        return points;
    }
}
