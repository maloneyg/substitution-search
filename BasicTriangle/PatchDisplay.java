// This is an improved version of EmptyBoundaryDebugDisplay.
// It is designed to take serialized EmptyBoundaryPatches as input.

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
    public static final int WINDOW_HEIGHT = 500;
    public static final int WINDOW_WIDTH  = 500;

    public PatchDisplay(EmptyBoundaryPatch patch) throws HeadlessException
    {
        activePanel = new DebugPanel(patch);
        setContentPane(activePanel);
        
        setSize(WINDOW_WIDTH,WINDOW_HEIGHT);
        setLocation(10,10);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("PatchDisplay");
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
    }

    public static class DebugPanel extends JPanel
    {
        // individual components
        private JButton next;
        private JButton previous;
        private int position;
        private JTextArea messageArea;

        // geometry data
        private EmptyBoundaryPatch patch;
        public static final int MAX_FRAMES = 3000; // up to this many frames will be rendered
        private ArrayList<PatchDisplay.DebugFrame> frames;
        private EmptyBoundaryWorkUnitFactory factory = EmptyBoundaryWorkUnitFactory.createEmptyBoundaryWorkUnitFactory();

        // data related to the current frame
        private DebugFrame currentFrame;
        private ArrayList<OrderedTriple> data;
        
        public DebugPanel(EmptyBoundaryPatch patch)
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
            messageArea = new JTextArea("Patch " + patch.hashCode());
            messageArea.setFont(new Font("SansSerif", Font.PLAIN, 10));
            messageArea.setEditable(false);
            messageArea.setLineWrap(true);
            messageArea.setBounds((Preinitializer.MY_TILE==4)? 10 : 300,75,200,150);
            add(messageArea);
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
        checkpointFile = new File("P1327775369.chk");
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
