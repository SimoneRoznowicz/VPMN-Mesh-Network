package com.examples.akshay.wifip2p;

import android.content.Context;
import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static java.lang.Thread.sleep;

//nel MAIN QUANDO MANDI UN MESSAGGIO , ESEGUI QUESTA CLASSE
public class ServerSocketThreadSender  extends AsyncTask {
    private ServerSocketThread serverSocketThread;
    private String message;
    private Socket socket;
    DataOutputStream dataOutputStream;
    public ServerSocketThreadSender(Context context, MainActivity activity, Socket client, String msg, boolean is_request, String nameOrigin, String nameDestination, String visitedDevices) {
        socket = client;
        System.out.println("msg == " +  msg);
        message = msg;
        System.out.println("message == " +  message);
        if(is_request) {
            System.out.println("requesttt");
            message ="$$" + nameOrigin + "||" + nameDestination + "::" + visitedDevices + "~~" + message;    //example: $$Huawei p10 lite||Simo_Roz::Huawei p10 lite++device1++device2++device3~~Hi how are you?
        }
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            serverSendMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void serverSendMessage() throws IOException {
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        System.out.println("writeUTF");
        dataOutputStream.writeUTF(message + '\n');
        System.out.println("message == " + message);
        System.out.println("flush");
        dataOutputStream.flush();
        System.out.println("dopo flush");
        /*try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }
}
