package com.upcstudios.tania.braccioapp3;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ProgramActivity extends AppCompatActivity {
    GlobalClasses globalClasses;
    EditText inputCode;
    TextView receivedText;
    Button programTest, buttonProcess, buttonSend, buttonStop;

    int send_command_pos = 0;
    int max_lines = 20;
    Handler programHandler;
    private Thread readBluetoothThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        globalClasses = (GlobalClasses) getApplication();
        setContentView(R.layout.activity_program);

        inputCode = findViewById(R.id.codeInput);
        receivedText = findViewById(R.id.receivedText);
        programTest = findViewById(R.id.programTest);
        buttonProcess = findViewById(R.id.buttonProcess);
        buttonSend = findViewById(R.id.buttonSend);
        buttonStop = findViewById(R.id.buttonStop);

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
                        programHandler.obtainMessage(0, newMessage.length(),-1, newMessage).sendToTarget();
                        // SAFE ZONE

                    }
                } catch (InterruptedException e) {
                    System.out.println("Interrupted BT read thread.");
                }
            }
        });

        programHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if (msg.what  == 0) {
                    String readMessage = (String) msg.obj;
                    if (readMessage.equals("Free")) {
                        globalClasses.ArduinoFree = true;
                        sendCommand();
                    }
                    //String[] tokens = readMessage.split(" ");
                    /*if (tokens[0].equals("RESPONSE"))
                    {
                        textCurr1.setText(tokens[1]);
                        textCurr2.setText(tokens[2]);
                        textCurr3.setText(tokens[3]);
                        textCurr4.setText(tokens[4]);
                        textCurr5.setText(tokens[5]);
                        textCurr6.setText(tokens[6]);
                    }
                    else*/ receivedText.setText("Data: " + readMessage);
                }
            }
        };

        programTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> array = new ArrayList<>();
                array.add("Program Test");
                globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_SENDTO, BluetoothClass.Communications.COM_SERIAL, BluetoothClass.Joints.J_NULL, array);
            }
        });

        inputCode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                if(keyCode == keyEvent.KEYCODE_ENTER && keyEvent.getAction() == keyEvent.ACTION_DOWN)
                {
                    if(((EditText)view).getLineCount() >= max_lines) return true;
                }

                return false;
            }
        });

        buttonProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (receivedText.getText() != "Data: ") receivedText.setText("Data: ");
                ArrayList<String> temp = new ArrayList<>();
                String codeTokens[] = inputCode.getText().toString().split("\n");

                globalClasses.CommandArray.clear();
                //globalClasses.CommandArray.add("9"); // 9: Program command
                //globalClasses.CommandArray.add("9 0"); // 0: Start

                // Remove empty strings
                for (int i=0; i<codeTokens.length;i++){
                    if(codeTokens[i].length() != 0) temp.add(codeTokens[i]);
                }
                codeTokens = temp.toArray(new String[temp.size()]);

                //receivedText.setText(receivedText.getText() + " " + codeTokens.length);

                for (int i=0; i<codeTokens.length;i++){
                    if (codeTokens[i].endsWith(";")) {
                        codeTokens[i] = codeTokens[i].substring(0,codeTokens[i].length()-1);
                        String argTokens[] = codeTokens[i].split(" ");

                        //receivedText.setText(receivedText.getText() + " " + argTokens.length);
                        
                        temp.clear();

                        // Remove empty strings
                        for (int j=0; j<argTokens.length;j++){
                            if(argTokens[j].length() != 0) temp.add(argTokens[j]);
                        }

                        argTokens = temp.toArray(new String[temp.size()]);

                        //receivedText.setText(receivedText.getText() + " " + argTokens.length);

                        temp.clear();

                        if (argTokens[0].equals("MOV")){
                            if (argTokens[1].equals("M1")){
                                int myNum = 0;

                                try{
                                    myNum = Integer.parseInt(argTokens[2]);
                                    if (myNum >= 0 && myNum <= 180){
                                        //TODO M1
                                        temp.add(argTokens[2]);
                                        globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_BASE, temp));
                                        //System.out.println(argTokens[2]);
                                    }
                                    else{
                                        Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                } catch (NumberFormatException nfe){
                                    Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else if (argTokens[1].equals("M2")){
                                int myNum = 0;

                                try{
                                    myNum = Integer.parseInt(argTokens[2]);
                                    if (myNum >= 15 && myNum <= 165){
                                        //TODO M2
                                        temp.add(argTokens[2]);
                                        globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_SHOULDER, temp));
                                        //System.out.println(argTokens[2]);
                                    }
                                    else{
                                        Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                } catch (NumberFormatException nfe){
                                    Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else if (argTokens[1].equals("M3")){
                                int myNum = 0;

                                try{
                                    myNum = Integer.parseInt(argTokens[2]);
                                    if (myNum >= 0 && myNum <= 180){
                                        //TODO M3
                                        temp.add(argTokens[2]);
                                        globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_ELBOW, temp));
                                        //System.out.println(argTokens[2]);
                                    }
                                    else{
                                        Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                } catch (NumberFormatException nfe){
                                    Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else if (argTokens[1].equals("M4")){
                                int myNum = 0;

                                try{
                                    myNum = Integer.parseInt(argTokens[2]);
                                    if (myNum >= 0 && myNum <= 180){
                                        //TODO M4
                                        temp.add(argTokens[2]);
                                        globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_VER, temp));
                                        //System.out.println(argTokens[2]);
                                    }
                                    else{
                                        Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                } catch (NumberFormatException nfe){
                                    Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else if (argTokens[1].equals("M5")){
                                int myNum = 0;

                                try{
                                    myNum = Integer.parseInt(argTokens[2]);
                                    if (myNum >= 0 && myNum <= 180){
                                        //TODO M5
                                        temp.add(argTokens[2]);
                                        globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_WRIST_ROT, temp));
                                        //System.out.println(argTokens[2]);
                                    }
                                    else{
                                        Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                } catch (NumberFormatException nfe){
                                    Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else if (argTokens[1].equals("M6")){
                                int myNum = 0;

                                try{
                                    myNum = Integer.parseInt(argTokens[2]);
                                    if (myNum >= 10 && myNum <= 73){
                                        //TODO M6
                                        temp.add(argTokens[2]);
                                        globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_GRIPPER, temp));
                                        //System.out.println(argTokens[2]);
                                    }
                                    else{
                                        Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                } catch (NumberFormatException nfe){
                                    Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else if (argTokens[1].startsWith("M") && !argTokens[1].equals("M1") && !argTokens[1].equals("M2") && !argTokens[1].equals("M3") && !argTokens[1].equals("M4") && !argTokens[1].equals("M5") && !argTokens[1].equals("M6")){
                                Toast.makeText(ProgramActivity.this, "No such motor at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else if (argTokens[1].equals("X")){
                                //TODO SET X
                            }
                            else if (argTokens[1].equals("Y")){
                                //TODO SET Y
                            }
                            else if (argTokens[1].equals("Z")){
                                //TODO SET Z
                            }
                            else if (argTokens[1].startsWith("P")){
                                // TODO: Comprobar si es una posicion disponible y si no lo es poner el Toast de que no son validos los argumentos+break
                            }
                            else{
                                Toast.makeText(ProgramActivity.this, "Invalid argument at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        else if (argTokens[0].equals("HCL")){
                            temp.add("1");
                            globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_HAND, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp));
                        }
                        else if (argTokens[0].equals("HOP")){
                            temp.add("0");
                            globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_HAND, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp));
                        }
                        else if (argTokens[0].equals("LOOP")){
                            // TODO LOOP -> desde que linea y cuantas veces
                        }
                        else{
                            Toast.makeText(ProgramActivity.this, "No such command at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    else {
                        Toast.makeText(ProgramActivity.this, "Missing ;", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    if (i == codeTokens.length-1){
                        //globalClasses.CommandArray.add("9 1"); //9: program 1: end
                        globalClasses.Code = inputCode.getText().toString();
                        buttonSend.setEnabled(true);
                    }
                }

                if(globalClasses.CommandArray.size() < codeTokens.length) {
                    globalClasses.CommandArray.clear();
                    if(buttonSend.isEnabled()) buttonSend.setEnabled(false);
                }
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                globalClasses.MyBluetooth.writeMessage(globalClasses.CommandArray.get(send_command_pos));
                globalClasses.ArduinoFree = false;
                send_command_pos++;
                buttonStop.setEnabled(true);
                /*for(int i = 0; i<globalClasses.CommandArray.size();i++){
                    globalClasses.MyBluetooth.writeMessage(globalClasses.CommandArray.get(i));
                }*/
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: send stop!
                buttonStop.setEnabled(false); //TODO: después del free por parar, deshabilitar, no solo con tocar
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        readBluetoothThread.start();
        if(globalClasses.Code != ""){
            inputCode.setText(globalClasses.Code);
        }
        buttonSend.setEnabled(false);
        buttonStop.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        readBluetoothThread.interrupt();
    }

    private void sendCommand(){
        if (send_command_pos < globalClasses.CommandArray.size() && globalClasses.ArduinoFree){
            globalClasses.MyBluetooth.writeMessage(globalClasses.CommandArray.get(send_command_pos));
            globalClasses.ArduinoFree = false;
            send_command_pos++;
        }
        else {
            send_command_pos = 0;
            buttonStop.setEnabled(false);
        }
    }
}
