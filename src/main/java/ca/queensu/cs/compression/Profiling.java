package ca.queensu.cs.compression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.uml2.uml.BodyOwner;

import ca.queensu.cs.umlrtParser.ParserEngine;

public class Profiling {
	
	//dataMembers <capsuleName, {StateName1, {<trName1,tr1Calls>,<trName2,tr2Calls>,..., StateName2, ..}, >>
	//public List<HashMap<String,List<HashMap<String,List<HashMap<String,Integer>>>>>> mapCapStsTrsPct = new ArrayList<>(); 	
	//construction
	Profiling(){}
	
	//==================================================================	
	//==============================================[refineParsing]
	//==================================================================
	public static void refineParsing(){
		
		for (int i = 0; i<ParserEngine.listTransitionData.size(); i++) {
			if (ParserEngine.listTransitionData.get(i).getTransitonName() != null) {
				
				String capName = ParserEngine.listTransitionData.get(i).capsuleName;
				String trName = ParserEngine.listTransitionData.get(i).getTransitonName();
				
				int count = getCallsCount(capName,trName);
				//int count = 0;
				ParserEngine.listTransitionData.get(i).setCallsCount(count);
			}
		}
		
		//-------
		System.out.println("=======================[callsCounts added from the output of profiling!]==========================");
		System.out.println("==================================[Refined TransitionData]========================================");
		for (int i = 0; i<ParserEngine.listTransitionData.size(); i++) {
			System.out.println("["+i+"]:" +ParserEngine.listTransitionData.get(i).allDataToString());
		}
	}
	
	
	//==================================================================	
	//==============================================[getCallsCount]
	//==================================================================
	public static int getCallsCount(String capsuleName, String trName){
		//String filePath = getProfilingFilePath();
		String filePath = "/home/majid/workspace-papyrusrt/Replication_2005_CDTProject/src/callgrind.out.12482";
		int callsCount = 0;
		try {
			File file = new File(filePath); 
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.contains("actionchain_____"+trName) && line.contains(capsuleName)) {
					while ((line = br.readLine()) != null) {
						if (line.contains("calls") && (callsCount == 0)) {
							String [] lineSplit1 = line.split("[\\=|\\s+]");
							callsCount = Integer.parseInt(lineSplit1[1]);
							return callsCount;
						}
					}
				}
			}
			return callsCount;
		}catch (Exception e) {
			return (Integer) null;
		}
		
	}
	
	//==================================================================	
	//==============================================[getCallsCount]
	//==================================================================
	/*public  List<Double> getCallsCount(String filePath){
		//https://explainjava.com/split-string-java/
		List<Double> listPCTchains  =  new ArrayList<Double>();
		try {
			File file = new File(filePath); 
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line = "";
			boolean fnDone = false;
			List<HashMap<String,List>> listCapsule = new ArrayList<HashMap<String,List>>();
			while ((line = br.readLine()) != null) {
				if (line.contains("state_____")) {
					List <HashMap> list
					String [] lineSplit1 = line.split("[\\::|\\s+|\\-]"); 
					String capsuleName = lineSplit1[2];
					
					String [] lineSplit2 = line.split("[\\state_____|\\(]"); 
					String stateName = lineSplit2[1];
					String trName = "";
					int totalStateCalls = 0;
					while ((line = br.readLine()) != null){
						lineSplit1 = line.split("\\=");
						
						if (lineSplit1[0].contentEquals("fn")) {break;}
						
						if (line.contains("calls") && (totalStateCalls == 0)) {
							lineSplit1 = line.split("[\\=|\\s+]");
							totalStateCalls = Integer.parseInt(lineSplit1[1]);
							
						}else if ( (line.contains("actionchain_____")) && (totalStateCalls > 0)) {
							lineSplit1 = line.split("[\\::|\\=|\\s+|\\actionchain_____|\\(]"); 
							trName = lineSplit1[3];
							int trCalls = 0;
							if(lineSplit1[0].contentEquals("cfn")) {
								while ((line = br.readLine()) != null){
									if (line.contains("calls") && (trCalls == 0)) {
										lineSplit1 = line.split("[\\=|\\s+]");
										trCalls = Integer.parseInt(lineSplit1[1]);
										HashMap<String,Integer> mapTrCalls = new HashMap<String,Integer>();
										mapTrCalls.put(trName, trCalls);
										break;
									}
								}
								if (trCalls > 0) {
									
								}
							}
						}
						
						
					}
					
				}
			}

			return listPCTchains;


		} catch (Exception e) {
			return null;
		}
	}*/



}
