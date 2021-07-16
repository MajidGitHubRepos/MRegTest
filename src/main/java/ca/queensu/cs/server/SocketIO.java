package ca.queensu.cs.server;

/*

Developers:
Majid Babaei (babaei@cs.queensu.ca): Initial development - 120918

*/

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public final class SocketIO {

    public Socket socket;
    public InputStream input;
    public OutputStream output;
    public String info;

    public SocketIO()throws IOException {
        socket = null;
        input = null;
        output = null;
        info = "It is SocketIO";
     
    }

    public void connect(final String ip, final int port) throws IOException {
        close();
        socket = new Socket(ip, port);
        input = new BufferedInputStream(socket.getInputStream());
        output = new BufferedOutputStream(socket.getOutputStream());
    }
    

    public boolean isConnected() {
        if (socket == null){
            return false;
        }
        else if (socket.isConnected()){
            return true;
        }
        else {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            socket = null;
            return false;
        }
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (input != null) {
            input.close();
        }
        if (output != null) {
            output.close();
        }
    }

    public void read(byte[] bytes) throws IOException {
        if (!isConnected()) {
            throw new IOException("Socket not connected");
        }
        input.read(bytes);

    }

    public int read(byte[] bytes, int i, int remaining) throws IOException {
        if (!isConnected()) {
            throw new IOException("Socket not connected");
        }
        return input.read(bytes, i, remaining);
    }

    public void write(byte[] bytes) throws IOException {
        if (!isConnected()) {
            throw new IOException("Socket not connected");
        }
        output.write(bytes);
    }

    public void flush() throws IOException {
        if (!isConnected()) {
            throw new IOException("Socket not connected");
        }
        output.flush();
    }
}