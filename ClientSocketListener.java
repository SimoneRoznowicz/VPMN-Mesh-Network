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
        //Thread.sleep(1000);
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
            //HO CAPITO!!! AL SECONDO MESSAGGIO E QUINDI ALLA SECONDA CHIAMATA DI LISTEN, SEI DENTRO A QUESTO CICLO E QUINDI NON APPENA
            //SETTO INTERRUPTED TRUE, QUANDO RIENTRO NEL COSTRUTTORE IL SOCKET E CREATO MS CHIUSO,,, DOBRESTI CONTROLLARE SE E' CONNECTED IN TEORIA
            //OPPURE PUOI PROVARE A PORRE A NULL IL SOCKET FINCJE NON VIEN RICREATO E CONNESSO
            //ISCONNECTED NON SERVE A NIENTE, RITORNA TRUE ANCHE SE IL SOCKET E' STATO CHIUSO!!!!!
            //  SENZA QUESTO SLEEP MUORE SUBITO AL SECONDO MESSAGGIO!!!!!!!!!!!!!!!!!!!
            Thread.sleep(500);

            //System.out.println("ClientSocketListener: step 1");
            //System.out.println("LISTEN");

            //while(!activity.socket.isConnected()){}

            //System.out.println("ClientSocketListener: step 2");

            //fSystem.out.println("LISTEN");
            /*if(clientSocket==null){
                System.out.println("NULLO CLIENTSOCKET");
            }
            if(clientSocket.getSocket()==null){
                System.out.println("NULLO clientSocket.getSocket()");
            }*/
            //if(activity.socket.getInputStream()==null){
            //    System.out.println("NULLO clientSocket.getSocket().getInputStream()");
            //}
            //System.out.println("0 Socket is closed??" + activity.socket.isClosed());



            //System.out.println("ClientSocketListener: step 1");

            //System.out.println("1 Socket is closed??" + activity.socket.isClosed());
            /*if(bufferedReader != null){
                System.out.println("bufferedReader diverso da null");
                //bufferedReader.close();
            }*/
            /*if(bufferedReader.readLine()==null){
                System.out.println("LISTEN3 XXX");
                continue;
            }*/
            /*System.out.println("2 Socket is closed??" + activity.socket.isClosed());

            System.out.println("LISTEN1");
            StringBuilder sb = new StringBuilder();
            String line = "";
            System.out.println("3 Socket is closed??" + activity.socket.isClosed());


            if(bufferedReader==null){
                System.out.println("LISTEN3");
                continue;
            }
            System.out.println("4 Socket is closed??" + activity.socket.isClosed());
            System.out.println("ClientSocketListener appena prima: step 1: numero bytes da leggere inputstream == " + inputstream.available());
            */

            //System.out.println("prima di while");
            while(!activity.socket.isConnected()){
                //System.out.println("dentro isconnected while");
            }
            try {
                //System.out.println("Try getInputStream()");
                inputstream = activity.socket.getInputStream();
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


            /*
            int j=0;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println("LISTEN WHILE");
                sb.append(line);
            }

            System.out.println("LISTEN2");
            bufferedReader.close();
            String receivedData = sb.toString();
            System.out.println("QUESTO E' IL MESSAGGIO MANDATO DAL SERVER AL CLIENT == " + line);
            */
            if (!line.equals("")) {
                listener.onUpdate(line);
            }
        }
        System.out.println("CLIENTSOCKETLISTENER: HO SMESSO DI ASCOLTARE");
        interrupted = false;
    }
}
