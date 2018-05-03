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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class ProgramActivity extends AppCompatActivity {

    // Variable definition
    GlobalClasses globalClasses;                // Variable that contains global variables
    EditText textCodeInput;
    EditText textFileName;
    Button buttonProcess;
    Button buttonSend;
    Button buttonStop;
    Button buttonSave;
    Button buttonImport;

    int send_command_pos = 0;                   // Index of the processed commands to be sent
    int max_lines = 100;                        // Variable that sets the maximum lines of code allowed to be inserted.
    Handler programHandler;                     // Variable that manages the received data from the Bluetooth module
    private Thread readBluetoothThread;         // Variable that is waiting for data to be received and handled

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // In order to use global variables, is necessary to get a reference to the application class
        globalClasses = (GlobalClasses) getApplication();


        setContentView(R.layout.activity_program);

        // Link of local variables to the objects in ProgramActivity screen
        textCodeInput = findViewById(R.id.textCodeInput);
        textFileName = findViewById(R.id.textFileName);
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

                        // Thread is waiting until the reception of data is detected. Then, it release
                        // the message received, a come back to waiting state
                        synchronized (BluetoothClass.messageLock) {
                            BluetoothClass.messageLock.wait();
                            newMessage = BluetoothClass.messageQueue.poll();
                        }

                        // SAFE ZONE START: this part of the thread can be modified

                        // The received message is sent to the handler to be processed.
                        programHandler.obtainMessage(0, newMessage.length(),-1, newMessage).sendToTarget();

                        // SAFE ZONE END

                    }
                } catch (InterruptedException e) {
                    // In case of having an error while receiving data, this message will be shown.
                    System.out.println("Interrupted BT read thread.");
                }
            }
        });

        programHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                // When a massage is received, free flag (indicated the robot has ended the last command)
                // is checked in order to sent the next command
                if (msg.what  == 0) {
                    String readMessage = (String) msg.obj;
                    if (readMessage.equals("Free") && buttonStop.isEnabled()) {
                        sendCommand();
                    }
                }
            }
        };

        // This method control that every time the user clicks the textCodeInput, the inserted lines
        // will be checked in order to not to allow more than what is specified.
        // It also disables the buttonSend in case that code text is modified.
        textCodeInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

                buttonSend.setEnabled(false);

                if(keyCode == keyEvent.KEYCODE_ENTER && keyEvent.getAction() == keyEvent.ACTION_DOWN)
                {
                    if(((EditText)view).getLineCount() >= max_lines) return true;
                }
                return false;
            }
        });

        // When this button is pressed, the processing of the inserted code starts.
        buttonProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> temp = new ArrayList<>();

                // First of all, an array, whose elements are each line of the inserted coded, is needed
                // The character that separates each line is a line break
                String codeTokens[] = textCodeInput.getText().toString().split("\n");

                // Clear of the commandArray, in order to saved only the new valid commands
                globalClasses.CommandArray.clear();

                // Remove empty strings of the lines array
                for (int i=0; i<codeTokens.length;i++){
                    if(codeTokens[i].length() != 0) temp.add(codeTokens[i]);
                }
                codeTokens = temp.toArray(new String[temp.size()]);

                // There will be a validation process for each line of code. When the format of a
                // line of code is incorrect, the processing of the code will be stopped and the
                // buttonSend will remain inactive until all the code is verified as correct.
                // For each error found, a message with its description will be shown
                for (int i=0; i<codeTokens.length;i++){

                    // The first check point is if the line stats with ;, which means that is an empty line
                    if (!codeTokens[i].equals(";")){
                        // The second check point is if the code line ends with the ; character
                        if (codeTokens[i].endsWith(";")) {

                            // If the code line ends with the ; character, a new array with the arguments
                            // of the code line can be created
                            codeTokens[i] = codeTokens[i].substring(0,codeTokens[i].length()-1);
                            String argTokens[] = codeTokens[i].split(" ");

                            // Clear of temp arraylist for future usages
                            temp.clear();

                            // Remove empty strings of the arguments array
                            for (int j=0; j<argTokens.length;j++){
                                if(argTokens[j].length() != 0) temp.add(argTokens[j]);
                            }
                            argTokens = temp.toArray(new String[temp.size()]);

                            // Clear of temp arraylist for future usage
                            temp.clear();

                            // Check if the first argument is a valid one
                            if (argTokens[0].equals("MOV")){

                                // MOV command has to have more than an argument
                                if (argTokens.length > 1){

                                    // Check if the second argument is any of the available ones
                                    if (argTokens[1].startsWith("M")){

                                        // If the automatic control movement is by joint, there has to be
                                        // three arguments
                                        if (argTokens.length  == 3){

                                            // Check if the second argument is any of the available ones,
                                            // having into account that it has to be a motor Mx
                                            if (argTokens[1].equals("M1")){
                                                int myNum = 0;

                                                // Check if the third argument is valid (a number in a
                                                // valid range according to the specified motor)
                                                try{
                                                    myNum = Integer.parseInt(argTokens[2]);
                                                    if (myNum >= 0 && myNum <= 180){
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

                                                // Check if the third argument is valid (a number in a
                                                // valid range according to the specified motor)
                                                try{
                                                    myNum = Integer.parseInt(argTokens[2]);
                                                    if (myNum >= 15 && myNum <= 165){
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

                                                // Check if the third argument is valid (a number in a
                                                // valid range according to the specified motor)
                                                try{
                                                    myNum = Integer.parseInt(argTokens[2]);
                                                    if (myNum >= 0 && myNum <= 180){
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

                                                // Check if the third argument is valid (a number in a
                                                // valid range according to the specified motor)
                                                try{
                                                    myNum = Integer.parseInt(argTokens[2]);
                                                    if (myNum >= 0 && myNum <= 180){
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

                                                // Check if the third argument is valid (a number in a
                                                // valid range according to the specified motor)
                                                try{
                                                    myNum = Integer.parseInt(argTokens[2]);
                                                    if (myNum >= 0 && myNum <= 180){
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

                                                // Check if the third argument is valid (a number in a
                                                // valid range according to the specified motor)
                                                try{
                                                    myNum = Integer.parseInt(argTokens[2]);
                                                    if (myNum >= 10 && myNum <= 65){
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
                                        else if (argTokens.length > 3) {
                                            Toast.makeText(ProgramActivity.this, "Too many arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                        else {
                                            Toast.makeText(ProgramActivity.this, "Missing arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }
                                    else if (argTokens[1].equals("X")){

                                        // If the automatic control movement is by coordinate axis X,
                                        // there has to be three arguments
                                        if (argTokens.length == 3){
                                            int myNum = 0;

                                            // Check if the third argument is valid (it has to be a number)
                                            try{
                                                myNum = Integer.parseInt(argTokens[2]);
                                                //TODO: Limit the values
                                                temp.add(argTokens[1]);
                                                temp.add(argTokens[2]);
                                                globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp));
                                            } catch (NumberFormatException nfe){
                                                Toast.makeText(ProgramActivity.this, "Missing value at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                                break;
                                            }
                                        }
                                        else if (argTokens.length > 3){
                                            Toast.makeText(ProgramActivity.this, "Too many arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                        else {
                                            Toast.makeText(ProgramActivity.this, "Missing arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }
                                    else if (argTokens[1].equals("Y")){

                                        // If the automatic control movement is by coordinate axis Y,
                                        // there has to be three arguments
                                        if (argTokens.length == 3){
                                            int myNum = 0;

                                            // Check if the third argument is valid (it has to be a number)
                                            try{
                                                myNum = Integer.parseInt(argTokens[2]);
                                                //TODO: Limit the values
                                                temp.add(argTokens[1]);
                                                temp.add(argTokens[2]);
                                                globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp));
                                            } catch (NumberFormatException nfe){
                                                Toast.makeText(ProgramActivity.this, "Missing value at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                                break;
                                            }
                                        }
                                        else if (argTokens.length > 3){
                                            Toast.makeText(ProgramActivity.this, "Too many arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                        else {
                                            Toast.makeText(ProgramActivity.this, "Missing arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }
                                    else if (argTokens[1].equals("Z")){

                                        // If the automatic control movement is by coordinate axis Z,
                                        // there has to be three arguments
                                        if (argTokens.length == 3){
                                            int myNum = 0;

                                            // Check if the third argument is valid (it has to be a number)
                                            try{
                                                myNum = Integer.parseInt(argTokens[2]);
                                                //TODO: Limit the values
                                                temp.add(argTokens[1]);
                                                temp.add(argTokens[2]);
                                                globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp));
                                            } catch (NumberFormatException nfe){
                                                Toast.makeText(ProgramActivity.this, "Missing value at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                                break;
                                            }
                                        }
                                        else if (argTokens.length > 3){
                                            Toast.makeText(ProgramActivity.this, "Too many arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                        else {
                                            Toast.makeText(ProgramActivity.this, "Missing arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }
                                    else if (argTokens[1].startsWith("P")){

                                        // If the automatic control movement is by saved joint position,
                                        // there has to be two arguments
                                        if(argTokens.length == 2){
                                            int myNum = 0;

                                            // Check if the second argument is any of the available ones
                                            try {
                                                myNum = Integer.parseInt(argTokens[1].substring(1));
                                                if (myNum >= 0 && myNum < globalClasses.maxPositions){
                                                    int position;
                                                    for (position = 0; position < 6; position++){
                                                        if (globalClasses.positions[myNum*6+position] == -1) break;
                                                    }
                                                    if (position < 6) {
                                                        Toast.makeText(ProgramActivity.this, "The selected position at line " + String.valueOf(i+1) + " does not exist", Toast.LENGTH_SHORT).show();
                                                        break;
                                                    }
                                                    else {
                                                        temp.add(argTokens[1].substring(0,1));
                                                        temp.add(argTokens[1].substring(1));
                                                        globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_MOVE, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp));
                                                    }
                                                }
                                            } catch (NumberFormatException nfe){
                                                Toast.makeText(ProgramActivity.this, "Insert a valid position at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                                break;
                                            }
                                        }
                                        else if (argTokens.length > 2) {
                                            Toast.makeText(ProgramActivity.this, "Too many arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                        else {
                                            Toast.makeText(ProgramActivity.this, "Missing arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                            break;
                                        }
                                    }
                                    else{
                                        Toast.makeText(ProgramActivity.this, "The second argument does not match with the command MOV at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                        break;
                                    }
                                }
                                else {
                                    Toast.makeText(ProgramActivity.this, "Missing arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else if (argTokens[0].equals("HCL")){

                                //Hand close command is a command with a single argument
                                if (argTokens.length < 2){
                                    temp.add("1");
                                    globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_HAND, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp));
                                }
                                else {
                                    Toast.makeText(ProgramActivity.this, "Too many arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else if (argTokens[0].equals("HOP")){

                                //Hand open command is a command with a single argument
                                if (argTokens.length < 2){
                                    temp.add("0");
                                    globalClasses.CommandArray.add(globalClasses.MyBluetooth.ProcessCommands(BluetoothClass.Commands.C_HAND, BluetoothClass.Communications.COM_NULL, BluetoothClass.Joints.J_NULL, temp));
                                }
                                else {
                                    Toast.makeText(ProgramActivity.this, "Too many arguments at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                            else{
                                Toast.makeText(ProgramActivity.this, "No such command at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                                break;
                            }
                        }
                        else {
                            Toast.makeText(ProgramActivity.this, "Missing ; at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    }
                    else {
                        Toast.makeText(ProgramActivity.this, "Insert a command at line " + String.valueOf(i+1), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

                // If all code lines have not been processed, the commandArray will be cleared,
                // and the send and stop buttons will remain inactive.
                if(globalClasses.CommandArray.size() < codeTokens.length) {
                    globalClasses.CommandArray.clear();
                    if(buttonSend.isEnabled()) {
                        buttonSend.setEnabled(false);
                        buttonStop.setEnabled(false);
                    }
                }
                // If all code lines have been processed, the buttonSend will be activated and the
                // inserted code will be saved in order to not to lose it when moving to Main o Link
                // activities.
                else {
                    globalClasses.Code = textCodeInput.getText().toString();
                    buttonSend.setEnabled(true);
                }
            }
        });

        // When this button is pressed, it sets the index of the commandArray to the first one, sends
        // the first processed command, activates the buttonStop, disables the buttonSend and increment
        // by 1 the commandArray index.
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

        // When this button is pressed, it sends the command Stop to the microcontroller, which will end
        // the current movement of the robot. Also, a message with the line where the program has been
        // stopped will be shown. In addiction, the index of the commandArray will be set to be first
        // position, the buttonStop will be disabled and the buttonSend will be activated.
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

        // This button performs the file saving into a local text file
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Check if some code text has been inserted
                if(textCodeInput.getText().length() == 0) Toast.makeText(ProgramActivity.this, "Insert some code.", Toast.LENGTH_SHORT).show();
                else {
                    // Check if the code inserted has been processed, by knowing the state of the buttonSend,
                    // which is active if the a some code has been validated.
                    if (!buttonSend.isEnabled()) Toast.makeText(ProgramActivity.this, "Verify your code.", Toast.LENGTH_SHORT).show();
                    else {
                        // Check if a file name has been inserted
                        if (textFileName.getText().length() == 0) Toast.makeText(ProgramActivity.this, "Insert a file name.", Toast.LENGTH_SHORT).show();
                        else {
                            // Check if a SdCard is present
                            boolean sdCardPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                            if(sdCardPresent){

                                // If the folder Codes does not exist, it will be created.
                                // By having the path of the folder, a file with the indicated name
                                // will be created, if it does not exist, and filled with the current
                                // validated code, with the current saved joint positions, including
                                // their index.
                                try {
                                    File root = new File(Environment.getExternalStorageDirectory(), "Codes");
                                    if(!root.exists()) root.mkdirs();
                                    File gpxfile = new File(root, textFileName.getText().toString()+".txt");
                                    FileWriter writer = new FileWriter(gpxfile);
                                    writer.append(textCodeInput.getText().toString());
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
                                    Toast.makeText(ProgramActivity.this, "Program saved in " + root.getPath(), Toast.LENGTH_SHORT).show();
                                } catch (IOException e){
                                    // In case that this process fails, a message with the error description will be shown.
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

        // This button performs the import of the data contained in a specified file
        buttonImport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if a file name has been inserted
                if (textFileName.getText().length() == 0) Toast.makeText(ProgramActivity.this, "Insert a valid file", Toast.LENGTH_SHORT).show();
                else {
                    // Check if the Codes folder exists
                    File root = new File(Environment.getExternalStorageDirectory(), "Codes");
                    if(!root.exists()) Toast.makeText(ProgramActivity.this, "The path does not exist", Toast.LENGTH_SHORT).show();
                    else {
                        // Check if the indicated file exists
                        File gpxfile = new File(root, textFileName.getText().toString()+".txt");

                        if(!gpxfile.exists()) Toast.makeText(ProgramActivity.this, "The file does not exist", Toast.LENGTH_SHORT).show();
                        else {

                            // If all the filters have been passed, the saved code will be inserted
                            // into the textCodeInput and saved into a global variable and the saved
                            // positions, if they exists, will be loaded into the global positions
                            // array, whose actual data will be deleted.
                            StringBuilder text = new StringBuilder();
                            try {
                                // Save and load the file code
                                BufferedReader br = new BufferedReader(new FileReader(gpxfile));
                                String line;

                                while ((line = br.readLine()) != null){
                                    text.append(line);
                                    text.append("\n");
                                }

                                br.close();

                                String[] tokens = text.toString().split("\nEND;\n");
                                textCodeInput.setText(tokens[0]);
                                globalClasses.Code = tokens[0];

                                // Check if positions there are saved positions
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

                                        //Save imported positions into the positions array and into the Arduino
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

        // Initialization of the ProgramActivity thread
        readBluetoothThread.start();

        // When entering to this activity, the saved code will be loaded onto de textCodeInput and the
        // send and stop buttons will be set disabled.
        if(globalClasses.Code != ""){
            textCodeInput.setText(globalClasses.Code);
        }
        buttonSend.setEnabled(false);
        buttonStop.setEnabled(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // When exiting of this activity, the ProgramActivity Thread will be interrupted
        readBluetoothThread.interrupt();
    }

    // When calling this function, the next processed command will be sent and the index of the command
    // will be incremented by 1. In case of not having next command, the index of the commandArray
    // will be set to be first position, the buttonStop will be disabled and the buttonSend will be activated.
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
