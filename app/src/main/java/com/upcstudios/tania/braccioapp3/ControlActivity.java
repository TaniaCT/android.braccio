package com.upcstudios.tania.braccioapp3;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class ControlActivity extends AppCompatActivity {

    private boolean close;
    GlobalClasses globalClasses;
    Button joggingButton, programButton;
    Handler controlHandler;

    private Thread readBluetoothThread;
    public boolean startedThread = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globalClasses = (GlobalClasses) getApplication();
        setContentView(R.layout.activity_control);

        globalClasses.MyBluetooth.VerifyBT();

        joggingButton = findViewById(R.id.joggingButton);
        programButton = findViewById(R.id.programButton);

        readBluetoothThread = new Thread(new Runnable() {

            @Override
            public void run() {
                System.out.println("Begin BT read thread.");
                try {
                    while (true) {

                        String newMessage = null;

                        synchronized (BluetoothClass.messageLock) {
                            BluetoothClass.messageLock.wait();
                            if (startedThread) newMessage = BluetoothClass.messageQueue.poll();
                        }

                        // SAFE ZONE
                        if (startedThread)
                        {
                            System.out.println("BT MESSAGE: " + newMessage);
                            ArrayList<String> array = new ArrayList<String>();
                            array.add("Echo1: "+newMessage);
                            globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_SENDTO, BluetoothClass.Communications.COM_SERIAL, BluetoothClass.Joints.J_NULL, array);
                            controlHandler.obtainMessage(0, newMessage.length(),-1, newMessage).sendToTarget();
                        }
                        //receivedText.setText("Dato:" + newMessage);
                        // SAFE ZONE

                    }
                } catch (InterruptedException e) {
                    System.out.println("Interrupted BT read thread.");
                }
            }
        });

        controlHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what == 0) {
                    String readMessage = (String) msg.obj;
                    // If something is need to do with the message, write it here
                }
            }
        };

        joggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close = false;
                Intent intent = new Intent(ControlActivity.this, JoggingActivity.class);
                startActivity(intent);
            }
        });

        programButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close = false;
                Intent intent = new Intent(ControlActivity.this, ProgramActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (readBluetoothThread.getState() == Thread.State.NEW) readBluetoothThread.start();
        else startedThread = true;

        close = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // The connection will be interrupted when going to the MainActivity
        if (close) {
            ArrayList<String> array = new ArrayList<String>();
            globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_DISCONNECT, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
            globalClasses.MyBluetooth.CloseConnection();
            readBluetoothThread.interrupt();
        }
        else startedThread = false;
    }
}
