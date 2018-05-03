package com.upcstudios.tania.braccioapp3;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Variable definition
    private boolean close;       // Variable that manages if the window was closed, for handler issues
    GlobalClasses globalClasses; // Variable that contains global variables
    Button buttonJogging;
    Button buttonProgram;
    Handler controlHandler;      // Variable that manages the received data from the Bluetooth module

    private Thread readBluetoothThread; // Variable that is waiting for data to be received and handled
    public boolean startedThread = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In order to use global variables, is necessary to get a reference to the application class
        globalClasses = (GlobalClasses) getApplication();

        setContentView(R.layout.activity_main);

        // Link of local variables to the objects in MainActivity screen
        buttonJogging = findViewById(R.id.buttonJogging);
        buttonProgram = findViewById(R.id.buttonProgram);

        readBluetoothThread = new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    while (true) {

                        String newMessage = null;

                        // Thread is waiting until the reception of data is detected. Then, it release
                        // the message received, a come back to waiting state
                        synchronized (BluetoothClass.messageLock) {
                            BluetoothClass.messageLock.wait();
                            if (startedThread) newMessage = BluetoothClass.messageQueue.poll();
                        }

                        // SAFE ZONE START: this part of the thread can be modified
                        if (startedThread)
                        {
                            // The received message is sent to the handler to be processed.
                            controlHandler.obtainMessage(0, newMessage.length(),-1, newMessage).sendToTarget();
                        }
                        // SAFE ZONE END

                    }
                } catch (InterruptedException e) {
                    // In case of having an error while receiving data, this message will be shown.
                    System.out.println("Interrupted BT read thread.");
                }
            }
        });

        controlHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // This part is implemented for future issues
                if (msg.what == 0) {
                    String readMessage = (String) msg.obj;
                    // If something is needed to be done with the message, write it here

                }
            }
        };

        // By pressing this button, the JoggingActivity will be opened.
        buttonJogging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close = false;
                Intent intent = new Intent(MainActivity.this, JoggingActivity.class);
                startActivity(intent);
            }
        });

        // By pressing this button, the ProgramActivity will be opened.
        buttonProgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close = false;
                Intent intent = new Intent(MainActivity.this, ProgramActivity.class);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Checks if it is the first time the user has entered to this activity, in order to open
        // the thread just once
        if (readBluetoothThread.getState() == Thread.State.NEW) readBluetoothThread.start();
        // startedThread variable controls that the receiving data is not processed if it current activity is
        // not this one
        else startedThread = true;

        // close variable helps to know if the activity has to be close or not (When navigating to
        // jogging and program activities) in order to no to close the thread until the user comes
        // back to LinkActivity
        close = true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // The connection will be interrupted when going to the LinkActivity
        if (close) {
            ArrayList<String> array = new ArrayList<String>();
            globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_DISCONNECT, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
            globalClasses.MyBluetooth.CloseConnection();
            readBluetoothThread.interrupt();
        }
        // If the opened activity is jogging or program, only the process of received data will be
        // interrupted
        else startedThread = false;
    }
}
