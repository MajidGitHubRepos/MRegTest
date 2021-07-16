package ca.queensu.cs.testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Map.Entry;

import org.json.simple.JSONObject;

import ca.queensu.cs.controller.CapsuleTracker;
import ca.queensu.cs.graph.ViewEngine;
import ca.queensu.cs.umlrtParser.ParserEngine;
import ca.queensu.cs.umlrtParser.StateData;
import ca.queensu.cs.umlrtParser.TransitionData;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.util.StringUtils;

public class RegressionTesting {
	private String selectedModelPath;
	private String selectedJsonPath;
	private int numConsistentOrdering;
	public static Map<String, List<String>> regtestMapRegionPaths = new HashMap<String, List<String>>();
	public static Map<String, List<String>> mapRegionPaths = new HashMap<String, List<String>>();
	public List<String> regTestListRegionalPaths = new ArrayList<String>();
	public static Map<String, String> regTestMapQnameId     = new HashMap<String, String>();
	public static Map<String, String> mapIdQname     = new HashMap<String, String>();
	public static Map<String, List<Map<String, String>>> regTestMapModelRegionPaths = new HashMap<String, List<Map<String, String>>>();

	public RegressionTesting() {
		selectedModelPath = "";
		selectedJsonPath = "";
		numConsistentOrdering = 0;
	}

	public void setSelectedModelPath(String path, int nco) {
		selectedModelPath = path;
		String[] values = path.split("\\.uml");
		selectedJsonPath = values[0]+"Description.json";
		System.out.println("<selectedModelPath>: "+selectedModelPath + "\n");
		System.out.println("<selectedJsonPath>: "+selectedJsonPath + "\n");
		numConsistentOrdering = nco;
		

	}

	public Object readJSONFile() throws IOException, ParseException {
		//JSON parser object to parse read file
		JSONParser jsonParser = new JSONParser();
		FileReader reader = new FileReader(selectedJsonPath);

		//Read JSON file
		Object obj = jsonParser.parse(reader);
		return obj;
	}

	public void descriptionMaker() throws InterruptedException, IOException, ParseException {
		if (selectedModelPath.contains("BankATM")) {
			System.out.println("<<<<<<<<<<[Sending model decription of BankATM.uml to the WebUI]>>>>>>>>>>\n\n");
			ViewEngine.sendJsonToServer("MAKE_LIST_EMPTY");
			Thread.sleep(400);
			ViewEngine.sendJsonToServer(readJSONFile().toString());
		}else if (selectedModelPath.contains("Failover.uml")) {
			System.out.println("<<<<<<<<<<[Sending model decription of Failover.uml to the WebUI]>>>>>>>>>>\n\n");
			ViewEngine.sendJsonToServer("MAKE_LIST_EMPTY");
			Thread.sleep(400);
			ViewEngine.sendJsonToServer(readJSONFile().toString());
		}else if (selectedModelPath.contains("DiningPhilosophers.uml")) {
			System.out.println("<<<<<<<<<<[Sending model decription of DiningPhilosophers.uml to the WebUI]>>>>>>>>>>\n\n");
			ViewEngine.sendJsonToServer("MAKE_LIST_EMPTY");
			Thread.sleep(400);
			ViewEngine.sendJsonToServer(readJSONFile().toString());
		}
	}

