package ca.queensu.cs.umlrtParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Vertex;
import org.springframework.statemachine.transition.TransitionKind;

public class TransitionData {
	//private  String name;
	public String capsuleName;
	public String capsuleInstanceName;
	private final String transitonName;
	private final Vertex source;
	private final Vertex target;
	private final String sourceName;
	private final String targetName;
	private final List<String> triggers;
	private final List<String> guards;
	private final List<String> actions;
	private  TransitionKind kind;
	private final Long period;
	private final Integer count;
	private boolean isInit;
	private Transition transition;
	private String id;
	private String path;
	private String regionName;
	private boolean fromExitPoint;
	private boolean toEntryPoint;
	private String srcId;
	private String trgId;
	private String actionCode;
	private int callsCount;
	//private List<Vector<String, String>> triggers = new ArrayList<HashMap<String, String>>();
	//private final SecurityRule securityRule;

	//public TransitionData() {}
	public TransitionData(String capsuleName, String capsuleInstanceName, String transitonName, String sourceName, String targetName, List<String> triggers, List<String> actions, String actionCode, List<String> guards, TransitionKind kind, Long period, Integer count,boolean isInit, Transition transition, String regionName ) {
		this(capsuleName,capsuleInstanceName,transitonName,null,sourceName, null, targetName, triggers, actions , actionCode, guards, kind, period, count, isInit, transition, regionName);
	}
	
	public TransitionData(String capsuleName, String capsuleInstanceName, String transitonName, String sourceName, String targetName, List<String> triggers, List<String> actions, String actionCode, List<String> guard, TransitionKind kind, Transition transition, String regionName) {
		this(capsuleName,capsuleInstanceName,transitonName,null,sourceName, null, targetName, triggers, actions, actionCode, guard, kind, null, null, false, transition, regionName);
	}
	
	public TransitionData(String capsuleName, String capsuleInstanceName, String transitonName, String sourceName, String targetName, List<String> actions, String actionCode, List<String> guard, TransitionKind kind, Long period, Integer count,boolean isInit, Transition transition, String regionName ) {
		this(capsuleName,capsuleInstanceName,transitonName, null,sourceName, null, targetName, null, actions, actionCode, guard, kind, period, count, isInit, transition, regionName);
	}
	
	public TransitionData() {
		this(null,null,null,null,null,null,null,null,null,null,null,false, null, null);
	}
	

	
	public TransitionData(String capsuleName, String capsuleInstanceName, String transitonName, Vertex source, 
			String sourceName, Vertex target, String targetName, List<String> triggers, List<String> actions, String actionCode, List<String> guards, 
			TransitionKind kind, Long period, Integer count, boolean isInit, Transition transition, String regionName) {
		this.actionCode = actionCode;
		this.capsuleName = capsuleName;
		this.capsuleInstanceName = capsuleInstanceName;
		this.source = source;
		this.sourceName = sourceName;
		this.target = target;
		this.targetName = targetName;
		this.triggers = triggers;
		this.guards = guards;
		this.actions = actions;
		this.kind = kind;
		this.period = period;
		this.count = count;
		this.isInit = isInit;
		this.transitonName = transitonName;
		this.transition = transition;
		this.fromExitPoint = false;
		this.toEntryPoint = false;
		this.srcId = null;
		this.trgId = null;
		this.callsCount = 0;
		if (transition  != null) {
			this.id = extractID(1,transition.toString());
			this.path = extractID(0, transition.getSource().toString())+"-"+this.id+"-"+extractID(2,transition.getTarget().toString());
		}
		if (transition  != null) {
			this.regionName = extractAllRegions(transition.getQualifiedName());
		}else
			this.regionName = regionName;
		//this.event = event;
	}

