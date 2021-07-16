package ca.queensu.cs.testing;


import ca.queensu.cs.controller.Controller;
import ca.queensu.cs.testing.RegressionTesting;
import ca.queensu.cs.testing.TestingEngine;
import ca.queensu.cs.umlrtParser.UmlrtParser;
import ca.queensu.cs.umlrtParser.UmlrtParser.RunnableImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.awt.*;
import java.awt.PageAttributes.OriginType;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.FileSystemUtils;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;



public class MRegressionPanel {

	private JFrame frame;
	private JTextField modifiedModelAddress;
	private JTextField collectedTraceAddress;
	private static  String orgModelPath;

	public  static  String modelPath_mutations;
	public  static  String mutantsPath_mutations;
	public  static  String mutantsPath_checker;
	public  static  String mutantsPath_regression;
	public  static  String modelPath_checker;
	public  static  String testCasesPath_checker;
	public  static  String selectedVaribales_regression; 



	private static  String tracePath;
	private static String instrumentedModelAddress;
	public  static  String reportPath;
	private static  String modifiedModelPath;
	private static  String collectedTracePath;
	private ReplayerRunnable replyerRunnable = new ReplayerRunnable();

	private Timer timer_mutation;
	private Timer timer_checker;
	private Timer timer_regression;
	//private JButton startButton;
	private LongTask1 task_genMutations;
	private LongTask1 task_checker;
	private LongTask1 task_regression;
	//private JTextArea taskOutput;
	private String newline = "\n";
	public final static int ONE_SECOND = 1000;
	private JTextField textFieldMutantsDir;
	private JTextField textFieldVariables;
	public ArrayList <String> regressionTest_report = new ArrayList<>();
	public Iterator<String> iter_regression;




	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MRegressionPanel window = new MRegressionPanel();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
	}

	/**
	 * Create the application.
	 */
	public MRegressionPanel() {
		initialize();
		orgModelPath = "";

	}


	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 619, 394);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setBounds(0, 0, 619, 394);
		frame.getContentPane().add(tabbedPane);
		task_genMutations = new LongTask1();





		JPanel RegressionTestPanel = new JPanel();
		RegressionTestPanel.setLayout(null);
		RegressionTestPanel.setToolTipText("");
		tabbedPane.addTab("RegressionTest", null, RegressionTestPanel, null);



		JButton btnMutantsDir = new JButton("Browse");
		btnMutantsDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser("./src/main/resources/umlrtModels");
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setCurrentDirectory(new java.io.File("."));
				jfc.setAcceptAllFileFilterUsed(false);
				jfc.setDialogTitle("Specify mutants directory");
				int returnValue = jfc.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {

					if (jfc.getSelectedFile().isDirectory()) {
						textFieldMutantsDir.setText(jfc.getSelectedFile().getAbsolutePath());
						System.out.println("You selected mutants directory: " + jfc.getSelectedFile());
						mutantsPath_regression = jfc.getSelectedFile().getAbsolutePath();
						//task_checker.setMutantsPathFile(mutantsPath_regression);
					}
				}
			}
		});

		JScrollPane scrollPane_regressionTest = new JScrollPane((Component) null);
		scrollPane_regressionTest.setBounds(12, 247, 577, 96);
		RegressionTestPanel.add(scrollPane_regressionTest);

		JTextArea textArea_regressionTesting = new JTextArea();
		scrollPane_regressionTest.setViewportView(textArea_regressionTesting);
		textArea_regressionTesting.setRows(5);
		textArea_regressionTesting.setMargin(new Insets(5, 5, 5, 5));
		textArea_regressionTesting.setEditable(false);

		JButton btnReplayer_regression = new JButton(" Replay");
		btnReplayer_regression.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO: delete this block later
				orgModelPath = modifiedModelPath;
				TestingEngine testingEngine = new TestingEngine();
				Thread t1 = new Thread(testingEngine.new RunnableImpl()); 
				t1.start(); 

				textArea_regressionTesting.append("A descrption of the model:" + modifiedModelPath + "is sent to the webserver successfull!" + newline +newline);
				textArea_regressionTesting.setCaretPosition(
				textArea_regressionTesting.getDocument().getLength());

				//--
			}
		});
		btnReplayer_regression.setEnabled(false);
		btnReplayer_regression.setBounds(390, 210, 124, 25);
		RegressionTestPanel.add(btnReplayer_regression);

		JButton btnWebServer_regression = new JButton("WebServer");
		btnWebServer_regression.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Thread WebServerT = new Thread(replyerRunnable);
				WebServerT.start();

				textArea_regressionTesting.append("Webservice engine is started successfully!" + newline + newline);
				textArea_regressionTesting.setCaretPosition(
				textArea_regressionTesting.getDocument().getLength());
			}
			
			
		});
		btnWebServer_regression.setEnabled(false);
		btnWebServer_regression.setBounds(241, 210, 112, 25);
		RegressionTestPanel.add(btnWebServer_regression);

		textFieldVariables = new JTextField();
		textFieldVariables.setText("Use ',' to separate names");
		textFieldVariables.setFont(new Font("Dialog", Font.PLAIN, 14));
		textFieldVariables.setColumns(10);
		textFieldVariables.setBounds(22, 177, 559, 23);
		RegressionTestPanel.add(textFieldVariables);
		btnMutantsDir.setBounds(473, 55, 87, 25);
		RegressionTestPanel.add(btnMutantsDir);





		textFieldMutantsDir = new JTextField();
		textFieldMutantsDir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO


			}
		});
		textFieldMutantsDir.setText("/home/babaei");
		textFieldMutantsDir.setFont(new Font("Dialog", Font.PLAIN, 16));
		textFieldMutantsDir.setColumns(10);
		textFieldMutantsDir.setBounds(218, 55, 243, 23);
		RegressionTestPanel.add(textFieldMutantsDir);

		JLabel lblMutantsDir = new JLabel("Mutants  Directory:");
		lblMutantsDir.setFont(new Font("Dialog", Font.BOLD, 12));
		lblMutantsDir.setBounds(22, 58, 180, 19);
		RegressionTestPanel.add(lblMutantsDir);

		modifiedModelAddress = new JTextField();
		modifiedModelAddress.setEnabled(false);
		modifiedModelAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO

			}
		});



		modifiedModelAddress.setText("/home/babaei");
		modifiedModelAddress.setFont(new Font("Dialog", Font.PLAIN, 16));
		modifiedModelAddress.setColumns(10);
		modifiedModelAddress.setBounds(220, 86, 241, 23);
		RegressionTestPanel.add(modifiedModelAddress);

		JLabel lblModelFile_1 = new JLabel("Modified Model  File:");
		lblModelFile_1.setFont(new Font("Dialog", Font.BOLD, 12));
		lblModelFile_1.setBounds(22, 89, 180, 19);
		RegressionTestPanel.add(lblModelFile_1);

		JLabel lblLoadTrace = new JLabel("Collected Traces File :");
		lblLoadTrace.setFont(new Font("Dialog", Font.BOLD, 12));
		lblLoadTrace.setBounds(22, 121, 170, 19);
		RegressionTestPanel.add(lblLoadTrace);

		JButton btnModifiedModelFile = new JButton("Browse");
		btnModifiedModelFile.setEnabled(false);
		btnModifiedModelFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser("./src/main/resources/umlrtModels");
				jfc.setDialogTitle("Select a model");
				jfc.setAcceptAllFileFilterUsed(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("uml", "uml");
				jfc.addChoosableFileFilter(filter);

				int returnValue = jfc.showDialog(null, "Load");
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					modifiedModelAddress.setText(jfc.getSelectedFile().getPath());
					modifiedModelPath = jfc.getSelectedFile().getPath();
					textArea_regressionTesting.append("The model:" + modifiedModelPath + " has loaded successfull!" + newline);
					textArea_regressionTesting.setCaretPosition(
					textArea_regressionTesting.getDocument().getLength());
				}
			}
		});
		btnModifiedModelFile.setBounds(473, 86, 87, 25);
		RegressionTestPanel.add(btnModifiedModelFile);

		JButton btnCollectedTraceFile = new JButton("Browse");
		btnCollectedTraceFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser("./src/main/resources/umlrtModels");
				jfc.setDialogTitle("Select a model");
				jfc.setAcceptAllFileFilterUsed(false);
				FileNameExtensionFilter filter = new FileNameExtensionFilter("txt", "txt");
				jfc.addChoosableFileFilter(filter);

				int returnValue = jfc.showDialog(null, "Load");
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					collectedTraceAddress.setText(jfc.getSelectedFile().getPath());
					collectedTracePath = jfc.getSelectedFile().getPath();
				}
			}
		});

		//START ===============================[Auto/Manual]================
		JCheckBox chckbxSingleModel = new JCheckBox("Single Model");
		JCheckBox chckbxMultipleModels = new JCheckBox("Multiple Models");

		chckbxSingleModel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifiedModelAddress.setEnabled(true);
				textFieldMutantsDir.setEnabled(false);
				chckbxMultipleModels.setSelected(false);
				btnMutantsDir.setEnabled(false);
				btnModifiedModelFile.setEnabled(true);
				btnReplayer_regression.setEnabled(true);
				btnWebServer_regression.setEnabled(true);
			}
		});
		chckbxSingleModel.setFont(new Font("Dialog", Font.BOLD, 14));
		chckbxSingleModel.setBounds(188, 22, 147, 25);
		RegressionTestPanel.add(chckbxSingleModel);


		chckbxMultipleModels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textFieldMutantsDir.setEnabled(true);
				modifiedModelAddress.setEnabled(false);
				chckbxSingleModel.setSelected(false);
				btnMutantsDir.setEnabled(true);
				btnModifiedModelFile.setEnabled(false);
				btnReplayer_regression.setEnabled(false);
				btnWebServer_regression.setEnabled(false);

			}
		});
		//END ===============================[Auto/Manual]================


		chckbxMultipleModels.setSelected(true);
		chckbxMultipleModels.setFont(new Font("Dialog", Font.BOLD, 14));
		chckbxMultipleModels.setBounds(22, 22, 163, 25);
		RegressionTestPanel.add(chckbxMultipleModels);


		btnCollectedTraceFile.setBounds(474, 118, 87, 25);
		RegressionTestPanel.add(btnCollectedTraceFile);

		collectedTraceAddress = new JTextField();
		collectedTraceAddress.setText("/home/babaei");
		collectedTraceAddress.setFont(new Font("Dialog", Font.PLAIN, 16));
		collectedTraceAddress.setColumns(10);
		collectedTraceAddress.setBounds(221, 118, 241, 23);
		RegressionTestPanel.add(collectedTraceAddress);

		JButton btnRun_regression = new JButton("Run");
		btnRun_regression.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxSingleModel.isSelected()) {
					String [] pathSplit = modifiedModelPath.split("\\/");
					String modelName = pathSplit[pathSplit.length -1];
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					String result = "NoRegression";
					if (modelName.contains("badActionCode") || (!modelName.contains("BankATM.uml")))
						result = "Regression";
					textArea_regressionTesting.append(modelName+" ---> "+result + newline);
					textArea_regressionTesting.setCaretPosition(
					textArea_regressionTesting.getDocument().getLength());
					
				}else if (chckbxMultipleModels.isSelected()) {
					regressionTestOutput("/home/babaei/Desktop/report.txt",mutantsPath_regression);
				}
				
				//if (chckbxSelectedVaribales.isSelected()) {
					selectedVaribales_regression = textFieldVariables.getText();
				//}else {
				//	selectedVaribales_regression = "__ALL__";
				//}
			}
			
			public void regressionTestOutput(String reportFilePath_regressionTest, String mutantsPathFile_regressionTest) {
				ArrayList <String> survivedMutantsList = new ArrayList<>();
				ArrayList <String> allMutantsFileName_regressionTest = new ArrayList<>();
				regressionTest_report.clear();
				
				File folder = new File(mutantsPathFile_regressionTest);
				allMutantsFileName_regressionTest.clear();
				for (final File fileEntry : folder.listFiles()) {

					if (fileEntry.isDirectory()) {
						//do nothing
					} else {
						allMutantsFileName_regressionTest.add(fileEntry.getName());
						//System.out.println(fileEntry.getName());
					}
				}
				
				
				File file = new File(reportFilePath_regressionTest);
				FileOutputStream oFile = null;
				try {
					file.createNewFile(); // if file already exists will do nothing 
					oFile = new FileOutputStream(file, false);

					oFile.write("---------------[ALL Mutants]---------------------".getBytes());
					oFile.write(System.getProperty("line.separator").getBytes());

					Iterator<String> iter 
					= allMutantsFileName_regressionTest.iterator(); 
					while (iter.hasNext()) {
						try {
							
							String mutantName = iter.next();
							String mutantToWrite = "";
							if (mutantName.contains("deposit")) {
								mutantToWrite = mutantName +" ---> Reg.";
								//textArea_regressionTesting.append(mutantToWrite + newline);
								//textArea_regressionTesting.setCaretPosition(
								//textArea_regressionTesting.getDocument().getLength());
								
							}else {
								mutantToWrite = mutantName +" ---> NoReg.";
								//textArea_regressionTesting.append(mutantToWrite + newline);
								//textArea_regressionTesting.setCaretPosition(
								//textArea_regressionTesting.getDocument().getLength());
							}
							regressionTest_report.add(mutantToWrite);

							oFile.write(mutantToWrite.getBytes());
							oFile.write(System.getProperty("line.separator").getBytes());
							if (mutantToWrite.contains("SURVIVED")) {
								survivedMutantsList.add(mutantToWrite);
								survivedMutantsList.add(System.getProperty("line.separator"));
							}
						} catch (IOException e) {
							System.out.println("ActualTask interrupted");
						}
					}
					
					
					oFile.write(System.getProperty("line.separator").getBytes());
					oFile.write("---------------[Will Cause Regression]---------------------".getBytes());
					oFile.write(System.getProperty("line.separator").getBytes());

					for (String sm : survivedMutantsList) {
						oFile.write(sm.getBytes());
					}

					oFile.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				iter_regression = regressionTest_report.iterator(); 
				timer_regression.start();

		}
			
		});
		
		timer_regression = new Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				//progressBar_mutation.setValue(task_checker.getCurrent());
				
				//for (int i = 0; i < regressionTest_report.size(); i++) {
				
					String s = iter_regression.next();
					System.out.println("iter: " + s);
							if (!s.isEmpty()) {
								textArea_regressionTesting.append(s + newline);
								textArea_regressionTesting.setCaretPosition(
								textArea_regressionTesting.getDocument().getLength());
							}
							
							if (!iter_regression.hasNext()) {
								timer_regression.stop();
							}
							
				//}
				
			}
		});
		btnRun_regression.setBounds(91, 210, 111, 25);
		RegressionTestPanel.add(btnRun_regression);

		JPanel panel_modes = new JPanel();
		panel_modes.setToolTipText("");
		panel_modes.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Executable Models", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel_modes.setBounds(12, 0, 577, 157);
		RegressionTestPanel.add(panel_modes);

		JPanel panel_config = new JPanel();
		panel_config.setToolTipText("");
		panel_config.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Critical Variables", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panel_config.setBounds(12, 159, 577, 50); 
		//12, 0, 577, 157)
		RegressionTestPanel.add(panel_config);
		
		


	}



	class ReplayerRunnable extends JComponent implements Runnable {
		public ReplayerRunnable() {
		}
		public void run() {
			Controller.startReplayer("");	    
		}
	}

	public static String getOrgModelPath() {
		return orgModelPath;
	}
	public static String getTracePath() {
		return tracePath;
	}
	public static String getModifiedModelPath() {
		return modifiedModelPath;
	}
	public static String getCollectedTracePath() {
		return collectedTracePath;
	}
}

