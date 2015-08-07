package surrogate;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SetupManager implements Runnable{
	final static String SURROGATE_ACTIVE = "surrogate-active";
	
	public volatile boolean ready;
	JSONObject setupData;
	boolean surrogateActive;
	
	SetupManager(){
		ready = false;
		loadSetupData();
	}
	
	private void loadSetupData() {
		File file = new File("surrogate.ini");
		JSONObject obj = null;
		JSONParser parser;
		if(!file.exists()){ 
			createSetupData(file);
		} else {
			parser = new JSONParser();
			try {
				obj = (JSONObject)parser.parse(new FileReader(file));
			} catch (Exception e) {
				System.out.println("Couldn't create parser for setup data. Exitting. @Setup Manager."); 
				e.printStackTrace();
				System.exit(1);
			}
			if(obj.containsKey("location") && obj.containsKey("countrycode")){
				if(obj.get("location") == null || obj.get("countrycode") == null){
					surrogateActive = false;
				} else {
					surrogateActive = true;
				}
				setupData = obj;
			} else {
				System.out.println("Incorrect setup file. Creating new file. @Setup Manager."); 
				createSetupData(file);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void createSetupData(File file) {
		setupData = new JSONObject();
		setupData.put("location", null);
		setupData.put("countrycode", null);
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(setupData.toJSONString());
            fileWriter.flush();
            fileWriter.close();
            System.out.println("Successfully created new setup file: " + file.getPath() + "@Setup Manager.");
        } catch (IOException e) {
        	System.out.println("Could not create setup file. Exitting. @Setup Manager.");  
            e.printStackTrace();
            System.exit(1);
        } 
	}

	private void saveSetupData() {
		File file = new File("surrogate.ini");
		try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(setupData.toJSONString());
            fileWriter.flush();
            fileWriter.close();
            System.out.println("Successfully created new setup file: " + file.getPath() + "@Setup Manager.");
        } catch (IOException e) {
        	System.out.println("Could not create setup file. Exitting. @Setup Manager.");  
            e.printStackTrace();
            System.exit(1);
        } 
	}

	public void run() {
		if(!surrogateActive){
			System.out.println("No setup data found. Running setup. @Setup Manager.");    
			runSetup();
		} else {
			ready = true;
		}
	}

	private void runSetup() {
		createSetupUI();		
	}

	private void createSetupUI() {
		JFrame frame = new JFrame("Surrogate setup - please enter details.");
		frame.setSize(400, 200);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

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
		saveButton.setBounds(10, 80, 120, 25);
		panel.add(saveButton);
		saveButton.addActionListener(new ActionListener(){
				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent event) {
					//TODO: check input for validity
					setupData.put("location", surrogateLocationText.getText());
					setupData.put("countrycode", surrogateCountryCodeText.getText());
					saveSetupData();
					ready = true;
					frame.dispose();
				}
			}
		);		
		frame.setVisible(true);
	}
}
