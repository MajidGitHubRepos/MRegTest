package ca.queensu.cs.server;

/*

Developers:
Majid Babaei (babaei@cs.queensu.ca): Initial development - 120918

*/


import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;

import ca.queensu.cs.server.ByteReader.Command;

public final class ByteWriter implements Runnable {

	private static int commandId = 0;
	private String[] capsuleNames;
	private SocketIO io = null;
	private ByteReader reader;
	//private DataOutputStream out;

	public ByteWriter(SocketIO serverIo, String[] caps) {
		io = serverIo;
		commandId = 0;
		this.capsuleNames = caps;


	}

	public final void run() {
		try {
			requestRun();
			while (!Thread.currentThread().isInterrupted()) {
				if (io.isConnected()){

					requestLastEvent();	


				}
				//Thread.sleep(100);
			}
			io.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public void setReader(ByteReader handler) {
		this.reader = handler;
	}

	public void requestRun() {
		for (int i = 0; i < capsuleNames.length; i++) {
			if (capsuleNames[i] == null) break;
			sendMessage("run -c "+ capsuleNames[i],
					ByteReader.Command.WAITING_FOR_RUN_ACK);
		}
		 
		/*
		sendMessage("run -c Pinger:pinger:0",
				ByteReader.Command.WAITING_FOR_RUN_ACK);
		sendMessage("run -c Ponger:ponger:0",
				ByteReader.Command.WAITING_FOR_RUN_ACK);
		*/
	}

	public void requestLastEvent() {
		
		for (int i = 0; i < capsuleNames.length; i++) {
			if (capsuleNames[i] == null) break;
			sendMessage("v -c "+ capsuleNames[i] +" -n " + String.valueOf(1) + " -e",
					ByteReader.Command.WAITING_FOR_EVENT_LIST);
		}
		/*
		System.out.println("++++++++++++++++++++++[sendCmd requestLastEvent]++++++++++++++++++++++");
		sendMessage("v -c Pinger:pinger:0" +" -n " + String.valueOf(1) + " -e",
				ByteReader.Command.WAITING_FOR_LAST_EVENT);
		*/
	}

	public void requestCapsuleList() {
		sendMessage("list Counter:Counter:0 -b",
				ByteReader.Command.WAITING_FOR_BREAKPOINT_LIST);
	}

	public void requestEventList(String capsuleName, int n) {
		sendMessage("v -c "+ capsuleName +" -n " + String.valueOf(n) + " -e",
				ByteReader.Command.WAITING_FOR_EVENT_LIST);
	}

	public void modifyValue(String capsuleName, String variableName, String value) {
		sendMessage("m -c " + capsuleName + " -n " + variableName + " -v " + value,
				ByteReader.Command.WAITING_FOR_VALUE_CHANGE_ACK);
	}

	public void stepOver(String capsuleName) {
		sendMessage("n -c " + capsuleName,
				ByteReader.Command.STEPPING);
	}


	public void resumeCapsule(String capsuleName) {
		sendMessage("c -c " + capsuleName,
				ByteReader.Command.RESUME);
	}

	//======================================================================
	private void sendMessage(String message, Command waitingForValueChangeAck) {
		System.out.println("[In sendMessage]========================[message]:" + message);
		try {
			
			io.output = new DataOutputStream(io.socket.getOutputStream());
			
			message = message + " -i " + ++commandId; 
			if (!message.startsWith("v")) {
				System.out.println(message);
			}
			String messageSize = String.valueOf(message.length());
			int length = messageSize.length();

			for (int i = 4; i > length; i--) {
				messageSize = "0" + messageSize;
			}

			message = messageSize + message;

			//this.reader.getCommandStack().put(commandId, waitingForValueChangeAck);
			if (commandId >= 1000) {
				commandId = 0;
			}
			
			io.write(message.getBytes());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}