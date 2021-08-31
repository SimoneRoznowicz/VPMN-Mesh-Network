package com.examples.akshay.wifip2p;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import android.support.v7.app.AppCompatActivity;

import static java.lang.Thread.sleep;


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class MainActivity extends AppCompatActivity implements View.OnClickListener,WifiP2pManager.ConnectionInfoListener {
    public static final String TAG = "===MainActivity";
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    WifiP2pDevice device;

    Button buttonDiscoveryStart;
    Button buttonDiscoveryStop;
    Button buttonConnect;
    Button buttonServerStart;
    Button buttonClientStart;
    Button buttonClientStop;
    Button buttonServerStop;
    Button buttonConfigure;
    EditText editTextTextInput;

    ServiceDiscovery serviceDisvcoery;

    ListView listViewDevices;
    TextView textViewDiscoveryStatus;
    TextView textViewWifiP2PStatus;
    TextView textViewConnectionStatus;
    TextView textViewReceivedData;
    TextView textViewReceivedDataStatus;

    ///////////////// AGGIUNTE
    TextView textViewNameDestination;
    TextView textViewNameDestination1;
    TextView textViewIDDestination;
    EditText EditIDDestination;
    TextView textViewRequest;
    TextView textViewRequestDestination;
    /////////////////

    public static String IP = null;
    ///////////////////
    public static String IP_server = null;
    public static String IP_client = null;
    public static String IP_MIO = null;
    public static String IP_MAMMA = null;
    public static String hostClient = null;
    public static String IP_address = null;
    Map<WifiP2pDevice,String> map = new HashMap<>();     //map containing as IP address as Value (String), the device as Key (WifiP2pDevice)
    ArrayList<MyEntry> arrList = new ArrayList<MyEntry>();
    String seenDevices = "";
    public boolean isRequest = false;
    public boolean isFirst = true;
    ///////////////////
    public static boolean IS_OWNER = false;

    static boolean  stateDiscovery = false;
    static boolean stateWifi = false;
    public static boolean stateConnection = false;
    ClientSocketListener clientSocketListener = null;
    ClientSocket clientSocket = null;
    public static Socket socket;
    public static boolean ignoreChange = false;

    ServerSocketThread serverSocketThread = null;
    static final int MESSAGE_READ=1;   //FORSE NON SERVE


    ArrayAdapter mAdapter;
    WifiP2pDevice[] deviceListItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceDisvcoery = new ServiceDiscovery();
        setUpUI();
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiBroadcastReceiver(mManager, mChannel, this);

        //serverSocketThread = new ServerSocketThread();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpIntentFilter();
        registerReceiver(mReceiver, mIntentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void setUpIntentFilter() {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void setUpUI() {
        buttonDiscoveryStart = findViewById(R.id.main_activity_button_discover_start);
        buttonDiscoveryStop = findViewById(R.id.main_activity_button_discover_stop);
        buttonConnect = findViewById(R.id.main_activity_button_connect);
        buttonServerStart = findViewById(R.id.main_activity_button_server_start);
        buttonServerStop = findViewById(R.id.main_activity_button_server_stop);
        buttonClientStart = findViewById(R.id.main_activity_button_client_start);
        buttonClientStop = findViewById(R.id.main_activity_button_client_stop);
        buttonConfigure = findViewById(R.id.main_activity_button_configure);
        listViewDevices = findViewById(R.id.main_activity_list_view_devices);
        textViewConnectionStatus = findViewById(R.id.main_activiy_textView_connection_status);
        textViewDiscoveryStatus = findViewById(R.id.main_activiy_textView_dicovery_status);
        textViewWifiP2PStatus = findViewById(R.id.main_activiy_textView_wifi_p2p_status);
        textViewReceivedData = findViewById(R.id.main_acitivity_data);
        textViewReceivedDataStatus = findViewById(R.id.main_acitivity_received_data);

        editTextTextInput = findViewById(R.id.main_acitivity_input_text);

        //////////////////// AGGIUNTE
        //textViewNameDestination = findViewById(R.id.main_activity_textView_name_destination);
        //textViewNameDestination1 = findViewById(R.id.main_activity_textView_name_destination1);
        textViewIDDestination = findViewById(R.id.main_activity_textView_ID_destination);
        EditIDDestination = findViewById(R.id.main_activity_edit_ID_destination);
        textViewRequest = findViewById(R.id.main_activity_textView_request);
        //textViewRequestDestination = findViewById(R.id.main_activity_textView_request_destination);
        ////////////////////

        buttonServerStart.setOnClickListener(this);
        buttonServerStop.setOnClickListener(this);
        buttonClientStart.setOnClickListener(this);
        buttonClientStop.setOnClickListener(this);
        buttonConnect.setOnClickListener(this);
        buttonDiscoveryStop.setOnClickListener(this);
        buttonDiscoveryStart.setOnClickListener(this);
        buttonConfigure.setOnClickListener(this);

        buttonClientStop.setVisibility(View.INVISIBLE);
        buttonClientStart.setVisibility(View.INVISIBLE);
        buttonServerStop.setVisibility(View.INVISIBLE);
        buttonServerStart.setVisibility(View.INVISIBLE);
        editTextTextInput.setVisibility(View.INVISIBLE);
        textViewReceivedDataStatus.setVisibility(View.INVISIBLE);
        textViewReceivedData.setVisibility(View.INVISIBLE);
        buttonConfigure.setVisibility(View.VISIBLE);



        ///////////***********
        buttonClientStop.setVisibility(View.VISIBLE);
        buttonClientStart.setVisibility(View.VISIBLE);
        editTextTextInput.setVisibility(View.VISIBLE);
        buttonServerStop.setVisibility(View.VISIBLE);
        buttonServerStart.setVisibility(View.VISIBLE);
        textViewReceivedData.setVisibility(View.VISIBLE);
        textViewReceivedDataStatus.setVisibility(View.VISIBLE);
        ///////////***********



        listViewDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            device = deviceListItems[i];
            Toast.makeText(MainActivity.this,"Selected device :"+ device.deviceName ,Toast.LENGTH_SHORT).show();
            }
        });
    }
