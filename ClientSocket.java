package com.examples.akshay.wifip2p;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by ash on 16/2/18.
 */

@TargetApi(Build.VERSION_CODES.CUPCAKE)
public class ClientSocket extends AsyncTask{
    private static String data;
    private static final String TAG = "===ClientSocket";

    private MainActivity mActivity;
    //private Handler handler;
    public static boolean isSocketCreated = false;
    OutputStream outputStream;
    InputStream inputStream;
    ClientSocketListener clientSocketListener;

    //when execute(result) is called 4 steps are executed one after the other: https://developer.android.com/reference/android/os/AsyncTask
    public Socket getSocket(){
        return mActivity.socket;
    }

    public ClientSocket(Context context, MainActivity activity, String data1, boolean is_request, String nameDestination, String nameOrigin, String visitedDevices,ClientSocketListener cssl) {
        mActivity = activity;
        String dataa = data1;
        //Handler handler = new Handler(Looper.getMainLooper());
        //AGGIUNTA IP PRIMA DEL TESTO + CARATTERE SPECIALE ("##") + data1
        if(data1 != null) {
            if(!is_request) {
                data = data1 + '\n';    //example:   192.168.1.72##Hi how are you?
                //System.out.println("again1 get_IP() == " + activity.get_IP());
            }
            else{   //is_request == true                                                    //example: $$nameOrigin||nameDestination::visitedDevices~~requestMessage
                data = "$$" + nameOrigin + "||" + nameDestination + "::" + visitedDevices + "~~" + data1 + '\n';    //example: $$Huawei p10 lite||Simo_Roz::Huawei p10 lite++device1++device2++device3~~Hi how are you?
                //System.out.println("again2 get_IP() == " + activity.get_IP());
            }
        }
        else data = "null data";
    }


    @Override
    protected void onPreExecute() {
        if(clientSocketListener != null){
            clientSocketListener.setInterrupted(true);
        }
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        System.out.println("**** SEND IN BACKGROUND");
        sendData();
        return null;
    }



    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    //onPostExecute(Result), invoked on the UI thread after the background computation finishes.
    // The result of the background computation is passed to this step as a parameter.
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        Log.d(ClientSocket.TAG,"SendDataTask Completed");
        System.out.println("ONPOSTEXECUTE");
    }

    public void destroySocket() throws IOException {
        if(outputStream!=null) {
            outputStream.close();
        }
        if(inputStream!=null) {
            inputStream.close();
        }
        if (mActivity.socket != null) {
            if (mActivity.socket.isConnected()) {
                try {
                    System.out.println("destroySocket: socket is closed? " + mActivity.socket.isClosed());
                    mActivity.socket.close();
                    System.out.println("destroySocket: socket is closed? " + mActivity.socket.isClosed());
                } catch (IOException e) {
                    //catch logic
                }
            }
        }
    }

    public void sendData()
    {
        System.out.println("SEND DATA");
        String host = MainActivity.IP;
        //AGGIUNTA
        ///////////////////////
        //System.out.println("get_IP() == " + mActivity.get_IP());
        //System.out.println("IP == " + mActivity.IP);
        System.out.println("IP_address == " + mActivity.IP_address);
        //MainActivity.hostClient = mActivity.get_IP() == mActivity.IP? mActivity.IP_address : mActivity.IP;
        /*if(mActivity.get_IP().equals("192.168.1.72")){
            mActivity.hostClient = "192.168.1.68";
        }
        if(mActivity.get_IP().equals("192.168.1.68")){
            mActivity.hostClient = "192.168.1.72";
        }*/
        //if(mActivity.arrList.size() == 0){
            //mActivity.hostClient = mActivity.IP;
            System.out.println("hostClient == " + mActivity.hostClient);
        //}
        /*else {
            //mActivity.hostClient = mActivity.arrList.get(mActivity.device);
            for (MainActivity.MyEntry myEntry : mActivity.arrList) {
                if(myEntry.getKey().equals(mActivity.device)){
                    System.out.println("--- ENTRATO");
                    mActivity.hostClient = myEntry.getValue();
                    System.out.println("hostClient == " + mActivity.hostClient);
                }
            }
        }*/
        //System.out.println("***** hostClient == " + mActivity.map.get(mActivity.device));

        ////////////////////////
        int port = 8888;
        int len;
        mActivity.socket = new Socket();
        byte buf[]  = new byte[1024];
        System.out.println("data e' ::: === " + data);


        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            System.out.println("SEND DATA PRIMA BIND");
            mActivity.socket.bind(null);
            System.out.println("SEND DATA DENTRO TRY");
            Log.d(ClientSocket.TAG,"Trying to connect...");
            System.out.println("Trying to connect...");
            System.out.println("socket is connected??? " + mActivity.socket.isConnected());


            //int y = 0;
            //while (mActivity.socket == null || (!mActivity.socket.isConnected()) || (y<6)) {
            mActivity.socket.connect((new InetSocketAddress(mActivity.hostClient, port)), 4000);
            //    System.out.println("PROVA NUMERO: " + ++y);
            //}
            System.out.println("2 socket is connected??? " + mActivity.socket.isConnected());

            Log.d(ClientSocket.TAG,"Connected...");
            isSocketCreated = true;

            outputStream = mActivity.socket.getOutputStream();
            inputStream = new ByteArrayInputStream(data.getBytes());

            //take a piece of the content of inputStream and put it into buf
            //then take that piece and write it into outputStream
            System.out.println("buf.length == " + buf.length);
            System.out.println("buf == " + buf);
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            System.out.println("ARRIVATO QUI???");

            System.out.println("CLIENTSOCKET IS NULL?? " + this == null);

            System.out.println("SORPASSATO WHILE");
/*
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mActivity.textViewReceivedData.setText(data);
                    System.out.println("handler.post clientSocket: data == " + data);
                }
            });
*/



        } catch (IOException e) {
            Log.d(ClientSocket.TAG,e.toString());
        }

        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred.
         */

    }

    public class sendDataTask extends AsyncTask{
        private String toSend;
        public sendDataTask(String data) {
            toSend = data;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            System.out.println("SENDDATATASK");
            sendString();
            return null;
        }

        private void sendString(){
            System.out.println("SENDSTRING");
        }
    }
}