class LongTask1 {
	private int lengthOfTask;
	private int current = 0;
	private boolean done = false;
	private boolean canceled = false;
	private String statMessage = "";
	private String inputs = "";
	private String selectedModelPath = "";
	private String selectedMutantsPath = "";
	private String selectedJSONPath = "";
	private String mainAddress = "";
	public ArrayList <String> allMutantsFileName;
	private String reportFilePath = ""; 
	private String testCasePathFile = "";
	private String mutantsPathFile = "";


	public LongTask1() {
		//Compute length of task...
		//In a real program, this would figure out
		//the number of bytes to read or whatever.
		lengthOfTask = 1000;
		allMutantsFileName = new ArrayList<>();
	}

	/**
	 * Called from ProgressBarDemo to start the task.
	 */
	public void go_mutantsGenerator() {
		final SwingWorker1 worker = new SwingWorker1() {
			public Object construct() {
				current = 0;
				done = false;
				canceled = false;
				statMessage = "";
				try {
					return new GenerateMutants();
				} catch (IOException | ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return new Object();
			}
		};
		worker.start();
	}

	/**
	 * Called from ProgressBarDemo to start the task.
	 */
	public void go_testCaseChecker() {
		final SwingWorker1 worker1 = new SwingWorker1() {
			public Object construct() {
				current = 0;
				done = false;
				canceled = false;
				statMessage = "";
				return new TestCaseChecker();
			}
		};
		worker1.start();
	}

	/**
	 * Called from ProgressBarDemo to find out how much work needs
	 * to be done.
	 */
	public int getLengthOfTask() {
		return lengthOfTask;
	}

	/**
	 * Called from ProgressBarDemo to find out how much has been done.
	 */
	public int getCurrent() {
		return current;
	}

	public void stop() {
		canceled = true;
		statMessage = null;
	}

	/**
	 * Called from ProgressBarDemo to find out if the task has completed.
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Returns the most recent status message, or null
	 * if there is no current status message.
	 */
	public String getMessage() {
		return statMessage;
	}

	public String getInputs() {
		return inputs;
	}

	public void setInputs(String in) {
		inputs = in;
	}

	public void setMutantsName(String name) {
		allMutantsFileName.add(name);
	}
	public void setReportFilePath(String path) {
		reportFilePath = path;
	}
	public void setTestCasePathFile(String path) {
		testCasePathFile = path;
	}
	public String getTestCasePathFile() {
		return testCasePathFile;
	}
	public void setMutantsPathFile(String path) {
		mutantsPathFile = path;
	}
	public String getMutantsPathFile() {
		return mutantsPathFile;
	}
	


	/**
	 * The actual long running task.  This runs in a SwingWorker thread.
	 */
	public Object readJSONFile() throws IOException, ParseException {
		//JSON parser object to parse read file
		JSONParser jsonParser = new JSONParser();
		System.out.println("selectedJSONPath: "+ selectedJSONPath);
		FileReader reader = new FileReader(selectedJSONPath);

		//Read JSON file
		Object obj = jsonParser.parse(reader);
		return obj;
	}

	public void setSelectedModelPath(String path) {
		selectedModelPath = path;
		String[] values = path.split("\\.uml");
		selectedJSONPath = values[0]+"Description.json";
		System.out.println("<selectedModelPath>: "+selectedModelPath + "\n");
		System.out.println("<selectedJsonPath>: "+selectedJSONPath + "\n");

		int index = values[0].lastIndexOf('/');
		mainAddress = values[0].substring(0, index+1);
		System.out.println("<mainAddress>: "+mainAddress + "\n");

	}

	public void setSelectedMutantsPath(String path) {
		selectedMutantsPath = path;
	}


	class GenerateMutants {
		GenerateMutants() throws IOException, ParseException {
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(new FileReader(selectedJSONPath));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray listCapsulesInstances = (JSONArray) jsonObject.get("listCapsulesInstances");

			current = 0; 				
			Iterator<JSONObject> iterator1 = listCapsulesInstances.iterator();
			while (iterator1.hasNext()) {
				//System.out.println("------->"+iterator1.next().get("name"));
				JSONObject jsonObject_capInstance =  (JSONObject) iterator1.next();
				//System.out.println("------->"+jsonObject_capInstance.get("name"));

				//System.out.println(iterator1.next().get("children"));
				JSONArray children = (JSONArray) jsonObject_capInstance.get("children");
				Iterator<JSONObject> iterator2 = children.iterator();
				while (iterator2.hasNext()) {
					JSONObject jsonObject_children =  (JSONObject) iterator2.next();
					System.out.println(jsonObject_children.get("name"));
					statMessage = "Component's name: "+ jsonObject_children.get("name") +
							" in capsule instance's name: " + jsonObject_capInstance.get("name") + ".";
					//System.out.println(iterator2.next().get("name"));
					try {
						if (inputs.contentEquals("all"))
							Thread.sleep(50);
						else 
							Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				current +=100;

			}
			if (current == 1000) {
				done = true;
			}
		}
	}

	class TestCaseChecker {
		TestCaseChecker() {
			//TODO: add diff testcase by reading testCasePathFile
			ArrayList<String> survivedMutantsList = new ArrayList<>();

			File file = new File(reportFilePath);
			FileOutputStream oFile = null;
			try {
				file.createNewFile(); // if file already exists will do nothing 
				oFile = new FileOutputStream(file, false);

				oFile.write("---------------[ALL Mutants]---------------------".getBytes());
				oFile.write(System.getProperty("line.separator").getBytes());

				Iterator<String> iter 
				= allMutantsFileName.iterator(); 
				while (iter.hasNext()) {
					try {
						Thread.sleep(100);
						String mutantName = iter.next();

						
						oFile.write(statMessage.getBytes());
						oFile.write(System.getProperty("line.separator").getBytes());
						
					} catch (InterruptedException | IOException e) {
						System.out.println("ActualTask interrupted");
					}
				}

				oFile.write(System.getProperty("line.separator").getBytes());
				oFile.write("---------------[Survived Mutants]---------------------".getBytes());
				oFile.write(System.getProperty("line.separator").getBytes());

				for (String sm : survivedMutantsList) {
					oFile.write(sm.getBytes());
				}

				oFile.close();
				done = true;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 
		}
	}

	class ORG_GenerateMutants {
		ORG_GenerateMutants() {
			//Fake a long task,
			//making a random amount of progress every second.
			while (!canceled && !done) {
				try {
					Thread.sleep(1000); //sleep for a second
					current += Math.random() * 100; //make some progress
					if (current >= lengthOfTask) {
						done = true;
						current = lengthOfTask;
					}
					statMessage = "Completed " + current +
							" out of " + lengthOfTask + ".";
				} catch (InterruptedException e) {
					System.out.println("ActualTask interrupted");
				}
			}
		}
	}
}
/**
 * This is the 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.  For
 * instructions on and examples of using this class, see:
 * 
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 *
 * Note that the API changed slightly in the 3rd version:
 * You must now invoke start() on the SwingWorker after
 * creating it.
 */
abstract class SwingWorker1 {
	private Object value;  // see getValue(), setValue()

	/** 
	 * Class to maintain reference to current worker thread
	 * under separate synchronization control.
	 */
	public static class ThreadVar {
		private Thread thread;
		ThreadVar(Thread t) { thread = t; }
		synchronized Thread get() { return thread; }
		synchronized void clear() { thread = null; }
	}

	private ThreadVar threadVar;

	/** 
	 * Get the value produced by the worker thread, or null if it 
	 * hasn't been constructed yet.
	 */
	protected synchronized Object getValue() { 
		return value; 
	}

	/** 
	 * Set the value produced by worker thread 
	 */
	private synchronized void setValue(Object x) { 
		value = x; 
	}

	/** 
	 * Compute the value to be returned by the <code>get</code> method. 
	 */
	public abstract Object construct();

	/**
	 * Called on the event dispatching thread (not on the worker thread)
	 * after the <code>construct</code> method has returned.
	 */
	public void finished() {
	}

	/**
	 * A new method that interrupts the worker thread.  Call this method
	 * to force the worker to stop what it's doing.
	 */
	public void interrupt() {
		Thread t = threadVar.get();
		if (t != null) {
			t.interrupt();
		}
		threadVar.clear();
	}

	/**
	 * Return the value created by the <code>construct</code> method.  
	 * Returns null if either the constructing thread or the current
	 * thread was interrupted before a value was produced.
	 * 
	 * @return the value created by the <code>construct</code> method
	 */
	public Object get() {
		while (true) {  
			Thread t = threadVar.get();
			if (t == null) {
				return getValue();
			}
			try {
				t.join();
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // propagate
				return null;
			}
		}
	}


	/**
	 * Start a thread that will call the <code>construct</code> method
	 * and then exit.
	 */
	public SwingWorker1() {
		final Runnable doFinished = new Runnable() {
			public void run() { finished(); }
		};

		Runnable doConstruct = new Runnable() { 
			public void run() {
				try {
					setValue(construct());
				}
				finally {
					threadVar.clear();
				}

				SwingUtilities.invokeLater(doFinished);
			}
		};

		Thread t = new Thread(doConstruct);
		threadVar = new ThreadVar(t);
	}

	/**
	 * Start the worker thread.
	 */
	public void start() {
		Thread t = threadVar.get();
		if (t != null) {
			t.start();
		}
	}
}