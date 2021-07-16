package ca.queensu.cs.server;

import ca.queensu.cs.controller.CapsuleTracker;

public class BypassOrdering implements Runnable {
	private Event event;
	private String capsuleInstance;
	private String sourceName;

	public BypassOrdering() {
		event = new Event();
		capsuleInstance = "__NOT_FOUND__";
		sourceName      = "__NOT_FOUND__";

	}

	public final void run() {
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<[BYPASS SERVER IS RUNNING]>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n\n");

		while(true) {
			if (!ServerBypassOrdering.eventQueue.isEmpty()) {
				try {
					event = ServerBypassOrdering.eventQueue.take();
					if (event.getCapsuleInstance() != null) {
						capsuleInstance = event.getCapsuleInstance();
					}
					if ((event.getSourceName() != null)) {
						int lastIndex = event.getSourceName().lastIndexOf("::");
						if ((event.getSourceName().length()>lastIndex+2))
							sourceName = event.getSourceName().substring(lastIndex+2);
					}
					try {
						//System.out.println("[Event]: "+capsuleInstance+", " +sourceName + "\n\n");
						//if ((sourceName != "__NOT_FOUND__") && (capsuleInstance != "__NOT_FOUND__"))
						//	CapsuleTracker.jsonToServer(ServerBypassOrdering.priorityEventCounter++,capsuleInstance,sourceName,event.getVariableData());
						capsuleInstance = "__NOT_FOUND__";
						sourceName      = "__NOT_FOUND__";
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.err.println("===[WEB_SERVER CONNECTION FAILD]===");
						System.err.println("=====[PROGRAM TERMINATED]=====");
						System.exit(0);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
