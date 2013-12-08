// This is an improved version of EmptyBoundaryDebugDisplay.
// It is designed to take serialized EmptyBoundaryPatches as input.

// note to self: setBounds(x,y,width,height)

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import org.apache.commons.math3.linear.*;
import com.google.common.collect.*;
import java.io.*;

public class PatchDisplay extends JFrame implements ActionListener
{
    private DebugPanel activePanel;
    public static final int WINDOW_HEIGHT = 600;
    public static final int WINDOW_WIDTH  = 600;

    public PatchDisplay(EmptyBoundaryPatch patch) throws HeadlessException
    {
        activePanel = new DebugPanel((ActionListener)this, patch);
        setContentPane(activePanel);
        
        setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
        setLocation(10,10);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("PatchDisplay");
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if ("advance".equals(e.getActionCommand()))
            {
                activePanel.position++;

                // prevent position from getting out of range
                if ( activePanel.position > activePanel.frames.size() - 1 )
                    activePanel.position = activePanel.frames.size() - 1;

                activePanel.updatePosition();
                if ( activePanel.position+1 == activePanel.frames.size() )
                    activePanel.next.setEnabled(false);
                if ( activePanel.position > 0)
                    activePanel.previous.setEnabled(true);
            }
        else if ("retreat".equals(e.getActionCommand()))
            {
                activePanel.position--;

                // prevent position from getting out of range
                if ( activePanel.position < 0 )
                    activePanel.position = 0;

                activePanel.updatePosition();
                if (activePanel.position+1 < activePanel.frames.size())
                    activePanel.next.setEnabled(true);
                if (activePanel.position == 0)
                    activePanel.previous.setEnabled(false);
            }
        else if ("play".equals(e.getActionCommand()))
            activePanel.playing = true;
        else if ("stop".equals(e.getActionCommand()))
            activePanel.playing = false;
        else if ("reverse".equals(e.getActionCommand()))
            {
                if (activePanel.forward == true)
                    activePanel.forward = false;
                else
                    activePanel.forward = true;
            }
        else if ("rewind".equals(e.getActionCommand()))
            activePanel.position = 0;
        else if ("animation".equals(e.getActionCommand()))
            {
                if ( activePanel.playing == true )
                    {
                        if ( activePanel.forward == true )
                            activePanel.next.doClick();
                        else if ( activePanel.forward == false )
                            activePanel.previous.doClick();
                }
            }    
        activePanel.updateUI();
        }

    public static class DebugPanel extends JPanel
    {
        // individual components
        private JButton next;
        private JButton previous;
        private int position;
        private JTextArea currentIndexArea;
        private JTextArea messageArea;
        private String positionString;

        // animation controls
        private boolean playing = false;
        private boolean forward = true;
        private static final int ANIMATION_DELAY = 1; // ms
        private static final int FRAME_INTERVAL = 1;  // every redraw, advance or retreat by this many frames
        private JButton playButton;
        private JButton stopButton;
        private JButton reverseButton;
        private JButton rewindButton;
        private javax.swing.Timer animationTimer;

        // geometry data
        private EmptyBoundaryPatch patch;
        public static final int MAX_FRAMES = 3000; // up to this many frames will be rendered
        private ArrayList<PatchDisplay.DebugFrame> frames;
        private EmptyBoundaryWorkUnitFactory factory = EmptyBoundaryWorkUnitFactory.createEmptyBoundaryWorkUnitFactory();

        // data related to the current frame
        private DebugFrame currentFrame;
        private ArrayList<OrderedTriple> data;
        
        // constants
        private static final int WINDOW_HEIGHT = PatchDisplay.WINDOW_HEIGHT;
        private static final int WINDOW_WIDTH = PatchDisplay.WINDOW_WIDTH;

