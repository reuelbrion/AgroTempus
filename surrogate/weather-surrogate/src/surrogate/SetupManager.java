package surrogate;

import  java.util.prefs.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SetupManager implements Runnable{
	final static String SURROGATE_ACTIVE = "surrogate-active";
	private Preferences prefs;
	public volatile boolean ready;
	
	SetupManager(){
		ready = false;
		prefs = Preferences.systemNodeForPackage(getClass());
	}
	
	public void run() {
		boolean surrogateActive = prefs.getBoolean(SURROGATE_ACTIVE, false);
		if(!surrogateActive){
			System.out.println("No setup data found. Running setup. @Setup Manager.");    
			runSetup();
		}

				
	}

	private void runSetup() {
		createSetupUI();		
		prefs.putBoolean(SURROGATE_ACTIVE, true);
	}

	private void createSetupUI() {
		JFrame frame = new JFrame("Surrogate setup - please enter details.");
		frame.setSize(400, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.add(panel);
		panel.setLayout(null);

		JLabel surrogateLocationLabel = new JLabel("Surrogate Location");
		surrogateLocationLabel.setBounds(10, 10, 180, 25);
		panel.add(surrogateLocationLabel);
		JTextField surrogateLocationText = new JTextField(20);
		surrogateLocationText.setBounds(200, 10, 160, 25);
		panel.add(surrogateLocationText);
		JLabel surrogateCountryCodeLabel = new JLabel("Country 2 letter code");
		surrogateCountryCodeLabel.setBounds(10, 40, 180, 25);
		panel.add(surrogateCountryCodeLabel);
		JTextField surrogateCountryCodeText = new JTextField(2);
		surrogateCountryCodeText.setBounds(200, 40, 160, 25);
		panel.add(surrogateCountryCodeText);
		JButton saveButton = new JButton("Save settings");
		saveButton.setBounds(10, 80, 80, 25);
		panel.add(saveButton);
		frame.setVisible(true);
	}
}
