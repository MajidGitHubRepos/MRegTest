package ca.queensu.cs.graph;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UMLPackage;

import ca.queensu.cs.umlrtParser.ParserEngine;
import ca.queensu.cs.umlrtParser.TableDataMember;
import ca.queensu.cs.umlrtParser.UmlrtParser;
import ca.queensu.cs.umlrtParser.UmlrtUtils;
import ca.queensu.cs.umlrtParser.UmlrtParser.RunnableImpl;

public class viewController {
	public ViewEngine viewEngine;
	public static Map<String, List<TableDataMember>> listTableData;
	//public static UmlrtParser umlrtParser;
	
	public viewController(Map<String, List<TableDataMember>> listTableData) {
		this.listTableData = listTableData;
		
	}
	public viewController() {
		this(null);
	}
	
	public void setListTableData(Map<String, List<TableDataMember>> listTableData) {
		this.listTableData = listTableData;
	}
  
    public class RunnableImpl implements Runnable { 

	public void run() {
		
		
		    viewEngine = new ViewEngine();
		    Thread viewEngineT = new Thread(viewEngine);
		    
		    viewEngineT.start();
		    viewEngineT.interrupt();
	    	try {
	    		viewEngineT.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
	   }
	  
	   

	}
}