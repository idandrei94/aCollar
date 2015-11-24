package com.example.bob.menu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Objects;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;

public class TrackActivity extends Activity {

    //Our iBeacon info
    private static final String BEACON_UUID = "f7826da64fa24e988024bc5b71e0893e";

    //Laboratory 13
    private static final int BEACON_MAJOR_1 = 3793;
    private static final int BEACON_MINOR_1 = 3569;

    //Gork
    private static final int BEACON_MAJOR_2 = 28629;
    private static final int BEACON_MINOR_2 = 40160;

    //iBaliza
    private static final int BEACON_MAJOR_3 = 19686;
    private static final int BEACON_MINOR_3 = 1936;

    private int aMinor, aMajor, bMinor, bMajor, cMinor, cMajor;

    private float distanceA = -1, distanceB = -1, distanceC = -1, yB, xC;

    private long timeOfDeparture = 0;

    private String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        ((Button) findViewById(R.id.finish)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        aMinor = aMajor = bMinor = bMajor = cMinor = cMajor = -1;

        Bundle arguments = getIntent().getExtras();

        aMajor = getIntent().getExtras().getInt("beaconAmaj", 0);
        aMinor = getIntent().getExtras().getInt("beaconAmin", 0);
        bMajor = getIntent().getExtras().getInt("beaconBmaj", 0);
        bMinor = getIntent().getExtras().getInt("beaconCmin", 0);
        yB = getIntent().getExtras().getFloat("beaconBdist", 0);
        cMinor = getIntent().getExtras().getInt("beaconCmin", 0);
        cMajor = getIntent().getExtras().getInt("beaconCmaj", 0);
        xC = getIntent().getExtras().getFloat("beaconCdist", 0);
        address = getIntent().getExtras().getString("address", "");


        BluetoothManager btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = btManager.getAdapter();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 0);
        }

        //I made a thread so i can Thread.sleep
        //It sleeps 3 seconds before terminating the search
        Runnable find = new FindDevices(btAdapter);
        Thread t = new Thread(find);
        t.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_track, menu);
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

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            int minor = getMinor(scanRecord);

            if (doWeWantThisDevice(scanRecord)) {
                if (minor == aMinor) {
                    distanceA = (float) calculateDistance(-70, rssi);
                } else if (minor == bMinor) {
                    distanceB = (float) calculateDistance(-70, rssi);
                } else if (minor == cMinor) {
                    distanceC = (float) calculateDistance(-70, rssi);
                }
            }
            if (!whereAmI()) {
                ((TextView) findViewById(R.id.trackText)).setText("Out of area!" + ((System.currentTimeMillis() - timeOfDeparture) / 1000));
                if (timeOfDeparture == 0) {
                    timeOfDeparture = System.currentTimeMillis();
                } else if ((System.currentTimeMillis() - timeOfDeparture) / 1000 > 5) {
                    timeOfDeparture = 0;
                    new RetrieveFeedTask().execute();
                }
            }
        }
    };

   private class RetrieveFeedTask extends AsyncTask<Object, String, String> {


        protected void onPostExecute() {
            // TODO: check this.exception
            // TODO: do something with the feed
        }

       @Override
       protected String doInBackground(Object... params) {
           try {
               Socket socket = new Socket(address, 49152);
               DataOutputStream output = new DataOutputStream(socket.getOutputStream());
               output.writeInt(0);
               socket.close();

           } catch (Exception e) {
               e.printStackTrace();
           }
           return null;
       }
   }

    private class FindDevices implements Runnable {

        BluetoothAdapter btAdapter;

        public FindDevices(BluetoothAdapter btAdapter) {
            this.btAdapter = btAdapter;
        }

        @Override
        public void run() {
            while(true) {
                if(true) {
                    btAdapter.startLeScan(leScanCallback);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    btAdapter.stopLeScan(leScanCallback);
                }
            }
        }
    }

    private String getUuidAsString(byte[] ba) {
        StringBuilder sb = new StringBuilder();
        for (int i=9; i < 25; i++) {
            sb.append(String.format("%02X", ba[i]));
        }
        return sb.toString();
    }

    private int getMajor(byte[] ba) {
        StringBuilder sb = new StringBuilder();
        for (int i=25; i < 27; i++) {
            sb.append(String.format("%02X", ba[i]));
        }
        try {
            return Integer.valueOf(sb.toString().toLowerCase().trim(), 16);
        } catch(Exception e)
        {
            return -1;
        }
    }

    private int getMinor(byte[] ba) {
        StringBuilder sb = new StringBuilder();
        for (int i=27; i < 29; i++) {
            sb.append(String.format("%02X", ba[i]));
        }
        try {
            return Integer.valueOf(sb.toString().toLowerCase().trim(), 16);
        } catch(Exception e)
        {
            return -1;
        }
    }

    private int getTxPower(byte[] ba) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X", ba[29]));
        try {
            return Integer.valueOf(sb.toString().toLowerCase().trim(), 16);
        } catch(Exception e)
        {
            return -1;
        }
    }

    private boolean doWeWantThisDevice(byte[] scanRec) {
        String uuid = getUuidAsString(scanRec);
        int major = getMajor(scanRec);
        int minor = getMinor(scanRec);
        return BEACON_UUID.compareToIgnoreCase(uuid) == 0 &&
                ((BEACON_MAJOR_1 == major && BEACON_MINOR_1 == minor)
                        || (BEACON_MAJOR_2 == major && BEACON_MINOR_2 == minor)
                        || (BEACON_MAJOR_3 == major && BEACON_MINOR_3 == minor));
    }

    private double calculateDistance(int txPower, int rssi) {
        if (rssi == 0){
            return -1;
        }
        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0){
            return 0.65 * Math.pow(ratio, 10.0);
        }else{
            double accuracy = (0.89976)*Math.pow(ratio, 7.7095) + 0.111;
            return 0.65*accuracy;
        }
    }

    private boolean whereAmI() {
        float x, y;

        x = (xC*xC - distanceC*distanceC + distanceA*distanceA) / (2*xC);
        y = (yB * yB - distanceB*distanceB + distanceA * distanceA) / (2*yB);

        return x >= 0-0.5 && x <= xC +0.5 && y >= 0-0.5 && y <= yB+0.5;
    }

    private float calcDistance(float x1, float y1, float x2, float y2) {
        return (float)Math.sqrt((x1-x2)*(x1-x2) + (y1 - y2) * (y1 - y2));
    }


}