/*
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch(msg.what){
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    textViewReceivedData.setText(tempMsg);
                    break;
            }
            return true;
        }
    });
*/
    private void discoverPeers()
    {
        Log.d(MainActivity.TAG,"discoverPeers()");
        setDeviceList(new ArrayList<WifiP2pDevice>());
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                stateDiscovery = true;
                Log.d(MainActivity.TAG,"peer discovery started");
                makeToast("peer discovery started");
                MyPeerListener myPeerListener = new MyPeerListener(MainActivity.this);
                //mManager.requestPeers(mChannel,myPeerListener);
            }

            @Override
            public void onFailure(int i) {
                stateDiscovery = false;
                if (i == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(MainActivity.TAG," peer discovery failed :" + "P2P_UNSUPPORTED");
                    makeToast(" peer discovery failed :" + "P2P_UNSUPPORTED");

                } else if (i == WifiP2pManager.ERROR) {
                    Log.d(MainActivity.TAG," peer discovery failed :" + "ERROR");
                    makeToast(" peer discovery failed :" + "ERROR");

                } else if (i == WifiP2pManager.BUSY) {
                    Log.d(MainActivity.TAG," peer discovery failed :" + "BUSY");
                    makeToast(" peer discovery failed :" + "BUSY");
                }
            }
        });