	/**
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws ParseException
	 */
	//public void traceMaker() throws InterruptedException, IOException, ParseException {	
	public void pathLocator(String regressionTestingTransitionHashCode) {
		Stack<String> stack = new Stack<String>();
		String [] transitionData = regressionTestingTransitionHashCode.split("\\-");
		List<String> listNxtIds = new ArrayList<String>();
		List<String> listStateMet = new ArrayList<String>();

		String path = "";
		String stateTrgID = transitionData[2];
		String nxtTransitionID = "";
		String regressionTestingTransitionHashCodeNxt = "";
		String lastIdInPath = "";
		String state = transitionData[2];
		//transitionData<StateID_src, transitionID, StateID_trg>
		//Make sure no redundant path is in listPath
		stack.push(transitionData[1]);

		while (!stack.isEmpty()) {

			path = stack.pop();

			lastIdInPath = extractLastPathID(path);

			regressionTestingTransitionHashCodeNxt = ParserEngine.mapTransitionData.get(lastIdInPath).getPath();
			String [] hashCodeNxtSplit = regressionTestingTransitionHashCodeNxt.split("\\-");



			//state = hashCodeNxtSplit[2];

			if (checkStateBasic(hashCodeNxtSplit[2])) {
				if (!listStateMet.contains(hashCodeNxtSplit[2])) {
					listNxtIds = findNxtTransitionIDs(hashCodeNxtSplit, "");
					for (String id : listNxtIds) {
						if (!stack.contains(id)) {
							//stack.pop();
							stack.push(id);
						}
					}
				}
				//if (!regTestListRegionalPaths.contains(path)) {
				//	regTestListRegionalPaths.add(path);
				//	listStateMet.add(hashCodeNxtSplit[2]);
				//}
				//if (listNxtIds.size() == 0) stack.pop();
			}else {
				if (!listStateMet.contains(hashCodeNxtSplit[2])) {
					listNxtIds = findNxtTransitionIDs(hashCodeNxtSplit, path);
					for (String id : listNxtIds) { 
						if (!stack.contains(path+","+id)) {
							//stack.pop(); 
							stack.push(path+","+id); 
						}
					}
				}
				//if (listNxtIds.size() == 0) stack.pop();
			}


		}
	}
	//==================================================================	
		//==============================================[removeDuplicates]
		//==================================================================	
		public void removeDuplicates() {
			Set<String> set = new LinkedHashSet<>(); 

			for (Map.Entry<String, List<String>> entry : regtestMapRegionPaths.entrySet()) {
				set.addAll(entry.getValue());
				entry.getValue().clear();
				entry.getValue().addAll(set);
				set.clear();
			}
		} 

		//==================================================================	
		//==============================================[extractLastPathID]
		//==================================================================	
		public String extractLastPathID(String path) {

			if (path.contains(",")) {
				int lastIndex = path.lastIndexOf(",");
				return path.substring(lastIndex+1);
			}

			return path;
		}

		//==================================================================	
		//==============================================[extractUpperRegion]
		//==================================================================	
		public String extractUpperRegion(String region) {
			int i = region.lastIndexOf("_");
			int j = region.lastIndexOf("::");
			if (region.contains("::") && i>j) {
				int lastIndex = region.lastIndexOf("_");
				return region.substring(0,lastIndex);
			}else {  
				return null;
			}
		}
		//==================================================================	
		//==============================================[regTestExtractLowerRegion]
		//==================================================================	
		public String regTestExtractLowerRegion(String id) {
			String lowerRegion = "";
			String instanceName = "";

			String stateName = ParserEngine.mapStateData.get(id).getPseudostate().getNamespace().getName();
			String capInstance = ParserEngine.mapStateData.get(id).getCapsuleInstanceName();
			String nameSpace = "";

			for (StateData sd :  ParserEngine.listStateData){
				if((sd.getStateName()!=null) && (sd.getStateName().contentEquals(stateName)) && (sd.getCapsuleInstanceName().contentEquals(capInstance))) {
					nameSpace = sd.getState().getQualifiedName()+"::";
					break;
				}
			}

			if ( (nameSpace != null) && (stateName != null)) {
				for (StateData sd :  ParserEngine.listStateData){

					if((sd.getState()!=null) && (sd.getState().getQualifiedName().contains(nameSpace))) {
						instanceName = sd.getCapsuleInstanceName();
						String [] qNameSplit = sd.getState().getQualifiedName().split("\\::");

						for (String str : qNameSplit) {

							if(str.contains("Region")) {
								if (lowerRegion.isEmpty())
									lowerRegion = str;
								else
									lowerRegion = lowerRegion + "_" +str;

							}
						}
						break;
					}
				}
				return instanceName+"::"+lowerRegion; 
			}
			return "";
		}
	//==================================================================	
	//==============================================[regTestToHistory]
	//==================================================================	
	public boolean regTestToHistory(String path, String pathRegion) {
		List <String> listRegionPaths = regtestMapRegionPaths.get(pathRegion);

		for(String regionPath : listRegionPaths) {
			if(regionPath.contains(path) && regionPath.contains(",")) {
				String [] regionPathSplit = regionPath.split("\\,");
				if(ParserEngine.mapTransitionData.get(regionPathSplit[regionPathSplit.length-1]).getIsInit())
					return true;
			}
		}
		return false;
	}

