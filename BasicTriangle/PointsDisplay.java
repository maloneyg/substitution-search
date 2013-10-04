import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import org.apache.commons.math3.linear.*;
import com.google.common.collect.*;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PointsDisplay extends JPanel implements ActionListener
{

    private final ImmutableList<BasicPatch> patches;
    private ArrayList<OrderedTriple> data;
    private JButton next;
    private JButton previous;
    private int position;
    public static final int windowSize = 500;

    public PointsDisplay(ImmutableList<BasicPatch> patches, String title)
    {
        this.patches = patches;
        this.position = 0;
        this.data = this.patches.get(position).graphicsDump();

        next = new JButton("next");
        if (position+1 >= patches.size()) {
            next.setEnabled(false);
        } else {
            next.setEnabled(true);
        }
        next.setActionCommand("advance");
        next.setMnemonic(KeyEvent.VK_A);
        next.addActionListener(this);

        previous = new JButton("previous");
        if (position == 0) {
            previous.setEnabled(false);
        } else {
            previous.setEnabled(true);
        }
        previous.setActionCommand("retreat");
        previous.setMnemonic(KeyEvent.VK_B);
        previous.addActionListener(this);

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

        add(previous);
        add(next);
    }

    public void actionPerformed(ActionEvent e) {
        if ("advance".equals(e.getActionCommand())) {
            position++;
            this.data = patches.get(position).graphicsDump();
            if (position+1 == patches.size()) next.setEnabled(false);
            if (position > 0) previous.setEnabled(true);
            this.updateUI();
        } else if ("retreat".equals(e.getActionCommand())) {
            position--;
            this.data = patches.get(position).graphicsDump();
            if (position+1 < patches.size()) next.setEnabled(true);
            if (position == 0) previous.setEnabled(false);
            this.updateUI();
        }
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

        double scaleX = windowSize*0.8 / widthX;
        double scaleY = windowSize*0.8 / widthY;
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

        byte Z = (byte) 0;

        BasicPrototile P0 = BasicPrototile.createBasicPrototile(new int[] { 1, 3, 3 });
        BasicPrototile P1 = BasicPrototile.createBasicPrototile(new int[] { 1, 2, 4 });
        BasicPrototile P2 = BasicPrototile.createBasicPrototile(new int[] { 2, 2, 3 });

        BasicTriangle T2 = P2.place(BytePoint.createBytePoint(new byte[] {Z,Z,Z,Z,Z,Z}),BasicAngle.createBasicAngle(1),false);
        BasicTriangle T1 = P1.place(BytePoint.createBytePoint(new byte[] {Z,Z,Z,Z,Z,Z}),BasicAngle.createBasicAngle(3),true);

        ArrayList<OrderedTriple> triplesList = new ArrayList<OrderedTriple>(3);
        triplesList.add(new OrderedTriple(T1.toArray()));
        triplesList.add(new OrderedTriple(T2.toArray()));

//        PointsDisplay theseData = new PointsDisplay(triplesList, "TriangleDraw");

//        PointsDisplay theseData = new PointsDisplay(patch.graphicsDump(), "TriangleDraw");

    }


} // end of class PointsDisplay
