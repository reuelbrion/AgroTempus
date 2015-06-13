package surrogate;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SurrogateUI extends OutputStream implements Runnable {
	private final JTextArea messageOutput;	
	private final StringBuilder sb;
	public final Surrogate surrogate;
	public volatile boolean initiated;
	
	public SurrogateUI(Surrogate surrogate){
		sb = new StringBuilder();
		messageOutput = new JTextArea(25, 80);
		messageOutput.setEditable (false);
		initiated = false;
		this.surrogate = surrogate;
	}
	
	public void run() {
		JFrame guiFrame = new JFrame();
	    guiFrame.setTitle("Weather Surrogate");
	    guiFrame.setSize(300,250);
	    guiFrame.setLocationRelativeTo(null);
	    guiFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        Container contentPane = guiFrame.getContentPane();
        contentPane.add(new JScrollPane (messageOutput, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        JButton exitUIButton = new JButton("Exit UI");
        JButton closeSurrogateButton = new JButton("Stop surrogate");
        
        //contentPane.setLayout(new BorderLayout ());
        
        contentPane.add(exitUIButton,BorderLayout.SOUTH);
        contentPane.add(closeSurrogateButton,BorderLayout.SOUTH);
        
        guiFrame.pack ();
        guiFrame.setVisible(true);
        
        System.setOut (new PrintStream(this));
		System.out.println("GUI successfully started. @GUI.");
		initiated = true;
        /*
        //Options for the JComboBox 
        String[] fruitOptions = {"Apple", "Apricot", "Banana"
                ,"Cherry", "Date", "Kiwi", "Orange", "Pear", "Strawberry"};
        
        //Options for the JList
        String[] vegOptions = {"Asparagus", "Beans", "Broccoli", "Cabbage"
                , "Carrot", "Celery", "Cucumber", "Leek", "Mushroom"
                , "Pepper", "Radish", "Shallot", "Spinach", "Swede"
                , "Turnip"};
        
        //The first JPanel contains a JLabel and JCombobox
        final JPanel comboPanel = new JPanel();
        JLabel comboLbl = new JLabel("Fruits:");
        JComboBox fruits = new JComboBox(fruitOptions);
        
        comboPanel.add(comboLbl);
        comboPanel.add(fruits);
        
        //Create the second JPanel. Add a JLabel and JList and
        //make use the JPanel is not visible.
        final JPanel listPanel = new JPanel();
        listPanel.setVisible(false);
        JLabel listLbl = new JLabel("Vegetables:");
        JList vegs = new JList(vegOptions);
        vegs.setLayoutOrientation(JList.HORIZONTAL_WRAP);
          
        listPanel.add(listLbl);
        listPanel.add(vegs);
        
        JButton vegFruitBut = new JButton( "Fruit or Veg");
        
        //The ActionListener class is used to handle the
        //event that happens when the user clicks the button.
        //As there is not a lot that needs to happen we can 
        //define an anonymous inner class to make the code simpler.
        vegFruitBut.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent event)
            {
               //When the fruit of veg button is pressed
               //the setVisible value of the listPanel and
               //comboPanel is switched from true to 
               //value or vice versa.
               listPanel.setVisible(!listPanel.isVisible());
               comboPanel.setVisible(!comboPanel.isVisible());

            }
        });
        
        //The JFrame uses the BorderLayout layout manager.
        //Put the two JPanels and JButton in different areas.
        guiFrame.add(comboPanel, BorderLayout.NORTH);
        guiFrame.add(listPanel, BorderLayout.CENTER);
        guiFrame.add(vegFruitBut,BorderLayout.SOUTH);
        
        //make sure the JFrame is visible
        guiFrame.setVisible(true);
        */
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