	//==================================================================	
	//==============================================[regTestCountPathInregtestMapRegionPaths]
	//==================================================================	
	public int regTestCountPathInregtestMapRegionPaths(String path, String pathRegion) {
		int count = 0;
		for (Map.Entry<String, List<String>> entry : regtestMapRegionPaths.entrySet()) {
			String region = entry.getKey();
			for(String str : entry.getValue()) {
				if(str.contains(path)) {
					if (str.contains(",")) {
						String [] strSplit = str.split("\\,");
						if (!ParserEngine.mapTransitionData.get(strSplit[strSplit.length-1]).getIsInit() && !regTestToHistory(path,pathRegion)) //path ends at initTr
							count++;
					}else
						count++;
				}
			}
		}
		return count;
	}

	//==================================================================	
	//==============================================[FindTheShortetPath]
	//==================================================================	
	public void FindTheShortetPath() {
		List<String> listLocalPaths = new ArrayList<String>();

		for (Map.Entry<String, List<String>> entry : regtestMapRegionPaths.entrySet()) {
			List<String> listNewPaths = new ArrayList<String>();			
			listLocalPaths = entry.getValue();
			String pathRegion = entry.getKey();
			for(String p : listLocalPaths) {

				if ((regTestCountPathInregtestMapRegionPaths(p,pathRegion)<=1) || 
						(ParserEngine.mapTransitionData.get(p)!=null && ParserEngine.mapTransitionData.get(p).getIsInit()))
					listNewPaths.add(p);
			}
			regtestMapRegionPaths.put(entry.getKey(), listNewPaths);
		}
	} 

	
	//==================================================================	
	//==============================================[showElements]
	//==================================================================	
	public void showElements() {
		System.out.println("=======================[regtestMapRegionPaths]==========================");
		String pathCurr = "";
		for (Map.Entry<String, List<String>> entry : regtestMapRegionPaths.entrySet()) {
			System.out.println("[KEY]= "+entry.getKey());
			regTestListRegionalPaths = entry.getValue();
			for (int i = 0; i<regTestListRegionalPaths.size(); i++) {

				if(regTestListRegionalPaths.get(i).contains(",")) {
					String [] pathsSplit = regTestListRegionalPaths.get(i).split("\\,");
					pathCurr = "";
					for (String str : pathsSplit) {
						if (pathCurr.isEmpty()) {
							pathCurr = mapIdQname.get(str);
							//pathCurr = str;
						}else {	
							pathCurr = pathCurr+ "-->" + mapIdQname.get(str);
							//pathCurr = pathCurr+ "-->" + str;
						}
					}
				}else {
					pathCurr = mapIdQname.get(regTestListRegionalPaths.get(i));
					//pathCurr = regTestListRegionalPaths.get(i);
				}
				System.out.println("["+i+"]:" +pathCurr);
			}
		}
		
		for (Entry<String, List<Map<String, String>>> entry : regTestMapModelRegionPaths.entrySet()) {
			System.out.println("[KEY]= "+entry.getKey());
			List<Map<String, String>>paths = entry.getValue();
			for (int i = 0; i<paths.size(); i++) {
				System.out.println("["+i+"]: RegionName: " +paths.get(i).keySet().toString().replaceAll("\\[", "").replaceAll("\\]","") + ", StateName: " + paths.get(i).values());
			}
		}
		/*System.out.println("=======================[regTestMapQnameId]==========================");

			for (Map.Entry<String, String> entry : regTestMapQnameId.entrySet()) {
				System.out.println("[KEY]= "+entry.getKey() + " [VALUE]= "+entry.getValue());
			}
			System.out.println("=======================[MapIdQName]==========================");

			for (Map.Entry<String, String> entry : mapIdQname.entrySet()) {
				System.out.println("[KEY]= "+entry.getKey() + " [VALUE]= "+entry.getValue());
			}
		 */
	}
	//==================================================================	
	//==============================================[findNxtTransitionID]
	//==================================================================	
	List<String> findNxtTransitionIDs(String [] transitionData, String path) {
		List<String> listNxtIds = new ArrayList<String>();
		for (TransitionData tr :  ParserEngine.listTransitionData){
			String [] pathSplit = tr.getPath().split("\\-");
			if (pathSplit[0].contentEquals(transitionData[2]) && 
					(!path.contains(pathSplit[1])) &&	//No redundant path
					(tr.getCapsuleInstanceName().contentEquals(ParserEngine.mapTransitionData.get(transitionData[1]).getCapsuleInstanceName())) && //The same capsule
					(tr.getReginName().contentEquals(ParserEngine.mapTransitionData.get(transitionData[1]).getReginName()))) { //The same region 
				listNxtIds.add(pathSplit[1]);
			}
		}
		return listNxtIds;
	}

