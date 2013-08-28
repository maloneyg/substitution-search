import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import org.apache.commons.math3.linear.*;

public class PointsDisplay extends JPanel
{
    private ArrayList<OrderedTriple> data;
    public static final int windowSize = 500;

    public PointsDisplay(ArrayList<OrderedTriple> data, String title)
    {
        this.data = data;

        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(this, BorderLayout.CENTER);
        setBackground(Color.WHITE);

        JFrame window = new JFrame(title);
        window.setContentPane(content);
        window.setSize(windowSize,windowSize);
        window.setLocation(10,10);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2;
        g2 = (Graphics2D)g;
        g2.setColor(Color.RED);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // determine maximum and minimum
        double minX = 0.0;
        double maxX = 0.0;
        double minY = 0.0;
        double maxY = 0.0;
        int count = 0;
        for (OrderedTriple t : data)
            {
                ArrayList<OrderedPair> points = t.getPoints();
                for (OrderedPair p : points)
                    {
                        count++;
                        if (count==1)
                            {
                                minX = p.getX();
                                maxX = p.getX();
                                minY = p.getY();
                                maxY = p.getY();
                            }

                        if ( p.getX() < minX )
                            minX = p.getX();
                        else if ( p.getX() > maxX )
                            maxX = p.getX();

                        if ( p.getY() < minY )
                            minY = p.getY();
                        else if ( p.getY() > maxY )
                            maxY = p.getY();
                    }
            }
        
        
        // determine scaling factors
        double widthX = maxX - minX;
        double widthY = maxY - minY;

        double scaleX = windowSize*0.9 / widthX;
        double scaleY = windowSize*0.9 / widthY;
        double scale  = Math.min(scaleX, scaleY);
        //scale = 1.5;
        
        // determine origin
        double offsetX = scaleX*(maxX+minX)/2.0;
        double offsetY = scaleY*(maxY+minY)/2.0;
        
        // plot data
        for (OrderedTriple t : data)
            {
                ArrayList<OrderedPair> points = t.getPoints();
                
                Point2D.Double point1 = new Point2D.Double(scale*points.get(0).getY()-offsetY+windowSize/2, -scale*points.get(0).getX()+offsetX+windowSize/2);
                Point2D.Double point2 = new Point2D.Double(scale*points.get(1).getY()-offsetY+windowSize/2, -scale*points.get(1).getX()+offsetX+windowSize/2);
                Point2D.Double point3 = new Point2D.Double(scale*points.get(2).getY()-offsetY+windowSize/2, -scale*points.get(2).getX()+offsetX+windowSize/2);
//                Point2D.Double point1 = new Point2D.Double(scale*points.get(0).getX()+offsetX, scale*points.get(0).getY()+offsetY);
//                Point2D.Double point2 = new Point2D.Double(scale*points.get(1).getX()+offsetX, scale*points.get(1).getY()+offsetY);
//                Point2D.Double point3 = new Point2D.Double(scale*points.get(2).getX()+offsetX, scale*points.get(2).getY()+offsetY);
                g2.draw(new Line2D.Double(point1,point2));
                g2.draw(new Line2D.Double(point2,point3));
                g2.draw(new Line2D.Double(point1,point3));
                // draw each point
                g2.fillOval((int)point1.getX()-2,(int)point1.getY()-2,5,5);
                g2.fillOval((int)point2.getX()-2,(int)point2.getY()-2,5,5);
                g2.fillOval((int)point3.getX()-2,(int)point3.getY()-2,5,5);
            }
    }


    public static void main(String[] args) {

        BasicPrototile P0 = BasicPrototile.createBasicPrototile(new int[] { 1, 3, 3 });
        System.out.println(P0);

        BasicPrototile P1 = BasicPrototile.createBasicPrototile(new int[] { 1, 2, 4 });
        System.out.println(P1);

        BasicPrototile P2 = BasicPrototile.createBasicPrototile(new int[] { 2, 2, 3 });
        System.out.println(P2);

        BasicTriangle T2 = P2.place(BasicPoint.createBasicPoint(new int[] {0,0,0,0,0,0}),BasicAngle.createBasicAngle(1),false);
        BasicTriangle T1 = P1.place(BasicPoint.createBasicPoint(new int[] {0,0,0,0,0,0}),BasicAngle.createBasicAngle(3),true);

        ArrayList<OrderedTriple> triplesList = new ArrayList<OrderedTriple>(3);
        triplesList.add(new OrderedTriple(T1.toArray()));
        triplesList.add(new OrderedTriple(T2.toArray()));

        PointsDisplay theseData = new PointsDisplay(triplesList, "TriangleDraw");



//        BasicEdge[] edgeList = P1.createSkeleton(//
//                                P1.lengths.get(0).getBreakdown(), //
//                                P1.lengths.get(1).getBreakdown(), //
//                                P1.lengths.get(2).getBreakdown()  //
//                                                );
//        for (BasicEdge e : edgeList) System.out.println(e);


    }


} // end of class PointsDisplay