//        requestConnection();
        System.out.println("hostclient is == " + hostClient);
    }

    private void stopPeerDiscover() {
        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                stateDiscovery = false;
                Log.d(MainActivity.TAG,"Peer Discovery stopped");
                makeToast("Peer Discovery stopped" );

            }

            @Override
            public void onFailure(int i) {
                Log.d(MainActivity.TAG,"Stopping Peer Discovery failed");
                makeToast("Stopping Peer Discovery failed" );
            }
        });
    }

    public void makeToast(String msg) {
        Toast.makeText(MainActivity.this,msg,Toast.LENGTH_SHORT).show();
    }

    public void disconnect(){
        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                System.out.println("SUCCESSFULLY DISCONNECTED");
                Toast.makeText(getApplication(),"Disconnected from " + device.deviceName,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                System.out.println("UNSUCCESSFULLY DISCONNECTED");
                if(reason == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(MainActivity.TAG, "P2P_UNSUPPORTED");
                    makeToast("Failed establishing discconnection: " + "P2P_UNSUPPORTED");
                }
                else if( reason == WifiP2pManager.ERROR) {
                    Log.d(MainActivity.TAG, "disconnection falied : ERROR");
                    makeToast("Failed establishing disconnection: " + "ERROR");

                }
                else if( reason == WifiP2pManager.BUSY) {
                    Log.d(MainActivity.TAG, "Disconneciton falied : BUSY");
                    makeToast("Failed establishing disconnection: " + "BUSY");
                }

            }
        });
    }
    public void groupDisconnect(){
        mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener(){
            @Override
            public void onSuccess() {
                System.out.println("SUCCESSFULLY DISCONNECTED group");
                Toast.makeText(getApplication(),"Disconnected group from " + device.deviceName,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reason) {
                System.out.println("UNSUCCESSFULLY DISCONNECTED group");
                if(reason == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(MainActivity.TAG, "P2P_UNSUPPORTED");
                    makeToast("Failed establishing discconnection: " + "P2P_UNSUPPORTED");
                }
                else if( reason == WifiP2pManager.ERROR) {
                    Log.d(MainActivity.TAG, "disconnection falied : ERROR");
                    makeToast("Failed establishing disconnection: " + "ERROR");

                }
                else if( reason == WifiP2pManager.BUSY) {
                    Log.d(MainActivity.TAG, "Disconneciton falied : BUSY");
                    makeToast("Failed establishing disconnection: " + "BUSY");
                }
            }
        });
    }

    public void connect(final WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        Log.d(MainActivity.TAG,"Trying to connect : " +device.deviceName);
        System.out.println("Trying to connect...");
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(MainActivity.TAG, "Connected to :" + device.deviceName);
                Toast.makeText(getApplication(),"Connection successful with::: " + device.deviceName,Toast.LENGTH_SHORT).show();
                System.out.println("SERVERSOCKETTHREAD E' NULL??? " + serverSocketThread == null);
                //buttonConfigure.performClick();
            }

            @Override
            public void onFailure(int reason) {
                if(reason == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(MainActivity.TAG, "P2P_UNSUPPORTED");
                    makeToast("Failed establishing connection: " + "P2P_UNSUPPORTED");
                }
                else if( reason == WifiP2pManager.ERROR) {
                    Log.d(MainActivity.TAG, "Conneciton falied : ERROR");
                    makeToast("Failed establishing connection: " + "ERROR");

                }
                else if( reason == WifiP2pManager.BUSY) {
                    Log.d(MainActivity.TAG, "Conneciton falied : BUSY");
                    makeToast("Failed establishing connection: " + "BUSY");
                }
            }
        });
        requestConnection();
    }

    public void requestConnection(){
        if (mManager == null) return;
        mManager.requestConnectionInfo(mChannel,this);
    }

    public void setDeviceList(ArrayList<WifiP2pDevice> deviceDetails) {
        // the following array contains the ID of each available device
        deviceListItems = new WifiP2pDevice[deviceDetails.size()];
        // the following array contains the name (name which appears on screen, it is not an ID) of each available device
        String[] deviceNames = new String[deviceDetails.size()];
        for(int i=0 ;i< deviceDetails.size(); i++){
            deviceNames[i] = deviceDetails.get(i).deviceName;
            Log.d(MainActivity.TAG, "*** Name device " + i + ": " + deviceNames[i]);
            System.out.println("*** Name device " + i + ": " + deviceNames[i]);

            deviceListItems[i] = deviceDetails.get(i);
            Log.d(MainActivity.TAG, "*** ID device " + i + ": " + deviceListItems[i]);
            System.out.println("*** ID device " + i + ": " + deviceListItems[i]);

        }
        mAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1,android.R.id.text1,deviceNames);
        listViewDevices.setAdapter(mAdapter);
    }

    public void setStatusView(int status) throws InterruptedException {

        switch (status)
        {
            case Constants.DISCOVERY_INITATITED:
                stateDiscovery = true;
                textViewDiscoveryStatus.setText("DISCOVERY_INITIATED");
                break;
            case Constants.DISCOVERY_STOPPED:
                stateDiscovery = false;
                textViewDiscoveryStatus.setText("DISCOVERY_STOPPED");
                break;
            case Constants.P2P_WIFI_DISABLED:
                stateWifi = false;
                textViewWifiP2PStatus.setText("P2P_WIFI_DISABLED");
                buttonDiscoveryStart.setEnabled(false);
                buttonDiscoveryStop.setEnabled(false);
                break;
            case Constants.P2P_WIFI_ENABLED:
                stateWifi = true;
                textViewWifiP2PStatus.setText("P2P_WIFI_ENABLED");
                buttonDiscoveryStart.setEnabled(true);
                buttonDiscoveryStop.setEnabled(true);
                break;
            case Constants.NETWORK_CONNECT:
                stateConnection = true;
                makeToast("It's a connect");

                //while(hostClient == null) {
                    //mManager.requestConnectionInfo(mChannel, this);
                //}
                //mManager.requestConnectionInfo(mChannel,this);
                /*
                System.out.println("hostclient is == " + hostClient);
                if(IS_OWNER) {//attivo server listener
                    serverSocketThread = new ServerSocketThread();
                    serverSocketThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    System.out.println("serversocketthread");
                    serverSocketThread.setUpdateListener(new ServerSocketThread.OnUpdateListener() {
                        public void onUpdate(String obj) {
                            setReceivedText(obj);
                        }
                    });
                }

                if(!IS_OWNER){  //as soon as the two devices connect, the client sends a default message to the server and the clientSocketListener starts listening
                    isRequest = false;
                    closeSocketAndInterrupt();
                    //sleep(1000);
                    String connectionMessage = "~~connectionMessage~~";
                    //sleep(1000);
                    //mManager.requestConnectionInfo(mChannel, this);
                    while(hostClient == null){
                        sleep(400);
                        mManager.requestConnectionInfo(mChannel,this);
                    }
                    ClientSocket clientSocket = new ClientSocket(MainActivity.this, this, connectionMessage, isRequest, null, null, null, clientSocketListener);
                    clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    startClientSocketListener();
                }
                */
                textViewConnectionStatus.setText("Connected");
                break;
            case Constants.NETWORK_DISCONNECT:
                stateConnection = false;
                textViewConnectionStatus.setText("Disconnected");
                makeToast("State is disconnected");
                break;
            default:
                Log.d(MainActivity.TAG,"Unknown status");
                break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.main_activity_button_discover_start:
                if(!stateDiscovery) {
                    discoverPeers();
                }
                break;
            case R.id.main_activity_button_discover_stop:
                if(stateDiscovery){
                    stopPeerDiscover();
                }
                break;
            case R.id.main_activity_button_connect:

                ///////////////// nell'inoltro cambia la destination
                //AGGIUNTA: quando faccio click su connect, non guardo solo se c'è un dispositivo
                // connesso ma anche se non è stato scritto niente nella casella request
                String requestDestination = EditIDDestination.getText().toString();
                if(device == null && requestDestination.equals("")) {        //Didn't select anything, didn't write anything
                    Toast.makeText(MainActivity.this,"Please discover and select a device OR write the destination below",Toast.LENGTH_SHORT).show();
                    return;
                }
                /*
                else if(device == null && !requestDestination.equals("")) {  //Didn't select a device, wrote destination
                    //ottenere la lista di dispositivi e connettersi a tutti
                    boolean found = false;
                    for(int i = 0; i < deviceListItems.length; i++) {
                        if(deviceListItems[i].deviceName.equals(requestDestination)){      //what was written in the request was, in reality, the name of a device present in the list
                            device = deviceListItems[i];
                            System.out.println("******* 1");
                            connect(device);
                            isRequest = false;
                            break;
                        }
                        if(!found) {
                            Toast.makeText(MainActivity.this,"Request is necessary, click on SEND button",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                else {    */                                                   //Selected a device, didn't write anything
                System.out.println("******* 2");
                connect(device);

                System.out.println("serversocketthread e' null?? " + serverSocketThread == null);
                /** POSSO METTERE NEW SERVERSOCEKETTHREAD IN ONCREATE**/
                if(serverSocketThread == null) {
                    System.out.println("entro1");
                    if (IS_OWNER) {//attivo server listener
                        System.out.println("entro2");
                        serverSocketThread = new ServerSocketThread();
                        serverSocketThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        System.out.println("serversocketthread!!!");
                        serverSocketThread.setUpdateListener(new ServerSocketThread.OnUpdateListener() {
                            public void onUpdate(String obj) {
                                setReceivedText(obj);
                            }
                        });
                    }
                }
                    /*if(!IS_OWNER) {
                        closeSocketAndInterrupt();
                        String connectionMessage = "~~connectionMessage~~";
                        ClientSocket clientSocket = new ClientSocket(MainActivity.this, this, connectionMessage, isRequest, null, null, null, clientSocketListener);
                        clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        startClientSocketListener();
                    }*/
                    //send an empty message in order to get the IP address
                    ///////////////////////////////////////////////////////////////////////////////////////
                    //mManager.requestConnectionInfo(mChannel,this);
                    //ClientSocket clientSocket = new ClientSocket(MainActivity.this, this, "", false, null);
                    //clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                //}
                break;
            case R.id.main_activity_button_server_start:
                //if (mManager == null) return;
                //mManager.requestConnectionInfo(mChannel,this);
                /*serverSocketThread = new ServerSocketThread();
                serverSocketThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                serverSocketThread.setUpdateListener(new ServerSocketThread.OnUpdateListener() {
                    public void onUpdate(String obj) {
                        setReceivedText(obj);
                    }
                });*/



                //ClientSocket clientSocket1 = new ClientSocket(MainActivity.this, this, "", false, null);
                //clientSocket1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


                break;
            case R.id.main_activity_button_server_stop:
                if(serverSocketThread != null) {
                    serverSocketThread.setInterrupted(true);
                } else {
                    Log.d(MainActivity.TAG,"serverSocketThread is null");
                }
                break;
            case R.id.main_activity_button_client_start:                    //button SEND
                requestDestination = EditIDDestination.getText().toString();
                String dataToSend = editTextTextInput.getText().toString();
                //if(IS_OWNER)  {
                    /*if(clientSocket.getSocket().isClosed()) {
                        System.out.println("serverSocketThread.client CLOSED");
                    }
                    else System.out.println("serverSocketThread.client NON E' CLOSED");
                    */
                    //clientSocket = new ClientSocket(MainActivity.this, this, dataToSend, isRequest, null,null, null, clientSocketListener);
                    //clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    /*try {
                        sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    /*if(clientSocket.getSocket().isClosed()) {
                        System.out.println("serverSocketThread.client CLOSED");
                    }
                    else System.out.println("serverSocketThread.client NON E' CLOSED");*/
                    //ServerSocketThreadSender serverSocketThreadSender = new ServerSocketThreadSender(MainActivity.this,this, serverSocketThread.client, dataToSend);
                    //serverSocketThreadSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    //return;
                //}

                if(device == null && requestDestination.equals("")) {        //Didn't select anything, didn't write anything
                    Toast.makeText(MainActivity.this,"Please discover and select a device OR write the destination below",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(!requestDestination.equals("")) {  //Didn't select a device, wrote destination
                    //ottenere la lista di dispositivi e connettersi a tutti
                    boolean found = false;
                    for(int i = 0; i < deviceListItems.length; i++) {
                        if(deviceListItems[i].deviceName.equals(requestDestination)){      //what was written in the request was, in reality, the name of a device present in the list
                            device = deviceListItems[i];
                            System.out.println("mi connetto a un dispositivo " + device.deviceName);
                            System.out.println("******* 3");
                            connect(device);                                        //connect to the device, then send the message
                            isRequest = false;
                            found = true;
                            if (!dataToSend.equals("")) {
                                if (!IS_OWNER) {
                                    closeSocketAndInterrupt();
                                    ClientSocket clientSocket = new ClientSocket(MainActivity.this, this, dataToSend, isRequest, null, null, null, clientSocketListener);
                                    clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    startClientSocketListener();
                                } else {//IS_OWNER
                                    ServerSocketThreadSender serverSocketThreadSender = new ServerSocketThreadSender(MainActivity.this, this, serverSocketThread.client, dataToSend, isRequest, null, null, null);
                                    serverSocketThreadSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                            break;
                        }
                    }
                    if(!found) {
                        Toast.makeText(MainActivity.this,"Request is being forwarded to all available devices 10",Toast.LENGTH_SHORT).show();
                        System.out.println("Request is being forwarded to all available devices 1");
                        for (int i = 0; i < deviceListItems.length; i++) {

                            if(deviceListItems[i].deviceName.equals("WAS-LX1A") || deviceListItems[i].deviceName.equals("HUAWEI P10 lite")) {
                                System.out.println("SALTO HUAWEI P10 LITE");
                                continue;
                            }/*
                            if(deviceListItems[i].deviceName.equals("JoeCattivo") || deviceListItems[i].deviceName.equals("Sim_roz") || deviceListItems[i].deviceName.equals("MAR-LX1A")) {
                                System.out.println("SALTO HUAWEI mio");
                                continue;
                            }*/
                            device = deviceListItems[i];
                            System.out.println("mi connetto a una caterva di dispositivi!!!!!!!!! " + device.deviceName);
                            System.out.println("******* 4");
                            connect(device);                                        //connect to each device, then send the message to all of them
                            isRequest = true;
                            String firstDeviceName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
                            seenDevices = firstDeviceName;
                            seenDevicesUpdate();
                            if (!dataToSend.equals("")) {
                                if (!IS_OWNER) {
                                    System.out.println("mi connetto a una caterva di dispositivi 1!!!!!!!!! " + device.deviceName);
                                    closeSocketAndInterrupt();
                                    ClientSocket clientSocket = new ClientSocket(MainActivity.this, this, dataToSend, isRequest, requestDestination, firstDeviceName, seenDevices, clientSocketListener);
                                    clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    startClientSocketListener();
                                } else {//IS_OWNER
                                    System.out.println("mi connetto a una caterva di dispositivi 2!!!!!!!!! " + device.deviceName);
                                    ServerSocketThreadSender serverSocketThreadSender = new ServerSocketThreadSender(MainActivity.this, this, serverSocketThread.client, dataToSend, isRequest, firstDeviceName, requestDestination, seenDevices);
                                    serverSocketThreadSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                        }
                    }
                }
                else {                                                //Selected a device, didn't write anything
                    System.out.println("******* 5 GIUSTO");
                    isRequest = false;
                    if (!dataToSend.equals("")) {
                        if (!IS_OWNER) {
                            closeSocketAndInterrupt();
                            clientSocket = new ClientSocket(MainActivity.this, this, dataToSend, isRequest, null, null, null, clientSocketListener);
                            clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            startClientSocketListener();
                        }
                        else{//IS_OWNER
                            dataToSend = editTextTextInput.getText().toString();
                            ServerSocketThreadSender serverSocketThreadSender = new ServerSocketThreadSender(MainActivity.this, this, serverSocketThread.client, dataToSend, isRequest, null, null, null);
                            serverSocketThreadSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
                }
                System.out.println("**** ENTRA CLIENT START");
                break;
            case R.id.main_activity_button_configure:
                if (mManager == null) return;
                //System.out.println("ENTRO BUTTON CONFIGURE!");
                //mManager.requestConnectionInfo(mChannel,this);
                if(!IS_OWNER){  //as soon as the two devices connect, the client sends a default message to the server and the clientSocketListener starts listening
                    isRequest = false;
                    closeSocketAndInterrupt();
                    String connectionMessage = "~~connectionMessage~~";
                    ClientSocket clientSocket = new ClientSocket(MainActivity.this, this, connectionMessage, isRequest, null, null, null, clientSocketListener);
                    clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    startClientSocketListener();
                }
                //ClientSocket clientSocket1 = new ClientSocket(MainActivity.this, this, "", false, null,null, null);
                //clientSocket1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                break;
            case R.id.main_activity_button_client_stop:
                System.out.println("disconnect");
                if(IS_OWNER){
                    serverSocketThread.setInterrupted(true);
                }
                if(!IS_OWNER){
                    clientSocketListener.setInterrupted(true);
                }
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                disconnect();
                System.out.println("disconnect group");
                groupDisconnect();
                break;
            default:
                break;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
        if(wifiP2pInfo.groupOwnerAddress == null) return;
        String hostAddress = wifiP2pInfo.groupOwnerAddress.getHostAddress();   //192.168.43.


        if (hostAddress == null) hostAddress = "host is null";

        Log.d(MainActivity.TAG,"wifiP2pInfo.groupOwnerAddress.getHostAddress() " + wifiP2pInfo.groupOwnerAddress.getHostAddress());
        hostClient = wifiP2pInfo.groupOwnerAddress.getHostAddress();
        if(hostClient == null){
            makeToast("RESTART CONNECTION, SOMETHING WENT WRONG");
            return;
        }
        IS_OWNER = wifiP2pInfo.isGroupOwner;

        if(IS_OWNER) {
            makeToast("OWNER-HOST: " + get_IP());

        } else {
            buttonServerStop.setVisibility(View.INVISIBLE);
            buttonServerStart.setVisibility(View.INVISIBLE);
            makeToast("CLIENT: " + get_IP());
        }
    }

    public void setReceivedText(final String data) {
        System.out.println("DATonUpdateA MAINACTIVITY SETRECEIVEDTEXT == " + data);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int i=0;
                System.out.println(data);
                if(data.equals("~~connectionMessage~~")){
                    return;
                }
                if(data.charAt(0) == '$' && data.charAt(1) == '$') {        //IT IS A REQUEST, example: $$HUAWEI P9 lite||Simo_Roz::Hi how are you?
                    /*while (data.charAt(i) != '#' && data.charAt(i+1) != '#') {
                        i++;
                    }*/
                    //IP_address = data.substring(2,i+1);
                    //System.out.println("****** IP_address quando IT IS REQUEST == " + IP_address);
                    /*if(device != null && IP_address != null) {
                        if(!data.equals("automatic##data")) {
                            arrList.add(new MyEntry(device, IP_address));
                        }
                    }*/
                    if(IS_OWNER){
                        System.out.println("owner entro");
                        serverSocketThread.setRequestMessage(data);
                    }
                    System.out.println("****** it IS A REQUEST: " + data);
                    textViewRequest.setText(data);                          //show everything BUT IN REALITY THE INTERMEDIATE USER SHOULD NOT BE ALLOWED TO SEE ANYTHING
                    ignoreChange = false;
                    //HERE textViewRequest.OnTextChanged() IS INVOKED
                }
                else{                                                       //IT IS NOT A REQUEST, example: 192.168.1.72##Hi how are you?
                    /*final String data_to_show = data.substring(i+3);
                    IP_address = data.substring(0,i+1);
                    if(device != null && IP_address != null) {
                        arrList.add(new MyEntry(device,IP_address));
                        //map.put(device, IP_address);
                    }
                    System.out.println("****** it IS NOT A REQUEST:" + data);
                    System.out.println("****** " + data_to_show);
                    System.out.println("****** " + IP_address);*/
                    System.out.println("SONO NELL'ELSE");

                    if(data.charAt(0) == '~' && data.charAt(1) == '~'){
                        System.out.println("SONO NELL'ELSE E POI IF");
                        String data_to_show = data.substring(2);
                        textViewReceivedData.setText(data_to_show);
                    }
                    if(data != ""){
                        System.out.println("SONO NELL'IF FINALE");
                        System.out.println("data alla fine e' " + data);
                        final String data_with_name = device.deviceName + ">> " + data;
                        System.out.println("data_with_name alla fine e' " + data_with_name);
                        textViewReceivedData.setText(data_with_name);           //show all text in the spot for requests
                        System.out.println("finito setText!!!");
                    }
                }
                //System.out.println("**** SET_RECEIVED_TEXT");
            }
        });

        textViewRequest.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!ignoreChange) {
                    ignoreChange = true;
                    System.out.println("***** before == " + before);
                    System.out.println("***** s == " + s);
                    //$$nameOrigin||nameDestination::visitedDevices~~requestMessage
                    //$$Huawei p10 lite||Simo_Roz::Huawei p10 lite++device1++device2++device3~~Hi how are you?
                    //message example $$HUAWEI P9 lite||Simo_Roz::Hi how are you?
                    //check if 192.168.1.72 is present

                    int jj = 1, ii = 1, kk = 1;
                    while (s.charAt(kk) != '|' && s.charAt(kk + 1) != '|') {
                        kk++;
                    }
                    System.out.println("primo while");

                    while (s.charAt(ii) != ':' && s.charAt(ii + 1) != ':') {
                        ii++;
                    }
                    System.out.println("secondo while");

                    while (s.charAt(jj) != '~' && s.charAt(jj + 1) != '~') {
                        jj++;
                    }
                    System.out.println("terzo while");

                    String nameOrigin = s.subSequence(2, kk + 1).toString();//ok
                    System.out.println("***** nameOrigin == " + nameOrigin);
                    String nameDestination = s.subSequence(kk + 3, ii + 1).toString();//ok
                    System.out.println("***** nameDestination == " + nameDestination);
                    String stringSeenDevices = s.subSequence(ii + 3, jj + 1).toString();//ok forse taglio qualcosa CONTROLLA!!!!!!!
                    System.out.println("***** stringSeenDevices == " + stringSeenDevices);
                    String dataToSend = s.subSequence(jj + 3, s.length()).toString();//ok
                    System.out.println("**** dataToSend == " + dataToSend);

                    //CONTROLLAARE CHE I DIVERSI DISPOSITIVI DI stringSeenDevices SIANO PRESENTI PRIMA DI CONNETTERTI

                    //ogni dispositivo sa a quanti dispositivi si connette:
                    //esempio: mi connetto a tre dispositivi --> se voglio essere sicuro che non ho trovato l'obiettivo, devo ricevere tre "no" dai dispositivi
                    //se uno di questi ha due figli, allora esso deve ricevere due no affinche' possa restituire al dispositivo originario un No

                    String deviceName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
                    System.out.println("**** deviceName == " + deviceName);
                    System.out.println("**** nameDestination == " + nameDestination);
                    if (nameDestination.equals(deviceName)) {
                        System.out.println("entrato");
                        final String data_with_name = nameOrigin + ">> " + dataToSend;
                        System.out.println("**** datawithname == " + data_with_name);
                        textViewReceivedData.setText(data_with_name);
                        return;
                    }


                    if (!nameDestination.equals("")) {//ok
                        Toast.makeText(MainActivity.this, "Request is being forwarded to all available devices 2", Toast.LENGTH_SHORT).show();
                        System.out.println("Request is being forwarded to all available devices 2");
                        //ottenere la lista di dispositivi e connettersi a tutti
                        boolean found = false;
                        //disconnect();
                        //groupDisconnect();
                        //System.out.println("disconnect");
                        for (int i = 0; i < deviceListItems.length; i++) {//ok
                            if (deviceListItems[i].deviceName.equals(nameDestination)) {
                                isRequest = false;                                              //sono giunto a destinazione??
                                found = true;
                                if (!dataToSend.equals("")) {
                                    if (!IS_OWNER) {
                                        dataToSend = "~~" + nameOrigin + ">>" + dataToSend;
                                        closeSocketAndInterrupt();
                                        ClientSocket clientSocket = new ClientSocket(MainActivity.this, MainActivity.this, dataToSend, isRequest, null, null, null, clientSocketListener);
                                        clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                        startClientSocketListener();
                                    } else {//IS_OWNER
                                        dataToSend = "~~" + nameOrigin + ">>" + dataToSend;
                                        //System.out.println("perform click*");
                                        //buttonConfigure.performClick();
                                        ServerSocketThreadSender serverSocketThreadSender = new ServerSocketThreadSender(MainActivity.this, MainActivity.this, serverSocketThread.client, dataToSend, isRequest, nameOrigin, nameDestination, seenDevices);
                                        serverSocketThreadSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    }
                                }
                                break;
                            }
                        }
                    /*if(!found) {//ok: aggiungi
                        seenDevicesUpdate();
                        for (int i = 0; i < deviceListItems.length; i++) {
                            System.out.println("device numero " + i + " si chiama " + deviceListItems[i].deviceName);
                            String MyDeviceName = Settings.Global.getString(getContentResolver(), Settings.Global.DEVICE_NAME);
                            System.out.println("MyDeviceName == " + MyDeviceName);
                            //if(MyDeviceName.equals("HUAWEI P9 lite") || MyDeviceName.equals("HUAWEI HUAWEI VNS-L21") || MyDeviceName.equals("HUAWEI VNS-L21")){
                            //    if(deviceListItems[i].deviceName.equals("Sim_Roz") || (deviceListItems[i].deviceName.equals("MAR-LX1A")) ||(deviceListItems[i].deviceName.equals("HUAWEI MAR-LX1A"))) {continue;}
                            //}
                            //if(MyDeviceName.equals("Sim_Roz") || MyDeviceName.equals("MAR-LX1A")){
                            //    if(deviceListItems[i].deviceName.equals("HUAWEI P10 lite")) {continue;}
                            //}
                            //if(MyDeviceName.equals("HUAWEI P9 lite") || MyDeviceName.equals("HUAWEI HUAWEI VNS-L21") || MyDeviceName.equals("HUAWEI VNS-L21")){
                            //    if(deviceListItems[i].deviceName.equals("Sim_Roz") || (deviceListItems[i].deviceName.equals("MAR-LX1A")) ||(deviceListItems[i].deviceName.equals("HUAWEI MAR-LX1A"))) {continue;}
                            //}
                            if(MyDeviceName.equals("Sim_Roz") || MyDeviceName.equals("MAR-LX1A")){
                                if(deviceListItems[i].deviceName.equals("HUAWEI HUAWEI VNS-L21") || deviceListItems[i].deviceName.equals("HUAWEI VNS-L21") || deviceListItems[i].deviceName.equals("HUAWEI P9 lite")) {
                                    System.out.println("salto HUWAEI VNS-L21");
                                    continue;
                                }
                            }
                            device = deviceListItems[i];
                            System.out.println("******* 7");
                            System.out.println("device numero " + i + " si chiama " + deviceListItems[i].deviceName);
                            makeToast("from ma**a to davide");
                            System.out.println("from ma**a to davide");
                            connect(deviceListItems[i]);                                        //connect to each device, then send the message to all of them
                            isRequest = true;
                            if (!dataToSend.equals("")) {
                                if(!IS_OWNER) {
                                    //System.out.println("perform click owner");
                                    //buttonConfigure.performClick();
                                    closeSocketAndInterrupt();
                                    ClientSocket clientSocket = new ClientSocket(MainActivity.this, MainActivity.this, dataToSend, isRequest, nameDestination, nameOrigin, seenDevices, clientSocketListener);
                                    clientSocket.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    startClientSocketListener();
                                }
                                else{//IS_OWNER
                                    //System.out.println("perform click NON owner");
                                    //buttonConfigure.performClick();
                                    ServerSocketThreadSender serverSocketThreadSender = new ServerSocketThreadSender(MainActivity.this, MainActivity.this, serverSocketThread.client, dataToSend, isRequest, nameOrigin, nameDestination, seenDevices);
                                    serverSocketThreadSender.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                }
                            }
                        }
                    }*/
                    } else {//ok
                        System.out.println("******* 8: IN TEORIA REQUEST DESTINATION E' \"\" MA NON DEVE ESSERE POSSIBILE :(");
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }




    //AGGIUNTA: the function allows to retrieve the IP address of the device running the code
    public String get_IP(){
        WifiManager wm1= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String IP_this = Formatter.formatIpAddress(wm1.getConnectionInfo().getIpAddress());
        System.out.println("**** get_IP() == " + IP_this);
        return IP_this;
    }

    public void closeSocketAndInterrupt(){
        if(clientSocket!=null) {
            try {
                clientSocket.destroySocket();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(clientSocketListener!=null) {
            System.out.println("set interrupted(true)");
            clientSocketListener.setInterrupted(true);
        }
    }

    public void seenDevicesUpdate(){
        for(int i = 0; i < deviceListItems.length; i++) {
            //device = deviceListItems[i];
            seenDevices = seenDevices + "++" + deviceListItems[i].deviceName;           //before sending any messages I collect each device name in the seenSevices String
            //connect(device);                                                //connect to the device, then send the message
        }
        System.out.println("updated seenDevices == " + seenDevices);
    }

    public void startClientSocketListener(){
        try {
            clientSocketListener = new ClientSocketListener(MainActivity.this,this, clientSocket);
            clientSocketListener.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            clientSocketListener.setUpdateListener(new ClientSocketListener.OnUpdateListener() {
                public void onUpdate(String obj) {
                    setReceivedText(obj);
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("PARTITO clientSocketListener");
    }







    public class MyEntry{
        private String val;
        private WifiP2pDevice key;

        public MyEntry(WifiP2pDevice k, String v){ //val==string==IP    key==WifiP2pDevice==device
            key = k;
            val = v;
        }
        public WifiP2pDevice getKey(){
            return key;
        }
        public String getValue(){
            return val;
        }
    }
}

