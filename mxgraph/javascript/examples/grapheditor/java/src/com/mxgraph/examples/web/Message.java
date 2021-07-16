package com.mxgraph.examples.web;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;



public class Message implements Comparable<Message> {
	private Integer priorityEventCounter;
	private String capsuleName;
	private String region;
	private String path;
	private HashMap<String, String> vatriablesHashMap;
	private String newTraceSize;
	private String oldTraceSize;
	private String greenState;
	private String grayState;
	public List<String> listMsgs;
	
	public Message(int priorityEventCounter, String capsuleName, String region, String path, String newTraceSize, String oldTraceSize, String greenState, String grayState ) {
		this.priorityEventCounter = priorityEventCounter;
		this.capsuleName = capsuleName;
		this.region = region;
		this.path = path;
		this.vatriablesHashMap = new HashMap<String, String>();
		this.oldTraceSize = oldTraceSize;
		this.newTraceSize = newTraceSize;
		this.greenState = greenState;
		this.grayState = grayState;
		this.listMsgs = new ArrayList<String>();
	}
	public Message() {
		this(0,null,null, null,"0","0", null,null);
	}
	public int getPriorityEventCounter() {
		return priorityEventCounter;
	}
	public String getCapsuleName() {
		return capsuleName;
	}
	public String getRegion() {
		return region;
	}
	public String getPath() {
		return path;
	}
	public String getNewTraceSize() {
		return newTraceSize;
	}
	public String getOldTraceSize() {
		return oldTraceSize;
	}
	public void putToVatriablesHashMap(String key, String value) {
		vatriablesHashMap.put(key, value);
	}
	public HashMap<String, String> getVatriablesHashMap() {
		return vatriablesHashMap;
	}
	public void setGreenState(String state) {
		greenState = state;
	}
	public void setGrayState(String state) {
		grayState = state;
	}
	
	@Override
	public int compareTo(Message msg) {
		return priorityEventCounter.compareTo(msg.getPriorityEventCounter());
	}
	public String makeJSON() {
		String result = "{\"traceID\":[\""+capsuleName+"\", \""+ region+ "\"";
		result = result+",[";
		if (path.contains(",")) {
			String [] pathsSplit = path.split("\\,");
			for (String str : pathsSplit) {
				if (str.contentEquals(pathsSplit[0]))
					result = result+"{\"name\":\""+str+"\"}";
				else
					result = result+",{\"name\":\""+str+"\"}";
			}
		}else {
			result = result+"{\"name\":\""+path+"\"}";
		}
		result = result+"]";
		
		Iterator iterator = vatriablesHashMap.entrySet().iterator();
		int counter = 0;
		if (iterator.hasNext()) {
			

			result = result+"],\"traceVar\":[";
			while(iterator.hasNext())
			{
				counter++;
				Map.Entry mentry = (Map.Entry) iterator.next();
				result = result+"{\"name\":\""+mentry.getKey()+"\", \"value\": \""+mentry.getValue() +"\"},";
				//System.out.println("["+ counter+"]> (Name): "+ mentry.getKey() + ", (Variable): " + mentry.getValue());
			}
		
			result = result.substring(0, result.length() - 1);
		}
		result =  result+"],\"traceSizes\":[\""+newTraceSize+"\",\""+oldTraceSize+"\"], \"activeStates\":[\""+greenState+"\", \""+grayState+"\"]";
		
		counter = 1;
		
		for (String msg : listMsgs) {
			String [] msgSplit = msg.split("\\,"); 
			String jsonKey = "msg"+counter;
			System.out.println("\n[ReplayNextServlet]> jsonKey: "+ jsonKey);

			if (msgSplit.length > 3)
				result =  result+",\""+jsonKey+"\":[\""+msgSplit[0]+"\",\""+msgSplit[1]+"\",\""+msgSplit[2]+"\",\""+msgSplit[3]+"\"]";	
			else
				result =  result+",\""+jsonKey+"\":[\""+msgSplit[0]+"\",\""+msgSplit[1]+"\",\""+msgSplit[2]+"\"]";	
			//System.out.println("\n[ReplayNextServlet]> result: "+ result);

			counter++;
		}
		System.out.println("result: "+result+"}");
		return result+"}";
		
	}
} 