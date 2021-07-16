package ca.queensu.cs.umlrtParser;

import java.util.List;

import org.eclipse.uml2.uml.State;

public class ChoiceData {
	private final State source;
	private final State target;
	private final String sourceName;
	private final String targetName;
	private final List<String> guard;
	private final List<String> actions;

	
	
	public ChoiceData(String sourceName, String targetName, List<String> guard, List<String> actions) {
		this(null, sourceName, null, targetName, guard, actions);
	}
	
	public ChoiceData(State source, String sourceName, State target, String targetName, List<String> guard) {
		this(source, sourceName, target, targetName, guard, null);
	}

	
	public ChoiceData(State source, String sourceName, State target, String targetName, List<String> guard, List<String> actions) {
		this.source = source;
		this.sourceName = sourceName;
		this.target = target;
		this.targetName = targetName;
		this.guard = guard;
		this.actions = actions;
	}

	
	public String getSourceName() {
		return sourceName;
	}

	
	public String getTargetName() {
		return targetName;
	}

	
	public List<String> getGuard() {
		return guard;
	}

	public List<String> getActions() {
		return actions;
	}
	
	public String allDataToString() {
		return "ChoicesData [source=" + source + ", sourceName=" + sourceName +", target=" + target + ", targetName=" + targetName  + ", actions=" + actions + ", guard=" + guard
				+ "]";
	}
}