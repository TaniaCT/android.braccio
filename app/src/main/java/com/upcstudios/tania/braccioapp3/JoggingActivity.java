package com.upcstudios.tania.braccioapp3;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class JoggingActivity extends AppCompatActivity {

    // Variable definition
    GlobalClasses globalClasses; // Variable that contains global variables
    Button buttonJogMinus1;
    Button buttonJogMinus2;
    Button buttonJogMinus3;
    Button buttonJogMinus4;
    Button buttonJogMinus5;
    Button buttonJogMinus6;
    Button buttonJogPlus1;
    Button buttonJogPlus2;
    Button buttonJogPlus3;
    Button buttonJogPlus4;
    Button buttonJogPlus5;
    Button buttonJogPlus6;
    Button buttonSavePosition;
    Button buttonView;
    Button buttonDelete;
    Button buttonJogMinusX;
    Button buttonJogMinusY;
    Button buttonJogMinusZ;
    Button buttonJogPlusX;
    Button buttonJogPlusY;
    Button buttonJogPlusZ;
    TextView textCurr1;
    TextView textCurr2;
    TextView textCurr3;
    TextView textCurr4;
    TextView textCurr5;
    TextView textCurr6;
    TextView textCurrX;
    TextView textCurrY;
    TextView textCurrZ;
    TextView positionSavedText;
    TextView resultPosText;
    EditText positionSelectedText;
    Handler joggingHandler;         // Variable that manages the received data from the Bluetooth module
    TabHost tabs;                   // Variable that contains the tab structure

    public enum CURRENT_BUTTON {NULL, BASE_P, BASE_M, SHOULDER_P, SHOULDER_M, ELBOW_P, ELBOW_M,
        WRIST_VER_P, WRIST_VER_M, WRIST_ROT_P, WRIST_ROT_M, GRIPPER_P, GRIPPER_M, X_P, X_M,
        Y_P, Y_M, Z_P, Z_M}
    CURRENT_BUTTON current_button = CURRENT_BUTTON.NULL;
    ArrayList<String> array = new ArrayList<>();

    private Thread readBluetoothThread; // Variable that is waiting for data to be received and handled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In order to use global variables, is necessary to get a reference to the application class
        globalClasses = (GlobalClasses) getApplication();

        setContentView(R.layout.activity_jogging);

        // Link of local variables to the objects in MainActivity screen
        textCurr1 = findViewById(R.id.textCurr1);
        textCurr2 = findViewById(R.id.textCurr2);
        textCurr3 = findViewById(R.id.textCurr3);
        textCurr4 = findViewById(R.id.textCurr4);
        textCurr5 = findViewById(R.id.textCurr5);
        textCurr6 = findViewById(R.id.textCurr6);
        textCurrX = findViewById(R.id.textCurrX);
        textCurrY = findViewById(R.id.textCurrY);
        textCurrZ = findViewById(R.id.textCurrZ);
        positionSavedText = findViewById(R.id.positionSavedText);
        resultPosText = findViewById(R.id.resultPosText);
        buttonJogMinus1 = findViewById(R.id.buttonJogMinus1);
        buttonJogMinus2 = findViewById(R.id.buttonJogMinus2);
        buttonJogMinus3 = findViewById(R.id.buttonJogMinus3);
        buttonJogMinus4 = findViewById(R.id.buttonJogMinus4);
        buttonJogMinus5 = findViewById(R.id.buttonJogMinus5);
        buttonJogMinus6 = findViewById(R.id.buttonJogMinus6);
        buttonJogPlus1 = findViewById(R.id.buttonJogPlus1);
        buttonJogPlus2 = findViewById(R.id.buttonJogPlus2);
        buttonJogPlus3 = findViewById(R.id.buttonJogPlus3);
        buttonJogPlus4 = findViewById(R.id.buttonJogPlus4);
        buttonJogPlus5 = findViewById(R.id.buttonJogPlus5);
        buttonJogPlus6 = findViewById(R.id.buttonJogPlus6);
        buttonJogMinusX = findViewById(R.id.buttonJogMinusX);
        buttonJogMinusY = findViewById(R.id.buttonJogMinusY);
        buttonJogMinusZ = findViewById(R.id.buttonJogMinusZ);
        buttonJogPlusX = findViewById(R.id.buttonJogPlusX);
        buttonJogPlusY = findViewById(R.id.buttonJogPlusY);
        buttonJogPlusZ = findViewById(R.id.buttonJogPlusZ);
        buttonSavePosition = findViewById(R.id.buttonSavePosition);
        buttonView = findViewById(R.id.buttonView);
        buttonDelete = findViewById(R.id.buttonDelete);
        positionSelectedText = findViewById(R.id.positionSelectedText);
        tabs = findViewById(R.id.joggingTabs);

        //-------- Tab Setup -----
        tabs.setup();

        // Add tab1
        TabHost.TabSpec spec = tabs.newTabSpec("myTab1");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Joint");
        tabs.addTab(spec);

        // Add tab2
        spec = tabs.newTabSpec("myTab2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("XYZ");
        tabs.addTab(spec);

        // Add tab3
        spec = tabs.newTabSpec("myTab3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("Position");
        tabs.addTab(spec);

        tabs.setCurrentTab(0);

        // When the position tab is selected, the saved positions are verified and their names/indexes
        // are shown un positionSavedText
        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                positionSavedText.setText("Saved positions:");
                if("myTab3".equals(s)){
                    resultPosText.setText("");
                    for (int position = 0; position < globalClasses.maxPositions; position++) {
                        if(globalClasses.positions[position*6] != -1){
                            positionSavedText.append(" " + String.valueOf(position));
                        }
                    }
                }
            }
        });
        //------------------------

        readBluetoothThread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Begin BT read thread.");
                try {
                    while (true) {

                        String newMessage;

                        // Thread is waiting until the reception of data is detected. Then, it release
                        // the message received, a come back to waiting state
                        synchronized (BluetoothClass.messageLock) {
                            BluetoothClass.messageLock.wait();
                            newMessage = BluetoothClass.messageQueue.poll();
                        }

                        // SAFE ZONE START: this part of the thread can be modified

                        // The received message is sent to the handler to be processed.
                        joggingHandler.obtainMessage(0, newMessage.length(),-1, newMessage).sendToTarget();
                        // SAFE ZONE END

                    }
                } catch (InterruptedException e) {
                    // In case of having an error while receiving data, this message will be shown.
                    System.out.println("Interrupted BT read thread.");
                }
            }
        });

        joggingHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // When a message is received, the only data processed is
                // the array of current angles sent by the Arduino, which will be displayed
                // in the related textCurrx
                if (msg.what  == 0) {
                    String readMessage = (String) msg.obj;
                    String[] tokens = readMessage.split(" ");
                    if (tokens[0].equals("RESPONSE"))
                    {
                        textCurr1.setText(tokens[1]);
                        textCurr2.setText(tokens[2]);
                        textCurr3.setText(tokens[3]);
                        textCurr4.setText(tokens[4]);
                        textCurr5.setText(tokens[5]);
                        textCurr6.setText(tokens[6]);
                    }
                    //else Do something with the message
                }
            }
        };

        // When pressing this button, the robot will be moved by subtracting a degree to the current
        // angle of the base joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogMinus1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.BASE_M;
                        array.add("1");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_BASE, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.BASE_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_BASE, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by adding a degree to the current
        // angle of the base joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogPlus1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.BASE_P;
                        array.add("2");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_BASE, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.BASE_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_BASE, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by subtracting a degree to the current
        // angle of the shoulder joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogMinus2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.SHOULDER_M;
                        array.add("1");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_SHOULDER, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.SHOULDER_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_SHOULDER, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by adding a degree to the current
        // angle of the shoulder joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogPlus2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.SHOULDER_P;
                        array.add("2");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_SHOULDER, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.SHOULDER_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_SHOULDER, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);;
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by subtracting a degree to the current
        // angle of the elbow joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogMinus3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.ELBOW_M;
                        array.add("1");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_ELBOW, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.ELBOW_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_ELBOW, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by adding a degree to the current
        // angle of the elbow joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogPlus3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.ELBOW_P;
                        array.add("2");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_ELBOW, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.ELBOW_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_ELBOW, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by subtracting a degree to the current
        // angle of the wrist_vertical joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogMinus4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.WRIST_VER_M;
                        array.add("1");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_VER, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.WRIST_VER_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_VER, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by adding a degree to the current
        // angle of the wrist_vertical joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogPlus4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.WRIST_VER_P;
                        array.add("2");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_VER, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.WRIST_VER_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_VER, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by subtracting a degree to the current
        // angle of the wrist_rotate joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogMinus5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.WRIST_ROT_M;
                        array.add("1");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_ROT, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.WRIST_ROT_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_ROT, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by adding a degree to the current
        // angle of the wrist_rotate joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogPlus5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.WRIST_ROT_P;
                        array.add("2");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_ROT, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.WRIST_ROT_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_ROT, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by subtracting a degree to the current
        // angle of the gripper joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogMinus6.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.GRIPPER_M;
                        array.add("1");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_GRIPPER, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.GRIPPER_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_GRIPPER, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved by adding a degree to the current
        // angle of the gripper joint every cycle of the Arduino's loop, until it reaches the limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogPlus6.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.GRIPPER_P;
                        array.add("2");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_GRIPPER, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.GRIPPER_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_GRIPPER, array);
                        array.clear();
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved along the X axis having into account
        // the negative direction, until the robot reaches its limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogMinusX.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.X_M;
                        array.add("1");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_X, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.X_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_X, array);
                        array.clear();
                        //globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved along the X axis having into account
        // the positive direction, until the robot reaches its limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogPlusX.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.X_P;
                        array.add("2");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_X, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.X_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_X, array);
                        array.clear();
                        //globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved along the Y axis having into account
        // the negative direction, until the robot reaches its limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogMinusY.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.Y_M;
                        array.add("1");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_Y, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.Y_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_Y, array);
                        array.clear();
                        //globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved along the Y axis having into account
        // the positive direction, until the robot reaches its limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogPlusY.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.Y_P;
                        array.add("2");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_Y, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.Y_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_Y, array);
                        array.clear();
                        //globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved along the Z axis having into account
        // the negative direction, until the robot reaches its limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogMinusZ.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.Z_M;
                        array.add("1");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_Z, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.Z_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_Z, array);
                        array.clear();
                        //globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // When pressing this button, the robot will be moved along the Z axis having into account
        // the positive direction, until the robot reaches its limit.
        // When releasing this button, the robot will stop the movement if it is moving.
        buttonJogPlusZ.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(!array.isEmpty()) array.clear();
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.Z_P;
                        array.add("2");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_Z, array);
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.Z_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        array.add("0");
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_JOGGING, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_Z, array);
                        array.clear();
                        //globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        // By pressing this button, the current joint positions will be saved in the indicated index
        // into the postions array and into the Arduino's array
        buttonSavePosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if a index has been selected
                if(positionSelectedText.getText().length() == 0){
                    Toast.makeText(JoggingActivity.this, "Select a name for the current position" , Toast.LENGTH_SHORT).show();
                }
                else {
                    // Check if the selected index is in the available range
                    int position = (Integer.parseInt(positionSelectedText.getText().toString()));
                    if(position >= 0 && position <= globalClasses.maxPositions-1){
                        position = position*6;

                        int i;
                        // There are 6 joint angles for each position
                        // Save position in the position array
                        for (i = 0; i < 6;i++){
                            if (i == 0) globalClasses.positions[position+i] = Integer.parseInt(textCurr1.getText().toString());
                            else if (i == 1) globalClasses.positions[position+i] = Integer.parseInt(textCurr2.getText().toString());
                            else if (i == 2) globalClasses.positions[position+i] = Integer.parseInt(textCurr3.getText().toString());
                            else if (i == 3) globalClasses.positions[position+i] = Integer.parseInt(textCurr4.getText().toString());
                            else if (i == 4) globalClasses.positions[position+i] = Integer.parseInt(textCurr5.getText().toString());
                            else if (i == 5) globalClasses.positions[position+i] = Integer.parseInt(textCurr6.getText().toString());
                        }

                        // Save position in Arduino
                        array.clear();
                        array.add("0");
                        array.add(positionSelectedText.getText().toString());
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_SAVEPOS, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                        Toast.makeText(JoggingActivity.this, "Position saved", Toast.LENGTH_SHORT).show();

                        // Refresh positionSavedText TextEdit
                        resultPosText.setText("");
                        positionSavedText.setText("Saved positions:");
                        for (position = 0; position < globalClasses.maxPositions; position++) {
                            if(globalClasses.positions[position*6] != -1){
                                positionSavedText.append(" " + String.valueOf(position));
                            }
                        }
                    }
                    else {
                        Toast.makeText(JoggingActivity.this, "Choose a position between 0 and " + String.valueOf(globalClasses.maxPositions-1), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // By pressing this button, the content of the selected valid position will be shown
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if a index has been selected
                if(positionSelectedText.getText().length() == 0){
                    Toast.makeText(JoggingActivity.this, "Select a position" , Toast.LENGTH_SHORT).show();
                }
                else {
                    // Check if the selected index is in the available range
                    int position = (Integer.parseInt(positionSelectedText.getText().toString()));
                    if(position >= 0 && position < globalClasses.maxPositions){
                        position = position*6;

                        // Check if the selected valid position is empty (contains -1 values) or has values
                        if (globalClasses.positions[position] == -1) resultPosText.setText("Empty position");
                        else {
                            resultPosText.setText("");
                            for (int i = 0; i < 6;i++){
                                resultPosText.append(String.valueOf(globalClasses.positions[position+i]));
                                if (i != 5) resultPosText.append(" ");
                            }
                        }
                    }
                    else {
                        Toast.makeText(JoggingActivity.this, "Choose a position between 0 and " + String.valueOf(globalClasses.maxPositions-1), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // By pressing this button, the selected valid position will be removed from the
        // positions array and from the Arduino
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if a index has been selected
                if(positionSelectedText.getText().length() == 0){
                    Toast.makeText(JoggingActivity.this, "Select a position" , Toast.LENGTH_SHORT).show();
                }
                else {
                    // Check if the selected index is in the available range
                    int position = (Integer.parseInt(positionSelectedText.getText().toString()));
                    if(position >= 0 && position < globalClasses.maxPositions){
                        position = position*6;

                        // Set of -1 each value of the position selected
                        if (globalClasses.positions[position] != -1) {
                            resultPosText.setText("");
                            for (int i = 0; i < 6; i++) globalClasses.positions[position + i] = -1;
                        }

                        // Delete position from Arduino
                        array.clear();
                        array.add("3");
                        array.add(positionSelectedText.getText().toString());
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_SAVEPOS, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);

                        // Refresh positionSavedText TextEdit
                        resultPosText.setText("");
                        positionSavedText.setText("Saved positions:");
                        for (position = 0; position < globalClasses.maxPositions; position++) {
                            if(globalClasses.positions[position*6] != -1){
                                positionSavedText.append(" " + String.valueOf(position));
                            }
                        }
                    }
                    else {
                        Toast.makeText(JoggingActivity.this, "Choose a position between 0 and " + String.valueOf(globalClasses.maxPositions-1), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Initialization of the JoggingmActivity thread
        readBluetoothThread.start();

        // Request to the Arduino in order to get the current joint angles
        ArrayList<String> array = new ArrayList<String>();
        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // When exiting of this activity, the ProgramActivity Thread will be interrupted
        readBluetoothThread.interrupt();
    }
}
