package ca.queensu.cs.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import ca.queensu.cs.controller.CapsuleTracker;
import ca.queensu.cs.controller.Controller;
import ca.queensu.cs.umlrtParser.EntryData;
import ca.queensu.cs.umlrtParser.PES;
import ca.queensu.cs.umlrtParser.ParserEngine;
import ca.queensu.cs.umlrtParser.TableDataMember;
import ca.queensu.cs.umlrtParser.UmlrtParser;
import ca.queensu.cs.umlrtParser.UmlrtUtils;

public class ViewEngine implements Runnable {
	public static UmlrtParser umlrtParser;
	public static OutputStream outputFileStream;
	
	
	


	public ViewEngine() {
		this.umlrtParser = new UmlrtParser();
		
	}

	//==================================================================	
	//==============================================[Run]
	//==================================================================	
	public final void run() {
		try {
			if (CapsuleTracker.isPortInUse("localhost",8090)) { //8090 used to send command to the local draw.io server
				//makeInitJsonFile();
				makeInitJsonFileFromMapRegionPaths();
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
		makeMxGraphModel();
		//sendJsonToServer();

	}

	//==================================================================	
	//==============================================[makeInitJsonFile]
	//==================================================================	
	public final static void makeInitJsonFile() throws IOException {
		System.out.println("["+ Thread.currentThread().getName() +"]==========================[makeInitJsonFile]==========================");

		//		
		//		JSONArray list1 = new JSONArray(); list1.add("foo"); list1.add(new Integer(100)); list1.add(new Double(1000.21));
		//
		//		JSONArray list2 = new JSONArray(); list2.add(new Boolean(true)); list2.add(null);
		//
		//		JSONObject obj = new JSONObject(); obj.put("name","foo"); obj.put("num",new Integer(100)); obj.put("balance",new Double(1000.21)); obj.put("is_vip",new Boolean(true)); obj.put("nickname",null);
		//
		//		obj.put("list1", list1); obj.put("list2", list2);
		//
		//		System.out.println(obj); 
		//		
		//		Result: {"balance":1000.21,"list2":[true,null],"num":100,"list1":["foo",100,1000.21],"nickname":null,"is_vip":true,"name":"foo"}
		//		
		//		https://code.google.com/archive/p/json-simple/wikis/EncodingExamples.wiki


		/*
		 * 
		 * Achtung: All State and tansition should have be named!
		 * 
		 */



		List<JSONObject> listJSONobj = new ArrayList<JSONObject>();
		JSONObject objTop = new JSONObject();
		JSONArray listCapsulesInstance = new JSONArray();
		JSONArray listCapsulesInstances = new JSONArray();
		objTop.put("name", "Top");
		//listJSONobj.add(topObj);
		System.out.println("---> listTableData: " + viewController.listTableData);
		//ViewEngine.listTableData = umlrtParser.getlistTableData();
		for (Map.Entry<String, List<TableDataMember>> entry  : viewController.listTableData.entrySet()) {
			//System.out.println("---> entry.getKey(): "+entry.getKey());
			
			//JSONObject objCapsules = new JSONObject();
			String capsuleInstance = entry.getKey().replaceAll("\\s+","");
			
			if (capsuleInstance.contains(",")) {
				String [] capsuleInstances = capsuleInstance.split("\\,");

				
				for (int i = 0; i < capsuleInstances.length; i++) {
					JSONObject objCapsulesInstance = new JSONObject();
					objCapsulesInstance.put("name", capsuleInstances[i]);
					JSONArray listSM = new JSONArray();
					for (int j = 0; j < entry.getValue().size(); j++) {
						//every value is a transition

						JSONObject objTR = new JSONObject();
						objTR.put("name",entry.getValue().get(j).getTransition().getTransitonName());
						objTR.put("style","");
						
						JSONArray listTR = new JSONArray();

						JSONObject objStateSrc = new JSONObject();
						objStateSrc.put("name",entry.getValue().get(j).getSource().getStateName());
						objStateSrc.put("style","");
						//listSM.add(objStateSrc);
						listTR.add(objStateSrc);

						JSONObject objStateTrg = new JSONObject();
						objStateTrg.put("name",entry.getValue().get(j).getTarget().getStateName());
						objStateTrg.put("style","");
						//listSM.add(objStateTrg);
						listTR.add(objStateTrg);
						
						objTR.put("SrcTrg",listTR);
						listSM.add(objTR);
					}
					objCapsulesInstance.put("stateMachine", listSM);
					listCapsulesInstances.add(objCapsulesInstance);
				}
				//objTop.put("capsules", listCapsulesInstances);
			}
			
			else {
					JSONObject objCapsulesInstance = new JSONObject();
					objCapsulesInstance.put("name", capsuleInstance);
					JSONArray listSM = new JSONArray();
					
					for (int j = 0; j < entry.getValue().size(); j++) {
						//every value is a transition

						JSONObject objTR = new JSONObject();
						objTR.put("name",entry.getValue().get(j).getTransition().getTransitonName());
						objTR.put("style","");
						
						JSONArray listTR = new JSONArray();

						JSONObject objStateSrc = new JSONObject();
						objStateSrc.put("name",entry.getValue().get(j).getSource().getStateName());
						objStateSrc.put("style","");
						//listSM.add(objStateSrc);
						listTR.add(objStateSrc);

						JSONObject objStateTrg = new JSONObject();
						objStateTrg.put("name",entry.getValue().get(j).getTarget().getStateName());
						objStateTrg.put("style","");
						//listSM.add(objStateTrg);
						listTR.add(objStateTrg);
						
						objTR.put("SrcTrg",listTR);
						listSM.add(objTR);
					}
					objCapsulesInstance.put("stateMachine", listSM);
					listCapsulesInstances.add(objCapsulesInstance);
				//}
					
			}
			objTop.put("capsules", listCapsulesInstances);
			
		}
		System.out.println("=====[SEND INITIAL JSON FILE TO THE WEBSERVER]======");
		/*ClassLoader classLoader = getClass().getClassLoader();
		String outputFileName = "input_output_Files/model.json";
		File outputFile = new File(classLoader.getResource(outputFileName).getFile());
		outputFile.createNewFile();
		System.out.println("\n["+ Thread.currentThread().getName() +"]> outputFile exists in : "+ outputFile.getAbsolutePath());
		
		outputFileStream = new FileOutputStream(outputFile,false);
		//FileWriter file;
		outputStreamToFile(outputFileStream,objTop.toJSONString());
		//-------
*/		
		sendJsonToServer(objTop.toJSONString()); //will be analysied in index.html by initialModelAnalysis()
			//NOTE: ModelJsonServer checks if its Queue is empty then it is a registation message and save it in a registration.json file
		


	}
	
	//==================================================================	
	//==============================================[makeInitJsonFileFromMapRegionPaths]
	//==================================================================	
	public final static void makeInitJsonFileFromMapRegionPaths() throws IOException {
		System.out.println("["+ Thread.currentThread().getName() +"]==========================[makeInitJsonFile From mapRegionPaths]==========================");

		List<JSONObject> listJSONobj = new ArrayList<JSONObject>();
		JSONObject objTop = new JSONObject();
		JSONArray listCapsulesInstances = new JSONArray();
		objTop.put("name", "Top");
		//--
		List<Map<String, String>> regions = new ArrayList<Map<String, String>>();
		String capInstances = "";
		for (Entry<String, List<Map<String, String>>> entryRegion : PES.mapModelRegionPaths.entrySet()) {
			capInstances = entryRegion.getKey();
			regions = entryRegion.getValue();
			System.out.println("====> capInstances: "+ capInstances);

			if (capInstances.contains(",")) {
				String [] capsuleInstancesSplit = capInstances.split("\\,");
				for (int i = 0; i < capsuleInstancesSplit.length; i++) {
					JSONObject objCapsulesInstance = new JSONObject();
					objCapsulesInstance.put("name", capsuleInstancesSplit[i]);

					JSONArray listRegions = new JSONArray();
					for (Map<String, String> regionState : regions) {
						JSONObject objRegion = new JSONObject();
						objRegion.put("name", regionState.keySet().toString().replaceAll("\\[", "").replaceAll("\\]",""));
						String key = capInstances+"::"+regionState.keySet().toString().replaceAll("\\[", "").replaceAll("\\]","");

						List<String> listRegionalPaths = PES.mapRegionPaths.get(key);
						//every value is a path
						JSONArray listPaths = new JSONArray();
						int pathCounter = 0;
						for (int ii = 0; ii<listRegionalPaths.size(); ii++) {

							JSONObject objPath = new JSONObject();
							JSONArray listTransitions = new JSONArray();

							if(listRegionalPaths.get(ii).contains(",")) {
								String [] pathsSplit = listRegionalPaths.get(ii).split("\\,");
								for (String str : pathsSplit) {
									JSONObject objTR = new JSONObject();
									objTR.put("name",PES.mapIdQname.get(str));
									objTR.put("style","");
									listTransitions.add(objTR);
								}
							}else {
								JSONObject objTR = new JSONObject();
								objTR.put("name",PES.mapIdQname.get(listRegionalPaths.get(ii)));
								objTR.put("style","");
								listTransitions.add(objTR);
							}
							objPath.put("Path"+pathCounter++, listTransitions);
							listPaths.add(objPath);
						}
						objRegion.put("listPaths",listPaths);
						listRegions.add(objRegion);
					}
					objCapsulesInstance.put("regions", listRegions);
					listCapsulesInstances.add(objCapsulesInstance);
				}

			}else {

				JSONObject objCapsulesInstance = new JSONObject();
				objCapsulesInstance.put("name", capInstances);

				JSONArray listRegions = new JSONArray();
				for (Map<String, String> regionState : regions) {
					JSONObject objRegion = new JSONObject();
					objRegion.put("name", regionState.keySet().toString().replaceAll("\\[", "").replaceAll("\\]",""));
					String key = capInstances+"::"+regionState.keySet().toString().replaceAll("\\[", "").replaceAll("\\]","");
					List<String> listRegionalPaths = PES.mapRegionPaths.get(key);
					//every value is a path
					JSONArray listPaths = new JSONArray();
					int pathCounter = 0;
					for (int ii = 0; ii<listRegionalPaths.size(); ii++) {
						JSONObject objPath = new JSONObject();
						JSONArray listTransitions = new JSONArray();

						if(listRegionalPaths.get(ii).contains(",")) {
							String [] pathsSplit = listRegionalPaths.get(ii).split("\\,");
							for (String str : pathsSplit) {
								JSONObject objTR = new JSONObject();
								objTR.put("name",PES.mapIdQname.get(str));
								objTR.put("style","");
								listTransitions.add(objTR);
							}
						}else {
							JSONObject objTR = new JSONObject();
							objTR.put("name",PES.mapIdQname.get(listRegionalPaths.get(ii)));
							objTR.put("style","");
							listTransitions.add(objTR);
						}
						objPath.put("Path"+pathCounter++, listTransitions);
					}
					objRegion.put("listPaths",listPaths);
					listRegions.add(objRegion);
				}
				objCapsulesInstance.put("regions", listRegions);
				listCapsulesInstances.add(objCapsulesInstance);
			}
		}
		objTop.put("listCapsulesInstances", listCapsulesInstances);
		System.out.println(objTop.toJSONString());
		System.out.println("=====[SEND INITIAL JSON FILE TO THE WEBSERVER]======");

		sendJsonToServer(objTop.toJSONString()); //will be analysied in index.html by initialModelAnalysis()
		//NOTE: ModelJsonServer checks if its Queue is empty then it is a registation message and save it in a registration.json file

	}

	//==================================================================	
	//==============================================[outputStreamToFile]
	//==================================================================	
	private static void outputStreamToFile(OutputStream os, String data) {
		try {
			os.write(data.getBytes(), 0, data.length());
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//==================================================================	
	//==============================================[makeMxGraphModel]
	//==================================================================	
	public final void makeMxGraphModel() {
		System.out.println("["+ Thread.currentThread().getName() +"]==========================[makeMxGraphModel]==========================");


	}
	

	//==================================================================	
	//==============================================[sendJsonToServer]
	//==================================================================	
	public final static void sendJsonToServer(String jsonString) throws IOException {
		PrintWriter out;
		Socket socket = new Socket("localhost", 8090);
		out = new PrintWriter(socket.getOutputStream(), true);
		out.println(jsonString);
		out.flush();
		out.close();
		socket.close();
	}

}
