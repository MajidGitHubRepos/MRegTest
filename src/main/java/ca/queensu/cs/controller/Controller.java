package ca.queensu.cs.controller;

/*

Developers:
Majid Babaei (babaei@cs.queensu.ca): Initial development - 120918

 */

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import ca.queensu.cs.compression.Profiling;
import ca.queensu.cs.graph.viewController;
import ca.queensu.cs.server.Event;
import ca.queensu.cs.server.Server;
import ca.queensu.cs.server.Server.RunnableImpl;
import ca.queensu.cs.umlrtParser.PES;
import ca.queensu.cs.umlrtParser.ParserEngine;
import ca.queensu.cs.umlrtParser.TableDataMember;
import ca.queensu.cs.umlrtParser.UmlrtParser;

public class Controller {

	public static UmlrtParser umlrtParser;
	public static viewController viewer;
	public static Server server;
	public static Event event;
	public static Semaphore semServer;
	public static Map<String, List<TableDataMember>> listTableData;
	public static String args0;
	public static HashMap<String, String> mapCapInstIdx, mapIdxCapInst;
	public PES pes;
	public static int WEBSERVER_PORT;
	public static int MAX_TRY_TO_SEND;


	
	

	public Controller(String args0) {
		this.MAX_TRY_TO_SEND = 2;
		this.WEBSERVER_PORT = 8090;
		this.pes = new PES();
		this.args0 = args0;
		this.mapCapInstIdx = new HashMap<String, String>();
		this.mapIdxCapInst = new HashMap<String, String>();
		viewer = new viewController();
		this.semServer = new Semaphore(1); 
		Event event = new Event();
		umlrtParser = new UmlrtParser();
		this.listTableData = null;
		try {
			server = new Server("127.0.0.1",8001, semServer);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//for(int i = 0; i<Controller.trackers.length;i++) {if (Controller.trackers[i].getCapsuleInstance() != null) this.trackerCount++;}
	}


	public static void main(String[] args) throws IOException 
	{
		try {
			if (args.length > 0) {
				if (args[0] != null) {
					args0 = args[0];
				}
			}
			Thread controllerT = new Thread(new Controller(args0).new RunnableImpl()); 
			controllerT.start(); 
			//--------------------------------------------------------------------------
			Thread umlrtParserT = new Thread(umlrtParser.new RunnableImpl()); 
			umlrtParserT.start();
			//--------------------------------------------------------------------------
			Thread serverT = new Thread(server.new RunnableImpl()); 
			serverT.start(); 
			//--------------------------------------------------------------------------

		} catch (NumberFormatException e) {
			System.err.println("Argument" + args[0] + " must be \"view\" to show the diagram.");
			System.exit(1);
		}

	}

	public class RunnableImpl implements Runnable {

		public void run() {
			long t1 = System.currentTimeMillis();
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<[Starting Data Process]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");
			System.out.println("Waiting for the UmlrtParsing thread complete the process ....");
			while (true) {if (umlrtParser.getUmlrtParsingDone()) break; else System.out.print(""); }
			pes.makeMapRegionPaths();
			System.out.println("\n\n<<<<<<<<<<<<<<<[Parsing process has been completed successfully]>>>>>>>>>>>>>>>>>\n\n");
			Profiling.refineParsing();
			long t2 = System.currentTimeMillis();
			 System.out.println("UmlrtParsingTime: "+ (t2-t1));
			
			
			Controller.listTableData = umlrtParser.getlistTableData();
			viewer.setListTableData(umlrtParser.getlistTableData());
			int numberOfRegions =  countRegions();
			
			
			//REGIONS
			System.out.println("\n\n<<<<<<<<<<<<<<<[numberOfCapsules]>>>>>>>>>>>>>>>>>\n\n"+numberOfRegions);

			
			//Message msg = new Message("process it", event);
			TrackerMaker trackerMaker = new TrackerMaker(semServer, numberOfRegions);
			new Thread(trackerMaker,"waiter").start();
			//Notifier notifier = new Notifier(msg, Controller.sem);
			//new Thread(notifier, "notifier").start();


			if (args0 != null)
				if (args0.contentEquals("view")) {
					//Staring view thread to make a mxGraph for the given model
					//--------------------------------------------------------------------------
					Thread viewerT = new Thread(viewer.new RunnableImpl()); 
					viewerT.start();
					//--------------------------------------------------------------------------
				}
		}
	}
	
	//==================================================================	
		//==============================================[countRegions]
		//==================================================================			

		public int countRegions()  
		{
			int countRegions = 0;
			for (Entry<String, List<String>> entry  : pes.mapRegionPaths.entrySet()) {

				if (entry.getKey().contains(",")) {

					String [] spiltCapsuleFullname = entry.getKey().split("\\,");
					countRegions = countRegions + spiltCapsuleFullname.length;
				}else {
					countRegions++;
				}
			}
			return countRegions;
		}

	//==================================================================	
	//==============================================[countCapsule]
	//==================================================================			

	public int countCapsule()  
	{
		int numberOfCapsules = 0;
		for (Map.Entry<String, List<TableDataMember>> entry  : Controller.listTableData.entrySet()) {

			if (entry.getKey().contains(",")) {

				String [] spiltCapsuleFullname = entry.getKey().split("\\,");
				for (String str: spiltCapsuleFullname)
					if (!str.isEmpty()) numberOfCapsules++;
				//numberOfCapsules = numberOfCapsules + spiltCapsuleFullname.length;
			}else {
				numberOfCapsules++;
			}

		}
		return numberOfCapsules;
	}
}