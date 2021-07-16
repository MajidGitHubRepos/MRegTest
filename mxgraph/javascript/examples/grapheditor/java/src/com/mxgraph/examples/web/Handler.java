package com.mxgraph.examples.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.List;


import javax.json.JsonArray;
import javax.json.JsonObject;

import java.io.*;
import org.json.*;

public class Handler extends Thread {
	private String name;
	private Socket socket;
	private BufferedReader in;
	private PrintWriter out;
	public  OutputStream outputFileStream;

	private Semaphore sem;
	public String outputFileName;
	public File outputFile;

	/**
	 * Constructs a handler thread, squirreling away the socket.
	 * All the interesting work is done in the run method.
	 */
	public Handler(Socket socket, OutputStream outputFileStream) {
		this.socket = socket;
		this.outputFileStream = outputFileStream;
		//this.inMsgQueue = inMsgQueue;



	}

	public void run() {
		try{
			//sem.acquire();
			BufferedReader socketReader = null;
			socketReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));



			String inMsg = null;
			inMsg = socketReader.readLine();
			//while ((inMsg = socketReader.readLine()) != null) {

			if (inMsg != null) {
				if (ModelJsonServer.inMsgQueue.isEmpty()) {
					System.out.println("==================================[write into the registration.json]============================");
					//  try {
					outputFileStream.write(inMsg.getBytes(), 0, inMsg.length());
					outputFileStream.flush();
					outputFileStream.close();
					System.out.println("\n>>>>> inMsgJSON: "+ inMsg);
					JSONObject inMsgJSON = new JSONObject(inMsg);
					//JSONArray inMsgJSONArr = inMsgJSON.getJSONArray("list");

					Message msg = new Message( -1, null, null,null,"0","0", null, null);
					ModelJsonServer.inMsgQueue.put(msg);
					inMsgJSON = null;
					msg = null;
					//}catch (InterruptedException e) {System.err.println("====[NoSuchElementException]===");}

				}else {
					//System.out.println("inMsgQueue: " + ModelJsonServer.inMsgQueue);
					JSONObject inMsgJSON = new JSONObject(inMsg);
					JSONArray inMsgJSONArrID = inMsgJSON.getJSONArray("traceID");
					JSONArray inMsgJSONArrVar = inMsgJSON.getJSONArray("traceVar");
					JSONArray inMsgJSONTraceSizes = inMsgJSON.getJSONArray("traceSizes");
					JSONArray inMsgJSONActiveStates = inMsgJSON.getJSONArray("activeStates");
					
					
					//JSONObject inMsgJSONnewTraceSize = inMsgJSON.getJSONObject("newTraceSize");
					//JSONObject inMsgJSONoldTraceSize = inMsgJSON.getJSONObject("oldTraceSize");
					Message msg = new Message( Integer.parseInt(inMsgJSONArrID.get(0).toString()), inMsgJSONArrID.get(1).toString(), inMsgJSONArrID.get(2).toString(), inMsgJSONArrID.get(3).toString(), inMsgJSONTraceSizes.get(0).toString(), inMsgJSONTraceSizes.get(1).toString(), null, null);
					
					msg.setGreenState(inMsgJSONActiveStates.get(0).toString());
					
					if (inMsgJSONActiveStates.length()>1)
						msg.setGrayState(inMsgJSONActiveStates.get(1).toString());
					
					// msg: [srcCapName,trgCapName,msgName,data]
					// msg: [Simulator__client__2,Simulator__environment,QueryConfig,]
					// msg: [Simulator__environment,Simulator__client__2,ReplyConfig,]
					// msg: [Simulator__environment,Simulator__server1,ReplyConfig,1:server2]
					
					int count = 1;
					while (true) {
						if (inMsgJSON.has("msg"+count)) {
							JSONArray inMsgJSONmsg = inMsgJSON.getJSONArray("msg"+count);
							msg.listMsgs.add(inMsgJSONmsg.get(0).toString()+","+inMsgJSONmsg.get(1).toString()+","+inMsgJSONmsg.get(2).toString()+","+inMsgJSONmsg.get(3).toString());
							count++;
						}else
							break;
						
					}
					
					System.out.println("\n>>>>> inMsgJSONArrID: "+ inMsgJSONArrID.toString() + ", inMsgJSONTraceSizes: "+inMsgJSONTraceSizes.toString()+", activeStates: "+ inMsgJSONActiveStates.toString()+", msg: "+ msg.listMsgs.toString());


					
					for (int i=0;i<inMsgJSONArrVar.length();i++) {
						String varName = inMsgJSONArrID.get(1).toString().concat("::").concat(inMsgJSONArrVar.get(i++).toString());
						i++;
						String varValue = inMsgJSONArrVar.get(i).toString();
						msg.putToVatriablesHashMap(varName, varValue); 
					}
					//showVatriablesHashMap();
					//System.out.println("\n>>>>> inMsgJSONArrVar: "+ inMsgJSONArrVar.toString());


					ModelJsonServer.inMsgQueue.put(msg);
					inMsgJSON = null;
					inMsgJSONArrID = null;
					msg = null;
				}
				//outputStreamToFile();
			}
			//}

			//FileWriter file;


			//outputStreamToFile();



			socket.close();
			//sem.release();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//==================================================================	
	//==============================================[showVatriablesHashMap]
	//==================================================================
		public void showVatriablesHashMap() {
			System.out.println("=======================[showVatriablesHashMap]==========================");

			Iterator iterator = ModelJsonServer.vatriablesHashMap.entrySet().iterator();
			int counter = 0;
			while(iterator.hasNext())
			{
				counter++;
				Map.Entry mentry = (Map.Entry) iterator.next();  
				System.out.println("["+ counter+"]> (Name): "+ mentry.getKey() + ", (Variable): " + mentry.getValue());
			}

		}



	//==================================================================	
	//==============================================[outputStreamToFile]
	//==================================================================	
	//    	public void outputStreamToFile() {
	//    		try {
	//    			while(!inMsgQueue.isEmpty()) {
	//    				
	//    				
	//    				 		System.out.println("==================================[write into the changes.json]============================");
	//    			    		//ClassLoader classLoader = getClass().getClassLoader();
	//    			    		
	//    			    		//outputFile.createNewFile();
	//    			    		//System.out.println("\n["+ Thread.currentThread().getName() +"]> outputFile exists in : "+ outputFile.getAbsolutePath());
	//    			    		
	//    			    		
	//    			        
	//    				 String msg = inMsgQueue.take();
	//    				 for (int i = 0; i < 10 ; i++) {
	//    					 msg = msg + System.lineSeparator();
	//    				 }
	//    				 outputFileStream.write(msg.getBytes(), 0, msg.length());
	//    				 outputFileStream.flush();
	//    				 //outputFileStream.close();
	//    				//outputFile = null;
	//    				//outputFileStream = null;
	//    			}
	//    		} catch (IOException e) {
	//    			e.printStackTrace();
	//    		}catch (InterruptedException e) {System.err.println("====[NoSuchElementException]===");}
	//    	}
	//        
}