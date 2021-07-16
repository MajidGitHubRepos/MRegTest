package ca.queensu.cs.server;

/*

Developers:
Majid Babaei (babaei@cs.queensu.ca): Initial development - 120918

 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;

import ca.queensu.cs.controller.CapsuleTracker;
import ca.queensu.cs.graph.ViewEngine;
import ca.queensu.cs.graph.viewController;
import ca.queensu.cs.umlrtParser.UmlrtParser;
import ca.queensu.cs.umlrtParser.UmlrtParser.RunnableImpl;

public class ServerVTOrdering {
	public static viewController viewer;
	private  String ip;
	private  int port;
	public  static SocketIO io;
	private  Thread writerT;
	private  Thread readerT;
	private String[] capsuleNames = {"Pinger:pinger:0","Ponger:ponger:0"};
	private ServerSocket serverSocket;
	private ReaderVTOrdering reader; //queueWriter because it can write into the queue
	public static BlockingQueue<Event> eventQueue;
	static Semaphore sem;
	public static UmlrtParser umlrtParser;
	public static int priorityEventCounter;
	public static String args0;
	
	public ServerVTOrdering(final String ipAddress, final int portNumber, Semaphore sem, String args0) throws IOException {
		this.args0 = args0;
		this.priorityEventCounter = 0;
		viewer = new viewController();
		umlrtParser = new UmlrtParser();
		this.sem = sem;
		ip = ipAddress;
		port = portNumber;
		io = new SocketIO();
		eventQueue = new LinkedBlockingQueue<Event>();
		reader = new ReaderVTOrdering(capsuleNames, sem);
		//writer = new ByteWriter(capsuleNames);
		readerT = new Thread(reader);
		//writerT = new Thread(writer);
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws IOException 
	{ 
		try {
			if (args.length > 0) {
				if (args[0] != null) {
					args0 = args[0];
				}
			}
		
		Thread t1 = new Thread(new ServerVTOrdering("127.0.0.1",8001, ServerVTOrdering.sem,args0).new RunnableImpl()); 
		t1.start();
		
		Thread t2 = new Thread(new VTOrdering()); 
		t2.start();
		
		} catch (NumberFormatException e) {
			System.err.println("Argument" + args[0] + " must be \"view\" to show the diagram.");
			System.exit(1);
		}

	} 

	public class RunnableImpl implements Runnable { 

		public void run() {        
			try {
				
				Thread umlrtParserT = new Thread(umlrtParser.new RunnableImpl()); 
				umlrtParserT.start();
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<[Starting Data Process]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
				System.out.println("Waiting for the UmlrtParsing thread complete the process ....");
				while (true) {if (umlrtParser.getUmlrtParsingDone()) break; else System.out.print(""); }
				System.out.println("\n\n<<<<<<<<<<<<<<<[Parsing process has been completed successfully]>>>>>>>>>>>>>>>>>\n\n");
				viewer.setListTableData(umlrtParser.getlistTableData());
				//Staring view thread to make a mxGraph for the given model
				//--------------------------------------------------------------------------
				if (args0 != null)
					if (args0.contentEquals("view")) {
						//Staring view thread to make a mxGraph for the given model
						//--------------------------------------------------------------------------
						Thread viewerT = new Thread(viewer.new RunnableImpl()); 
						viewerT.start();
						//--------------------------------------------------------------------------
					}

				//--------------------------------------------------------------------------
				
				//sendInitJsonFile();
				System.out.println("Now accepting new client!");
				//writer.setReader(reader);
				io.socket = serverSocket.accept();        	
				System.out.println("socket communication created");
				//writerT.start();
				readerT.start();                        
				while (!Thread.currentThread().isInterrupted()) {
					if (!io.isConnected()) {
						break;
					}
				}
			} catch (final IOException e) {
				e.printStackTrace();        	
			}
			try {
				io.close();
				//writerT.interrupt();
				readerT.interrupt();
				//writerT.join();
				readerT.join();
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	//==================================================================	
	//==============================================[getModelInfo]
	//==================================================================
	public void getModelInfo() {
		try {
			//Enter data using BufferReader 
			BufferedReader reader =  
					new BufferedReader(new InputStreamReader(System.in)); 
			System.out.print("How many capsules do you have? ");
			int capsuleNumber = Integer.valueOf(reader.readLine());
			for (int i = 0; i<capsuleNumber; i++) {
				System.out.print("["+Integer.toString(i+1)+"]: ");
				String capsuleName;			
				capsuleName = reader.readLine();
				capsuleNames[i] = capsuleName;	
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}
	//==================================================================	
		//==============================================[getModelInfo]
		//==================================================================
		public static void sendInitJsonFile() {
	try {
		if (CapsuleTracker.isPortInUse("localhost",8090)) { //8090 used to send command to the local draw.io server
			ViewEngine.makeInitJsonFile();
		}else {
			System.err.println("===[WEB_SERVER CONNECTION FAILD]===");
			System.err.println("=====[PROGRAM TERMINATED]=====");
			System.exit(0);
		}
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
		}

}
