package com.upcstudios.tania.braccioapp3;

import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class JoggingActivity extends AppCompatActivity {
    GlobalClasses globalClasses;
    Button testJoggingButton, jogMinusButton1, jogMinusButton2, jogMinusButton3, jogMinusButton4;
    Button jogMinusButton5, jogMinusButton6, jogPlusButton1, jogPlusButton2, jogPlusButton3, jogPlusButton4;
    Button jogPlusButton5, jogPlusButton6;
    TextView receivedText2, textTarget1, textTarget2, textTarget3, textTarget4, textTarget5, textTarget6;
    TextView textCurr1, textCurr2, textCurr3, textCurr4, textCurr5, textCurr6;
    Handler joggingHandler;
    TabHost tabs;
    public enum CURRENT_BUTTON {NULL, BASE_P, BASE_M, SHOULDER_P, SHOULDER_M, ELBOW_P, ELBOW_M,
        WRIST_VER_P, WRIST_VER_M, WRIST_ROT_P, WRIST_ROT_M, GRIPPER_P, GRIPPER_M}
    CURRENT_BUTTON current_button = CURRENT_BUTTON.NULL;

    private Thread readBluetoothThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globalClasses = (GlobalClasses) getApplication();
        setContentView(R.layout.activity_jogging);

        Resources res = getResources();

        testJoggingButton = findViewById(R.id.testJoggingButton);
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

        testJoggingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> array = new ArrayList<String>();
                array.add("Hello from jog");
                globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.SENDTEXT, array);
            }
        });

        jogMinusButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.BASE_M;
                        //globalClasses.MyBluetooth.writeMessage("JOG Base -1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.BASE_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Base 0");
                    }
                }
                return true;
            }
        });

        jogPlusButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.BASE_P;
                        //globalClasses.MyBluetooth.writeMessage("JOG Base +1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.BASE_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Base 0");
                    }
                }
                return true;
            }
        });

        jogMinusButton2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.SHOULDER_M;
                        //globalClasses.MyBluetooth.writeMessage("JOG Shoulder -1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.SHOULDER_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Shoulder 0");
                    }
                }
                return true;
            }
        });

        jogPlusButton2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.SHOULDER_P;
                        //globalClasses.MyBluetooth.writeMessage("JOG Shoulder +1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.SHOULDER_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Shoulder 0");
                    }
                }
                return true;
            }
        });

        jogMinusButton3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.ELBOW_M;
                        //globalClasses.MyBluetooth.writeMessage("JOG Elbow -1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.ELBOW_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Elbow 0");
                    }
                }
                return true;
            }
        });

        jogPlusButton3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.ELBOW_P;
                        //globalClasses.MyBluetooth.writeMessage("JOG Elbow +1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.ELBOW_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Elbow 0");
                    }
                }
                return true;
            }
        });

        jogMinusButton4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.WRIST_VER_M;
                        //globalClasses.MyBluetooth.writeMessage("JOG Wrist_ver -1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.WRIST_VER_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Wrist_ver 0");
                    }
                }
                return true;
            }
        });

        jogPlusButton4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.WRIST_VER_P;
                        //globalClasses.MyBluetooth.writeMessage("JOG Wrist_ver +1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.WRIST_VER_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Wrist_ver 0");
                    }
                }
                return true;
            }
        });

        jogMinusButton5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.WRIST_ROT_M;
                        //globalClasses.MyBluetooth.writeMessage("JOG Wrist_rot -1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.WRIST_ROT_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Wrist_rot 0");
                    }
                }
                return true;
            }
        });

        jogPlusButton5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.WRIST_ROT_P;
                        //globalClasses.MyBluetooth.writeMessage("JOG Wrist_rot +1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.WRIST_ROT_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Wrist_rot 0");
                    }
                }
                return true;
            }
        });

        jogMinusButton6.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.GRIPPER_M;
                        //globalClasses.MyBluetooth.writeMessage("JOG Gripper -1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.GRIPPER_M) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Gripper 0");
                    }
                }
                return true;
            }
        });

        jogPlusButton6.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (current_button == CURRENT_BUTTON.NULL) {
                        current_button = CURRENT_BUTTON.GRIPPER_P;
                        //globalClasses.MyBluetooth.writeMessage("JOG Gripper +1");
                    }
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    if(current_button == CURRENT_BUTTON.GRIPPER_P) {
                        current_button = CURRENT_BUTTON.NULL;
                        //globalClasses.MyBluetooth.writeMessage("JOG Gripper 0");
                    }
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        readBluetoothThread.start();
        ArrayList<String> array = new ArrayList<String>();
        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.REQUEST, array);
    }

    @Override
    protected void onPause() {
        super.onPause();

        readBluetoothThread.interrupt();
    }
}
