package ca.queensu.cs.controller;

public class Message {
	private String capsuleInstance;
	private String port;
	private String msg;
	private int[] logicalVectorTime;
	
	public Message (String port, String msg, String capsuleInstance, int [] logicalVectorTime) {
		this.msg = msg;
		this.port = port;
		this.capsuleInstance = capsuleInstance;
		this.logicalVectorTime = logicalVectorTime;
	}
	public Message() {
		this(null, null, null, null);
	}

	public void setLogicalVectorTime(int [] logicalVectorTime) {
		this.logicalVectorTime = logicalVectorTime;
	}
	
	public void setCapsuleInstance(String capsuleInstance) {
		this.capsuleInstance = capsuleInstance;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setPort(String port) {
		this.port = port;
	}
	
	public String getMsg() {
		return this.msg;
	}

	public String getPort() {
		return this.port;
	}
	public String getCapsuleInstance() {
		return this.capsuleInstance;
	}
	public int []  getLogicalVectorTime() {
		return this.logicalVectorTime;
	}



}
