package ca.queensu.cs.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.json.JsonWriter;
import javax.swing.ListModel;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.microprofile.metrics.Timer;
import org.eclipse.uml2.uml.State;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import ca.queensu.cs.antler4AC.ACLexer;
import ca.queensu.cs.antler4AC.ACParser;
import ca.queensu.cs.antler4AC.EvalVisitor;
import ca.queensu.cs.antler4AC.SendMessage;
import ca.queensu.cs.antler4AC.Value;
import ca.queensu.cs.graph.ViewEngine;
import ca.queensu.cs.server.Event;
import ca.queensu.cs.server.ServerVTOrdering;
import ca.queensu.cs.umlrtParser.CapsuleConn;
import ca.queensu.cs.umlrtParser.MyConnector;
import ca.queensu.cs.umlrtParser.PES;
import ca.queensu.cs.umlrtParser.ParserEngine;
import ca.queensu.cs.umlrtParser.StateData;
import ca.queensu.cs.umlrtParser.TableDataMember;
import ca.queensu.cs.umlrtParser.TransitionData;
import ca.queensu.cs.umlrtParser.UmlrtParser;
import ca.queensu.cs.umlrtParser.UmlrtUtils;


public class CapsuleTracker implements Runnable{
	private Semaphore semCapsuleTracker;
	private PriorityBlockingQueue <Event> eventQueueTmp;
	private String currentStatus; //{StartUp, INIT, TRANSITIONEND}
	public String previousState;
	public String transitionName;
	public DataContainer dataContainer;
	private OutputStream outputFileStream;
	private double timeMilli;
	private String outputStrTofile;
	private long offset;
	private Timer timer;
	private int[] logicalVectorTime;
	private int TrackerMakerNumber;
	private Event stateExitEvent;
	private double stateExitTimer =0;
	private List<String> listPaths;
	private Map<String,List<String>> mapPathTrigger;
	private Map<String,List<String>> mapPathGuards;
	private Map<String,List<String>> mapPathActionCode;
	private Map<String, Value> maplocalHeap;
    private List<SendMessage> listPortMsg;
    private List<SendMessage> copyListPortMsg;
    private List<String> listConsumedPaths;
    private List<String> listAllPathTaken;
    private String lastTookPath;
    private String msgTobeConsumed;
    private String msgSender;
    private boolean msgConsumedTmpQueue;
    private boolean msgConsumedQueue;
    private List<String> listSoFarMachedTR;
    private PriorityBlockingQueue <Event> accessoryEventQ;
    private List<String> listSupplementaryAC;
    private String supplementaryActiveRegion;


	public CapsuleTracker(Semaphore semCapsuleTracker, OutputStream outputFileStream, int[] logicalVectorTime, DataContainer dataContainer) {
		this.supplementaryActiveRegion = "";
		this.accessoryEventQ = new PriorityBlockingQueue<Event>();
		this.listSupplementaryAC = new ArrayList<String>();
		this.listSoFarMachedTR = new ArrayList<String>();
		this.msgConsumedQueue = false;
		this.msgConsumedTmpQueue = false;
		this.msgTobeConsumed = "";
		this.maplocalHeap = new HashMap<String, Value>();
		this.listConsumedPaths = new ArrayList<String>();
		this.listPortMsg = new ArrayList<SendMessage>();
		this.copyListPortMsg = new ArrayList<SendMessage>();
		this.mapPathGuards =  new HashMap<String,List<String>>();
		this.mapPathTrigger =  new HashMap<String,List<String>>();
		this.mapPathActionCode =  new HashMap<String,List<String>>();
		this.listAllPathTaken =  new ArrayList<String>();
		this.listPaths =  new ArrayList<String>();
		this.dataContainer = dataContainer;
		this.stateExitEvent = null;
		this.TrackerMakerNumber = 0;
		this.semCapsuleTracker = semCapsuleTracker;
		this.outputFileStream = outputFileStream;
		this.logicalVectorTime = logicalVectorTime;
		this.eventQueueTmp = new PriorityBlockingQueue<Event>(); // read but not consume!
		this.currentStatus = "StartUp";
		this.timeMilli = 0;
		this.outputStrTofile = "";
		this.timer = new TimerImpl();
		this.lastTookPath = "";
		this.msgSender = "";
	}