	//------------[GETERS]
	//public String getName() {
	//	return name;
	//}
	public String getActionCode() {
		return actionCode;
	}
	public String getTrgId() {
		return trgId;
	}
	public String getSrcId() {
		return srcId;
	}
	public boolean getFromExitPoint() {
		return fromExitPoint;
	}
	public boolean getToEntryPoint() {
		return toEntryPoint;
	}
	public String extractAllRegions(String qName) {
		String allRegions = "";
		String [] qNameSplit = qName.split("\\::");
		for (String str : qNameSplit) {
			if(str.contains("Region")) {
				if (allRegions.isEmpty())
					allRegions = str;
				else
					allRegions = allRegions + "_" +str;
			}
		}
		
		return allRegions;
	}
	public String extractID(int opt, String transitionStr) {
		//opt [0,1,2]: 0=>State_src 1=>Transition 2=>State_trg
		
		int idx_1 = transitionStr.indexOf("@");
		int idx_2 = transitionStr.indexOf(" ");
		String id = transitionStr.substring(idx_1+1, idx_2);
		
		boolean S_trg_ENTRY = false; 
		boolean S_src_EXIT = false; 
		
		if ( (ParserEngine.mapStateData.get(id) != null) && 
				((ParserEngine.mapStateData.get(id).getPseudoStateKind() != null))) {
				
			if(opt == 0) {
				srcId = id;
			}else if(opt == 2) {
				trgId = id;
			}
		}
		
		
						
		//Entry
		if ( (ParserEngine.mapStateData.get(id) != null) && 
				((ParserEngine.mapStateData.get(id).getPseudoStateKind() != null)) &&
				ParserEngine.mapStateData.get(id).getPseudoStateKind().toString().contentEquals("ENTRY")) {
			S_trg_ENTRY = true;}
		//Exit
		if ( (ParserEngine.mapStateData.get(id) != null) &&
				((ParserEngine.mapStateData.get(id).getPseudoStateKind() != null)) &&
				ParserEngine.mapStateData.get(id).getPseudoStateKind().toString().contentEquals("EXIT")) {
			S_src_EXIT = true;}
		
		if (S_trg_ENTRY) {
		// change trg
			this.toEntryPoint = true;
			String nameSpace = ParserEngine.mapStateData.get(id).getPseudostate().getNamespace().getName();
			//System.out.println("=======================[Enter to ] "+ nameSpace);
			// find the entry state
			for (Entry<String, StateData> entry : ParserEngine.mapStateData.entrySet()) {
				StateData stateData = entry.getValue();
				if((stateData.getCapsuleInstanceName().contentEquals(capsuleInstanceName)) && (stateData.getStateName() != null) && stateData.getStateName().contentEquals(nameSpace)) {
					return stateData.getId();
				}
			}
		}
		if (S_src_EXIT) {
		//chage src
			this.fromExitPoint = true;
			String nameSpace = ParserEngine.mapStateData.get(id).getPseudostate().getNamespace().getName();
			//System.out.println("=======================[Enter to ] "+ nameSpace);
			// find the exit state
			for (Entry<String, StateData> entry : ParserEngine.mapStateData.entrySet()) {
				StateData stateData = entry.getValue();
				if((stateData.getStateName() != null) && stateData.getStateName().contentEquals(nameSpace)) {
					return stateData.getId();
				}
			}
		}
		return id;
	}
	
	public String getId() {
		return id;
	}
	
	public int getCallsCount() {
		return callsCount;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getReginName() {
		return regionName;
	}
	
	public Transition getTransition() {
		return transition;
	}
	public String getCapsuleName() {
		return capsuleName;
	}
	
	public void setCapsuleInstanceName(String capsuleInstanceName) {
		this.capsuleInstanceName = capsuleInstanceName;
	}
	
	public String getTransitonName() {
		return transitonName;
	}
	
	public Vertex getSource() {
		return source;
	}

	public Vertex getTarget() {
		return target;
	}
	
	public String getSourceName() {
		return sourceName;
	}

	public String getTargetName() {
		return targetName;
	}
	
	public Integer getCount() {
		return count;
	}

	public List<String> getActions() {
		return actions;
	}

	public List<String> getGuard() {
		return guards;
	}
	
	public List<String> getTriggers() {
		return triggers;
	}
	
	public boolean getIsInit() {
		return isInit;
	}

	//public List<HashMap<String, String>> getTriggers() {
	//	return triggers;
	//}

	public TransitionKind getKind() {
		return kind;
	}
	
	public void setIsInit() {
		this.isInit = true;;
	}
	
	public String getCapsuleInstanceName() {
		return this.capsuleInstanceName;
	}
	//------------[SETTERS]
	//public void setName(String n) {
	//	this.name = n;
	//}
	/*
	public void setSource(String s) {
		this.sourceName = s;
	}

	public void setTargetName(String t) {
		this.targetName = t;
	}

	public void setEvent(String e) {
		this.event = e;
	}

	public void setCount(Integer i) {
		this.count= i;
	}

	public void setActions(String a) {
		this.actions = a;
	}

	public void setGuard(String g) {
		this.guard = g;
	}

	public void setTriggers(List<HashMap<String, String>> tr) {
		this.triggers = tr;
	}

	public void setKind (TransitionKind k) {
		this.kind = k;
	}
	*/
	
	public void setCallsCount(int count) {
		this.callsCount = count;
	}
	
	public String allDataToString() {
		return "TransitionData [ ID= "+ id + ", path= "+path+", CapsuleName= "+capsuleName+ ", CapsuleInstanceName= "+capsuleInstanceName+ ", region= "+regionName +", transitonName= " + transitonName +", source= " + source + ", sourceName=" + sourceName +", target=" + target + ", targetName=" + targetName + ", triggers=" + triggers + ", actions=" + actions + ", guards=" + guards
				+ ", period=" + period + ", count=" + count + ", kind=" + kind + ", isInit= "+ isInit + ", transition= "+ transition+ ", srcID= "+srcId + ", trgID= " + trgId+ ", callsCount= "+ callsCount+"]";
	}

}
