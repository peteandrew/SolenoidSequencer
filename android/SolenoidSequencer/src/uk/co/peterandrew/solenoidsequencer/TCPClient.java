package uk.co.peterandrew.solenoidsequencer;

import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
 
 
public class TCPClient {
 
    private String serverMessage;
    public static final String SERVERIP = "192.168.0.100";
    public static final int SERVERPORT = 5320;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
 
    PrintWriter out;
    BufferedReader in;
 

    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }
 

    public void sendMessage(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    
    public void stopClient() {
        mRun = false;
    }
 
    
    public void run() {
        mRun = true;
 
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
 
            Log.e("TCP Client", "Connecting...");
 
            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVERPORT);
 
            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
 
                Log.e("TCP Client", "Sent."); 
 
                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    serverMessage = in.readLine();
                    
                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;                	
                }
 
            } catch (Exception e) {
 
                Log.e("TCP", "S: Error", e);
 
            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }
 
        } catch (Exception e) {
 
            Log.e("TCP", "C: Error", e);
 
        }
 
    }
    
	
    
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}