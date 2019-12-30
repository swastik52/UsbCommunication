package com.example.usbserialcom;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usbserialcom.Utils.Utils;
import com.example.usbserialcom.helper.DataAvailableListner;
import com.example.usbserialcom.helper.UsbMessageListner;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements UsbMessageListner {
    public static final String INTENT_ACTION_GRANT_USB = BuildConfig.APPLICATION_ID + ".GRANT_USB";

    UsbManager manager;
    List<UsbSerialDriver> availableDrivers;
    private UsbSerialPort mSerialPort,port0,port1;
    private Button s1,s2;
    private static DataAvailableListner dataAvailableListner;
    TextView tv;
    int ii=0;

    private List<UsbSerialPort> mEntries = new ArrayList<UsbSerialPort>();
    public static final String TAG = MainActivity.class.getSimpleName();

    private final ExecutorService mExecutor = Executors.newCachedThreadPool();

    private SerialInputOutputManager mSerialIoManager;
    private SerialInputOutputManager mSerialIoManager1;
    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };

    private final SerialInputOutputManager.Listener mListener1 =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.updateReceivedData(data);
                        }
                    });
                }
            };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        tv=findViewById(R.id.tv);
        s1=findViewById(R.id.send1);
        s1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               sendData(1, "HEALTH\n", new DataAvailableListner() {
                        @Override
                        public void onDataAvailable(String data,int port) {
                                tv.setText(data);
                        }

                        @Override
                        public void timeOutError() {

                        }
                    });
                }

        });
        s2=findViewById(R.id.send2);
        s2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData(2, "HEALTH\n", new DataAvailableListner() {
                        @Override
                        public void onDataAvailable(String data,int port) {

                        }

                        @Override
                        public void timeOutError() {

                        }
                    });
                }

        });
        Utils utils = Utils.getContext();
        utils.setListner(this);
        setFilters();
        findDevices();
    }

    private void updateReceivedData(byte[] data) {
        /*final String message = "Read " + data.length + " bytes: \n"
                + HexDump.dumpHexString(data) + "\n\n";*/
        String data1 = null;
        try {
            data1 = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "updateReceivedData: "+data1);
    }
    private BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(INTENT_ACTION_GRANT_USB)){
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    Log.d(TAG, "onReceive: ");
                    askPermission1();
                    if (ii==2) {
                        openDevice();
                    }
                } else {
                    Toast.makeText(context, "USB permission denied", Toast.LENGTH_SHORT).show();
                }
            }
            else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                Toast.makeText(MainActivity.this, "", Toast.LENGTH_SHORT).show();
                //findDevices();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

            }
        }
    };

    private void findDevices() {
        availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        Log.d(TAG, "findDevices: "+availableDrivers.size());
        for (final UsbSerialDriver driver : availableDrivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            Log.d(TAG, String.format("+ %s: %s port%s",
                    driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
            mEntries.addAll(ports);
        }
        Log.d(TAG, "findDevices: deviceNo"+mEntries);
        if (!availableDrivers.isEmpty()) {
            askPermission();

        }
    }

    private void askPermission() {
        mSerialPort = mEntries.get(0);
        UsbDevice device = mSerialPort.getDriver().getDevice();
        if (!manager.hasPermission(device)) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            manager.requestPermission(device, usbPermissionIntent);
        }else{
            openDevice();
        }
    }
    private void askPermission1() {
        mSerialPort = mEntries.get(1);
        UsbDevice device = mSerialPort.getDriver().getDevice();
        if (!manager.hasPermission(device)) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), 0);
            manager.requestPermission(device, usbPermissionIntent);
        }else{
            openDevice();
        }
    }

    private void openDevice() {
        Log.d(TAG, "openDevice: "+availableDrivers);
        for (UsbSerialPort s :mEntries) {
            final UsbSerialPort port = mEntries.get(0);
            final UsbSerialDriver driver = port.getDriver();
            final UsbDevice device = driver.getDevice();
            final String title = String.format("Vendor %4X Product %4X", device.getVendorId(), device.getProductId());
            Log.d(TAG, "openDevice: "+title);
        }

        port0 = mEntries.get(0);
        Log.d(TAG, "openDevice:port "+port0);
        UsbDeviceConnection connection = manager.openDevice(port0.getDriver().getDevice());

        port1 = mEntries.get(1);
        UsbDeviceConnection connection1 = manager.openDevice(port1.getDriver().getDevice());
        if (connection == null) {
            // add UsbManager.requestPermission(driver.getDevice(), ..) handling here
            return;
        }

        //port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            port0.open(connection);
            port0.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            port1.open(connection1);
            port1.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }

        if (mSerialIoManager1 != null) {
            Log.i(TAG, "Stopping io manager111 ..");
            mSerialIoManager1.stop();
            mSerialIoManager1 = null;
        }
    }

    private void startIoManager() {
        if (port0 != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(port0, mListener);
            mExecutor.submit(mSerialIoManager);
        }

        if (port1 != null) {
            Log.i(TAG, "Starting io manager1 ..");
            mSerialIoManager1 = new SerialInputOutputManager(port1, mListener1);
            mExecutor.submit(mSerialIoManager1);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_ACTION_GRANT_USB);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mUsbReceiver);
    }

    public void send1() {
        String a = "HEALTH\n";
        try {
            port0.write(a.getBytes(),2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send2() {
        String a = "HEALTH\n";
        try {
            port1.write(a.getBytes(),2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendData(int port, String message, DataAvailableListner dataAvailable) {
        try {
            if (port==1) {
                port0.write(message.getBytes(), 2000);
            }else if (port==2){
                port1.write(message.getBytes(), 2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void secondActivity(View view) {
        Intent intent=new Intent(this,Main2Activity.class);
        startActivity(intent);
    }
}