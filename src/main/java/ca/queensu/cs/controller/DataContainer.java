package ca.queensu.cs.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

import ca.queensu.cs.antler4AC.SendMessage;
import ca.queensu.cs.antler4AC.Value;
import ca.queensu.cs.server.Event;
import ca.queensu.cs.server.Server;
import ca.queensu.cs.umlrtParser.TableDataMember;

/*

Developers:
Majid Babaei (babaei@cs.queensu.ca): Initial development - 120918

 */



public class DataContainer {
	
	private String capsuleInstance;
	private String capsuleName;
	public PriorityBlockingQueue <Event> eventQueue;
	public List<Map<String,SendMessage>> listMapSendMessages;
	public Map<String, String> mapRegionCurrentState;
	public String activeRegion;
	
	//private String threadName;
	//--
	public DataContainer(String capsuleName, String capsuleInstance, PriorityBlockingQueue <Event> eventQueue) {
		this.capsuleName = capsuleName;
		this.mapRegionCurrentState =  new HashMap<String, String>();
		this.capsuleInstance = capsuleInstance;
		this.eventQueue = eventQueue;
		this.listMapSendMessages =  new ArrayList<Map<String,SendMessage>>();
		this.activeRegion = "";
	}
	//--
	public DataContainer() {
		this(null, null, null);
	}
	
	  
	//--GETTERS
	public String getCapsuleName() {
		return capsuleName;
	}
	public String getCapsuleInstance() {
		return this.capsuleInstance;
	}
	public Queue getEventQueue() {
		return this.eventQueue;
	}

	//--SETTERS
	public void setCapsuleInstance(String capsuleInstance ) {
		this.capsuleInstance = capsuleInstance;
	}
	public void setEventToEventQueue(Event event) throws InterruptedException {
		this.eventQueue.put(event);
	}
	public void setEventQueue(PriorityBlockingQueue <Event> eventQueue) {
		this.eventQueue = eventQueue;
	}
	
	public void removeFromListMapSendMessages(String msg) {
		boolean removed =false;
		for (Map<String,SendMessage> listItem : listMapSendMessages) {
			for (Entry<String, SendMessage> entry : listItem.entrySet()) { 
				if (entry.getKey().contentEquals(msg)) {
					listItem.remove(msg);
					removed = true;
					break;
				}
			}
			if (removed)
				break;
		}
	}
	
	public String showMapMSG() {
		String result = "capsuleInstance: " + this.capsuleInstance + ", MSG [ ";
		for (Map<String,SendMessage> listItem : listMapSendMessages) {
			for (Entry<String, SendMessage> entry : listItem.entrySet()) { 
				result = result + "[Name: " + entry.getKey() + ", value: "+ entry.getValue() + "], ";
				
			}
		}
		return result + " ]";
	}
	

}
