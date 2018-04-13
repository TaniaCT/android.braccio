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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BluetoothClass {

    public boolean BTConnected = false;
    public static final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    public static final Object messageLock = new Object();
    public enum Commands {C_CONNECT, C_DISCONNECT, C_REQUEST, C_SENDTO, C_JOGGING, C_MOVE, C_HAND, C_NULL}
    public enum Communications{COM_SERIAL, COM_BLUETOOTH,COM_NULL}
    public enum JogCommand {JC_MINUS, JC_EQU, JC_PLUS, JC_NULL}
    public enum Joints{
        J_BASE, J_SHOULDER, J_ELBOW, J_WRIST_VER, J_WRIST_ROT, J_GRIPPER, J_NULL
        /*NULL(0), BASE(1),SHOULDER(2), ELBOW(3), WRIST_VER(4), WRIST_ROT(5), GRIPPER(6);
        private int value;
        private Joints(int val){
            value = val;
        }
        public int getValue() {
            return value;
        }*/
    }

    private Context BTContext;
    private BluetoothAdapter BTAdapter = null;
    private ArrayAdapter<String> PairedDevicesArrayAdapter;
    private String BTAddress = null;
    private BluetoothSocket BTSocket = null;
    private BluetoothDevice BTDevice;
    private UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private Thread btConnectionThread;
    private OutputStream mmOutStream = null;


    public BluetoothClass(Context context) {
        BTContext = context;
        BTAdapter = BluetoothAdapter.getDefaultAdapter();
        PairedDevicesArrayAdapter = new ArrayAdapter<String>(context, R.layout.device_list_item);
    }

    public BluetoothAdapter GetBTAdapter() {
        return BTAdapter;
    }

    public void VerifyBT() {
        if (BTAdapter == null)
            Toast.makeText(BTContext, "This device does not support Bluetooth", Toast.LENGTH_LONG).show();
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
        if (BTAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = BTAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    PairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
        else Toast.makeText(BTContext, "Bluetooth adapter disabled", Toast.LENGTH_SHORT).show();
        return PairedDevicesArrayAdapter;
    }

    public void SetAddress(String address) throws IOException {
        BTAddress = address;
        BTDevice = BTAdapter.getRemoteDevice(BTAddress);

        SetSocket();
        if (BTConnected) {
            btConnectionThread = new Thread(new ConnectedRunnable(BTSocket));
            btConnectionThread.start();
        }
    }

    public String GetAddress() {
        return BTAddress;
    }

    private void SetSocket() throws IOException {
        try {
            BTSocket = BTDevice.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
            for (int i = 0; i < 100; i++) ;
            try {
                BTSocket.connect();
                ArrayList<String> array = new ArrayList<String>();
                writeMessage(Commands.C_CONNECT, Communications.COM_BLUETOOTH, Joints.J_NULL, array);
            } catch (IOException e) {
                Toast.makeText(BTContext, "Error connecting: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                try {
                    BTSocket.close();
                }
                catch (IOException e2) {}
            }
            BTConnected = BTSocket.isConnected();
        } catch (IOException e) {
            Toast.makeText(BTContext, "Socket creation failed. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void SetUUID(UUID NewUUID) {
        BTMODULEUUID = NewUUID;
    }

    public void CloseConnection() {
        try {
            BTSocket.close();
            BTConnected = false;
            btConnectionThread.interrupt();
            mmOutStream = null;
        } catch (IOException e) {
        }
    }

    private class ConnectedRunnable implements Runnable{
        private final InputStream mmInStream;
        //private final OutputStream mmOutStream;

        public ConnectedRunnable(BluetoothSocket btsocket){
            // Get the input and output streams, using temp objects because
            // member streams are final
            InputStream tmpIn = null;
            try {
                tmpIn = btsocket.getInputStream();
                //tmpOut = btsocket.getOutputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
        }

        @Override
        public void run() {
            char ch; // bytes returned from read()
            String data;
            System.out.println("BTConnection: Begin Connection Thread");

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    data = "";
                    // Read from the InputStream
                    while ((ch = (char) mmInStream.read()) != '#') {
                        data = data + ch;
                    }

                    // Get lock and add new item
                    synchronized (messageLock) {
                        messageQueue.add(data);
                        messageLock.notifyAll();
                    }

                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    public void writeMessage(Commands command, Communications communication, Joints joint, ArrayList<String> arguments) {
        try {
            if(BTSocket != null && mmOutStream == null)
                mmOutStream = BTSocket.getOutputStream();
            String data = ProcessCommands(command, communication, joint, arguments);
            if (data != "") {
                data = data.concat("#");
                mmOutStream.write(data.getBytes());
            }

        } catch (IOException e) {
            System.out.println("BTConnection: Error writting to BT");
        }
    }

    public void writeMessage(String data) {
        try {
            if(BTSocket != null && mmOutStream == null)
                mmOutStream = BTSocket.getOutputStream();
            if (data != "") {
                data = data.concat("#");
                mmOutStream.write(data.getBytes());
            }

        } catch (IOException e) {
            System.out.println("BTConnection: Error writting to BT");
        }
    }

    public String ProcessCommands (Commands command, Communications communication, Joints joint, ArrayList<String> arguments) {
        String data = "";
        int arg1 = 0;
        int arg2 = 0;
        switch (command){
            case C_CONNECT: data = "0 1";
                break;
            case C_DISCONNECT: data = "1 1";
                break;
            case C_REQUEST: data = "2 1";
                break;
            case C_SENDTO: //SENDTO [COMMUNICATION, TEXT]
                switch (communication)
                {
                    case COM_SERIAL:
                        data = "3 0 ";
                        data = data.concat(arguments.get(0));
                        break;
                    case COM_BLUETOOTH:
                        data = "3 1 ";
                        data = data.concat(arguments.get(0));
                        break;
                    case COM_NULL:
                        break;
                    default: break;
                }
                break;
            case C_JOGGING:
                arg1 = Integer.parseInt(arguments.get(0));
                if(arg1 >= 0 && arg1 <= 2) {
                    switch (joint) {
                        case J_BASE:
                            data = data.concat("4 0 " + arguments.get(0));
                            break;
                        case J_SHOULDER:
                            data = data.concat("4 1 " + arguments.get(0));
                            break;
                        case J_ELBOW:
                            data = data.concat("4 2 " + arguments.get(0));
                            break;
                        case J_WRIST_VER:
                            data = data.concat("4 3 " + arguments.get(0));
                            break;
                        case J_WRIST_ROT:
                            data = data.concat("4 4 " + arguments.get(0));
                            break;
                        case J_GRIPPER:
                            data = data.concat("4 5 " + arguments.get(0));
                            break;
                        case J_NULL: break;
                        default: break;
                    }
                }
                break;
            case C_MOVE: //TODO: AÑADIR X Y Z Y POSICIONES DISPONIBLES
                switch (joint) {
                    case J_BASE:
                        data = data.concat("5 0 " + arguments.get(0));
                        break;
                    case J_SHOULDER:
                        data = data.concat("5 1 " + arguments.get(0));
                        break;
                    case J_ELBOW:
                        data = data.concat("5 2 " + arguments.get(0));
                        break;
                    case J_WRIST_VER:
                        data = data.concat("5 3 " + arguments.get(0));
                        break;
                    case J_WRIST_ROT:
                        data = data.concat("5 4 " + arguments.get(0));
                        break;
                    case J_GRIPPER:
                        data = data.concat("5 5 " + arguments.get(0));
                        break;
                    case J_NULL: break;
                    default: break;
                }
                break;
            /*case C_MOVE:
                arg1 = Integer.parseInt(arguments.get(0));
                if (arg1 > 0 && arg1 < Joints.values().length){
                    arg2 = Integer.parseInt(arguments.get(1));
                    if(arg1 == 0 && (arg2 >= 0 && arg2 <= 180)){
                        data = data.concat("6 " + arguments.get(0) + " " + arguments.get(1));
                    }
                    else if(arg1 == 1 && (arg2 >= 15 && arg2 <= 165)){
                        data = data.concat("6 " + arguments.get(0) + " " + arguments.get(1));
                    }
                    else if(arg1 == 2 && (arg2 >= 0 && arg2 <= 180)){
                        data = data.concat("6 " + arguments.get(0) + " " + arguments.get(1));
                    }
                    else if(arg1 == 3 && (arg2 >= 0 && arg2 <= 180)){
                        data = data.concat("6 " + arguments.get(0) + " " + arguments.get(1));
                    }
                    else if(arg1 == 4 && (arg2 >= 0 && arg2 <= 180)){
                        data = data.concat("6 " + arguments.get(0) + " " + arguments.get(1));
                    }
                    else if(arg1 == 5 && (arg2 >= 10 && arg2 <= 73)){
                        data = data.concat("6 " + arguments.get(0) + " " + arguments.get(1));
                    }
                }
                break;*/
            case C_HAND:
                data = data.concat("6 " + arguments.get(0));
                break;
            case C_NULL: break;
            default: break;

        }
        return data;
    }

    private String CommandRecieved (){

        return "";
    }
}
