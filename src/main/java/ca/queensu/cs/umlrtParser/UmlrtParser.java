package ca.queensu.cs.umlrtParser;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.util.EcoreUtil.ContentTreeIterator;
import org.eclipse.uml2.uml.BehavioredClassifier;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Collaboration;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Enumeration;
import org.eclipse.uml2.uml.Interface;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.resources.util.UMLResourcesUtil;

import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Vertex;
import org.eclipse.uml2.uml.OpaqueBehavior;


public class UmlrtParser {
	
	//private  Thread parserEngineT;
	private ParserEngine parserEngine;
	private boolean umlrtParsingDone;
	public static  Map<String, List<TableDataMember>> listTableData;
	private List<CapsuleConn> listCapsuleConn;
	public File outputFile;
	public File policyFile;
	
	public UmlrtParser() {
		this.outputFile = new File("");
		this.policyFile = new File("");
		this.listTableData = new HashMap<String, List<TableDataMember>>();
		this.listCapsuleConn = new ArrayList<CapsuleConn>();
		this.umlrtParsingDone = false;
	}
	
	//----------------------------------
	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
	}
	
	public void setPolicyFile(File policyFile) {
		this.policyFile = policyFile;
	}
	
	public void setParserEngine(ParserEngine pe) {
		parserEngine = pe;
	}
	
	public File getOutputFile() {
		return this.outputFile;
	}
	
	public File getPolicyFile() {
		return this.policyFile;
	}
	//----------------------------------
	
	public Map<String, List<TableDataMember>> getlistTableData() {
		return this.listTableData;
	}
	
	public List<CapsuleConn> getlistCapsuleConn() {
		return this.listCapsuleConn;
	}
	
	public boolean getUmlrtParsingDone() {
		return this.umlrtParsingDone;
	}
	
	public ParserEngine getParserEngine() {
		return parserEngine;
	}
	   
	public static void main(String[] args) throws IOException 
    { 
		UmlrtParser umlrtParser = new UmlrtParser();
        Thread t1 = new Thread(umlrtParser.new RunnableImpl()); 
        t1.start(); 
    } 
  
    public class RunnableImpl implements Runnable { 

	public void run() {
    			   		 	
		 	//String fileName = "umlrtModels/PingPong.uml";
    		//String fileName = "umlrtModels/NonDeterministic.uml";
	 		//String fileName = "umlrtModels/SimpleCP.uml";
	 		//String fileName = "umlrtModels/SimpleCapsuleHierarchy.uml";
    		//String fileName = "umlrtModels/2Pinger1Ponger.uml";
    		//String fileName = "umlrtModels/CausalMsgOrdering.uml";
			//String fileName = "umlrtModels/CarDoorLock.uml";
			//String fileName = "umlrtModels/SimpleCS.uml";
			//String fileName = "umlrtModels/SimpleCS_compound.uml";
			//String fileName = "umlrtModels/CSwithCP.uml";
		
			//String fileName = "umlrtModels/Replication_1c.uml";
			String fileName = "umlrtModels/Replication.uml";
    		
			//String fileName = "umlrtModels/Counter.uml";
			//String fileName = "umlrtModels/ParcelRouter.uml";
			//String fileName = "umlrtModels/ParcelRouter_basic.uml";
			
			//String fileName = "umlrtModels/BankATM.uml";
			//String fileName = "umlrtModels/2Pinger1Ponger_threads.uml"; //Montreal Trip 
			//String fileName = "umlrtModels/Replication_Refined.uml";
					
    		ClassLoader classLoader = new RunnableImpl().getClass().getClassLoader();
	        File file = new File(classLoader.getResource(fileName).getFile());
		 	System.out.println("path: " +file.getAbsolutePath());
		 	
		 	String modelPath = file.getAbsolutePath();
		 	XMIResource mainResource = UmlrtUtils.getResource(modelPath);
		    Model inputModel = (Model) EcoreUtil.getObjectByType(mainResource.getContents(), UMLPackage.Literals.MODEL);
		    String topCapsuleName = UmlrtUtils.getTopCapsuleName(inputModel);
		    
		    List<XMIResource> allResources = new LinkedList<XMIResource>();
		    allResources.add(mainResource);
		    
		    EList<PackageableElement> modelElements = inputModel.getPackagedElements();	
		    
		    parserEngine = new ParserEngine(modelElements, topCapsuleName);
		    Thread parserEngineT = new Thread(parserEngine);
		    
		    parserEngineT.start();
		    parserEngineT.interrupt();
	    	try {
				parserEngineT.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	while(true) {
	    		if (parserEngine.getUmlrtParsingDone()) {
	    			listTableData = parserEngine.getListTableData();
	    			listCapsuleConn = parserEngine.getListCapsuleConn();
	    			umlrtParsingDone = true;
	    			break;
	    		}
	    	}
		    
	   }
	  
	   

	}
}