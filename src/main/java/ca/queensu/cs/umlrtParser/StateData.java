package ca.queensu.cs.umlrtParser;

/*

Developers:
Majid Babaei (babaei@cs.queensu.ca): Initial development - 120918

 */

import java.util.HashMap;
import java.util.List;

import org.eclipse.uml2.uml.Event;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Transition;

import ca.queensu.cs.umlrtParser.UmlrtUtils.PseudoStateKind;

public class StateData {
	
	private String capsuleName;
	public String capsuleInstanceName;
	private String parentName;
	private String regionName;
	private String stateName;
	private State state;
	private Pseudostate pseudostate;
	private List<String> deferred;
	private List<String> entryActions;
	private List<String> exitActions;
	private boolean initial = false;
	private boolean end = false;
	private PseudoStateKind pseudoStateKind;
	private String id;
	private String entryAC;
	private String exitAC;
		
	public StateData(String capsuleName, String capsuleInstanceName, State state, Pseudostate pseudostate, String sName, List<String> enList, String enAC, List<String> exList, String exAC, List<String> defList, String pName, String rName, boolean isInitialState, boolean isFinalState) {
		this.entryAC = enAC;
		this.exitAC = exAC;
		this.capsuleName = capsuleName;
		this.capsuleInstanceName = capsuleInstanceName;
		this.state = state;
		this.pseudostate = pseudostate;
		this.stateName = sName;
		this.deferred = defList;
		this.entryActions = enList;
		this.exitActions = exList;
		this.parentName = pName;
		if (state  != null) {
			this.regionName = extractAllRegions(state.getQualifiedName());
		}else
			this.regionName = rName;
		this.initial = isInitialState;
		this.end = isFinalState;
	}
	
	public StateData(String capsuleName,String capsuleInstanceName, String sName, String pName, String rName) {
		this(capsuleName, capsuleInstanceName, null, null ,sName, null,null, null, null, null, pName,rName, false, false);
	}
	public StateData(String capsuleName,String capsuleInstanceName, String sName, String pName, String rName , boolean initial, boolean end) {
		this(capsuleName, capsuleInstanceName, null, null ,sName, null,null, null, null, null, pName,rName, initial, end);
	}
	public StateData() {
		this(null, null, null,null, null, null,null, null, null, null,null,null, false,false);
	}
	
	
	
	//------------[GETERS]
	public String extractID(State state) {
		int idx_1 = state.toString().indexOf("@");
		int idx_2 = state.toString().indexOf(" ");
		String id = state.toString().substring(idx_1+1, idx_2);
		return id;
	}
	public String extractID(Pseudostate state) {
		int idx_1 = state.toString().indexOf("@");
		int idx_2 = state.toString().indexOf(" ");
		String id = state.toString().substring(idx_1+1, idx_2);
		return id;
	}
	
	public String getEntrAC() {
		return entryAC;
	}
	
	public String getExitAC() {
		return exitAC;
	}
	
	public String getCapsuleName() {
		return capsuleName;
	}
	
	public String getCapsuleInstanceName() {
		return this.capsuleInstanceName;
	}
	public String getId() {
		return id;
	}
	public State getState() {
		return state;
	}
	public Pseudostate getPseudostate() {
		return pseudostate;
	}
	public String getStateName() {
		return stateName;
	}
	public List<String> getDeferred() {
		return deferred;
	}
	
	public List<String> getEntryActions() {
		return entryActions;
	}
	
	public List<String> getExitActions() {
		return exitActions;
	}
	
	public String getParent() {
		return parentName;
	}
	
	public String getRegion() {
		return regionName;
	}
	
	public boolean isInitial() {
		return initial;
	}
	
	public boolean isEnd() {
		return end;
	}
	
	public PseudoStateKind getPseudoStateKind() {
		return pseudoStateKind;
	}
	//------------[SETERS]
	
	public void setCapsuleInstanceName(String capsuleInstanceName) {
		this.capsuleInstanceName = capsuleInstanceName;
	}
	
	
	public void setDeferred(List<String> deferred) {
		this.deferred = deferred;
	}
	
	public void setParent(String parent) {
		this.parentName = parent;
	}
	
	public void setRegion(String region) {
		this.regionName = region;
	}
	
	public void setInitial(boolean initial) {
		this.initial = initial;
	}
	
	public void setEnd(boolean end) {
		this.end = end;
	}
	
	public void setPseudoStateKind(PseudoStateKind pseudoStateKind) {
		this.pseudoStateKind = pseudoStateKind;
	}
	
	public void setEntryActions(List<String> entryActions) {
		this.entryActions = entryActions;
	}
	
	public void setExitActions(List<String> exitActions) {
		this.exitActions = exitActions;
	}
	public void setId(State state) {
		this.id = extractID(state);
	}
	public void setId(Pseudostate state) {
		this.id = extractID(state);
	}
	
	public void setPseudostate(Pseudostate pseudostate) {
		this.pseudostate = pseudostate;
	}
	
	public void setState(State state) {
		this.state = state;
	}
	//----------
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
	
	public String allDataToString() {
		return "StateData [ ID= "+ id +" ,CapsuleName="+capsuleName+ ", CapsuleInstanceName= "+capsuleInstanceName+", StateName=" + stateName + ", region=" + regionName + ", state=" + state + ", PseudoState=" + pseudostate + ", deferred=" + deferred
				+ ", entryActions=" + entryActions + ", exitActions=" + exitActions + ", initial=" + initial
				+  ", end=" + end + ", pseudoStateKind=" + pseudoStateKind + "]";
	}
	

	

}
