package ca.queensu.cs.antler4AC;


public class SendMessage {
	
	public String srcCapsuleInstance;
	public String trgCapsuleInstance;
	public String port;
	public String msg;
	public String dataName;
	public Value data;
	public Value dest;
	
	public SendMessage (String _port, String _msg, String _dataName, Value _data, Value _dest) {
		this.srcCapsuleInstance = "";
		this.trgCapsuleInstance = "";
		this.msg = _msg;
		this.port = _port;
		this.dataName = _dataName;
		this.data = _data;
		this.dest = _dest;
	}
	
	public SendMessage() {
		this(null, null, null, null, null);
	}
	
	public String allDataToString() {
		if (dest != null && data != null)
			return "[PORT=" + port + ", MSG=" + msg + ", DATAName= "+dataName + ", DATA=" + data.toString()+ ", DEST=" + dest.toString() +"]";
		else if (dest == null && data != null)
			return "[PORT=" + port + ", MSG=" + msg + ", DATAName= "+dataName + ", DATA=" + data.toString() + "]";
		else
			return "[PORT=" + port + ", MSG=" + msg + "]";
	}
	
}