	//==================================================================	
	//==============================================[checkStateBasic]
	//==================================================================	
	boolean checkStateBasic(String stateID) {

		StateData stateData = ParserEngine.mapStateData.get(stateID);
		//System.out.println("=======================> [stateID]"+ stateID);
		//System.out.println("=======================> [stateData]"+ stateData.allDataToString());

		if ((stateData.getPseudoStateKind() != null)) { //TODO: Exit is not a basic state
			if (stateData.getPseudoStateKind().toString().contentEquals("EXIT") ||
					stateData.getPseudoStateKind().toString().contentEquals("ENTRY"))
				return true;
			else
				return false;
		}else 	
			return true;
	}

	//==================================================================	
	//==============================================[findRegionStateName]
	//==================================================================
	public String findRegionStateName(String capInstances, String regionName) {
		// we assume that a region at least has a state !
		for (int i = 0; i<ParserEngine.listStateData.size(); i++) {
			String qualifiedName = "";
			if ((ParserEngine.listStateData.get(i).getRegion().contentEquals(regionName)) && (ParserEngine.listStateData.get(i).getState() != null) && (ParserEngine.listStateData.get(i).getState().getQualifiedName() != null)) {
				qualifiedName = ParserEngine.listStateData.get(i).getState().getQualifiedName();
				int lastIdx = qualifiedName.lastIndexOf("::");
				String str = qualifiedName.substring(0,lastIdx);
				lastIdx = str.lastIndexOf("::");
				str = str.substring(0,lastIdx);
				lastIdx = str.lastIndexOf("::");
				return str.substring(lastIdx+2);
			}		
		}

		System.exit(1);
		return null;
	}

	//==================================================================	
	//==============================================[makeregTestMapModelRegionPaths]
	//==================================================================
	public void makeregTestMapModelRegionPaths() {
		Set<Map<String, String>> set = new LinkedHashSet<>(); 
		for (Map.Entry<String, List<String>> entry : regtestMapRegionPaths.entrySet()) {
			String [] keySplit = entry.getKey().split("\\::");
			String capInstances = keySplit[0];
			String regionName = keySplit[1];
			String StateName = "";

			if (!regionName.contains("_"))
				StateName = "__MainRegion__";
			else
				StateName = findRegionStateName(capInstances,regionName);

			List<Map<String, String>> currentRegionList = new ArrayList<Map<String, String>>();

			currentRegionList = regTestMapModelRegionPaths.get(capInstances);
			if((currentRegionList != null) && (currentRegionList.size()>0)) {
				Map<String, String> mapRegionState = new HashMap<String, String>();
				mapRegionState.put(regionName, StateName);
				currentRegionList.add(mapRegionState);
				set.addAll(currentRegionList);
				currentRegionList.clear();
				currentRegionList.addAll(set);
				set.clear();
				regTestMapModelRegionPaths.put(capInstances, currentRegionList);
			}else {
				List<Map<String, String>> newRegionList = new ArrayList<Map<String, String>>();
				Map<String, String> mapRegionState = new HashMap<String, String>();
				mapRegionState.put(regionName, StateName);
				newRegionList.add(mapRegionState);
				regTestMapModelRegionPaths.put(capInstances, newRegionList);
			}
		}

		//Sorting region in each capsule based on "_" //upper Region must be processed first!

		for (Entry<String, List<Map<String, String>>> entry : regTestMapModelRegionPaths.entrySet()) {
			int count_ = 0;
			Map<String, String> sameLeveRegions = new HashMap<String, String>();
			List<Map<String, String>> currentRegionList = new ArrayList<Map<String, String>>();
			do {
				sameLeveRegions = new HashMap<String, String>();
				for(Map<String, String> regionState : entry.getValue()) {
					if (StringUtils.countOccurrencesOf(regionState.keySet().toString().replaceAll("\\[", "").replaceAll("\\]",""), "_")==count_) {
						if (sameLeveRegions.isEmpty())
							sameLeveRegions= regionState;
						else
							sameLeveRegions.putAll(regionState); 
					}
				}
				if (!sameLeveRegions.isEmpty())
					currentRegionList.add(sameLeveRegions);
				count_++;
			}while(!sameLeveRegions.isEmpty());
			if (!currentRegionList.isEmpty()) {				
				regTestMapModelRegionPaths.put(entry.getKey(), currentRegionList);
			}
		}
	}



	//}

}
