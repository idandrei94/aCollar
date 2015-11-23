package com.example.bob.menu;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import static android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE;

public class calibeateLocation extends Activity {

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

    private float distance1 = 0, distance2 = 0, distance3 = 0;

    private boolean scan = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibeate_location);

        Button okButton = (Button) findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                if(distance1<distance2 && distance1<distance3) {
                    returnIntent.putExtra("beaconAmaj", BEACON_MAJOR_1);
                    returnIntent.putExtra("beaconAmin", BEACON_MINOR_1);
                }else if(distance2 < distance1 && distance2 < distance3) {
                    returnIntent.putExtra("beaconAmaj", BEACON_MAJOR_2);
                    returnIntent.putExtra("beaconAmin", BEACON_MINOR_2);
                }else {
                    returnIntent.putExtra("beaconAmaj", BEACON_MAJOR_3);
                    returnIntent.putExtra("beaconAmin", BEACON_MINOR_3);
                }
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

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
    protected void onPause() {
        super.onPause();
        scan = false;
    }

    @Override
    protected  void onResume() {
        super.onResume();
        scan = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calibeate_location, menu);
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

            if(doWeWantThisDevice(scanRecord)) {
                TextView text = null;
                switch(minor) {
                    case BEACON_MINOR_1:
                        text = (TextView)findViewById(R.id.text1);
                        distance1 = (float)calculateDistance(getTxPower(scanRecord), rssi);
                        text.setText("Beacon A: " + distance1 + "m.");
                        break;
                    case BEACON_MINOR_2:
                        text = (TextView)findViewById(R.id.text2);
                        distance2 = (float)calculateDistance(getTxPower(scanRecord), rssi);
                        text.setText("Beacon B: " + distance2 + "m.");
                        break;
                    case BEACON_MINOR_3:
                        text = (TextView)findViewById(R.id.text3);
                        distance3 = (float)calculateDistance(getTxPower(scanRecord), rssi);
                        text.setText("Beacon C: " + distance3 + "m.");
                        break;
                }
            } else {
                Toast.makeText(calibeateLocation.this, "not found", Toast.LENGTH_SHORT).show();
            }
        }
    };

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
                        Thread.sleep(5000);
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
            return Math.pow(ratio, 10.0);
        }else{
            double accuracy = (0.89976)*Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }
}
