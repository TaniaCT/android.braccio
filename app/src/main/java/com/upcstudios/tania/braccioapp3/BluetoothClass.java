package com.upcstudios.tania.braccioapp3;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by tania on 13/11/2017.
 */

public class BluetoothClass {
    private Context BTContext;
    private BluetoothAdapter BTAdapter = null;
    private ArrayAdapter<String> PairedDevicesArrayAdapter;
    private String BTAddress = null;
    private BluetoothSocket BTSocket = null;
    private BluetoothDevice BTDevice;
    private static UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public boolean BTConnected = false;
    public ConnectedThread MyBTConnection;
    public List<String> Messages = new ArrayList<String>();


    public BluetoothClass(Context context) {
        BTContext = context;
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        PairedDevicesArrayAdapter = new ArrayAdapter<String>(context, R.layout.device_list_item);
        VerifyBT();
    }

    public BluetoothAdapter GetBTAdapter() {
        return BTAdapter;
    }

    public void VerifyBT () {
        if (BTAdapter == null) Toast.makeText(BTContext, "This device does not support Bluetooth", Toast.LENGTH_LONG).show();
        else {
            if (!BTAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                BTContext.startActivity(intent);
            }
        }
    }

    public ArrayAdapter<String> GetPairedDevices() {
        if (PairedDevicesArrayAdapter.getCount() != 0) PairedDevicesArrayAdapter.clear();
        VerifyBT();
        Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device: pairedDevices) {
                PairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        return PairedDevicesArrayAdapter;
    }

    public void SetAddress (String address) throws IOException {
        BTAddress = address;
        BTDevice = BTAdapter.getRemoteDevice(BTAddress);

        /*if (BTSocket != null) {
            try {
                BTSocket.close();
                SetSocket();
                if (BTConnected) {
                }
            }
            catch (IOException e) { Toast.makeText(BTContext, "Could close the connection", Toast.LENGTH_LONG).show();}
        }
        else {*/
            SetSocket();
            MyBTConnection = new ConnectedThread(BTSocket);
            MyBTConnection.start();
        //}
    }

    public String GetAddress () { return BTAddress; }

    private void SetSocket() throws IOException {
        try {
            BTSocket = BTDevice.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
            for (int i = 0; i<100; i++);
            try {
                BTSocket.connect();
            }
            catch (IOException e) {
                Toast.makeText(BTContext, "Error connecting: " + e.getMessage(), Toast.LENGTH_LONG).show();
                /*try {
                    BTSocket.close();
                }
                catch (IOException e2) {}*/
            }
            BTConnected = BTSocket.isConnected();
        }
        catch (IOException e) { Toast.makeText(BTContext, "Socket creation failed. Error: " + e.getMessage(), Toast.LENGTH_LONG).show();}
    }

    public void SetUUID (UUID NewUUID) { BTMODULEUUID = NewUUID; }

    public void CloseConnection() {
        try {
            BTSocket.close();
            BTConnected = false;
        }
        catch (IOException e) {}
    }

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        InputStream tmpIn = null;

        public ConnectedThread(BluetoothSocket socket) {
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void interrupt() {
            super.interrupt();
            // Algo?
        }

        public void run() {
            //byte[] buffer = new byte[1024];  // buffer store for the stream
            //int bytes;
            char ch; // bytes returned from read()
            String data = new String();

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    data = "";
                    // Read from the InputStream
                    while((ch = (char)mmInStream.read()) != '#') {
                        data = data + ch ;
                    }
                    data = data + '#';
                    //String msg = new String(data);

                    //bytes = mmInStream.read(buffer);
                    //String msg = new String(buffer, 0, bytes);

                    // Send the obtained bytes to the UI activity
                    MessageReceived(data);
                    //this.interrupt();

                    //BTHandler.obtainMessage(handlerState, bytes, -1, msg).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            try {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                Toast.makeText(BTContext, "Connection failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void MessageReceived(String msg) {
        try {
            int endOfLineIndex = msg.indexOf("#");

            if (endOfLineIndex > 0) {
                Messages.add(msg.substring(0, endOfLineIndex));
            }
            try {
                this.notify();
            } catch (IllegalMonitorStateException e){}
        } catch (Exception e) {Toast.makeText(BTContext, "Failed to received message: " + e.getMessage(), Toast.LENGTH_SHORT).show();}
    }

    public void writeMessage(String data) {
        MyBTConnection.write(data);
    }
}