        public DebugPanel(ActionListener parentFrame, EmptyBoundaryPatch patch)
        {
            // pre-compute the data for this panel
            this.patch = patch;
            frames = new ArrayList<PatchDisplay.DebugFrame>();
            System.out.println();
            patch.debugSolve(frames);
            System.out.println("done.");
            
            // set the current frame
            if ( frames.size() > 0 )
                {
                    currentFrame = frames.get(0);
                    data = currentFrame.getPatch().graphicsDump();
                }

            // general parameters for this JPanel
            setLayout(null);
            setBackground(Color.WHITE);
            setSize(PatchDisplay.WINDOW_WIDTH, PatchDisplay.WINDOW_HEIGHT);

            // individual components
            positionString = "---";
            currentIndexArea = new JTextArea(positionString);
            currentIndexArea.setFont(new Font("SansSerif", Font.PLAIN, 10));
            currentIndexArea.setEditable(false);
            currentIndexArea.setBounds(5,50,80,15);
            add(currentIndexArea);

            messageArea = new JTextArea("---");
            messageArea.setFont(new Font("SansSerif", Font.PLAIN, 10));
            messageArea.setEditable(false);
            messageArea.setLineWrap(true);
            int messageX = 0;
            if ( Preinitializer.MY_TILE == 3 )
                messageX = 10;
            else if ( Preinitializer.MY_TILE == 4 )
                messageX = 300;
            else
                messageX = 10;
            messageArea.setBounds(messageX,75,250,200);
            add(messageArea);

            next = new JButton("next");
            next.setEnabled(true);
            next.setActionCommand("advance");
            next.setMnemonic(KeyEvent.VK_A);
            next.addActionListener(parentFrame);
            next.setBounds(120,10,90,20);
            add(next);

            playButton = new JButton("play");
            playButton.setEnabled(true);
            playButton.setActionCommand("play");
            playButton.addActionListener(parentFrame);
            playButton.setBounds(220,10,90,20);
            add(playButton);

            stopButton = new JButton("stop");
            stopButton.setEnabled(true);
            stopButton.setActionCommand("stop");
            stopButton.addActionListener(parentFrame);
            stopButton.setBounds(310,10,90,20);
            add(stopButton);
            reverseButton = new JButton("reverse");
            reverseButton.setEnabled(true);
            reverseButton.setActionCommand("reverse");
            reverseButton.addActionListener(parentFrame);
            reverseButton.setBounds(400,10,90,20);
            add(reverseButton);

            rewindButton = new JButton("rewind");
            rewindButton.setEnabled(true);
            rewindButton.setActionCommand("rewind");
            rewindButton.addActionListener(parentFrame);
            rewindButton.setBounds(490,10,90,20);
            add(rewindButton);

            previous = new JButton("previous");
            if (position == 0)
                previous.setEnabled(false);
            else 
                previous.setEnabled(true);
            previous.setActionCommand("retreat");
            previous.setMnemonic(KeyEvent.VK_B);
            previous.addActionListener(parentFrame);
            previous.setBounds(10,10,90,20);
            add(previous);

            animationTimer = new javax.swing.Timer(ANIMATION_DELAY,parentFrame);
            animationTimer.setActionCommand("animation");
            animationTimer.start();
        }

        public void updatePosition()
        {
            DebugFrame thisFrame = frames.get(position);
            data = thisFrame.getPatch().graphicsDump();
            messageArea.setText( thisFrame.getMessage() );
            currentIndexArea.setText( (position+1) + "/" + frames.size() );
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

            double scaleX = WINDOW_HEIGHT*0.8 / widthX;
            double scaleY = WINDOW_WIDTH*0.8 / widthY;
            double scale  = Math.min(scaleX, scaleY);
            //scale = 1.5;

            // determine origin
            double offsetX = scaleX*(maxX+minX)/2.0;
            double offsetY = scaleY*(maxY+minY)/2.0;

            // plot data
            for (OrderedTriple t : data)
                {
                    ArrayList<OrderedPair> points = t.getPoints();

                    Point2D.Double point1 = new Point2D.Double(scale*points.get(0).getY()-offsetY+WINDOW_WIDTH/2, -scale*points.get(0).getX()+offsetX+WINDOW_HEIGHT/2);
                    Point2D.Double point2 = new Point2D.Double(scale*points.get(1).getY()-offsetY+WINDOW_WIDTH/2, -scale*points.get(1).getX()+offsetX+WINDOW_HEIGHT/2);
                    Point2D.Double point3 = new Point2D.Double(scale*points.get(2).getY()-offsetY+WINDOW_WIDTH/2, -scale*points.get(2).getX()+offsetX+WINDOW_HEIGHT/2);
                    g2.draw(new Line2D.Double(point1,point2));
                    g2.draw(new Line2D.Double(point2,point3));
                    g2.draw(new Line2D.Double(point1,point3));
                    // draw each point
                    g2.fillOval((int)point1.getX()-2,(int)point1.getY()-2,5,5);
                    g2.fillOval((int)point2.getX()-2,(int)point2.getY()-2,5,5);
                    g2.fillOval((int)point3.getX()-2,(int)point3.getY()-2,5,5);
                }
        }
    }

    public static class DebugFrame
    {
        private ImmutablePatch patch;
        private String message;

        public DebugFrame(ImmutablePatch patch, String message)
        {
            this.patch = patch;
            this.message = message;
        }

        public ImmutablePatch getPatch()
        {
            return patch;
        }

        public String getMessage()
        {
            return message;
        }
    }

    public static void main(String[] args)
    {
        File storageDirectory = new File(Preinitializer.SERIALIZATION_DIRECTORY);
        File[] fileList = storageDirectory.listFiles();
        File checkpointFile = null;
        // select the first appropriate file in the directory
        for ( File f : fileList )
            if ( f.isFile() )
                if ( f.getName().startsWith("P") || f.getName().startsWith("N") )
                    {
                        checkpointFile = f;
                        break;
                    }
        if ( checkpointFile != null )
            {
                System.out.println("Initializing debug PatchDisplay with " + checkpointFile.getName() + "...");
                try
                    {
                        String filename = Preinitializer.SERIALIZATION_DIRECTORY + "/" + checkpointFile.getName();
                        FileInputStream fileIn = new FileInputStream(filename);
                        ObjectInputStream in = new ObjectInputStream(fileIn);
                        EmptyBoundaryPatch patch = ((EmptyBoundaryPatch)in.readObject());
                        PatchDisplay display = new PatchDisplay(patch);
                    }
                catch (HeadlessException e)
                    {
                        System.out.println("error: no X11 display");
                    }
                catch (Exception e)
                    {
                        e.printStackTrace();
                    }
            }
        else
            System.out.println("No valid files found.");
    }
}
