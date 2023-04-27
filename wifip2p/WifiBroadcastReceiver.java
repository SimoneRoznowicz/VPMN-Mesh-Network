package com.examples.akshay.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaActionSound;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;


public class WifiBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG = "===WifiBReceiver";

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public WifiBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                                       MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.d(WifiBroadcastReceiver.TAG,"WIFI P2P ENABLED");
                try {
                    mActivity.setStatusView(Constants.P2P_WIFI_ENABLED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(WifiBroadcastReceiver.TAG,"WIFI P2P NOT ENABLED");
                try {
                    mActivity.setStatusView(Constants.P2P_WIFI_DISABLED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.d(WifiBroadcastReceiver.TAG,"WIFI_P2P_PEERS_CHANGED_ACTION");
            if (mManager != null) {
                MyPeerListener myPeerListener = new MyPeerListener(mActivity);
                mManager.requestPeers(mChannel, myPeerListener);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                try {
                    mActivity.setStatusView(Constants.NETWORK_CONNECT);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                // It's a disconnect
                Log.d(WifiBroadcastReceiver.TAG,"Its a disconnect");
                try {
                    mActivity.setStatusView(Constants.NETWORK_DISCONNECT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.d(WifiBroadcastReceiver.TAG,"WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
            // Respond to this device's wifi state changing


        } else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 10000);
            if( state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED ) {
                try {
                    mActivity.setStatusView(Constants.DISCOVERY_INITATITED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else if(state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                try {
                    mActivity.setStatusView(Constants.DISCOVERY_STOPPED);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
    }
}
