package surrogate;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

public class SurrogateUI extends OutputStream implements Runnable {
	private final static String CLOSE_UI_EXIT_SURROGATE = "This will stop the surrogate. Are you sure?";
	private final static String CLOSE_UI_SURROGATE_KEEPS_RUNNING = "This will close the UI, and keep the surrogate running. Are you sure?";
		
	private final JTextArea messageOutput;	
	private final StringBuilder sb;
	public final Surrogate surrogate;
	public volatile boolean initiated;
	public volatile boolean running;
	private boolean closingWindow;
	private String closeString;
	
	public SurrogateUI(Surrogate surrogate){
		sb = new StringBuilder();
		messageOutput = new JTextArea(25, 80);
		initiated = false;
		running = true;
		closingWindow = false;
		this.surrogate = surrogate;
		closeString = CLOSE_UI_SURROGATE_KEEPS_RUNNING;
	}
	
	public void run() {
		JFrame guiFrame = new JFrame();
	    guiFrame.setTitle("Weather Surrogate User Interface");
	    guiFrame.setLocationRelativeTo(null);
	    guiFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    setExitListener(guiFrame);
	    //text panel
		messageOutput.setEditable (false);
		messageOutput.setLineWrap(true);
		DefaultCaret caret = (DefaultCaret)messageOutput.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        //title panel
	    final JPanel titlePanel = new JPanel();
	    JLabel titleLabel = new JLabel("Weather Surrogate: component message overview.");
        titlePanel.add(titleLabel);
	    guiFrame.add(titlePanel, BorderLayout.NORTH);
	    //message panel
	    final JPanel messagePanel = new JPanel();
        messagePanel.add(new JScrollPane (messageOutput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        guiFrame.add(messagePanel, BorderLayout.CENTER);
        //buttons
        final JPanel buttonPanel = new JPanel();
        JButton exitUIButton = new JButton("Exit User Interface");
        setExitUIButtonAction(exitUIButton, guiFrame);
        JButton closeSurrogateButton = new JButton("Stop Surrogate");
        setCloseSurrogateButtonAction(closeSurrogateButton, guiFrame);
        buttonPanel.add(exitUIButton);
        buttonPanel.add(closeSurrogateButton);
        buttonPanel.setVisible(true);
        guiFrame.add(buttonPanel, BorderLayout.SOUTH);
    
        guiFrame.pack();
        guiFrame.setVisible(true);
        
		System.out.println("Starting GUI. @GUI.");
        System.setOut (new PrintStream(this));
		initiated = true;
		while(running){
			
		}
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}

	private void setExitListener(JFrame guiFrame) {
		WindowListener exitListener = new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	            int confirm = JOptionPane.showOptionDialog(
	                 null, closeString, 
	                 "Please confirm.", JOptionPane.YES_NO_OPTION, 
	                 JOptionPane.QUESTION_MESSAGE, null, null, null);
	            if (confirm == 0) {
	            	guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	            	System.out.println("Closing GUI. @GUI.");
	            	closingWindow = true;
	            }
	        }
	    };
	    guiFrame.addWindowListener(exitListener);
	}

	private void setCloseSurrogateButtonAction(JButton closeSurrogateButton, JFrame guiFrame) {
		closeSurrogateButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
            	closeString = CLOSE_UI_EXIT_SURROGATE;
            	guiFrame.dispatchEvent(new WindowEvent(guiFrame, WindowEvent.WINDOW_CLOSING));
            	if(closingWindow){
                	surrogate.userExit = true;
            	} else {
            		closeString = CLOSE_UI_SURROGATE_KEEPS_RUNNING;
            	}
            }
        });
	}

	private void setExitUIButtonAction(JButton exitUIButton, JFrame guiFrame) {
		exitUIButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event){
            	closeString = CLOSE_UI_SURROGATE_KEEPS_RUNNING;
            	guiFrame.dispatchEvent(new WindowEvent(guiFrame, WindowEvent.WINDOW_CLOSING));
            	if(closingWindow){
            		surrogate.userExitUI = true;
            	}
            }
        });
	}

	public void write(int b) throws IOException {
		if(b == '\n'){
			final String text = sb.toString() + "\n";
			messageOutput.append(text);
            sb.setLength(0);
		} else {
			sb.append((char)b);
		}	
	}
}
