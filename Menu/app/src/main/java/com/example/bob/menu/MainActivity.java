package com.example.bob.menu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.ArraySet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Collection;
import java.util.Iterator;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;

public class MainActivity extends Activity implements WifiP2pManager.ActionListener, WifiP2pManager.PeerListListener {

    private Button calibrateBeacon,connect;

    private int beaconAmaj = 0, beaconAmin = 0, beaconBmaj = 0, beaconBmin = 0, beaconCmaj = 0, beaconCmin = 0;
    private float beaconBdist = 0, beaconCdist = 0;

    private WifiP2pManager mManager;
    //The thing returned with p2p
    private WifiP2pManager.Channel mChannel;
    //For them intents inside Android
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private Collection peerList;

    private String serverIP = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calibrateBeacon = (Button) findViewById(R.id.calibrateB);
        calibrateBeacon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, calibeateLocation.class);
                startActivityForResult(intent, 0);
            }
        });

        connect = (Button) findViewById(R.id.start);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(beaconAmaj == 0 || beaconAmin == 0) {
                    Intent intent = new Intent(MainActivity.this, calibeateLocation.class);
                    startActivityForResult(intent, 0);
                }
                else {
                    Intent startIntent = new Intent(MainActivity.this, TrackActivity.class);
                    Bundle bundle = new Bundle();
                    startIntent.putExtra("beaconAmaj", beaconAmaj);
                    startIntent.putExtra("beaconAmin", beaconAmin);
                    startIntent.putExtra("beaconBmaj", beaconBmaj);
                    startIntent.putExtra("beaconBmin", beaconBmin);
                    startIntent.putExtra("beaconBdist", beaconBdist);
                    startIntent.putExtra("beaconCmaj", beaconCmaj);
                    startIntent.putExtra("beaconCmin", beaconCmin);
                    startIntent.putExtra("beaconCdist", beaconCdist);
                    startActivity(startIntent);
                }
            }
        });

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(getApplicationContext(), getMainLooper(), null);
        mIntentFilter = new IntentFilter();
        mReceiver = new MyBroadcastReceiver(mManager, mChannel, MainActivity.this);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        registerReceiver(mReceiver, mIntentFilter);
        mManager.discoverPeers(mChannel, MainActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        beaconAmaj = data.getIntExtra("beaconAmaj", 0);
        beaconAmin = data.getIntExtra("beaconAmin", 0);
        beaconBmaj = data.getIntExtra("beaconBmaj", 0);
        beaconBmin = data.getIntExtra("beaconBmin", 0);
        beaconBdist = data.getFloatExtra("beaconBdist", 0);
        beaconCmin = data.getIntExtra("beaconCmin", 0);
        beaconCmaj = data.getIntExtra("beaconCmaj", 0);
        beaconCdist = data.getFloatExtra("beaconCdist", 0);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(int reason) {

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        peerList = peers.getDeviceList();
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private MainActivity mActivity;

        public MyBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mActivity) {
            super();
            this.mActivity = mActivity;
            this.mChannel = mChannel;
            this.mManager = mManager;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION))
            {
                //Merge dracia + notify activity
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
                {
                    //merge, all good
                }
                else
                {
                    //Yo, turn wifi on
                    Toast.makeText(mActivity, "Please turn on wifi and try again!", Toast.LENGTH_LONG).show();
                }
            }else if(action.equals(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION))
            {
                if(mManager!=null)
                {
                    Toast.makeText(MainActivity.this, "mManager not NULL", Toast.LENGTH_SHORT);
                    mManager.requestPeers(mChannel, mActivity);
                }else {
                    Toast.makeText(MainActivity.this, "mManager is NULL", Toast.LENGTH_SHORT);
                }
            }else if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION))
            {

            }else if(action.equals(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION))
            {

            }
        }
    }
}
