package ca.queensu.cs.umlrtParser;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.uml2.uml.Port;

public class CapsuleConn {
	
	public List<MyConnector> listMyConnectors;
	private String capsuleName;
	private String capsuleInstanceName;
	//private List<String> listPortName;
	private List<Port> listPorts;
	
	
	public CapsuleConn (String portName_protocolName_connectorName, String capsuleName, String capsuleInstanceName) {
		this.listMyConnectors = new ArrayList<MyConnector>();
		//this.listPortName = new ArrayList<String>();
		this.capsuleInstanceName = capsuleInstanceName;
		this.listPorts = new ArrayList<Port>();
		this.capsuleName = capsuleName;
	}
	
	public CapsuleConn (String capsuleName, String capsuleInstanceName) {
		this(null, capsuleName, capsuleInstanceName);
	}
	
	public CapsuleConn() {
		this(null, null, null);
	}

	public void addToListMyConnector(MyConnector myConnector) {
		this.listMyConnectors.add(myConnector);
	}

	public void setCapsuleName(String capsuleName) {
		this.capsuleName = capsuleName;
	}
	
	public void setCapsuleInstanceName(String capsuleInstanceName) {
		this.capsuleInstanceName = capsuleInstanceName;
	}
	
	public String getCapsuleInstanceName() {
		return this.capsuleInstanceName;
	}
	
	//public void addListPortName(String portName) {
	//	this.listPortName.add(portName);
	//}
	
	public void addListPorts(Port port) {
		this.listPorts.add(port);
	}
	
	public List<MyConnector> getListMyConnector() {
		return this.listMyConnectors;
	}
	
	public List<Port> getListPorts() {
		return this.listPorts;
	}

	/*public void setPortName_connectorName_protocolName(int i, String PortName_connectorName_PortName_protocolName) {
		this.listPortName_connectorName_PortName_protocolName.set(i,PortName_connectorName_PortName_protocolName);
	}*/
	
	
	
	//public List<String> getListPortName() {
	//	return this.listPortName;
	//}
	
	public boolean isListPortNameContainsPort(String port) {
		for(int i = 0; i<this.listPorts.size();i++) {
			if (port.contains(listPorts.get(i).getName()))
				return true;
		}
		return false;
	}
	
	/*public  List<String> getListPortName_connectorName_PortName_protocolName() {
		return this.listPortName_connectorName_PortName_protocolName;
	}*/
	
	/*public String getPortName_connectorName_PortName_protocolName(int i) {
		return this.listPortName_connectorName_PortName_protocolName.get(i);
	}*/
	
	public String getCapsuleName() {
		return this.capsuleName;
	}
	
	public String allDataToString() {
		return "CapsuleConn [capsuleName= "+capsuleName +", capsuleInstanceName= "+capsuleInstanceName+", listMyConnector= " + listMyConnectors+"]";
	}

}
