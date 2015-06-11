package surrogate;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class SurrogateUI implements Runnable {

	public SurrogateUI(){
		System.out.println("GUI successfully started. @GUI.");
	}
	
	public void run() {
		 JTextArea textArea = new JTextArea (25, 80);

	        textArea.setEditable (false);
		
		JFrame guiFrame = new JFrame("stdout");
	    guiFrame.setTitle("Weather Surrogate");
	    guiFrame.setSize(300,250);
	    
	  //This will center the JFrame in the middle of the screen
        guiFrame.setLocationRelativeTo(null);
        
        Container contentPane = guiFrame.getContentPane ();
        contentPane.setLayout (new BorderLayout ());
        contentPane.add (
            new JScrollPane (
                textArea, 
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED),
            BorderLayout.CENTER);
        guiFrame.pack ();
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
}
