package ca.queensu.cs.umlrtParser;

import java.util.List;

import org.eclipse.uml2.uml.State;

public class JunctionData<S, E> {

	private final State source;
	private final State target;
	private final String sourceName;
	private final String targetName;
	private final List<String> guard;
	private final List<String> actions;

	
	
	public JunctionData(String sourceName, String targetName, List<String> guard, List<String> actions) {
		this(null, sourceName, null, targetName, guard, actions);
	}
	
	public JunctionData(State source, String sourceName, State target, String targetName, List<String> guard) {
		this(source, sourceName, target, targetName, guard, null);
	}

	
	public JunctionData(State source, String sourceName, State target, String targetName, List<String> guard, List<String> actions) {
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
}