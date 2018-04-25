package com.upcstudios.tania.braccioapp3;

import android.os.Environment;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ProgramActivity extends AppCompatActivity {
    GlobalClasses globalClasses;
    EditText inputCode, fileName;
    Button buttonProcess, buttonSend, buttonStop, buttonSave, buttonImport;

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
        fileName = findViewById(R.id.fileName);
        buttonProcess = findViewById(R.id.buttonProcess);
        buttonSend = findViewById(R.id.buttonSend);
        buttonStop = findViewById(R.id.buttonStop);
        buttonSave = findViewById(R.id.buttonSave);
        buttonImport = findViewById(R.id.buttonImport);

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
                    if (readMessage.equals("Free") && buttonStop.isEnabled()) {
                        sendCommand();
                    }
                }
            }
        };

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
                ArrayList<String> temp = new ArrayList<>();
                String codeTokens[] = inputCode.getText().toString().split("\n");

                globalClasses.CommandArray.clear();

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

                            if (argTokens[1].startsWith("M")){
                                if (argTokens[1].equals("M1")){
                                    int myNum = 0;

                                    try{
                                        myNum = Integer.parseInt(argTokens[2]);
                                        if (myNum >= 0 && myNum <= 180){
                                            //TODO M1
                                            temp.add(argTokens[2]);
                                            globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_BASE, temp));
                                        }
                                        else{
                                            Toast.makeText(ProgramActivity.this, "Invalid angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    } catch (NumberFormatException nfe){
                                        Toast.makeText(ProgramActivity.this, "Missing angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
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
                                        }
                                        else{
                                            Toast.makeText(ProgramActivity.this, "Invalid angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    } catch (NumberFormatException nfe){
                                        Toast.makeText(ProgramActivity.this, "Missing angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
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
                                        }
                                        else{
                                            Toast.makeText(ProgramActivity.this, "Invalid angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    } catch (NumberFormatException nfe){
                                        Toast.makeText(ProgramActivity.this, "Missing angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
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
                                        }
                                        else{
                                            Toast.makeText(ProgramActivity.this, "Invalid angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    } catch (NumberFormatException nfe){
                                        Toast.makeText(ProgramActivity.this, "Missing angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
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
                                        }
                                        else{
                                            Toast.makeText(ProgramActivity.this, "Invalid angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    } catch (NumberFormatException nfe){
                                        Toast.makeText(ProgramActivity.this, "Missing angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                                else if (argTokens[1].equals("M6")){
                                    int myNum = 0;

                                    try{
                                        myNum = Integer.parseInt(argTokens[2]);
                                        if (myNum >= 10 && myNum <= 65){
                                            //TODO M6
                                            temp.add(argTokens[2]);
                                            globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_GRIPPER, temp));
                                        }
                                        else{
                                            Toast.makeText(ProgramActivity.this, "Invalid angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    } catch (NumberFormatException nfe){
                                        Toast.makeText(ProgramActivity.this, "Missing angle at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                                else {
                                    Toast.makeText(ProgramActivity.this, "No such motor at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
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
                                int myNum = 0;
                                try {
                                    myNum = Integer.parseInt(argTokens[1].substring(1));
                                    if (myNum >= 0 && myNum < globalClasses.maxPositions){
                                        int position;
                                        for (position = 0; position < 6; position++){
                                            if (globalClasses.positions[myNum*6+position] == -1) break;
                                        }
                                        if (position < 6) Toast.makeText(ProgramActivity.this, "The selected position at line " + String.valueOf(i+1) + " does not exist", Toast.LENGTH_SHORT).show();
                                        else {
                                            temp.add(argTokens[1].substring(0,1));
                                            temp.add(argTokens[1].substring(1));
                                            globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp));
                                        }
                                    }
                                } catch (NumberFormatException nfe){
                                    Toast.makeText(ProgramActivity.this, "Insert a valid position at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                }
                                // TODO: Comprobar si es una posicion disponible y si no lo es poner el Toast de que no son validos los argumentos+break
                            }
                            else{
                                Toast.makeText(ProgramActivity.this, "The second argument does not match with the command at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
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
                }

                if(globalClasses.CommandArray.size() < codeTokens.length) {
                    globalClasses.CommandArray.clear();
                    if(buttonSend.isEnabled()) {
                        buttonSend.setEnabled(false);
                        buttonStop.setEnabled(false);
                    }
                }
                else {
                    globalClasses.Code = inputCode.getText().toString();
                    buttonSend.setEnabled(true);
                }
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_command_pos = 0;
                globalClasses.MyBluetooth.writeMessage(globalClasses.CommandArray.get(send_command_pos));
                send_command_pos++;
                buttonSend.setEnabled(false);
                buttonStop.setEnabled(true);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> temp = new ArrayList<>();
                globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_STOP, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp);
                Toast.makeText(ProgramActivity.this, "Program stopped at line " + String.valueOf(send_command_pos), Toast.LENGTH_SHORT).show();
                send_command_pos = 0;
                buttonSend.setEnabled(true);
                buttonStop.setEnabled(false);
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(inputCode.getText().length() == 0) Toast.makeText(ProgramActivity.this, "Insert some code.", Toast.LENGTH_SHORT).show();
                else {
                    if (!buttonSend.isEnabled()) Toast.makeText(ProgramActivity.this, "Verify your code.", Toast.LENGTH_SHORT).show();
                    else {
                        if (fileName.getText().length() == 0) Toast.makeText(ProgramActivity.this, "Insert a file name.", Toast.LENGTH_SHORT).show();
                        else {
                            boolean sdCardPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                            if(sdCardPresent){
                                try {
                                    File root = new File(Environment.getExternalStorageDirectory(), "Codes");
                                    if(!root.exists()) root.mkdirs();
                                    File gpxfile = new File(root, fileName.getText().toString()+".txt");
                                    FileWriter writer = new FileWriter(gpxfile);
                                    writer.append(inputCode.getText().toString());
                                    writer.append("\nEND;\n");

                                    for (int position = 0; position < globalClasses.maxPositions; position++) {
                                        for(int i = 0; i < 6; i++){
                                            if(globalClasses.positions[position*6+i] == -1) break;
                                            else {
                                                if (i == 0) writer.append(String.valueOf(position) + "+");
                                                writer.append(String.valueOf(globalClasses.positions[position * 6 + i]));
                                                if (i != 5) writer.append("+");
                                                else {
                                                    if (position > 0 && position < globalClasses.maxPositions-1) writer.append("\n");
                                                }
                                            }
                                        }
                                    }

                                    writer.flush();
                                    writer.close();
                                    Toast.makeText(ProgramActivity.this, "Program saved in" + root.getPath(), Toast.LENGTH_SHORT).show();
                                } catch (IOException e){
                                    Toast.makeText(ProgramActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(ProgramActivity.this, "SD Card not available", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }
        });

        buttonImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fileName.getText().length() == 0) Toast.makeText(ProgramActivity.this, "Insert a valid file", Toast.LENGTH_SHORT).show();
                else {
                    File root = new File(Environment.getExternalStorageDirectory(), "Codes");
                    if(!root.exists()) Toast.makeText(ProgramActivity.this, "The path does not exist", Toast.LENGTH_SHORT).show();
                    else {
                        File gpxfile = new File(root, fileName.getText().toString()+".txt");

                        if(!gpxfile.exists()) Toast.makeText(ProgramActivity.this, "The file does not exist", Toast.LENGTH_SHORT).show();
                        else {
                            StringBuilder text = new StringBuilder();
                            try {
                                BufferedReader br = new BufferedReader(new FileReader(gpxfile));
                                String line;

                                while ((line = br.readLine()) != null){
                                    text.append(line);
                                    text.append("\n");
                                }

                                br.close();

                                String[] tokens = text.toString().split("\nEND;\n");
                                inputCode.setText(tokens[0]);
                                globalClasses.Code = tokens[0];

                                if(tokens.length > 1) {
                                    tokens[1] = tokens[1].substring(0, tokens[1].length()-1).replace("+", " ");
                                    tokens[1] = tokens[1].replace("\n", " ");
                                    String[] angles = tokens[1].split(" ");
                                    Arrays.fill(globalClasses.positions,-1);
                                    try {
                                        int pos = 0;
                                        for(int i = 0; i < angles.length/7; i++){
                                            pos = Integer.parseInt(angles[i*7]);
                                            for (int y = 0; y < 6; y++){
                                                globalClasses.positions[pos*6+y] = Integer.parseInt(angles[i*7+y+1]);
                                            }
                                        }

                                        ArrayList<String> temp = new ArrayList<>();

                                        // Delete all positions in Arduino
                                        temp.add("2");
                                        globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_SAVEPOS, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp);

                                        //Save imported positions
                                        for (int position = 0; position < globalClasses.maxPositions; position++) {
                                            if(globalClasses.positions[position*6] != -1){
                                                temp.clear();
                                                temp.add("1");
                                                temp.add(String.valueOf(position));
                                                String posAngles = "";
                                                for (int i = 0; i < 6; i++) posAngles = posAngles.concat(" " + String.valueOf(globalClasses.positions[position*6+i]));
                                                temp.add(posAngles);
                                                globalClasses.MyBluetooth.writeMessage(BluetoothClass.Commands.C_SAVEPOS, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp);
                                            }
                                        }

                                        Toast.makeText(ProgramActivity.this, "New positions saved", Toast.LENGTH_SHORT).show();
                                    } catch (NumberFormatException e){
                                        Toast.makeText(ProgramActivity.this, "Error while loading positions", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } catch (IOException e){
                                Toast.makeText(ProgramActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
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
        if (send_command_pos < globalClasses.CommandArray.size()){
            globalClasses.MyBluetooth.writeMessage(globalClasses.CommandArray.get(send_command_pos));
            send_command_pos++;
        }
        else {
            send_command_pos = 0;
            buttonSend.setEnabled(true);
            buttonStop.setEnabled(false);
        }
    }
}
