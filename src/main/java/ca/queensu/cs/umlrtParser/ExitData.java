package ca.queensu.cs.umlrtParser;

import org.eclipse.uml2.uml.State;

public class ExitData {
	private final State source;
	private final String sourceName;
	private final State target;
	private final String targetName;
	

	
	public ExitData(State source, String sourceName , State target, String targetName) {
		this.source = source;
			if ((source != null) && (source.getName() != null))
				this.sourceName = source.getName();
			else
				this.sourceName = "NULL";
		this.target = target;
		if ((target != null) && (target.getName() != null))
			this.targetName = target.getName();
		else
			this.targetName = "NULL";
	}
	public ExitData(String sourceName , String targetName) {
		this(null,sourceName, null, targetName);
	}
	
	public String getSourceName() {
		return sourceName;
	}

	
	public String getTargetName() {
		return targetName;
	}

}

