package ca.queensu.cs.umlrtParser;

public class TableDataMember {
	private StateData source;
	private StateData target;
	private TransitionData transition;
	
	public TableDataMember() {
		this(null, null, null);
	}
	
	public TableDataMember(StateData source,StateData target, TransitionData transition) {
		this.source = source;
		this.target = target;
		this.transition = transition;
	}
	
	public StateData getSource() {
		return this.source;
	}
	public StateData getTarget() {
		return this.target;
	}
	public TransitionData getTransition() {
		return this.transition;
	}
	
	public void setSource(StateData source) {
		this.source = source;
	}
	public void setTarget(StateData target) {
		this.target = target;
	}
	public void setTransition(TransitionData transition) {
		this.transition = transition;
	}
	
	public String allDataToString() {
		return "TableDataMember [source=" + source.allDataToString() + ", target=" + target.allDataToString() + ", transiton= "+ transition.allDataToString() + "]";
	}

}
