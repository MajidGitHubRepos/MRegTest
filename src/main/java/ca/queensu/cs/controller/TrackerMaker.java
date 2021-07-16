package ca.queensu.cs.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/*

Developers:
Majid Babaei (babaei@cs.queensu.ca): Initial development - 120918

 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

import ca.queensu.cs.antler4AC.Value;
import ca.queensu.cs.controller.Controller.RunnableImpl;
import ca.queensu.cs.server.Event;
import ca.queensu.cs.server.Server;
import ca.queensu.cs.umlrtParser.PES;
import ca.queensu.cs.umlrtParser.ParserEngine;
import ca.queensu.cs.umlrtParser.TableDataMember;
import ca.queensu.cs.umlrtParser.TransitionData;
import ca.queensu.cs.umlrtParser.UmlrtUtils;

public class TrackerMaker implements Runnable{

	//private Event event;
	public String capsuleInstances__indexes;
	private Semaphore semServer;
	private Semaphore semCapsuleTracker;

	//public static CapsuleTracker capsuleTrackers[];
	

	public static int trackerCount;
	public static int priorityEventCounter;
	public static int eventCount;
	public static OutputStream outputFileStream;
	
	private int MAX_NUM_CAPSULE;
	private static int MAX_NUM_POLICY;
	private static List<String[]> listPolicies;
	public static boolean checkPolicy;
	public static HashMap<String, String> capsulePathsMap;
	public static HashMap<String, CapsuleTracker> mapCapsuleTracker;
	public static List<String> listNotMetReq;
    public static long newTraceSize = 0;
	public static long oldTraceSize = 0;

	
	public TrackerMaker(Semaphore semServer, int numberOfCapsules){
		this.listNotMetReq = new ArrayList<String>();
		this.capsulePathsMap   =  new HashMap<String, String>();
		this.mapCapsuleTracker =  new HashMap<String, CapsuleTracker>();
		this.MAX_NUM_POLICY = 2; //Maximum number of entities in the policy chain
		this.MAX_NUM_CAPSULE = numberOfCapsules;
		this.semServer = semServer;
		this.semCapsuleTracker = new Semaphore(1); 
		//this.capsuleTrackers = new CapsuleTracker[MAX_NUM_CAPSULE];
		this.trackerCount = 0;
		this.priorityEventCounter = 0;
		this.eventCount = 0;
		this.capsuleInstances__indexes = "";

			try {
	    		System.out.println("=================================================[Output/Policy files]========================================");
	    		System.out.println("==============[ADDRESS:> workspace/model_aware_event_ordering/target/classes/input_output_Files]==============");

	    		ClassLoader classLoader = getClass().getClassLoader();
	    		//String outputFileName = "input_output_Files/output.txt";
	    		String outputFileDir = "input_output_Files";
	    		File outputFile = new File(classLoader.getResource(outputFileDir).getFile()+"/output.txt");
	    		// ADDRESS: workspace/model_aware_event_ordering/target/classes/input_output_Files
	    		outputFile.createNewFile(); // if file already exists will do nothing 
	    		System.out.println("\n["+ Thread.currentThread().getName() +"]> outputFile exists in : "+ outputFile.getAbsolutePath());
				this.outputFileStream = new FileOutputStream(outputFile,false);
				//-----------
				
	    		//String policyFileName = "input_output_Files/policy.txt";
	    		File policyFile = new File(classLoader.getResource(outputFileDir).getFile()+"/policy.txt");
	    		policyFile.createNewFile(); // if file already exists will do nothing 
	    		this.listPolicies = UmlrtUtils.readListPolicies(policyFile.getAbsolutePath().toString());
	    		if (this.listPolicies.size() == 0) {
		    		System.out.println("["+ Thread.currentThread().getName() +"]> NO Policy defined in : "+ outputFile.getAbsolutePath());
		    		this.checkPolicy = false;
	    		}else {
		    		System.out.println("["+ Thread.currentThread().getName() +"]> <<"+ this.listPolicies.size() +">> Policy/Policies defined in : "+ policyFile.getAbsolutePath());
		    		this.checkPolicy = true; //e.g: bank,controller,cashDispenser
	    		}
	    		System.out.println("\n============================================================================================================");

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.err.println("===[FileNotFoundException]===");
				System.err.println("=====[PROGRAM TERMINATED]=====");
				System.exit(0);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("===[IOException]===");
				System.err.println("=====[PROGRAM TERMINATED]=====");
				System.exit(0);
				e.printStackTrace();
			}
		
	}

	public void run() {
		String name = Thread.currentThread().getName();
		makeCapsuleTrakers();

		while(true) {
			/*if (TrackerMaker.priorityEventCounter>1000) {
				System.err.println("=====================================[EXPERIMENT DONE]======================================");
				 System.exit(0);
			}*/
			System.out.print("");

			if (!Server.eventQueue.isEmpty()) {

				Event eventTmp;
				try {
					eventTmp = getEventFromServerQueue();
					//if (eventTmp.getCapsuleInstance().contains("server1"))
					//	System.err.println("=======SRV1=============EVENT=================> " + eventTmp.allDataToString());
					enqueue(eventTmp);

				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//if (eventCount > 300) { for (int i = 0; i<trackerCount;i++) {capsuleTrackers[i].shutdown();} break;} // only first 100 events are considered! [for testing propose] 
			}
		}
	}//run()

	public Event getEventFromServerQueue() throws InterruptedException {
		//semServer.acquire(); //blockingQueue can handle that with an internal semaphore
		Event event = Server.eventQueue.poll();
		//semServer.release(); 
		return event;
	}

	public void enqueue(Event event) throws InterruptedException {

		//System.out.println("\n\n\n["+ Thread.currentThread().getName() +"]+++>[event] : "+ event.allDataToString());
		String capsuleInstance__capsuleIndex = event.getCapsuleInstance() + "__" + event.getCapsuleIndex();
		if (!capsuleInstances__indexes.contains(capsuleInstance__capsuleIndex)) {
			try {
				this.semCapsuleTracker.acquire();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String chosenInstance = mappingIdxToInstance(event.getCapsuleInstance(), event.getCapsuleIndex());
			
			//Mapping TODO: we assume that all StartUp messagres received first 
			if (chosenInstance == null) {
				//System.err.println("=====================================[Not enough Instance Found]======================================");
			 	System.exit(0);
			}			
			
			Controller.mapIdxCapInst.put(capsuleInstance__capsuleIndex,chosenInstance);
			//if (event.getSourceName().contains("QueryConfig") || event.getSourceName().contains("MasterAnnouncment") || event.getSourceName().contains("configOK"))
				//System.err.println("====================EVENT=================> " + event.allDataToString());

			mapCapsuleTracker.get(chosenInstance).dataContainer.eventQueue.add(event);

			capsuleInstances__indexes = capsuleInstances__indexes + ", " + capsuleInstance__capsuleIndex;
			eventCount++;
			this.semCapsuleTracker.release();
			showElements();

		}else {
			//System.out.println("2 ============> [Controller.mapIdxCapInst.get(capsuleInstance__capsuleIndex)]: "+ Controller.mapIdxCapInst.get(capsuleInstance__capsuleIndex));
			mapCapsuleTracker.get(Controller.mapIdxCapInst.get(capsuleInstance__capsuleIndex)).dataContainer.eventQueue.add(event);
			eventCount++;
		}
		
	}//enqueue
	

	//==================================================================	
	//==============================================[showAllMSGlists]
	//==================================================================			
	public static void showAllMSGlists(){
		System.err.println("=====================[ALL MSGs]");
		for (Entry<String, CapsuleTracker> entry : mapCapsuleTracker.entrySet()) {
			System.out.println(entry.getValue().dataContainer.showMapMSG());
		}
	}
	
	//==================================================================	
	//==============================================[makeCapsuleTrakers]
	//==================================================================			
	public void makeCapsuleTrakers(){
		for (Entry<String, List<Map<String, String>>> entry : PES.mapModelRegionPaths.entrySet()) {
			String regionKey = entry.getKey().replaceAll("\\s","");
			String [] regionKeySplit = regionKey.split("\\,");
			List<Map<String, String>> listRegionStates = entry.getValue();
			Map<String, String> mapRegionCurrentState   =  new HashMap<String, String>(); //TODO: put regionName StateName into that!
			for(Map<String, String> regionStates: listRegionStates) {
				mapRegionCurrentState.put(regionStates.keySet().toString().replaceAll("\\[", "").replaceAll("\\]",""), "INIT");
			}
			
			for (String capsuleFullname: regionKeySplit) {
				if (!capsuleFullname.isEmpty()) {
					
					PriorityBlockingQueue<Event> eventQueue = new PriorityBlockingQueue<Event>();
					//BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
					//CapFullname
					//System.out.println("[in makeCapsuleTrakers] capsuleFullname: " + capsuleFullname);
					int idx = capsuleFullname.indexOf("__");
					
					String capsuleName = ""; 
					for (TransitionData td : ParserEngine.listTransitionData) {
						//System.out.println("[in makeCapsuleTrakers] td.getCapsuleInstanceName(): " + td.getCapsuleInstanceName());
						if (td.getCapsuleInstanceName().contains(capsuleFullname)) {
							capsuleName = td.capsuleName;
							break;
						}
					}
					
					DataContainer dataContainer = new DataContainer(capsuleName,capsuleFullname, eventQueue);
										
					int[] logicalVectorTime = new int[MAX_NUM_CAPSULE]; //default value of 0 for arrays of integral types is guaranteed by the language spec
					CapsuleTracker capsuleTracker = new CapsuleTracker(semCapsuleTracker, outputFileStream, logicalVectorTime, dataContainer); 
					Thread capsuleTrackerT = new Thread(capsuleTracker); 
					capsuleTrackerT.setPriority(Thread.NORM_PRIORITY); 					
					capsuleTrackerT.start();
					mapCapsuleTracker.put(capsuleFullname, capsuleTracker);
					trackerCount++;
				}
			}			
		}
		
	}
	
	//==================================================================	
	//==============================================[mappingIdxToInstance]
	//==================================================================			
	public String mappingIdxToInstance(String capsuleInstance ,String capsuleIndex){
		String capsuleInstance__capsuleIndex = capsuleInstance + "__" + capsuleIndex;
		
		for (Entry<String, List<Map<String, String>>> entry : PES.mapModelRegionPaths.entrySet()) {
			String regionKey = entry.getKey().replaceAll("\\s","");
			if (!regionKey.isEmpty() && regionKey.contains(capsuleInstance)) {
				String [] regionKeySplit = regionKey.split("\\,");
				for (String key: regionKeySplit) {
					if (!Controller.mapCapInstIdx.containsKey(key) && key.contains(capsuleInstance)) {
						Controller.mapCapInstIdx.put(key, capsuleInstance__capsuleIndex);
						return key;
					}
				}
			}
		}
		return null;
	}
	
	//==================================================================	
	//==============================================[findCapfullNameByCapInstanceIdx]
	//==================================================================			

	public String findCapfullNameByCapInstanceIdx (String capsuleInstance__capsuleIndex)  
	{
		for (Map.Entry<String, String> entry  : Controller.mapCapInstIdx.entrySet()) {
			if(entry.getValue().contains(capsuleInstance__capsuleIndex)) {
				String capfullName = entry.getKey().replaceAll("\\s+","");
				return capfullName;
			}
		}
		System.err.println("==================[__capsuleFullNameNotFound__]["+capsuleInstance__capsuleIndex+"]======================");
		return "";
		
	}

	//==================================================================	
	//==============================================[showElements]
	//==================================================================			

	public void showElements()  
	{
		System.out.println("=======================[mapCapInstIdx]==========================");
		for (Map.Entry<String, String> entry  : Controller.mapCapInstIdx.entrySet()) {
			//if (entry.getKey().contains(event.getCapsuleInstance())) { 
			System.out.println("["+ Thread.currentThread().getName() +"]+++>[entry.getKey()] : "+ entry.getKey()+" [entry.getValue()] : "+entry.getValue());
			//}
		}
		System.out.println("=======================[mapCapsuleTracker]==========================");
		for (Entry<String, CapsuleTracker> entry  : mapCapsuleTracker.entrySet()) {
			//if (entry.getKey().contains(event.getCapsuleInstance())) { 
			System.out.println("["+ Thread.currentThread().getName() +"]+++>[entry.getKey()] : "+ entry.getKey()+" [entry.getValue()] : "+entry.getValue());
			//}
		}
	}

	//==================================================================	
	//==============================================[checkPolicyViolation]
	//==================================================================			

	public static boolean checkPolicyViolation(String capsuleInstance, Event event)  
	{
		boolean result = false; //policy respected! //TODO: too much optimistic assumption
		String requirement = "";
		
		for (Map.Entry<String, String> entry  : capsulePathsMap.entrySet()) {
			if (entry.getKey().contentEquals(capsuleInstance)) {
				String [] currPathArr = entry.getValue().split("\\,");
				System.out.println("["+ Thread.currentThread().getName() +"]===================> [capsuleInstance]: "+ capsuleInstance +" [PATH]: "+ entry.getValue());
				//Arbitrary security policy can be applied here on the received event
				
				// looking for the requirement in listPolices
				for(int i = 0; i < listPolicies.size();i++) {
					String [] policies = listPolicies.get(i);
					for (int j = 0; j < policies.length ;j++) {
						if ((capsuleInstance.contains(policies[j])) && (j > 0)){
							requirement = policies[j-1];
							break;
						}
					}
					if (requirement != "")
						break;
				}
				
				//Check Happen-Before rule
				// TODO: only internal Happen-Before relationship supported in this version
				if (requirement != "") {
					int lastIndexOf_ = capsuleInstance.lastIndexOf("__");
					String rootName = capsuleInstance.substring(0, lastIndexOf_);
					
					for (Map.Entry<String, String> entryPath  : capsulePathsMap.entrySet()) {
						if ((entryPath.getKey().contains(rootName)) && (entryPath.getKey().contains(requirement))) {
							//check security policy
							String [] pathArr = entryPath.getValue().split("\\,");
							if (pathArr.length == 0) { //assumption: if pathArr.length > 0 then required capsule is being sent or has been done! 
								//requirement has not met!
								result = true;
								System.out.println("["+ Thread.currentThread().getName() +"]*****************> [rootName]: "+ rootName +" [requirement]: "+ requirement);
								System.out.println("["+ Thread.currentThread().getName() +"]*****************> [capsuleInstance]: "+ capsuleInstance +" [BAD PATH]: "+ entryPath.getValue());

								break;
							}
						}
					}
				}
			}
		}
		
		return result;

	}

	//==================================================================	
	//==============================================[addCapsulePaths]
	//==================================================================			

	public static boolean addCapsulePaths(String capsuleInstance, Event event)  
	{
		for (Map.Entry<String, String> entry  : capsulePathsMap.entrySet()) {
			if (entry.getKey().contentEquals(capsuleInstance)) {
				String currPath = entry.getValue();
				// check MAX_NUM_POLICY to avoid memory overflow
				String [] currPathArr = currPath.split("\\,");
				if (currPathArr.length > MAX_NUM_POLICY) {
					currPath = event.getSourceKind()+"_"+event.getType()+"_"+event.getCapsuleInstance()+"_"+event.getCapsuleIndex();
				} else {
				currPath = currPath + ","+event.getSourceKind()+"_"+event.getType()+"_"+event.getCapsuleInstance()+"_"+event.getCapsuleIndex();
				}
				capsulePathsMap.put(entry.getKey(),currPath);
				return true;
			}
		}
	
		capsulePathsMap.put(capsuleInstance, event.getSourceKind()+"_"+event.getType()+"_"+event.getCapsuleInstance()+"_"+event.getCapsuleIndex());
		return false;
	}
}