	public void run() {
		try {
			semCapsuleTracker.acquire();
			//Go to the initial state and wait for the right event
			System.out.println("============> Running : "+ dataContainer.getCapsuleName() );
			

			List<String> listAttributes = ParserEngine.mapCapAttributes.get(dataContainer.getCapsuleName());

			if (listAttributes != null) {
				for (String att : listAttributes) {
					String [] attSplit = att.split(":");
					if (attSplit[1].contentEquals("String"))
						maplocalHeap.put(attSplit[0], new Value("", "String"));
					else if (attSplit[1].contentEquals("Integer"))
						maplocalHeap.put(attSplit[0], new Value(Double.valueOf(0), "Integer"));
					else if (attSplit[1].contentEquals("Real"))
						maplocalHeap.put(attSplit[0], new Value(Double.valueOf(0), "Double"));
					else if (attSplit[1].contentEquals("Boolean"))
						maplocalHeap.put(attSplit[0], new Value(false, "Boolean"));
					else {
						System.err.println("__________ The Tyep of Attribute Not supported! __________");
						System.exit(1);
					}
				}
			}
			semCapsuleTracker.release();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while(true) {
			try {
				if (((dataContainer.getEventQueue().size() > 0) || (eventQueueTmp.size() > 0))) {
				semCapsuleTracker.acquire();
				
				if (TrackerMaker.listNotMetReq.contains(dataContainer.getCapsuleInstance()))
						Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				else
					Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
				
				if ((!eventQueueTmp.isEmpty() || !dataContainer.getEventQueue().isEmpty()) /*&& !TrackerMaker.listNotMetReq.contains(dataContainer.getCapsuleInstance())*/) {
					Event currentEventTmp = new Event();
					Event currentEvent = new Event();
					msgConsumedTmpQueue = false;
					msgConsumedQueue = false;
					
					if (!eventQueueTmp.isEmpty()) {
						int eventQueueTmpSize = eventQueueTmp.size();
						for (int j = 0; j < eventQueueTmpSize;  j++) {
							

							currentEventTmp = eventQueueTmp.take();
							
							if (!listConsumedPaths.isEmpty() && isPassedEvent(currentEventTmp)) {calcTraceSizes(currentEventTmp);}
							else if (isConsumable(currentEventTmp)) {
								if (currentStatus.contentEquals("TRANISTIONEND")) {
									if ((currentEventTmp.getSourceName() != null) && validMatchedPath(PES.mapQnameId.get(currentEventTmp.getSourceName()))) {
										listSoFarMachedTR.add(PES.mapQnameId.get(currentEventTmp.getSourceName()));
									}
									if ((listPaths.size() == 1) && listSoFarMachedTR.toString().replaceAll("\\s","").contains(listPaths.get(0))) {
										if (isRequirementMet(listPaths.get(0))) {
											TrackerMaker.listNotMetReq.clear();
											calcTraceSizes(currentEventTmp);
											//if (!jsonToServer(TrackerMaker.priorityEventCounter,dataContainer.getCapsuleInstance(), currentEventTmp.getReginName(),listPaths.get(0),maplocalHeap,TrackerMaker.newTraceSize,TrackerMaker.oldTraceSize,makeStates(),listPortMsg)&&(Controller.args0 == "view")) System.err.println("===[WEB_SERVER CONNECTION FAILD]===");
											listAllPathTaken.add(listPaths.get(0));
											addToListConsumedPaths(listPaths.get(0), PES.mapQnameId.get(currentEventTmp.getSourceName()));
											updateLocalHeap(listPaths.get(0),msgSender,false);
											if (!jsonToServer(TrackerMaker.priorityEventCounter,dataContainer.getCapsuleInstance(), currentEventTmp.getReginName(),listPaths.get(0),maplocalHeap,TrackerMaker.newTraceSize,TrackerMaker.oldTraceSize,makeStates(),copyListPortMsg)&&(Controller.args0 == "view")) System.err.println("===[WEB_SERVER CONNECTION FAILD]===");
											copyListPortMsg.clear();
											updateCurrentState();
											if (!msgTobeConsumed.isEmpty())
												dataContainer.removeFromListMapSendMessages(msgTobeConsumed);
											msgTobeConsumed = "";
											msgSender = "";
											listPaths.clear();
											listSoFarMachedTR.clear();
											msgConsumedTmpQueue = true;
											break;
										}else {accessoryEventQ.add(currentEventTmp); listPaths.clear();
										if (!TrackerMaker.listNotMetReq.contains(dataContainer.getCapsuleInstance())) TrackerMaker.listNotMetReq.add(dataContainer.getCapsuleInstance());
										//System.err.println(dataContainer.getCapsuleInstance()+" >>>>>[TMP]>>>> TrackerMaker.listNotMetReq: " + TrackerMaker.listNotMetReq.toString());
										//System.err.println(dataContainer.getCapsuleInstance()+" >>>>>[TMP]>>>> listConsumedPaths: " + listConsumedPaths.toString());
										//System.out.println(dataContainer.getCapsuleInstance()+" >>>>>[TMP]>>>> BAD EVENT: " + currentEventTmp.allDataToString());
										//TrackerMaker.showAllMSGlists();
										} //req did not meet!
									}else {}
								}
							}else {accessoryEventQ.add(currentEventTmp);/*eventQueueTmp.add(currentEventTmp);*/} //evet is out of order
						}
					}
					
					if (!accessoryEventQ.isEmpty()) {	
						eventQueueTmp.addAll(accessoryEventQ);
						accessoryEventQ.clear();
					}

					if(!dataContainer.getEventQueue().isEmpty()) {
						boolean eventFound = true;
						do {
							currentEvent =  dataContainer.eventQueue.take(); //push it back to the queue if it dose not consume !
							if (!listConsumedPaths.isEmpty() && isPassedEvent(currentEvent)) {calcTraceSizes(currentEvent);}
							else if (eventQueueTmp.isEmpty() && isConsumable(currentEvent)) {

								if (currentStatus.contentEquals("TRANISTIONEND")) {
									if ((currentEvent.getSourceName() != null) && validMatchedPath(PES.mapQnameId.get(currentEvent.getSourceName()))) {
										listSoFarMachedTR.add(PES.mapQnameId.get(currentEvent.getSourceName()));
									}
									if ((listPaths.size() == 1) && listSoFarMachedTR.toString().replaceAll("\\s","").contains(listPaths.get(0))) {
										if (isRequirementMet(listPaths.get(0))) {
											TrackerMaker.listNotMetReq.clear();
											calcTraceSizes(currentEvent);
											//if (!jsonToServer(TrackerMaker.priorityEventCounter,dataContainer.getCapsuleInstance(), currentEvent.getReginName(),listPaths.get(0),maplocalHeap, TrackerMaker.newTraceSize, TrackerMaker.oldTraceSize, makeStates(), listPortMsg)&&(Controller.args0 == "view")) System.err.println("===[WEB_SERVER CONNECTION FAILD]===");
											listAllPathTaken.add(listPaths.get(0));
											addToListConsumedPaths(listPaths.get(0), PES.mapQnameId.get(currentEvent.getSourceName()));
											updateLocalHeap(listPaths.get(0),msgSender,false);
											if (!jsonToServer(TrackerMaker.priorityEventCounter,dataContainer.getCapsuleInstance(), currentEvent.getReginName(),listPaths.get(0),maplocalHeap, TrackerMaker.newTraceSize, TrackerMaker.oldTraceSize, makeStates(), copyListPortMsg)&&(Controller.args0 == "view")) System.err.println("===[WEB_SERVER CONNECTION FAILD]===");
											copyListPortMsg.clear();
											updateCurrentState();
											if (!msgTobeConsumed.isEmpty())
												dataContainer.removeFromListMapSendMessages(msgTobeConsumed);
											msgTobeConsumed = "";
											msgSender = "";
											listPaths.clear();
											listSoFarMachedTR.clear();
											msgConsumedQueue = true;
											//break;
										}else {eventQueueTmp.add(currentEvent);
										if (!TrackerMaker.listNotMetReq.contains(dataContainer.getCapsuleInstance())) TrackerMaker.listNotMetReq.add(dataContainer.getCapsuleInstance()); 
										//System.err.println(dataContainer.getCapsuleInstance()+" >>>>>>>>> TrackerMaker.listNotMetReq: " + TrackerMaker.listNotMetReq.toString());
										//System.err.println(dataContainer.getCapsuleInstance()+" >>>>>>>>> listConsumedPaths: " + listConsumedPaths.toString());
										//System.out.println(dataContainer.getCapsuleInstance()+" >>>>>>>>> BAD EVENT: " + currentEvent.allDataToString());
										//eventMinCounter = currentEvent.getCounter();
										//TrackerMaker.showAllMSGlists();
										eventFound = false;
										} //req did not meet!
									}else {}
								}
							}else {eventQueueTmp.add(currentEvent); eventFound = false;} //evnet is out of order
						}while(eventFound);
						
					}


					if (msgConsumedTmpQueue && currentEventTmp != null && currentEventTmp.getSourceName() != null) {
						System.err.println(dataContainer.getCapsuleInstance()+"---[Event is consumed from TmpEventQueue]---------- "+currentEventTmp.allDataToString());
						showInfo(currentEventTmp);
						currentEventTmp = null;
					}if (msgConsumedQueue && currentEvent != null && currentEvent.getSourceName() !=null) {
						System.err.println(dataContainer.getCapsuleInstance()+"-----[Event is consumed from EventQueue]--------- " + currentEvent.allDataToString());
						showInfo(currentEvent);
						currentEvent = null;
					}
					msgConsumedTmpQueue = false;
					msgConsumedQueue = false;

				}
				semCapsuleTracker.release();
				Thread.yield();
			}
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	//==================================================================	
	//====================================================[isCompositeState]
	//==================================================================	
	public boolean isCompositeState(String stateId) {
		String activeState = ParserEngine.mapStateData.get(stateId).getStateName();
		for (Entry<String, List<Map<String, String>>> entry : PES.mapModelRegionPaths.entrySet()) {
			if (entry.getKey().contains(dataContainer.getCapsuleInstance())) {
				List<Map<String, String>>paths = entry.getValue();
				for (int i = 0; i<paths.size(); i++) {
					String state = paths.get(i).values().toString().replaceAll("\\[", "").replaceAll("\\]","");
					if (activeState.contentEquals(state))
						return true;
				}
			}
		}
		return false;
		
	}
	//==================================================================	
	//====================================================[makeStates]
	//==================================================================	
	public List<String> makeStates() {
		List<String> listStates = new ArrayList<>();
		
		//find the active state
		String stateId = dataContainer.mapRegionCurrentState.get(dataContainer.activeRegion);
		String activeState = ParserEngine.mapStateData.get(stateId).getStateName();
		listStates.add(activeState);
		
		for (Entry<String, String> entry : dataContainer.mapRegionCurrentState.entrySet()) {
			if (!activeState.contentEquals(ParserEngine.mapStateData.get(entry.getValue()).getStateName()) && !isCompositeState(entry.getValue()))
				listStates.add(ParserEngine.mapStateData.get(entry.getValue()).getStateName());
		}
		
		
		
		return listStates;
	}
	//==================================================================	
	//====================================================[calcTraceSizes]
	//==================================================================	
	public void calcTraceSizes(Event event) {
		int overHeadEntryAndExit = 247;
		TrackerMaker.newTraceSize += event.allDataToString().length();
		TrackerMaker.oldTraceSize += event.allDataToString_originalFromMDebugger().length()+overHeadEntryAndExit;
	}
	
	//==================================================================	
	//====================================================[validMatchedPath]
	//==================================================================	
	public boolean validMatchedPath(String id) {
		if (!listSoFarMachedTR.isEmpty()) {
			if (!listSoFarMachedTR.toString().contains(id) && listPaths.toString().contains(id)){ 
				String soFarMatched = "";
				if (!listSoFarMachedTR.isEmpty()) {
					for (String path : listSoFarMachedTR) {
						if (soFarMatched.isEmpty())
							soFarMatched = path;
						else
							soFarMatched = soFarMatched +","+path;
					}
				}
				if (listPaths.toString().contains(soFarMatched+","+id))
					return true;
				
			}else {
				return false;
			}
		}else {
			for (String path : listPaths) {
				String firstTr = findFistID(path);
				if (!firstTr.contentEquals(id)) {
					return false;
				}
			}
		}

		return true;
	}
	
	//==================================================================	
	//====================================================[pathCompeleted]
	//==================================================================	
	public boolean pathCompeleted(String path) {
		if (path.contains(",")) {
			String [] pathsSplit = path.split("\\,");
			for (String tr : pathsSplit) {
				if (!listConsumedPaths.contains(tr))
					return false;
			}
		}
		return true;
	}

	//==================================================================	
	//====================================================[separateTopFromInstanceName]
	//==================================================================	
	public String separateTopFromInstanceName(String top__instanceName) {
		if (top__instanceName.contains("__")) {
			int idx = top__instanceName.indexOf("__");
			return top__instanceName.substring(idx+2);
		}else {
			System.err.println("__________ bad instanceName __________");
			System.exit(1);
		}
		return null;
	}

	//==================================================================	
	//====================================================[addToListConsumedPaths]
	//==================================================================	
	public void addToListConsumedPaths(String path, String id) {
		if (path.contains(",")) {
			String lastTr = findLastID(path);
			if (!id.contentEquals(lastTr)) // if the event is for "initTrBackup" the path: StartUp-->BeSlave-->initTrBackup should not be added to the listConsumedPaths
				listConsumedPaths.add(path);

		}
			
	}
	
	//==================================================================	
	//====================================================[isPassedEvent]
	//==================================================================	
	public boolean isPassedEvent(Event event) {
		String id = PES.mapQnameId.get(event.getSourceName());

		Iterator<String> iter = listConsumedPaths.iterator();
		while (iter.hasNext()) {
			String path = iter.next();
			System.err.println(dataContainer.getCapsuleInstance()+", Path: "+path+", iter to REMOVE "+ iter);

			if ((path != null) && (path.contains(id))) {
				String lastTr = findLastID(path);
					if (lastTr.contentEquals(id)) {
						iter.remove();
					}
					return true;
				}			
		}
		return false;
	}
	
	//==================================================================	
	//====================================================[vTimeHandler]
	//==================================================================
	public void vTimeHandler(Event event) {
		if (currentStatus.contains("TRANISTIONEND")) { // current event is stateExit => save event and do not put timestamp before transition done
			stateExitEvent = event;
			stateExitTimer = ((TimerImpl) timer).nowNano();
		}else if ((currentStatus.contains("STATEENTRYEND")) && (stateExitEvent != null)) { // current event is transition => put timestamp for stateExit and transition
			logicalVectorTime[TrackerMakerNumber]--;
			outputStrTofile = "Vector Time: "+Arrays.toString(logicalVectorTime)+/*"-["+String.format("%f", stateExitTimer)+"]: "+*/stateExitEvent.allDataToString()+"\n\n";
			outputStreamToFile(this.outputFileStream,outputStrTofile);
			logicalVectorTime[TrackerMakerNumber]++;
			outputStrTofile = "Vector Time: "+Arrays.toString(logicalVectorTime)+/*"-["+String.format("%f", ((TimerImpl) timer).nowNano())+"]: "+*/event.allDataToString()+"\n\n";
			outputStreamToFile(this.outputFileStream,outputStrTofile);
			stateExitEvent = null;
			stateExitTimer = 0;
		}else {
			outputStrTofile = "Vector Time: "+Arrays.toString(logicalVectorTime)+/*"-["+String.format("%f", ((TimerImpl) timer).nowNano())+"]: "+*/event.allDataToString()+"\n\n";
			outputStreamToFile(this.outputFileStream,outputStrTofile);
		}
	}
	
	//==================================================================	
	//==============================================[jsonToServer]
	//==================================================================
	public static boolean jsonToServer(int priorityEventCounter, String capsuleInstance, String region, String path, Map<String, Value> allVariables, long newTraceSize, long oldTraceSize, List<String> activeStates, List<SendMessage> listMsgs) throws Exception {
		if (isPortInUse("localhost",8090)) { //8090 used to send command to the local draw.io server
			try {
				
				JSONObject jsonObj = makeJSONobj(priorityEventCounter,capsuleInstance, region, path, allVariables, newTraceSize, oldTraceSize, activeStates, listMsgs);
				ViewEngine.sendJsonToServer(jsonObj.toJSONString());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			TrackerMaker.priorityEventCounter++;
			return true;
		}
		return false;
	}
	//==================================================================	
	//==============================================[makeJSONobj]
	//==================================================================
	private static JSONObject makeJSONobj(int priorityEventCounter, String capInstName, String region, String path, Map<String, Value> allVariables, long newTraceSize, long oldTraceSize, List<String> activeStates , List<SendMessage> listMsgs) {
		JSONObject jsonObj = new JSONObject();
		JSONArray traceID = new JSONArray();
		JSONArray listTRs = new JSONArray();
		traceID.add(priorityEventCounter); traceID.add(capInstName); traceID.add(region);
		String pathValue = "";
		if(path.contains(",")) {
			String [] pathsSplit = path.split("\\,");
			for (String str : pathsSplit) {
				//JSONObject objTR = new JSONObject();
				//objTR.put("name",PES.mapIdQname.get(str));
				//listTRs.add(objTR);
				if (pathValue.isEmpty())
					pathValue = PES.mapIdQname.get(str);
				else
					pathValue = pathValue +"," +PES.mapIdQname.get(str);
			}
		}else {
			pathValue = PES.mapIdQname.get(path);
		}
		traceID.add(pathValue);
		
		jsonObj.put("traceID", traceID);
		JSONArray traceVar = new JSONArray();
		//processing variables
		if(allVariables!=null) 
			for (Entry<String, Value> entry : allVariables.entrySet()) {//Sample: pongCount,Integer,7
				traceVar.add(entry.getKey()); traceVar.add(entry.getValue().getType()); traceVar.add(entry.getValue().asString());
			}
		
		jsonObj.put("traceVar", traceVar);
		JSONArray traceSizes = new JSONArray();
		traceSizes.add(newTraceSize);
		traceSizes.add(oldTraceSize);
		jsonObj.put("traceSizes", traceSizes);	
		
		JSONArray stateNameArray = new JSONArray();
		for (String stateName : activeStates) {
			stateNameArray.add(stateName);
		}
		jsonObj.put("activeStates", stateNameArray);
		int counter = 1;
		//System.err.println(capInstName+", ----------> listMsgs: "+listMsgs.toString());
		for (SendMessage sendMessage : listMsgs) {
			
			JSONArray msg = new JSONArray();
			msg.add(sendMessage.srcCapsuleInstance);
			msg.add(sendMessage.trgCapsuleInstance);
			
			if (!sendMessage.msg.isEmpty())
				msg.add(sendMessage.msg);
			else
				msg.add("");
			
			if (sendMessage.data != null)
				msg.add(sendMessage.data.toString());
			else
				msg.add("");
			
			jsonObj.put("msg"+counter, msg);
			counter++;
		}
		
		/*Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(jsonObj.toString());
		String prettyJsonString = gson.toJson(je);
		
		System.err.println(prettyJsonString);*/
		
		return jsonObj;

	}
	//==================================================================	
	//==============================================[isConsumable]
	//==================================================================
	public boolean isConsumable(Event event) throws InterruptedException {
		try {
			String capsuleInstance = dataContainer.getCapsuleInstance();
		switch (currentStatus) {
			case "StartUp":          	if (registerChecking(event))       {
				System.out.println(">>>>>>>>>>>>>>>["+ Thread.currentThread().getName() +"]--> ["+capsuleInstance+"]: StartUp received!");
				return true;};
				break;
			
			case "INIT":  	      		if (initChecking(event))           {
				System.out.println(">>>>>>>>>>>>>>>["+ Thread.currentThread().getName() +"]--> ["+capsuleInstance+"]: Init received!");
				return true;};
				break;
				
			case "TRANISTIONEND":     	if (transitionChecking(event))     {
				return true;};
				break;
		}

		return false;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
			
		}
	}
	
	
	//==================================================================	
	//==============================================[showInfo]
	//==================================================================
	public void showInfo(Event event) {
		System.out.println("=====================[CapsuleInstance]: " + dataContainer.getCapsuleInstance() +", [eventQueueTmp.size()]: "+eventQueueTmp.size() + ", [dataContainer.eventQueue.size():]: "+ dataContainer.eventQueue.size());
		if (event !=null)
			System.out.println("=====================[event]: " + event.allDataToString());
		else
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<[No Event Selected]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("=====================[listAllPathTaken]");
		int i = 0;
		
		for (String path : listAllPathTaken) {
			String pathCurr = "";
			if(path.contains(",")) {
				String [] pathsSplit = path.split("\\,");
				pathCurr = "";
				for (String str : pathsSplit) {
					if (pathCurr.isEmpty()) {
						pathCurr = PES.mapIdQname.get(str);
					}else {	
						pathCurr = pathCurr+ "-->" + PES.mapIdQname.get(str);
					}
				}
			}else {
				pathCurr = PES.mapIdQname.get(path);
			}
			System.out.println("["+i+++"]:" +pathCurr);
		}

		System.out.println("=====================[listPaths]");
		i = 0;
		for (String path : listPaths) {
			String pathCurr = "";
			if(path.contains(",")) {
				String [] pathsSplit = path.split("\\,");
				pathCurr = "";
				for (String str : pathsSplit) {
					if (pathCurr.isEmpty()) {
						pathCurr = PES.mapIdQname.get(str);
					}else {	
						pathCurr = pathCurr+ "-->" + PES.mapIdQname.get(str);
					}
				}
			}else {
				pathCurr = PES.mapIdQname.get(path);
			}
			System.out.println("["+i+++"]:" +pathCurr);
		}
		System.out.println("=====================[currentState]");
		for (Entry<String, String> entry : dataContainer.mapRegionCurrentState.entrySet()) {
			System.out.println("key: " + entry.getKey() + ", value: "+ ParserEngine.mapStateData.get(entry.getValue()).getStateName());
		}
		System.out.println("=====================[Active Region]:"+dataContainer.activeRegion);
		System.out.println("=====================[MSG List]");
		for (Map<String,SendMessage> listItem : dataContainer.listMapSendMessages) {
			for (Entry<String, SendMessage> entry : listItem.entrySet()) { 
				System.out.println("Name: " + entry.getKey());
			}
		}
		System.out.println("=====================[mapLocalHeap]");
		for (Entry<String, Value> entry : maplocalHeap.entrySet()) {
			System.out.println("key: " + entry.getKey() + ", value: "+ entry.getValue());
		}
		
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<[DONE]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
	}
	
	//==================================================================	
	//==============================================[updateCurrentState]
	//==================================================================		
	public boolean updateCurrentState () {
		String [] pathSplit;
		String lastId = "";
		String region = "";
		String region_begin = "";
		String region_begin_currentState = "";
		boolean firstTrTook = false;
		boolean trTookIntoCS = false;
		String trPrv = "";
		String borderStateID = "";

		
		if (listPaths.size()==1) {
			String path = listPaths.get(0);

			if(path.contains(",")) {
				pathSplit = path.split("\\,");
				lastId = pathSplit[pathSplit.length -1];
				for(String tr : pathSplit) {
					if (!firstTrTook) {
						region_begin = ParserEngine.mapTransitionData.get(tr).getReginName();
						region_begin_currentState = dataContainer.mapRegionCurrentState.get(region_begin);
						
						if (region_begin.contains("_")) {//finding border state
							int idx = region_begin.indexOf("_");
							String upperRegionName = region_begin.substring(0,idx);
							
							borderStateID = dataContainer.mapRegionCurrentState.get(upperRegionName);
							//System.err.println("region_begin: " + region_begin +" __________ uperRegionName: " + upperRegionName + ", borderStateID: "+borderStateID);
						}
						firstTrTook = true;
					}
					region = ParserEngine.mapTransitionData.get(tr).getReginName();
					String [] trSplit = ParserEngine.mapTransitionData.get(tr).getPath().split("\\-");
					//1. Check the target state is not a PseudoState
					//2. Avoid border state
					
					if (!trSplit[2].contentEquals(borderStateID) && (ParserEngine.mapStateData.get(trSplit[2]).getPseudoStateKind() == null)) {
						dataContainer.mapRegionCurrentState.put(region, trSplit[2]);
						dataContainer.activeRegion = ParserEngine.mapStateData.get(trSplit[2]).getRegion();
					}

					if (region.contains(region_begin+"_")) { //into the Composite State, We assuem that _ separates regions 
						String [] trPrvSplit = ParserEngine.mapTransitionData.get(trPrv).getPath().split("\\-");
						region_begin_currentState = trPrvSplit[2];
						trTookIntoCS = true;
					}
					trPrv = tr;
				}
			}else {
					lastId = path;
					region = ParserEngine.mapTransitionData.get(lastId).getReginName();
					String [] trSplit = ParserEngine.mapTransitionData.get(lastId).getPath().split("\\-");
					dataContainer.mapRegionCurrentState.put(region, trSplit[2]);	
					dataContainer.activeRegion = ParserEngine.mapStateData.get(trSplit[2]).getRegion();
				}
			
			if ( (trTookIntoCS || firstTrTook) && !region.contentEquals(region_begin)) { //recover the current state in the initial region
				dataContainer.mapRegionCurrentState.put(region_begin, region_begin_currentState);
			}
			
			//setActiveRegion when we backed to the composite state and we dont take the init TR in the CS
			if (!supplementaryActiveRegion.isEmpty()) {
				dataContainer.activeRegion = supplementaryActiveRegion;
				supplementaryActiveRegion = "";
			}
			
			
			return true;
		}else {
			//showInfo(null);
			//System.err.println("__________ There are too much path in the listPaths __________ size: " + listPaths.size());
			//System.exit(1);
		}
		return false;
	}

	//==================================================================	
	//======================================================[checkPathConsistency]
	//==================================================================		
	public boolean checkPathConsistency (String region, String path) {
		String firstId = "";
		if (path.contains(",")) {
			String [] pathSplit = path.split("\\,");
			firstId = pathSplit[0];
		}else {
			firstId = path;}
		String [] stateIds = ParserEngine.mapTransitionData.get(firstId).getPath().split("\\-");		
		
		if (((region.contentEquals(dataContainer.activeRegion)) && (dataContainer.mapRegionCurrentState.get(region) != null) && dataContainer.mapRegionCurrentState.get(region).contentEquals(stateIds[0])) || 
				((currentStatus.contentEquals("INIT") && !region.contains("_"))))
			return true;
		else 
			return false;
	}

	//==================================================================	
	//======================================================[jumpTr]
	//==================================================================		
	public boolean jumpTr (String id) {
		String soFarMatched = "";
		if (!listSoFarMachedTR.isEmpty()) {
			for (String path : listSoFarMachedTR) {
				if (soFarMatched.isEmpty())
					soFarMatched = path;
				else
					soFarMatched = soFarMatched +","+path;
			}
		}
		for (String path : listPaths) {
			if (!soFarMatched.isEmpty() && path.contains(soFarMatched+","+id)) {
				return false;
			}else if (!soFarMatched.isEmpty()) { 
				String lastId = "";
				if (path.contains(",")) {
					String [] pathSplit = path.split("\\,");
					lastId = pathSplit[pathSplit.length -1];
				}else {
					lastId = path;
				}
				if (listSoFarMachedTR.get(listSoFarMachedTR.size()-1).contentEquals(lastId))
					return false;
			}else if (soFarMatched.isEmpty()) {
				String firsId = "";
				if (path.contains(",")) {
					String [] pathSplit = path.split("\\,");
					firsId = pathSplit[0];
				}else {
					firsId = path;
				}
				
				if (id.contentEquals(firsId))
					return false;
			}
		}
		return true;
	}
	//==================================================================	
	//======================================================[pathFinder]
	//==================================================================		
	public boolean pathFinder (String qName) {

		String id = PES.mapQnameId.get(qName);
		String region = ParserEngine.mapTransitionData.get(id).getReginName();
		String capInstance = ParserEngine.mapTransitionData.get(id).getCapsuleInstanceName();
		boolean inOrderEvent = false;
		
		if (listPaths.isEmpty()) {
			//System.out.println(dataContainer.getCapsuleInstance()+", [pathFinder] 1 " + qName);
			for (Map.Entry<String, List<String>> entry : PES.mapRegionPaths.entrySet()) {
				if (entry.getKey().contains(capInstance+"::"+region)) {
					for (String path : entry.getValue()) {
						if (path.contains(id) && checkPathConsistency(region, path)) {
							listPaths.add(path);
							inOrderEvent = true;
						}
					}
					break;
				}
			}
		}else if ((listPaths.size() == 1) && !jumpTr(id)){
			inOrderEvent = true;
		}else if ((listPaths.size()>1) && listPaths.toString().contains(id) && !jumpTr(id)){
			inOrderEvent = true;
			List <String> listPathsTmp =  new ArrayList<String>();
			listPathsTmp.clear();
			
			for (String path : listPaths) {
				if (path.contains(id)){
					listPathsTmp.add(path);
				}
			}
			
			listPaths.clear();
			listPaths.addAll(listPathsTmp);

			if (listPaths.size() == 2) { //For pahts like: Recover-->PassiveMode-->initTrBackup & Recover-->PassiveMode
				String currentState = "";
				capInstance = "";
				region = "";
				boolean initRefined = false;
				Iterator<String> iter = listPaths.iterator();
				while (iter.hasNext()) {
				    String path = iter.next();
					String lastId = findLastID(path);
					
					if(ParserEngine.mapTransitionData.get(lastId).getIsInit()) {
						region = ParserEngine.mapTransitionData.get(lastId).getReginName();
						currentState = dataContainer.mapRegionCurrentState.get(region);
						if((currentState != null) && !currentState.contains("INIT")) {
							//remove path with initTr, so listPaths.size == 1
							iter.remove();
							initRefined = true;
						}else if ((currentState != null) && currentState.contains("INIT")){
							//remove path without initTr, so listPaths.size == 1
							listPaths.clear();
							listPaths.add(path);
							initRefined = true;
						}
						
					}
				}
				//listPahts has been shrunk to 1
				if ((listPaths.size() == 1) && initRefined && !currentState.contains("INIT")) { // For paths like Recover-->PassiveMode
					//Path Recover-->PassiveMode should be taken + the entry action code of the active state like (TryToBeMaster)
					//add the entry action code as the supplimentary actionCode
					String [] stateTrState = ParserEngine.mapTransitionData.get(findLastID(listPaths.get(0))).getPath().split("\\-");
					
					String innerRegion = findRegionByState(ParserEngine.mapStateData.get(stateTrState[2]).getStateName()); 
					String activeStateID = dataContainer.mapRegionCurrentState.get(innerRegion);
					listSupplementaryAC.add(ParserEngine.mapStateData.get(activeStateID).getEntrAC());
					supplementaryActiveRegion = innerRegion;
					//System.err.println(qName +", "+ dataContainer.getCapsuleInstance()+ " __________ listSupplementaryAC in very rare situation : "+listSupplementaryAC.toString());

				}	
			}
		}
		return inOrderEvent;
	}

	//==================================================================	
	//==============================================[findRegionByState]
	//==================================================================		
	public String findRegionByState (String stateName) {
		
		for (Entry<String, List<Map<String, String>>> entry : PES.mapModelRegionPaths.entrySet()) {
			if (entry.getKey().contains(dataContainer.getCapsuleInstance())) {
				List<Map<String, String>>paths = entry.getValue();
				for (int i = 0; i<paths.size(); i++) {
					if (paths.get(i).values().contains(stateName)) {
						return paths.get(i).keySet().toString().replaceAll("\\[", "").replaceAll("\\]","");
					}
				}
			}	
		}
		System.err.println("______________ ERROR : No region found by the given stateName: "+ stateName);
		System.exit(1);
		return null;
	}

	
	//==================================================================	
	//==============================================[findFistID]
	//==================================================================		
	public String findFistID (String path) {
		String firstId = "";
		if (path.contains(",")) {
			String [] pathSplit = path.split("\\,");
			firstId = pathSplit[0];
		}else {
			firstId = path;
		}
		return firstId;
	}

	//==================================================================	
	//==============================================[findLastID]
	//==================================================================		
	public String findLastID (String path) {
		String lastId = "";
		if (path.contains(",")) {
			String [] pathSplit = path.split("\\,");
			lastId = pathSplit[pathSplit.length -1];
		}else {
			lastId = path;
		}
		return lastId;
		 
	}
	
	//==================================================================	
	//==============================================[setLogicalVectorTime]
	//==================================================================		
	public void setLogicalVectorTime (int [] logicalVT) {
		for (int i = 0; i<logicalVectorTime.length -1 ; i++)
		if (logicalVectorTime[i]< logicalVT[i]) {
			logicalVectorTime[i] = logicalVT[i];
		}
	}

	//==================================================================	
	//==============================================[toHistory]
	//==================================================================	
	public boolean toHistory(List<String> list) {
		
		for(String path : list) {
			if(path.contains(",")) {
				String [] regionPathSplit = path.split("\\,");
				if(ParserEngine.mapTransitionData.get(regionPathSplit[regionPathSplit.length-1]).getIsInit())
					return true;
			}
		}
		return false;
	}

	//==================================================================	
	//==============================================[registerChecking]
	//==================================================================
	public boolean registerChecking(Event event) {

		//Check transitionKind = 10 and eventType = 36 [For REGISTER] 
		if (event.getSourceKind().contentEquals("10") && event.getType().contentEquals("36")) {
			currentStatus = "INIT";
			return true;
		}
		return false;
	}

	//==================================================================	
	//==============================================[initChecking]
	//==================================================================	
	public boolean initChecking(Event event) throws InterruptedException {
		//System.out.println(dataContainer.getCapsuleInstance() +", in initChecking "+ listPaths.size());

		boolean result = false;
		String id = PES.mapQnameId.get(event.getSourceName());
		
		//Samples: PingPong::Pinger::PingerStateMachine::Region::PLAYING , PingPong::Pinger::PingerStateMachine::Region::onPong

		if (event.getSourceKind().contentEquals("3") && event.getType().contentEquals("14") && ParserEngine.mapTransitionData.get(id).getIsInit()) {
			//System.out.println(dataContainer.getCapsuleInstance() + "============> [Status : initChecking]:> "+ event.allDataToString());
			
			String [] pathSplit = ParserEngine.mapTransitionData.get(id).getPath().split("\\-");
			dataContainer.mapRegionCurrentState.put(ParserEngine.mapTransitionData.get(id).getReginName(),pathSplit[2]);
			dataContainer.activeRegion = ParserEngine.mapStateData.get(pathSplit[2]).getRegion();
			
			pathFinder(event.getSourceName());
			if ((listPaths.size() == 1)) {
				result =  true;
				currentStatus = "TRANISTIONEND";
			}else if ((listPaths.size() > 1)){
				result = false;
			
			}else {
				//System.err.println("__________ No Path Found! __________");
				//System.exit(1);
			}
		}
		return result;
	}
	
	
	//==================================================================	
	//==============================================[sendMessages]
	//==================================================================	
	public void sendMessages() throws InterruptedException {

		for (SendMessage sendMessage : listPortMsg) {
			sendMessage.srcCapsuleInstance = dataContainer.getCapsuleInstance();
			List<MyConnector> listMyConnectors = new ArrayList <MyConnector>();
			String trgCapsule = "";
			if (sendMessage.dest != null) {
				//System.out.println("sendMessage.dest != null");
				if (sendMessage.dest.asString().contentEquals("msg->sapIndex0_")) { // msg->sapIndex0 returns port index that the msg came in
	            	 for (MyConnector myConnector : ParserEngine.listMyConnectors) {
	            		 if (msgSender.contains(myConnector.capInstanceName1) && dataContainer.getCapsuleInstance().contains(myConnector.capInstanceName2)) {
	            			 sendMessage.dest = new Value(myConnector.port2Idx,"Integer");
	            			 break;
	            		 }else if (msgSender.contains(myConnector.capInstanceName2) && dataContainer.getCapsuleInstance().contains(myConnector.capInstanceName1)){
	            			 sendMessage.dest = new Value(myConnector.port1Idx,"Integer");
	            			 break;
	            		 }
	            	 }            	 
	        	 }	
				
				for ( CapsuleConn capConn : ParserEngine.listCapsuleConn) {
					if (capConn.getCapsuleInstanceName().contains(dataContainer.getCapsuleInstance())) { //e.g.: [capConn.getCapsuleInstanceName(): Simulator__server1, Simulator__server2] [dataContainer.getCapsuleInstance(): Simulator__server1]
						listMyConnectors = capConn.getListMyConnector();
						for (MyConnector connector : listMyConnectors) { 
							if (connector.portCap1.contentEquals(sendMessage.port) && (connector.port1Idx == sendMessage.dest.asInteger())) {
								trgCapsule = connector.capInstanceName2;
								sendMessage.trgCapsuleInstance = getCapInstanceName(sendMessage.srcCapsuleInstance, trgCapsule);
								break;
							}else if (connector.portCap2.contentEquals(sendMessage.port)&& (connector.port2Idx == sendMessage.dest.asInteger())) {
								trgCapsule = connector.capInstanceName1;
								sendMessage.trgCapsuleInstance = getCapInstanceName(sendMessage.srcCapsuleInstance, trgCapsule);
								break;
							}
						}
						break;
					}
				}
				
			}else {
				for ( CapsuleConn capConn : ParserEngine.listCapsuleConn) {
					if (capConn.getCapsuleInstanceName().contains(dataContainer.getCapsuleInstance())) {

						listMyConnectors = capConn.getListMyConnector();
						for (MyConnector connector : listMyConnectors) { 
							if (connector.portCap1.contentEquals(sendMessage.port) && sendMessage.srcCapsuleInstance.contains(connector.capInstanceName1) && (!sendMessage.srcCapsuleInstance.contains(connector.capInstanceName2))) {
								trgCapsule = connector.capInstanceName2;
								sendMessage.trgCapsuleInstance = getCapInstanceName(sendMessage.srcCapsuleInstance, trgCapsule);
								break;
							}else if (connector.portCap2.contentEquals(sendMessage.port) && sendMessage.srcCapsuleInstance.contains(connector.capInstanceName2) && (!sendMessage.srcCapsuleInstance.contains(connector.capInstanceName1))) {
								trgCapsule = connector.capInstanceName1;
								sendMessage.trgCapsuleInstance = getCapInstanceName(sendMessage.srcCapsuleInstance, trgCapsule);
								break;
							}
						}
						break;
					}
				}
			}

			System.out.println("In " + dataContainer.getCapsuleInstance() + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>> msg: " + sendMessage.msg + " with dataName "+ sendMessage.dataName+ " : " + sendMessage.data +", is sending via " + sendMessage.port +" to "+ trgCapsule);
			//Thread.currentThread().sleep(300);
			if (trgCapsule.isEmpty()){
				System.err.println("In " + dataContainer.getCapsuleInstance() + ">>>>>>>>>>>>>>>>>>>>>>>>>>>>> msg: " + sendMessage.msg + ", is sending via " + sendMessage.port +" to "+ trgCapsule);
				System.err.println(sendMessage.srcCapsuleInstance + "  In " + dataContainer.getCapsuleInstance() + ">>>>>>>>>>>>> msgSender: " +msgSender +", dest: " + sendMessage.dest.asString());
				System.err.println("__________ Target Capsule not Found! __________");
				System.exit(1);
			}
				
			
			Iterator<Entry<String, CapsuleTracker>> itr = TrackerMaker.mapCapsuleTracker.entrySet().iterator();  
			while(itr.hasNext()) 
	        { 
				Entry<String, CapsuleTracker> entry = itr.next();
				if (entry.getKey().contains(trgCapsule)) {
					if ((sendMessage.data != null) && (sendMessage.dataName.contentEquals("__getName__") || sendMessage.data.toString().contains("__CapInstanceName__"))) { //TODO: find the corresponding varibale name in the target capsule; we have the same rule in the AC.g4
						sendMessage.data = new Value (separateTopFromInstanceName(dataContainer.getCapsuleInstance()),"String");
						Map<String,SendMessage> mapMsgToSend =  new HashMap<String, SendMessage>();
						mapMsgToSend.put(sendMessage.msg, sendMessage);
						entry.getValue().dataContainer.listMapSendMessages.add(mapMsgToSend);
						//TrackerMaker.showAllMSGlists();
					}else {
						Map<String,SendMessage> mapMsgToSend =  new HashMap<String, SendMessage>();
						mapMsgToSend.put(sendMessage.msg, sendMessage);
						entry.getValue().dataContainer.listMapSendMessages.add(mapMsgToSend);
						//TrackerMaker.showAllMSGlists();
					}
					break;
				}
	        }
			
			listMyConnectors = null;
		}
	}
	//==================================================================	
	//==============================================[getCapInstanceName]
	//==================================================================	
	public String getCapInstanceName(String srcCapName, String trgCapName) {
		
		int idx = srcCapName.indexOf("__");
		String topCapName = srcCapName.substring(0,idx);
		
		return topCapName+"__"+trgCapName;
			
	}
	//==================================================================	
	//==============================================[interpretActionCode]
	//==================================================================	
	public void interpretActionCode(String actionCode, String msgSender , boolean guardEval) throws InterruptedException {
        //System.out.println("[actionCode]: "+actionCode);        
		ACLexer lexer = new ACLexer(new ANTLRInputStream(actionCode));
		ACParser parser = new ACParser(new CommonTokenStream(lexer));
		EvalVisitor visitor = new EvalVisitor(maplocalHeap,separateTopFromInstanceName(dataContainer.getCapsuleInstance()));
		visitor.visit(parser.parse());
		Iterator<Entry<String, Value>> itr = visitor.getHeapMem().entrySet().iterator(); 
        
        while(itr.hasNext()) 
        { 
             Map.Entry<String, Value> entry = itr.next();
             if (entry.getValue().toString().contentEquals("__CapInstanceName__")) {
            	 maplocalHeap.put(entry.getKey(), new Value (separateTopFromInstanceName(dataContainer.getCapsuleInstance()),"String"));
             }else if (entry.getValue().toString().contentEquals("msg->sapIndex0")) { // msg->sapIndex0 returns port index that the msg came in
            	 //who did this path trigger ? (which message from which port)
            	 for (MyConnector myConnector : ParserEngine.listMyConnectors) {
            		 if (msgSender.contains(myConnector.capInstanceName1) && dataContainer.getCapsuleInstance().contains(myConnector.capInstanceName2)) {
            			 maplocalHeap.put(entry.getKey(), new Value(myConnector.port2Idx,"Integer"));
            			 break;
            		 }else if (msgSender.contains(myConnector.capInstanceName2) && dataContainer.getCapsuleInstance().contains(myConnector.capInstanceName1)){
            			 maplocalHeap.put(entry.getKey(), new Value(myConnector.port1Idx,"Integer"));
            			 break;
            		 }
            		 
            	 }            	 
        	 }else {
            	 maplocalHeap.put(entry.getKey(), entry.getValue());
        	 }
        }
        
        if (!guardEval) {
			//System.err.println(">>>>>>>>>>>>> In: "+ dataContainer.getCapsuleInstance() + ", actionCode: "+actionCode+ " , calling sendMessages in [interpretActionCode] because guardEval is  "+ guardEval +", visitor.getListPortMsg():" + visitor.getListPortMsg().toString());
        	listPortMsg = visitor.getListPortMsg();
        	copyListPortMsg.addAll(listPortMsg);
        	sendMessages();
        }
		
		lexer = null;
		parser = null;
		visitor = null;
		
	}
	//==================================================================	
	//==============================================[processTriggers]
	//==================================================================	
	public void processTriggers(String path) {
		List<String> listTrigger = new ArrayList<String>();

		listTrigger = mapPathTrigger.get(path);

		if (!listTrigger.isEmpty()) {

			for(String msg : listTrigger) {
				String [] msgSplit = msg.split("\\.");
				for (Map<String,SendMessage> listItem : dataContainer.listMapSendMessages) {
					SendMessage sendMessage = listItem.get(msgSplit[1]);
					if ((sendMessage != null) && (sendMessage.dataName != null) && (sendMessage.data != null)) { 
						maplocalHeap.put(sendMessage.dataName, sendMessage.data);
						break;
					}
				}
			}
		}
	}
	//==================================================================	
	//==============================================[updateLocalHeap]
	//==================================================================	
	public void updateLocalHeap(String path, String msgSender, boolean guardEval) throws InterruptedException {
		List<String> listActionCode = new ArrayList<String>();
		String firstTrInPath = findFistID(path);
			
		processTriggers(path);
		
		listActionCode = mapPathActionCode.get(path);
		
		for(String ac : listActionCode) {
			if (!ac.isEmpty())
				interpretActionCode(ac,msgSender, guardEval);
		}
		for(String ac : listSupplementaryAC) { //when we back to the composite state we need to exe entry action code of the active state in the innerRegion
			guardEval = false; 
			if (!ac.isEmpty())
				interpretActionCode(ac,msgSender, guardEval);
		}
		listSupplementaryAC.clear();
	}
	//==================================================================	
	//==============================================[makeListTrigger]
	//==================================================================	
		public List<String> makeListTrigger(String path) throws InterruptedException {
			List<String> listTrigger = new ArrayList<String>();
			List<String> listGuards = new ArrayList<String>();
			List<String> listActionCode = new ArrayList<String>();
			
			if (path.contains(",")) {
				String [] pathSplit = path.split("\\,");
				for (String pathID : pathSplit) {
					listTrigger.addAll(ParserEngine.mapTransitionData.get(pathID).getTriggers());
					listGuards.addAll(ParserEngine.mapTransitionData.get(pathID).getGuard());
					listActionCode.add(ParserEngine.mapTransitionData.get(pathID).getActionCode());
					
					String [] trPartsID = ParserEngine.mapTransitionData.get(pathID).getPath().split("\\-");
					if (ParserEngine.mapStateData.get(trPartsID[0]).getExitAC() != null)
						listActionCode.add(ParserEngine.mapStateData.get(trPartsID[0]).getExitAC());
					if (ParserEngine.mapStateData.get(trPartsID[2]).getEntrAC() != null)
						listActionCode.add(ParserEngine.mapStateData.get(trPartsID[2]).getEntrAC());
					
				}
			}else {
				listTrigger.addAll(ParserEngine.mapTransitionData.get(path).getTriggers());
				listGuards.addAll(ParserEngine.mapTransitionData.get(path).getGuard());
				listActionCode.add(ParserEngine.mapTransitionData.get(path).getActionCode());
				
				
				String [] trPartsID = ParserEngine.mapTransitionData.get(path).getPath().split("\\-");
				if (ParserEngine.mapStateData.get(trPartsID[0]).getExitAC() != null)
					listActionCode.add(ParserEngine.mapStateData.get(trPartsID[0]).getExitAC());
				if (ParserEngine.mapStateData.get(trPartsID[2]).getEntrAC() != null)
					listActionCode.add(ParserEngine.mapStateData.get(trPartsID[2]).getEntrAC());
			}
			if (listTrigger != null) {
				mapPathGuards.put(path, listGuards);
				mapPathTrigger.put(path, listTrigger);
				mapPathActionCode.put(path, listActionCode);
				
			}else {
				System.err.println("__________ No Trigger Found! __________"); //TODO: make it clean!
				System.exit(1);
			}
				
			return listTrigger;
		}
	
	//==================================================================	
	//==============================================[isRequirementMet]
	//==================================================================	
	public boolean isRequirementMet(String path) throws InterruptedException {
		Thread.currentThread().sleep(50);
		
		boolean result = false;
		msgTobeConsumed = "";
		List<String> listTrigger = new ArrayList<String>();
		List<String> listGuards = new ArrayList<String>();
		List<String> listActionCode = new ArrayList<String>();
		
		listTrigger = mapPathTrigger.get(path);
		listGuards = mapPathGuards.get(path);

		if (listTrigger == null){
			listTrigger = makeListTrigger(path);
		}
		
		if (listTrigger.isEmpty())
			result = true;
		else {
			result = true;
			for(String msg : listTrigger) {
				if(!msg.contains("__TIMER__")){
					result = false;
				}
			}

			for(String msg : listTrigger) {
				String [] msgSplit = msg.split("\\.");
				for (Map<String,SendMessage> listItem : dataContainer.listMapSendMessages) {
					if ((listItem.get(msgSplit[1]) != null)) {
						msgSender = listItem.get(msgSplit[1]).srcCapsuleInstance;
						result = true;
						msgTobeConsumed = msgSplit[1];
						break;
					}
				}
				if (!msgTobeConsumed.isEmpty())
					break;
			}
		}
		/*
		//GUARD EVALUATION -new version
		if (result && listGuards != null) {
			//make a local copy before guard evaluation
			Map<String, Value> maplocalHeapCopy = new HashMap<String, Value>();
			maplocalHeapCopy.putAll(maplocalHeap);
			updateLocalHeap(path,"",true);
			
			if (path.contains(",")) {
				String [] TRs =  path.split("\\,"); 
				for (String tr : TRs) {
					String ac = ParserEngine.mapTransitionData.get(tr).getActionCode();
					if (!ac.isEmpty())
						interpretActionCode(ac,msgSender, true);
					List<String> listGuard = ParserEngine.mapTransitionData.get(tr).getGuard();
					if (!listGuard.isEmpty()) {
						for (String guard : listGuard) {
							interpretActionCode(guard,msgSender, true);
							boolean guardBool = maplocalHeap.get("__GUARD__").asBoolean();
							if (!guardBool) {
								System.out.println(dataContainer.getCapsuleInstance() +", path: "+path+", listSoFarMachedTR: "+listSoFarMachedTR.toString() + ", GUARD evaluated false!, "+guard);
								result = false;
								break;
							}
							maplocalHeap.remove("__GUARD__");
						}
						if (!result)
							break;
					}		
				}
			}else {}//path with one tr does not have Guard!
			maplocalHeap.clear();
			maplocalHeap.putAll(maplocalHeapCopy);
		}
		*/
		
		//GUARD EVALUATION -old version
		 /* if (result && listGuards != null) {
			//make a local copy before guard evaluation
			Map<String, Value> maplocalHeapCopy = new HashMap<String, Value>();
			maplocalHeapCopy.putAll(maplocalHeap);
			updateLocalHeap(path,"",true);
			
			for(String ac : listGuards) {
				if (!ac.isEmpty()) {
					//System.out.println(dataContainer.getCapsuleInstance() + " AC: " + ac);
					interpretActionCode(ac,msgSender, true);
					boolean guard = maplocalHeap.get("__GUARD__").asBoolean();
					//System.out.println("guard: " + guard);
					if (!guard) {
						System.out.println(dataContainer.getCapsuleInstance() +", path: "+path+", listSoFarMachedTR: "+listSoFarMachedTR.toString() + ", GUARD evaluated false!, "+ac);
						result = false;
						break;
					}
					maplocalHeap.remove("__GUARD__");
				}
			}
			maplocalHeap.clear();
			maplocalHeap.putAll(maplocalHeapCopy);
		}*/
	
		//if (!result)
		//	TrackerMaker.showAllMSGlists();

		return result;
			
	}
	
	//==================================================================	
	//==============================================[transitionChecking]
	//==================================================================	
	public boolean transitionChecking(Event event) throws InterruptedException {
		boolean result = false;
		String id = PES.mapQnameId.get(event.getSourceName());

		String currentSateId = dataContainer.mapRegionCurrentState.get(ParserEngine.mapTransitionData.get(id).getReginName());
		
		//Samples: PingPong::Pinger::PingerStateMachine::Region::PLAYING , PingPong::Pinger::PingerStateMachine::Region::onPong

		if (event.getSourceKind().contentEquals("3") && event.getType().contentEquals("14")) {
			boolean inOrderEvent = pathFinder(event.getSourceName());
			
			if ((listPaths.size() == 1) && inOrderEvent) {
				result =  true;
				currentStatus = "TRANISTIONEND";
			}else if ((listPaths.size() > 1) && inOrderEvent) {
				result =  true;
				currentStatus = "TRANISTIONEND";
			}else if ((listPaths.size() > 0) && !inOrderEvent){
				//System.err.println("__________ EVENT is received out of order! __________ [EVENT]:" + event.allDataToString());
				//System.out.println("listPaths.toString(): " + listPaths.toString());
				currentStatus = "TRANISTIONEND";
				result = false;
			}else if (listPaths.size() == 0) {
				result = false;
				//System.err.println("__________ No Path Found! __________[EVENT]:" + event.allDataToString());
				//System.exit(1);
			}
		}
		return result;
	}
	//==================================================================	
	//==============================================[outputStreamToFile]
	//==================================================================	
	private static void outputStreamToFile(OutputStream os, String data) {
		try {
			os.write(data.getBytes(), 0, data.length());
		} catch (IOException e) {
			e.printStackTrace();
		}/*finally{
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
	}

	//==================================================================	
	//==============================================[isPortInUse]
	//==================================================================
	public static boolean isPortInUse(String host, int port) {
		  // Assume no connection is possible.
		  boolean result = false;

		  try {
		    (new Socket(host, port)).close();
		    result = true;
		  }
		  catch(SocketException e) {
		    // Could not connect.
		  } catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		  return result;
		}
	
	@SuppressWarnings("deprecation")
	public void shutdown() {
		Thread.currentThread().stop();
	}


}
