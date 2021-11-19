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

/**
 * Created by ash on 16/2/18.
 */

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
                    //bufferedReader.close();
                }
                //Log.d(ServerSocketThread.TAG,"0 client e' closed? " + client.isClosed());
                //Log.d(ServerSocketThread.TAG,"Accepted Connection");
                InputStream inputstream = client.getInputStream();
                //Log.d(ServerSocketThread.TAG,"0.1 client e' closed? " + client.isClosed());
                bufferedReader = new BufferedReader(new InputStreamReader(inputstream));
                //Log.d(ServerSocketThread.TAG,"0.2 client e' closed? " + client.isClosed());
                //StringBuilder sb = new StringBuilder();
                //Log.d(ServerSocketThread.TAG,"0.3 client e' closed? " + client.isClosed());
                //gLog.d(ServerSocketThread.TAG,"string line");
                /*while ((line = bufferedReader.readLine()) != null) {
                    Log.d(ServerSocketThread.TAG,"string line nel while");
                    sb.append(line);
                }*/
                //System.out.println("ServerSocketThread: step 1: numero bytes da leggere inputstream == " + inputstream.available());
                String line = bufferedReader.readLine();
                //Log.d(ServerSocketThread.TAG,"0.4 client e' closed? " + client.isClosed());

                //bufferedReader.close();           QUESTO E' L'ERRORE: CHIUDENDO BUFFERREADER CHIUDO ANCHE IL SOCKET!!!!
                //Log.d(ServerSocketThread.TAG,"0.5 client e' closed? " + client.isClosed());
                //Log.d(ServerSocketThread.TAG,"Completed ReceiveDataTask");
                //receivedData = sb.toString();
                //Log.d(ServerSocketThread.TAG,"1 client e' closed? " + client.isClosed());
                if(line == null) continue;
                String line1 = line;
                if (listener != null) {
                    //Log.d(ServerSocketThread.TAG,"SERVERSOCKETTHREAD: LISTENER ON UPDATE");
                    listener.onUpdate(line);
                    //Log.d(ServerSocketThread.TAG,"2 client e' closed? " + client.isClosed());
                }
                Log.d(ServerSocketThread.TAG," ================ " + line);
                //I'm owner here
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

