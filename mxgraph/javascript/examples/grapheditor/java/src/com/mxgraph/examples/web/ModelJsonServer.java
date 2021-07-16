package com.mxgraph.examples.web;

import java.io.*;
import java.util.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class ModelJsonServer implements Runnable {

	public static int PORT;
	public static OutputStream outputFileStream;
	public static BlockingQueue <Message> inMsgQueue;
	public static Stack <Message> mainStack;
	public static Stack <Message> tmpStack;
	public static String outputFileName;
    public static File outputFile;
    public static boolean run;
    public static long counter;
    public static HashMap<String, List<String>> mapTraceSizes;//<counter,<newTraceSize,oldTraceSize>>
    public static HashMap<String, String> vatriablesHashMap;

	public ModelJsonServer(int PORT) {
		this.counter = 0;
		this.run  = true;
		this.PORT = PORT;
		this.inMsgQueue = new PriorityBlockingQueue<Message>(); // input events --> ReplayNextServlet.java
		this.mainStack = new Stack(); // consumed events --> ReplayPreviousServlet.java
		this.tmpStack = new Stack();
		this.vatriablesHashMap = new HashMap<String, String>();
		this.mapTraceSizes = new HashMap<String, List<String>>();
		try {
    		this.outputFileName = "./javascript/examples/grapheditor/www/resources/registration.json";
    		this.outputFile = new File(outputFileName);
    		this.outputFileStream = new FileOutputStream(outputFile,false);
    		}catch (FileNotFoundException e) {System.err.println("====[NoSuchElementException]===");}
		
	}

	//==================================================================	
	//==============================================[Run]
	//==================================================================	
	public final void run() {
		

        try {
    		System.out.println("The model server is running.");
    		ServerSocket listener = new ServerSocket(PORT);

            while (true) {
            	Socket connectionSocket = listener.accept();
            	BufferedReader inFromClient =
        			    new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            	//System.out.println("waiting to accept....");
            	// System.out.println("Received: " + inFromClient.readLine());
                new Handler(connectionSocket,outputFileStream).start();
            }
        } catch (final IOException e) {
			e.printStackTrace();
			//listener.close();
		}
	}

	//==================================================================	
	//==============================================[updateVatriablesHashMap]
	//==================================================================
		public static void updateVatriablesHashMap(HashMap<String, String> msgHashMap) {

			Iterator iterator = msgHashMap.entrySet().iterator();
			int counter = 0;
			while(iterator.hasNext())
			{
				counter++;
				Map.Entry mentry = (Map.Entry) iterator.next(); 
				ModelJsonServer.vatriablesHashMap.put(mentry.getKey().toString(), mentry.getValue().toString());
			}

		}
	
}
