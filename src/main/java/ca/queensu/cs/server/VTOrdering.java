package ca.queensu.cs.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ca.queensu.cs.controller.CapsuleTracker;

public class VTOrdering implements Runnable {
	private Event event;
	private String capsuleInstance;
	private String sourceName;
	private int[] localVT;
	private int MAX;
	private static BlockingQueue<Event> eventQueueVT;

	public VTOrdering() {
		MAX = 100;
		event = new Event();
		localVT = new int[MAX]; //TODO
		capsuleInstance = "__NOT_FOUND__";
		sourceName      = "__NOT_FOUND__";
		eventQueueVT = new LinkedBlockingQueue<Event>();
	}

	public final void run() {
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<[VECTOR-TIME ORDERING SERVER IS RUNNING]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
		long t1 = System.currentTimeMillis();
		int totalSize = 0;
		
		while(true) {
			
			if (ServerVTOrdering.priorityEventCounter>50000)
			{
				System.err.println("=====================================[EXPERIMENT DONE]======================================");
				long t2 = System.currentTimeMillis();
				 System.err.println("VT_orderingTime: "+ (t2-t1));
				 System.err.println("Total size: " + totalSize);
				 System.exit(0);
			}
			
			if (!ServerVTOrdering.eventQueue.isEmpty()) {
				try {
					event = ServerVTOrdering.eventQueue.take();
					if (event.getCapsuleInstance() != null) {
						capsuleInstance = event.getCapsuleInstance();
					}
					if ((event.getSourceName() != null)) {
						int lastIndex = event.getSourceName().lastIndexOf("::");
						if ((event.getSourceName().length()>lastIndex+2))
							sourceName = event.getSourceName().substring(lastIndex+2);
					}
					try {
						//System.out.println("[Event]: "+capsuleInstance+", " +sourceName + "\n\n");
						if ((sourceName != "__NOT_FOUND__") && (capsuleInstance != "__NOT_FOUND__")) {
							System.out.println("ServerVTOrdering.priorityEventCounter: " + ServerVTOrdering.priorityEventCounter);
							
							if (event.getVT().isEmpty()) {
								totalSize += event.eventSize("vt");
								//CapsuleTracker.jsonToServer(ServerVTOrdering.priorityEventCounter++,capsuleInstance,sourceName,event.getVariableData());
							}else if (vtIsRespected(event.getVT())) {
								for (int i = 0; i < 4; i++){
									System.out.println("LOCAL VT ["+i+"]: " + localVT[i]);
								}
								
								System.out.println("CURRENT VT: " + event.getVT());
								
								//CapsuleTracker.jsonToServer(ServerVTOrdering.priorityEventCounter++,capsuleInstance,sourceName,event.getVariableData());
								totalSize += event.eventSize("vt");
							}else {
								eventQueueVT.put(event);
								event = lookForTheNextEvent();
								if(event != null) {
									if (event.getCapsuleInstance() != null) {
										capsuleInstance = event.getCapsuleInstance();
									}
									if ((event.getSourceName() != null)) {
										int lastIndex = event.getSourceName().lastIndexOf("::");
										if ((event.getSourceName().length()>lastIndex+2))
											sourceName = event.getSourceName().substring(lastIndex+2);
									}
									//CapsuleTracker.jsonToServer(ServerVTOrdering.priorityEventCounter++,capsuleInstance,sourceName,event.getVariableData());
									totalSize += event.eventSize("vt");
								}/*else {
									System.err.println("=====[No event found in eventQueueVT!]=====");
									System.exit(0);
								}*/
							}

						}
						capsuleInstance = "__NOT_FOUND__";
						sourceName      = "__NOT_FOUND__";
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.err.println("===[WEB_SERVER CONNECTION FAILD]===");
						System.err.println("=====[PROGRAM TERMINATED]=====");
						System.exit(0);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		
	}
	//==================================================================	
	//==============================================[vtIsRespected]
	//==================================================================	
	public boolean vtIsRespected(String vt) throws InterruptedException {

		//vt to int array
		String[] vtSplited = vt.split(" "); 
		// Splits each spaced integer into a String array.
		int[] vtArray = new int[vtSplited.length]; 
		// Creates the integer array.
		for (int i = 0; i < vtArray.length; i++){
			vtArray[i] = Integer.parseInt(vtSplited[i]);
			if (localVT[i]>vtArray[i]) {
				return false;
			}
		}
		//Assign to the localVT
		for (int i = 0; i < vtArray.length; i++){
			localVT[i] = vtArray[i];
		}   
		return true;
	}
	//==================================================================	
	//==============================================[lookForTheNextEvent]
	//==================================================================	
	public Event lookForTheNextEvent() throws InterruptedException {
		BlockingQueue<Event> tmpEventQueueVT = new LinkedBlockingQueue<Event>();
		for(int i=0; i<eventQueueVT.size();i++) {
			Event eventTmp = eventQueueVT.take();
			String vt = eventTmp.getVT();
			//System.out.println("VT: " + vt);
			if (vtIsRespected(vt)) {
				for(int j=0; j<tmpEventQueueVT.size();i++) {
					eventQueueVT.put(tmpEventQueueVT.take());
				}
				tmpEventQueueVT = null;
				return eventTmp;
			}else {
				tmpEventQueueVT.put(eventTmp);
				eventQueueVT.put(eventTmp);
			}
		}
		return null;

	}

}
