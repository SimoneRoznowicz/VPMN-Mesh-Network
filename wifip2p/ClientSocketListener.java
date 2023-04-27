package com.examples.akshay.wifip2p;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ClientSocketListener extends AsyncTask {
    private static String data;
    private static final String TAG = "===ClientSocket";
    private MainActivity activity;
    private ClientSocket clientSocket;
    private static boolean interrupted = false;
    ClientSocketListener.OnUpdateListener listener;
    BufferedReader bufferedReader;
    InputStream inputstream;

    //when execute(result) is called 4 steps are executed one after the other: https://developer.android.com/reference/android/os/AsyncTask
    public ClientSocketListener(Context context, MainActivity activ, ClientSocket clSocket) throws InterruptedException {
        activity = activ;
        clientSocket = clSocket;
        System.out.println("COSTRUTTORE CLIENTSOCKETLISTENER");
        if(clSocket==null) {
            System.out.println("clSocket E' NULL");
        }
        
        int a=0;
        long timeInitial = System.currentTimeMillis();
        while(!ClientSocket.isSocketCreated){
            if((System.currentTimeMillis() - timeInitial)>4000){
                System.out.println("return too much time");
                return;
            }
        }
        System.out.println("isSocketCreated == TRUE");
        ClientSocket.isSocketCreated = false;
        interrupted = false;
    }

    public void setInterrupted(boolean isTrue){
        interrupted = isTrue;
    }

    public interface OnUpdateListener {
        public void onUpdate(String data);
    }
    public void setUpdateListener(ClientSocketListener.OnUpdateListener listener) {
        this.listener = listener;
    }


    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            listen();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void listen() throws IOException, InterruptedException {
        while(!interrupted) {
            Thread.sleep(500);
            while(!MainActivity.socket.isConnected()){
                //System.out.println("dentro isconnected while");
            }
            try {
                //System.out.println("Try getInputStream()");
                inputstream = MainActivity.socket.getInputStream();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            System.out.println("ClientSocketListener: step 1: numero bytes da leggere inputstream == " + inputstream.available());

            String line = "";
            bufferedReader = new BufferedReader(new InputStreamReader(inputstream));
            if(inputstream.available()>2) {
                System.out.println("readline");
                line = bufferedReader.readLine();
                System.out.println("readline finished");
                System.out.println("line == " + line);
                if(line.length() != inputstream.available()) {
                    //System.out.println("HO PERSO QUALCOSA NEL MEZZO ");
                    //line = bufferedReader.readLine();
                }
                if(line.length()>2) {
                    line = line.substring(2);
                }
                //System.out.println("LISTEN3bis.");
            }
            if (!line.equals("")) {
                listener.onUpdate(line);
            }
        }
        System.out.println("CLIENTSOCKETLISTENER: HO SMESSO DI ASCOLTARE");
        interrupted = false;
    }
}
