package com.upcstudios.tania.braccioapp3;

import android.content.res.Resources;
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
    GlobalClasses globalClasses;
    Button joggingTest, jogMinusButton1, jogMinusButton2, jogMinusButton3, jogMinusButton4;
    Button jogMinusButton5, jogMinusButton6, jogPlusButton1, jogPlusButton2, jogPlusButton3, jogPlusButton4;
    Button jogPlusButton5, jogPlusButton6, buttonSavePosition;
    TextView receivedText2, textTarget1, textTarget2, textTarget3, textTarget4, textTarget5, textTarget6;
    TextView textCurr1, textCurr2, textCurr3, textCurr4, textCurr5, textCurr6;
    EditText textSavePos;
    Handler joggingHandler;
    TabHost tabs;
    public enum CURRENT_BUTTON {NULL, BASE_P, BASE_M, SHOULDER_P, SHOULDER_M, ELBOW_P, ELBOW_M,
        WRIST_VER_P, WRIST_VER_M, WRIST_ROT_P, WRIST_ROT_M, GRIPPER_P, GRIPPER_M}
    CURRENT_BUTTON current_button = CURRENT_BUTTON.NULL;
    ArrayList<String> array = new ArrayList<>();

    private Thread readBluetoothThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globalClasses = (GlobalClasses) getApplication();
        setContentView(R.layout.activity_jogging);

        //Resources res = getResources();

        joggingTest = findViewById(R.id.joggingTest);
        receivedText2 = findViewById(R.id.receivedText2);
        textTarget1 = findViewById(R.id.textTarget1);
        textTarget2 = findViewById(R.id.textTarget2);
        textTarget3 = findViewById(R.id.textTarget3);
        textTarget4 = findViewById(R.id.textTarget4);
        textTarget5 = findViewById(R.id.textTarget5);
        textTarget6 = findViewById(R.id.textTarget6);
        textCurr1 = findViewById(R.id.textCurr1);
        textCurr2 = findViewById(R.id.textCurr2);
        textCurr3 = findViewById(R.id.textCurr3);
        textCurr4 = findViewById(R.id.textCurr4);
        textCurr5 = findViewById(R.id.textCurr5);
        textCurr6 = findViewById(R.id.textCurr6);
        jogMinusButton1 = findViewById(R.id.jogMinusButton1);
        jogMinusButton2 = findViewById(R.id.jogMinusButton2);
        jogMinusButton3 = findViewById(R.id.jogMinusButton3);
        jogMinusButton4 = findViewById(R.id.jogMinusButton4);
        jogMinusButton5 = findViewById(R.id.jogMinusButton5);
        jogMinusButton6 = findViewById(R.id.jogMinusButton6);
        jogPlusButton1 = findViewById(R.id.jogPlusButton1);
        jogPlusButton2 = findViewById(R.id.jogPlusButton2);
        jogPlusButton3 = findViewById(R.id.jogPlusButton3);
        jogPlusButton4 = findViewById(R.id.jogPlusButton4);
        jogPlusButton5 = findViewById(R.id.jogPlusButton5);
        jogPlusButton6 = findViewById(R.id.jogPlusButton6);
        buttonSavePosition = findViewById(R.id.buttonSavePosition);
        textSavePos = findViewById(R.id.textSavePos);
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

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                // Add code
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

                        synchronized (BluetoothClass.messageLock) {
                            BluetoothClass.messageLock.wait();
                            newMessage = BluetoothClass.messageQueue.poll();
                        }

                        // SAFE ZONE
                        System.out.println("BT MESSAGE: " + newMessage);
                        //globalClasses.MyBluetooth.writeMessage("Echo2: "+newMessage);
                        joggingHandler.obtainMessage(0, newMessage.length(),-1, newMessage).sendToTarget();
                        // SAFE ZONE

                    }
                } catch (InterruptedException e) {
                    System.out.println("Interrupted BT read thread.");
                }
            }
        });

        joggingHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

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
                    else receivedText2.setText("Data: " + readMessage);
                }
            }
        };

        joggingTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!array.isEmpty()) array.clear();
                array.add("Jogging Test");
                globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_SENDTO, BluetoothClass.Communications.COM_SERIAL, BluetoothClass.Joints.J_NULL, array);
            }
        });

        jogMinusButton1.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogPlusButton1.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogMinusButton2.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogPlusButton2.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);;
                    }
                }
                return true;
            }
        });

        jogMinusButton3.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogPlusButton3.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogMinusButton4.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogPlusButton4.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogMinusButton5.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogPlusButton5.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogMinusButton6.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        jogPlusButton6.setOnTouchListener(new View.OnTouchListener() {
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
                        array.clear(); //  <-- TODO: Para no pasar algo innecesario?
                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
                    }
                }
                return true;
            }
        });

        buttonSavePosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(textSavePos.getText().length() == 0){
                    Toast.makeText(JoggingActivity.this, "Select a name for the current position" , Toast.LENGTH_SHORT).show();
                }
                else {
                    int position = (Integer.parseInt(textSavePos.getText().toString())-1)*6;
                    for (int i = 0; i < 6;i++){
                        if (i == 0) globalClasses.positions[position+i] = Integer.parseInt(textCurr1.getText().toString());
                        else if (i == 1) globalClasses.positions[position+i] = Integer.parseInt(textCurr2.getText().toString());
                        else if (i == 2) globalClasses.positions[position+i] = Integer.parseInt(textCurr3.getText().toString());
                        else if (i == 3) globalClasses.positions[position+i] = Integer.parseInt(textCurr4.getText().toString());
                        else if (i == 4) globalClasses.positions[position+i] = Integer.parseInt(textCurr5.getText().toString());
                        else if (i == 5) globalClasses.positions[position+i] = Integer.parseInt(textCurr6.getText().toString());
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        readBluetoothThread.start();
        ArrayList<String> array = new ArrayList<String>();
        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_REQUEST, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, array);
    }

    @Override
    protected void onPause() {
        super.onPause();

        readBluetoothThread.interrupt();
    }
}
