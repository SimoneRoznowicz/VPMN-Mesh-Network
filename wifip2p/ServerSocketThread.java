package com.examples.akshay.wifip2p;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ServerSocketThread extends AsyncTask {

    private static final String TAG = "===ServerSocketThread";
    public ServerSocket serverSocket;
    String receivedData = "null";
    private int port = 8888;
    private boolean interrupted = false;
    OnUpdateListener listener;
    Socket client = null;
    BufferedReader bufferedReader;
    private String requestMessage = "";

    public Socket getSocket(){
        return client;
    }
    public ServerSocketThread() {
    }
    public interface OnUpdateListener {
        public void onUpdate(String data);
    }
    public void setUpdateListener(OnUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Object[] objects) {
        try {
            Log.d(ServerSocketThread.TAG," started DoInBackground");
            serverSocket = new ServerSocket(8888);
            while (!interrupted) {
                //RIASSUNTO: SONO SICURO CHE IL PROBLEMA SIA CLIENT. INFATTI IN QUALCHE MODO VIENE CHIUSO E IN TEORIA
                //TUTTO VIENE FATTO ALL'INTERNO DEL WHILE PERCHE' INTERRUPTED NON VIENE MAI CAMBIATO SE NON QUANDO SCHIACCIO SERVER STOP
                client = serverSocket.accept();
                if(bufferedReader != null){
                    Log.d(ServerSocketThread.TAG,"bufferedReader diverso da null");
                }
                InputStream inputstream = client.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputstream));
                String line = bufferedReader.readLine();
                if(line == null) continue;
                String line1 = line;
                if (listener != null) {
                    listener.onUpdate(line);
                }
                Log.d(ServerSocketThread.TAG," ================ " + line);
                if(line1.equals("~~connectionMessage~~")) {    //when I receive a default message I send what I have in the request
                    if (!requestMessage.equals("")) {
                        DataOutputStream dataOutputStream = new DataOutputStream(client.getOutputStream());
                        System.out.println("auto writeUTF");
                        dataOutputStream.writeUTF(requestMessage + '\n');
                        System.out.println("auto message == " + requestMessage);
                        System.out.println("auto flush");
                        dataOutputStream.flush();
                        requestMessage = "";
                    }
                }
            }


            serverSocket.close();
            //System.out.println("serversocketthread: socket client is closed? " + client.isClosed());
            return null;

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(ServerSocketThread.TAG,"IOException occurred");
        }
        return null;
    }
    public void setRequestMessage(String requestMsg) {
        requestMessage = requestMsg;
    }

    public boolean isInterrupted() {
        return interrupted;
    }
    public void setInterrupted(boolean interrupted) {
        this.interrupted = interrupted;
    }
}

