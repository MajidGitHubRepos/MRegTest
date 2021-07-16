package ca.queensu.cs.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.json.simple.parser.ParseException;

import ca.queensu.cs.graph.viewController;
import ca.queensu.cs.outlook.MRegressionPanel;
import ca.queensu.cs.outlook.MReplayerPanel;
import ca.queensu.cs.umlrtParser.UmlrtParser;
import ca.queensu.cs.umlrtParser.UmlrtParser.RunnableImpl;

public class TestingEngine {
	public static UmlrtParser umlrtParser;
	public static viewController viewer;
	public static RegressionTesting regerssionTesting;
	
	public TestingEngine() {
		regerssionTesting = new RegressionTesting();
	}

	
	public class RunnableImpl implements Runnable {

		public void run() {
			
			//regerssionTesting.setSelectedModelPath(MReplayerPanel.getModifiedModelPath(),MReplayerPanel.getConsistentOrdering());
			regerssionTesting.setSelectedModelPath(MRegressionPanel.getModifiedModelPath(),1);

			try {
				editeIndexHtml();
				regerssionTesting.descriptionMaker();
				regerssionTesting.traceMaker();
			} catch (InterruptedException | IOException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	
	public void editeIndexHtml() throws IOException {
		String [] pathSplit = MRegressionPanel.getModifiedModelPath().split("\\/");
		//String add = "/home/babaei/workspace/RegressionTesting/src/main/resources/umlrtModels/DiningPhilosophers.uml";
		//String [] pathSplit = add.split("\\/");
		//String [] pathSplit = MRegressionPanel.getModifiedModelPath().split("\\/");
		String modelName = pathSplit[pathSplit.length -1];
		String [] modelNameSplit = modelName.split("\\.");

		//https://regex101.com/
		String pattern = "localModel_[A-Za-z0-9]*(_*[A-Za-z0-9]+).xml";
		String localModelName = "localModel_"+modelNameSplit[0]+".xml";
		modifyFile("./mxgraph/javascript/examples/grapheditor/www/index.html", pattern, localModelName);
			
	}
	

	
	public void modifyFile(String filePath, String oldString, String newString)
    {
        File fileToBeModified = new File(filePath);
         
        String oldContent = "";
         
        BufferedReader reader = null;
         
        FileWriter writer = null;
         
        try
        {
            reader = new BufferedReader(new FileReader(fileToBeModified));
             
            //Reading all the lines of input text file into oldContent
             
            String line = reader.readLine();
             
            while (line != null) 
            {
                oldContent = oldContent + line + System.lineSeparator();
                 
                line = reader.readLine();
            }
             
            //Replacing oldString with newString in the oldContent
             
            String newContent = oldContent.replaceAll(oldString, newString);
             
            //Rewriting the input text file with newContent
             
            writer = new FileWriter(fileToBeModified);
             
            writer.write(newContent);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                //Closing the resources
                 
                reader.close();
                 
                writer.close();
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
    }

}
